package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.PlrNavigationMenuFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.AddFacilityIdFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.AddFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.net.Facility;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.navigateToAddFacilityPage;
import static org.testng.Assert.*;

public class CreateFacilitySimpleTests implements SimpleTest {

    private static final Logger LOG = ExecutionLogManager.getLogger();
    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;

    public CreateFacilitySimpleTests()
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

    @BeforeMethod
    public void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) workflow.login().openPlr();
    }

    @Test
    // F3-001. Create Facility
    public void testCreateFacility()
    {
        
    }

    @Test
    // F3-002. Restrict Facility Access by User Role
    public void testRestrictFacilityAccessByUserRole()
    {
        // Test that ADMIN can see Add Facility menu item
        PlrWebWorkflow adminWorkflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
        PlrNavigationMenuFragment adminMenu = adminWorkflow.getPlrWebAccessActions().waitForPlrNavigationMenuFragment();
        
        assertTrue(adminMenu.grabItemVisible(PlrNavigationMenuFragment.Item.ADD_FACILITY),
            "Add Facility menu item should be visible for ADMIN user role");

        // Test that other user types cannot see Add Facility menu item
        UserType[] userTypesToTest = {UserType.PRIMARY, UserType.SECONDARY, UserType.CONSUMER};

        for (UserType userType : userTypesToTest) {

            PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, userType);
            PlrNavigationMenuFragment menu = workflow.getPlrWebAccessActions().waitForPlrNavigationMenuFragment();

            assertFalse(menu.grabItemVisible(PlrNavigationMenuFragment.Item.ADD_FACILITY),
                String.format("Add Facility menu item should NOT be visible for %s user role", userType));

        }

    }    
    
    @Test
    // F3-003. Facility Minimum Data Requirements
    public void testFacilityMinimumDataRequirements()
    {

    }

    @Test
    // F3-004. Validate Facility Type Code
    public void testValidateFacilityTypeCode()
    {

    }

    @Test
    // F3-005. Adding a Facility ID
    public void testAddingFacilityID()
    {

    }

    @Test
    // F3-007. Generating Internal Facility Code (IFC)
    public void testGeneratingInternalFacilityCode()
    {

    }

    @Test
    // F3-008. Validate Facility Name
    public void testValidateFacilityName()
    {

    }

    @Test
    // F3-009. Validate Facility Description
    public void testValidateFacilityDescription()
    {

    }

    @Test
    // F3-011. Facility Duplicate Check
    public void testFacilityDuplicateCheck()
    {

    }

    @Test
    // F3-017. Validate Address Lines
    public void testValidateAddressLines()
    {

    }

    @Test
    //F3-018. Validate City
    public void testValidateCity()
    {

    }

    @Test
    // F3-022. Validate Effective Start Date Format
    public void testValidateEffectiveStartDateFormat()
    {

    }

}
