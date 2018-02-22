package hudson.plugins.sectioned_view;

/* Jenkins utility classes */
import hudson.model.*;
import jenkins.model.Jenkins;
import hudson.Extension;
import hudson.util.EnumConverter;

/* JavaScript Reverse proxy classes */
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/* Dependency plugins classes */
import com.cloudbees.hudson.plugins.folder.Folder;
import com.wangyin.parameter.WHideParameterDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

/* Java utility classes */
import javax.servlet.ServletException;
import java.io.*;
import java.util.*;
import net.sf.json.JSONObject;


public class TextSection extends SectionedViewSection {
    public List<String> hooks;
    List<String> hookList = new ArrayList<>();

    public ConfigFileHelper getConfigFileHelper() {
        return new ConfigFileHelper();
    }

    public List<String> getHookList() {
        return hookList;
    }

    public void setHookList(List<String> hookList) {
        this.hookList = hookList;
    }

    private String text;
    private Style style;


    public TextSection(String name){
       super(name);
    }

    /**
     * @deprecated since 1.8 use {@link #TextSection(String, Width, Positioning, String, Style)} instead
     */
    @Deprecated
    public TextSection(String name, Width width, Positioning alignment, String text) {
        this(name, width, alignment, text, Style.NONE);
    }


    @DataBoundConstructor
    public TextSection(String name, Width width, Positioning alignment, String text, Style style) {
        super(name, width, alignment);
        this.text = text;
        this.style = style;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Style getStyle() {
        return style;
    }

    public boolean hasStyle() {
        return style != Style.NONE;
    }

    @JavaScriptMethod
    public void updateSequence(Integer newIndex, String hookName) throws IOException {
        Integer oldIndex = null;
        List<String> hooks;
        hooks = getHookList();

        System.out.println("Update function is called "+ hooks.toString());
        System.out.println("Got change Index " + newIndex.toString() +" HookName : "+ hookName);


        int index = 0;
        for (String entry : hooks) {
            if(entry.equals(hookName)){
                oldIndex = index;
            }
            index = index +1;
        }

        System.out.println(" Element with Key = " + oldIndex.toString());

        hooks.remove(hookName);
        hooks.add(newIndex, hookName);
        System.out.println("updated list is "+ hooks.toString());
        setHookList(hooks);

        getConfigFileHelper().createConfigFile(getName(),getName(),hooks, "PRE_BUILD");
    }

    @JavaScriptMethod
    public void printSequence(){
        List<String> seq = getHookList();
        System.out.println("Got this info " +seq.toString());
    }


    @Extension
    public static final class DescriptorImpl extends SectionedViewSectionDescriptor {

        @Override
        public SectionedViewSection newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return (SectionedViewSection)req.bindJSON(getClass().getDeclaringClass(), formData);
        }

        @Override
        public String getDisplayName() {
            return "CustomHook";
        }
    }

    public List<String> getHooks(){
        return hooks;
    }

    public Job getJobOf(){
        String name = "";
        Jenkins instance = Jenkins.getInstance();

        List<Item> itm = instance.getAllItems();
        for(Item item : itm){
            if(item instanceof Folder){
                if(item.getDisplayName() == "MukeshSingal"){

                    return (Job) item;
                }

            }
        }
        return null;
    }

    public String getCustomHookPostBuildList(){

        String name = "";
        Jenkins instance = Jenkins.getInstance();

        List<Item> itm = instance.getAllItems();
        for(Item item : itm){
            if(item instanceof Folder){
                if(item.getFullName().trim().equals((getName() +"/Visualizer/Builds/CustomHook/POST_BUILD").trim())){
                    System.out.println("POST_BUILD Folder Content ->");

                    Object[] jobsArr = item.getAllJobs().toArray();

                    for(Object job : jobsArr) {
                        name = name +","+ ((Job) job).getDisplayName();
                        System.out.println("       Job ->"+ name);
                    }
                }
            }
        }
        return name;
    }

    public void printMsg(String functionName, String propertyName, Object msg){
        if(msg == null) {
            System.out.println("\n"+functionName + ": " + propertyName + " = is Null");
        }
        else{
            System.out.println("\n"+functionName + ": " + propertyName + " = " + msg.toString());
        }
    }

