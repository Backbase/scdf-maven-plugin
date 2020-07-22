![](logo.svg)

## Stream Maven plugin
The Stream maven plugin provides an easy and quick way to register Stream apps in a running Stream instance.

Once configured in the project, register an app running:
```bash
mvn stream:register -Premote
```

## How to use in a project

In projects created with a Stream starter as parent (i.e. `stream-task-starter-parent`), the necessary dependencies, plugins and most of the configuration are already included. It's only necessary to configure config specifics for your project.

Start by defining Stream and docker repo urls, and Stream username and password (in case auth is enabled)

```xml
     <properties>
        <stream.dataflow.url>http://localhost:8090</stream.dataflow.url>
        <stream.dataflow.username>myuser</stream.dataflow.username>
        <stream.dataflow.password>mypass</stream.dataflow.password>
        <docker.repository.url>localhost:5000/</docker.repository.url>
    </properties>
```

When building the docker image with Jib you are about to register, you need to activate the profile `include-metadata` (e.g. `mvn package jib:dockerBuild -Pinclude-metadata`), so it will hook the `spring-cloud-app-starter-metadata-maven-plugin` that will read the properties from classes annotated with `@ConfigurationProperties` and export them as an encoded json which will allow us to set them on launch time in the SC Dataflow UI.
You can override the default plugin configuration (and it is recommended) to include here which configuration classes should be filtered presented in the UI, by adding the `sourceTypes` and `names` as in the example below. If skipped, all the application properties (including Spring Boot's autoconfig) will be presented.

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-app-starter-metadata-maven-plugin</artifactId>
                <configuration>
                    <metadataFilter>
                        <names>
                            <filter>bootstrap</filter>
                        </names>
                        <sourceTypes>
                            <filter>com.rbs.coutts.bootstrap.entitlements.config.BootstrapConfigurationProperties</filter>
                        </sourceTypes>
                    </metadataFilter>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

Apart from the encoded json, a metadata jar with these properties will be generated as well. In case you prefer to load the properties as jar from a maven artifactory instead of the embedded json, filter needs to be done by adding `src/main/resources/META-INF/spring-configuration-metadata-whitelist.properties` to the project.

After adding the plugins, add a profile with the desired Stream configuration for the project:

```xml
    <profiles>
        <profile>
            <id>remote</id>
            <build>
                <pluginManagement>
                    <plugins>
                        <plugin>
                            <groupId>com.backbase.stream</groupId>
                            <artifactId>stream-maven-plugin</artifactId>
                            <configuration>
                                <dataflowUrl>${stream.dataflow.url}</dataflowUrl>
                                <dataflowUsername>${stream.dataflow.username}</dataflowUsername>
                                <dataflowPassword>${stream.dataflow.password}</dataflowPassword>
                            </configuration>
                        </plugin>
                    </plugins>
                </pluginManagement>
            </build>
        </profile>
    </profiles>
```

Apart from the above config properties, these are/ can be set as well:

| Name   |      Description      |  Default |
|----------|:-------------:|------:|
| `dataflowUrl` |  Stream (SCDF) url, where app will be registered | `http://localhost:9393` |
| `dataflowUsername` |    optional   |    |
| `dataflowPassword` | optional |     |
| `name` |  App name. Inherited from starter parent | `${project.artifactId}` |
| `force` |  Force registration and replace existing one (if exists). Inherited from starter parent | `true` |
| `appUri` |  Format of URI. Inherited from starter parent | `docker:${jib-maven-plugin.image}:${jib-maven-plugin.tag}` |
| `type` |  App type. Inherited from starter parent | One of `APP, SOURCE, PROCESSOR, SINK, TASK` |
| `embeddedMetadata` |  Embed properties metadata in Docker image. Inherited from starter parent | `true` |
| `mavenMetadata` |  Must be set if `embeddedMetadata` is `false`. Ignored otherwise. Inherited from starter parent | `maven://${project.groupId}:${project.artifactId}:jar:metadata:${project.version}` |
