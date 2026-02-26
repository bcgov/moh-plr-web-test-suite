package ca.bc.gov.health.qa.autotest.plr.web.actions.provider;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.add.AddFacilityAddressFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderAddressFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderStatusFragment;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.OrganizationalProviderRoleType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderRoleType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.StatusCodeOption;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.StatusReasonCodeOption;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.Map;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.provider.CreateProviderTests.errorList;
import static org.testng.Assert.*;

/**
 * Actions class for the Add Provider page/functions
 */
public class AddProviderActions {
    private final SeleniumSession selenium_;

    /**
     * Initializes class and SeleniumSession.
     * @param selenium The current SeleniumSession
     */
    public AddProviderActions(SeleniumSession selenium) { selenium_ = selenium; }

    /**
     * Checks that a block with the specified name is visible on the page.
     * Used to verify that expected sections of the add provider form are displayed after navigating to them.
     * @param blockName the name of the block/section to check for visibility (e.g. "Personal Information", "Address", etc.)
     */
    public void checkBlockVisibility(String blockName)
    {
        By blockLocator = By.xpath(String.format(
                "//table//tbody//tr//td//div//div//span[text() = '%s']", blockName));

        assertTrue(selenium_.grabElementVisible(blockLocator),
                String.format("Expected block '%s' to be visible", blockName));
    }

    /**
     * Navigates through the add provider flow up to the specified section
     * Used to reach specific sections of the flow for testing without having to fill in all previous sections.
     * @param page the AddProviderPage object to perform actions on (expects to be on the first page of the add provider flow)
     * @param providerType the type of provider being added, which determines some of the fields that are filled in the flow (e.g. identifier type in the identifier step)
     * @param section the section of the add provider flow to navigate to (e.g. "Demographics", "Address", etc.)
     * @return the AddProviderPage object after navigating to the specified section, which can be used for further actions in that section
     */
    public AddProviderPage skipToSection(AddProviderPage page, ProviderType providerType, String section)
    {
        switch (providerType) {
            case OOP_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OOPRECT, null, null, "OOPID", "1");
            case BC_PRACTITIONER ->
                    page.fillIdentifier(ProviderRoleType.OPT, null, null, "OPTID", "1");
            case ORGANIZATION ->
                    page.fillIdentifier(OrganizationalProviderRoleType.BUSINESS, null, null, "ORGID", "1");
        }
        if (section.equals("Status")) { return page; }

