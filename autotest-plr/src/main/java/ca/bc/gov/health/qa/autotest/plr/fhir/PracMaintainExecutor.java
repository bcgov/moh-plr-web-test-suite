package ca.bc.gov.health.qa.autotest.plr.fhir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.fhir.actions.PlrFhirActions;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainPracBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * Utility with a single entry point to build a minimal Practitioner maintain request and submit it.
 * <p>Intended for quick automation smoke scenarios. Adjust defaults as needed.</p>
 *
 */
public final class PracMaintainExecutor implements AutoCloseable
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    // Environment / configuration derived once in constructor
    private final URI fhirUri_;
    private final URI keycloakUri_;
    private final Path keyStorePath_;
    private final char[] keyStorePassword_;
    private final UserType userType_;
    private final PlrFhirActions actions_;

    /**
     * Constructor performs all configuration and login side-effects so later method only builds and submits.
     */
    public PracMaintainExecutor(UserType userType)
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

        this.keyStorePath_ = PlrData.getKeyStorePath();
        String ksPwd = PlrData.getKeystorePassword();
        if (ksPwd == null || ksPwd.isBlank())
        {
            LOG.warn("Keystore password not set (property plr.fhir.keystore.password). Proceeding with blank password; SSL context will fail if keystore requires one.");
            ksPwd = "";
        }
        this.keyStorePassword_ = ksPwd.toCharArray();
        if (!java.nio.file.Files.exists(this.keyStorePath_))
        {
            throw new IllegalStateException("Keystore file not found: " + this.keyStorePath_);
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

    /**
     * Single public operation: build the practitioner maintain payload and submit it.
     * Returns practitioner id.
     * @param identifierType
     * @param identifierValue
     * @param familyName
     * @param givenName
     * @param gender
     * @param birthDate
     * @param roleType
     * @param addressType
     * @param addressPurpose
     * @param addressLine1
     * @param addressCity
     * @param addressPostalCode
     * @return the created practitioner id
     * @throws IllegalStateException if the submission fails
     */
    public String submitPractitioner(
            IdentifierType identifierType,
            String identifierValue,
            String familyName,
            String givenName,
            String gender,
            String birthDate,
            String roleType,
            String addressType,
            String addressPurpose,
            String addressLine1,
            String addressCity,
            String addressPostalCode)
    {
        MaintainPracBuilder builder = new MaintainPracBuilder()
                .identifier(identifierType, identifierValue)
                .familyName(familyName)
                .firstName(givenName)
                .gender(gender.toLowerCase(Locale.ROOT))
                .birthDate(birthDate)
                .roleType(roleType)
                .addStatus("LIC", "ACTIVE", "GS")
                .addAddress(addressType, addressPurpose, addressLine1, addressCity, addressPostalCode);

        JSONObject payload = builder.build();
        LOG.info(payload.toString());

        try
        {
            String id = actions_.submitMaintainRequest(payload);
            LOG.info("Practitioner maintain submitted. Returned id {}.", id);
            return id;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while submitting practitioner maintain request", e);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("I/O failure during practitioner maintain request", e);
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
}
