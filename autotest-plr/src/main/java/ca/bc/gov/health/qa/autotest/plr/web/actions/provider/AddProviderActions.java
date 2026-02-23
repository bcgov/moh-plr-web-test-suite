package ca.bc.gov.health.qa.autotest.plr.web.actions.provider;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.add.AddFacilityAddressFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderAddressFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.OrganizationalProviderRoleType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderRoleType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.StatusCodeOption;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.StatusReasonCodeOption;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

import java.util.List;

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
                page.waitForAddProviderStep("Personal Information", true);
            case "Personal Information":
                page.fillPI(null, "Test", null, null, "Provider");
            case "Demographics":
                page.fillDemographics(List.of(2020, 1, 1), "U");
                page.clickNext("Demographic Details", "");
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
}