        page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);
        page.clickNext("Status", "");
        page.waitForAddProviderStep("Personal Information", true);
        if (section.equals("Personal Information")) { return page; }

        page.fillPI(null, "Test", null, null, "Provider");
        if (section.equals("Demographic Details")) { return page; }

        page.fillDemographics(List.of(2020, 1, 1), "U");
        page.clickNext("Demographic Details", "");
        page.waitForAddProviderStep("Address", true);
        if (section.equals("Address")) { return page; }

        AddProviderAddressFragment address = page.fillAddress("P", "HC",
                List.of("123 Test St", "Unit 1", ""), "Victoria", "BC", "CA", "V9V9V9");
        if (section.equals("Phone Number")) { return page; }

        page.fillPhone("250", "5551234", "123");
        if (section.equals("Fax Number")) { return page; }

        page.fillFax("250", "5555678");
        if (section.equals("Email")) { return page; }

        page.fillEmail("test@example.com");
        page.clickNext("Address", "Address Invalid");
        address.handleWidgetButton("Address Invalid");
        page.waitForAddProviderStep("Credential", true);
        if (section.equals("Credential")) { return page; }

        page.fillCredentials("BD", "Test", "5358", "TestInst",
                "Victoria", "CA", "BC", true, "2001");

        return page;
    }

    /**
     * Completes the add provider flow up to the point of submission, filling in all fields with test data. Navigates through
     * the flow based on the current fragment and provider type, so can be used to complete partial flows as well.
     * @param page the AddProviderPage object to perform actions on
     * @param providerType the type of provider being added, which determines some of the fields that are filled in the flow
     * @param currentFragment the current fragment of the add provider flow, which determines the starting point of the flow
     *                        (e.g. if the current fragment is "Address", the method will start filling in fields from the address step)
     * @return the ViewProviderPage object that is reached after submitting the add provider form
     */
    public ViewProviderPage finishCreateFlow(AddProviderPage page, ProviderType providerType, String currentFragment)
    {
        switch (currentFragment) {
            case "Identifier":
                switch (providerType) {
                    case OOP_PRACTITIONER -> page.fillIdentifier(ProviderRoleType.OOPRECT, null, null, "OOPID", "1");
                    case BC_PRACTITIONER -> page.fillIdentifier(ProviderRoleType.OPT, null, null, "OPTID", "1");
                    case ORGANIZATION ->
                            page.fillIdentifier(OrganizationalProviderRoleType.BUSINESS, null, null, "ORGID", "1");
                }
            case "Status":
                page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);
                page.clickNext("Status", "");
                if (providerType.equals(ProviderType.ORGANIZATION)) page.waitForAddProviderStep("Organization", true);
                else page.waitForAddProviderStep("Personal Information", true);
            case "Personal Information":
                if (!providerType.equals(ProviderType.ORGANIZATION))
                    page.fillPI(null, "Test", null, null, "Provider");
            case "Demographics":
                if (!providerType.equals(ProviderType.ORGANIZATION)) {
                    page.fillDemographics(List.of(2020, 1, 1), "U");
                    page.clickNext("Demographic Details", "");
                }
            case "Organization":
                if (providerType.equals(ProviderType.ORGANIZATION)) {
                    page.fillOrganizationName("Test Organization", "Test Description");
                    page.clickNext("Organization", "");
                }
                page.waitForAddProviderStep("Address", true);
            case "Address":
                AddProviderAddressFragment address = page.fillAddress("P", "HC",
                        List.of("123 Test St", "Unit 1", ""), "Victoria", "BC", "CA", "V9V9V9");
                page.fillPhone("250", "5551234", "123");
                page.fillFax("250", "5555678");
                page.fillEmail("test@example.com");
                page.clickNext("Address", "Address Invalid");
                address.handleWidgetButton("Address Invalid");
                page.waitForAddProviderStep("Credential", true);
            case "Credentials":
                page.fillCredentials("BD", "Test", "5358", "TestInst",
                        "Victoria", "CA", "BC", true, "2001");
            case "Expertise":
                page.fillExpertise("ENG", "2500");
        }
        return page.clickSubmitButton();
    }

    /**
     * Checks that the minimum required data for a provider is saved and displayed correctly
     * in the provider view page after submission of the add provider form.
     * @param section the section of the provider view page to check (e.g. "Role Type", "Identifiers", etc.)
     * @param viewPage the ViewProviderPage object representing the provider view page to check the data on
     * @param providerType the type of provider that was added
     */
    public void checkMinimumData(ProviderSection section, ViewProviderPage viewPage, ProviderType providerType)
    {
        switch (section)
        {
            case ROLE_TYPE -> {
                // Role Type
                Map<String,String> roleBlock = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
                if (providerType.equals(ProviderType.OOP_PRACTITIONER))
                {
                    assertEquals(roleBlock.get("Role Type"),
                            "OOP-RECT (OOP Recreation Therapist)",
                            "Provider role type did not save expected value.");
                } else {
                    assertEquals(roleBlock.get("Role Type"), "OPT (Optometrist)",
                            "Role Type did not save expected value.");
                }
            }
            case IDENTIFIERS -> {
                String expectedIdentifierType = "";
                if (providerType.equals(ProviderType.OOP_PRACTITIONER)) expectedIdentifierType = "Out of Province Provider (OOPID)";
                else if (providerType.equals(ProviderType.BC_PRACTITIONER)) expectedIdentifierType = "Optometrist ID Number (OPTID)";

                boolean foundIdentifier = false;
                for (int i = 0; i < viewPage.grabDataBlockCount(ProviderSection.IDENTIFIERS); i++)
                {
                    Map<String, String> identifierBlock = viewPage.grabDataBlockContent(ProviderSection.IDENTIFIERS, i);
                    if (!identifierBlock.get("Type").equals(expectedIdentifierType)) continue;

                    assertEquals(identifierBlock.get("Identifier"), "1",
                            "Identifier value did not save expected value.");
                    assertEquals(identifierBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                            "Identifier 'Effective From' date did not default to current date.");
                    foundIdentifier = true;
                }
                if (!foundIdentifier)
                {
                    fail("Expected identifier type '" + expectedIdentifierType + "' not found in provider view page.");
                }
            }
            case STATUSES -> {
                Map<String,String> statusBlock = viewPage.grabDataBlockContent(ProviderSection.STATUSES, 0);
                assertEquals(statusBlock.get("Type"), "Active (ACTIVE)",
                        "Status type did not save expected value.");
                assertEquals(statusBlock.get("Class"), "Licensure (LIC)",
                        "Status class code did not save expected value.");
                assertEquals(statusBlock.get("Reason"), "Good Standing (GS)",
                        "Status reason code did not save expected value.");
                assertEquals(statusBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                        "Status 'Effective From' date did not default to current date.");
            }
            case PRACTITIONER_NAMES -> {
                Map<String, String> nameBlock = viewPage.grabDataBlockContent(ProviderSection.PRACTITIONER_NAMES, 0);
                assertEquals(nameBlock.get("Name Type"), "Current Known Name (CURR)",
                        "Name Type did not save expected default value.");
                assertEquals(nameBlock.get("First Name"), "Minimum",
                        "First name did not save expected value.");
                assertEquals(nameBlock.get("Surname"), "Data",
                        "Surname did not save expected value.");
                assertEquals(nameBlock.get("Preferred"), "No",
                        "Preferred Flag did not save expected default value.");
                assertEquals(nameBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                        "Name 'Effective From' date did not default to current date.");
            }
            case DEMOGRAPHICS -> {
                Map<String, String> demoBlock = viewPage.grabDataBlockContent(ProviderSection.DEMOGRAPHICS, 0);
                assertEquals(demoBlock.get("Birth Date"), "1980-06-30",
                        "Date of Birth did not save expected value.");
                assertEquals(demoBlock.get("Gender"), "Unknown (U)",
                        "Gender did not save expected value.");
                assertEquals(demoBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                        "Demographics 'Effective From' date did not default to current date.");
            }
            case ADDRESSES -> {
                Map<String, String> addressBlock = viewPage.grabDataBlockContent(ProviderSection.ADDRESSES, 0);
                assertEquals(addressBlock.get("Address Type"), "Physical location (P)",
                        "Address type did not save expected value.");
                assertEquals(addressBlock.get("Address Purpose"), "Ministry Contact (MC)",
                        "Address purpose did not save expected value.");
                assertEquals(addressBlock.get("Address Line 1"), "123 Test St",
                        "Address Line 1 did not save expected value.");
                assertEquals(addressBlock.get("Country"), "CANADA (CA)",
                        "Country did not save expected value.");
                assertEquals(addressBlock.get("State/Prov"), "BC",
                        "Province/State did not save expected value.");
                assertEquals(addressBlock.get("City"), "Victoria",
                        "City did not save expected value.");
                assertEquals(addressBlock.get("Effective From"), UpdateSimpleHelper.effective_date(),
                        "Address 'Effective From' date did not default to current date.");
            }
        }
    }

    /**
     * Checks that the appropriate error message is displayed
     * when trying to proceed from the status section of the add provider flow.
     * @param error the specific data field being tested (e.g. "Type", "Class", "Reason")
     *              to determine which error message to check for
     * @param providerType the type of provider being added
     * @param page the AddProviderPage object representing the current page of the add provider flow,
     *             expects to be on the first section of the flow
     */
    public void validateStatusField(String error, ProviderType providerType, AddProviderPage page)
    {
        page = page.changeProviderType(providerType);

        skipToSection(page, providerType, "Status");

        AddProviderStatusFragment status = page.fillStatus(null, null, null);
        status.selectStatusCode("Select One");

        page.clickNext("Status", null);

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        assertEquals(errorMessageList.getFirst(), errorList.get(String.format("missingStatus%sCode", error)),
                String.format("Expected error message for missing status field '%s' not found.", error));
    }
}
