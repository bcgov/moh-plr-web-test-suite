package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper.*;
import static org.testng.Assert.*;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.AddressType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ConditionType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ElectronicAddressPurpose;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ElectronicAddressType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.TelecommunicationPurpose;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.TelecommunicationType;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpdateProviderTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    public static JSONObject errorList;

    private static final Map<ProviderType, MaintainRequestBuilder> defaultProviders = new LinkedHashMap<>();
    private static final int MAX_DIS_ACTION_DES = 3000;
    private UpdateProviderTests() {
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
    public void teardown() {
    	 workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }

    @BeforeMethod
    public void before(Object[] parameters) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
        if (!workflow.isLoggedIn()) {
            workflow.login().openPlr();
        }
    }

    @BeforeTest
    public void beforeTest() {
        FHIRController fhirController = new FHIRController(UserType.ADMIN);
        
        // Setup practitioners
        MaintainIndividualBuilder defaultBC = fhirController
                .createIndividual(new IndividualMaintainConfig(IndividualRoleType.OPT));
        LOG.info("Created default BC provider with IPC: {}", defaultBC.getIdentifier(IdentifierType.IPC));
        MaintainIndividualBuilder defaultOOP = fhirController
                .createIndividual(new IndividualMaintainConfig(IndividualRoleType.OOP_RECT));
        LOG.info("Created default OOP provider with IPC: {}", defaultOOP.getIdentifier(IdentifierType.IPC));
        MaintainOrgBuilder defaultOrg = fhirController.createOrganization(OrgRoleType.HDS);
        LOG.info("Created default organization provider with IPC: {}", defaultOrg.getIdentifier(IdentifierType.IPC));
        defaultProviders.put(ProviderType.BC_PRACTITIONER, defaultBC);
        defaultProviders.put(ProviderType.OOP_PRACTITIONER, defaultOOP);
        defaultProviders.put(ProviderType.ORGANIZATION, defaultOrg);
        
        // Log all providers for debugging
        LOG.info("Provider Map - BC: {}, OOP: {}, ORG: {}", 
                getIdentifierFromBuilder(defaultProviders, ProviderType.BC_PRACTITIONER),
                getIdentifierFromBuilder(defaultProviders, ProviderType.OOP_PRACTITIONER),
                getIdentifierFromBuilder(defaultProviders, ProviderType.ORGANIZATION));
        
        fhirController.close();
    }

    // Update Provider - Add Conditions
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testAddConditions(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addConditionDataBlock("LOC", "99999", true,
                "Test Explanation", effective_date(), increment_year_for_effective_date(), false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 1,
                "Expected 1 active condition data block");

        page.ceaseDataBlock(ProviderSection.CONDITIONS, 0);

        page.clickHeaderAddButton(ProviderSection.CONDITIONS);
        page.fillConditionDataBlock("LOC", "99999", true,
                "Test Explanation", effective_date(), increment_year_for_effective_date());
        page.clickDialogCancelButton(ProviderSection.CONDITIONS);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 0,
                "Expected no active condition data blocks after cancelling add");
    }

    // Update Provider - Generating a Default Condition ID
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testGenerateDefaultConditionID(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addConditionDataBlock("LOC", null, false, "Test",
                effective_date(), increment_year_for_effective_date(), false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 1,
                "Expected 1 active condition data block after adding condition with no identifier");

        String condIdentifier = page.grabDataBlockContent(ProviderSection.CONDITIONS, 0).get("Identifier");
        assertTrue(condIdentifier.matches("CDN\\.\\d{1,6}\\.PRS"),
                "Expected generated condition identifier to match pattern 'CDN.####.PRS'");

        page.ceaseDataBlock(ProviderSection.CONDITIONS, 0);
    }

    // Update Provider - Validate Condition ID
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateConditionID(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        String error = page.addConditionDataBlock("LOC", generateNumericString(241), false,
                "Test", effective_date(), increment_year_for_effective_date(), true);

        assertEquals(error, errorList.get("conditionIdentifierTooLong"),
                "Expected error for condition identifier exceeding max length");

        page.addConditionDataBlock("LOC", null, false, "Test",
                effective_date(), increment_year_for_effective_date(), false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 1,
                "Expected 1 active condition data block after adding condition with no identifier");

        String condIdentifier = page.grabDataBlockContent(ProviderSection.CONDITIONS, 0).get("Identifier");
        assertFalse(condIdentifier.isEmpty(),
                "Expected generated condition identifier when adding condition with no identifier");

        page.ceaseDataBlock(ProviderSection.CONDITIONS, 0);

        page.addConditionDataBlock("LOC", generateNumericString(240), false,
                "Test", effective_date(), increment_year_for_effective_date(), false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 1,
                "Expected 1 active condition data block after adding valid identifier");

        page.ceaseDataBlock(ProviderSection.CONDITIONS, 0);
    }

    // Update Provider - Validate Condition Restriction Flag
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateConditionRestrictionFlag(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addConditionDataBlock("LOC", "99999", true,
                "Test", effective_date(), increment_year_for_effective_date(), false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 1,
                "Expected 1 active condition data block with restriction flag set to true");
        assertEquals(page.grabDataBlockContent(ProviderSection.CONDITIONS, 0).get("Restriction Flag"), "Yes",
                "Expected 'Yes' value for restriction flag in data block when set to true");

        page.ceaseDataBlock(ProviderSection.CONDITIONS, 0);

        page.addConditionDataBlock("LOC", "99999", false,
                "Test", effective_date(), increment_year_for_effective_date(), false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 1,
                "Expected 1 active condition data block with restriction flag set to true");
        assertEquals(page.grabDataBlockContent(ProviderSection.CONDITIONS, 0).get("Restriction Flag"), "No",
                "Expected 'No' value for restriction flag in data block when set to false");
    }

    // Update Provider - Validate Condition Type Code
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateConditionTypeCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.clickHeaderAddButton(ProviderSection.CONDITIONS);

        assertTrue(page.getMandatoryFields(ProviderSection.CONDITIONS).contains("Condition Type"),
                "Expected 'Condition Type' to be a mandatory field when adding a condition data block");

        List<String> condOptions = page.getDropdownListOptions(ProviderSection.CONDITIONS, "conditionType");
        condOptions.remove("Select One");
        List<String> expectedOptions = Arrays.stream(ConditionType.values()).map(ConditionType::getText).toList();
        assertTrue(condOptions.containsAll(expectedOptions),
                "Expected condition type dropdown options to contain all defined condition types");

        page.clickDialogCancelButton(ProviderSection.CONDITIONS);

        String error = page.addConditionDataBlock("Select One", "99999", false,
                "Test", effective_date(), increment_year_for_effective_date(), true);

        assertEquals(error, errorList.get("missingConditionType"),
                "Expected error message for missing condition type when adding condition data block");

        page.addConditionDataBlock("LOC", "99999", false,
                "Test", effective_date(), increment_year_for_effective_date(), false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 1,
                "Expected 1 active condition data block after adding condition with valid type");
        assertEquals(page.grabDataBlockContent(ProviderSection.CONDITIONS, 0).get("Type"), "Location (LOC)",
                "Expected 'LOC' value for condition type in data block after adding condition with LOC type");

        page.ceaseDataBlock(ProviderSection.CONDITIONS, 0);
    }
    
	// Update Provider --Add Disciplinary Actions
	@Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
	public void testAddDisciplinaryAction(ProviderType providerType) {

		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

		String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
		UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

		page.ceaseAllDataBlockUnderSection(ProviderSection.DISCIPLINARY_ACTIONS);

    	String actionIdentifier = "actionId" + UpdateSimpleHelper.generateAlphabetNumericString(4);
		page.addDisciplinaryActionDataBlock(actionIdentifier, true, "description",
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), false);
		assertEquals(page.grabActiveDataBlockCount(ProviderSection.DISCIPLINARY_ACTIONS, true), 1,
				"Expected 1 active data block after adding first disciplinary action");

		actionIdentifier = "actionId" + UpdateSimpleHelper.generateAlphabetNumericString(4);
		page.cancleAddDisciplinaryActionDataBlock(actionIdentifier, true, "description",
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date());
		assertEquals(page.grabActiveDataBlockCount(ProviderSection.DISCIPLINARY_ACTIONS, true), 1,
				"Expected 1 active data block after canceling second disciplinary action adding");
	}

	//Update Provider - Validate Disciplinary Action
	@Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
	public void testValidateDisciplinaryAction(ProviderType providerType) {

		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

		String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
		UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);
		page.ceaseAllDataBlockUnderSection(ProviderSection.DISCIPLINARY_ACTIONS);

		String actionIdentifier = "actionId1" + UpdateSimpleHelper.generateAlphabetNumericString(4);
		page.addDisciplinaryActionDataBlock(actionIdentifier, true, UpdateSimpleHelper.generateAlphabetNumericString(40),
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.effective_date(), "", false);
		assertEquals(page.grabActiveDataBlockCount(ProviderSection.DISCIPLINARY_ACTIONS, true), 1,
				"Expected 1 active data block after adding disciplinary action once");

		actionIdentifier = "actionId2" + UpdateSimpleHelper.generateAlphabetNumericString(4);
		page.addDisciplinaryActionDataBlock(actionIdentifier, true, "description",
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.effective_date(), "", false);
		assertEquals(page.grabActiveDataBlockCount(ProviderSection.DISCIPLINARY_ACTIONS, true), 2,
				"Expected 2 active data block after adding disciplinary action twice");
	}
	// Update Provider - Validate Disciplinary Action Description Text
	@Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
	public void testValidateDisciplinaryActionDescriptionText(ProviderType providerType) {
		
		String errorMsgDisActionDesLenth5003 = (String) errorList.get("errorMsgDisActionDesLenth5003");
		String errorMsgDisActionDesLenthMissing5000 = (String) errorList.get("errorMsgDisActionDesLenthMissing5000");

		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

		String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
		UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);
		page.ceaseAllDataBlockUnderSection(ProviderSection.DISCIPLINARY_ACTIONS);

		String msg=page.addDisciplinaryActionDataBlock(null, true, UpdateSimpleHelper.generateAlphabetNumericString(MAX_DIS_ACTION_DES+1),
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.effective_date(), "", true);
        assertEquals(errorMsgDisActionDesLenth5003, msg, "Expected error message not found");
		
		msg=page.addDisciplinaryActionDataBlock(null, true, UpdateSimpleHelper.generateAlphabetNumericString(MAX_DIS_ACTION_DES),
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(msg), "Unexpected error on adding Disciplinary Action Data Block");

		msg=page.addDisciplinaryActionDataBlock(null, true, null,
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.effective_date(), "", true);
        assertEquals(errorMsgDisActionDesLenthMissing5000, msg, "Expected error message not found");
	}

	// Update Provider - Generating A Default Disciplinary Action ID
	@Test(dataProvider = "practitioners",dataProviderClass = InjectableData.class)
	public void testGeneratingDefaultDisciplinaryActionID(ProviderType providerType) {

		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

		String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
		UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);
		page.ceaseAllDataBlockUnderSection(ProviderSection.DISCIPLINARY_ACTIONS);

		String msg=page.addDisciplinaryActionDataBlock(null, true, UpdateSimpleHelper.generateAlphabetNumericString(40),
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(msg), "Unexpected error on adding Disciplinary Action Data Block");

		LinkedHashMap<String, String> content = page.grabDataBlockContent(ProviderSection.DISCIPLINARY_ACTIONS, 0);
		String idString = content.get("Identifier");

		assertTrue(idString.startsWith("DA."),"The default ID should follows this pattern DA.X.PRS, where X is a unique positive integer");
		assertTrue(idString.endsWith(".PRS"),"The default ID should follows this pattern DA.X.PRS, where X is a unique positive integer");
		int startIndex = idString.indexOf('.');
		int endIndex = idString.indexOf('.', startIndex + 1);
		String numString = idString.substring(startIndex + 1, endIndex);
		assertTrue(UpdateSimpleHelper.isStringPositiveInteger(numString),
				"The default ID should follows this pattern DA.X.PRS, where X is a unique positive integer");		 
	}

	// Update Provider - Validate Provider Conditions
	@Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
	public void testValidateProviderConditions(ProviderType providerType) {
		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

		String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
		UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

		assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 0,
				"Expected no active condition data blocks for provider initially");

		page.addConditionDataBlock("LOC", "99999", false, "Test", effective_date(), increment_year_for_effective_date(),
				false);

		assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 1,
				"Expected 1 active condition data block after adding condition to provider");

		page.addConditionDataBlock("EXP", "99998", true, "Test 2", effective_date(),
				increment_year_for_effective_date(), false);

		assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 2,
				"Expected 2 active condition data blocks after adding second condition to provider");
	}

    // Update Provider - Validate Restriction Explanation
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateRestrictionExplanation(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        String error = page.addConditionDataBlock("LOC", "99999", true,
                generateAlphabetNumericString(241),
                effective_date(), increment_year_for_effective_date(), true);

        assertEquals(error, errorList.get("explanationTooLong"),
                "Expected error message for restriction explanation exceeding max length when adding condition data block");

        page.addConditionDataBlock("LOC", "99999", true,
                null, effective_date(), increment_year_for_effective_date(), false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.CONDITIONS, true), 1,
                "Expected 1 active condition data block after adding condition with no restriction explanation");

        page.ceaseDataBlock(ProviderSection.CONDITIONS, 0);
    }

    // Update Provider - Add block - Add Addresses
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testAddAddresses(ProviderType providerType) {
        //Add valid address block 

        //Open dialog to add address block and cancel. Check address was not added.

    }

    // Update Provider - Add block - Add Electronic Addresses
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testAddElectronicAddresses(ProviderType providerType) {
        //Add valid electronic address block 

        //Open dialog to add electronic address block and cancel. Check electronic address was not added.


    }

    // Update Provider - Add block - Add Telecommunications
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testAddTelecommunications(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Add valid telecommunication block
        page.addTelecommunicationDataBlock(
                TelecommunicationType.PHONE.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "604",
                "5551234",
                "100",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.TELECOMMUNICATIONS, true), 1,
                "Expected 1 active telecommunication data block");

        page.ceaseDataBlock(ProviderSection.TELECOMMUNICATIONS, 0);

        // Open dialog to add telecommunication block and cancel. Check telecommunication was not added.
        page.cancelAddTelecommunicationDataBlock(
                TelecommunicationType.MOBILE.getText(),
                TelecommunicationPurpose.HOME_CONTACT.getText(),
                "778",
                "5555678",
                null,
                effective_date(),
                increment_year_for_effective_date());

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.TELECOMMUNICATIONS, true), 0,
                "Expected no active telecommunication data blocks after cancelling add");
    }

    // Update Provider - Add block - General Address Validation
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testGeneralAddressValidation(ProviderType providerType) {
        //Add address with address line 1 but without address line 2 and 3 - success

        //Add address without address line 1 but with address line 2 - fail

        //Add address in line 1 that contains a space between # and # eg 456 789 Main St- success

        //Add address using address that includes the post office box and station information in line 1 address

        //Add address that includes street type abbreviated
    }

    // Update Provider - Add block - Validate Address Line One
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressLineOne(ProviderType providerType) {
        //Enter address with line 1 exceeding 70 characters

        //Enter address with line 1 blank

        //Enter address with line 1 as "NO FIXED ADDRESS" - Success

        //Enter address with line 1 a "UNKNOWN" -Success

        //Enter address line 1 as "NA" - Success

    }

    // Update Provider - Add block - Validate Address Purpose Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressPurposeCode(ProviderType providerType) {
        //Verify purpose code is mandatory by not settin a value

        //Verify the code set available for Purpose code

        //Add address with purpose code
    }

    // Update Provider - Add block - Validate Address Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressTypeCode(ProviderType providerType) {
        //Verify type code is mandatory by not settin a value

        //Verify the code set available for Type code

        //Add address with type code

}

    // Update Provider - Add block - Validate Address Uniqueness Rules
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressUniquenessRules(ProviderType providerType) {
        //Add address with mailing address type and MC purpose code

        //Add address with mailing address type and MC contact purpose code - fail due to uniqueness rules

        //Repeat previous steps with all purpose codes for mailing address type to verify uniqueness rules are working as expected for all purpose codes

        //Repeat all steps for phyisical address type and verify uniqueness rules are working as expected
}

    // Update Provider - Add block - Validate City
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateCity(ProviderType providerType) {
        //Enter address with city blank

        //Enter address with more than 60 characters

        //Enter address with city = to 60 characters

    }

    // Update Provider - Add block - Validate Communication Purpose Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateCommunicationPurposeTypeCode(ProviderType providerType) {
        //add telecomunication without purpose code - failure

        //add electronic address without purpose code - failure

        //add address without purpose code - failure

    }

    // Update Provider - Add block - Validate Electronic Address Txt
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateElectronicAddressTxt(ProviderType providerType) {
        //add electronic address with more than 500 characters - failure

        //add electronic address with field blank - failure

        //add electronic address with invalid email format - failure

        //add electronic address with email with one or more periods before the @ - success

        //add electronic address using email with 500 charactes - success

    }

    // Update Provider - Add block - Validate Electronic Address Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateElectronicAddressTypeCode(ProviderType providerType) {
    
        //add electronic address without type code - failure

        //add electronic address with valid type code - success

    }

    // Update Provider - Add block - Validate Postal Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidatePostalCode(ProviderType providerType) {
        //Enter address with country as Brazil - postal code exceeding 25 characters - failure

        //Enter address with country as Canada - postal code exceeding 25 characters - failure

        //Enter address with country as Canada - with postal code as ANA NAN - success

    }

    // Update Provider - Add block - Validate Province
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateProvince(ProviderType providerType) {
        //Enter address with country as Canada - with valid province code - success

        //Enter address with country as Canada - with invalid province code - failure

        //Enter address with country as Brazil - with valid state code - success

        //Enter address with country as Brazil - with invalid state code - failure

    }

    // Update Provider - Add block - Validate Province and State Address Codes with Country
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateProvinceAndStateAddressCodesWithCountry(ProviderType providerType) {
    
        //In the add address popup verify CA has all valid provinces

        //In the add address popup verify US has all valid states

        //Verify that when a country other than CA or US is selected, the province/state field is a free text field

        //With address other than CA or US, try to send an address without province/state - failure

        //With address other than CA or US, try to send an address with provnce > 30 characters - failure

        //Repeat above with province = 30 characters - success

    }

    // Update Provider - Add block - Validate Telecommunication Number
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateTelecommunicationNumber(ProviderType providerType) {
        //Try to add telecommunication with extension > 15 characters

        //Try to add teleocmmunication with number > 30 characters

        //Try to add telecommunication with area code > 15 characters

        //Add telecom with area code = 15 characters, nuiber 30 characters and extension 30 characters - success
    }

    // Update Provider - Add block - Validate Telecom Uniqueness Rules
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateTelecomUniquenessRules(ProviderType providerType) {
        //Add telecom as fax and type MC

        //Add telecom as fax and type MC contact - fail due to uniqueness rules

        //repeat above with all combinations of telecom type and purpose type to verify uniqueness rules are working as expected.
    }

    // Update Provider - Add block - Validate Telecommunication Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateTelecommunicationTypeCode(ProviderType providerType) {
        //Check that in the telecommunication type code dropdown, the options available are as expected

        //Try to add telecommunciation without type - failure

    }

    // Update Provider - Add block - Validate eAddress Uniqueness Rules
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateEAddressUniquenessRules(ProviderType providerType) {
        //Add electronic address with type ftp and purpose MC

        //Add electronic address with type email and purpose MC contact - fail due to uniqueness rules

        //repeat above with all combinations of electronic address type and purpose type to verify uniqueness rules are working as expected.

    }

    
}
