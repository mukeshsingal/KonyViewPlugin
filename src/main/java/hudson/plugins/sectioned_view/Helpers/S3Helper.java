package hudson.plugins.sectioned_view.Helpers;
import java.io.IOException;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;

import hudson.plugins.s3.S3Profile;
import hudson.plugins.s3.ClientHelper;
import hudson.EnvVars;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;

public class S3Helper {

    //private static String bucketName = "afdev04001-builds";
    //private static String keyName    = "Same8/CustomHook/PRE_BUILD_STEP/Mukesh/1/Hook.zip";

    public static void deleteHooksFromS3 (String keyName) throws IOException {

        /* Resolve Environment variables from Jenkins configuration. */
        EnvVars envVars = resolveGlobalEnvironmentVariables();

        String bucketName = envVars.get("S3_BUCKET_NAME");
        String region = envVars.get("S3_BUCKET_REGION");

        AmazonS3Client client = ClientHelper.createClient(
                null,
                null,
                true,
                region,
                Jenkins.getInstance().proxy);

        try {
            client.deleteObject(new DeleteObjectRequest(bucketName, keyName));
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException.");
            System.out.println("Error Message: " + ace.getMessage());
        }catch (Exception e){
            System.out.println("S3 Deletion failed. " + e.getMessage());
        }
        finally {
            System.out.println("S3 Delete Tried!!" + bucketName +"/" + keyName);
        }
    }

    public static EnvVars resolveGlobalEnvironmentVariables(){

        Jenkins instance = Jenkins.getInstance();

        DescribableList<NodeProperty<?>, NodePropertyDescriptor> globalNodeProperties = instance.getGlobalNodeProperties();
        List<EnvironmentVariablesNodeProperty> envVarsNodePropertyList = globalNodeProperties.getAll(EnvironmentVariablesNodeProperty.class);

        EnvironmentVariablesNodeProperty newEnvVarsNodeProperty = null;
        EnvVars envVars = null;

        if ( envVarsNodePropertyList == null || envVarsNodePropertyList.size() == 0 ) {
            newEnvVarsNodeProperty = new hudson.slaves.EnvironmentVariablesNodeProperty();
            globalNodeProperties.add(newEnvVarsNodeProperty);
            envVars = newEnvVarsNodeProperty.getEnvVars();
        } else {
            envVars = envVarsNodePropertyList.get(0).getEnvVars();
        }


        return envVars;
    }
}

