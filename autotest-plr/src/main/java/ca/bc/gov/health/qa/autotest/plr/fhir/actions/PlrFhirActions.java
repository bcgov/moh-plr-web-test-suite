package ca.bc.gov.health.qa.autotest.plr.fhir.actions;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpClient;
import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpRequest;
import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpRequestBuilder;
import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpResponse;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainProviderAccessor;
import ca.bc.gov.health.qa.autotest.plr.keycloak.actions.KeycloakActions;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * TODO (AZ) - doc
 */
public class PlrFhirActions
implements AutoCloseable
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final SimpleHttpClient client_;
    private final URI              uri_;
    private final KeycloakActions  keycloakActions_;

    private String userId_;

    /**
     * TODO (AZ) - doc
     *
     * @param fhirURI
     *        ???
     *
     * @param keycloakURI
     *        ???
     *
     * @param keyStorePath
     *        ???
     *
     * @param keyStorePassword
     *        ???
     */
    public PlrFhirActions(URI fhirURI, URI keycloakURI, Path keyStorePath, char[] keyStorePassword)
    {
        requireNonNull(fhirURI, "Null URI.");
        uri_ = fhirURI;
        LOG.info("URL ({}).", uri_);

        client_          = new SimpleHttpClient(false, keyStorePath, keyStorePassword);
        keycloakActions_ = new KeycloakActions(keycloakURI);
    }

    /**
     * TODO (AZ) - doc
     *
     * <p>If already closed, invoking this method has no effect.
     */
    @Override
    public void close()
    {
        try
        {
            keycloakActions_.close();
        }
        finally
        {
            client_.close();
        }
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
        keycloakActions_.login(username, password);
        userId_ = username;
    }

    /**
     * TODO (AZ) - doc
     */
    public void logout()
    {
        userId_ = null;
        keycloakActions_.logout();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param response
     *        ???
     *
     * @return ???
     */
    public String processMaintainResponse(SimpleHttpResponse response)
    {
        String id;
        if (response.getStatusCode() == 200)
        {
            JSONObject json = new JSONObject(response.getTextResponseBody());
            MaintainProviderAccessor accessor = new MaintainProviderAccessor(json);
            id = accessor.getResourceJson(0, null).getString("id");
        }
        else
        {
            throw new IllegalStateException("FHIR maintain request failed.");
        }
        return id;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param identifier
     *        ???
     *
     * @return ???
     *
     * @throws InterruptedException
     *         if the current thread is interrupted
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    public JSONObject queryOrganizationByIdentifier(String identifier)
    throws InterruptedException,
           IOException
    {
        return queryProviderByIdentifier("Organization", identifier);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param identifierType
     *        ???
     *
     * @param identifierValue
     *        ???
     *
     * @return ???
     *
     * @throws InterruptedException
     *         if the current thread is interrupted
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    public JSONObject queryOrganizationByIdentifier(
            IdentifierType identifierType, String identifierValue)
    throws InterruptedException,
           IOException
    {
        return queryOrganizationByIdentifier(
                identifierType.getSourceSystem() + "|" + identifierValue);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param identifier
     *        ???
     *
     * @return ???
     *
     * @throws InterruptedException
     *         if the current thread is interrupted
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    public JSONObject queryPractitionerByIdentifier(String identifier)
    throws InterruptedException,
           IOException
    {
        return queryProviderByIdentifier("Practitioner", identifier);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param identifierType
     *        ???
     *
     * @param identifierValue
     *        ???
     *
     * @return ???
     *
     * @throws InterruptedException
     *         if the current thread is interrupted
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    public JSONObject queryPractitionerByIdentifier(
            IdentifierType identifierType, String identifierValue)
    throws InterruptedException,
           IOException
    {
        return queryPractitionerByIdentifier(
                identifierType.getSourceSystem() + "|" + identifierValue);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param requestJson
     *        ???
     *
     * @return ???
     *
     * @throws InterruptedException
     *         if the current thread is interrupted
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    public SimpleHttpResponse sendMaintainRequest(JSONObject requestJson)
    throws InterruptedException,
           IOException
    {
        return sendMaintainRequest(requestJson.toString(2));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param requestPayload
     *        ???
     *
     * @return ???
     *
     * @throws InterruptedException
     *         if the current thread is interrupted
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    public SimpleHttpResponse sendMaintainRequest(String requestPayload)
    throws InterruptedException,
           IOException
    {
        verifyLoggedIn();
        SimpleHttpRequest request = createHttpRequestBuilder()
                .transactionName("FHIR:Maintain")
                .uri(uri_.resolve("$maintain"))
                .methodPost()
                .body(requestPayload)
                .build();
        return client_.send(request);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param requestJson
     *        ???
     *
     * @return ???
     *
     * @throws InterruptedException
     *         if the current thread is interrupted
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    public String submitMaintainRequest(JSONObject requestJson)
    throws InterruptedException,
           IOException
    {
        return processMaintainResponse(sendMaintainRequest(requestJson));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param providerType
     *        ???
     *        Organization, Practitioner
     *
     * @param identifier
     *        ???
     *
     * @return ???
     *
     * @throws InterruptedException
     *         if the current thread is interrupted
     *
     * @throws IOException
     *         if an I/O error occurs
     */
    protected JSONObject queryProviderByIdentifier(String providerType, String identifier)
    throws InterruptedException,
           IOException
    {
        verifyLoggedIn();
        SimpleHttpRequest request = createHttpRequestBuilder()
                .transactionName("FHIR:Query" + providerType)
                .uri(uri_.resolve(providerType + "/$entityQuery"))
                .queryParameter("identifier", identifier)
                .build();
        SimpleHttpResponse response = client_.send(request);
        JSONObject responseData;
        if (response.getStatusCode() == 200)
        {
            responseData = new JSONObject(response.getTextResponseBody());
        }
        else
        {
            throw new IllegalStateException(providerType + " FHIR query failed.");
        }
        return responseData;
    }

    /**
     * Creates a new HTTP request builder with standard headers.
     *
     * @return the new HTTP request builder
     */
    private SimpleHttpRequestBuilder createHttpRequestBuilder()
    {
        SimpleHttpRequestBuilder requestBuilder = new SimpleHttpRequestBuilder()
                .header("Authorization", "Bearer " + keycloakActions_.getAccessToken(), true)
                .header("Accept", "application/fhir+json; fhirVersion=4; BCPLRVersion=1")
                .header("userID", userId_);
        return requestBuilder;
    }

    /**
     * TODO (AZ) - doc
     *
     * @throws IllegalStateException
     *         ???
     */
    private void verifyLoggedIn()
    {
        if (!keycloakActions_.isLoggedIn())
        {
            throw new IllegalStateException("Not logged in.");
        }
    }
}
