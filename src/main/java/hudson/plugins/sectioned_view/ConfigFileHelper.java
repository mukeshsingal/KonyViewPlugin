package hudson.plugins.sectioned_view;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.Folder;

import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import hudson.util.DescribableList;
import org.jenkinsci.lib.configprovider.ConfigProvider;
import org.jenkinsci.plugins.configfiles.custom.CustomConfig;
import org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty;
import org.jenkinsci.lib.configprovider.model.Config;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.ranges.Range;
import java.util.Iterator;
import java.io.IOException;
import java.util.*;

public class ConfigFileHelper {

    void createConfigFile(String folderName, String fileId, List<String> content, String stage) throws IOException {
        Item folderObject = getFolderObject(folderName);
        FolderConfigFileProperty folderConfigFilesObject = getConfigPropertyObject(folderObject);
        Collection<Config> availableConfigs = getAvailableConfigs(folderConfigFilesObject);
        createConfig(fileId, stage, content, folderConfigFilesObject, availableConfigs);
    }


    void updateStatusOfHook(String projectName,String fileId, String hookName, String stage, String status) throws IOException{
        /* configFileName is same as hookName */
        Item folderObject = getFolderObject(projectName);
        FolderConfigFileProperty folderConfigFilesObject = getConfigPropertyObject(folderObject);
        Collection<Config> availableConfigs = getAvailableConfigs(folderConfigFilesObject);
        updateStatus(fileId, stage, hookName, status, folderConfigFilesObject, availableConfigs);

    }
    void deleteHookInJson(String projectName,String fileId, String hookName, String stage) throws IOException{
        /* configFileName is same as hookName */
        Item folderObject = getFolderObject(projectName);
        FolderConfigFileProperty folderConfigFilesObject = getConfigPropertyObject(folderObject);
        Collection<Config> availableConfigs = getAvailableConfigs(folderConfigFilesObject);
        deleteHook(fileId, stage, hookName, folderConfigFilesObject, availableConfigs);

    }

    private void deleteHook(String configFileName, String stage, String hookName, FolderConfigFileProperty folderConfigFilesObject, Collection<Config> availableConfigs) throws IOException {
        String olderContent = getConfigFileContent(configFileName, availableConfigs);
        Long creationDate = new Date().getTime();
        String newConfigComments = "This config updated at "+creationDate.toString()+" for hook "+configFileName;
        JSONObject root = new JSONObject(olderContent);

        JSONArray oldHooksProperties  = root.getJSONArray(stage);
        Integer numberOfHooks = oldHooksProperties.length();
        if(numberOfHooks != null){
            Integer index =0;
            while(index < numberOfHooks){
                JSONObject array = oldHooksProperties.getJSONObject(index);

                if((((String) array.get("hookName")).trim()).equals(hookName)){
                    oldHooksProperties.remove(index);
                    break;
                }
                index++;
            }
        }
        folderConfigFilesObject.save(new CustomConfig(configFileName, configFileName, newConfigComments, root.toString(4)));
        System.out.println("Delete: CustomFile "+configFileName+" has been Updated successfully.");
    }


    public void updateStatus(String configFileName, String stage, String hookName, String status, FolderConfigFileProperty folderConfigFilesObject, Collection<Config> availableConfigs) throws IOException {
        String olderContent = getConfigFileContent(configFileName, availableConfigs);
        Long creationDate = new Date().getTime();
        String newConfigComments = "This config updated at "+creationDate.toString()+" for hook "+configFileName;
        JSONObject root = new JSONObject(olderContent);

        JSONArray oldHooksProperties  = root.getJSONArray(stage);
        Integer numberOfHooks = oldHooksProperties.length();
        if(numberOfHooks != null){
            Integer index =0;
            while(index < numberOfHooks){
                JSONObject array = oldHooksProperties.getJSONObject(index);

                if((((String) array.get("hookName")).trim()).equals(hookName)){
                    array.put("status", status);
                    break;
                }
                index++;
            }
        }
        folderConfigFilesObject.save(new CustomConfig(configFileName, configFileName, newConfigComments, root.toString(4)));
        System.out.println("Update: CustomFile "+configFileName+" has been Updated successfully with  Hook, Status");
    }


