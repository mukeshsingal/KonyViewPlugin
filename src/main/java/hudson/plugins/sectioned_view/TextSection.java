/*
 * The MIT License
 *
 * Copyright (c) 2009-2011, Timothy Bingaman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.sectioned_view;

import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;
import hudson.Extension;
import hudson.util.EnumConverter;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import com.cloudbees.hudson.plugins.folder.Folder;

import javax.servlet.ServletException;
import java.io.*;
import java.util.*;


public class TextSection extends SectionedViewSection {
    public List<String> hooks;
    List<String> hookList = new ArrayList<>();

    public List<String> getHookList() {
        return hookList;
    }

    public void setHookList(List<String> hookList) {
        this.hookList = hookList;
    }


    private String text;
    private Style style;



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
    public void updateSequence(Integer newIndex, String hookName){
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
            if(item instanceof com.cloudbees.hudson.plugins.folder.Folder){
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
            if(item instanceof com.cloudbees.hudson.plugins.folder.Folder){
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
    @JavaScriptMethod
    public String hookStatus(String state, String hookName){
        String status= "Not Available";
        Jenkins instance = Jenkins.getInstance();

        List<Item> itm = instance.getAllItems();
        for(Item item : itm){
            if(item instanceof com.cloudbees.hudson.plugins.folder.Folder){
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
            if(item instanceof com.cloudbees.hudson.plugins.folder.Folder){
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
        else{
            if (OldHookList.size() == hooks.size()){
                return String.join(",", OldHookList);
            }
            else {
                setHookList(hooks);
                return name;
            }
        }
    }


    @JavaScriptMethod
    public void disableHook(String hookName, String stage) throws IOException, ServletException {
        System.out.println("Disabled Hook - " + hookName );
        Item job = Jenkins.getInstance().getItemByFullName(getName() +"/Visualizer/Builds/CustomHook/"+stage+"/"+hookName);

        if(job instanceof hudson.model.FreeStyleProject){
            System.out.println("--Set Disable Invoked for FS--");
            ((FreeStyleProject) job).disable();
        }

        if(job instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob){
            System.out.println("--Set Disable Invoked for pipeline--");
            ((WorkflowJob) job).setDisabled(true);
        }
    }

    @JavaScriptMethod
    public void deleteHook(String hookName, String stage) throws Throwable {
        System.out.println("Delete  Hook - " + hookName );
        Item job = Jenkins.getInstance().getItemByFullName(getName() +"/Visualizer/Builds/CustomHook/"+stage+"/"+hookName);

        if(job instanceof hudson.model.FreeStyleProject){
            System.out.println("--Set Delete Invoked for FS--");
            job.delete();
        }

        if(job instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob){
            System.out.println("--Set delete Invoked for pipeline--");
            job.delete();
        }
    }

    public String getCustomHookPostTestList(){
        String name = "";
        Jenkins instance = Jenkins.getInstance();

        List<Item> itm = instance.getAllItems();
        for(Item item : itm){
            if(item instanceof com.cloudbees.hudson.plugins.folder.Folder){
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
