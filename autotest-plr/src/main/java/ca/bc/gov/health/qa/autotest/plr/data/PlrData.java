package ca.bc.gov.health.qa.autotest.plr.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.PractitionerRelationshipCode;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.query.IndividualQueryCriteriaParams;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.query.OrgQueryCriteriaParams;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Identifier;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
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
     * Returns the PLR FHIR keystore password (may be {@code null} if not present).
     *
     * @return keystore password or {@code null} if not defined
     */
    public static String getKeystorePassword()
    {
        Path filePath = SECURITY_DIR.resolve("credentials-" + ENV_NAME + ".properties");
        try
        {
            Map<String,String> map = PropertyUtils.loadPropertyMap(filePath);
            return map.get("plr.fhir.keystore.password");
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read credentials (%s).", filePath);
            throw new IllegalStateException(msg, e);
        }
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
    @Deprecated
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

    public static void setupPractitioner(FHIRController fhir, ProviderType providerType,
            Map<ProviderType, MaintainRequestBuilder> defaultMap, Map<ProviderType, MaintainRequestBuilder> minimumMap)
    {
        // TODO: to add to default: work locations, communication preferences, registry user relationships

        final IndividualDataGenerator dataGen = IndividualDataGenerator.getInstance();
        final IndividualBuilderFactory factory = new IndividualBuilderFactory(dataGen);

        final IdentifierType ipc = IdentifierType.IPC;
        final String pracType = providerType.equals(ProviderType.BC_PRACTITIONER) ? "BC" : "OOP";
        final IndividualRoleType roleType = pracType.equals("BC") ? IndividualRoleType.DEN : IndividualRoleType.OOP_DEN;

        final String defaultName = pracType.equals("BC") ? "TestBCPrac2" : "TestOOPPrac2";
        final String minimumName = pracType.equals("BC") ? "MinimumDataBCPrac" : "MinimumDataOOPPrac";

        MaintainIndividualBuilder defaultBuilder = factory.build(new IndividualMaintainConfig()
                        .withDemographics().withGivenNames().withAllTelecom().withNotes(2).withStatuses(2)
                        .withCredentials(2).withConditions(2).withDisciplinaryActions(2)
                        .withExpertise(0).withOrganizationRelationships(2).withIndividualRelationships(2))
                .familyName(defaultName).roleType(roleType).confidentiality(false)
                .addExpertise("ENG", dataGen.shortText())
                .addExpertise("SPAN", dataGen.shortText());

        final MaintainIndividualBuilder minimumBuilder = factory.build(new IndividualMaintainConfig()
                .withRoleType(roleType).withExpertise(0))
                .familyName(minimumName).setAddressList(List.of(Map.of(
                        "type", "physical",
                        "purpose", "BC",
                        "line1", "1175 DOUGLAS ST",
                        "city", "Victoria",
                        "postalCode", "V8W 2E1")));

        // Default
        List<MaintainIndividualBuilder> defaultQuery = fhir.queryIndividualByCriteria(
                new IndividualQueryCriteriaParams().setRoleType(roleType).setFamily(defaultName).setExpertise("ENG"));

        MaintainIndividualBuilder defaultIndiv;
        if (defaultQuery.isEmpty())
        {
            // Create Organization/Individual relationships
            //String orgRelIdentifier = fhir.createOrganization(OrganizationDataGenerator.getInstance().randomOrgRoleType())
            //        .getIdentifier(IdentifierType.IPC);
            //defaultBuilder = defaultBuilder.addOrganizationRelationship(
            //        IdentifierType.IPC, orgRelIdentifier, dataGen.generatePractitionerRelationshipCode());

            //orgRelIdentifier = fhir.createOrganization(OrganizationDataGenerator.getInstance().randomOrgRoleType())
            //        .getIdentifier(IdentifierType.IPC);
            //defaultBuilder = defaultBuilder.addOrganizationRelationship(
            //        IdentifierType.IPC, orgRelIdentifier, dataGen.generatePractitionerRelationshipCode());

            //String indRelIdentifier = fhir.createIndividual(dataGen.randomRoleType(false))
            //        .getIdentifier(IdentifierType.IPC);
            //defaultBuilder = defaultBuilder.addIndividualRelationship(
            //        IdentifierType.IPC, indRelIdentifier, dataGen.generatePractitionerRelationshipCode());

            //indRelIdentifier = fhir.createIndividual(dataGen.randomRoleType(false))
            //        .getIdentifier(IdentifierType.IPC);
            //defaultBuilder = defaultBuilder.addIndividualRelationship(
            //        IdentifierType.IPC, indRelIdentifier, dataGen.generatePractitionerRelationshipCode());

            defaultIndiv = fhir.queryIndividualByIdentifier(ipc, fhir.submitIndividual(defaultBuilder).getIdentifier(ipc));
        } else defaultIndiv = defaultQuery.getFirst();

        defaultMap.put(providerType, defaultIndiv);

        // Minimum
        List<MaintainIndividualBuilder> minimumQuery = fhir.queryIndividualByCriteria(
                new IndividualQueryCriteriaParams().setRoleType(roleType).setFamily(minimumName).setAddressCity("Victoria"));

        MaintainIndividualBuilder minimumIndiv;
        if (minimumQuery.isEmpty())
            minimumIndiv = fhir.queryIndividualByIdentifier(ipc, fhir.submitIndividual(minimumBuilder).getIdentifier(ipc));
        else
            minimumIndiv = minimumQuery.getFirst();

        minimumMap.put(providerType, minimumIndiv);
    }

    /**
     * Finds organizations with expected properties for tests, or creates them if they don't exist.
     *
     * @param fhir          the FHIRController reference for fhir endpoint use
     * @param defaultMap    a map of default data set provider builders based on provider type
     * @param minimumMap    a map of minimum data set provider builders based on provider type
     */
    public static void setupOrgProvider(FHIRController fhir,
            Map<ProviderType, MaintainRequestBuilder> defaultMap, Map<ProviderType, MaintainRequestBuilder> minimumMap)
    {
        // TODO: to add to default: work locations
        final OrganizationDataGenerator dataGen = OrganizationDataGenerator.getInstance();
        final OrganizationBuilderFactory factory = new OrganizationBuilderFactory(dataGen);

        final IdentifierType ipc = IdentifierType.IPC;

        final String defaultName = "TestDefaultOrganization";
        final String minimumName = "MinimumDataOrganization";
        final OrganizationMaintainConfig defaultConfig = new OrganizationMaintainConfig(OrgRoleType.BUSINESS)
                .withName(defaultName)
                .withAddress().withAllTelecom().withNotes(2).withStatuses(2)
                .withAllOrgProperties(2,2,2,2);
        MaintainOrgBuilder defaultBuilder = factory.build(defaultConfig).confidentiality(false);

        // Default
        List<MaintainOrgBuilder> defaultQuery = fhir.queryOrganizationByCriteria(
                new OrgQueryCriteriaParams().setName(defaultName).setRoleType(OrgRoleType.BUSINESS));

        MaintainOrgBuilder defaultOrg;
        if (defaultQuery.isEmpty())
        {
            // Facility Relationships
            MaintainFacilityBuilder facility = fhir.createFacility();
            defaultBuilder = defaultBuilder.addFacilityRelationship(
                    IdentifierType.IFC, facility.getIdentifier(), facility.getName());

            facility = fhir.createFacility();
            defaultBuilder = defaultBuilder.addFacilityRelationship(
                    IdentifierType.IFC, facility.getIdentifier(), facility.getName());

            // Organization / Individual Relationships
            String orgRelIdentifier = fhir.createOrganization(dataGen.randomOrgRoleType())
                    .getIdentifier(IdentifierType.IPC);
            defaultBuilder = defaultBuilder.addOrganizationRelationship(
                    IdentifierType.IPC, orgRelIdentifier, dataGen.generatePractitionerRelationshipCode());

            orgRelIdentifier = fhir.createOrganization(dataGen.randomOrgRoleType())
                    .getIdentifier(IdentifierType.IPC);
            defaultBuilder = defaultBuilder.addOrganizationRelationship(
                    IdentifierType.IPC, orgRelIdentifier, dataGen.generatePractitionerRelationshipCode());

            String indRelIdentifier = fhir.createIndividual(
                    IndividualDataGenerator.getInstance().randomRoleType(false)).getIdentifier(IdentifierType.IPC);
            defaultBuilder = defaultBuilder.addIndividualRelationship(
                    IdentifierType.IPC, indRelIdentifier, dataGen.generatePractitionerRelationshipCode());

            indRelIdentifier = fhir.createIndividual(
                    IndividualDataGenerator.getInstance().randomRoleType(false)).getIdentifier(IdentifierType.IPC);
            defaultBuilder = defaultBuilder.addIndividualRelationship(
                    IdentifierType.IPC, indRelIdentifier, dataGen.generatePractitionerRelationshipCode());

            defaultBuilder = fhir.submitOrganization(defaultBuilder);
            defaultOrg = fhir.queryOrganizationByIdentifier(ipc, defaultBuilder.getIdentifier(ipc));
        } else defaultOrg = defaultQuery.getFirst();

        defaultMap.put(ProviderType.ORGANIZATION, defaultOrg);

        // Minimum Data Set
        List<MaintainOrgBuilder> minimumQuery = fhir.queryOrganizationByCriteria(
                new OrgQueryCriteriaParams().setName(minimumName).setRoleType(OrgRoleType.ORG));

        MaintainOrgBuilder minimumOrg;
        if (minimumQuery.isEmpty())
            minimumOrg = fhir.queryOrganizationByIdentifier(ipc, fhir.createOrganization(
                    new OrganizationMaintainConfig(OrgRoleType.ORG).withName(minimumName)).getIdentifier(ipc));
        else
            minimumOrg = minimumQuery.getFirst();

        minimumMap.put(ProviderType.ORGANIZATION, minimumOrg);
    }

    /**
     * TODO (KD) - doc
     *
     * @param key the configuration key name
     * @return the string value for the given key, or null if absent
     */
    public static JSONObject getFacility(String key) {
    	JSONObject provider;
        JSONObject providers = readProviderData("facilities-" + ENV_NAME + ".json");
        if (providers.has(key))
        {
            provider = providers.getJSONObject(key);
        }
        else
        {
            String msg = String.format("Provider data not found (%s).", key);
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
        return readProviderData(fileName);
    }

    private static JSONObject readProviderData(String fileName)
    {
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
