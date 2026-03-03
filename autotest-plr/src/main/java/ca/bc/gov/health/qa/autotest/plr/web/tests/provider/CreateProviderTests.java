package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.AddProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.*;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.*;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static ca.bc.gov.health.qa.autotest.plr.data.AddProviderConstants.*;
import static org.testng.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CreateProviderTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    public static JSONObject errorList;

    private CreateProviderTests() {
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
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) {
            workflow.login().openPlr();
        }
    }

    // Create Provider
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testCreateProvider(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        assertEquals(page.getStep(), "Identifier and Status",
                "Did not navigate to expected first step of add provider flow.");
        actions.checkBlockVisibility("Identifier");
        actions.checkBlockVisibility("Status");

        actions.skipToSection(page, providerType, "Personal Information", null, false);

        assertEquals(page.getStep(), "Name",
                "Did not navigate to expected step after completing Identifier and Status step");
        actions.checkBlockVisibility("Personal Information");
        actions.checkBlockVisibility("Demographic Details");

        page.fillPI("Jr.", "Test", "Second", "Third", "Provider");
        page.fillDemographics(List.of(1980, 6, 30), "U");
        page.clickNext("Demographic Details", "");
        page.waitForAddProviderStep("Address", true);

        assertEquals(page.getStep(), "Contact",
                "Did not navigate to expected step after completing Name step");
        actions.checkBlockVisibility("Address");
        actions.checkBlockVisibility("Phone Number");
        actions.checkBlockVisibility("Fax Number");
        actions.checkBlockVisibility("Email");

        AddProviderAddressFragment address = page.fillAddress("P", "MC",
                List.of("123 Test St", "Unit 1", "Lot 4"), "Victoria", "BC", "CA", "V9V9V9");
        page.fillPhone("250", "5551234", "123");
        page.fillFax("250", "5555678");
        page.fillEmail("test@example.com");
        page.clickNext("Address", "Address Invalid");
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);

        assertEquals(page.getStep(), "Credential and Expertise",
                "Did not navigate to expected step after completing Contact step");
        actions.checkBlockVisibility("Credential");
        actions.checkBlockVisibility("Expertise");

        ViewProviderPage viewPage = actions.finishCreateFlow(page, providerType, "Credentials");

        for (ProviderSection section : List.of(ProviderSection.ROLE_TYPE, ProviderSection.IDENTIFIERS,
                ProviderSection.STATUSES, ProviderSection.DEMOGRAPHICS,
                ProviderSection.ADDRESSES))
        {
            actions.checkMinimumData(section, viewPage, providerType);
        }

        // Names
        Map<String, String> nameBlock = viewPage.grabDataBlockContent(ProviderSection.PRACTITIONER_NAMES, 0);
        assertEquals(nameBlock.get("Prefix"), "Jr.",
                "Prefix did not save expected value.");
        assertEquals(nameBlock.get("First Name"), "Test",
                "First name did not save expected value.");
        assertEquals(nameBlock.get("Second Name"), "Second",
                "Second name did not save expected value.");
        assertEquals(nameBlock.get("Third Name"), "Third",
                "Third name did not save expected value.");
        assertEquals(nameBlock.get("Surname"), "Provider",
                "Surname did not save expected value.");
        assertEquals(nameBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                "Name 'Effective From' date did not default to current date.");

        // Addresses
        Map<String, String> addressBlock = viewPage.grabDataBlockContent(ProviderSection.ADDRESSES, 0);
        assertEquals(addressBlock.get("Address Line 2"), "Unit 1",
                "Address Line 2 did not save expected value.");
        assertEquals(addressBlock.get("Address Line 3"), "Lot 4",
                "Address Line 3 did not save expected value.");
        assertEquals(addressBlock.get("Postal/Zip Code"), "V9V 9V9",
                "Postal code did not save expected value.");
        assertEquals(addressBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                "Address 'Effective From' date did not default to current date.");

        // Telecommunications
        for (int i = 0; i < viewPage.grabDataBlockCount(ProviderSection.TELECOMMUNICATIONS); i++)
        {
            Map<String, String> telecomBlock = viewPage.grabDataBlockContent(ProviderSection.TELECOMMUNICATIONS, i);
            if (telecomBlock.get("Type").equals("Fax (FAX)"))
            {
                assertEquals(telecomBlock.get("Area Code"), "250",
                        "Fax area code did not save expected value.");
                assertEquals(telecomBlock.get("Number"), "5555678",
                        "Fax number did not save expected value.");
            }
            if (telecomBlock.get("Type").equals("Telephone (T)"))
            {
                assertEquals(telecomBlock.get("Area Code"), "250",
                        "Phone area code did not save expected value.");
                assertEquals(telecomBlock.get("Number"), "5551234",
                        "Phone number did not save expected value.");
                assertEquals(telecomBlock.get("Extension"), "123",
                        "Phone extension did not save expected value.");
            }
            assertEquals(telecomBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                    "Telecommunications 'Effective From' date did not default to current date.");
        }

        // Electronic Addresses
        Map<String, String> emailBlock = viewPage.grabDataBlockContent(ProviderSection.ELECTRONIC_ADDRESSES, 0);
        assertEquals(emailBlock.get("Address"), "test@example.com",
                "Email did not save expected value.");
        assertEquals(emailBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                "Electronic Address 'Effective From' date did not default to current date.");

        // Credentials
        Map<String, String> credentialBlock = viewPage.grabDataBlockContent(ProviderSection.CREDENTIALS, 0);
        assertEquals(credentialBlock.get("Credential Type"), "Bachelor Degree (BD)",
                "Credential type did not save expected value.");
        assertEquals(credentialBlock.get("Designation"), "Test",
                "Credential designation did not save expected value.");
        assertEquals(credentialBlock.get("Registration Number"), "5358",
                "Registration number did not save expected value.");
        assertEquals(credentialBlock.get("Granting Institution"), "TestInst",
                "Granting institution did not save expected value.");
        assertEquals(credentialBlock.get("Equivalency Flag"), "Yes",
                "Equivalency flag did not save expected value.");
        assertEquals(credentialBlock.get("Institution City"), "Victoria",
                "Institution city did not save expected value.");
        assertEquals(credentialBlock.get("Institution Country"), "CANADA (CA)",
                "Institution country did not save expected value.");
        assertEquals(credentialBlock.get("Institution Prov/State"), "British Columbia (BC)",
                "Institution province/state did not save expected value.");
        assertEquals(credentialBlock.get("Year Issued"), "2001",
                "Credential year issued did not save expected value.");
        assertEquals(credentialBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                "Credential 'Effective From' date did not default to current date.");

        // Expertise
        Map<String, String> expertiseBlock = viewPage.grabDataBlockContent(ProviderSection.EXPERTISE, 0);
        assertEquals(expertiseBlock.get("Type"), "English (ENG)",
                "Expertise type did not save expected value.");
        assertEquals(expertiseBlock.get("Source's Code"), "2500",
                "Expertise description did not save expected value.");
        assertEquals(expertiseBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                "Expertise 'Effective From' date did not default to current date.");
    }

    // Create Provider - Auto-linking of Provider Roles and Individual Provider
    @Test
    public void testAutolinkingProviderRoles()
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        String linkedRNID = UpdateSimpleHelper.generateNumericString(12);

        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        page.fillIdentifier(ProviderRoleType.RN, null, null, "RNID", linkedRNID);

        ViewProviderPage viewPage = actions.finishCreateFlow(page, ProviderType.BC_PRACTITIONER, "Status");

        String linkedCPN = "";
        for (int i = 0; i < viewPage.grabDataBlockCount(ProviderSection.IDENTIFIERS); i++)
        {
            Map<String,String> identifierBlock = viewPage.grabDataBlockContent(ProviderSection.IDENTIFIERS, i);
            if (!identifierBlock.get("Type").equals("Common Party Number (CPN)")) continue;
            linkedCPN = identifierBlock.get("Identifier");
            break;
        }
        if (linkedCPN.isEmpty())
            fail("Expected linked CPN identifier not found for provider role with RNID '" + linkedRNID + "'.");

        page = workflow.getPlrWebAccessActions().openAddProvider();
        page.fillIdentifier(ProviderRoleType.RNP, null, null, "RNID", linkedRNID);

        viewPage = actions.finishCreateFlow(page, ProviderType.BC_PRACTITIONER, "Status");

        for (int i = 0; i < viewPage.grabDataBlockCount(ProviderSection.IDENTIFIERS); i++)
        {
            Map<String,String> identifierBlock = viewPage.grabDataBlockContent(ProviderSection.IDENTIFIERS, i);
            if (!identifierBlock.get("Type").equals("Common Party Number (CPN)")) continue;
            assertEquals(identifierBlock.get("Identifier"), linkedCPN,
                    "Expected linked CPN identifier value '" + linkedCPN +
                            "' not found for provider role with RNID '" + linkedRNID + "'.");
            break;
        }
    }

    // Create Provider - Code Validation Restriction - Credential
    @Test(dataProvider = "practitionerRoleTypes", dataProviderClass = InjectableData.class)
    public void testCodeRestrictionCredential(ProviderType providerType, ProviderRoleType roleType)
    {
        final List<String> expectedCredentialList = Stream.concat(CREDENTIAL_BASE_OPTIONS.stream(),
                        CREDENTIAL_OPTIONS_MAP.getOrDefault(roleType, List.of()).stream()).toList();

        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        page.fillIdentifier(roleType, null, null, IDENTIFIER_TYPE_OPTIONS_MAP.getOrDefault(roleType,
                List.of("OOPID - Out of Province Provider")).getFirst(), "1");

        page = actions.skipToSection(page, providerType, "Credential", roleType, true);

        AddProviderCredentialFragment credential = page.fillCredentials(null, null, null, null,
                null, null, null, true, null);
        List<String> credentialTypeOptions = credential.getCredentialTypeOptions();

        for (String item : credentialTypeOptions)
        {
            if (item.equals("Select One")) continue;

            assertTrue(expectedCredentialList.contains(item),
                    "Expected credential option '" + item + "' not found for provider role '" + roleType.getText() + "'.");
        }

        assertEquals(credentialTypeOptions.size() - 1, expectedCredentialList.size(),
                "Unexpected number of expertise options found for provider role '" + roleType.getText());
    }

    // Create Provider - Code Validation Restriction - Expertise
    @Test(dataProvider = "practitionerRoleTypes", dataProviderClass = InjectableData.class)
    public void testCodeRestrictionExpertise(ProviderType providerType, ProviderRoleType roleType)
    {
        final List<String> expectedExpertiseList = Stream.concat(EXPERTISE_LANG_OPTIONS.stream(),
                        EXPERTISE_OPTIONS_MAP.getOrDefault(roleType, List.of()).stream()).toList();

        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        page.fillIdentifier(roleType, null, null, IDENTIFIER_TYPE_OPTIONS_MAP.getOrDefault(roleType,
                            List.of("OOPID - Out of Province Provider")).getFirst(), "1");

        page = actions.skipToSection(page, providerType, "Expertise", roleType, true);

        AddProviderExpertiseFragment expertise = page.fillExpertise(null, null);
        List<String> expertiseTypeOptions = expertise.getExpertiseOptions();

        for (String item : expertiseTypeOptions)
        {
            assertTrue(expectedExpertiseList.contains(item),
                    "Expected expertise option '" + item + "' not found for provider role '" + roleType.getText() + "'.");
        }

        assertEquals(expertiseTypeOptions.size(), expectedExpertiseList.size(),
                "Unexpected number of expertise options found for provider role '" + roleType.getText());
    }

    // Create Provider - Code Validation Restriction - Identifier
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testCodeRestrictionIdentifier(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        AddProviderIdFragment id = page.fillIdentifier(null, null, null, null, null);
        List<String> roleOptions = id.getProviderRoleTypeOptions();
        for (ProviderRoleType roleType : ProviderRoleType.values())
        {
            if (!roleOptions.contains(roleType.getText())) continue;

            page.fillIdentifier(roleType, null, null, null, null);
            List<String> identifierTypeOptions = id.getIdentifierTypeOptions();

            IDENTIFIER_TYPE_OPTIONS_MAP.getOrDefault(roleType, List.of("OOPID - Out of Province Provider"))
                    .forEach(option -> assertTrue(identifierTypeOptions.contains(option),
                            "Expected identifier type option '" + option
                                    + "' not found for provider role '" + roleType.getText() + "'"));
        }
    }

    // Create Provider - Code Validation Restriction - Status Code
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testCodeRestrictionStatusCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

        AddProviderStatusFragment status;
        for (StatusCodeOption statusCode : StatusCodeOption.values())
        {
            status = page.fillStatus(null, statusCode, null);

            List<String> reasonCodeOptions = status.getStatusReasonCodeOptions();

            STATUS_REASON_CODE_OPTIONS_MAP.get(statusCode).forEach(option ->
                    assertTrue(reasonCodeOptions.contains(option.getText()), "Expected reason code option '"
                            + option.getText() + "' not found for status code '" + statusCode.getText() + "'"));
        }
    }

    // Create Provider - Individual Provider Minimum Data
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testIndProviderMinimumData(ProviderType providerType)
    {
        final List<ProviderSection> sectionsToTest = List.of(
                ProviderSection.ROLE_TYPE,
                ProviderSection.IDENTIFIERS,
                ProviderSection.STATUSES,
                ProviderSection.PRACTITIONER_NAMES,
                ProviderSection.DEMOGRAPHICS,
                ProviderSection.ADDRESSES
        );

        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        String identifierType = "OPTID";
        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) {
            page = page.changeProviderType(providerType);
            identifierType = "OOPID";
        }

        page.fillIdentifier(null, null, null, identifierType, "1");
        page.fillStatus(null, null, null);
        page.clickNext("Status", "");
        page.waitForAddProviderStep("Personal Information", true);

        page.fillPI(null, "Minimum", null, null, "Data");
        page.fillDemographics(List.of(1980, 6, 30), "U");
        page.clickNext("Demographic Details", "");
        page.waitForAddProviderStep("Address", true);

        AddProviderAddressFragment address = page.fillAddress("P", "MC",
                List.of("123 Test St", "", ""), "Victoria", null, null, null);
        page.clickNext("Address", "Address Invalid");
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);

        ViewProviderPage viewPage = page.clickSubmitButton();

        for (ProviderSection section : sectionsToTest)
            actions.checkMinimumData(section, viewPage, providerType);
    }

    // Create Provider - Validate Date of Birth
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateDOB(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        actions.skipToSection(page, providerType, "Demographic Details", null, false);

        AddProviderDemographicFragment demo = page.fillDemographics("", "U");
        page.clickNext("Demographic Details", null);

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingDOB"),
                "Expected error message for missing date of birth not found.");

        demo.getDateOfBirthMenu().typeDateRaw("06-30-1980");
        page.clickNext("Demographic Details", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("invalidDateFormatDOB"),
                "Expected error message for invalid date of birth format not found.");

        demo.getDateOfBirthMenu().typeDateRaw("1799-12-31");
        page.clickNext("Demographic Details", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("outOfBoundsDOB"),
                "Expected error message for date of birth too far in the past not found.");

        demo.getDateOfBirthMenu().typeDateRaw("2099-01-01");
        page.clickNext("Demographic Details", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("outOfBoundsDOB"),
                "Expected error message for date of birth in the future not found.");

        demo = page.fillDemographics(List.of(2020,1,1), null);

        assertEquals(demo.getDateOfBirth(), "2020-01-01",
                "Date of Birth did not save the expected value.");

        page.clickNext("Demographic Details", "");
        page.waitForAddProviderStep("Address", true);

        assertEquals(page.getStep(), "Contact",
                "Did not navigate to the expected next step after entering valid date of birth.");
    }

    // Create Provider - Validate Gender Code
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateGenderCode(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

        actions.skipToSection(page, providerType,"Demographic Details", null, false);

        AddProviderDemographicFragment demo = page.fillDemographics(List.of(2020, 6, 30), null);

        page.clickNext("Demographic Details", null);

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingGender"),
                "Expected error message for missing gender not found.");

        List<String> genderOptions = demo.getGenderMenu().grabRadioOptions();
        assertEquals(genderOptions, List.of("U - Unknown", "F - Female", "M - Male"),
                "Expected gender options not found or in unexpected order.");

        page.fillDemographics("", "U");
        page.clickNext("Demographic Details", "");
        page.waitForAddProviderStep("Address", true);

        assertEquals(page.getStep(), "Contact",
                "Did not navigate to the expected next step after entering valid gender.");
    }

    // Create Provider - Validate Individual Name
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateIndividualName(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        page = actions.skipToSection(page, providerType, "Personal Information", null, false);

        page.fillDemographics(List.of(1980,6,30), "U");

        // Prefix

        page.fillPI(UpdateSimpleHelper.generateAlphabetString(11), "Test", null, null, "Provider");
        page.clickNext("Personal Information", null);

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("prefixTooLong"),
                "Expected error message for exceeding max length of prefix not found.");

        // First Name

        page.fillPI("", "", null, null, "Provider");
        page.clickNext("Personal Information", null);


        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingFirstName"),
                "Expected error message for missing first name not found.");

        page.fillPI(null, "", "Second", "Third", "Provider");
        page.clickNext("Personal Information", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingFirstName"),
                "Expected error message for missing first name not found.");

        page.fillPI(null, UpdateSimpleHelper.generateAlphabetString(51), "", "", "Provider");
        page.clickNext("Personal Information", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("firstNameTooLong"),
                "Expected error message for exceeding max length of first name not found.");

        page.fillPI(null, " ", null, null, "Provider");
        page.clickNext("Personal Information", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("firstNameOnlySpaces"),
                "Expected error message for first name with only spaces not found.");

        page.fillPI(null, "Jr., Test", null, null, "Provider");
        page.clickNext("Personal Information", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("firstNameInvalidCharacters"),
                "Expected error message for invalid character in first name not found.");

        // Second Name

        page.fillPI(null, "Test", UpdateSimpleHelper.generateAlphabetString(51), null, "Provider");
        page.clickNext("Personal Information", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("secondNameTooLong"),
                "Expected error message for exceeding max length of second name not found.");

        // Third Name

        page.fillPI(null, "Test", "", UpdateSimpleHelper.generateAlphabetString(51), "Provider");
        page.clickNext("Personal Information", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("thirdNameTooLong"),
                "Expected error message for exceeding max length of third name not found.");

        // Surname

        page.fillPI(null, "Test", null, "", "");
        page.clickNext("Personal Information", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingSurname"),
                "Expected error message for missing surname not found.");

        page.fillPI(null, "Test", null, null, UpdateSimpleHelper.generateAlphabetString(51));
        page.clickNext("Personal Information", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("surnameTooLong"),
                "Expected error message for exceeding max length of surname not found.");

        page.fillPI(null, "Test", null, null, "Jr., Provider");
        page.clickNext("Personal Information", null);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("firstNameInvalidCharacters"),
                "Expected error message for invalid character in surname not found.");

        // Positive Test (Suffixes, spaces between first name/surname, maximum character limit)

        String expectedPrefix = UpdateSimpleHelper.generateAlphabetString(10);
        String expectedFirstName = UpdateSimpleHelper.generateAlphabetString(46) + " Sr.";
        expectedFirstName = expectedFirstName.substring(0,1).toUpperCase() + expectedFirstName.substring(1);
        String expectedSecondName = UpdateSimpleHelper.generateAlphabetString(50);
        String expectedThirdName = UpdateSimpleHelper.generateAlphabetString(50);
        String expectedSurname = UpdateSimpleHelper.generateAlphabetString(46) + " Jr.";
        page.fillPI(expectedPrefix, expectedFirstName, expectedSecondName, expectedThirdName, expectedSurname);
        page.clickNext("Personal Information", "");

        ViewProviderPage viewPage = actions.finishCreateFlow(page, providerType, "Address");

        Map<String, String> nameBlock = viewPage.grabDataBlockContent(ProviderSection.PRACTITIONER_NAMES, 0);

        assertEquals(nameBlock.get("Prefix"), expectedPrefix, "Prefix did not save correctly.");
        assertEquals(nameBlock.get("First Name"), expectedFirstName, "First name did not save correctly.");
        assertEquals(nameBlock.get("Second Name"), expectedSecondName, "Second name did not save correctly.");
        assertEquals(nameBlock.get("Third Name"), expectedThirdName, "Third name did not save correctly.");
        assertEquals(nameBlock.get("Surname"), expectedSurname, "Surname did not save correctly.");
    }

    // Create Provider - Validate Provider Expertise Original Source
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateProviderExpertiseOriginalSource(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        actions.skipToSection(page, providerType, "Expertise", null, true);

        AddProviderExpertiseFragment expertise = page.fillExpertise("ENG", UpdateSimpleHelper.generateNumericString(51));
        page.clickSubmitButton();

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("expertiseSourceTooLong"),
                "Expected error message for exceeding max length of expertise original source not found.");

        expertise.fillSourceCode(UpdateSimpleHelper.generateNumericString(50));
        ViewProviderPage viewPage = page.clickSubmitButton();

        Map<String, String> expertiseBlock = viewPage.grabDataBlockContent(ProviderSection.EXPERTISE, 0);
        assertEquals(expertiseBlock.get("Source's Code").length(), 50,
                "Expertise original source code of maximum length is not fully displayed.");

        page = workflow.getPlrWebAccessActions().openAddProvider();
        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        actions.skipToSection(page, providerType, "Expertise", null, true);

        page.fillExpertise("ENG", "");
        viewPage = page.clickSubmitButton();

        expertiseBlock = viewPage.grabDataBlockContent(ProviderSection.EXPERTISE, 0);
        assertEquals(expertiseBlock.get("Source's Code"), "",
                "Expertise original source code did not save empty value correctly.");

        page = workflow.getPlrWebAccessActions().openAddProvider();
        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        actions.skipToSection(page, providerType, "Credential", null, true);
        viewPage = page.clickSubmitButton();

        assertEquals(viewPage.grabDataBlockCount(ProviderSection.EXPERTISE), 0,
                "Expertise block is displayed when no expertise information is entered.");
    }

    // Create Provider - Validate Provider Identifiers
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateProviderIdentifiers(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        page = page.changeProviderType(providerType);

        for (String testIdentifier : List.of("tæst", "te$t", "", " "))
        {
            switch (providerType) {
                case OOP_PRACTITIONER ->
                        page.fillIdentifier(ProviderRoleType.OOPRECT, null, null, "OOPID", testIdentifier);
                case BC_PRACTITIONER ->
                        page.fillIdentifier(ProviderRoleType.OPT, null, null, "OPTID", testIdentifier);
                case ORGANIZATION ->
                        page.fillIdentifier(OrganizationalProviderRoleType.BUSINESS, null, null, "ORGID", testIdentifier);
            }

            page.fillStatus(null, null, null);
            page.clickNext("Identifier", null);

            List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
            if (testIdentifier.isEmpty())
            {
                String emptyError = "missingProviderIdentifier";
                if (providerType.equals(ProviderType.BC_PRACTITIONER)) emptyError = "missingBCProviderIdentifier";
                assertEquals(errorMessageList.getFirst(), errorList.get(emptyError),
                        "Expected error message for missing identifier not found.");
                continue;
            }
            assertEquals(errorMessageList.getFirst(), errorList.get("foreignCharacterIdentifier"),
                    "Expected error message for invalid identifier not found.");
        }
    }

    // Create Provider - Validate Name Preferred Flag
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateNamePreferredFlag(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (!providerType.equals(ProviderType.BC_PRACTITIONER)) page = page.changeProviderType(providerType);

        ViewProviderPage viewPage = actions.finishCreateFlow(page, providerType, "Identifier");

        ProviderSection correctSection = providerType.equals(ProviderType.ORGANIZATION) ?
                ProviderSection.ORGANIZATION_NAMES : ProviderSection.PRACTITIONER_NAMES;
        Map<String,String> nameContent = viewPage.grabDataBlockContent(correctSection, 0);
        assertEquals(nameContent.get("Preferred"), "No",
                "Expected preferred flag to default to 'No'.");

    }

    // Create Provider - Validate Name Type Code
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateNameTypeCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        ViewProviderPage viewPage = actions.finishCreateFlow(page, providerType, "Identifier");

        Map<String,String> nameContent = viewPage.grabDataBlockContent(ProviderSection.PRACTITIONER_NAMES, 0);
        assertEquals(nameContent.get("Name Type"), "Current Known Name (CURR)",
                "Expected name type code to default to 'CURR'.");
    }

    // Create Provider - Validate Provider Credential
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateProviderCredential(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        actions.skipToSection(page, providerType, "Credential", null, true);

        AddProviderCredentialFragment cred = page.fillCredentials("BD ", null,
                null, null, null, null, null, true, null);
        page.clickSubmitButton();

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingDesignation"),
                "Expected error message for missing credential designation not found.");

        cred.selectCredentialType("Select One");
        cred.fillDesignation("Test");
        page.clickSubmitButton();

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingCredentialType"),
                "Expected error message for missing credential type not found.");

        cred.selectCredentialType("BD ");
        cred.fillDesignation(UpdateSimpleHelper.generateAlphabetString(241));
        page.clickSubmitButton();

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("designationTooLong"),
                "Expected error message for exceeding max length of credential designation not found.");

        cred.fillDesignation("Test");
        cred.fillRegistrationNumber(UpdateSimpleHelper.generateNumericString(241));
        page.clickSubmitButton();

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("registrationNumberTooLong"),
                "Expected error message for exceeding max length of credential registration number not found.");

        cred.fillRegistrationNumber("500");
        cred.fillInstitution(UpdateSimpleHelper.generateAlphabetString(241));
        page.clickSubmitButton();

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("institutionTooLong"),
                "Expected error message for exceeding max length of credential granting institution not found.");

        cred.fillInstitution("Test");
        cred.fillYear(UpdateSimpleHelper.generateAlphabetString(5));
        page.clickSubmitButton();

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("yearIssuedTooLong"),
                "Expected error message for exceeding max length of credential year issued not found.");

        cred.fillYear("2000");
        cred.fillCity(UpdateSimpleHelper.generateAlphabetString(241));
        page.clickSubmitButton();

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("credentialCityTooLong"),
                "Expected error message for exceeding max length of credential city not found.");
    }

    // Create Provider - Validate Provider Credential Granting Institution Name
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateProviderCredentialInstitution(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        actions.skipToSection(page, providerType, "Credential", null, true);

        AddProviderCredentialFragment cred = page.fillCredentials("BD ", "Test",
                null, UpdateSimpleHelper.generateAlphabetString(241), null, null,
                null, false, null);
        page.clickSubmitButton();

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("institutionTooLong"),
                "Expected error message for exceeding max length of credential granting institution not found.");

        String expectedInstitution = UpdateSimpleHelper.generateAlphabetString(240);
        cred.fillInstitution(expectedInstitution);
        ViewProviderPage viewPage = page.clickSubmitButton();

        assertEquals(viewPage.grabDataBlockContent(ProviderSection.CREDENTIALS,0).get("Granting Institution"),
                expectedInstitution, "Credential granting institution did not save the expected value.");
    }

    // Create Provider - Validate Provider Credential Registration Number
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateProviderCredentialRegistrationNumber(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        actions.skipToSection(page, providerType, "Credential", null, true);

        AddProviderCredentialFragment cred = page.fillCredentials("BD ", "Test",
                UpdateSimpleHelper.generateAlphabetNumericString(241), null, null, null,
                null, false, null);
        page.clickSubmitButton();

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("registrationNumberTooLong"),
                "Expected error message for exceeding max length of credential registration number not found.");

        String expectedRegistrationNumber = UpdateSimpleHelper.generateAlphabetNumericString(240);
        cred.fillRegistrationNumber(expectedRegistrationNumber);
        ViewProviderPage viewPage = page.clickSubmitButton();

        assertEquals(viewPage.grabDataBlockContent(ProviderSection.CREDENTIALS,0).get("Registration Number"),
                expectedRegistrationNumber, "Credential registration number did not save the expected value.");
    }

    // Create Provider - Validate Provider Credential Type Code
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateProviderCredentialTypeCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(providerType);

        actions.skipToSection(page, providerType, "Credential", null, true);

        page.fillCredentials(null, "Test", "1234", "Test Institution",
                "Victoria", null, null, false, "2000");
        page.clickSubmitButton();

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingCredentialType"),
                "Expected error message for missing credential type not found.");
    }

    // Create Provider - Validate Provider Role Type
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateProviderRoleType(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        page = page.changeProviderType(providerType);

        page.fillIdentifier("Select One", null, null, null, null, null);

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingProviderRoleType"),
                "Expected error message for missing provider role type not found.");
    }

    // Create Provider - Validate Status Class Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateStatusClassCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        actions.validateStatusField("Class", providerType, page);
    }

    // Create Provider - Validate Status Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateStatusTypeCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        actions.validateStatusField("Type", providerType, page);
    }

    // Create Provider - Validate Status Reason Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateStatusReasonCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        actions.validateStatusField("Reason", providerType, page);
    }

    // Create Provider - Validate Year of Credential Issue
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateYearOfCredentialIssue(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (!providerType.equals(ProviderType.BC_PRACTITIONER)) page = page.changeProviderType(providerType);

        actions.skipToSection(page, providerType, "Credential", null, true);

        page.fillCredentials("BD ", "Test", null, null, null,
                null, null, false,
                "1" + UpdateSimpleHelper.generateNumericString(4));
        page.clickSubmitButton();

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("yearIssuedTooLong"),
                "Expected error message for exceeding max length of year of credential issue not found.");
    }
}
