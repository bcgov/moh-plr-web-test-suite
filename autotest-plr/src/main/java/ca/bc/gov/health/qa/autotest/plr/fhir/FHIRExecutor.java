package ca.bc.gov.health.qa.autotest.plr.fhir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.fhir.actions.PlrFhirActions;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainPracBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.ResourceType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * Base executor encapsulating common FHIR + Keycloak setup, keystore loading and login lifecycle.
 * Subclasses only need to focus on building and submitting resource specific payloads.
 */
public class FHIRExecutor implements AutoCloseable
{
    /**
     * Class logger used for diagnostic and execution trace output.
     */
    protected static final Logger LOG = ExecutionLogManager.getLogger();

    /**
     * Base FHIR endpoint (base URL) used for all subsequent REST/FHIR calls.
     */
    protected final URI fhirUri_;

    /**
     * Keycloak base endpoint used for obtaining / refreshing access tokens.
     */
    protected final URI keycloakUri_;

    /**
     * Test user type (role/profile) which determines the credential set pulled from configuration / test data.
     */
    protected final UserType userType_;

    /**
     * Facade encapsulating low‑level HTTPS connection handling, authentication lifecycle (login / logout),
     * token management and FHIR request submission helpers. Shared safely per executor instance.
     */
    protected final PlrFhirActions actions_; // Connection, requests and credential manager

    /**
     * Constructor. Will set up credentials by logging in into keycloack based on the userType profile provided.
     * @param userType user role (determines credential set)
     */
    public FHIRExecutor(UserType userType)
    {
        this.userType_ = userType;

        Config config = ConfigProvider.get().getConfig();
        String fhirUrl = config.get("fhir.url");
        String keycloakUrl = config.get("keycloak.url");
        if (fhirUrl == null || keycloakUrl == null)
        {
            throw new IllegalStateException("Missing required config properties (fhir.url and/or keycloak.url)");
        }
        this.fhirUri_ = URI.create(fhirUrl);
        this.keycloakUri_ = URI.create(keycloakUrl);

        Path keyStorePath_ = PlrData.getKeyStorePath();
        String ksPwd = PlrData.getKeystorePassword();
        if (ksPwd == null || ksPwd.isBlank())
        {
            throw new IllegalStateException("Keystore password is missing");
        }

        char[] keyStorePassword_ = ksPwd.toCharArray();

        if (!Files.exists(keyStorePath_))
        {
            throw new IllegalStateException("Keystore file not found: " + keyStorePath_);
        }

        Map<String,String> creds = PlrData.getCredentials("plr.fhir", userType_);

        try
        {
            actions_ = new PlrFhirActions(fhirUri_, keycloakUri_, keyStorePath_, keyStorePassword_);
            actions_.login(creds.get("username"), creds.get("password"));
            LOG.info("Logged in (userType={}, user={}).", userType_, creds.get("username"));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during login", e);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("I/O failure during login", e);
        }
    }

    @Override
    public void close()
    {
        try
        {
            actions_.logout();
        }
        catch (Exception ignore)
        {
            // ignore
        }
        finally
        {
            try { actions_.close(); } catch (Exception ignore) {}
        }
    }

    private JSONObject buildPractitionerMaintainPayload(Map<String,String> params){

        IdentifierType idType = IdentifierType.valueOf(params.get("identifierType"));
                MaintainPracBuilder b = new MaintainPracBuilder()
                        .identifier(idType, params.get("identifierValue"))
                        .familyName(params.get("familyName"))
                        .firstName(params.get("givenName"))
                        .gender(params.get("gender").toLowerCase(Locale.ROOT))
                        .birthDate(params.get("birthDate"))
                        .roleType(params.get("roleType"))
                        .addStatus("LIC", "ACTIVE", "GS")
                        .addAddress(
                                params.get("addressType"),
                                params.get("addressPurpose"),
                                params.get("addressLine1"),
                                params.get("addressCity"),
                                params.get("addressPostalCode"));
        
        return b.build();
    }


    private JSONObject buildFacilityMaintainPayload(Map<String,String> params){

        MaintainFacilityBuilder b = new MaintainFacilityBuilder()
                        .identifier(params.get("identifier"))
                        .name(params.get("name"))
                        .description(params.get("description"))
                        .addAddress(
                                params.get("addressLine1"),
                                params.get("addressCity"),
                                params.get("addressPostalCode"));

        return b.build();
    }

    /**
     * Generic submit method that builds the correct resource payload based on the ResourceType and provided parameters map.
     * Keys per resource type:
     *  PRACTITIONER: identifierType, identifierValue, familyName, givenName, gender, birthDate, roleType, addressType, addressPurpose, addressLine1, addressCity, addressPostalCode
     *  FACILITY: identifier, name, addressLine1, addressCity, addressPostalCode, description
     * @param resourceType target resource type
     * @param params key/value parameters (see above)
     * 
     * @return the created Facility, Practitioner or Organization id
     */
    public String submitMaintainRequest(ResourceType resourceType, Map<String,String> params){

        JSONObject payload;
        switch (resourceType)
        {
            case PRACTITIONER:
            {
                payload = buildPractitionerMaintainPayload(params);
                break;
            }
            case FACILITY:
            {
                payload = buildFacilityMaintainPayload(params);
                break;
            }
            case ORGANIZATION:
                // TO DO
            default:
                throw new UnsupportedOperationException("Submit not implemented for resource type: " + resourceType);
        }

        LOG.info(payload.toString());

        try
        {
            String id = actions_.submitMaintainRequest(payload);
            LOG.info("Maintain submitted. Returned id {}.", id);
            return id;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while submitting maintain request", e);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("I/O failure during maintain request", e);
        }
    }

    
    /**
     * Query a resource (facility / practitioner / organization) using a single identifier value
     * where the identifier system is implied by the server configuration / default search behavior.
     *
     * @param resourceType the FHIR resource type to search (e.g. PRACTITIONER, FACILITY, ORGANIZATION)
     * @param identifier   the identifier value (system determined implicitly)
     * @return JSON object representing the FHIR search response or resource bundle
     * @throws IllegalStateException if the thread is interrupted or an I/O error occurs during the request
     */
    public JSONObject queryByIdentifier(ResourceType resourceType, String identifier) {

        try
        {
            return actions_.queryByIdentifier(resourceType, identifier);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while submitting query request", e);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("I/O failure during query request", e);
        }

    }

    /**
     * Query a resource by an explicit identifier type + value pair.
     *
     * @param resourceType   the FHIR resource type to search (e.g. PRACTITIONER, FACILITY, ORGANIZATION)
     * @param identifierType the logical / business identifier type enum used to derive the identifier system
     * @param identifierValue the identifier value
     * @return JSON search response (bundle or single resource representation depending on server behavior)
     * @throws IllegalStateException if the thread is interrupted or an I/O error occurs during the request
     */
    public JSONObject queryByIdentifier(ResourceType resourceType, IdentifierType identifierType, String identifierValue) {
        try
        {
            return actions_.queryByIdentifier(resourceType, identifierType, identifierValue);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while submitting query request", e);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("I/O failure during query request", e);
        }
    }

}
