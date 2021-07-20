package com.backbase.oss.scdf;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.cloud.dataflow.core.ApplicationType;

import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class RegisterMojoTest {

    public static final String TEST_APP_URL = "/apps/app/pollable-children";
    public static final String ROOT_RESPONSE = "{\"_links\":{\"dashboard\":{\"href\":\"http://localhost:12353/dashboard\"},\"audit-records\":{\"href\":\"http://localhost:12353/audit-records\"},\"streams/definitions\":{\"href\":\"http://localhost:12353/streams/definitions\"},\"streams/definitions/definition\":{\"href\":\"http://localhost:12353/streams/definitions/{name}\",\"templated\":true},\"streams/validation\":{\"href\":\"http://localhost:12353/streams/validation/{name}\",\"templated\":true},\"runtime/streams\":{\"href\":\"http://localhost:12353/runtime/streams{?names}\",\"templated\":true},\"runtime/streams/{streamNames}\":{\"href\":\"http://localhost:12353/runtime/streams/{streamNames}\",\"templated\":true},\"runtime/apps\":{\"href\":\"http://localhost:12353/runtime/apps\"},\"runtime/apps/{appId}\":{\"href\":\"http://localhost:12353/runtime/apps/{appId}\",\"templated\":true},\"runtime/apps/{appId}/instances\":{\"href\":\"http://localhost:12353/runtime/apps/{appId}/instances\",\"templated\":true},\"runtime/apps/{appId}/instances/{instanceId}\":{\"href\":\"http://localhost:12353/runtime/apps/{appId}/instances/{instanceId}\",\"templated\":true},\"streams/deployments\":{\"href\":\"http://localhost:12353/streams/deployments\"},\"streams/deployments/{name}{?reuse-deployment-properties}\":{\"href\":\"http://localhost:12353/streams/deployments/{name}?reuse-deployment-properties=false\",\"templated\":true},\"streams/deployments/{name}\":{\"href\":\"http://localhost:12353/streams/deployments/{name}\",\"templated\":true},\"streams/deployments/history/{name}\":{\"href\":\"http://localhost:12353/streams/deployments/history/{name}\",\"templated\":true},\"streams/deployments/manifest/{name}/{version}\":{\"href\":\"http://localhost:12353/streams/deployments/manifest/{name}/{version}\",\"templated\":true},\"streams/deployments/platform/list\":{\"href\":\"http://localhost:12353/streams/deployments/platform/list\"},\"streams/deployments/rollback/{name}/{version}\":{\"href\":\"http://localhost:12353/streams/deployments/rollback/{name}/{version}\",\"templated\":true},\"streams/deployments/update/{name}\":{\"href\":\"http://localhost:12353/streams/deployments/update/{name}\",\"templated\":true},\"streams/deployments/deployment\":{\"href\":\"http://localhost:12353/streams/deployments/{name}\",\"templated\":true},\"streams/deployments/scale/{streamName}/{appName}/instances/{count}\":{\"href\":\"http://localhost:12353/streams/deployments/scale/{streamName}/{appName}/instances/{count}\",\"templated\":true},\"streams/logs\":{\"href\":\"http://localhost:12353/streams/logs\"},\"streams/logs/{streamName}\":{\"href\":\"http://localhost:12353/streams/logs/{streamName}\",\"templated\":true},\"streams/logs/{streamName}/{appName}\":{\"href\":\"http://localhost:12353/streams/logs/{streamName}/{appName}\",\"templated\":true},\"tasks/platforms\":{\"href\":\"http://localhost:12353/tasks/platforms\"},\"tasks/definitions\":{\"href\":\"http://localhost:12353/tasks/definitions\"},\"tasks/definitions/definition\":{\"href\":\"http://localhost:12353/tasks/definitions/{name}\",\"templated\":true},\"tasks/executions\":{\"href\":\"http://localhost:12353/tasks/executions\"},\"tasks/executions/name\":{\"href\":\"http://localhost:12353/tasks/executions{?name}\",\"templated\":true},\"tasks/executions/current\":{\"href\":\"http://localhost:12353/tasks/executions/current\"},\"tasks/executions/execution\":{\"href\":\"http://localhost:12353/tasks/executions/{id}\",\"templated\":true},\"tasks/validation\":{\"href\":\"http://localhost:12353/tasks/validation/{name}\",\"templated\":true},\"tasks/logs\":{\"href\":\"http://localhost:12353/tasks/logs/{taskExternalExecutionId}{?platformName}\",\"templated\":true},\"tasks/schedules\":{\"href\":\"http://localhost:12353/tasks/schedules\"},\"tasks/schedules/instances\":{\"href\":\"http://localhost:12353/tasks/schedules/instances/{taskDefinitionName}\",\"templated\":true},\"jobs/executions\":{\"href\":\"http://localhost:12353/jobs/executions\"},\"jobs/executions/name\":{\"href\":\"http://localhost:12353/jobs/executions{?name}\",\"templated\":true},\"jobs/executions/status\":{\"href\":\"http://localhost:12353/jobs/executions{?status}\",\"templated\":true},\"jobs/executions/execution\":{\"href\":\"http://localhost:12353/jobs/executions/{id}\",\"templated\":true},\"jobs/executions/execution/steps\":{\"href\":\"http://localhost:12353/jobs/executions/{jobExecutionId}/steps\",\"templated\":true},\"jobs/executions/execution/steps/step\":{\"href\":\"http://localhost:12353/jobs/executions/{jobExecutionId}/steps/{stepId}\",\"templated\":true},\"jobs/executions/execution/steps/step/progress\":{\"href\":\"http://localhost:12353/jobs/executions/{jobExecutionId}/steps/{stepId}/progress\",\"templated\":true},\"jobs/instances/name\":{\"href\":\"http://localhost:12353/jobs/instances{?name}\",\"templated\":true},\"jobs/instances/instance\":{\"href\":\"http://localhost:12353/jobs/instances/{id}\",\"templated\":true},\"tools/parseTaskTextToGraph\":{\"href\":\"http://localhost:12353/tools\"},\"tools/convertTaskGraphToText\":{\"href\":\"http://localhost:12353/tools\"},\"jobs/thinexecutions\":{\"href\":\"http://localhost:12353/jobs/thinexecutions\"},\"jobs/thinexecutions/name\":{\"href\":\"http://localhost:12353/jobs/thinexecutions{?name}\",\"templated\":true},\"jobs/thinexecutions/jobInstanceId\":{\"href\":\"http://localhost:12353/jobs/thinexecutions{?jobInstanceId}\",\"templated\":true},\"jobs/thinexecutions/taskExecutionId\":{\"href\":\"http://localhost:12353/jobs/thinexecutions{?taskExecutionId}\",\"templated\":true},\"apps\":{\"href\":\"http://localhost:12353/apps\"},\"about\":{\"href\":\"http://localhost:12353/about\"},\"completions/stream\":{\"href\":\"http://localhost:12353/completions/stream{?start,detailLevel}\",\"templated\":true},\"completions/task\":{\"href\":\"http://localhost:12353/completions/task{?start,detailLevel}\",\"templated\":true}},\"api.revision\":14}";
    public static final String ABOUT_RESPONSE = "{\"featureInfo\":{\"analyticsEnabled\":true,\"streamsEnabled\":true,\"tasksEnabled\":true,\"schedulesEnabled\":true,\"monitoringDashboardType\":\"GRAFANA\"},\"versionInfo\":{\"implementation\":{\"name\":\"spring-cloud-dataflow-server\",\"version\":\"2.7.2\"},\"core\":{\"name\":\"Spring Cloud Data Flow Core\",\"version\":\"2.7.2\"},\"dashboard\":{\"name\":\"Spring Cloud Dataflow UI\",\"version\":\"3.0.2\"},\"shell\":{\"name\":\"Spring Cloud Data Flow Shell\",\"version\":\"2.7.2\",\"url\":\"https://repo1.maven.org/maven2/org/springframework/cloud/spring-cloud-dataflow-shell/2.7.2/spring-cloud-dataflow-shell-2.7.2.jar\"}},\"securityInfo\":{\"authenticationEnabled\":false,\"authenticated\":false,\"username\":null,\"roles\":[]},\"runtimeEnvironment\":{\"appDeployer\":{\"deployerImplementationVersion\":\"2.6.2\",\"deployerName\":\"Spring Cloud Skipper Server\",\"deployerSpiVersion\":\"2.6.2\",\"javaVersion\":\"1.8.0_292\",\"platformApiVersion\":\"\",\"platformClientVersion\":\"\",\"platformHostVersion\":\"\",\"platformSpecificInfo\":{\"default\":\"kubernetes\"},\"platformType\":\"Skipper Managed\",\"springBootVersion\":\"2.3.10.RELEASE\",\"springVersion\":\"5.2.14.RELEASE\"},\"taskLaunchers\":[{\"deployerImplementationVersion\":\"2.5.2\",\"deployerName\":\"KubernetesTaskLauncher\",\"deployerSpiVersion\":\"2.5.2\",\"javaVersion\":\"1.8.0_292\",\"platformApiVersion\":\"v1\",\"platformClientVersion\":\"unknown\",\"platformHostVersion\":\"unknown\",\"platformSpecificInfo\":{\"namespace\":\"backbase\",\"master-url\":\"https://172.20.0.1:443/\"},\"platformType\":\"Kubernetes\",\"springBootVersion\":\"2.3.10.RELEASE\",\"springVersion\":\"5.2.14.RELEASE\"}]},\"monitoringDashboardInfo\":{\"url\":\"https://grafana.proto.backbasecloud.com\",\"refreshInterval\":15,\"dashboardType\":\"GRAFANA\",\"source\":\"default-scdf-source\"},\"_links\":{\"self\":{\"href\":\"http://localhost:12353/about\"}}}";
    
    public static final int PORT = 12353;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    private void setupWireMock() {
        WireMock.stubFor(WireMock.get("/").willReturn(okJson(ROOT_RESPONSE).withStatus(200)));
        WireMock.stubFor(WireMock.post(TEST_APP_URL).willReturn(aResponse().withStatus(200)));
        WireMock.stubFor(WireMock.get("/about").willReturn(okJson(ABOUT_RESPONSE).withStatus(200)));
    }

    @Test
    public void testRegisterWithoutName() throws MojoExecutionException, URISyntaxException {

        setupWireMock();

        MavenProject mavenProject = new MavenProject();
        mavenProject.setBuild(new Build());
        mavenProject.setGroupId("com.backbase.oss.scdf");
        mavenProject.setArtifactId("stream-transaction-generate-mock-processor");
        mavenProject.setVersion("1.0.0-SNAPSHOT");

        RegisterMojo registerMojo = new RegisterMojo();
        registerMojo.project = mavenProject;

        registerMojo.dataflowUrl = "http://localhost:" + PORT;
        registerMojo.force = true;
        registerMojo.appUri = "docker:/my-registry/appname:1.0.0-SNAPSHOT";


        registerMojo.execute();

        verify(exactly(0), postRequestedFor(urlEqualTo(TEST_APP_URL)));

    }


    @Test
    public void testRegister() throws MojoExecutionException, URISyntaxException {
        setupWireMock();

        MavenProject mavenProject = new MavenProject();
        mavenProject.setBuild(new Build());
        mavenProject.setGroupId("com.backbase.oss.scdf");
        mavenProject.setArtifactId("stream-transaction-generate-mock-processor");
        mavenProject.setVersion("1.0.0-SNAPSHOT");

        RegisterMojo registerMojo = new RegisterMojo();
        registerMojo.project = mavenProject;
        registerMojo.name = "pollable-children";
        registerMojo.type = ApplicationType.app;
        registerMojo.dataflowUrl = "http://localhost:" + PORT;
        registerMojo.force = true;
        registerMojo.appUri = "docker:/my-registry/appname:1.0.0-SNAPSHOT";
        registerMojo.execute();

        verify(exactly(1), postRequestedFor(urlEqualTo(TEST_APP_URL)));
    }

    @Test
    public void testSkipRegister() throws MojoExecutionException {
        setupWireMock();


        MavenProject mavenProject = new MavenProject();
        mavenProject.setBuild(new Build());
        mavenProject.setGroupId("com.backbase.oss.scdf");
        mavenProject.setArtifactId("stream-transaction-generate-mock-processor");
        mavenProject.setVersion("1.0.0-SNAPSHOT");

        RegisterMojo registerMojo = new RegisterMojo();
        registerMojo.skip = true;
        registerMojo.name = "pollable-children";
        registerMojo.type = ApplicationType.source;
        registerMojo.dataflowUrl = "http://localhost:" + PORT;
        registerMojo.project = mavenProject;

        registerMojo.execute();
    }
}
