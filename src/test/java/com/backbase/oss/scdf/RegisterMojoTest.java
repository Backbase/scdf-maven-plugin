package com.backbase.oss.scdf;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import com.backbase.oss.scdf.RegisterMojo;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class RegisterMojoTest {

    public static final String TEST_APP_URL = "/apps/source/pollable-children/1.0.0-SNAPSHOT";

    public static final int PORT = 12353;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Test
    public void testRegisterWithoutName() throws MalformedURLException, MojoExecutionException {

        WireMock.stubFor(WireMock.post(TEST_APP_URL).willReturn(aResponse().withStatus(200)));

        MavenProject mavenProject = new MavenProject();
        mavenProject.setBuild(new Build());
        mavenProject.setGroupId("com.backbase.stream");
        mavenProject.setArtifactId("stream-transaction-generate-mock-processor");
        mavenProject.setVersion("1.0.0-SNAPSHOT");

        RegisterMojo registerMojo = new RegisterMojo();
        registerMojo.project = mavenProject;
        registerMojo.dataflowUrl = new URL("http://localhost:" + PORT);
        registerMojo.force = true;
        registerMojo.appUri = "docker:/my-registry/appname:1.0.0-SNAPSHOT";
        registerMojo.mavenMetadata = "maven://com.backbase.stream:appname:jar:metadata:1.0.0-SNAPSHOT";

        registerMojo.execute();

        verify(exactly(0), postRequestedFor(urlEqualTo(TEST_APP_URL)));

    }


    @Test
    public void testRegister() throws MalformedURLException, MojoExecutionException {

        WireMock.stubFor(WireMock.post(TEST_APP_URL).willReturn(aResponse().withStatus(200)));

        MavenProject mavenProject = new MavenProject();
        mavenProject.setBuild(new Build());
        mavenProject.setGroupId("com.backbase.stream");
        mavenProject.setArtifactId("stream-transaction-generate-mock-processor");
        mavenProject.setVersion("1.0.0-SNAPSHOT");

        RegisterMojo registerMojo = new RegisterMojo();
        registerMojo.project = mavenProject;
        registerMojo.name = "pollable-children";
        registerMojo.type = RegisterMojo.Type.SOURCE;
        registerMojo.dataflowUrl = new URL("http://localhost:" + PORT);
        registerMojo.force = true;
        registerMojo.appUri = "docker:/my-registry/appname:1.0.0-SNAPSHOT";
        registerMojo.mavenMetadata = "maven://com.backbase.stream:appname:jar:metadata:1.0.0-SNAPSHOT";
        registerMojo.execute();

        verify(exactly(1), postRequestedFor(urlEqualTo(TEST_APP_URL)));
    }

    @Test
    public void testSkipRegister() throws MojoExecutionException {

        WireMock.stubFor(WireMock.post(TEST_APP_URL).willReturn(aResponse().withStatus(500)));

        MavenProject mavenProject = new MavenProject();
        mavenProject.setBuild(new Build());
        mavenProject.setGroupId("com.backbase.stream");
        mavenProject.setArtifactId("stream-transaction-generate-mock-processor");
        mavenProject.setVersion("1.0.0-SNAPSHOT");

        RegisterMojo registerMojo = new RegisterMojo();
        registerMojo.skip = true;
        registerMojo.name = "pollable-children";
        registerMojo.type = RegisterMojo.Type.SOURCE;
        registerMojo.project = mavenProject;

        registerMojo.execute();
    }
}
