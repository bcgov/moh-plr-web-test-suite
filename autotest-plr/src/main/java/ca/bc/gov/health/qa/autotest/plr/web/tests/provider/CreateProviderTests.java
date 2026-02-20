package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.AddProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderDemographicFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderIdFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderStatusFragment;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.*;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static ca.bc.gov.health.qa.autotest.plr.data.AddProviderConstants.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CreateProviderTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList, warningList;

    private CreateProviderTests() {
        try
        {
            errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
            warningList = new JSONObject(Files.readString(errorPath)).getJSONObject("warnings");
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read JSON data (%s).", errorPath);
            throw new IllegalStateException(msg, e);
        }
    }

    @BeforeMethod
    public void before(Object[] parameters) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) {
            workflow.login().openPlr();
        }
    }

    // Create Provider - Code Validation Restriction - Identifier
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testCodeRestrictionIdentifier(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

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

    // Create Provider - Validate Date of Birth
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateDOB(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

        switch (providerType) {
            case OOP_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OOPRECT, null, null, "OOPID", "1");
            case BC_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OPT, null, null, "OPTID", "1");
        }

        page.fillStatus(null, null, null);
        page.clickNext("Identifier", "");
        page.waitForAddProviderStep("Personal Information", true);
        page.fillPI(null, "Test", null, null, "Provider");

        AddProviderDemographicFragment demo = page.fillDemographics(null, "U");
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

    // Create Provider - Validate Individual Name
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testValidateIndividualName(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();
        AddProviderActions actions = workflow.getAddProviderActions();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

        switch (providerType) {
            case OOP_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OOPRECT, null, null, "OOPID", "1");
            case BC_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OPT, null, null, "OPTID", "1");
        }

        page.fillStatus(null, null, null);
        page.clickNext("Identifier", "");
        page.waitForAddProviderStep("Personal Information", true);
        page.fillDemographics(List.of(2020,6,30), "U");

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

        page = page.changeProviderType(providerType);

        switch (providerType) {
            case OOP_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OOPRECT, null, null, "OOPID", "1");
            case BC_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OPT, null, null, "OPTID", "1");
            case ORGANIZATION ->
                    page.fillIdentifier(OrganizationalProviderRoleType.BUSINESS, null, null, "ORGID", "1");
        }

        page.fillStatus("Select One", null, null);

        page.clickNext("Status", null);

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingStatusClassCode"),
                "Expected error message for missing status class code not found.");
    }

    // Create Provider - Validate Status Type Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateStatusTypeCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        page = page.changeProviderType(providerType);

        switch (providerType) {
            case OOP_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OOPRECT, null, null, "OOPID", "1");
            case BC_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OPT, null, null, "OPTID", "1");
            case ORGANIZATION ->
                    page.fillIdentifier(OrganizationalProviderRoleType.BUSINESS, null, null, "ORGID", "1");
        }

        AddProviderStatusFragment status = page.fillStatus(null, null, null);
        status.selectStatusCode("Select One");

        page.clickNext("Status", null);

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingStatusTypeCode"),
                "Expected error message for missing status type code not found.");
    }

    // Create Provider - Validate Status Reason Code
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testValidateStatusReasonCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        page = page.changeProviderType(providerType);

        switch (providerType) {
            case OOP_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OOPRECT, null, null, "OOPID", "1");
            case BC_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OPT, null, null, "OPTID", "1");
            case ORGANIZATION ->
                    page.fillIdentifier(OrganizationalProviderRoleType.BUSINESS, null, null, "ORGID", "1");
        }

        AddProviderStatusFragment status = page.fillStatus(null, null, null);
        status.selectStatusReasonCode("Select One");

        page.clickNext("Status", null);

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get("missingStatusReasonCode"),
                "Expected error message for missing status type code not found.");
    }
}
