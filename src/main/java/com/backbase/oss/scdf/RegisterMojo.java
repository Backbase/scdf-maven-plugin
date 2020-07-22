package com.backbase.oss.scdf;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Registration Mojo that sends a REST request to a Spring Cloud Data Flow to register the application.
 */
@Mojo(name = "register", threadSafe = true)
@Slf4j
public class RegisterMojo extends AbstractMojo {

    @Parameter
    protected String name;

    /**
     * Type of application to be registered. Must be one of APP, SOURCE, PROCESSOR, SINK, TASK
     */
    @Parameter
    protected Type type;

    /**
     * Address of the Data Flow Server [default: http://localhost:9393].
     */
    @Parameter(property = "dataflowUrl", defaultValue = "http://localhost:9393")
    protected URL dataflowUrl;


    /**
     * Force registration of App even if same version exists.
     */
    @Parameter(defaultValue = "true")
    protected boolean force;

    /**
     * Username of the Data Flow Server [no default].
     */
    @Parameter
    protected String dataflowUsername;

    /**
     * Password of the Data Flow Server [no default].
     */
    @Parameter
    protected String dataflowPassword;

    /**
     * Default App URL. Defaults to docker image.
     */
    @Parameter(defaultValue = "docker:${project.artifactId}:${project.version}")
    protected String appUri;

    /**
     * Embed app metadata into docker image.
     */
    @Parameter(defaultValue = "true")
    protected boolean embeddedMetadata;

    /**
     * App Metadata Artifact. When metadata is not embedded in docker image.
     */
    @Parameter(defaultValue = "maven://${project.groupId}:${project.artifactId}:jar:metadata:${project.version}")
    protected String mavenMetadata;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * Skip plugin execution.
     */
    @Parameter(defaultValue = "false")
    protected boolean skip;

    /**
     * Method executed by Maven by invoking mvn stream:register.
     *
     * @throws MojoExecutionException When it fails to register
     */
    public void execute() throws MojoExecutionException {

        if (skip) {
            log.info("Skipping execution for project: {}", project.getName());
            return;
        }

        if (name == null || type == null) {
            log.info("Skipping execution. No name of type defined for project: {}", project.getName());
            return;
        }

        if (embeddedMetadata) {
            log.info("Register app: {} with embedded metadata to: {}", appUri, dataflowUrl);
        } else {
            log.info("Register app: {} with metadata: {} to: {}", appUri, mavenMetadata, dataflowUrl);
        }
        DataOutputStream out;
        HttpURLConnection urlConnection;
        int status;
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("uri", appUri);
            parameters.put("force", Boolean.toString(force));
            if (!embeddedMetadata) {
                parameters.put("metadata-uri", mavenMetadata);
            }

            String paramsString = ParameterStringBuilder.getParamsString(parameters);

            String spec = dataflowUrl.toString() + "/apps"
                + "/" + type.name().toLowerCase()
                + "/" + name
                + "/" + project.getVersion();

            log.debug("Register app {} to: {} with body: \n{}", name, spec, paramsString);

            URL url = new URL(spec);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            out = new DataOutputStream(urlConnection.getOutputStream());

            out.writeBytes(paramsString);
            out.flush();
            out.close();

            status = urlConnection.getResponseCode();

            String content = readResponse(urlConnection, status);

            if (status == 201) {
                log.info("Registered application: {} response: [{}]\n{}", appUri, status, content);
            } else {
                log.warn("Failed to register application: {} response: [{}] \n{}", appUri, status, content);
            }

        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Cannot create data flow url", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read content for error response from server", e);
        }
    }

    /**
     * Read the response from the HTTP Connection.
     *
     * @param urlConnection The URL Connection
     * @param status        THe HTTP Response
     * @return The response as a String
     * @throws IOException The exception if the request failed.
     */
    private String readResponse(HttpURLConnection urlConnection, int status) throws IOException {
        Reader streamReader;
        if (status > 299) {
            streamReader = new InputStreamReader(urlConnection.getErrorStream());
        } else {
            streamReader = new InputStreamReader(urlConnection.getInputStream());
        }

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content.toString();
    }

    /**
     * Type of application to register.
     */
    public enum Type {
        APP, SOURCE, PROCESSOR, SINK, TASK
    }

    /**
     * Convenience method to build request body.
     */
    private static class ParameterStringBuilder {

        static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
        }
    }
}


