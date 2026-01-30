package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.viewByIdentifier;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.collect.Ordering;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.HdsType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateOrganizationPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
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
    private static JSONObject errorList,warningList;
    private static MaintainOrgBuilder defaultOrg = null;
    private static UpdateOrganizationPage defaultOrgPage = null;

	private UpdateOrganizationPropertiesTests() {
		
		try
        {
            organizationDataGenerator = OrganizationDataGenerator.getInstance();
            
            errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
            warningList = new JSONObject(Files.readString(errorPath)).getJSONObject("warnings");
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
        //Step 2 - Log in and navigate to Update Organization page for the default org
		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
		if (!workflow.isLoggedIn()) {
			workflow.login().openPlr();
		}
		
		// Navigate to the default org and get UpdateOrganizationPage
		defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(defaultOrg.getIdentifier(IdentifierType.IPC), workflowManager_);
	}

	// 001. Add property Clinic Hours of Operation
	@Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
	public void testAddClinicHoursOfOperation() {
        //Step 3 - Make a Search by Identifier, make sure Clinic Hours of Operation is displayed correctly in View screen
		String validHours = organizationDataGenerator.generateClinicHourEntry();
		
		String errorMsg = defaultOrgPage.addOrganizationPropertyDataBlock(
			OrganizationProperties.CLINIC_HOURS_OF_OPERATION, 
			validHours);
		
		assertTrue(errorMsg.isEmpty(), 
			"Failed to add Clinic Hours of Operation: " + errorMsg);
		LOG.info("Successfully added Clinic Hours of Operation property");

        //Step 4 - Update Clinic Hours of Operation property to a valid value

        //Step 7 - Try to update Hours of operation block with invalid day format "Monday" instead of "Mon"

        //Step 8 - Try to create a duplicate clinic hours of operation block

        //Step 5 - Cease Clinic Hours of Operation property

        //Step 6 - Try adding an Hours of operation block with invalid characters at start and end of days of week

	}

    // 002. Add property Clinic Ownership Type
	@Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
	public void testAddClinicOwnershipType() {
        //Step1-3 Create Org and afterwards add Clinic Ownership Type property, then cease block and repeat adding property with all different valid values

        //Step 4 - Try to add a second clinic ownership type block when one is already active


	}

    //003. Validate Clinic Ownership Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicOwnershipType() {
        //Step 1 and 2 - create org through FHIR with Clinic Ownership Type.

        //Step 3 - Update value with CHG

        //Step 4 - Update value with CORR

        //Step 5 - Cease value

        //Step 6-9 N/A
    }

    //004. Add property Clinic Service Delivery Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddClinicServiceDeliveryType() {
        //Step 1 and 2 - create org and add Clinic Service Delivery Type.

        //Step 3 - Cease and add block with all other valid values

        //Step 4 - Try to add a second clinic service delivery type block when one is already active
    }

    //005. Validate Clinic Service Delivery Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicServiceDeliveryType() {
        //Step 1 and 2 -Create org through FHIR with Clinic Service Delivery Type.

        //Step 3 - Update value with CHG

        //Step 4 - Update value with CORR

        //Step 8 - Try to add a second duplicate block when one is already active

        //Step 5 - Cease value

        //Step 6-7 N/A
 

    }

    //006. Add property Clinic Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddClinicType() {
        //Step 1 and 2 - create org and add Clinic Type.

        //Step 3 - Cease and add block with all other valid values

        //Step 4 - Try to add a second clinic type block when one is already active
    }

    //007. Validate Clinic Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicType() {
        //Step 1 and 2 -Create org through FHIR with Clinic Type.

        //Step 3 - Update value with CHG

        //Step 4 - Update value with CORR

        //Step 8 - Try to add a second duplicate block when one is already active

        //Step 5 - Cease value

        //Step 6-7 N/A
    }

    //008. Add property PCI Flag
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddPciFlag() {
        //Step 1 and 2 - create org and add PCI Flag as true.

        //Step 3 - Update PCI Flag to false

        //Step 4 - try to add a second PCI Flag block when one is already active
    }

    //009. Validate PCI Flag
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidatePciFlag() {
        //Step 1 and 2 -Create org through FHIR with PCI Flag as true.

        //Step 3 - Update PCI Flag to false with CHG

        //Step 4 - Update PCI Flag to true with CORR

        //Step 8 - Try to add a second duplicate block when one is already active

        //Step 5 - Cease value
    }

    //010. Add property Clinic Owner
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddClinicOwner() {
        //Step 1 and 2 - create org and add Clinic Owner.

        //Step 3 - Update Clinic Owner to another valid value end reason CHG

        //Step 4 - Add Clinic Owner with another valid value. Successfully added new block.
    }

    //011. Validate Clinic Owner
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicOwner() {
        //Step 1 and 2 -Create org through FHIR with Clinic Owner.

        //Step 3 - Update Clinic Owner to another valid value end reason CHG

        //Step 4 - Update Clinic Owner to another valid value end reason CORR

        //Step 7 - Try to update block with more than 400 characters

        //Step 8 - Try to add a second duplicate block

        //Step 5 - Cease value
    }

    //012. Add property Clinic Legal name
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddClinicLegalName() {
        //Step 1 and 2 - create org and add Clinic Legal name.

        //Step 3 - Update Clinic Legal name to another valid value end reason CHG

        //Step 4 - Try to add a second Clinic Legal name block when one is already active


    }

    //013. Validate Clinic Legal name
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateClinicLegalName() {
        //Step 1 and 2 -Create org through FHIR with Clinic Legal name.

        //Step 3 - Update Clinic Legal name to another valid value end reason CHG

        //Step 4 - Update Clinic Legal name to another valid value end reason CORR

        //Step 7 - Try to update block with more than 400 characters

        //Step 8 - Try to add a second duplicate block

        //Step 5 - Cease value

    }

    //014. Add property Payee Number
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddPayeeNumber() {
        //Step 1 and 2 - create org and add Payee Number.

        //Step 3 - Update Payee Number to another valid value end reason CHG
    }

    //015. Validate Payee Number
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidatePayeeNumber() {
        //Step 1 and 2 -Create org through FHIR with Payee Number.

        //Step 3 - Update Payee Number to another valid value end reason CHG

        //Step 4 - Update Payee Number to another valid value end reason CORR

        //Step 6 - Try to update block with invalid characters

        //Step 7 - Try to update block with more than 400 characters

        //Step 8 - Try to add a second duplicate block

        //Step 5 - Cease value
    }

    //016. Add property HDS Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testAddHdsType() {
        //Step 1 - 2 Create an HDS Org and afterwards add HDS Type property

        //Step 3 - Cease block and repeat adding property with all different valid values

        //Step 4 - Try to add a second HDS Type block when one is already active
    }

    //017. Validate HDS Type
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testValidateHdsType() {
        //Step 1 and 2 - create HDS org through FHIR with HDS Type.

        //Step 3 - Update value with CHG

        //Step 4 - Update value with CORR

        //Step 8 - Try to add a second duplicate block when one is already active (Marked as N/A in docs, however we can try and check result)

        //Step 5 - Cease value

    }

    //018. Update Org Property
    @Test(groups = { "UpdateProvider", "UpdateOrganization", "OrganizationProperties"})
    public void testUpdateOrgProperty() {
        //Step 1 - Create org through FHIR

        //Step 2 and 3- Add and try to update a property

        //Step 4 - Logout and login again, update property again.
        
        //Step 5 - Repeat step 2-4 for all other org properties
    }

}
