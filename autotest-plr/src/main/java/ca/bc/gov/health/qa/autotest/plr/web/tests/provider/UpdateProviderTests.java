package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper.*;
import static org.testng.Assert.*;
import static ca.bc.gov.health.qa.autotest.plr.data.UpdateProviderConstants.*;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
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
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ConditionType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
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
    private static MaintainOrgBuilder defaultOrg;
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
        MaintainIndividualBuilder defaultBC = fhirController
                .createIndividual(new IndividualMaintainConfig(IndividualRoleType.OPT));
        LOG.info("Created default BC provider with IPC: {}", defaultBC.getIdentifier(IdentifierType.IPC));
        MaintainIndividualBuilder defaultOOP = fhirController
                .createIndividual(new IndividualMaintainConfig(IndividualRoleType.OOP_RECT));
        LOG.info("Created default OOP provider with IPC: {}", defaultBC.getIdentifier(IdentifierType.IPC));
        defaultOrg = fhirController
                .createOrganization(new OrganizationMaintainConfig(OrgRoleType.ORG));
        LOG.info("Created default organization with IPC: {}", defaultOrg.getIdentifier(IdentifierType.IPC));
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

    // Update Provider - Add Provider Relationships
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testAddProviderRelationships(ProviderType providerType)
    {
        final MaintainIndividualBuilder otherProvider = switch (providerType) {
            case BC_PRACTITIONER -> defaultProviders.get(ProviderType.OOP_PRACTITIONER);
            case OOP_PRACTITIONER -> defaultProviders.get(ProviderType.BC_PRACTITIONER);
            default -> new MaintainIndividualBuilder(); // should not occur
        };
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        WebElement dialog = page.clickHeaderAddButton(ProviderSection.PROVIDER_RELATIONSHIPS);
        assertTrue(dialog.isDisplayed(), "Expected provider relationship dialog to be displayed after clicking add button");
        page.clickDialogCancelButton(ProviderSection.PROVIDER_RELATIONSHIPS);

        page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "LOC", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.PROVIDER_RELATIONSHIPS, true), 1,
                "Expected 1 active provider relationship data block after adding provider relationship");

        page.clickHeaderAddButton(ProviderSection.PROVIDER_RELATIONSHIPS);
        page.fillProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "ER");
        page.clickDialogCancelButton(ProviderSection.PROVIDER_RELATIONSHIPS);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.PROVIDER_RELATIONSHIPS, true), 1,
                "Expected 1 active provider relationship data block after cancelling add of second provider relationship");

        page.ceaseDataBlock(ProviderSection.PROVIDER_RELATIONSHIPS, 0);
    }

    // Update Provider - Add Registry User Relationships
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testAddRegUserRelationships(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        WebElement dialog = page.clickHeaderAddButton(ProviderSection.REGISTRY_USER_RELATIONSHIPS);
        assertTrue(dialog.isDisplayed(), "Expected registry user relationship dialog to be displayed after clicking add button");

        page.clickDialogCancelButton(ProviderSection.REGISTRY_USER_RELATIONSHIPS);

        page.addRegUserRelationshipDataBlock("RES", "00002855", UserType.ADMIN, false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.REGISTRY_USER_RELATIONSHIPS, true), 1,
                "Expected 1 active registry user relationship data block after adding registry user relationship");

        page.clickHeaderAddButton(ProviderSection.REGISTRY_USER_RELATIONSHIPS);
        page.fillRegUserRelationshipDataBlock("RES", "00002855", UserType.ADMIN);
        page.clickDialogCancelButton(ProviderSection.REGISTRY_USER_RELATIONSHIPS);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.REGISTRY_USER_RELATIONSHIPS, true), 1,
                "Expected 1 active registry user relationship data block after cancelling add of second registry user relationship");

        page.ceaseDataBlock(ProviderSection.REGISTRY_USER_RELATIONSHIPS, 0);
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

		String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

		String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

		String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
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

    // Update Provider - Generating a Work Location ID
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testGeneratingDefaultWorkLocationID(ProviderType providerType)
    {
        PlrWebWorkflow workflow = logIn(workflowManager_, UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);
        page.addWorkLocationDataBlock(null, false, "Test Name", "CC", null, false);

        logIn(workflowManager_, UserType.PRIMARY);

        UpdateProviderPage primaryPage = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        primaryPage.addWorkLocationDataBlock(null, false, "Test Name", "CC", null, false);

        page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addWorkLocationDataBlock(null, false, "Test Name", "CC", null, false);

        Map<String,String> wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);

        assertEquals(wlContent.get("Identifier"), "1",
                "Expected first work location to have generated identifier \"1\"");

        wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 1);

        assertEquals(wlContent.get("Identifier"), "2",
                "Expected first work location to have generated identifier \"2\"");

        wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 2);

        assertEquals(wlContent.get("Identifier"), "3",
                "Expected first work location to have generated identifier \"3\"");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 2);
        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 1);
        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
    }

    // Update Provider - Provider Relationship Types
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testProviderRelationshipTypes(ProviderType providerType)
    {
        final MaintainIndividualBuilder otherProvider = switch (providerType) {
            case BC_PRACTITIONER -> defaultProviders.get(ProviderType.OOP_PRACTITIONER);
            case OOP_PRACTITIONER -> defaultProviders.get(ProviderType.BC_PRACTITIONER);
            default -> new MaintainIndividualBuilder(); // should not occur
        };
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "ER", false);

        Map<String,String> prContent = page.grabDataBlockContent(ProviderSection.PROVIDER_RELATIONSHIPS, 0);

        assertEquals(prContent.get("Relationship Type"), "Employer (ER)",
                "Expected relationship type to be 'Employer (ER)' after adding provider relationship with ER type");

        page = viewByIdentifierAsUpdateProvider(otherProvider.getIdentifier(IdentifierType.IPC), workflowManager_);

        prContent = page.grabDataBlockContent(ProviderSection.PROVIDER_RELATIONSHIPS, 0);

        assertEquals(prContent.get("Relationship Type"), "Employee (EE)",
                "Expected relationship type to be 'Employee (EE)' on related provider after adding provider relationship with ER type");

        page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.ceaseDataBlock(ProviderSection.PROVIDER_RELATIONSHIPS, 0);
    }

    // Update Provider - Provider to Provider Relationship Validation
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testProviderRelationshipValidation(ProviderType providerType)
    {
        final MaintainIndividualBuilder otherProvider = switch (providerType) {
            case BC_PRACTITIONER -> defaultProviders.get(ProviderType.OOP_PRACTITIONER);
            case OOP_PRACTITIONER -> defaultProviders.get(ProviderType.BC_PRACTITIONER);
            default -> new MaintainIndividualBuilder(); // should not occur
        };
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        String error = page.addProviderRelationshipDataBlock(null, otherProvider.getIdentifier(IdentifierType.IPC),
                "LOC", true);

        assertEquals(error, errorList.get("erromMessageGRS5000IdType"),
                "Expected error message for missing relationship type when adding provider relationship");

        error = page.addProviderRelationshipDataBlock(IdentifierType.IPC, null, "LOC", true);

        assertEquals(error, errorList.get("erromMessageGRS5000Id"),
                "Expected error message for missing identifier when adding provider relationship");

        error = page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "Select One", true);

        assertEquals(error, errorList.get("errMsg5000RelType"),
                "Expected error message for missing relationship type when adding provider relationship");

        page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "LOC", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.PROVIDER_RELATIONSHIPS, true), 1,
                "Expected 1 active provider relationship data block after adding valid provider relationship");

        page.ceaseDataBlock(ProviderSection.PROVIDER_RELATIONSHIPS, 0);
    }

    // Update Provider - Provider to Registry User Relationship
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testProviderRegistryUserRelationship(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        String error = page.addRegUserRelationshipDataBlock(null, "00002855", UserType.ADMIN, true);

        assertEquals(error, errorList.get("registryTypeMissing"),
                "Expected error message for missing relationship type when adding registry user relationship");

        error = page.addRegUserRelationshipDataBlock("RES", null, UserType.ADMIN, true);

        assertEquals(error, errorList.get("registryIdMissing"),
                "Expected error message for missing identifier when adding registry user relationship");

        error = page.addRegUserRelationshipDataBlock("RES", "00002855", null, true);

        assertEquals(error, errorList.get("registryUserTypeMissing"),
                "Expected error message for missing user type when adding registry user relationship");

        error = page.addRegUserRelationshipDataBlock("RES", "nonexistentid", UserType.ADMIN, true);

        assertEquals(error, errorList.get("registryUserDoesNotExist"),
                "Expected error message for non-existent registry user when adding registry user relationship");

        page.addRegUserRelationshipDataBlock("RES", "00002855", UserType.ADMIN, false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.REGISTRY_USER_RELATIONSHIPS, true), 1,
                "Expected 1 active registry user relationship data block after adding valid registry user relationship");

        page.ceaseDataBlock(ProviderSection.REGISTRY_USER_RELATIONSHIPS, 0);
    }

    // Update Provider - Update Work Locations
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testUpdateWorkLocations(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier;
        if (providerType.equals(ProviderType.ORGANIZATION)) identifier = defaultOrg.getIdentifier(IdentifierType.IPC);
        else identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);

        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addWorkLocationDataBlock("12345", true, "Test Name", "CC", "Test Info", false);

        WebElement dialog = page.clickDataBlockUpdateButton(ProviderSection.WORK_LOCATIONS, 0);
        assertTrue(dialog.isDisplayed(), "Expected work location dialog to be displayed after clicking update button on work location data block");

        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);

        page.updateWorkLocationDataBlock(false, "Updated Name", "HID", "Updated Info", EndReason.CHG, false);

        Map<String,String> wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);
        final String details = "Work Location Details-0-";

        LOG.info(wlContent);

        assertEquals(wlContent.get(details+"Name"), "Updated Name",
                "Expected updated name to be reflected in work location data block after updating work location");
        assertEquals(wlContent.get(details+"Type"), "Health Information Distribution",
                "Expected updated type to be reflected in work location data block after updating work location");
        assertEquals(wlContent.get(details+"Default Flag"), "No",
                "Expected updated default flag to be reflected in work location data block after updating work location");
        assertEquals(wlContent.get(details+"Additional Info"), "Updated Info",
                "Expected updated additional info to be reflected in work location data block after updating work location");
        assertEquals(wlContent.get(details+"Effective From"), "2000-01-01",
                "Expected updated effective from to be reflected in work location data block after updating work location");
        assertEquals(wlContent.get(details+"Effective To"), "2999-01-01",
                "Expected updated effective to to be reflected in work location data block after updating work location");

        page.clickDataBlockUpdateButton(ProviderSection.WORK_LOCATIONS, 0);
        page.fillWorkLocationDataBlockUpdate(true, "Test Name", "CC", "Test Info", EndReason.CHG);
        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true), 1,
                "Expected 1 active work location data block after cancelling update of work location");
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

    // Update Provider - Validate Provider Relationship Type Code
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateProviderRelationshipTypeCode(ProviderType providerType)
    {
        final MaintainIndividualBuilder otherProvider = switch (providerType) {
            case BC_PRACTITIONER -> defaultProviders.get(ProviderType.OOP_PRACTITIONER);
            case OOP_PRACTITIONER -> defaultProviders.get(ProviderType.BC_PRACTITIONER);
            default -> new MaintainIndividualBuilder(); // should not occur
        };
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.clickHeaderAddButton(ProviderSection.PROVIDER_RELATIONSHIPS);

        List<String> relationshipTypes = page.getDropdownListOptions(ProviderSection.PROVIDER_RELATIONSHIPS,
                "relationshipType");
        relationshipTypes.remove("Select One");

        assertTrue(relationshipTypes.containsAll(RELATIONSHIP_TYPE_OPTIONS),
                "Expected relationship type dropdown options to contain all defined relationship types");

        page.clickDialogCancelButton(ProviderSection.PROVIDER_RELATIONSHIPS);

        String error = page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "Select One", true);

        assertEquals(error, errorList.get("errMsg5000RelType"),
                "Expected error message for missing relationship type when adding provider relationship");

        page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "LOC", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.PROVIDER_RELATIONSHIPS, true), 1,
                "Expected 1 active provider relationship data block after adding provider relationship with valid type");

        page.ceaseDataBlock(ProviderSection.PROVIDER_RELATIONSHIPS, 0);
    }

    // Update Provider - Validate Related Provider ID
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateRelatedProviderID(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        String error = page.addProviderRelationshipDataBlock(IdentifierType.IPC, "test",
                "LOC", true);

        assertEquals(error, errorList.get("errMsg7036"),
                "Expected error message for invalid related provider identifier when adding provider relationship");

        error = page.addProviderRelationshipDataBlock(IdentifierType.IPC, null,
                "LOC", true);

        assertEquals(error, errorList.get("erromMessageGRS5000Id"),
                "Expected error message for missing related provider identifier when adding provider relationship");

        error = page.addProviderRelationshipDataBlock(IdentifierType.IPC, "IPC.00000000.BC.PRS!#%",
                "LOC", true);

        assertEquals(error, errorList.get("foreignCharacterIdentifier"),
                "Expected error message for invalid related provider identifier with special character when adding provider relationship");
    }

    // Update Provider - Validate Related Provider ID and Relationship
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testRelatedProviderIDAndRelationship(ProviderType providerType) {
        final MaintainIndividualBuilder otherProvider = switch (providerType) {
            case BC_PRACTITIONER -> defaultProviders.get(ProviderType.OOP_PRACTITIONER);
            case OOP_PRACTITIONER -> defaultProviders.get(ProviderType.BC_PRACTITIONER);
            default -> new MaintainIndividualBuilder(); // should not occur
        };
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "LOC", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.PROVIDER_RELATIONSHIPS, true), 1,
                "Expected 1 active provider relationship data block after adding provider relationship with valid related provider and relationship type");

        String error = page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "LOC", true);

        assertEquals(error, errorList.get("duplicateProviderRel"),
                "Expected error message for duplicate provider relationship when adding provider relationship with same related provider and relationship type");

        error = page.addProviderRelationshipDataBlock(IdentifierType.IPC, null,
                "LOC", true);

        assertEquals(error, errorList.get("erromMessageGRS5000Id"),
                "Expected error message for missing related provider identifier when adding provider relationship with duplicate relationship type");

        error = page.addProviderRelationshipDataBlock(IdentifierType.IPC, "test",
                "LOC", true);

        assertEquals(error, errorList.get("errMsg7036"),
                "Expected error message for invalid related provider identifier when adding provider relationship");

        page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "ER", false);

        page.addProviderRelationshipDataBlock(IdentifierType.IPC, otherProvider.getIdentifier(IdentifierType.IPC),
                "PHCST", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.PROVIDER_RELATIONSHIPS, true), 3,
                "Expected 3 active provider relationship data blocks after adding provider relationships with same related provider and different relationship types");

        page.ceaseDataBlock(ProviderSection.PROVIDER_RELATIONSHIPS, 2);
        page.ceaseDataBlock(ProviderSection.PROVIDER_RELATIONSHIPS, 1);
        page.ceaseDataBlock(ProviderSection.PROVIDER_RELATIONSHIPS, 0);
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

    // Update Provider - Validate Update Work Location Additional Addressee
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocationAdditionalAddresseeUpdate(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addWorkLocationDataBlock("12345", true, "Test Name", "CC", "Test Info", false);

        String error = page.updateWorkLocationDataBlock(true, "Test Name", "CC",
                generateAlphabetNumericString(241), EndReason.CHG, true);

        assertEquals(error, errorList.get("WLAddressInfoTooLong"),
                "Expected error message for addressee info exceeding max length when updating work location data block");

        page.updateWorkLocationDataBlock(false, "Test Name", "CC", "", EndReason.CHG, false);

        Map<String,String> wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);

        assertEquals(wlContent.get("Work Location Details-0-Additional Info"), "",
                "Expected empty string for additional addressee info in work location data block when no additional addressee info is provided during update");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
    }

    // Update Provider - Validate Work Location Additional Addressee
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocationAdditionalAddressee(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        String error = page.addWorkLocationDataBlock("12345", true, "Test Name", "CC",
                generateAlphabetNumericString(241), true);

        assertEquals(error, errorList.get("WLAddressInfoTooLong"),
                "Expected error message for addressee info exceeding max length when adding work location data block");

        page.addWorkLocationDataBlock("12345", true, "Test Name", "CC", null, false);

        Map<String,String> wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);

        assertEquals(wlContent.get("Work Location Details-0-Additional Info"), "",
                "Expected empty string for additional addressee info in work location data block when no additional addressee info is provided");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
    }

    // Update Provider - Validate Work Location Default Flag
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocationDefaultFlag(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.clickHeaderAddButton(ProviderSection.WORK_LOCATIONS);

        assertFalse(page.isCheckBoxChecked(ProviderSection.WORK_LOCATIONS, "defaultFlag"),
                "Expected default flag checkbox to be unchecked by default when adding work location data block");

        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);

        page.addWorkLocationDataBlock("12345", false, "Test Name", "CC", "Test Info", false);

        Map<String,String> wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);

        assertEquals(wlContent.get("Work Location Details-0-Default Flag"), "No",
                "Expected 'No' value for default flag in work location data block when default flag checkbox is unchecked");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);

        page.addWorkLocationDataBlock("123456", true, "Test Name", "CC", "Test Info", false);

        wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);

        assertEquals(wlContent.get("Work Location Details-0-Default Flag"), "Yes",
                "Expected 'Yes' value for default flag in work location data block when default flag checkbox is unchecked");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
    }

    // Update Provider - Validate Work Location ID
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocationID(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        String error = page.addWorkLocationDataBlock(generateNumericString(21), true,
                "Test Name", "CC", "Test Info", true);

        assertEquals(error, errorList.get("WLIDTooLong"),
                "Expected error message for work location identifier exceeding max length when adding work location data block");

        String expectedID = generateNumericString(15);

        page.addWorkLocationDataBlock(expectedID, true, "Test Name",
                "CC", "Test Info", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true), 1,
                "Expected 1 active work location data block after adding work location with valid identifier");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);

        page.addWorkLocationDataBlock(null, false, "Test Name", "CC", "Test Info", false);

        Map<String,String> wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true), 1,
                "Expected 1 active work location data block after adding work location with no identifier");
        assertEquals(wlContent.get("Identifier"), String.valueOf(Long.parseLong(expectedID)+1),
                "Expected generated identifier for work location data block when no identifier is provided");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);

        error = page.addWorkLocationDataBlock("-1", false, "Negative ID", "CC", "Test Info", true);

        // TODO replace with error message if possible
        assertFalse(error.isEmpty() || error.contains("successfully"),
                "Expected error message for invalid work location identifier when adding work location data block");

        error = page.addWorkLocationDataBlock("test", false, "Non-numeric ID", "CC", "Test Info", true);

        assertFalse(error.isEmpty(), "Expected error message for non-numeric work location identifier when adding work location data block");

        // remove an unexpected work location at the end if there is any
        if (page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true) > 0) {
            page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
        }
    }

    // Update Provider - Validate Update Work Location Name
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocationNameUpdate(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addWorkLocationDataBlock("12345", true, "Test Name", "CC", "Test Info", false);

        String error = page.updateWorkLocationDataBlock(false, generateAlphabetNumericString(256),
                "CC", "Test Info", EndReason.CHG, true);

        assertEquals(error, errorList.get("WLNameTooLong"),
                "Expected error message for work location name exceeding max length when updating work location data block");

        error = page.updateWorkLocationDataBlock(false, "", "CC", "Test Info", EndReason.CHG, true);

        assertEquals(error, errorList.get("WLNameMissing"),
                "Expected error message for missing work location name when updating work location data block");

        String name = generateAlphabetNumericString(255);
        page.updateWorkLocationDataBlock(false, name, "CC", "Test Info", EndReason.CHG, false);

        Map<String,String> wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);

        assertEquals(wlContent.get("Work Location Details-0-Name"), name,
                "Expected updated name to be reflected in work location data block after updating work location with valid name");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
    }

    // Update Provider - Validate Work Location Name
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocationName(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        String error = page.addWorkLocationDataBlock("12345", true,
                generateAlphabetNumericString(256), "CC", "Test Info", true);

        assertEquals(error, errorList.get("WLNameTooLong"),
                "Expected error message for work location name exceeding max length when adding work location data block");

        error = page.addWorkLocationDataBlock("12345", true, null,"CC",
                "Test Info", true);

        assertEquals(error, errorList.get("WLNameMissing"),
                "Expected error message for missing work location name when adding work location data block");

        page.addWorkLocationDataBlock("12345", true, generateAlphabetNumericString(255),
                "CC", "Test Info", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true), 1,
                "Expected 1 active work location data block after adding work location with valid name");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
    }

    // Update Provider - Validate Work Location Purpose Code
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocationPurposeCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.clickHeaderAddButton(ProviderSection.WORK_LOCATIONS);

        assertTrue(page.getMandatoryFields(ProviderSection.WORK_LOCATIONS).contains("Type"),
                "Expected 'Purpose' to be a mandatory field when adding a work location data block");

        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);

        String error = page.addWorkLocationDataBlock("12345", true, "Test Name", "Select One",
                "Test Info", true);

        assertEquals(error, errorList.get("errMsg5000Type"),
                "Expected error message for missing purpose code when adding work location data block");

        page.addWorkLocationDataBlock("12345", true, "Test Name", "CC",
                "Test Info", false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.WORK_LOCATIONS, true), 1,
                "Expected 1 active work location data block after adding work location with valid purpose code");

        page.ceaseDataBlock(ProviderSection.WORK_LOCATIONS, 0);
    }
}
