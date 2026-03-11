package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper.*;
import static org.testng.Assert.*;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ConditionType;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
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

    private static final Map<ProviderType, MaintainIndividualBuilder> defaultProviders = new HashMap<>();
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
        //workflowManager_.logoutAllAndClose();
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
        MaintainIndividualBuilder defaultBC = fhirController
                .createIndividual(new IndividualMaintainConfig(IndividualRoleType.OPT));
        LOG.info("Created default BC provider with IPC: {}", defaultBC.getIdentifier(IdentifierType.IPC));
        MaintainIndividualBuilder defaultOOP = fhirController
                .createIndividual(new IndividualMaintainConfig(IndividualRoleType.OOP_RECT));
        LOG.info("Created default OOP provider with IPC: {}", defaultBC.getIdentifier(IdentifierType.IPC));
        fhirController.close();

        defaultProviders.put(ProviderType.BC_PRACTITIONER, defaultBC);
        defaultProviders.put(ProviderType.OOP_PRACTITIONER, defaultOOP);
    }

    // Update Provider - Add Conditions
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testAddConditions(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

    // Update Provider - Add Work Locations
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testAddWorkLocations(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        WebElement dialog = page.clickHeaderAddButton(ProviderSection.WORK_LOCATIONS);
        assertTrue(dialog.isDisplayed(), "Expected work location dialog to be displayed after clicking add button");

        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);

        page.addWorkLocationDataBlock("12345", true, "Test Name", "CC", "Test Info", false);
        assertEquals(page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true), 1,
                "Expected 1 active work location data block after adding work location");

        page.clickHeaderAddButton(ProviderSection.WORK_LOCATIONS);
        page.fillWorkLocationDataBlock("12345", true, "Test Name", "CC", "Test Info");
        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true), 1,
                "Expected 1 active work location data block after cancelling add of second work location");

        // cleanup for if test cases are done in sequence
        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
    }

    // Update Provider - Generating a Default Condition ID
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testGenerateDefaultConditionID(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

		String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
		UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

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

		String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
		UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

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

		String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
		UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);
		
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

		String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
		UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

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

		String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

    // Update Provider - Validate Work Location
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocation(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addWorkLocationDataBlock("12345", true, "Test Name", "CC", "Test Info", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true), 1,
                "Expected 1 active work location data block after adding work location with valid code");

        page.addWorkLocationDataBlock("1234", true, "Test Name", "CC", "Test Info", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true), 2,
                "Expected 2 active work locations data block after adding work location with valid code");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 1);
        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
    }
}
