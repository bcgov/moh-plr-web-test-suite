package ca.bc.gov.health.qa.autotest.plr.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.context.LocalContext;
import ca.bc.gov.health.qa.autotest.core.util.io.PropertyUtils;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * TODO (AZ) - doc
 */
public class PlrData
{
    private static final String ENV_NAME;
    private static final Path   PROVIDERS_DIR;
    private static final Path   SECURITY_DIR;
    private static final Path   KEY_STORE_PATH;
    static
    {
        Config config  = ConfigProvider.get().getConfig();
        /* This original code is likely related to PlrWebWorkflowManager/multiple selenium instances */
        // Config config = LocalContext.get().getConfig();
        Path dataDir   = Path.of(config.get("data.dir"));
        ENV_NAME       = config.get("env.name");
        PROVIDERS_DIR  = dataDir.resolve("providers");
        SECURITY_DIR   = dataDir.resolve("security");
        KEY_STORE_PATH = SECURITY_DIR.resolve("FHA_HI1.pfx");
    }

    private static final Logger LOG = ExecutionLogManager.getLogger();

    private PlrData()
    {}

    /**
     * Gets the credentials (username/password) for a user in the environment.
     *
     * @param credentialType
     *        The type of credential to find in the property map (plr.web/plr.fhir)
     *
     * @param userType
     *        The type of user to load credentials from
     *
     * @return  A Map object containing credentials for a user for Web/FHIR
     */
    public static Map<String,String> getCredentials(String credentialType, UserType userType)
    {
        Path filePath = SECURITY_DIR.resolve("credentials-" + ENV_NAME + ".properties");
        Map<String,String> credentialsMap;
        try
        {
            credentialsMap = PropertyUtils.loadPropertyMap(filePath);
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read credentials (%s).", filePath);
            throw new IllegalStateException(msg, e);
        }
        String prefix = credentialType + "." + userType.toString().toLowerCase(Locale.ROOT) + ".";
        return Map.of(
                "username", getCredentialValue(credentialsMap, prefix + "username"),
                "password", getCredentialValue(credentialsMap, prefix + "password"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public static Path getKeyStorePath()
    {
        return KEY_STORE_PATH;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param providerType
     *        ???
     *
     * @param key
     *        ???
     *
     * @return ???
     *
     * @throws IllegalStateException
     *         if the provider type is not supported
     */
    public static JSONObject getProvider(ProviderType providerType, String key)
    {
        LOG.info("Provider data ({}:{}).", providerType, key);
        JSONObject provider;
        JSONObject providers = readProviderData(providerType);
        if (providers.has(key))
        {
            provider = providers.getJSONObject(key);
        }
        else
        {
            String msg = String.format("Provider data not found (%s:%s).", providerType, key);
            throw new IllegalStateException(msg);
        }
        return provider;
    }
    
    public static JSONObject getFacility(String key) {
    	JSONObject provider;
        JSONObject providers = readProviderData(ProviderType.FACILITY);
        if (providers.has(key))
        {
            provider = providers.getJSONObject(key);
        }
        else
        {
            String msg = String.format("Provider data not found (%s:%s).", ProviderType.FACILITY, key);
            throw new IllegalStateException(msg);
        }
        return provider;
    }

    private static String getCredentialValue(Map <String,String>credentialsMap, String key)
    {
        String value = credentialsMap.get(key);
        if (value == null)
        {
            String msg = String.format("Credential value not found (%s).", key);
            throw new IllegalStateException(msg);
        }
        return value;
    }

    private static JSONObject readProviderData(ProviderType providerType)
    {
        String fileName = providerType.toString().toLowerCase(Locale.ROOT).replace("_", "-")
                + "s-" + ENV_NAME + ".json";
        if(ProviderType.FACILITY.equals(providerType))
        	fileName = fileName.replace("facilitys","facilities");
        Path filePath = PROVIDERS_DIR.resolve(fileName);
        String data;
        try
        {
            // TODO (AZ) - cache string data read
            data  = Files.readString(filePath);
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read provider data (%s).", filePath);
            throw new IllegalStateException(msg, e);
        }

        return new JSONObject(data);
    }
    private static JSONObject readFacilityData(ProviderType providerType)
    {
        String fileName = providerType.toString().toLowerCase(Locale.ROOT).replace("_", "-")
                + "s-" + ENV_NAME + ".json";
        Path filePath = PROVIDERS_DIR.resolve(fileName);
        String data;
        try
        {
            // TODO (AZ) - cache string data read
            data  = Files.readString(filePath);
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read provider data (%s).", filePath);
            throw new IllegalStateException(msg, e);
        }

        return new JSONObject(data);
    }
}