    @JavaScriptMethod
    public void updateHookJob(String hookName, String hookStage) throws IOException {
        /* Get hook properties */
        System.out.println("\n\nupdateHookJob: ->");
        String hookProperties = getConfigFileHelper().getHookFromConfigs(getName(), hookName, hookStage);
        printMsg("updateHookJob","hookProperties", hookProperties);

        org.json.JSONObject hookPropertiesInJson = new org.json.JSONObject(hookProperties);
        printMsg("updateHookJob","hookPropertiesInJson", hookPropertiesInJson);

        String propogateBuildStatus = hookPropertiesInJson.getString("propogateBuildStatus");
        printMsg("updateHookJob","propogateBuildStatus", propogateBuildStatus);

        String url = hookPropertiesInJson.getString("hookUrl");
        printMsg("updateHookJob","url", url);


        /* Read hookParameters from Config */
        String parameters = getConfigFileHelper().getParametersOfHookFromConfigs(getName(), hookName, hookStage);
        org.json.JSONObject hookParametersInJson = new org.json.JSONObject(parameters);

        String scriptArgs = hookParametersInJson.getString("SCRIPT_ARGUMENTS");
        printMsg("updateHookJob","scriptArgs", scriptArgs);

        String buildAction = hookParametersInJson.getString("BUILD_ACTION");
        printMsg("updateHookJob","buildAction", buildAction);

        String pipelineStage = hookParametersInJson.getString("PIPELINE_STAGE");
        printMsg("updateHookJob","pipelineStage", pipelineStage);

        /* Parameters descriptions */
        String hookNameDesc             = "Name of Custom Hook. It must start with a letter, only contain letters and numbers and be between 4 and 17 characters long.";
        String buildStepDesc            = "Select one of the following phases where you want to inject custom hook.";
        String newHookNameDesc          = "Optional: Rename Custom Hook to new Custom Hook name.";
        String buildActionDesc          = "Optional: Type of hook you want to run.";
        String scriptArgumentDesc       = "Optional: Specify targets, goal or arguments for Hook. These args will be used while invoking Custon Hook scripts. In Ant - pass args like -DProjectName=ABC In Maven - Specify goals clean install ";
        String propogateBuildStatusDesc           = "Optional: Block until the triggered Hooks finish their builds.";
        String hookFileParameterDesc    = "Optional: Upload custom hook project zip. It must contain ant and maven script at root location.\n Currently only Ant and Maven hooks are being supported by AppFactory.";

        /* Put Hook Properties and Parameter in update job. */
        Jenkins instance = Jenkins.getInstance();
        List<Item> items = instance.getAllItems();

        for(Item item : items){
            if(item instanceof Folder){
                if(item.getFullName().trim().equals((getName() +"/Visualizer/Builds/CustomHook").trim())){
                    Object[] jobsArray = item.getAllJobs().toArray();
                    for(Object job : jobsArray) {
                        if(((Job) job).getDisplayName().equals("_updateCustomHook")) {

                            /* Helpers to create default values in Choice Parameters */
                            StringParameterDefinition defaultBuildActionParameter = new StringParameterDefinition("XYZ", buildAction);
                            String[] buildActionChoices = {"Execute Ant", "Execute Maven"};

                            /*Helper to create pipelineStage*/
                            StringParameterDefinition defaultPipelineStageParameter = new StringParameterDefinition("defaultPipelineStage", pipelineStage);

                            String[] pipelineStageChoices;

                            if(hookStage.equals("PRE_BUILD")){
                                pipelineStageChoices= new String[]{"ANDROID_STAGE", "IOS_STAGE", "IOS_IPA_STAGE", "SPA_STAGE"};
                            }
                            else if (hookStage.equals("POST_BUILD")){
                                pipelineStageChoices = new String[]{"ANDROID_STAGE", "IOS_STAGE", "SPA_STAGE"};
                            }
                            else{
                                pipelineStageChoices = new String[]{"ANDROID_STAGE", "IOS_STAGE"};
                            }

                            /* Create new parameter definitions with hook parameters and properties */
                            WHideParameterDefinition hookNameParameter = new WHideParameterDefinition("HOOK_NAME",hookName,hookNameDesc);

                            WHideParameterDefinition buildStepParameter = new WHideParameterDefinition("BUILD_STEP",hookStage+"_STEP",buildStepDesc);

                            StringParameterDefinition newHookNameParameter = new StringParameterDefinition("NEW_HOOK_NAME", hookName,newHookNameDesc );

                            ChoiceParameterDefinition buildActionParameterWithoutDefaultValue = new ChoiceParameterDefinition( "BUILD_ACTION", buildActionChoices, buildActionDesc);
                            ParameterDefinition buildActionParameter = buildActionParameterWithoutDefaultValue.copyWithDefaultValue(defaultBuildActionParameter.getDefaultParameterValue());

                            ChoiceParameterDefinition pipelineStageParameterWithoutDefaultValue = new ChoiceParameterDefinition( "PIPELINE_STAGE", pipelineStageChoices, buildActionDesc);
                            ParameterDefinition pipelineStageParameter = pipelineStageParameterWithoutDefaultValue.copyWithDefaultValue(defaultPipelineStageParameter.getDefaultParameterValue());

                            StringParameterDefinition scriptArgumentsParameter = new StringParameterDefinition("SCRIPT_ARGUMENTS", scriptArgs, scriptArgumentDesc);

                            FileParameterDefinition hookFileParameter = new FileParameterDefinition("HOOK_ARCHIVE_FILE", hookFileParameterDesc);

                            BooleanParameterDefinition propogateBuildStatusParameter = new BooleanParameterDefinition("PROPAGATE_BUILD_STATUS", Boolean.valueOf(propogateBuildStatus),propogateBuildStatusDesc);

                            ParameterDefinition[] newParameters = {
                                    hookNameParameter,
                                    buildStepParameter,
                                    newHookNameParameter,
                                    buildActionParameter,
                                    pipelineStageParameter,
                                    scriptArgumentsParameter,
                                    hookFileParameter,
                                    propogateBuildStatusParameter
                            };

                            /* Remove old parameters from _updateHookJob*/
                            ((Job) job).removeProperty(ParametersDefinitionProperty.class);
                            /* Add new Parameters to _updateHookJob*/
                            ((Job) job).addProperty(new ParametersDefinitionProperty(newParameters));
                        }
                    }
                }
            }
        }
    }

