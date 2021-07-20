package com.backbase.oss.scdf;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.dataflow.core.ApplicationType;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.resource.AppRegistrationResource;

import java.net.URISyntaxException;

/**
 * Registration Mojo that sends a REST request to a Spring Cloud Data Flow to register the application.
 */
@Mojo(name = "register", threadSafe = true)
public class RegisterMojo extends AbstractDataFlowMojo {

    private static final Logger log = LoggerFactory.getLogger(RegisterMojo.class);

    @Parameter(property = "scdf.app.name", defaultValue = "${project.artifactId}")
    protected String name;

    /**
     * Type of application to be registered. Must be one of APP, SOURCE, PROCESSOR, SINK, TASK
     */
    @Parameter(property = "scdf.app.type", required = true)
    protected ApplicationType type;


    /**
     * Force registration of App even if same version exists.
     */
    @Parameter(property = "scdf.app.force", defaultValue = "false")
    protected boolean force;

    /**
     * Default App URL. Defaults to docker image.
     */
    @Parameter(property = "scdf.app.uri",   required = true)
    protected String appUri;

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
            log.info("Skipping execution. No name or type defined for project: {}", project.getName());
            return;
        }

        DataFlowTemplate dataFlowTemplate = null;
        try {
            dataFlowTemplate = super.dataFlowTemplate();
        } catch (URISyntaxException e) {
            log.error("Failed to setup Data Flow connection", e);
            throw new MojoExecutionException("Failed to setup Data Flow connection", e);
        }
        log.info("Registering app: {} with type: {} to Spring Cloud Data Flow server: {}", name, type, super.dataflowUrl);
        AppRegistrationResource register = dataFlowTemplate.appRegistryOperations().register(name, type, appUri, null, force);

        System.out.println(register);

    }


}


