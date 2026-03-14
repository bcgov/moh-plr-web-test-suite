package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderAddressFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.*;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;


public class AddProviderTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private final IndividualDataGenerator dataGen = IndividualDataGenerator.getInstance();
    private final OrganizationDataGenerator orgDataGen = OrganizationDataGenerator.getInstance();
    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;
    private static JSONObject warningList;

    private AddProviderTests() {

        try
        {
            JSONObject root = new JSONObject(Files.readString(errorPath));
            errorList = root.getJSONObject("errors");
            warningList = root.getJSONObject("warnings");

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

        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) {
            workflow.login().openPlr();
        }
    }

    // sample test to aid in development of page objects and workflow for Add Provider. Does not correspond to any test case in ALM.
    @Test
    public void testAddProviderSampleTest() {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

        page.fillIdentifier(ProviderRoleTypeOptions.OOPMD, null, null, "OOPID", "252526");
        page.fillStatus("AE", StatusCodeOption.CANCELLED, StatusReasonCodeOption.LAP);

        page.clickNext("Status", "");
        page.waitForAddProviderStep("Personal Information", true);

        page.fillPI("Dr.", "Testing", "Provider", null, "Smith");
        page.fillDemographics(List.of(2011,1,1), "U");

        page.clickNext("Personal Information", "");
        page.waitForAddProviderStep("Address", true);

        AddProviderAddressFragment address = page.fillAddress("P", "HC", List.of("123 Test St", "Unit 1", ""),
                "Victoria", "BC", "CA", "V9V9V9");
        page.fillPhone("250", "5551234", "123");
        page.fillFax("250", "5555678");
        page.fillEmail("test@example.com");

        page.clickNext("Address", "Address Invalid");
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);

        page.fillCredentials("BD", "Test", "5358", "TestInst",
                "Victoria", "CA", "BC", true, "2001");
        page.fillExpertise("ENG", "2500");
        ViewProviderPage viewPage = page.clickSubmitButton();
    }

    // sample test to aid in development of page objects and workflow for Add Provider. Does not correspond to any test case in ALM.
    @Test
    public void testAddOrganizationSampleTest() {
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddOrganization().openProviderPage(ProviderType.ORGANIZATION);


        page.fillOrganizationIdentifier(OrganizationalProviderRoleType.ORG, null, null, "ORGID", "252526");
        page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);

        page.clickNext("Status", "");
        page.waitForAddProviderStep("Organization", true);

        page.fillOrganizationName("test00", "Testing");
        page.clickNext("Organization", "");
        page.waitForAddProviderStep("Address", true);


        AddProviderAddressFragment address = page.fillAddress("P", "BC", List.of("123 Test St", "Unit 1", ""),
                "Victoria", "BC", "CA", "V9V0C6");
        page.fillPhone("250", "5551234", "123");
        page.fillFax("250", "5555678");
        //page.fillEmail("test@organization.com");
        page.clickNext("Address", "Address Invalid");
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);


        ViewProviderPage viewPage = page.clickSubmitButton();
    }

    //Validate Address line 1
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressLine1(ProviderType providerType) {
        //Step 1 & 2 - Login and navigate to create provider page, contact tab
        AddProviderPage page = navigateToAddressScreen(providerType);

        AddProviderAddressFragment address = page.fillAddress("P", "HC", List.of("A".repeat(101), "", ""),
                "Victoria", "BC", "CA", "V9V9V9");

        page.clickNext("Address", null);
        shortUiPause();
        // Verify the error message appears
        List<String> errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String addressLine1TooLong = errorList.getString("addressLine1TooLong");
        assertTrue(errors.contains(addressLine1TooLong), "Address line 1 with more than 100 characters should return an error.");

        //Step 4 - Enter an address line 1 blank
        address = page.fillAddress("P", "HC", List.of("", "", ""),
                "Victoria", "BC", "CA", "V9V9V9");

        page.clickNext("Address", null);
        shortUiPause();
        errors = page.waitForAlertMessagesFragment().grabErrorMessageList();

        String addressLine1Blank = errorList.getString("missingAddressLine1");
        assertTrue(errors.contains(addressLine1Blank), "Address line 1 blank should return an error.");
    }

    //Validate Address Purpose code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressPurposeCode(ProviderType providerType) {
        //Step 1 - Login and navigate to create provider page, contact tab
        AddProviderPage page = navigateToAddressScreen(providerType);

        // Initialize the address fragment to access purpose options
        AddProviderAddressFragment addressFragment = new AddProviderAddressFragment(
                workflowManager_.getSelectedWorkflow().getSeleniumSession());

        //Step 3 - Verify address purpose code is mandatory and the list of codes is complete
        List<String> purposeOptions = addressFragment.getAddressPurposeOptions();

        // Log the available options for debugging
        LOG.info("Available Address Purpose options: {}", purposeOptions);

        // Verify all expected purpose codes are present using the CommunicationPurpose enum
        for (CommunicationPurpose expectedPurpose : CommunicationPurpose.values()) {
            boolean found = purposeOptions.stream().anyMatch(expectedPurpose::matchesOption);
            assertTrue(found, "Address Purpose options should contain code: " + expectedPurpose.getCode());
        }

       page.fillAddress("P", null, List.of("123 Test St", "", ""),
                "Victoria", "BC", "CA", "V8V1V1");

        page.clickNext("Address", null);
        shortUiPause();
        List<String> errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String missingPurpose = errorList.getString("errMsg5000Purpose");
        assertTrue(errors.contains(missingPurpose), "Missing Purpose should return an error.");
    }

    //Validate Address Type code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateAddressTypeCode(ProviderType providerType) {
        //Step 1 - Login and navigate to create provider page, contact tab
        AddProviderPage page = navigateToAddressScreen(providerType);

        // Initialize the address fragment to access type options
        AddProviderAddressFragment addressFragment = new AddProviderAddressFragment(
                workflowManager_.getSelectedWorkflow().getSeleniumSession());

        //Step 2 - Verify that the list of address type codes is complete and mandatory
        List<String> typeOptions = addressFragment.getAddressTypeOptions();

        // Log the available options for debugging
        LOG.info("Available Address Type options: {}", typeOptions);

        // Verify all expected type codes are present using the AddressType enum
        for (AddressType expectedType : AddressType.values()) {
            boolean found = typeOptions.stream().anyMatch(expectedType::matchesOption);
            assertTrue(found, "Address Type options should contain code: " + expectedType.getCode());
        }

        //Step 3 - Fill address without selecting a Type (leave as "Select One") and verify error
        page.fillAddress(null, "HC", List.of("123 Test St", "", ""),
                "Victoria", "BC", "CA", "V8V1V1");

        page.clickNext("Address", null);
        shortUiPause();
        List<String> errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String missingType = errorList.getString("errMsg5000AddressType");
        assertTrue(errors.contains(missingType), "Missing Address Type should return an error.");
    }

    //Validate city
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateCity(ProviderType providerType) {
        //Step 1 - Login and navigate to create provider page, contact tab
        AddProviderPage page = navigateToAddressScreen(providerType);

        //Step 2 - Enter minimum data to add an address and leave city blank
        AddProviderAddressFragment address = page.fillAddressRawCity("P", "HC", List.of("123 Test St", "", ""),
                "", "BC", "CA", "V9V9V9");

        page.clickNext("Address", null);
        shortUiPause();
        List<String> errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String missingCity = errorList.getString("missingCity");
        assertTrue(errors.contains(missingCity), "City blank should return an error.");

        //Step 3 - Enter a city with more than 60 characters
        address = page.fillAddressRawCity("P", "HC", List.of("123 Test St", "", ""),
                "A".repeat(61), "BC", "CA", "V9V9V9");

        page.clickNext("Address", null);
        shortUiPause();
        errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String cityTooLong = errorList.getString("cityTooLong");
        assertTrue(errors.contains(cityTooLong), "City with more than 60 characters should return an error.");

        //Step 4 - Enter a city with 60 characters (should pass validation)
        address = page.fillAddressRawCity("P", "HC", List.of("123 Test St", "", ""),
                "A".repeat(60), "BC", "CA", "V9V9V9");

        page.clickNext("Address", "Address Invalid");
        shortUiPause();
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);
    }

    //Validate Electronic Address txt
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateElectronicAddress(ProviderType providerType) {
        //Step 1&2 - Login and navigate to create provider page, address tab
        AddProviderPage page = navigateToAddressScreen(providerType);

        // Fill minimum address data to ensure form is valid for submission
        AddProviderAddressFragment address = page.fillAddress("P", "HC", List.of("123 Test St", "", ""),
                "Victoria", "BC", "CA", "V9V9V9");

        //Step 3 - In the email section, enter an email address with more than 501 characters
        page.fillEmail("A".repeat(492) + "@test.com"); // 501 total characters

        page.clickNext("Address", null);
        shortUiPause();
        List<String> errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String emailTooLong = errorList.getString("errMsg5003ElectronicAddress");
        assertTrue(errors.contains(emailTooLong), "Email with more than 500 characters should return an error.");

        //Step 4 - In the email section, enter an email address blank
        page.fillEmail("");

        page.clickNext("Address", null);
        shortUiPause();
        errors = page.waitForAlertMessagesFragment().grabErrorMessageList();

        String emailBlank = errorList.getString("errMsg5000EmailAddress");
        assertTrue(errors.contains(emailBlank), "Email blank should return an error.");

        //Step 5 - In the email section, enter an email address in invalid format
        page.fillEmail("invalidemail");

        page.clickNext("Address", null);
        shortUiPause();
        errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String invalidFormat = errorList.getString("errMsgprovider7013");
        assertTrue(errors.contains(invalidFormat), "Invalid email format should return an error.");

        //Step 6 - In the email section, enter an email that has one or more periods separating portions of the text, preceding and following the @ separator
        page.fillEmail("test.user@example.domain.com");

        page.clickNext("Address", "Address Invalid");
        shortUiPause();
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);
    }

    //Validate Postal code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidatePostalCode(ProviderType providerType) {
        //Step 1 - Login and navigate to create provider page, contact tab
        AddProviderPage page = navigateToAddressScreen(providerType);

        //Step 2 - Select country that is not US or CA and enter a postal code with more than 25 characters.
        AddProviderAddressFragment address = page.fillAddressRawCity("P", "HC", List.of("123 Test St", "", ""),
                "London", null, "GB", "A".repeat(26));

        page.clickNext("Address", null);
        shortUiPause();
        List<String> errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String postalCodeTooLong = errorList.getString("errMsg5003PostalCode");
        assertTrue(errors.contains(postalCodeTooLong), "Postal code with more than 25 characters should return an error for non-CA/US country.");

        //Step 3 - Select CA as country and enter a postal code with more than 25 characters.
        address = page.fillAddressRawCity("P", "HC", List.of("123 Test St", "", ""),
                "Victoria", null, "CA", "A".repeat(26));

        page.clickNext("Address", null);
        shortUiPause();
        errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String postalCodeIncorrectFormat = errorList.getString("errMsg7009");
        assertTrue(errors.contains(postalCodeIncorrectFormat), "Postal code with more than 25 characters should return a formatting error for CA country.");

        //Step 4 - Select CA as country and enter a postal code in correct format (ANA NAN).
        address = page.fillAddress("P", "HC", List.of("123 Test St", "", ""),
                "Victoria", "BC", "CA", "V8V 1V1");

        page.clickNext("Address", "Address Invalid");
        shortUiPause();
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);
    }

    //Validate Telcommunication number
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateTelecommunicationNumber(ProviderType providerType) {
        //Step 1 - Login and navigate to create provider page, contact tab
        AddProviderPage page = navigateToAddressScreen(providerType);

        // Fill minimum address data to ensure form is valid for submission
        AddProviderAddressFragment address = page.fillAddress("P", "HC", List.of("123 Test St", "", ""),
                "Victoria", "BC", "CA", "V9V9V9");

        //Step 2 - Send area code with more than 15 characters.
        page.fillPhone("1".repeat(16), "5551234", null);

        page.clickNext("Address", null);
        shortUiPause();
        List<String> errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String areaCodeTooLong = errorList.getString("errMsg5003TelecomAreaCode");
        assertTrue(errors.contains(areaCodeTooLong), "Area code with more than 15 characters should return an error.");

        //Step 3 - Send phone number with more than 30 characters.
        page.fillPhone("250", "1".repeat(31), null);

        page.clickNext("Address", null);
        shortUiPause();
        errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String phoneNumberTooLong = errorList.getString("errMsg5003TelecomPhoneNumber");
        assertTrue(errors.contains(phoneNumberTooLong), "Phone number with more than 30 characters should return an error.");

        //Step 4 - Send extension with more than 15 characters.
        page.fillPhone("250", "5551234", "1".repeat(16));

        page.clickNext("Address", null);
        shortUiPause();
        errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        String extensionTooLong = errorList.getString("errMsg5003TelecomExtension");
        assertTrue(errors.contains(extensionTooLong), "Extension with more than 15 characters should return an error.");

        //Step 5 - In the fax section. Add a area code with more than 15 characters.
        page.fillPhone("250", "5551234", "123"); // Reset phone to valid values
        page.fillFax("1".repeat(16), "5555678");

        page.clickNext("Address", null);
        shortUiPause();
        errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertTrue(errors.contains(areaCodeTooLong), "Fax area code with more than 15 characters should return an error.");

        //Step 6 - In the fax section. Add a number with more than 30 characters.
        page.fillFax("250", "1".repeat(31));

        page.clickNext("Address", null);
        shortUiPause();
        errors = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertTrue(errors.contains(phoneNumberTooLong), "Fax number with more than 30 characters should return an error.");

        //Final step - Enter valid data and proceed to next step
        page.fillFax("250", "5555678");
        page.clickNext("Address", "Address Invalid");
        shortUiPause();
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);
    }

    //Validate Province and state Address codes with Country
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateProvinceAndStateAddressCodesWithCountry(ProviderType providerType) {
        //Step 1&2 - Login and navigate to create provider page, contact tab.
        AddProviderPage page = navigateToAddressScreen(providerType);

        // Initialize the address fragment to access province/country options
        AddProviderAddressFragment addressFragment = new AddProviderAddressFragment(
                workflowManager_.getSelectedWorkflow().getSeleniumSession());

        //Select CA as country and verify the list of provinces displayed is correct.
        addressFragment.selectCountry("CA");
        shortUiPause();

        // Verify province dropdown is displayed for Canada
        assertTrue(addressFragment.isProvinceStateDropdownDisplayed(),
                "Province/State dropdown should be displayed when Canada is selected.");

        List<String> caProvinceOptions = addressFragment.getProvinceStateOptions();
        // Verify all expected Canadian provinces/territories are present
        for (CanadianProvince expectedProvince : CanadianProvince.values()) {
            boolean found = caProvinceOptions.stream().anyMatch(expectedProvince::matchesOption);
            assertTrue(found, "Province options should contain code: " + expectedProvince.getCode());
        }

        //Step 3 - Select US as country and verify the list of states displayed is correct.
        addressFragment.selectCountry("US");
        shortUiPause();

        // Verify province/state dropdown is displayed for United States
        assertTrue(addressFragment.isProvinceStateDropdownDisplayed(),
                "Province/State dropdown should be displayed when United States is selected.");

        List<String> usStateOptions = addressFragment.getProvinceStateOptions();
        // Verify all expected US states are present
        for (USState expectedState : USState.values()) {
            boolean found = usStateOptions.stream().anyMatch(expectedState::matchesOption);
            assertTrue(found, "State options should contain code: " + expectedState.getCode());
        }

        //Step 4 - Select a non-CA/US country and verify that province/state field becomes a free text field.
        addressFragment.selectCountry("MX");
        shortUiPause();

        // Verify province/state dropdown is NOT displayed for non-CA/US country
        assertTrue(!addressFragment.isProvinceStateDropdownDisplayed() || addressFragment.isProvinceStateTextInputDisplayed(),
                "Province/State dropdown should be hidden or text input should be displayed when a non-CA/US country is selected.");

        // If text input is displayed, verify we can fill it with raw text
        if (addressFragment.isProvinceStateTextInputDisplayed()) {
            addressFragment.fillProvinceStateRaw("Test Province");
            LOG.info("Successfully filled Province/State text input for non-CA/US country.");
        }
    }

    //Validate Communication Purpose Type Code
    //Test does not apply to Add Provider screen
    /*@Test
    public void testValidateCommunicationPurposeTypeCode() {
    }*/

    // OOP Provider Role Types
    @Test(dataProvider = "oopRoleTypes", dataProviderClass = InjectableData.class)
    public void testOOPProviderRoleTypes(ProviderRoleType roleType) {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

        page.fillIdentifier(roleType, null, null, "OOPID", "252526");
        page.fillStatus("AE", StatusCodeOption.CANCELLED, StatusReasonCodeOption.LAP);

        page.clickNext("Status", "");
        page.waitForAddProviderStep("Personal Information", true);

        page.fillPI("Dr.", "Testing", "Provider", null, "Smith");
        page.fillDemographics(List.of(2011,1,1), "U");

        page.clickNext("Personal Information", "");
        page.waitForAddProviderStep("Address", true);

        AddProviderAddressFragment address = page.fillAddress("P", "HC", List.of("123 Test St", "Unit 1", ""),
                "Victoria", "BC", "CA", "V9V9V9");

        page.clickNext("Address", "Address Invalid");
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);

        page.fillCredentials("BD", "Test", "5358", "TestInst",
                "Victoria", "CA", "BC", true, "2001");
        page.fillExpertise("ENG", "2500");
        ViewProviderPage viewPage = page.clickSubmitButton();

        // Verify Role Type matches
        String displayedRoleType = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0).get("Role Type");
        assertTrue(displayedRoleType != null && displayedRoleType.startsWith(roleType.getText().split(" ")[0]),
            "Displayed Role Type '" + displayedRoleType + "' does not match expected '" + roleType.getText() + "'");
    }

    // 07.Status codes for Out of Province Practitioner
    @Test
    public void testStatusCodesForOutOfProvincePractitioner() {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

        page.fillIdentifier(ProviderRoleType.OOPDEN, null, null, "OOPID", "252526");
        page.fillStatus("AE", StatusCodeOption.UNKNOWN, StatusReasonCodeOption.OOP);

        page.clickNext("Status", "");
        page.waitForAddProviderStep("Personal Information", true);

        page.fillPI("Dr.", "Testing", "Provider", null, "Smith");
        page.fillDemographics(List.of(2011,1,1), "U");

        page.clickNext("Personal Information", "");
        page.waitForAddProviderStep("Address", true);

        AddProviderAddressFragment address = page.fillAddress("P", "HC", List.of("123 Test St", "Unit 1", ""),
                "Victoria", "BC", "CA", "V9V9V9");

        page.clickNext("Address", "Address Invalid");
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);

        page.fillCredentials("BD", "Test", "5358", "TestInst",
                "Victoria", "CA", "BC", true, "2001");
        page.fillExpertise("ENG", "2500");
        ViewProviderPage viewPage = page.clickSubmitButton();

        // Verify Status Code and Reason Code in view screen
        String displayedStatusCode = viewPage.grabDataBlockContent(ProviderSection.STATUSES, 0).get("Type");
        String displayedReasonCode = viewPage.grabDataBlockContent(ProviderSection.STATUSES, 0).get("Reason");
        assertTrue(displayedStatusCode != null && displayedStatusCode.toUpperCase().contains("UNKNOWN"),
            "Displayed Status Code '" + displayedStatusCode + "' does not match expected 'UNKNOWN'");
        assertTrue(displayedReasonCode != null && displayedReasonCode.toUpperCase().contains("OOP"),
            "Displayed Reason Code '" + displayedReasonCode + "' does not match expected 'OOP'");
    }

    /**
     * Navigates to the Address screen on the Add Provider page using randomized template data.
     * Fills Status and Personal Information/Organization screens with generated values, then returns the page
     * so tests can fill the address fields themselves.
     *
     * @param providerType the type of provider to create
     * @return the AddProviderPage positioned at the Address screen
     */
    private AddProviderPage navigateToAddressScreen(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page;
        String identifierValue = dataGen.generateNumericId().substring(0, 8);

        if (providerType == ProviderType.ORGANIZATION) {
            // Organization flow
            page = workflow.getPlrWebAccessActions().openAddOrganization().openProviderPage(ProviderType.ORGANIZATION);

            page.fillOrganizationIdentifier(OrganizationalProviderRoleType.ORG, null, null, "ORGID", identifierValue);
            page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);

            page.clickNext("Status", "");
            page.waitForAddProviderStep("Organization", true);

            // Fill Organization screen with randomized data
            String orgName = orgDataGen.generateName();
            String orgDescription = orgDataGen.generateDescription();
            page.fillOrganizationName(orgName, orgDescription);

            page.clickNext("Organization", "");
            page.waitForAddProviderStep("Address", true);
        } else {
            // Practitioner flow (BC or OOP)
            page = workflow.getPlrWebAccessActions().openAddProvider();

            Object roleType;
            String identifierType;

            switch (providerType) {
                case OOP_PRACTITIONER:
                    page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);
                    roleType = ProviderRoleTypeOptions.OOPMD;
                    identifierType = "OOPID";
                    break;
                case BC_PRACTITIONER:
                default:
                    page = page.changeProviderType(ProviderType.BC_PRACTITIONER);
                    roleType = ProviderRoleTypeOptions.MD;
                    identifierType = "CPSID";
                    break;
            }

            // Fill Status screen with randomized data
            page.fillIdentifier(roleType, null, null, identifierType, identifierValue);
            page.fillStatus("AE", StatusCodeOption.CANCELLED, StatusReasonCodeOption.LAP);

            page.clickNext("Status", "");
            page.waitForAddProviderStep("Personal Information", true);

            // Fill Personal Information screen with randomized data
            String[] givenNames = dataGen.generateGivenNames();
            String familyName = dataGen.generateFamilyName();
            page.fillPI("Dr.", givenNames[0], givenNames[1], givenNames[2], familyName);

            // Parse birthdate and convert gender
            String birthDate = dataGen.generateBirthDate(); // YYYY-MM-DD
            String[] dateParts = birthDate.split("-");
            List<Integer> birthDateList = List.of(
                Integer.parseInt(dateParts[0]),
                Integer.parseInt(dateParts[1]),
                Integer.parseInt(dateParts[2])
            );
            String gender = dataGen.generateGender();
            String genderCode = gender.equals("male") ? "M" : gender.equals("female") ? "F" : "U";
            page.fillDemographics(birthDateList, genderCode);

            page.clickNext("Personal Information", "");
            page.waitForAddProviderStep("Address", true);
        }

        return page;
    }

    // Minimal helper to add a tiny pause for async UI updates
    private static void shortUiPause()
    {
        try { Thread.sleep(1000); } catch (InterruptedException ignored) { }
    }

}