    @JavaScriptMethod
    public String hookStatus(String state, String hookName){
        String status= "Not Available";
        Jenkins instance = Jenkins.getInstance();

        List<Item> itm = instance.getAllItems();
        for(Item item : itm){
            if(item instanceof Folder){
                if(item.getFullName().trim().equals((getName() +"/Visualizer/Builds/CustomHook/"+state).trim())){
                    Object[] jobsArr = item.getAllJobs().toArray();
                    for(Object job : jobsArr) {
                        if(((Job) job).getDisplayName().equals(hookName)) {
                            status = String.valueOf(((Job) job).isBuildable());
                            System.out.println("       " + hookName +"    isBuildable "+ status);
                        }
                    }
                }
            }
        }
        return status;
    }
    public String getCustomHookPreBuildList(){

        List<String> hooks = new ArrayList<>();
        List<String> OldHookList;
        OldHookList = getHookList();

        int index = 0;
        String name = "";
        Jenkins instance = Jenkins.getInstance();

        List<Item> itm = instance.getAllItems();
        for(Item item : itm){
            if(item instanceof Folder){
                if(item.getFullName().trim().equals((getName() +"/Visualizer/Builds/CustomHook/PRE_BUILD").trim())){
                    System.out.println("PRE_BUILD Folder Content ->");

                    Object[] jobsArr = item.getAllJobs().toArray();

                    for(Object job : jobsArr) {

                        String hookName = ((Job) job).getDisplayName();
                        //UpdateCurrentSequence
                        hooks.add(hookName);
                        index = index +1;

                        name = name +","+ hookName;
                        System.out.println("       Job ->"+ name + String.valueOf(((Job) job).isBuildable()));
                        hookStatus("PRE_BUILD",hookName);
                    }
                }
            }
        }

        if(OldHookList == null){
            setHookList(hooks);
            return String.join(",", hooks);
        }

        if(isHookListValid(OldHookList, hooks)){
            if (OldHookList.size() == hooks.size()){
                return String.join(",", OldHookList);
            }
            else {
                setHookList(hooks);
                return name;
            }
        }
        else{
            setHookList(hooks);
            return String.join(",", hooks);
        }
    }

