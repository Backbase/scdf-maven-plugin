package com.backbase.oss.scdf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.config.DataFlowClientProperties;
import org.springframework.cloud.dataflow.rest.util.HttpClientConfigurer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public abstract class AbstractDataFlowMojo extends AbstractMojo {

    private static Log logger = LogFactory.getLog(AbstractDataFlowMojo.class);
    private static final String DEFAULT_REGISTRATION_ID = "default";
    /**
     * Address of the Data Flow Server [default: http://localhost:9393].
     */
    @Parameter(property = "scdf.server.url", defaultValue = "http://localhost:9393")
    protected String dataflowUrl;

    @Parameter(property = "scdf.server.tokenUrl")
    protected String tokenUrl;

    @Parameter(property = "scdf.server.scope")
    protected Set<String> scope;


    @Parameter(property = "scdf.server.clientSecret")
    protected String clientSecret;

    @Parameter(property = "scdf.server.clientRegistrationId")
    protected String clientRegistrationId;

    @Parameter(property = "scdf.server.skipSslValidation", defaultValue = "false")
    protected boolean skipSslValidation;

    /**
     * Username of the Data Flow Server [no default].
     */
    @Parameter(property = "scdf.server.username")
    protected String dataflowUsername;

    /**
     * Password of the Data Flow Server [no default].
     */
    @Parameter(property = "scdf.server.password")
    protected String dataflowPassword;

    public DataFlowTemplate dataFlowTemplate() throws URISyntaxException {
        RestTemplate restTemplate = DataFlowTemplate.prepareRestTemplate(new RestTemplate());
        final HttpClientConfigurer httpClientConfigurer = HttpClientConfigurer.create(new URI(dataflowUrl))
            .skipTlsCertificateVerification(skipSslValidation);

        if (!StringUtils.hasText(clientRegistrationId) && StringUtils.hasText(dataflowUsername) && StringUtils.hasText(dataflowPassword)) {

            httpClientConfigurer.basicAuthCredentials(dataflowUsername, dataflowPassword);
            restTemplate.setRequestFactory(httpClientConfigurer.buildClientHttpRequestFactory());
            logger.info("Configured Basic Authentication for accessing the Data Flow Server");
        } else if(StringUtils.hasText(clientRegistrationId) && StringUtils.hasText(dataflowUsername) && StringUtils.hasText(dataflowPassword)) {

            ClientRegistrationRepository clientRegistrations = clientRegistrationRepository();

            ClientRegistration clientRegistration = clientRegistrations.findByRegistrationId(DEFAULT_REGISTRATION_ID);
            restTemplate.getInterceptors().add(clientCredentialsTokenResolvingInterceptor(clientRegistration,
                clientRegistrations, clientRegistrationId));

            logger.info("Configured OAuth2 Client Credentials for accessing the Data Flow Server");
        } else {
            logger.info("Not configuring security for accessing the Data Flow Server");
        }


        return new DataFlowTemplate(new URI(dataflowUrl), restTemplate);
    }

    public InMemoryClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId(DEFAULT_REGISTRATION_ID)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri(tokenUrl)
            .clientId(clientRegistrationId)
            .clientSecret(clientSecret)
            .scope(scope)
            .build();
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    private void setupAuthentication(DataFlowClientProperties dataFlowClientProperties) {
        DataFlowClientProperties.Authentication authentication;
        if (StringUtils.hasText(clientRegistrationId) && StringUtils.hasText(dataflowUsername) && StringUtils.hasText(dataflowPassword)) {
            authentication = new DataFlowClientProperties.Authentication();
            DataFlowClientProperties.Authentication.Oauth2 oauth2 = new DataFlowClientProperties.Authentication.Oauth2();
            oauth2.setClientRegistrationId(clientRegistrationId);
            oauth2.setUsername(dataflowUsername);
            oauth2.setPassword(dataflowPassword);
            authentication.setOauth2(oauth2);
            dataFlowClientProperties.setAuthentication(authentication);
        } else if (!StringUtils.hasText(clientRegistrationId) && StringUtils.hasText(dataflowUsername) && StringUtils.hasText(dataflowPassword)) {
            authentication = new DataFlowClientProperties.Authentication();
            DataFlowClientProperties.Authentication.Basic oauth2 = new DataFlowClientProperties.Authentication.Basic();
            oauth2.setUsername(dataflowUsername);
            oauth2.setPassword(dataflowPassword);
            authentication.setBasic(authentication.getBasic());
            dataFlowClientProperties.setAuthentication(authentication);
        }
    }

    private ClientHttpRequestInterceptor clientCredentialsTokenResolvingInterceptor(
        ClientRegistration clientRegistration, ClientRegistrationRepository clientRegistrationRepository,
        String clientId) {
        Authentication principal = createAuthentication(clientId);
        OAuth2AuthorizedClientService authorizedClientService = new InMemoryOAuth2AuthorizedClientService(
            clientRegistrationRepository);
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientService);
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials().build();
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId(DEFAULT_REGISTRATION_ID).principal(principal).build();

        return (request, body, execution) -> {
            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
            request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
            return execution.execute(request, body);
        };
    }

    private static Authentication createAuthentication(final String principalName) {
        return new AbstractAuthenticationToken(null) {
            private static final long serialVersionUID = -2038812908189509872L;

            @Override
            public Object getCredentials() {
                return "";
            }

            @Override
            public Object getPrincipal() {
                return principalName;
            }
        };
    }
}
