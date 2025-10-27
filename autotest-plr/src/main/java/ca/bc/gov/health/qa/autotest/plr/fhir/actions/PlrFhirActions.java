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
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainAccessor;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.PlrFhirResourceType;
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
     * Processes a maintain response and extracts the id of the primary resource type requested.
     * The server may return additional related resources (e.g. OrganizationAffiliation) before
     * the primary one; therefore we iterate all bundle entries until we find the target FHIR
     * resource type matching {@code targetType.wire()}.
     *
     * @param response   HTTP response returned by the maintain submission (expected 200)
     * @param targetType resource type whose id should be returned (e.g. FACILITY -> Location)
     * @return id of the created/updated primary resource
     * @throws IllegalStateException if the status code is not 200 or the target resource is missing
     */
    public String processMaintainResponse(SimpleHttpResponse response, PlrFhirResourceType targetType)
    {
        if (response.getStatusCode() != 200) {
            throw new IllegalStateException("FHIR maintain request failed. " + response.getTextResponseBody());
        }
        JSONObject json = new JSONObject(response.getTextResponseBody());
        MaintainAccessor accessor = new MaintainAccessor(json);
        String desiredWireType = targetType.wire();
        String resourceId = null;
        for (int i = 0; i < accessor.getEntryArrayJson().length(); i++) {
            JSONObject resource = accessor.getResourceJson(i, null);
            if (desiredWireType.equals(resource.optString("resourceType"))) {
                resourceId = resource.optString("id", null);
                break;
            }
        }
        if (resourceId == null) {
            throw new IllegalStateException("Maintain response did not contain a " + desiredWireType + " resource id.");
        }
        return resourceId;
    }

    /**
     * TODO (AZ) - doc
     * @param resourceType
     *        ???
     *        Organization, Practitioner, Facility
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
    public JSONObject queryByIdentifier(PlrFhirResourceType resourceType, String identifier)
    throws InterruptedException,
           IOException
    {
        return  entityQueryByIdentifier(resourceType.wire(), identifier);
    }

    /**
     * TODO (AZ) - doc
     * 
     * @param resourceType
     *        ???
     *        Organization, Practitioner, Facility
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
    public JSONObject queryByIdentifier(
            PlrFhirResourceType resourceType, IdentifierType identifierType, String identifierValue)
    throws InterruptedException,
           IOException
    {
        return queryByIdentifier(
                resourceType, identifierType.getSourceSystem() + "|" + identifierValue);
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
    private SimpleHttpResponse sendMaintainRequest(String requestPayload)
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
    /**
     * Submits a maintain request payload and returns the id of the primary resource represented
     * by {@code targetType}. Use this overload instead of the legacy one to support multiple
     * maintain request kinds (facility/location, practitioner, organization, etc.).
     *
     * @param targetType  primary resource type contained in the maintain bundle
     * @param requestJson maintain bundle JSON payload
     * @return created/updated resource id
     * @throws InterruptedException if the thread is interrupted while sending the request
     * @throws IOException          if an I/O error occurs while sending the request
     */
    public String submitMaintainRequest(PlrFhirResourceType targetType, JSONObject requestJson)
    throws InterruptedException,
           IOException
    {
        return processMaintainResponse(sendMaintainRequest(requestJson), targetType);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param resourceType
     *        ???
     *        Organization, Practitioner, Facility
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
    private JSONObject  entityQueryByIdentifier(String resourceType, String identifier)
    throws InterruptedException,
           IOException
    {
        verifyLoggedIn();
        SimpleHttpRequest request = createHttpRequestBuilder()
                .transactionName("FHIR:Query" + resourceType)
                .uri(uri_.resolve(resourceType + "/$entityQuery"))
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
            throw new IllegalStateException(resourceType + " FHIR query failed.");
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
