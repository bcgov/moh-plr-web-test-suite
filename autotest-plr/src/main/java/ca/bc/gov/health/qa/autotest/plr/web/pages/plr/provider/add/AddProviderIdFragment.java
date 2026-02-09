package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderRoleType;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Page object for the Identifier step of the Add Provider workflow
 */
public class AddProviderIdFragment extends AddProviderStepFragment {

    private final ProviderType providerType;

    private static final String IDENTIFIER_FIELD_CSS = "input#form\\:identifier";

    /**
     * Initializes page object and changes selenium's main locator to the Identifier heading
     * @param selenium the selenium session
     */
    public AddProviderIdFragment(SeleniumSession selenium, ProviderType providerType) {
        super(selenium, "Identifier");
        this.providerType = providerType;
        STEP_PREFIX = "identifier";
    }

    /**
     * Constructs a DropDownMenu component for the Provider Role Type field,
     * which has different locators based on the provider type
     * @return a DropDownMenu component for the Provider Role Type field
     */
    private DropDownMenu getProviderRoleTypeMenu()
    {
        String provider = switch (providerType) {
            case BC_PRACTITIONER -> "form\\:providerRoleTypeIND";
            case OOP_PRACTITIONER -> "form\\:providerRoleTypeOOPIND";
            case ORGANIZATION -> "form\\:providerRoleTypeORG";
        };

        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#" + provider + "_label"),
                By.cssSelector("div#" + provider + "_panel")
        );
    }

    /**
     * Gets the value currently selected in the Provider Role Type field
     * @return a string of the value currently selected as Provider Role Type
     */
    public String getProviderRoleType() { return getProviderRoleTypeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Provider Role Type dropdown
     * @return a list of strings of all provider role type options
     */
    public List<String> getProviderRoleTypeOptions()
    {
        DropDownMenu menu = getProviderRoleTypeMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects the given option in the Provider Role Type dropdown, then returns the value now selected in the dropdown
     * @param providerRoleType the provider role type option to select in the dropdown (OPT, MD, etc.)
     * @return a string of the value currently selected as Provider Role Type after selecting the given option
     */
    public String selectProviderRoleType(String providerRoleType)
    {
        DropDownMenu menu = getProviderRoleTypeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(providerRoleType);
        return menu.grabSelectedItem();
    }

    /**
     * Selects the given option in the Provider Role Type dropdown, then returns the value now selected in the dropdown
     * @param providerRoleType the provider role type option to select in the dropdown as a ProviderRoleType enum (OPT, MD, etc.)
     * @return a string of the value currently selected as Provider Role Type after selecting the given option
     */
    public String selectProviderRoleType(ProviderRoleType providerRoleType)
    {
        DropDownMenu menu = getProviderRoleTypeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(providerRoleType.getText());
        return menu.grabSelectedItem();
    }

    /**
     * Constructs a DropDownMenu component for the Identifier Type field,
     * which has the same locators regardless of provider type
     * @return a DropDownMenu component for the Identifier Type field
     */
    private DropDownMenu getIdentifierTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#form\\:identifierType_label"),
                By.cssSelector("div#form\\:identifierType_panel")
        );
    }

    /**
     * Gets the value currently selected in the Identifier Type field
     * @return a string of the value currently selected as Identifier Type
     */
    public String getIdentifierType() { return getIdentifierTypeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Identifier Type dropdown
     *
     * @return a list of strings of all identifier type options
     */
    public List<String> getIdentifierTypeOptions()
    {
        DropDownMenu menu = getIdentifierTypeMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects the given option in the Identifier Type dropdown, then returns the value now selected in the dropdown
     * @param identifierType the identifier type option to select in the dropdown
     * @return a string of the value currently selected as Identifier Type after selecting the given option
     */
    public String selectIdentifierType(String identifierType)
    {
        DropDownMenu menu = getIdentifierTypeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(identifierType);
        return menu.grabSelectedItem();
    }

    /**
     * Fills in the Identifier field with the given identifier
     * @param identifier the identifier to fill in the Identifier field
     */
    public void fillIdentifier(String identifier)
    {
        if (identifier != null) selenium_.fillFieldByCss(IDENTIFIER_FIELD_CSS, identifier);
    }
}
