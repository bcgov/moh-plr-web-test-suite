package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper.*;
import static org.testng.Assert.*;
import static ca.bc.gov.health.qa.autotest.plr.data.UpdateProviderConstants.*;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
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
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.CanadianProvince;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.CommunicationPurpose;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ConditionType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ElectronicAddressPurpose;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ElectronicAddressType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.TelecommunicationPurpose;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.TelecommunicationType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.USState;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.By;
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

    private static final Map<ProviderType, MaintainRequestBuilder> defaultProviders = new LinkedHashMap<>();
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

    // Update Provider - Add Provider Relationships
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testAddProviderRelationships(ProviderType providerType)
    {
        final MaintainIndividualBuilder otherProvider = getOtherProvider(defaultProviders, providerType);
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
	public void testAddDisciplinaryAction(ProviderType providerType)
    {
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
	public void testValidateDisciplinaryAction(ProviderType providerType)
    {
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
	public void testValidateDisciplinaryActionDescriptionText(ProviderType providerType)
    {
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
	public void testGeneratingDefaultDisciplinaryActionID(ProviderType providerType)
    {
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
        final MaintainIndividualBuilder otherProvider = getOtherProvider(defaultProviders, providerType);
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
        final MaintainIndividualBuilder otherProvider = getOtherProvider(defaultProviders, providerType);
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
    public void testUpdateWorkLocations(ProviderType providerType)
    {
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
	public void testValidateProviderConditions(ProviderType providerType)
    {
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

    // Update Provider - Validate Provider Relationship Type Code
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateProviderRelationshipTypeCode(ProviderType providerType)
    {
        final MaintainIndividualBuilder otherProvider = getOtherProvider(defaultProviders, providerType);
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
    public void testValidateRelatedProviderID(ProviderType providerType)
    {
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
    public void testRelatedProviderIDAndRelationship(ProviderType providerType)
    {
        final MaintainIndividualBuilder otherProvider = getOtherProvider(defaultProviders, providerType);
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

    // Update Provider - Validate Update Work Location Default Flag
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocationDefaultFlagUpdate(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addWorkLocationDataBlock("12345", false, "Test Name", "CC", "Test Info", false);

        page.clickDataBlockUpdateButton(ProviderSection.WORK_LOCATIONS, 0);

        assertFalse(page.isCheckBoxChecked(ProviderSection.WORK_LOCATIONS, "defaultFlag"),
                "Expected default flag checkbox to be unchecked in work location data block when default flag is set to false");

        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);

        page.updateWorkLocationDataBlock(false, "Test Name Change", null, null, EndReason.CHG, false);

        final String details = "Work Location Details-0-";
        Map<String,String> wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);
        assertEquals(wlContent.get(details+"Default Flag"), "No",
                "Expected default flag to remain 'No' in work location data block after updating work location with default flag set to false");

        page.clickDataBlockUpdateButton(ProviderSection.WORK_LOCATIONS, 0);

        assertFalse(page.isCheckBoxChecked(ProviderSection.WORK_LOCATIONS, "defaultFlag"),
                "Expected default flag checkbox to remain unchecked in work location data block after updating work location without changing default flag");

        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);

        page.updateWorkLocationDataBlock(true, "Test Name Change", null, null, EndReason.CHG, false);

        wlContent = page.grabDataBlockContent(ProviderSection.WORK_LOCATIONS, 0);
        assertEquals(wlContent.get(details+"Default Flag"), "Yes",
                "Expected default flag to be 'Yes' in work location data block after updating work location with default flag set to true");

        page.clickDataBlockUpdateButton(ProviderSection.WORK_LOCATIONS, 0);

        assertTrue(page.isCheckBoxChecked(ProviderSection.WORK_LOCATIONS, "defaultFlag"),
                "Expected default flag checkbox to be checked in work location data block after updating work location with default flag set to true");

        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);
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

    // Update Provider - Validate Update Work Location ID
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateWorkLocationIDUpdate(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        page.addWorkLocationDataBlock(generateNumericString(15), true, "Test Name",
                "CC", "Test Info", false);

        WebElement dialog = page.clickDataBlockUpdateButton(ProviderSection.WORK_LOCATIONS, 0);
        assertTrue(dialog.findElements(By.cssSelector("input#maintainWorkLocationForm\\:wlChid")).isEmpty(),
                "Expected work location identifier field to be non-editable when updating work location data block");

        page.clickDialogCancelButton(ProviderSection.WORK_LOCATIONS);

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

    // Update Provider - Add block - Add Addresses
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testAddAddresses(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Add valid address block
        page.addAddressDataBlock(
                AddressType.M.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "123 Test Street",
                "Suite 100",
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data block");

        page.ceaseDataBlock(ProviderSection.ADDRESSES, 1);

        // Open dialog to add address block and cancel. Check address was not added.
        page.cancelAddAddressDataBlock(
                AddressType.M.getText(),
                TelecommunicationPurpose.HOME_CONTACT.getText(),
                "456 Cancel Ave",
                null,
                null,
                "Victoria",
                "BC - British Columbia",
                "CA - CANADA",
                "V8V 2B2",
                effective_date(),
                increment_year_for_effective_date());

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 1,
                "Expected no active address data blocks after cancelling add");
    }

    // Update Provider - Add block - Add Electronic Addresses
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testAddElectronicAddresses(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Add valid electronic address block
        page.addElectronicAddressDataBlock(
                ElectronicAddressType.EMAIL.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "test@example.com",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true), 1,
                "Expected 1 active electronic address data block");

        page.ceaseDataBlock(ProviderSection.ELECTRONIC_ADDRESSES, 0);

        // Open dialog to add electronic address block and cancel. Check electronic address was not added.
        page.cancelAddElectronicAddressDataBlock(
                ElectronicAddressType.HTTP.getText(),
                TelecommunicationPurpose.HOME_CONTACT.getText(),
                "https://www.example.com",
                effective_date(),
                increment_year_for_effective_date());

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true), 0,
                "Expected no active electronic address data blocks after cancelling add");
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
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Add address with address line 1 but without address line 2 and 3 - success
        String purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "123 Test Street",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding address with line 1 only");
        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);

        // Add address without address line 1 but with address line 2 - fail
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        String error = page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                null,
                "Suite 200",
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("missingAddressLine1"),
                "Expected error for missing address line 1");

        // Add address in line 1 that contains a space between # and # e.g., "456 789 Main St" - success
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "456 789 Main St",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding address with space in line 1");
        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);

        // Add address using address that includes the post office box and station information in line 1 address - success
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "PO Box 1234 Station Main",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding PO Box address");
        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);

        // Add address that includes street type abbreviated - success
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "789 Oak Ave",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding address with abbreviated street type");
        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);
    }

    // Update Provider - Add block - Validate Address Line One
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressLineOne(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Enter address with line 1 exceeding 101 characters - fail
        String purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        String error = page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                generateAlphabetNumericString(101),
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("addressLine1TooLong"),
                "Expected error for address line 1 exceeding 101 characters");

        // Enter address with line 1 blank - fail
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        error = page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                null,
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("missingAddressLine1"),
                "Expected error for blank address line 1");

        // Enter address with line 1 as "NO FIXED ADDRESS" - Success
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "NO FIXED ADDRESS",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding 'NO FIXED ADDRESS'");
        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);

        // Enter address with line 1 as "UNKNOWN" - Success
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "UNKNOWN",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding 'UNKNOWN'");
        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);

        // Enter address line 1 as "NA" - Success
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "NA",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding 'NA'");
        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);
    }

    // Update Provider - Add block - Validate Address Purpose Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressPurposeCode(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Verify purpose code is mandatory by checking mandatory fields in dialog
        page.clickHeaderAddButton(ProviderSection.ADDRESSES);

        assertTrue(page.getMandatoryFields(ProviderSection.ADDRESSES).contains("Purpose"),
                "Expected 'Purpose' to be a mandatory field when adding an address data block");

        // Verify purpose code dropdown has options available
        List<String> purposeOptions = page.getDropdownListOptions(ProviderSection.ADDRESSES, "addressPurpose");
        purposeOptions.remove("Select One");
        assertFalse(purposeOptions.isEmpty(),
                "Expected purpose dropdown to have available options");

        // Verify purpose options follow expected format (CODE - Description)
        for (String option : purposeOptions) {
            assertTrue(option.matches("^[A-Z]{2} - .+$"),
                    "Expected purpose option to follow format 'XX - Description', found: " + option);
        }

        page.clickDialogCancelButton(ProviderSection.ADDRESSES);

        // Try to add address without selecting a purpose code - should fail
        String error = page.addAddressDataBlock(
                AddressType.M.getText(),
                "Select One",
                "123 Test Street",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5000Purpose"),
                "Expected error message for missing purpose when adding address data block");

        // Add address with valid purpose code - should succeed
        String purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "456 Valid Street",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding address with valid purpose code");

        // Verify the purpose code is displayed correctly in the data block
        LinkedHashMap<String, String> content = page.grabDataBlockContent(ProviderSection.ADDRESSES, 1);
        String addressPurpose = content.get("Address Purpose");
        assertNotNull(addressPurpose,
                "Expected 'Address Purpose' key to be present in address data block");
        assertFalse(addressPurpose.isEmpty(),
                "Expected purpose code to be displayed in address data block");

        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);
    }

    // Update Provider - Add block - Validate Address Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressTypeCode(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Verify type code is mandatory by not setting a value
        String error = page.addAddressDataBlock(
                "Select One",
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "123 Test Street",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5000AddressType"),
                "Expected error message for missing address type when adding address data block");

        // Verify the code set available for Type code
        page.clickHeaderAddButton(ProviderSection.ADDRESSES);

        List<String> typeOptions = page.getDropdownListOptions(ProviderSection.ADDRESSES, "addressType");
        typeOptions.remove("Select One");
        List<String> expectedOptions = Arrays.stream(AddressType.values())
                .map(AddressType::getText).toList();
        assertTrue(typeOptions.containsAll(expectedOptions),
                "Expected address type dropdown options to contain all defined address types");

        page.clickDialogCancelButton(ProviderSection.ADDRESSES);

        // Add address with type code
        String purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "456 Valid Street",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding address with valid type code");

        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);
    }

    // Update Provider - Add block - Validate Address Uniqueness Rules
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressUniquenessRules(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Get current active address count - there should be at least 1 existing address
        int initialCount = page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true);
        assertTrue(initialCount >= 1,
                "Expected at least 1 active address data block initially");

        // Get the existing address type and purpose from first address to avoid starting with same combo
        LinkedHashMap<String, String> existingAddress = page.grabDataBlockContent(ProviderSection.ADDRESSES, 0);
        String existingType = existingAddress.get("Address Type");
        String existingPurpose = existingAddress.get("Address Purpose");

        // Extract codes from displayed values - handles multiple formats
        String existingTypeCode = extractCodeFromDisplayValue(existingType, true);
        String existingPurposeCode = extractCodeFromDisplayValue(existingPurpose, false);

        LOG.info("Existing address: Type={}, Purpose={}", existingTypeCode, existingPurposeCode);

        // Test uniqueness rules for each address type
        for (AddressType addressType : AddressType.values()) {
            LOG.info("Testing uniqueness for address type: {}", addressType.getCode());

            // Test each purpose code from CommunicationPurpose enum
            for (CommunicationPurpose purpose : CommunicationPurpose.values()) {
                String purposeCode = purpose.getCode();

                // Skip if this is the existing address combo - we'd be creating a duplicate immediately
                if (addressType.getCode().equals(existingTypeCode) &&
                        purposeCode.equals(existingPurposeCode)) {
                    LOG.info("Skipping existing combo: Type={}, Purpose={}", addressType.getCode(), purposeCode);
                    continue;
                }

                LOG.info("Testing uniqueness: Type={}, Purpose={}", addressType.getCode(), purposeCode);

                // First, add address with this type and purpose - should succeed
                page.addAddressDataBlock(
                        addressType.getText(),
                        purpose.getText(),
                        "123 Test Street",
                        null,
                        null,
                        "Vancouver",
                        "BC - British Columbia",
                        "CA - CANADA",
                        "V6B 1A1",
                        effective_date(),
                        increment_year_for_effective_date(),
                        false);

                int countAfterFirst = page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true);
                assertEquals(countAfterFirst, initialCount + 1,
                        "Expected " + (initialCount + 1) + " active address blocks after adding first address with Type=" +
                                addressType.getCode() + ", Purpose=" + purposeCode);

                // Now try to add another address with same type and purpose - should fail
                String error = page.addAddressDataBlock(
                        addressType.getText(),
                        purpose.getText(),
                        "456 Duplicate Street",
                        null,
                        null,
                        "Victoria",
                        "BC - British Columbia",
                        "CA - CANADA",
                        "V8V 2B2",
                        effective_date(),
                        increment_year_for_effective_date(),
                        true);

                // Verify uniqueness error - check for error code 2201 and address type/purpose in message
                assertNotNull(error,
                        "Expected error when adding duplicate address with Type=" + addressType.getCode() +
                                ", Purpose=" + purposeCode);
                assertTrue(error.contains("2201"),
                        "Expected error code 2201 for uniqueness violation, got: " + error);

                // Verify count didn't change after failed add
                int countAfterDuplicate = page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true);
                assertEquals(countAfterDuplicate, initialCount + 1,
                        "Address count should not increase after failed duplicate add");

                // Cease the address we just added to keep only the original
                page.ceaseLastDataBlock(ProviderSection.ADDRESSES);

                // Verify we're back to initial count
                int countAfterCease = page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true);
                assertEquals(countAfterCease, initialCount,
                        "Expected to return to initial count after ceasing test address");
            }
        }

        LOG.info("All address uniqueness rules verified successfully");
    }

    /**
     * Extracts the code from a displayed value. Handles multiple formats:
     * - "Physical location (P)" -> "P" (code in parentheses)
     * - "P - Physical location" -> "P" (code before dash)
     * - "Physical" -> "P" (matches enum description)
     * - "Business Contact (BC)" -> "BC"
     * - "BC - Business Contact" -> "BC"
     * - "Business" -> "BC" (partial match)
     *
     * @param value the displayed value to extract the code from
     * @param isAddressType true for address type, false for purpose
     * @return the extracted code, or null if not found
     */
    private String extractCodeFromDisplayValue(String value, boolean isAddressType) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // Try to extract from parentheses at the end: "Physical location (P)" -> "P"
        if (value.contains("(") && value.contains(")")) {
            int start = value.lastIndexOf('(') + 1;
            int end = value.lastIndexOf(')');
            if (start < end) {
                return value.substring(start, end);
            }
        }

        // Try to extract code before dash: "P - Physical location" -> "P"
        if (value.contains(" - ")) {
            String potentialCode = value.split(" - ")[0].trim();
            if (potentialCode.length() <= 3) { // Codes are usually 1-3 characters
                return potentialCode;
            }
        }

        // Try to match by description against enum values
        if (isAddressType) {
            for (AddressType at : AddressType.values()) {
                if (value.toLowerCase().contains(at.getDescription().toLowerCase()) ||
                    at.getDescription().toLowerCase().contains(value.toLowerCase())) {
                    return at.getCode();
                }
            }
        } else {
            for (CommunicationPurpose cp : CommunicationPurpose.values()) {
                String desc = cp.getDescription();
                if (value.toLowerCase().contains(desc.toLowerCase()) ||
                    desc.toLowerCase().contains(value.toLowerCase())) {
                    return cp.getCode();
                }
            }
        }

        return null;
    }

    // Update Provider - Add block - Validate City
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateCity(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Enter address with city blank - should fail
        String purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        String error = page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "123 Test Street",
                null,
                null,
                null, // blank city
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("missingCity"),
                "Expected error for missing city");

        // Enter address with city exceeding 60 characters - should fail (use raw city to skip autocomplete)
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        error = page.addAddressDataBlockRawCity(
                AddressType.M.getText(),
                purposeCode,
                "123 Test Street",
                null,
                null,
                generateAlphabetNumericString(61), // city > 60 chars
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("cityTooLong"),
                "Expected error for city exceeding 60 characters");

        // Enter address with city = 60 characters - should succeed (use raw city to skip autocomplete)
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlockRawCity(
                AddressType.M.getText(),
                purposeCode,
                "123 Test Street",
                null,
                null,
                generateAlphabetNumericString(60), // city = 60 chars
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding address with valid city length");

        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);
    }

    // Update Provider - Add block - Validate Communication Purpose Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateCommunicationPurposeTypeCode(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Add telecommunication without purpose code - should fail - Not Applies. Telecommunicaiton purpose is selected automatically and no default is on the list.
        /*String error = page.addTelecommunicationDataBlock(
                TelecommunicationType.PHONE.getText(),
                "Select One", // no purpose code
                "604",
                "5551234",
                null,
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5000Purpose"),
                "Expected error for missing purpose code when adding telecommunication");*/

        // Add electronic address without purpose code - should fail
        String error = page.addElectronicAddressDataBlock(
                ElectronicAddressType.EMAIL.getText(),
                "Select One", // no purpose code
                "test@example.com",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5000Purpose"),
                "Expected error for missing purpose code when adding electronic address");

        // Add address without purpose code - should fail
        error = page.addAddressDataBlock(
                AddressType.M.getText(),
                "Select One", // no purpose code
                "123 Test Street",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5000Purpose"),
                "Expected error for missing purpose code when adding address");
    }

    // Update Provider - Add block - Validate Electronic Address Txt
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateElectronicAddressTxt(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Add electronic address with more than 500 characters - failure
        String error = page.addElectronicAddressDataBlock(
                ElectronicAddressType.EMAIL.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                generateAlphabetNumericString(501) + "@test.com", // > 500 chars total
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5003ElectronicAddress"),
                "Expected error for electronic address exceeding 500 characters");

        // Add electronic address with field blank - failure
        error = page.addElectronicAddressDataBlock(
                ElectronicAddressType.EMAIL.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                null, // blank address
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5000EAddress"),
                "Expected error for missing electronic address");

        // Add electronic address with invalid email format - failure
        error = page.addElectronicAddressDataBlock(
                ElectronicAddressType.EMAIL.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "invalid-email-format", // no @ symbol
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsgprovider7013"),
                "Expected error for invalid email format");

        // Add electronic address with email with one or more periods before the @ - success
        page.addElectronicAddressDataBlock(
                ElectronicAddressType.EMAIL.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "test.user.name@example.com", // periods before @
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true), 1,
                "Expected 1 active electronic address data block after adding email with periods");

        page.ceaseDataBlock(ProviderSection.ELECTRONIC_ADDRESSES, 0);

        // Add electronic address using email with 500 characters - success
        String longEmail = generateAlphabetNumericString(490) + "@test.com"; // 500 chars total
        page.addElectronicAddressDataBlock(
                ElectronicAddressType.EMAIL.getText(),
                TelecommunicationPurpose.HOME_CONTACT.getText(),
                longEmail,
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true), 1,
                "Expected 1 active electronic address data block after adding 500 char email");

        page.ceaseDataBlock(ProviderSection.ELECTRONIC_ADDRESSES, 0);
    }

    // Update Provider - Add block - Validate Electronic Address Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateElectronicAddressTypeCode(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Add electronic address without type code - failure
        String error = page.addElectronicAddressDataBlock(
                "Select One", // no type
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "test@example.com",
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5000Type"),
                "Expected error for missing electronic address type");

        // Add electronic address with valid type code (EMAIL) - success
        page.addElectronicAddressDataBlock(
                ElectronicAddressType.EMAIL.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "test@example.com",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true), 1,
                "Expected 1 active electronic address data block after adding with EMAIL type");

        page.ceaseDataBlock(ProviderSection.ELECTRONIC_ADDRESSES, 0);

        // Add electronic address with valid type code (FTP) - success
        page.addElectronicAddressDataBlock(
                ElectronicAddressType.FTP.getText(),
                TelecommunicationPurpose.HOME_CONTACT.getText(),
                "ftp://example.com/files",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true), 1,
                "Expected 1 active electronic address data block after adding with FTP type");

        page.ceaseDataBlock(ProviderSection.ELECTRONIC_ADDRESSES, 0);

        // Add electronic address with valid type code (HTTP) - success
        page.addElectronicAddressDataBlock(
                ElectronicAddressType.HTTP.getText(),
                TelecommunicationPurpose.OTHER_CONTACT.getText(),
                "https://www.example.com",
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true), 1,
                "Expected 1 active electronic address data block after adding with HTTP type");

        page.ceaseDataBlock(ProviderSection.ELECTRONIC_ADDRESSES, 0);
    }

    // Update Provider - Add block - Validate Postal Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidatePostalCode(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Enter address with country as Canada - postal code with invalid format - failure
        String purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        String error = page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "123 Test Street",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "INVALID", // invalid format (not ANA NAN or ANANAN)
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg7009"),
                "Expected error for invalid Canadian postal code format");

        // Enter address with country as Canada - postal code exceeding 25 characters - failure
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        error = page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "123 Test Street",
                null,
                null,
                "Vancouver",
                "Sao Paulo",
                "BR - BRAZIL",
                generateAlphabetNumericString(26), // > 25 chars
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5003PostalCode"),
                "Expected error for postal code exceeding 25 characters (country not Canada)");

        // Enter address with country as Canada - with postal code as ANA NAN (valid format) - success
        purposeCode = page.getAvailablePurposeCodeForAddressType(AddressType.M.getCode());
        page.addAddressDataBlock(
                AddressType.M.getText(),
                purposeCode,
                "123 Test Street",
                null,
                null,
                "Vancouver",
                "BC - British Columbia",
                "CA - CANADA",
                "V6B 1A1", // valid Canadian postal code format
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ADDRESSES, true), 2,
                "Expected 2 active address data blocks after adding address with valid postal code");

        page.ceaseLastDataBlock(ProviderSection.ADDRESSES);
    }

    // Update Provider - Add block - Validate Telecommunication Number
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateTelecommunicationNumber(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Try to add telecommunication with extension > 15 characters - failure
        String error = page.addTelecommunicationDataBlock(
                TelecommunicationType.PHONE.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "604",
                "5551234",
                generateNumericString(16), // extension > 15 chars
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5003TelecomExtension"),
                "Expected error for extension exceeding 15 characters");

        // Try to add telecommunication with number > 30 characters - failure
        error = page.addTelecommunicationDataBlock(
                TelecommunicationType.PHONE.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "604",
                generateNumericString(31), // phone number > 30 chars
                null,
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5003TelecomPhoneNumber"),
                "Expected error for phone number exceeding 30 characters");

        // Try to add telecommunication with area code > 15 characters - failure
        error = page.addTelecommunicationDataBlock(
                TelecommunicationType.PHONE.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                generateNumericString(16), // area code > 15 chars
                "5551234",
                null,
                effective_date(),
                increment_year_for_effective_date(),
                true);

        assertEquals(error, errorList.get("errMsg5003TelecomAreaCode"),
                "Expected error for area code exceeding 15 characters");

        // Add telecom with area code = 15 characters, number = 30 characters and extension = 15 characters - success
        page.addTelecommunicationDataBlock(
                TelecommunicationType.PHONE.getText(),
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                generateNumericString(15), // area code = 15 chars
                generateNumericString(30), // phone number = 30 chars
                generateNumericString(15), // extension = 15 chars
                effective_date(),
                increment_year_for_effective_date(),
                false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.TELECOMMUNICATIONS, true), 1,
                "Expected 1 active telecommunication data block after adding with max valid lengths");

        page.ceaseDataBlock(ProviderSection.TELECOMMUNICATIONS, 0);
    }

    // Update Provider - Add block - Validate Telecom Uniqueness Rules
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateTelecomUniquenessRules(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Get current active telecom count - there should be at least 0 existing telecom blocks
        int initialCount = page.grabActiveDataBlockCount(ProviderSection.TELECOMMUNICATIONS, true);

        // If there is at least one telecom block, get its type and purpose to avoid immediate duplicate
        String existingTypeCode = null;
        String existingPurposeCode = null;
        if (initialCount > 0) {
            LinkedHashMap<String, String> existingTelecom = page.grabDataBlockContent(ProviderSection.TELECOMMUNICATIONS, 0);
            String existingType = existingTelecom.get("Telecommunication Type");
            String existingPurpose = existingTelecom.get("Telecommunication Purpose");
            existingTypeCode = extractCodeFromDisplayValue(existingType, true);
            existingPurposeCode = extractCodeFromDisplayValue(existingPurpose, false);
        }

        // Loop through all combinations of telecom type and purpose
        for (TelecommunicationType telecomType : TelecommunicationType.values()) {
            for (CommunicationPurpose purpose : CommunicationPurpose.values()) {
                String typeCode = telecomType.getText();
                String purposeText = purpose.getText();

                // Skip if this is the existing block combo
                if (existingTypeCode != null && existingPurposeCode != null &&
                    typeCode.contains(existingTypeCode) && purposeText.contains(existingPurposeCode)) {
                    continue;
                }

                // Add telecom block with this type and purpose - should succeed
                page.addTelecommunicationDataBlock(
                        typeCode,
                        purposeText,
                        "604",
                        "5551234",
                        null,
                        effective_date(),
                        increment_year_for_effective_date(),
                        false);

                int countAfterFirst = page.grabActiveDataBlockCount(ProviderSection.TELECOMMUNICATIONS, true);
                assertEquals(countAfterFirst, initialCount + 1,
                        "Expected " + (initialCount + 1) + " active telecom blocks after adding first with Type=" + typeCode + ", Purpose=" + purposeText);

                // Try to add another telecom block with same type and purpose - should fail
                String error = page.addTelecommunicationDataBlock(
                        typeCode,
                        purposeText,
                        "778",
                        "5555678",
                        null,
                        effective_date(),
                        increment_year_for_effective_date(),
                        true);

                // Verify uniqueness error - check for error code 2201 and type/purpose in message
                assertNotNull(error,
                        "Expected error when adding duplicate telecom with Type=" + typeCode + ", Purpose=" + purposeText);
                assertTrue(error.contains("2201"),
                        "Expected error code 2201 for uniqueness violation, got: " + error);

                // Verify count didn't change after failed add
                int countAfterDuplicate = page.grabActiveDataBlockCount(ProviderSection.TELECOMMUNICATIONS, true);
                assertEquals(countAfterDuplicate, initialCount + 1,
                        "Telecom count should not increase after failed duplicate add");

                // Cease the telecom block we just added to keep only the original
                page.ceaseLastDataBlock(ProviderSection.TELECOMMUNICATIONS);

                // Verify we're back to initial count
                int countAfterCease = page.grabActiveDataBlockCount(ProviderSection.TELECOMMUNICATIONS, true);
                assertEquals(countAfterCease, initialCount,
                        "Expected to return to initial count after ceasing test telecom block");
            }
        }
    }

    // Update Provider - Add block - Validate Telecommunication Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateTelecommunicationTypeCode(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Verify telecommunication type dropdown options
        page.clickHeaderAddButton(ProviderSection.TELECOMMUNICATIONS);

        waitSeconds(2);

        List<String> typeOptions = page.getDropdownListOptions(ProviderSection.TELECOMMUNICATIONS, "telecomType");
        typeOptions.remove("Select One");

        List<String> expectedOptions = Arrays.stream(TelecommunicationType.values())
                .map(TelecommunicationType::getText).toList();
        assertTrue(typeOptions.containsAll(expectedOptions),
                "Expected telecommunication type dropdown options to contain all defined types");
        page.clickDialogCancelButton(ProviderSection.TELECOMMUNICATIONS);

        // Try to add telecommunication without type - should fail
        String error = page.addTelecommunicationDataBlock(
                "Select One",
                TelecommunicationPurpose.BUSINESS_CONTACT.getText(),
                "604",
                "5551234",
                null,
                effective_date(),
                increment_year_for_effective_date(),
                true);
        assertEquals(error, errorList.get("errMsg5000Type"),
                "Expected error message for missing telecommunication type when adding telecommunication data block");
    }


    // Update Provider - Add block - Validate eAddress Uniqueness Rules
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateEAddressUniquenessRules(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        String identifier = getIdentifierFromBuilder(defaultProviders, providerType);
        UpdateProviderPage page = viewByIdentifierAsUpdateProvider(identifier, workflowManager_);

        // Cease all existing electronic address blocks to start fresh
        int blockCount = page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true);
        for (int i = blockCount - 1; i >= 0; i--) {
                page.ceaseDataBlock(ProviderSection.ELECTRONIC_ADDRESSES, i);
        }

        int initialCount = page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true);

        for (ElectronicAddressType eType : ElectronicAddressType.values()) {
                String typeText = eType.getText();
                //default value for email, change for other types
                String value = "test@example.com";
                if (eType == ElectronicAddressType.FTP) value = "ftp://example.com";
                if (eType == ElectronicAddressType.HTTP) value = "https://example.com";

                for (ElectronicAddressPurpose purpose : ElectronicAddressPurpose.values()) {
                        String purposeText = purpose.getText();

                        // 1. Add eType with this purpose (should succeed)
                        page.addElectronicAddressDataBlock(
                                typeText,
                                purposeText,
                                value,
                                effective_date(),
                                increment_year_for_effective_date(),
                                false);

                        int countAfterFirst = page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true);
                        assertEquals(countAfterFirst, initialCount + 1,
                                "Expected " + (initialCount + 1) + " active electronic address blocks after adding " + typeText + " with Purpose=" + purposeText);
                        // 2. Try to add duplicate eType (should fail)
                        String error = page.addElectronicAddressDataBlock(
                                typeText,
                                purposeText,
                                "duplicate@example.com",
                                effective_date(),
                                increment_year_for_effective_date(),
                                true);
                        assertNotNull(error,
                                "Expected error when adding duplicate " + typeText + " with Purpose=" + purposeText);
                        assertTrue(error.contains("2201"),
                                "Expected error code 2201 for uniqueness violation, got: " + error);

                        // Cease eType and repeat for next purpose
                        page.ceaseLastDataBlock(ProviderSection.ELECTRONIC_ADDRESSES);

                        int countAfterCease = page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true);
                        assertEquals(countAfterCease, initialCount,
                                "Expected to return to initial count after ceasing " + typeText + " for Purpose=" + purposeText);
                }
        }

   }

    /**
     * Tries to wait some number of seconds. Will fail the test used in if interrupted.
	 * TODO this should be used as little as possible in favour of selenium implicit waits.
     *
     * @param second the number of seconds to wait.
     */
        public void waitSeconds(int second) {
		try {
			Thread.sleep(1000L * second);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}
}
