package ca.bc.gov.health.qa.autotest.plr.keycloak.actions;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpClient;
import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpRequest;
import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpRequestBuilder;
import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpResponse;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * TODO (AZ) - doc
 */
public class KeycloakActions
implements AutoCloseable
{
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final Logger LOG              = ExecutionLogManager.getLogger();
    private static final String MASK_INDICATOR   = "*".repeat(8);

    private final SimpleHttpClient client_;
    private final URI              uri_;

    private String accessToken_ = null;

    /**
     * TODO (AZ) - doc
     *
     * @param uri
     *        ???
     */
    public KeycloakActions(URI uri)
    {
        requireNonNull(uri, "Null URI.");
        uri_ = uri;
        LOG.info("URL ({}).", uri_);

        client_ = new SimpleHttpClient();
    }

    /**
     * TODO (AZ) - doc
     *
     * <p>If already closed, invoking this method has no effect.
     */
    @Override
    public void close()
    {
        client_.close();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param username
     *        ???
     *
     * @param password
     *        ???
     *
     * @throws IllegalStateException
     *         if the login fails or the access token cannot be retrieved
     *
     * @throws InterruptedException
     *         if the current thread is interrupted
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    public void login(String username, String password)
    throws InterruptedException,
           IOException
    {
        if (isLoggedIn())
        {
            LOG.warn("Already logged in. Resetting the access token.");
            accessToken_ = null;
        }

        LOG.info("Logging in ({}) ...", username);
        String postBody   = prepareCredentialsContent(username, password);
        String maskedBody = prepareCredentialsContent(username, MASK_INDICATOR);
        SimpleHttpRequest request = new SimpleHttpRequestBuilder()
                .transactionName("Keycloak:Login")
                .uri(uri_.resolve("token"))
                .methodPost()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(postBody, maskedBody)
                .build();
        SimpleHttpResponse response = client_.send(request, true, false);
        SimpleHttpResponse artifactResponse;
        if (response.getStatusCode() == 200)
        {
            JSONObject responseData = new JSONObject(response.getTextResponseBody());
            String accessToken = responseData.optString(ACCESS_TOKEN_KEY);
            if (accessToken.isEmpty())
            {
                // Generate artifact with original response before failing
                SimpleHttpClient.generateResponseArtifacts(response, true);
                throw new IllegalStateException("Access token missing in Keycloak response.");
            }
            LOG.debug("Keycloak token response received (token length {}).", accessToken.length());
            // Mask token for artifact
            responseData.put(ACCESS_TOKEN_KEY, MASK_INDICATOR);
            artifactResponse = createAlteredHttpResponse(response, responseData.toString(2));
            accessToken_ = accessToken;
        }
        else
        {
            // Non-200: capture original for troubleshooting
            SimpleHttpClient.generateResponseArtifacts(response, true);
            String snippet = response.getTextResponseBody();
            if (snippet.length() > 300) snippet = snippet.substring(0, 300) + "...";
            throw new IllegalStateException(
                    "Keycloak login failed (status=" + response.getStatusCode() + ") body=" + snippet + ")");
        }
        // Always generate artifact for successful case
        SimpleHttpClient.generateResponseArtifacts(artifactResponse, false);
        LOG.info("Login successful.");
    }

    /**
     * TODO (AZ) - doc
     */
    public void logout()
    {
        if (isLoggedIn())
        {
            // TODO (AZ) - implement logout.
            LOG.warn("Currently logout is not implemented.");
            accessToken_ = null;
        }
        else
        {
            LOG.warn("Not logged in.");
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String getAccessToken()
    {
        return accessToken_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean isLoggedIn()
    {
        return accessToken_ != null;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param response
     *        ???
     *
     * @param alteredTextResponseBody
     *        ???
     *
     * @return ???
     */
    private static SimpleHttpResponse createAlteredHttpResponse(
            SimpleHttpResponse response,
            String             alteredTextResponseBody)
    {
        return new SimpleHttpResponse(
                response.getUri(),
                response.getStatusCode(),
                response.getHttpVersion(),
                response.getResponseHeaderMap(),
                response.getResponseType(),
                new byte[0],
                alteredTextResponseBody,
                response.getTransactionTiming());
    }

    /**
     * TODO (AZ) - doc
     *
     * @param username
     *        ???
     *
     * @param password
     *        ???
     *
     * @return ???
     */
    private static String prepareCredentialsContent(String username, String password)
    {
        return new StringBuilder("grant_type=client_credentials")
                .append("&client_id=")
                .append(URLEncoder.encode(username, StandardCharsets.UTF_8))
                .append("&client_secret=")
                .append(URLEncoder.encode(password, StandardCharsets.UTF_8))
                .toString();
    }
}
