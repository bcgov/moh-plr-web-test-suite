package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants.*;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.util.*;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.UpdateFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.ViewFacilityActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.generateAlphabetString;
import static java.lang.Integer.TYPE;
import static java.lang.Integer.parseInt;
import static org.testng.Assert.*;
import static ca.bc.gov.health.qa.autotest.plr.web.tests.UpdateSimpleHelper.*;
import static ca.bc.gov.health.qa.autotest.plr.data.UpdateFacilityConstants.*;

public class UpdateFacilityComplexTests implements SimpleTest
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private static FHIRController fhirController;
    private static MaintainFacilityBuilder dummyFacility;

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;

    public UpdateFacilityComplexTests()
    {
        try
        {
            errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read JSON data (%s).", errorPath);
            throw new IllegalStateException(msg, e);
        }
    }

    /*
    @AfterClass
    public void teardown()
    {
        fhirController.close();
        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }
     */

    @BeforeTest
    public void beforeTest()
    {
        fhirController = new FHIRController(UserType.ADMIN);

        final FacilityMaintainConfig config = new FacilityMaintainConfig();
        dummyFacility = fhirController.queryFacilityByIdentifier(IdentifierType.IFC, "IFC.00006919.BC.PRS");
        //dummyFacility = fhirController.createFacility(config);
    }

    @BeforeMethod
    public void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn())
        {
            workflow.login().openPlr();
        }
    }

    @Test
    // F4-022. Rejection of Non-Acceptable Characters
    public void rejectionNonAcceptableCharacters()
    {
        final ViewFacilityActions actions = workflowManager_.getSelectedWorkflow().getViewFacilityActions();
        actions.openFacility("6905");
        // attempt to add identifier
        // attempt to update identifier
        // attempt to add name
        // attempt to update name
        // attempt to update civic address
        // attempt to update other address
        // attempt to add new telecom
        // attempt to update telecom
        // attempt to add e-address
        // attempt to update e-address
        // attempt to add note
        // attempt to update note
    }

    @Test
    // F4-031. Validate Telecommunication Number
    public void validateTelecomNumber()
    {
        final TelecommunicationType telecomType = TelecommunicationType.MODEM;
        final String errMsg7008 = errorList.getString("errMsg7008");
        final String errMsg5003TelecomAreaCode = errorList.getString("errMsg5003TelecomAreaCode");
        final String errMsg5003TelecomPhoneNumber = errorList.getString("errMsg5003TelecomPhoneNumber");
        final String errMsg5003TelecomExtension = errorList.getString("errMsg5003TelecomExtension");
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        List<List<String>> overMaximumTelecomNumbers = Arrays.asList(
            List.of(generateNumericString(TELECOM_AREA_CODE_MAX+1), generateNumericString(7), "", errMsg5003TelecomAreaCode),
            List.of(generateNumericString(3), generateNumericString(TELECOM_PHONE_NUMBER_MAX+1), "", errMsg5003TelecomPhoneNumber),
            List.of(generateNumericString(3), generateNumericString(7), generateNumericString(TELECOM_EXTENSION_MAX+1), errMsg5003TelecomExtension)
        );
        List<List<String>> invalidCharTelecomNumbers = Arrays.asList(
            List.of(generateAlphabetString(3), generateNumericString(7), ""),
            List.of(generateNumericString(3), generateAlphabetString(7), ""),
            List.of(generateNumericString(3), generateNumericString(7), generateAlphabetString(3))
        );
        LinkedHashMap<String,String> telecomInfo = new LinkedHashMap<>();

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        for (List<String> telecomNumber : overMaximumTelecomNumbers)
        {
            String error = actions.addTelecommunicationNumber(page, telecomType, telecomNumber, true);
            assertEquals(error, telecomNumber.getLast(), "Error message does not match expected result");
        }

        for (List<String> telecomNumber : invalidCharTelecomNumbers)
        {
            String error = actions.addTelecommunicationNumber(page, telecomType, telecomNumber, true);
            assertEquals(error, errMsg7008, "Error message does not match expected result");
        }

        page.addTelecommunicationDataBlock(telecomType.getText(),
                generateNumericString(TELECOM_AREA_CODE_MAX),
                generateNumericString(TELECOM_PHONE_NUMBER_MAX),
                generateNumericString(TELECOM_EXTENSION_MAX),
                effective_date(), "", false);

        int telecomIndex = page.grabActiveDataBlockCount(FacilitySection.TELECOMMUNICATIONS, true);
        for (int index = 0; index < telecomIndex; index++)
        {
            telecomInfo = page.grabTelecommunicationsBlockContent(index);
            if (telecomInfo.get(TelecomField.TYPE.getString()).equals(TelecommunicationType.MODEM.getDataField())) {
                telecomIndex = index;
                LOG.info(index);
                break;
            }
        }
        assertEquals(telecomInfo.get(TelecomField.AREA_CODE.getString()).length(), TELECOM_AREA_CODE_MAX,
                "Area Code is not the specified maximum allowed character count");
        assertEquals(telecomInfo.get(TelecomField.NUMBER.getString()).length(), TELECOM_PHONE_NUMBER_MAX,
                "Phone Number is not the specified maximum allowed character count");
        assertEquals(telecomInfo.get(TelecomField.EXTENSION.getString()).length(), TELECOM_EXTENSION_MAX,
                "Extension is not the specified maximum allowed character count");

        for (List<String> telecomNumber : overMaximumTelecomNumbers)
        {
            String error = actions.updateTelecommunicationNumber(page, telecomType, telecomNumber, telecomIndex, true);
            assertEquals(error, telecomNumber.getLast(), "Error message does not match expected result");
        }

        for (List<String> telecomNumber : invalidCharTelecomNumbers)
        {
            String error = actions.updateTelecommunicationNumber(page, telecomType, telecomNumber, telecomIndex, true);
            assertEquals(error, errMsg7008, "Error message does not match expected result");
        }

        // TODO positive update test?
    }

    @Test
    // F4-045. Generating Internal Relationship Identifier (RID)
    public void generateRelationshipIdentifier()
    {
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        final OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig();
        final MaintainOrgBuilder org1 = fhirController.createOrganization(orgConfig.withName(generateAlphabetString(15)));
        final MaintainOrgBuilder org2 = fhirController.createOrganization(orgConfig.withName(generateAlphabetString(15)));

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org1.getIdentifier(),
                                            RelationshipType.LOCATION.getText(), effective_date(),
                                    "", false);
        String org1RelIdentifier = StringUtils.getDigits(page.grabOrgRelationshipsBlockContent(0)
                                        .get(OrgRelationshipField.RELATIONSHIP_IDENTIFIER.getString()));

        page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org2.getIdentifier(),
                                            RelationshipType.LOCATION.getText(), effective_date(),
                                    "", false);
        String org2RelIdentifier = StringUtils.getDigits(page.grabOrgRelationshipsBlockContent(0)
                                        .get(OrgRelationshipField.RELATIONSHIP_IDENTIFIER.getString()));

        assertNotEquals(org1RelIdentifier, org2RelIdentifier,
                "Organization Relationship Identifiers are unexpectedly equal");
        assertEquals(parseInt(org1RelIdentifier), parseInt(org2RelIdentifier) - 1,
                "Second relationship identifier is not immediately after the first");
    }

    @Test
    // F4-046. Validate Facility Relationship Type Code
    public void validateRelationshipTypeCode()
    {
        final String errMsg5000OrgRel = errorList.getString("errMsg5000OrgRelType");
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        final OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig();
        final MaintainOrgBuilder org = fhirController.createOrganization(orgConfig.withName(generateAlphabetString(15)));

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        String error = page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org.getIdentifier(),
                "Select One", effective_date(), "", true);

        assertEquals(error, errMsg5000OrgRel, "Error message does not match expected result");

        for (RelationshipType relType : RelationshipType.values())
        {
            page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org.getIdentifier(),
                    relType.getText(), effective_date(), "", false);
            String orgRelType = page.grabOrgRelationshipsBlockContent(0)
                    .get(OrgRelationshipField.RELATIONSHIP_TYPE.getString());

            assertEquals(orgRelType, relType.getBlockText(),
                    "New org relationship has unexpected relationship type");
        }
    }

    @Test
    // F4-047. Validate Related Organization ID and Relationship Type Code Combination
    public void relatedOrgIDTypeCodeCombo()
    {
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
        final OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig();
        final MaintainOrgBuilder org = fhirController.createOrganization(orgConfig.withName(generateAlphabetString(15)));

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        // related provider identifier + relationship type

        // different org, same relationship type

        // same org, different relationship type

        //
    }
}
