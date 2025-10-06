package ca.bc.gov.health.qa.autotest.plr.fhir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.fhir.actions.PlrFhirActions;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * Base executor encapsulating common FHIR + Keycloak setup, keystore loading and login lifecycle.
 * Subclasses only need to focus on building and submitting resource specific payloads.
 */
public abstract class BaseMaintainExecutor implements AutoCloseable
{
    protected static final Logger LOG = ExecutionLogManager.getLogger();

    protected final URI fhirUri_;
    protected final URI keycloakUri_;
    protected final Path keyStorePath_;
    protected final char[] keyStorePassword_;
    protected final UserType userType_;
    protected final PlrFhirActions actions_;
    protected final String username_;

    /**
     * @param userType user role (determines credential set)
     */
    protected BaseMaintainExecutor(UserType userType)
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
            throw new IllegalStateException("Keystore password is missing");
        }
        this.keyStorePassword_ = ksPwd.toCharArray();
        if (!Files.exists(this.keyStorePath_))
        {
            throw new IllegalStateException("Keystore file not found: " + this.keyStorePath_);
        }

        Map<String,String> creds = PlrData.getCredentials("plr.fhir", userType_);
        this.username_ = creds.get("username");
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
}