    public boolean isHookListValid(List<String> oldHookList,List<String> hookList ){
        Boolean status = true;
        for(String hook : hookList){
            if(!oldHookList.contains(hook)){
                status = false;
            }
        }
        return status;
    }


    @JavaScriptMethod
    public void disableHook(String hookName, String stage) throws IOException, ServletException {
        System.out.println("Disabled Hook - " + hookName );
        Item job = Jenkins.getInstance().getItemByFullName(getName() +"/Visualizer/Builds/CustomHook/"+stage+"/"+hookName);

        if(job instanceof hudson.model.FreeStyleProject){
            System.out.println("--Set Disable Invoked for FS--");
            ((FreeStyleProject) job).disable();
            getConfigFileHelper().updateStatusOfHook(getName(),getName(),job.getDisplayName(),stage,"disabled");
        }

        if(job instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob){
            System.out.println("--Set Disable Invoked for pipeline--");
            ((WorkflowJob) job).setDisabled(true);
            getConfigFileHelper().updateStatusOfHook(getName(),getName(),job.getDisplayName(),stage,"disabled");
        }
    }
    @JavaScriptMethod
    public void enableHook(String hookName, String stage) throws IOException, ServletException {
        System.out.println("Enable Hook - " + hookName );
        Item job = Jenkins.getInstance().getItemByFullName(getName() +"/Visualizer/Builds/CustomHook/"+stage+"/"+hookName);

        if(job instanceof hudson.model.FreeStyleProject){
            System.out.println("--Set enable Invoked for FS--");
            ((FreeStyleProject) job).enable();
            getConfigFileHelper().updateStatusOfHook(getName(),getName(),job.getDisplayName(),stage,"enabled");
        }

        if(job instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob){
            System.out.println("--Set enable Invoked for pipeline--");
            ((WorkflowJob) job).setDisabled(false);
            getConfigFileHelper().updateStatusOfHook(getName(),getName(),job.getDisplayName(),stage,"enabled");
        }
    }

    @JavaScriptMethod
    public void deleteHook(String hookName, String stage) throws Throwable {
        System.out.println("Delete  Hook - " + hookName );
        Item job = Jenkins.getInstance().getItemByFullName(getName() +"/Visualizer/Builds/CustomHook/"+stage+"/"+hookName);

        if(job instanceof hudson.model.FreeStyleProject){
            System.out.println("--Set Delete Invoked for FS--");
            job.delete();
            getConfigFileHelper().deleteHookInJson(getName(),getName(),job.getDisplayName(),stage);
        }

        if(job instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob){
            System.out.println("--Set delete Invoked for pipeline--");
            job.delete();
            getConfigFileHelper().deleteHookInJson(getName(),getName(),job.getDisplayName(),stage);
        }
    }

    public String getCustomHookPostTestList(){
        String name = "";
        Jenkins instance = Jenkins.getInstance();

        List<Item> itm = instance.getAllItems();
        for(Item item : itm){
            if(item instanceof Folder){
                if(item.getFullName().trim().equals((getName() +"/Visualizer/Builds/CustomHook/POST_TEST").trim())){
                    System.out.println("POST_TEST Folder Content ->");

                    Object[] jobsArr = item.getAllJobs().toArray();

                    for(Object job : jobsArr) {
                        name = name +","+ ((Job) job).getDisplayName();
                        System.out.println("       Job ->"+ name);
                    }
                }
            }
        }
        return name;
    }

    public String getElement(){
        String element = "<h1>I am An Html Tag</h1>";
        return element;
    }

    /**
     * Constants that control how a Text Section is styled.
     */
    public enum Style {
        NONE("None", ""),
        NOTE("Note", "sectioned-view-note"),
        INFO("Info", "sectioned-view-info"),
        WARN("Warning", "sectioned-view-warning"),
        TIP("Tip", "sectioned-view-tip");

        private final String description;

        private final String cssClass;

        public String getDescription() {
            return description;
        }

        public String getCssClass() {
            return cssClass;
        }

        public String getName() {
            return name();
        }


        Style(String description, String cssClass) {
            this.description = description;
            this.cssClass = cssClass;
        }

        static {
            Stapler.CONVERT_UTILS.register(new EnumConverter(), Style.class);
        }
    }
}
