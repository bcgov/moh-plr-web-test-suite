package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.PlrNavigationMenuFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.AddFacilityAddressFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.AddFacilityIdFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.AddFacilityNameFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.AddFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.AddFacilitySummaryFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
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
import java.util.LinkedHashMap;
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
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        //TODO use FHIR data generator in order to create unique facilities.

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.fillFacilitySection("Test Facility", "Facility Description");
        addFacility.clickNext("Facility", "");
        addFacility.fillAddressSection("2269 DOUGLAS ST, V", "2269 DOUGLAS ST, V");
        addFacility.clickNext("Address", "");
        AddFacilitySummaryFragment summaryFragment = addFacility.getFacilitySummary();
        ViewFacilityPage newFacility = summaryFragment.clickSubmitButton();
        
        // TODO implement FHIR search check to verify data matches.

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

            workflow.logout();
            workflow.close();
        }

    }
    
    @Test
    // F3-003. Facility Minimum Data Requirements
    public void testFacilityMinimumDataRequirements()
    {
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);
 
        //TODO use FHIR data generator in order to create unique facilities.
        //Step 1 - Create a new facility using minimum data.
        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.clickNext("Facility", "");

        AddFacilityAddressFragment addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillAddressLine1("50 Douglas St");
        addressFragment.fillCity("Victoria", "Victoria");
        addressFragment.effectiveFromCurrentDate();
        addFacility.clickNext("Address", "");
        AddFacilitySummaryFragment summaryFragment = addFacility.getFacilitySummary();
        ViewFacilityPage newFacility = summaryFragment.clickSubmitButton();
        
        //Step 2 - Verify all the information on each data block
        // Verify there is exactly one identifier
        int identifierCount = newFacility.grabDataBlockCount(FacilitySection.IDENTIFIERS);
        assertEquals(identifierCount, 1, 
                "There should be exactly one identifier in the facility.");
               
        // Verify there are NO names (since we created with minimum data)
        int nameCount = newFacility.grabDataBlockCount(FacilitySection.NAMES);
        assertEquals(nameCount, 0, 
                "There should be no facility names when created with minimum data.");

        //Step 3 - Examine the civic address data block. Make sure there is only one address.
        int civicAddressCount = newFacility.grabDataBlockCount(FacilitySection.CIVIC_ADDRESSES);
        assertEquals(civicAddressCount, 1, 
                "There should be exactly one civic address in the facility.");

        //Step 4 - Examine the other address data block. Make sure there is only one address.
        int otherAddressCount = newFacility.grabDataBlockCount(FacilitySection.OTHER_ADDRESS);
        assertEquals(otherAddressCount, 1, 
                "There should be exactly one other address in the facility.");

        //Step 5 - Attempt to create a new facility using minimum data, but do not specify the "Facility Start date".
        addFacility = navigateToAddFacilityPage(workflowManager_);
        AddFacilityIdFragment identifierFields = addFacility.fillIdentifierSection("BUILDING", "Select One", "", null);
        addFacility.clickNext("Identifier", null);

        List<String> errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        List<String> highlightedFields = identifierFields.getHighlightedFields();
        
        assertTrue(errorMessageList.contains("GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Effective From'. Your transaction has not been processed. Correct and resubmit."),
                "Facility Identifier Effective From not selected should return an error.");
        assertEquals(highlightedFields.getLast(), "Effective From:*",
                "Facility Identifier Effective From is unhighlighted, or more than one error occurred.");

        //Step 6 - Attempt to create a new facility using minimum data, but do not specify the "Facility Type".
        // Navigate to fresh Add Facility page to clear previous form state
        addFacility = navigateToAddFacilityPage(workflowManager_);
        
        identifierFields = addFacility.fillIdentifierSection("Select One", "Select One", "", List.of(2025, 11, 20));
        addFacility.clickNext("Identifier", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierFields.getHighlightedFields();

        assertTrue(errorMessageList.contains("GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Facility Type'. Your transaction has not been processed. Correct and resubmit."),
                "Facility Type not selected should return an error.");
        assertEquals(highlightedFields.getLast(), "Facility Type:*",
                "Facility Type is unhighlighted, or more than one error occurred.");

        //Step 7 - Attempt to create a new facility using minimum data (as in Step 1), but do not specify the "Facility Address Line 1".
        addFacility = navigateToAddFacilityPage(workflowManager_);
        
        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.clickNext("Facility", "");
        
        addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillCity("Victoria", "Victoria");
        addressFragment.effectiveFromCurrentDate();
        addFacility.clickNext("Address", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        List<String> highlightedAddressFields = addressFragment.getHighlightedFields();
        
        assertTrue(errorMessageList.contains("GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Address Line 1'. Your transaction has not been processed. Correct and resubmit."),
                "Address Line 1 not filled should return an error.");
        assertEquals(highlightedAddressFields.getLast(), "Address Line 1:*",
                "Address Line 1 is unhighlighted, or more than one error occurred.");

        //Step 8 - Attempt to create a new facility using minimum data (as in Step 1), but do not specify the "Facility Address City".
        addFacility = navigateToAddFacilityPage(workflowManager_);
        
        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.clickNext("Facility", "");
        
        addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillAddressLine1("123 Test Street");
        addressFragment.effectiveFromCurrentDate();
        addFacility.clickNext("Address", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedAddressFields = addressFragment.getHighlightedFields();
        
        assertTrue(errorMessageList.contains("GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'City'. Your transaction has not been processed. Correct and resubmit."),
                "City not filled should return an error.");
        assertEquals(highlightedAddressFields.getLast(), "City:*",
                "City is unhighlighted, or more than one error occurred.");

        //Step 9 - Attempt to create a new facility using minimum data (as in Step 1), but do not specify the "Facility Address Effective From Date".
        addFacility = navigateToAddFacilityPage(workflowManager_);
        
        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.clickNext("Facility", "");
        
        addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillAddressLine1("123 Test Street");
        addressFragment.fillCity("Victoria", "Victoria");
        addFacility.clickNext("Address", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedAddressFields = addressFragment.getHighlightedFields();
        
        assertTrue(errorMessageList.contains("GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Effective From'. Your transaction has not been processed. Correct and resubmit."),
                "Address Effective From date not selected should return an error.");
        assertEquals(highlightedAddressFields.getLast(), "Effective From:*",
                "Address Effective From is unhighlighted, or more than one error occurred.");
    } 

    @Test
    // F3-004. Validate Facility Type Code
    public void testValidateFacilityTypeCode()
    {
        // Navigate to Add Facility page
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);
        
        // Get the workflow to properly create a fragment reference
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
        
        // Wait for the page and create fragment - the page is already on the Identifier step
        addFacility.waitForAddFacilityStep("Identifier", true);
        
        // Create the fragment (it will find the existing identifier section on the page)
        AddFacilityIdFragment identifierFragment = new AddFacilityIdFragment(workflow.getSeleniumSession());
        
        // Get the default selected value before interacting with dropdown
        String defaultValue = identifierFragment.getFacilityType();
        
        // Verify the default value is "Select One"
        assertEquals(defaultValue, "Select One", 
                "Default Facility Type should be 'Select One' (not selected)");
        
        // Get all available options in the Facility Type dropdown
        List<String> facilityTypeOptions = identifierFragment.getFacilityTypeOptions();
        
        // Verify that only "BUILDING" is available as an option
        assertEquals(facilityTypeOptions.size(), 2,
                "Facility Type dropdown should contain two options");
        assertTrue(facilityTypeOptions.contains("BUILDING - Building"),
                "'BUILDING - Building' should be a Facility Type option");
        assertTrue(facilityTypeOptions.contains("Select One"),
                "'Select One' should be a Facility Type option");

    }

    @Test
    // F3-007. Generating Internal Facility Code (IFC)
    public void testGeneratingInternalFacilityCode()
    {
        //TODO
    }

    @Test
    // F3-008. Validate Facility Name
    public void testValidateFacilityName()
    {
        //Step 1 - Create a new facility without name
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);
        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.clickNext("Facility", "");

        AddFacilityAddressFragment addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillAddressLine1("20 Olympia Ave");
        addressFragment.fillCity("Victoria", "Victoria");
        addressFragment.effectiveFromCurrentDate();
        addFacility.clickNext("Address", "");
        AddFacilitySummaryFragment summaryFragment = addFacility.getFacilitySummary();
        summaryFragment.clickSubmitButton();

        //TODO check with FHIR that facility got created without name

        //Step 2 - Start creating a new facility, specify a facility name, but do not specify the "Effective From" date for the name.
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("Test Facility", "Facility Description", null);
        addFacility.clickNext("Facility", null);

        List<String> errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        
        assertTrue(errorMessageList.contains("Effective Start Date is required when Name is provided"),
                "Not selecting Effective From date for Facility Name should return an error if a name is provided.");
        
        //Step 3 - Start creating a new facility, specify a facility name that exceeds the maximum length of 100 characters.
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Facility Description");
        addFacility.clickNext("Facility", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        assertTrue(errorMessageList.contains("GRS.SYS.UNK.UNK.1.0.5003: Entry Error. 'Facility Name' length must be between 0 and 100. Your transaction has not been processed. Correct and resubmit."),
                "Facility Name should return an error if a name is provided with more than a 100 characters.");        

        //Step 4 - Create a new facility with the facility name exactly the maximum length of 100 characters.
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Facility Description");
        addFacility.clickNext("Facility", "");

        addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillAddressLine1("576 Dallas Rd");
        addressFragment.fillCity("Victoria", "Victoria");
        addressFragment.effectiveFromCurrentDate();
        addFacility.clickNext("Address", "");
        summaryFragment = addFacility.getFacilitySummary();
        summaryFragment.clickSubmitButton();

        //TODO check with FHIR that facility got created with name with 100 characters long

        //Step 5 - Send acceptable characters for Facility name and description.
        //Valid chars as per ALM: <blank space>&()+-./0123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZ\abcdefghijklmnopqrstuvwxyz
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("ABCDEFGHIJKLMNOPQRSTUVWXYZ\\&()+-./0123456789: abcdefghijklmnopqrstuvwxyz", "Facility Description");
        addFacility.clickNext("Facility", "");
        
        addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillAddressLine1("18 Douglas St");
        addressFragment.fillCity("Victoria", "Victoria");
        addressFragment.effectiveFromCurrentDate();
        addFacility.clickNext("Address", "");
        summaryFragment = addFacility.getFacilitySummary();
        summaryFragment.clickSubmitButton();

        //TODO check with FHIR that facility created with valid characters in name
    }

    @Test
    // F3-009. Validate Facility Description
    public void testValidateFacilityDescription()
    {
        //Step 1 - Create a new facility, specify a facility name, but do not specify facility description.
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        //TODO use FHIR data generator in order to create unique facilities.

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.fillFacilitySection("Test Facility", "");
        addFacility.clickNext("Facility", "");

        AddFacilityAddressFragment addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillAddressLine1("580 Simcoe St");
        addressFragment.fillCity("Victoria", "Victoria");
        addressFragment.effectiveFromCurrentDate();
        addFacility.clickNext("Address", "");

        AddFacilitySummaryFragment summaryFragment = addFacility.getFacilitySummary();
        summaryFragment.clickSubmitButton();
        
        // TODO implement FHIR search check to verify data matches.

        //Step 2 - Start creating a new facility, specify a facility description, but do not specify the facility name.
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("", "Facility Description");
        addFacility.clickNext("Facility", null);

        List<String> errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        
        assertTrue(errorMessageList.contains("Name is required when Description is provided"),
                "An error should be returned when Facility Description is provided without a Name.");

        //Step 3 - Start creating a new facility, specify a facility name and facility description, but do not specify the "Effective From" date for the name.
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("Test Facility", "Facility Description", null);
        addFacility.clickNext("Facility", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        
        assertTrue(errorMessageList.contains("Effective Start Date is required when Name is provided"),
                "Not selecting Effective From date for Facility Name should return an error if a name and description is provided.");

        //Step 4 - Start creating a new facility, specify a facility name, and a facility description that exceeds the maximum length of 200 characters.
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("Test Facility", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        addFacility.clickNext("Facility", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        
        assertTrue(errorMessageList.contains("GRS.SYS.UNK.UNK.1.0.5003: Entry Error. 'Facility Description' length must be between 0 and 200. Your transaction has not been processed. Correct and resubmit."),
                "Facility Description should return an error if a description is provided with more than 200 characters.");

        //Step 5 - Create a new facility with a facility name and the facility description exactly the maximum length of 200 characters.
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("Test Facility", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        addFacility.clickNext("Facility", "");

        addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillAddressLine1("204 Government St");
        addressFragment.fillCity("Victoria", "Victoria");
        addressFragment.effectiveFromCurrentDate();
        addFacility.clickNext("Address", "");
        
        summaryFragment = addFacility.getFacilitySummary();
        summaryFragment.clickSubmitButton();

        // TODO implement FHIR search check to verify data matches.
        
    }

    @Test
    // F3-011. Facility Duplicate Check
    public void testFacilityDuplicateCheck()
    {
        //For this test case it would be useuful to have the FHIR endpoints to create a Facility first, then attempt to create via UI to check for duplicates.\

        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);
        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.fillFacilitySection("Test Facility", "");
        addFacility.clickNext("Facility", "");

        AddFacilityAddressFragment addressFragment = new AddFacilityAddressFragment(workflowManager_.selectWorkflow(UserType.ADMIN).getSeleniumSession());
        addressFragment.fillAddressLine1("580 Simcoe St");
        addressFragment.fillCity("Victoria", "Victoria");
        addressFragment.effectiveFromCurrentDate();
        addFacility.clickNext("Address", null);

        List<String> warningMessageList = addFacility.waitForAlertMessagesFragment().grabWarningMessageList();
        
        assertTrue(warningMessageList.contains("PRS.SYS.ADR.UNK.1.0.7055: An existing facility address was found, can not create duplicate facility. IFC.00006533.BC.PRS"),
                "Facility Duplicate Check did not return expected warning message.");

    }

    /*@Test
    // F3-017. Validate Address Lines
    public void testValidateAddressLines()
    {
        //TODO
    }

    @Test
    //F3-018. Validate City
    public void testValidateCity()
    {
        //TODO
    }

    @Test
    // F3-022. Validate Effective Start Date Format
    public void testValidateEffectiveStartDateFormat()
    {
        //Step 1 and 2 - Start creating a Facility and specify a facility type, and enter the "Effective From" date in an incorrect format (e.g. MM-DD-YYYY).
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "", List.of(12, 31, 2025));
        addFacility.clickNext("Identifier", null);

        List<String> errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        
        assertTrue(errorMessageList.contains("GRS.SYS.UNK.UNK.1.0.5004: Entry error. The following field may contain only a date: 'Effective From'. Your transaction has not been processed. Correct and resubmit."),
                "MM-DD-YYYY format for Effective From date should return an error in Type, Identifier Facility.");
        
        //Step 3 and 4 - Enter a facility name, and enter the "Effective From" date in an incorrect format (e.g. MM-DD-YYYY).
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("Test Facility", "Facility Description", List.of(12, 31, 2025));
        addFacility.clickNext("Facility", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        
        assertTrue(errorMessageList.contains("GRS.SYS.UNK.UNK.1.0.5004: Entry error. The following field may contain only a date: 'Effective From'. Your transaction has not been processed. Correct and resubmit."),
                "MM-DD-YYYY format for Effective From date should return an error in Facility Name section.");

        //Step 5, 6 and 7- Enter a facility address, and enter the "Effective From" date in an incorrect format (e.g. MM-DD-YYYY). Finally correct and create facility.
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        addFacility.fillFacilitySection("Test Facility", "Facility Description");
        addFacility.clickNext("Facility", "");

        //TODO finish facility creation

    }*/

}
