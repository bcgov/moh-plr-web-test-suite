package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.HdsType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.OrganizationalProviderRoleType;
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
        List<String> options = menu.grabItemList();
        menu.expandItemPanel(false);
        return options;
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
     * @param providerRoleType the provider role type option to select in the dropdown as an enum (OPT, MD, etc.)
     * @return a string of the value currently selected as Provider Role Type after selecting the given option
     */
    public String selectProviderRoleType(ProviderRoleType providerRoleType)
    {
        return selectProviderRoleType(providerRoleType.getText());
    }

    /**
     * Selects the given option in the Provider Role Type dropdown, then returns the value now selected in the dropdown
     * @param providerRoleType the provider role type option to select in the dropdown as an enum (HDS, ORG, etc.)
     * @return a string of the value currently selected as Provider Role Type after selecting the given option
     */
    public String selectProviderRoleType(OrganizationalProviderRoleType providerRoleType)
    {
        return selectProviderRoleType(providerRoleType.getText());
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
        List<String> options = menu.grabItemList();
        menu.expandItemPanel(false);
        return options;
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
        String identifierCss = IDENTIFIER_FIELD_CSS;
        if (providerType.equals(ProviderType.OOP_PRACTITIONER) || providerType.equals(ProviderType.ORGANIZATION))
            identifierCss += "OOP";

        if (identifier != null) selenium_.fillFieldByCss(identifierCss, identifier);
    }

    /**
     * Constructs a DropDownMenu component for the HDS Type field,
     * @return a DropDownMenu component for the HDS Type field
     */
    private DropDownMenu getHdsTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#form\\:hdsTypeId_label"),
                By.cssSelector("div#form\\:hdsTypeId_panel")
        );
    }

    /**
     * Gets the value currently selected in the HDS Type field
     * @return a string of the value currently selected as HDS Type
     */
    public String getHdsType() { return getHdsTypeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the HDS Type dropdown
     * @return a list of strings of all HDS type options
     */
    public List<String> getHdsTypeOptions()
    {
        DropDownMenu menu = getHdsTypeMenu();
        menu.expandItemPanel(true);
        List<String> options = menu.grabItemList();
        menu.expandItemPanel(false);
        return options;
    }

    /**
     * Selects the given option in the HDS Type dropdown, then returns the value now selected in the dropdown
     * @param hdsType the HDS type option to select in the dropdown
     * @return a string of the value currently selected as HDS Type after selecting the given option
     */
    public String selectHdsType(String hdsType)
    {
        DropDownMenu menu = getHdsTypeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(hdsType);
        return menu.grabSelectedItem();
    }

    /**
     * Selects the given option in the HDS Type dropdown, then returns the value now selected in the dropdown
     * @param hdsType the HDS type option to select in the dropdown as an HdsType enum
     * @return a string of the value currently selected as HDS Type after selecting the given option
     */
    public String selectHdsType(HdsType hdsType)
    {
        DropDownMenu menu = getHdsTypeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(hdsType.getText());
        return menu.grabSelectedItem();
    }

    /**
     * Constructs a DropDownMenu component for the HDS Sub Type field,
     * @return a DropDownMenu component for the HDS Sub Type field
     */
    private DropDownMenu getHdsSubTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#form\\:hdsSubTypeId_label"),
                By.cssSelector("div#form\\:hdsSubTypeId_panel")
        );
    }

    /**
     * Gets the value currently selected in the HDS Sub Type field
     * @return a string of the value currently selected as HDS Sub Type
     */
    public String getHdsSubType() { return getHdsSubTypeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the HDS Sub Type dropdown
     * @return a list of strings of all HDS sub type options
     */
    public List<String> getHdsSubTypeOptions()
    {
        DropDownMenu menu = getHdsSubTypeMenu();
        menu.expandItemPanel(true);
        List<String> options = menu.grabItemList();
        menu.expandItemPanel(false);
        return options;
    }

    /**
     * Selects the given option in the HDS Sub Type dropdown, then returns the value now selected in the dropdown
     * @param hdsSubType the HDS sub type option to select in the dropdown
     * @return a string of the value currently selected as HDS Sub Type after selecting the given option
     */
    public String selectHdsSubType(String hdsSubType)
    {
        DropDownMenu menu = getHdsSubTypeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(hdsSubType);
        return menu.grabSelectedItem();
    }
}
