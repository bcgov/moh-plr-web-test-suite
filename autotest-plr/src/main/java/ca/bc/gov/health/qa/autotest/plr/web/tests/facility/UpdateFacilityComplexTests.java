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
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
    // F4-029. Mandatory Facility Telecommunication Attributes
    public void mandatoryTelecomAttributes()
    {
        final String errMsg5000EndReason = errorList.getString("errMsg5000EndReason");
        final String errMsg5000TelecomType = errorList.getString("errMsg5000TelecomType");
        final String errMsg5000TelecomAreaCode = errorList.getString("errMsg5000TelecomAreaCode");
        final String errMsg5000TelecomPhoneNumber = errorList.getString("errMsg5000TelecomPhoneNumber");
        final String missingEffectiveFrom = errorList.getString("missingEffectiveFrom");

        final TelecommunicationType telecomType = TelecommunicationType.MOBILE;
        List<String> expectedPhoneNumber = List.of(generateNumericString(3), generateNumericString(7));
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        final List<List<String>> errorDetails = new ArrayList<>(Arrays.asList(
                List.of("Select One", generateNumericString(3), generateNumericString(7), "", effective_date(), "", errMsg5000TelecomType),
                List.of(telecomType.getText(), "", generateNumericString(7), "", effective_date(), "", errMsg5000TelecomAreaCode),
                List.of(telecomType.getText(), generateNumericString(3), "", "", effective_date(), "", errMsg5000TelecomPhoneNumber),
                List.of(telecomType.getText(), generateNumericString(3), generateNumericString(7), "", "", "", missingEffectiveFrom)
        ));

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        for (List<String> detail : errorDetails)
        {
            String error = actions.addTelecommunicationNumber(page, detail, true);

            assertEquals(error, detail.getLast(), "Error message does not match or did not appear as expected");
        }

        page.addTelecommunicationDataBlock(telecomType.getText(),
                expectedPhoneNumber.get(0), expectedPhoneNumber.get(1), "",
                effective_date(), "", false);

        actions.verifyMandatoryAttributesPositive(page, telecomType, expectedPhoneNumber.get(0), expectedPhoneNumber.get(1));

        errorDetails.removeFirst(); // Type error not testable in update flow

        for (List<String> detail : errorDetails)
        {
            String error = actions.updateTelecommunicationNumber(page, telecomType, detail, true);

            assertEquals(error, detail.getLast(), "Error message does not match or did not appear as expected");
        }

        int telecomIndex = Integer.parseInt(actions.getTelecomInfo(page, telecomType).get("index"));

        page.clickDataBlockUpdateButton(FacilitySection.TELECOMMUNICATIONS, telecomIndex);
        page.clickDialogSubmitButton(FacilitySection.TELECOMMUNICATIONS);
        String error = page.waitErrorMessage(FacilitySection.TELECOMMUNICATIONS);

        assertEquals(error, errMsg5000EndReason, "End Reason error message did not appear as expected");

        expectedPhoneNumber = List.of(generateNumericString(3), generateNumericString(7));

        page.updateTelecommunicationBlock(telecomType.getText(),
                expectedPhoneNumber.get(0), expectedPhoneNumber.get(1), "",
                effective_date(), "", telecomIndex, false);

        actions.verifyMandatoryAttributesPositive(page, telecomType, expectedPhoneNumber.get(0), expectedPhoneNumber.get(1));
    }

    @Test
    // F4-030. Optional Telecommunication Attributes
    public void optionalTelecomAttributes()
    {
        final TelecommunicationType telecomType1 = TelecommunicationType.PAGER;
        final TelecommunicationType telecomType2 = TelecommunicationType.FAX;
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        page.addTelecommunicationDataBlock(telecomType1.getText(),
                generateNumericString(3), generateNumericString(7), "",
                effective_date(), increment_month_for_effective_date(), false);

        LinkedHashMap<String,String> telecomInfo = actions.getTelecomInfo(page, telecomType1);
        int telecomIndex = Integer.parseInt(telecomInfo.get("index"));

        assertEquals(telecomInfo.get(TelecomField.EFFECTIVE_TO.getString()), increment_month_for_effective_date(),
                "Effective To Date add failed to fill as expected");

        page.updateTelecommunicationBlock(telecomType1.getText(),
                generateNumericString(3), generateNumericString(7), "",
                effective_date(), increment_year_for_effective_date(), telecomIndex, false);

        telecomInfo = page.grabTelecommunicationsBlockContent(telecomIndex);

        assertEquals(telecomInfo.get(TelecomField.EFFECTIVE_TO.getString()), increment_year_for_effective_date(),
                "Effective To date update failed to fill as expected");

        String expectedExtension = generateNumericString(3);
        page.addTelecommunicationDataBlock(telecomType2.getText(), generateNumericString(3),
                generateNumericString(7), expectedExtension, effective_date(), "", false);

        telecomInfo = actions.getTelecomInfo(page, telecomType2);
        telecomIndex = Integer.parseInt(telecomInfo.get("index"));

        assertEquals(telecomInfo.get(TelecomField.EXTENSION.getString()), expectedExtension,
                "Extension add failed to fill as expected");

        expectedExtension = generateNumericString(3);
        page.updateTelecommunicationBlock(telecomType2.getText(), generateNumericString(3),
                generateNumericString(7), expectedExtension,effective_date(),
                "", telecomIndex, false);

        telecomInfo = page.grabTelecommunicationsBlockContent(telecomIndex);

        assertEquals(telecomInfo.get(TelecomField.EXTENSION.getString()), expectedExtension,
                "Extension update failed to fill as expected");
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
            List.of(telecomType.getText(), generateNumericString(TELECOM_AREA_CODE_MAX+1), generateNumericString(7), "", effective_date(), "", errMsg5003TelecomAreaCode),
            List.of(telecomType.getText(), generateNumericString(3), generateNumericString(TELECOM_PHONE_NUMBER_MAX+1), "", effective_date(), "", errMsg5003TelecomPhoneNumber),
            List.of(telecomType.getText(), generateNumericString(3), generateNumericString(7), generateNumericString(TELECOM_EXTENSION_MAX+1), effective_date(), "", errMsg5003TelecomExtension)
        );
        List<List<String>> invalidCharTelecomNumbers = Arrays.asList(
            List.of(telecomType.getText(), generateAlphabetString(3), generateNumericString(7), "", effective_date(), ""),
            List.of(telecomType.getText(), generateNumericString(3), generateAlphabetString(7), "", effective_date(), ""),
            List.of(telecomType.getText(), generateNumericString(3), generateNumericString(7), generateAlphabetString(3), effective_date(), "")
        );
        LinkedHashMap<String,String> telecomInfo;

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        for (List<String> telecomNumber : overMaximumTelecomNumbers)
        {
            String error = actions.addTelecommunicationNumber(page, telecomNumber, true);
            assertEquals(error, telecomNumber.getLast(), "Error message does not match expected result");
        }

        for (List<String> telecomNumber : invalidCharTelecomNumbers)
        {
            String error = actions.addTelecommunicationNumber(page, telecomNumber, true);
            assertEquals(error, errMsg7008, "Error message does not match expected result");
        }

        page.addTelecommunicationDataBlock(telecomType.getText(),
                generateNumericString(TELECOM_AREA_CODE_MAX),
                generateNumericString(TELECOM_PHONE_NUMBER_MAX),
                generateNumericString(TELECOM_EXTENSION_MAX),
                effective_date(), "", false);

        telecomInfo = actions.getTelecomInfo(page, telecomType);
        int telecomIndex = Integer.parseInt(telecomInfo.get("index"));
        assertEquals(telecomInfo.get(TelecomField.AREA_CODE.getString()).length(), TELECOM_AREA_CODE_MAX,
                "Area Code is not the specified maximum allowed character count");
        assertEquals(telecomInfo.get(TelecomField.NUMBER.getString()).length(), TELECOM_PHONE_NUMBER_MAX,
                "Phone Number is not the specified maximum allowed character count");
        assertEquals(telecomInfo.get(TelecomField.EXTENSION.getString()).length(), TELECOM_EXTENSION_MAX,
                "Extension is not the specified maximum allowed character count");

        for (List<String> telecomNumber : overMaximumTelecomNumbers)
        {
            String error = actions.updateTelecommunicationNumber(page, telecomType, telecomNumber, true);
            assertEquals(error, telecomNumber.getLast(), "Error message does not match expected result");
        }

        for (List<String> telecomNumber : invalidCharTelecomNumbers)
        {
            String error = actions.updateTelecommunicationNumber(page, telecomType, telecomNumber,true);
            assertEquals(error, errMsg7008, "Error message does not match expected result");
        }

        page.updateTelecommunicationBlock(telecomType.getText(),
                generateNumericString(15), generateNumericString(30), generateNumericString(15),
                effective_date(), "", telecomIndex, false);

        telecomInfo = page.grabTelecommunicationsBlockContent(telecomIndex);

        assertEquals(telecomInfo.get(TelecomField.AREA_CODE.getString()).length(), TELECOM_AREA_CODE_MAX,
                "Area Code is not the specified maximum allowed character count");
        assertEquals(telecomInfo.get(TelecomField.NUMBER.getString()).length(), TELECOM_PHONE_NUMBER_MAX,
                "Phone Number is not the specified maximum allowed character count");
        assertEquals(telecomInfo.get(TelecomField.EXTENSION.getString()).length(), TELECOM_EXTENSION_MAX,
                "Extension is not the specified maximum allowed character count");
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
}
