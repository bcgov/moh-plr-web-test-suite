package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.ClinicOwnerBusinessType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.ClinicServices;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.ClinicType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.HdsSubType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateOrganizationPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.OrganizationProperties;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class UpdateOrganizationPropertiesTests implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private FHIRController fhirController;
    private OrganizationDataGenerator organizationDataGenerator;
	private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;
    private static MaintainOrgBuilder defaultOrg = null;
    private static UpdateOrganizationPage defaultOrgPage = null;


    private final String errorInvFormatClinicHours;
    private final String errorDuplicateClinicHours;
    private final String errorDuplicateClinicOwnershipType;
	private final String errorDuplicateClinicServiceDeliveryType;
	private final String errorDuplicateClinicType;
	private final String errorDuplicatePciFlag;
	private final String errorDuplicateClinicOwner;
	private final String errorDuplicateClinicLegalName;
	private final String errorInvCharsPayeeNumber;
	private final String errorDuplicatePayeeNumber;
	private final String errorDuplicateHdsSubType;
    
    private UpdateOrganizationPropertiesTests() {
		
		try
        {
            organizationDataGenerator = OrganizationDataGenerator.getInstance();
            
            errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
            
            // Initialize error messages from JSON file
            errorInvFormatClinicHours = errorList.getString("errorInvFormatClinicHours");
            errorDuplicateClinicHours = errorList.getString("errorDuplicateClinicHours");
            errorDuplicateClinicOwnershipType = errorList.getString("errorDuplicateClinicOwnershipType");
            errorDuplicateClinicServiceDeliveryType = errorList.getString("errorDuplicateClinicServiceDeliveryType");
            errorDuplicateClinicType = errorList.getString("errorDuplicateClinicType");
            errorDuplicatePciFlag = errorList.getString("errorDuplicatePciFlag");
            errorDuplicateClinicOwner = errorList.getString("errorDuplicateClinicOwner");
            errorDuplicateClinicLegalName = errorList.getString("errorDuplicateClinicLegalName");
            errorInvCharsPayeeNumber = errorList.getString("errorInvCharsPayeeNumber");
            errorDuplicatePayeeNumber = errorList.getString("errorDuplicatePayeeNumber");
            errorDuplicateHdsSubType = errorList.getString("errorDuplicateHdsSubType");
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read JSON data (%s).", errorPath);
            throw new IllegalStateException(msg, e);
        }
	}

	@BeforeTest
	public void beforeTest() {
		// Step 1 - Create the default HDS org once for all tests
		fhirController = new FHIRController(UserType.ADMIN);
		defaultOrg = fhirController.createOrganization(new OrganizationMaintainConfig(OrgRoleType.HDS));
		fhirController.close();
		LOG.info("Created default HDS organization with IPC: {}", defaultOrg.getIdentifier(IdentifierType.IPC));
	}

	@AfterClass
	public void teardown() {
		workflowManager_.logoutAllAndClose();
		LOG.info("Done.");
	}

	@BeforeMethod
	public void before(Object[] parameters) {
		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
		if (!workflow.isLoggedIn()) {
			workflow.login().openPlr();
		}
	}

	// 001. Add property Clinic Hours of Operation
	@Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
	public void testAddClinicHoursOfOperation() {
		// Navigate to the default org
		defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);
		
        //Step 3 - Make a Search by Identifier, make sure Clinic Hours of Operation is displayed correctly in View screen
		Map<String, String> result = createOrganizationProperty(OrganizationProperties.CLINIC_HOURS_OF_OPERATION);
		String errorMsg = result.get("error");
		
		assertTrue(errorMsg.isEmpty(), 
			"Failed to add Clinic Hours of Operation: " + errorMsg);

        //Step 4 - Update Clinic Hours of Operation property to a valid value
		String updatedHours = organizationDataGenerator.generateClinicHourEntry();
		errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
			OrganizationProperties.CLINIC_HOURS_OF_OPERATION, 
			updatedHours, 
            EndReason.CHG, 
			0, 
            false);
		
		assertTrue(errorMsg.isEmpty(), 
			"Failed to update Clinic Hours of Operation: " + errorMsg);

        //Step 7 - Try to update Hours of operation block with invalid day format "Monday" instead of "Mon"
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
			OrganizationProperties.CLINIC_HOURS_OF_OPERATION, 
			"Monday 09:00-17:00", 
            EndReason.CHG, 
			0, 
            true);
		
		assertEquals(errorMsg, errorInvFormatClinicHours,
			"Error should be displayed when updating Clinic Hours with in valid format");

        //Step 8 - Try to create a duplicate clinic hours of operation block
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_HOURS_OF_OPERATION,
            updatedHours);

        assertEquals(errorMsg, errorDuplicateClinicHours,
            "Error should be displayed when adding duplicate Clinic Hours of Operation block");

        //Step 5 - Cease Clinic Hours of Operation property
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_HOURS_OF_OPERATION,
            updatedHours,
            EndReason.CEASE,
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to cease Clinic Hours of Operation: " + errorMsg);

        //Step 6 - Try adding an Hours of operation block with invalid characters at start and end of days of week
        String invalidClinicHours = "*/" + organizationDataGenerator.generateClinicHourEntry() + "*/";
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_HOURS_OF_OPERATION,
            invalidClinicHours);

        assertEquals(errorMsg, errorInvFormatClinicHours,
            "Error should be displayed when adding Clinic Hours with invalid format");
	}

    // 002. Add property Clinic Ownership Type
	@Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
	public void testAddClinicOwnershipType() {
		// Navigate to the default org
		defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);
		
        //Step1-3 Create Org and afterwards add Clinic Ownership Type property, then cease block and repeat adding property with all different valid values
        for (ClinicOwnerBusinessType type : ClinicOwnerBusinessType.values()) {
            // Create the property block
            String errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
                OrganizationProperties.CLINIC_OWNER_BUSINESS_TYPE,
                type.getText());
            
            assertTrue(errorMsg.isEmpty(), 
                "Failed to add Clinic Owner Business Type '" + type.getText() + "': " + errorMsg);
            
            // Cease the property block
            errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                OrganizationProperties.CLINIC_OWNER_BUSINESS_TYPE,
                type.getText(),
                EndReason.CEASE,
                0, 
                false);

            assertTrue(errorMsg.isEmpty(), 
                "Failed to cease Clinic Owner Business Type: " + errorMsg);
        }

	}

    //003. Validate Clinic Ownership Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicOwnershipType() {
		// Navigate to the default org
		defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);
		
        //Step 1 and 2 - create org and add Clinic Ownership Type.
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.CLINIC_OWNER_BUSINESS_TYPE);
        String validOwnershipType = result.get("value");
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add Clinic Owner Business Type: " + errorMsg);

        //Step 3 - Update value with CHG
        //get a different valid ownership type
        String updatedType = differentOwnershipType(validOwnershipType);

        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_BUSINESS_TYPE, 
            updatedType, 
            EndReason.CHG, 
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to CHG Clinic Owner Business Type: " + errorMsg);
        
        //Step 4 - Update value with CORR
        updatedType = differentOwnershipType(updatedType);

        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_BUSINESS_TYPE, 
            updatedType, 
            EndReason.CORR, 
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to CORR Clinic Owner Business Type: " + errorMsg);

        //Step 9 - Try to add a second clinic ownership type block when one is already active
        updatedType = differentOwnershipType(updatedType);

        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_BUSINESS_TYPE,
            updatedType);

        assertEquals(errorMsg, errorDuplicateClinicOwnershipType, 
            "Error should be displayed when adding duplicate Clinic Owner Business Type block");

        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_BUSINESS_TYPE, 
            updatedType, 
            EndReason.CEASE, 
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to CEASE Clinic Owner Business Type: " + errorMsg);

        //Step 6-8 N/A
    }

    //004. Add property Clinic Service Delivery Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddClinicServiceDeliveryType() {
		// Navigate to the default org
		defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);
		
        //Step 1 - 3 - create org and add Clinic Service Delivery Type. Cease and add block with all different valid values

        for (ClinicServices type : ClinicServices.values()) {
            // Create the property block
            String errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
                OrganizationProperties.CLINIC_SERVICE_DELIVERY_TYPE,
                type.getText());
            
            assertTrue(errorMsg.isEmpty(), 
                "Failed to add Clinic Service Delivery Type '" + type.getText() + "': " + errorMsg);
            
            // Cease the property block
            errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                OrganizationProperties.CLINIC_SERVICE_DELIVERY_TYPE,
                type.getText(),
                EndReason.CEASE,
                0, 
                false);

            assertTrue(errorMsg.isEmpty(), 
                "Failed to cease Clinic Service Delivery Type: " + errorMsg);
        }
        
        //Step 4 - Present in testValidateClinicServiceDeliveryType
    }

    //005. Validate Clinic Service Delivery Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicServiceDeliveryType() {
		// Navigate to the default org
		defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);
		
        //Step 1 and 2 -Create org through FHIR with Clinic Service Delivery Type.
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.CLINIC_SERVICE_DELIVERY_TYPE);
        String validServiceDeliveryType = result.get("value");
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add Clinic Service Delivery Type: " + errorMsg);
            
        //Step 3 - Update value with CHG
        String updatedType = differentClinicServiceDeliveryType(validServiceDeliveryType);

        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                OrganizationProperties.CLINIC_SERVICE_DELIVERY_TYPE,
                updatedType,
                EndReason.CHG,
                0, 
                false);

        assertTrue(errorMsg.isEmpty(), 
            "Failed to update Clinic Service Delivery Type with CHG: " + errorMsg);

        //Step 4 - Update value with CORR
        updatedType = differentClinicServiceDeliveryType(updatedType);

        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                OrganizationProperties.CLINIC_SERVICE_DELIVERY_TYPE,
                updatedType,
                EndReason.CORR,
                0, 
                false);

        assertTrue(errorMsg.isEmpty(), 
            "Failed to update Clinic Service Delivery Type with CORR: " + errorMsg);

        //Step 8 - Try to add a second duplicate block when one is already active
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_SERVICE_DELIVERY_TYPE,
            updatedType);

        assertEquals(errorMsg, errorDuplicateClinicServiceDeliveryType,
            "Error should be displayed when adding duplicate Clinic Service Delivery Type block");
        
        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                OrganizationProperties.CLINIC_SERVICE_DELIVERY_TYPE,
                updatedType,
                EndReason.CEASE,
                0, 
                false);

        assertTrue(errorMsg.isEmpty(), 
            "Failed to update Clinic Service Delivery Type with CEASE: " + errorMsg);

        //Step 6-7 N/A
 

    }

    //006. Add property Clinic Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddClinicType() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);


        for (ClinicType type : ClinicType.values()) {
            // Create the property block
            String errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
                OrganizationProperties.CLINIC_TYPE,
                type.getText());
            
            assertTrue(errorMsg.isEmpty(), 
                "Failed to add Clinic Type '" + type.getText() + "': " + errorMsg);
            
            // Cease the property block
            errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                OrganizationProperties.CLINIC_TYPE,
                type.getText(),
                EndReason.CEASE,
                0, 
                false);

            assertTrue(errorMsg.isEmpty(), 
                "Failed to cease Clinic Type: " + errorMsg);
        }

        //Step 4 - Present in testValidateClinicType
    }

    //007. Validate Clinic Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicType() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - Create org and add Clinic Type
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.CLINIC_TYPE);
        String validClinicType = result.get("value");
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add Clinic Type: " + errorMsg);

        //Step 3 - Update value with CHG
        String updatedType = differentClinicType(validClinicType);

        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_TYPE, 
            updatedType, 
            EndReason.CHG, 
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Clinic Type with CHG: " + errorMsg);

        //Step 4 - Update value with CORR
        updatedType = differentClinicType(updatedType);

        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_TYPE, 
            updatedType, 
            EndReason.CORR, 
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Clinic Type with CORR: " + errorMsg);

        //Step 8 - Try to add a second duplicate block when one is already active
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_TYPE,
            updatedType);

        assertEquals(errorMsg, errorDuplicateClinicType,
            "Error should be displayed when adding duplicate Clinic Type block");

        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_TYPE, 
            updatedType, 
            EndReason.CEASE, 
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to cease Clinic Type: " + errorMsg);

        //Step 6-7 N/A
    }

    //008. Add property PCI Flag
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddPciFlag() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - create org and add PCI Flag as true
        String errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.PCI_FLAG,
            "true");
        
        assertTrue(errorMsg.isEmpty(), 
            "Failed to add PCI Flag as true: " + errorMsg);

        //Step 3 - Update PCI Flag to false
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PCI_FLAG,
            "false",
            EndReason.CHG,
            0,
            false);
        
        assertTrue(errorMsg.isEmpty(), 
            "Failed to update PCI Flag to false: " + errorMsg);

        //Step 4 - try to add a second PCI Flag block when one is already active
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.PCI_FLAG,
            "true");
        
        assertEquals(errorMsg, errorDuplicatePciFlag,
            "Error should be displayed when adding duplicate PCI Flag block");

        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PCI_FLAG,
            "false",
            EndReason.CEASE,
            0,
            false);
        
        assertTrue(errorMsg.isEmpty(), 
            "Failed to cease PCI Flag: " + errorMsg);
    }

    //009. Validate PCI Flag
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidatePciFlag() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - Create org and add PCI Flag as true
        String errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.PCI_FLAG,
            "true");
        
        assertTrue(errorMsg.isEmpty(), 
            "Failed to add PCI Flag as true: " + errorMsg);

        //Step 3 - Update PCI Flag to false with CHG
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PCI_FLAG,
            "false",
            EndReason.CHG,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update PCI Flag to false with CHG: " + errorMsg);

        //Step 4 - Update PCI Flag to true with CORR
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PCI_FLAG,
            "true",
            EndReason.CORR,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update PCI Flag to true with CORR: " + errorMsg);

        //Step 8 - Try to add a second duplicate block when one is already active
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.PCI_FLAG,
            "false");

        assertEquals(errorMsg, errorDuplicatePciFlag,
            "Error should be displayed when adding duplicate PCI Flag block");

        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PCI_FLAG,
            "true",
            EndReason.CEASE,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to cease PCI Flag: " + errorMsg);
    }

    //010. Add property Clinic Owner
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddClinicOwner() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - create org and add Clinic Owner
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.CLINIC_OWNER_NAMES);
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add Clinic Owner: " + errorMsg);

        //Step 3 - Update Clinic Owner to another valid value end reason CHG
        String updatedOwner = organizationDataGenerator.generateClinicOwnerName();
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_NAMES,
            updatedOwner,
            EndReason.CHG,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Clinic Owner with CHG: " + errorMsg);

        //Step 4 - Add Clinic Owner with another valid value. Successfully added new block.
        String secondOwner = organizationDataGenerator.generateClinicOwnerName();
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_NAMES,
            secondOwner);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add second Clinic Owner block: " + errorMsg);

        //Step 5 - For cleanup cease values
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_NAMES,
            updatedOwner,
            EndReason.CEASE,
            1,
            false);

        assertTrue(errorMsg.isEmpty(),
            "Failed to cease second Clinic Owner block: " + errorMsg);

        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_NAMES,
            secondOwner,
            EndReason.CEASE,
            0,
            false);

        assertTrue(errorMsg.isEmpty(),
            "Failed to cease first Clinic Owner block: " + errorMsg);

    }

    //011. Validate Clinic Owner
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicOwner() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - Create org and add Clinic Owner
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.CLINIC_OWNER_NAMES);
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add Clinic Owner: " + errorMsg);

        //Step 3 - Update Clinic Owner to another valid value with CHG
        String updatedOwner = organizationDataGenerator.generateClinicOwnerName();
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_NAMES,
            updatedOwner,
            EndReason.CHG,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Clinic Owner with CHG: " + errorMsg);

        //Step 4 - Update Clinic Owner to another valid value with CORR
        updatedOwner = organizationDataGenerator.generateClinicOwnerName();
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_NAMES,
            updatedOwner,
            EndReason.CORR,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Clinic Owner with CORR: " + errorMsg);

        //Step 7 - NA

        //Step 8 - Try to add a second duplicate block
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_NAMES,
            updatedOwner);

        assertEquals(errorMsg, errorDuplicateClinicOwner,
            "Error should be displayed when adding duplicate Clinic Owner block");

        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_OWNER_NAMES,
            updatedOwner,
            EndReason.CEASE,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to cease Clinic Owner: " + errorMsg);
    }

    //012. Add property Clinic Legal name
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddClinicLegalName() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - create org and add Clinic Legal name
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME);
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add Clinic Legal Business Name: " + errorMsg);

        //Step 3 - Update Clinic Legal name to another valid value end reason CHG
        String updatedLegalName = organizationDataGenerator.generateClinicLegalBusinessName();
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME,
            updatedLegalName,
            EndReason.CHG,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Clinic Legal Business Name with CHG: " + errorMsg);

        //Step 4 - Try to add a second Clinic Legal name block when one is already active
        String secondLegalName = organizationDataGenerator.generateClinicLegalBusinessName();
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME,
            secondLegalName);

        assertEquals(errorMsg, errorDuplicateClinicLegalName,
            "Error should be displayed when adding duplicate Clinic Legal Business Name block");

        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME,
            updatedLegalName,
            EndReason.CEASE,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to cease Clinic Legal Business Name: " + errorMsg);
    }

    //013. Validate Clinic Legal name
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicLegalName() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - Create org and add Clinic Legal name
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME);
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add Clinic Legal Business Name: " + errorMsg);

        //Step 3 - Update Clinic Legal name to another valid value with CHG
        String updatedLegalName = organizationDataGenerator.generateClinicLegalBusinessName();
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME,
            updatedLegalName,
            EndReason.CHG,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Clinic Legal Business Name with CHG: " + errorMsg);

        //Step 4 - Update Clinic Legal name to another valid value with CORR
        updatedLegalName = organizationDataGenerator.generateClinicLegalBusinessName();
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME,
            updatedLegalName,
            EndReason.CORR,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Clinic Legal Business Name with CORR: " + errorMsg);

        //Step 7 - NA
        //Step 8 - Try to add a second duplicate block
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME,
            updatedLegalName);

        assertEquals(errorMsg, errorDuplicateClinicLegalName,
            "Error should be displayed when adding duplicate Clinic Legal Business Name block");

        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME,
            updatedLegalName,
            EndReason.CEASE,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to cease Clinic Legal Business Name: " + errorMsg);
    }

    //014. Add property Payee Number
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddPayeeNumber() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - create org and add Payee Number
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.PAYEE_NUMBER);
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add Payee Number: " + errorMsg);

        //Step 3 - Update Payee Number to another valid value end reason CHG
        String updatedPayeeNumber = organizationDataGenerator.generatePayeeNumber();
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PAYEE_NUMBER,
            updatedPayeeNumber,
            EndReason.CHG,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Payee Number with CHG: " + errorMsg);

        //Step 4 - For cleanup cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PAYEE_NUMBER,
            updatedPayeeNumber,
            EndReason.CEASE,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to cease Payee Number: " + errorMsg);
    }

    //015. Validate Payee Number
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidatePayeeNumber() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - Create org and add Payee Number
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.PAYEE_NUMBER);
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add Payee Number: " + errorMsg);

        //Step 3 - Update Payee Number to another valid value with CHG
        String updatedPayeeNumber = organizationDataGenerator.generatePayeeNumber();
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PAYEE_NUMBER,
            updatedPayeeNumber,
            EndReason.CHG,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Payee Number with CHG: " + errorMsg);

        //Step 4 - Update Payee Number to another valid value with CORR
        updatedPayeeNumber = organizationDataGenerator.generatePayeeNumber();
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PAYEE_NUMBER,
            updatedPayeeNumber,
            EndReason.CORR,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update Payee Number with CORR: " + errorMsg);

        //Step 6 - Try to update block with invalid characters
        String invalidPayeeNumber = "ABC@#${%";
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PAYEE_NUMBER,
            invalidPayeeNumber,
            EndReason.CHG,
            0,
            true);

        assertEquals(errorMsg, errorInvCharsPayeeNumber,
            "Error should be displayed when updating Payee Number with invalid characters");

        //Step 7 - NA

        //Step 8 - Try to add a second duplicate block
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.PAYEE_NUMBER,
            updatedPayeeNumber);

        assertEquals(errorMsg, errorDuplicatePayeeNumber,
            "Error should be displayed when adding duplicate Payee Number block");

        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.PAYEE_NUMBER,
            updatedPayeeNumber,
            EndReason.CEASE,
            0,
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to cease Payee Number: " + errorMsg);
    }

    //016. Add property HDS Sub Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddHdsSubType() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - create org and add HDS Sub Type

        //Step 3 - Cease and add block with all other valid values

        for (HdsSubType type : HdsSubType.values()) {
            // Create the property block
            String errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
                OrganizationProperties.HDS_SUB_TYPE,
                type.getText());
            
            assertTrue(errorMsg.isEmpty(), 
                "Failed to add HDS Sub Type '" + type.getText() + "': " + errorMsg);
            
            // Cease the property block
            errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                OrganizationProperties.HDS_SUB_TYPE,
                type.getText(),
                EndReason.CEASE,
                0, 
                false);

            assertTrue(errorMsg.isEmpty(), 
                "Failed to cease HDS Sub Type: " + errorMsg);
        }

        //Step 4 - Present in testValidateHdsSubType
    }

    //017. Validate HDS Sub Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateHdsSubType() {
        // Navigate to the default org
        defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);

        //Step 1 and 2 - Create org and add HDS Sub Type
        Map<String, String> result = createOrganizationProperty(OrganizationProperties.HDS_SUB_TYPE);
        String validHdsSubType = result.get("value");
		String errorMsg = result.get("error");

        assertTrue(errorMsg.isEmpty(), 
			"Failed to add HDS Sub Type: " + errorMsg);

        //Step 3 - Update value with CHG
        String updatedType = differentHdsSubType(validHdsSubType);

        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.HDS_SUB_TYPE, 
            updatedType, 
            EndReason.CHG, 
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update HDS Sub Type with CHG: " + errorMsg);

        //Step 4 - Update value with CORR
        updatedType = differentHdsSubType(updatedType);

        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.HDS_SUB_TYPE, 
            updatedType, 
            EndReason.CORR, 
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to update HDS Sub Type with CORR: " + errorMsg);

        //Step 8 - Try to add a second duplicate block when one is already active
        String differentType = differentHdsSubType(updatedType);
        errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
            OrganizationProperties.HDS_SUB_TYPE,
            differentType);

        assertEquals(errorMsg, errorDuplicateHdsSubType,
            "Error should be displayed when adding duplicate HDS Sub Type block");

        //Step 5 - Cease value
        errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
            OrganizationProperties.HDS_SUB_TYPE, 
            updatedType, 
            EndReason.CEASE, 
            0, 
            false);

        assertTrue(errorMsg.isEmpty(), 
			"Failed to cease HDS Sub Type: " + errorMsg);

        //Step 6-7 N/A
    }

    //018. Update Org Property
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testUpdateOrgProperty() {
		// Navigate to the default org
		defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);
		
        //Step 1 - Create org through FHIR (already done in beforeTest)

        //Step 5 - Iterate through all organization properties (Step 2 - 4 for each)
        for (OrganizationProperties property : OrganizationProperties.values()) {
            LOG.info("Testing update for property: {}", property.getDisplayName());
            
            //Step 2 - Add property
            Map<String, String> result = createOrganizationProperty(property);
            String initialValue = result.get("value");
            String errorMsg = result.get("error");
            
            assertTrue(errorMsg.isEmpty(), 
                "Failed to add " + property.getDisplayName() + ": " + errorMsg);
            
            //Step 3 - Update the property to a different value
            String updatedValue = getDifferentPropertyValue(property, initialValue);
            errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                property,
                updatedValue,
                EndReason.CHG,
                0,
                false);
            
            assertTrue(errorMsg.isEmpty(), 
                "Failed to update " + property.getDisplayName() + " with CHG: " + errorMsg);
            
            //Step 4 - Logout, close browser, open new browser and login again, then update the property
            workflowManager_.logoutAndClose(UserType.ADMIN);
            
            // Wait for Chrome to fully terminate before starting new session
            shortUiPause();
            
            // Open new browser and re-login
            TestHelper.logIn(workflowManager_, UserType.ADMIN);
            
            // Navigate back to the default org
            defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);
            
            // Update the property again with another different value
            String secondUpdatedValue = getDifferentPropertyValue(property, updatedValue);
            errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                property,
                secondUpdatedValue,
                EndReason.CHG,
                0,
                false);
            
            assertTrue(errorMsg.isEmpty(), 
                "Failed to update " + property.getDisplayName() + " after re-login: " + errorMsg);

            //Finally, cease the property for cleanup
            errorMsg = defaultOrgPage.updateOrganizationPropertyDataBlock(
                property,
                secondUpdatedValue,
                EndReason.CEASE,
                0,
                false);
            
            assertTrue(errorMsg.isEmpty(), 
                "Failed to cease " + property.getDisplayName() + ": " + errorMsg);
            
            LOG.info("Successfully tested update for property: {}", property.getDisplayName());
        }
    }

	/**
	 * Generic method to create an organization property based on the property type.
	 * Follows the flow pattern established by the clinic hours of operation test.
	 * Uses OrganizationDataGenerator methods to generate appropriate test data.
	 * 
	 * @param propertyType the type of organization property to create
	 * @return Map with keys "value" (generated property value) and "error" (error message, empty if successful)
	 */
	private Map<String, String> createOrganizationProperty(OrganizationProperties propertyType) {
		String generatedValue = "";
		String errorMsg = "";
		
		switch (propertyType) {
			case CLINIC_HOURS_OF_OPERATION:
				generatedValue = organizationDataGenerator.generateClinicHourEntry();
				errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
					OrganizationProperties.CLINIC_HOURS_OF_OPERATION, 
					generatedValue);
				break;
				
			case CLINIC_OWNER_BUSINESS_TYPE:
				generatedValue = organizationDataGenerator.randomClinicOwnerBusinessType().getText();
				errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
					OrganizationProperties.CLINIC_OWNER_BUSINESS_TYPE, 
					generatedValue);
				break;
				
			case CLINIC_SERVICE_DELIVERY_TYPE:
				generatedValue = organizationDataGenerator.randomClinicServices().getText();
				errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
					OrganizationProperties.CLINIC_SERVICE_DELIVERY_TYPE, 
					generatedValue);
				break;
				
			case CLINIC_TYPE:
				generatedValue = organizationDataGenerator.randomClinicType().getText();
				errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
					OrganizationProperties.CLINIC_TYPE, 
					generatedValue);
				break;
				
			case PCI_FLAG:
				boolean pciValue = organizationDataGenerator.generatePciFlag();
				generatedValue = String.valueOf(pciValue);
				errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
					OrganizationProperties.PCI_FLAG, 
					generatedValue);
				break;
				
			case CLINIC_OWNER_NAMES:
				generatedValue = organizationDataGenerator.generateClinicOwnerName();
				errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
					OrganizationProperties.CLINIC_OWNER_NAMES, 
					generatedValue);
				break;
				
			case CLINIC_LEGAL_BUSINESS_NAME:
				generatedValue = organizationDataGenerator.generateClinicLegalBusinessName();
				errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
					OrganizationProperties.CLINIC_LEGAL_BUSINESS_NAME, 
					generatedValue);
				break;
				
			case PAYEE_NUMBER:
				generatedValue = organizationDataGenerator.generatePayeeNumber();
				errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
					OrganizationProperties.PAYEE_NUMBER, 
					generatedValue);
				break;
				
			case ADDRESS_UNIT:
				generatedValue = organizationDataGenerator.generateAddressUnit();
				errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
					OrganizationProperties.ADDRESS_UNIT, 
					generatedValue);
				break;
				
			case HDS_SUB_TYPE:
			generatedValue = organizationDataGenerator.randomHdsSubType().getText();
			errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
				OrganizationProperties.HDS_SUB_TYPE, 
				generatedValue);
			break;
			default:
				errorMsg = "Unknown property type: " + propertyType;
				LOG.error(errorMsg);
				break;
		}
		
		if (errorMsg.isEmpty()) {
			LOG.info("Successfully created {} property with value: {}", propertyType.getDisplayName(), generatedValue);
		} else {
			LOG.warn("Failed to create {} property. Error: {}", propertyType.getDisplayName(), errorMsg);
		}
		
		return Map.of("value", generatedValue, "error", errorMsg);
	}

    /* Returns a different random valid ownership type to the one sent as parameter
     */
    private String differentOwnershipType(String validOwnershipType) {

        String updatedType;
        do {
            updatedType = organizationDataGenerator.randomClinicOwnerBusinessType().getText();

        } while (updatedType.equals(validOwnershipType));

        return updatedType;
    }

    /* Returns a different random valid clinic service delivery type to the one sent as parameter
     */
    private String differentClinicServiceDeliveryType(String validServiceDeliveryType) {

        String updatedType;
        do {
            updatedType = organizationDataGenerator.randomClinicServices().getText();
        } while (updatedType.equals(validServiceDeliveryType));

        return updatedType;
    }

    /* Returns a different random valid clinic type to the one sent as parameter
     */
    private String differentClinicType(String validClinicType) {

        String updatedType;
        do {
            updatedType = organizationDataGenerator.randomClinicType().getText();
        } while (updatedType.equals(validClinicType));

        return updatedType;
    }

    /* Returns a different random valid HDS sub type to the one sent as parameter
     */
    private String differentHdsSubType(String validHdsSubType) {

        String updatedType;
        do {
            updatedType = organizationDataGenerator.randomHdsSubType().getText();
        } while (updatedType.equals(validHdsSubType));

        return updatedType;
    }

    /**
     * Generic method to get a different property value based on the property type.
     * Leverages the existing "different" helper methods for enum-based properties.
     * 
     * @param propertyType the type of organization property
     * @param currentValue the current value that should be different from the returned value
     * @return a different valid value for the property type
     */
    private String getDifferentPropertyValue(OrganizationProperties propertyType, String currentValue) {
        switch (propertyType) {
            case CLINIC_OWNER_BUSINESS_TYPE:
                return differentOwnershipType(currentValue);
                
            case CLINIC_SERVICE_DELIVERY_TYPE:
                return differentClinicServiceDeliveryType(currentValue);
                
            case CLINIC_TYPE:
                return differentClinicType(currentValue);
                
            case HDS_SUB_TYPE:
                return differentHdsSubType(currentValue);
                
            case PCI_FLAG:
                // For boolean, flip the current value
                return String.valueOf(!Boolean.parseBoolean(currentValue));
                
            case CLINIC_HOURS_OF_OPERATION:
                return organizationDataGenerator.generateClinicHourEntry();
                
            case CLINIC_OWNER_NAMES:
                return organizationDataGenerator.generateClinicOwnerName();
                
            case CLINIC_LEGAL_BUSINESS_NAME:
                return organizationDataGenerator.generateClinicLegalBusinessName();
                
            case PAYEE_NUMBER:
                return organizationDataGenerator.generatePayeeNumber();
                
            case ADDRESS_UNIT:
                return organizationDataGenerator.generateAddressUnit();
                
            default:
                LOG.error("Unknown property type for getDifferentPropertyValue: {}", propertyType);
                return "";
        }
    }

    // Minimal helper to add a tiny pause for async UI updates
    private static void shortUiPause()
    {
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { }
    }

}
