package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants.*;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.*;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.UpdateFacilitySimpleActions;
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
import java.util.*;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.generateAlphabetString;
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

    @AfterClass
    public void teardown()
    {
        fhirController.close();
        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }

    @BeforeTest
    public void beforeTest()
    {
        fhirController = new FHIRController(UserType.ADMIN);

        final FacilityMaintainConfig config = new FacilityMaintainConfig();
        dummyFacility = fhirController.createFacility(config);
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
    public void rejectNonAcceptableCharacters(){
        //Step 1 - Open facility details screen
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
        UpdateFacilityPage page = actions.openFacility(dummyFacility);
        
        final String errMsgForeignFacilityText = errorList.getString("foreignCharacterFacility");

        //Step 2 - n/a

        //Step 3 - n/a

        //Step 4 - Attempt to add a new name data block using characters that are not accepted in the following fields: Name, Description
        if (page.grabActiveDataBlockCount(FacilitySection.NAMES, true) > 0) {
            page.ceaseDataBlock(FacilitySection.NAMES, 0);
        }
        
        String err = page.addNameDataBlock("NÀME-" + generateAlphabetString(5),
                "", effective_date(), "");
        assertEquals(err, errMsgForeignFacilityText,
                "Expected error message for invalid characters in Name field did not appear");

        err = page.addNameDataBlock(dummyFacility.getName(),
                "DÈSC-" + generateAlphabetString(5), effective_date(), "");
        assertEquals(err, errMsgForeignFacilityText);

        //Step 5 - Attempt to update an existing name data block using characters that are not accepted in the following fields: Name, Description
        if (page.grabActiveDataBlockCount(FacilitySection.NAMES, true) == 0) {
            err = page.addNameDataBlock("NAME-VALID", "DESC-VALID", effective_date(), "");
            assertTrue(StringUtils.isEmpty(err), "Precondition failed: could not create valid name block");
        }

        int nameIndex = 0;
        err = page.updateNameDataBlock("NÀME-INVALID", "DESC-OK", effective_date(), "", nameIndex);
        assertEquals(err, errMsgForeignFacilityText,
                "Expected error message for invalid characters in Name field did not appear");

        err = page.updateNameDataBlock("NAME-OK", "DÈSC-INVALID", effective_date(), "", nameIndex);
        assertEquals(err, errMsgForeignFacilityText, 
                "Expected error message for invalid characters in Description field did not appear");

        //Step 6 - n/a

        //Step 7 - n/a

        //Step 8 - Attempt to add a new telecommunication data block using characters that are not accepted in the following fields: Area Code, Phone Number, Extension

        final String errMsg7008 = errorList.getString("errMsg7008");
        final TelecommunicationType telecomType = TelecommunicationType.MOBILE;
        List<List<String>> invalidTelecomAdds = Arrays.asList(
                List.of(telecomType.getText(), generateAlphabetString(3), generateNumericString(7), "", effective_date(), ""),
                List.of(telecomType.getText(), generateNumericString(3), generateAlphabetString(7), "", effective_date(), ""),
                List.of(telecomType.getText(), generateNumericString(3), generateNumericString(7), generateAlphabetString(3), effective_date(), "")
        );

        for (List<String> detail : invalidTelecomAdds){
        
                String error = actions.addTelecommunicationNumber(page, detail, true);
                assertEquals(error, errMsg7008);
        }
        

        //Step 9 - Attempt to update an existing telecommunication data block using characters that are not accepted in the following fields: Area Code, Phone Number, Extension
        String preErr = page.addTelecommunicationDataBlock(telecomType.getText(),
                        generateNumericString(3), generateNumericString(7), "",
                        effective_date(), "", false);
        
        assertTrue(StringUtils.isEmpty(preErr));

        List<List<String>> invalidTelecomUpdates = Arrays.asList(
                List.of(telecomType.getText(), generateAlphabetString(3), generateNumericString(7), "", effective_date(), ""),
                List.of(telecomType.getText(), generateNumericString(3), generateAlphabetString(7), "", effective_date(), ""),
                List.of(telecomType.getText(), generateNumericString(3), generateNumericString(7), generateAlphabetString(3), effective_date(), "")
        );

        for (List<String> detail : invalidTelecomUpdates)
        {
                String error = actions.updateTelecommunicationNumber(page, telecomType, detail, true);
                assertEquals(error, errMsg7008);
        }

        //Step 10 - Attempt to add a new electronic address data block using characters that are not accepted in the following fields: Electronic Address
        final String errMsg7013 = errorList.getString("errMsg7013");
        final ElectronicAddressType eaType = ElectronicAddressType.HTTP;
        String error = page.addElectronicAddressDataBlock(eaType.getText(), "invàlid-" + generateAlphabetString(6) + ".com",
                effective_date(), "", true);

        assertEquals(errMsg7013, error, "Expected error for invalid characters in Electronic Address did not appear");
        

        //Step 11 - Attempt to update an existing electronic address data block using characters that are not accepted in the following fields: Electronic Address
        if (page.grabActiveDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES, true) == 0) {
            err = page.addElectronicAddressDataBlock(eaType.getText(), generateHTTP(), effective_date(), "", false);
            assertTrue(StringUtils.isEmpty(err), "Precondition failed: could not create valid electronic address block");
        }
        error = page.updateElectronicAddressDataBlock("invàlid-" + generateAlphabetString(6) + ".com",
                effective_date(), "", 0, true);


        assertEquals(errMsg7013, error, "Expected error for invalid characters in Electronic Address update did not appear");

        //Step 12 - Attempt to add a new notes data block using characters that are not accepted in the following fields: Note Identifier, Note Text
        final String errMsg7004NoteIdentifier = errorList.getString("errMsg7004NoteIdentifier");
        final String errMsg7004NoteText = errorList.getString("errMsg7004NoteText");
        if (page.grabActiveDataBlockCount(FacilitySection.NOTES, true) > 0) {
            page.ceaseDataBlock(FacilitySection.NOTES, 0);
        }

        error = page.addNoteDataBlock("ID-INVÀLID###", "TEXT-OK", effective_date(), "", true);
        assertEquals(errMsg7004NoteIdentifier, error, "Expected error for invalid characters in Note Identifier did not appear");

        error = page.addNoteDataBlock("ID-OK", "TÈXT-INVALID###", effective_date(), "", true);
        assertEquals(errMsg7004NoteText, error, "Expected error for invalid characters in Note Text did not appear");

        //Step 13 - Attempt to update an existing notes data block using characters that are not accepted in the following fields: Note Text
        if (page.grabActiveDataBlockCount(FacilitySection.NOTES, true) == 0) {
            err = page.addNoteDataBlock("ID-OK", "TEXT-OK", effective_date(), "", false);
            assertTrue(StringUtils.isEmpty(err), "Precondition failed: could not create valid note block");
        }

        String updNoteErr = page.updateNoteDataBlock("TÈXT-INVALID###", effective_date(), "", 0, true);
        assertEquals(errMsg7004NoteText, updNoteErr, "Expected error for invalid characters in Note Text update did not appear");
    }

    @Test
    // F4-029. Mandatory Facility Telecommunication Attributes
    public void mandatoryTelecomAttributes()
    {
        final String errMsg5000EndReason = errorList.getString("errMsg5000EndReason");
        final String errMsg5000Type = errorList.getString("errMsg5000Type");
        final String errMsg5000TelecomAreaCode = errorList.getString("errMsg5000TelecomAreaCode");
        final String errMsg5000TelecomPhoneNumber = errorList.getString("errMsg5000TelecomPhoneNumber");
        final String missingEffectiveFrom = errorList.getString("missingEffectiveFrom");

        final TelecommunicationType telecomType = TelecommunicationType.MOBILE;
        List<String> expectedPhoneNumber = List.of(generateNumericString(3), generateNumericString(7));
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        final List<List<String>> errorDetails = new ArrayList<>(Arrays.asList(
                List.of("Select One", generateNumericString(3), generateNumericString(7), "", effective_date(), "", errMsg5000Type),
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

        actions.verifyMandatoryAttributesTelecom(page, telecomType, expectedPhoneNumber.get(0), expectedPhoneNumber.get(1));

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

        page.updateTelecommunicationBlock(expectedPhoneNumber.get(0), expectedPhoneNumber.get(1), "",
                effective_date(), "", telecomIndex, false);

        actions.verifyMandatoryAttributesTelecom(page, telecomType, expectedPhoneNumber.get(0), expectedPhoneNumber.get(1));
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

        page.updateTelecommunicationBlock(generateNumericString(3), generateNumericString(7), "",
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
        page.updateTelecommunicationBlock(generateNumericString(3), generateNumericString(7),
                expectedExtension, effective_date(), "", telecomIndex, false);

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

        page.updateTelecommunicationBlock(generateNumericString(15), generateNumericString(30),
                generateNumericString(15), effective_date(), "", telecomIndex, false);

        telecomInfo = page.grabTelecommunicationsBlockContent(telecomIndex);

        assertEquals(telecomInfo.get(TelecomField.AREA_CODE.getString()).length(), TELECOM_AREA_CODE_MAX,
                "Area Code is not the specified maximum allowed character count");
        assertEquals(telecomInfo.get(TelecomField.NUMBER.getString()).length(), TELECOM_PHONE_NUMBER_MAX,
                "Phone Number is not the specified maximum allowed character count");
        assertEquals(telecomInfo.get(TelecomField.EXTENSION.getString()).length(), TELECOM_EXTENSION_MAX,
                "Extension is not the specified maximum allowed character count");
    }

    @Test
    // F4-035. Mandatory Facility Electronic Address Attributes
    public void mandatoryEAddressAttributes()
    {
        final String errMsg5000EndReason = errorList.getString("errMsg5000EndReason");
        final String errMsg5000Type = errorList.getString("errMsg5000Type");
        final String errMsg5000EAddress = errorList.getString("errMsg5000EAddress");
        final String missingEffectiveFrom = errorList.getString("missingEffectiveFrom");

        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
        final ElectronicAddressType eaType = ElectronicAddressType.EMAIL;

        List<List<String>> errorDetails = new ArrayList<>(Arrays.asList(
                List.of("Select One", generateEmail(), effective_date(), "", errMsg5000Type),
                List.of(eaType.getText(), "", effective_date(), "", errMsg5000EAddress),
                List.of(eaType.getText(), generateEmail(), "", "", missingEffectiveFrom)
        ));

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        for (List<String> detail : errorDetails)
        {
            String error = actions.addEAddress(page, detail, true);

            assertEquals(error, detail.getLast(), "Error message does not match or did not appear as expected");
        }

        String expectedEmail = generateEmail();
        page.addElectronicAddressDataBlock(eaType.getText(), expectedEmail,
                effective_date(), "", false);

        actions.verifyMandatoryAttributesEAddress(page, eaType, expectedEmail);

        errorDetails.removeFirst(); // Type error not testable in update flow

        for (List<String> detail : errorDetails)
        {
            String error = actions.updateEAddress(page, eaType, detail, true);

            assertEquals(error, detail.getLast(), "Error message does not match or did not appear as expected");
        }

        int eaIndex = Integer.parseInt(actions.getEAddressInfo(page, eaType).get("index"));

        page.clickDataBlockUpdateButton(FacilitySection.ELECTRONIC_ADDRESSES, eaIndex);
        page.clickDialogSubmitButton(FacilitySection.ELECTRONIC_ADDRESSES);
        String error = page.waitErrorMessage(FacilitySection.ELECTRONIC_ADDRESSES);

        assertEquals(error, errMsg5000EndReason, "End Reason error message did not appear as expected");

        expectedEmail = generateEmail();

        page.updateElectronicAddressDataBlock(expectedEmail, effective_date(), "", eaIndex,false);

        actions.verifyMandatoryAttributesEAddress(page, eaType, expectedEmail);
    }

    @Test
    // F4-036. Optional Electronic Address Attributes
    public void optionalEAddressAttributes()
    {
        final ElectronicAddressType eaType = ElectronicAddressType.FTP;
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        page.addElectronicAddressDataBlock(eaType.getText(), generateHTTP(),
                effective_date(), increment_month_for_effective_date(), false);

        LinkedHashMap<String,String> eaInfo = actions.getEAddressInfo(page, eaType);
        int eaIndex = Integer.parseInt(eaInfo.get("index"));

        assertEquals(eaInfo.get(EAddressField.EFFECTIVE_TO.getString()), increment_month_for_effective_date(),
                "Effective To field failed to add as expected");

        page.updateElectronicAddressDataBlock(generateHTTP(),
                effective_date(), increment_year_for_effective_date(), eaIndex, false);

        eaInfo = actions.getEAddressInfo(page, eaType);

        assertEquals(eaInfo.get(EAddressField.EFFECTIVE_TO.getString()), increment_year_for_effective_date(),
                "Effective To field failed to update as expected");
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
    public void validateRelatedOrganizationIdAndRelationshipTypeCodeCombination()
    {
         //Step 1 - Open facility details screen
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        //Step 2 - Create a new organization relationship using a related provider identifier and a relationship type.
        final OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig()
                .withName();

        MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);

        page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org.getIdentifier(),
                RelationshipType.LOCATION.getText(), effective_date(), "", false);

        String orgRelIdentifier = page.grabOrgRelationshipsBlockContent(0)
                                        .get(OrgRelationshipField.RELATED_ORGANIZATION_IDENTIFIER.getString());

        String orgRelType = page.grabOrgRelationshipsBlockContent(0)
                                        .get(OrgRelationshipField.RELATIONSHIP_TYPE.getString());

        assertTrue(org.getIdentifier().contains(orgRelIdentifier), 
        "Relationship Identifier does not match the Organization Identifier used to create");

        assertEquals(orgRelType, RelationshipType.LOCATION.getBlockText(), 
        "Relationship Type does not match the used to create");

        //Step 3 - Create a new organization relationship using a different organization and the same relationship type.
        MaintainOrgBuilder org2 = fhirController.createOrganization(orgConfig);

        page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org2.getIdentifier(),
                RelationshipType.LOCATION.getText(), effective_date(), "", false);

        String org2RelIdentifier = page.grabOrgRelationshipsBlockContent(1)
                                        .get(OrgRelationshipField.RELATED_ORGANIZATION_IDENTIFIER.getString());

        String org2RelType = page.grabOrgRelationshipsBlockContent(1)
                                        .get(OrgRelationshipField.RELATIONSHIP_TYPE.getString());

        assertTrue(org2.getIdentifier().contains(org2RelIdentifier), 
        "Relationship Identifier does not match the Organization Identifier used to create");

        assertEquals(org2RelType, RelationshipType.LOCATION.getBlockText(), 
        "Relationship Type does not match the used to create");

        //Step 4 - Create a new organization relationship using the same organization and a different relationship type.
        page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org2.getIdentifier(),
                RelationshipType.LOCATED.getText(), effective_date(), "", false);

        String org2Rel2Identifier = page.grabOrgRelationshipsBlockContent(0)
                                        .get(OrgRelationshipField.RELATED_ORGANIZATION_IDENTIFIER.getString());

        String org2Rel2Type = page.grabOrgRelationshipsBlockContent(0)
                                        .get(OrgRelationshipField.RELATIONSHIP_TYPE.getString());

        assertTrue(org2.getIdentifier().contains(org2Rel2Identifier), 
        "Relationship Identifier does not match the Organization Identifier used to create");

        assertEquals(org2Rel2Type, RelationshipType.LOCATED.getBlockText(), 
        "Relationship Type does not match the used to create");

        //Step 5 - Attempt to create another organization relationship using the same organization and the same relationship type.
        final String errMsg7033DupRelIdentifier = errorList.getString("errMsg7033Dup");

        String error = page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org2.getIdentifier(),
                RelationshipType.LOCATED.getText(), effective_date(), "", true);

        assertEquals(error, errMsg7033DupRelIdentifier, 
                "Error message does not match expected result. Should not be possible to add duplicate F2O relationships.");
        
        //Step 6 - Attempt to create another organization relationship using a different related provider identifier type (e.g. ORGID vs. IPC) but for the same organization, and the same relationship type as in the previous step.
        
        //Orgs will have autogenerated an IPC and a CPM by default. The difference is the inital format i.e IPC.########.BC.PRS to CPN.########.BC.PRS
        String org2CPN = org2.getIdentifier().replace("IPC", "CPN");

        error = page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.CPN.getText(), org2CPN,
                RelationshipType.LOCATED.getText(), effective_date(), "", true);

        assertEquals(error, errMsg7033DupRelIdentifier, 
                "Error message does not match expected result. Should not be possible to add duplicate F2O relationships.");

    }
}
