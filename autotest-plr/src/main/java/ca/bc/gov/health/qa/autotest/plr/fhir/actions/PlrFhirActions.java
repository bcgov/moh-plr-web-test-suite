package ca.bc.gov.health.qa.autotest.plr.fhir.actions;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpClient;
import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpRequest;
import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpRequestBuilder;
import ca.bc.gov.health.qa.autotest.core.util.net.http.SimpleHttpResponse;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.MaintainAccessor;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.PlrFhirResourceType;
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
            throw new IllegalStateException(resourceType + " FHIR query failed." + 
                                            response.getTextResponseBody());
        }
        return responseData;
    }

    /**
     * Executes an Organization $entityQuery using optional criteria.
     * Only non-null/non-blank parameters are sent. 
     *
     * Supported parameters:
     * - name
     * - description
     * - type (e.g., HDS)
     * - address-city
     * - address-line1
     *
     * @param name           optional organization name filter
     * @param description    optional description filter
     * @param type           role type filter (e.g., HDS)
     * @param addressCity    optional address city filter
     * @param addressLine1   optional address first line filter
     * @param withHistory    include "withHistory" flag as empty query param when true
     * @return JSON response payload
     * @throws InterruptedException if the current thread is interrupted
     * @throws IOException          if an I/O error occurs
     */
    public JSONObject queryOrganizationByCriteria(
            String name,
            String description,
            String type,
            String addressCity,
            String addressLine1,
            boolean withHistory)
    throws InterruptedException,
           IOException
    {
        Map<String, String> params = new HashMap<>();
        if (name != null && !name.isBlank())                 params.put("name", name);
        if (description != null && !description.isBlank())   params.put("description", description);
        if (type != null && !type.isBlank())                 params.put("type", type);
        if (addressCity != null && !addressCity.isBlank())   params.put("address-city", addressCity);
        if (addressLine1 != null && !addressLine1.isBlank()) params.put("address-line1", addressLine1);
        if (withHistory)                                     params.put("withHistory", "true");
            

        return entityQueryWithParams("Organization", params);
    }

    /**
     * Queries FHIR Practitioner resources by optional criteria.
     * Only non-null/non-blank parameters are sent. Returns all matching results.
     * @param role           optional practitioner role type filter
     * @param addressCity    optional address city filter
     * @param family         optional family (last) name filter
     * @param expertise      optional expertise code filter
     * @param communication  optional communication language code filter
     * @param given          optional given (first) name filter
     * @param statusReason   optional status reason code filter
     * @param status         optional status code filter
     * @param gender         optional gender filter
     * @param withHistory    include "withHistory" flag as empty query param when true
     * @return JSON response payload
     * @throws InterruptedException if the current thread is interrupted
     * @throws IOException          if an I/O error occurs
     */
    public JSONObject queryIndividualByCriteria(
            String role,
            String addressCity,
            String family,
            String expertise,
            String communication,
            String given,
            String statusReason,
            String status,
            String gender,
            boolean withHistory)
    throws InterruptedException,
           IOException
    {
        Map<String, String> params = new HashMap<>();
        if (role != null && !role.isBlank())                 params.put("role", role);
        if (addressCity != null && !addressCity.isBlank())   params.put("address-city", addressCity);
        if (family != null && !family.isBlank())             params.put("family", family);
        if (expertise != null && !expertise.isBlank())       params.put("expertise", expertise);
        if (communication != null && !communication.isBlank()) params.put("communication", communication);
        if (given != null && !given.isBlank())               params.put("given", given);
        if (statusReason != null && !statusReason.isBlank()) params.put("status-reason", statusReason);
        if (status != null && !status.isBlank())             params.put("status", status);
        if (gender != null && !gender.isBlank())             params.put("gender", gender);
        if (withHistory)                                     params.put("withHistory", "true");

        return entityQueryWithParams("Practitioner", params);
    }

    /**
     * Generic $entityQuery helper that adds provided parameters and optional empty flags.
     * @param resourceType resource type path (e.g., "Organization")
     * @param params       map of query parameters to include
     * @return JSON response payload
     * @throws InterruptedException if interrupted
     * @throws IOException          on I/O errors
     */
    private JSONObject entityQueryWithParams(
            String resourceType,
            Map<String,String> params)
    throws InterruptedException,
           IOException
    {
        verifyLoggedIn();
        SimpleHttpRequestBuilder builder = createHttpRequestBuilder()
                .transactionName("FHIR:Query" + resourceType)
                .uri(uri_.resolve(resourceType + "/$entityQuery"));
        if (params != null) {
            for (Map.Entry<String,String> e : params.entrySet()) {
                builder.queryParameter(e.getKey(), e.getValue());
            }
        }

        SimpleHttpResponse response = client_.send(builder.build());
        JSONObject responseData;

        if (response.getStatusCode() == 200)
        {
            responseData = new JSONObject(response.getTextResponseBody());
        }
        else
        {
            throw new IllegalStateException(resourceType + " FHIR query failed." + 
                                            response.getTextResponseBody());
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