    /*
    To be able to store devices for the test with Config File Provider,
    we need to get Folder object where we want to store devices list first.
    */
    Item getFolderObject(String folderName) {
        Item folderObject = Jenkins.getInstance().getItem(folderName);
        if(folderObject !=null){
            System.out.println("In getFolder Object Not Null " + folderObject.getDisplayName());
        }
        else{
            System.out.println("In getFolderObject got null");
        }
        return folderObject;
    }

    /* Get Config File Provider property in provided project Folder for storing devices list */
    FolderConfigFileProperty getConfigPropertyObject(Item folderObject) {

        final AbstractFolder<?> folder = AbstractFolder.class.cast(folderObject);
        FolderConfigFileProperty folderConfigFileProperty = folder.getProperties().get(FolderConfigFileProperty.class);

        if(folderConfigFileProperty == null){
            System.out.println("folderConfigFileProperty is null");
        }
        else{
            System.out.println("folderConfigFileProperty is not null");

        }

        return folderConfigFileProperty;
    }

    /* Get all device pools that been created before, for remove step */
    Collection<Config> getAvailableConfigs(FolderConfigFileProperty folderConfigFilesObject) {
        Collection<Config> availableConfigs = null;

        if (folderConfigFilesObject != null) {
             availableConfigs = folderConfigFilesObject.getConfigs();
        }

        if(availableConfigs !=null) {
            System.out.println("getAvailableConfigs not null" + availableConfigs.toString());
        }
        else{
            System.out.println("getAvailableConfigs null");
        }

        return availableConfigs;
    }

    /* Create Config File object of CustomConfig type for provided device list */
    void createConfig(String configFileName, String stage, List<String> content, FolderConfigFileProperty folderConfigFilesObject,Collection<Config> availableConfigs) throws IOException {

        String olderContent = "";
        Long creationDate = new Date().getTime();
        String newConfigComments = "This config created at "+creationDate.toString()+" for hook "+configFileName;
        System.out.println("Creating CustomFile "+configFileName);


        if (isUnique(configFileName, availableConfigs) == true) {
            folderConfigFilesObject.save(new CustomConfig(configFileName, configFileName, newConfigComments, content.toString()));
            System.out.println("CustomFile "+configFileName+"has been created successfully");
        }
        else{
            /*Update the existing code with the data */

            olderContent = getConfigFileContent(configFileName, availableConfigs);

            JSONObject root = new JSONObject(olderContent);
            JSONArray oldHooksProperties= root.getJSONArray(stage);
            Integer numberOfHooks = oldHooksProperties.length();

            if(numberOfHooks != null){
                Integer index =0;
                while(index < numberOfHooks){
                    JSONObject array = oldHooksProperties.getJSONObject(index);

                    Integer oldIndex = content.indexOf(((String) array.get("hookName")).trim());
                    array.put("index",oldIndex.toString());

                    System.out.println(array);
                    index++;
                }
            }
            folderConfigFilesObject.save(new CustomConfig(configFileName, configFileName, newConfigComments, root.toString(4)));
            System.out.println("Indexing : CustomFile "+configFileName+" has been Updated successfully");
        }
    }

    boolean isUnique(String configFileName, Collection<Config> availableConfigs) throws IOException {
        Boolean unique = true;
        String olderContent = "";

        for(Config config : availableConfigs){
            Run<?, ?> build = null;
            FilePath workspace= null;
            ConfigProvider provider = config.getDescriptor();
            List<String> tempFiles = new ArrayList<>();
            tempFiles.add("Mukesh");
            System.out.println("Hooks Config File Are  : "+config.name+ "And Param " + configFileName);
            if((config.name).equals(configFileName)){
                unique = false;
            }
        }
        return unique;
    }
    String getConfigFileContent(String configFileName, Collection<Config> availableConfigs) throws IOException {
        String olderContent = "";

        for(Config config : availableConfigs){
            Run<?, ?> build = null;
            FilePath workspace= null;
            ConfigProvider provider = config.getDescriptor();
            List<String> tempFiles = new ArrayList<>();
            tempFiles.add("Mukesh");
            System.out.println("Hooks Config File Are  : "+config.name+ "And Param " + configFileName);
            if((config.name).equals(configFileName)){
                olderContent = config.getDescriptor().supplyContent(config, build, workspace, TaskListener.NULL,tempFiles);
            }
        }
        return olderContent;
    }
}


