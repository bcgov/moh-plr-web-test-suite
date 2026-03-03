package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.AutocompleteMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.add.AddFacilityStepFragment;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AddProviderAddressFragment extends AddFacilityStepFragment {

    private static final String AUTOCOMPLETE_FIELD_CSS = "input#form\\:autoComplete_input";

    private static final String ADDRESS_LINE_1_FIELD_CSS = "input#form\\:addressLine1";

    private static final String ADDRESS_LINE_2_FIELD_CSS = "input#form\\:addressLine2";

    private static final String ADDRESS_LINE_3_FIELD_CSS = "input#form\\:addressLine3";

    private static final String CITY_FIELD_CSS = "input#form\\:city_input";

    private static final String PROVINCE_STATE_FIELD_CSS = "label#form\\:province_drop_label";

    private static final String COUNTRY_FIELD_CSS = "label#form\\:country_label";

    private static final String POSTAL_CODE_FIELD_CSS = "input#form\\:postalCode";

    private static final String WIDGET_TITLE_SPAN_CSS = "div.ui-dialog-titlebar > span.ui-dialog-title";

    /**
     * Initializes fragment and changes selenium's main locator to header of the Address form
     * @param selenium the current SeleniumSession
     */
    public AddProviderAddressFragment(SeleniumSession selenium) {
        super(selenium, "Address");
        STEP_PREFIX = "address";
    }

    private DropDownMenu getAddressTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#form\\:addressType_label"),
                By.cssSelector("div#form\\:addressType_panel")
        );
    }

    /**
     * Gets the currently selected Address Type
     * @return a string of the currently selected Address Type option
     */
    public String getAddressType() { return getAddressTypeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Address Type dropdown
     * @return a list of strings of all Address Type options
     */
    public List<String> getAddressTypeOptions()
    {
        DropDownMenu menu = getAddressTypeMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects an option in the Address Type field
     * @param addressType the Address Type option to select
     * @return a string of the currently selected Address Type option after selection
     */
    public String selectAddressType(String addressType)
    {
        DropDownMenu menu = getAddressTypeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(addressType);
        return getAddressType();
    }

    private DropDownMenu getAddressPurposeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#form\\:addressPurpose_label"),
                By.cssSelector("div#form\\:addressPurpose_panel")
        );
    }

    /**
     * Gets the currently selected Address Purpose
     * @return a string of the currently selected Address Purpose option
     */
    public String getAddressPurpose() { return getAddressPurposeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Address Purpose dropdown
     * @return a list of strings of all Address Purpose options
     */
    public List<String> getAddressPurposeOptions()
    {
        DropDownMenu menu = getAddressPurposeMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects an option in the Address Purpose field
     * @param addressPurpose the Address Purpose option to select
     * @return a string of the currently selected Address Purpose option after selection
     */
    public String selectAddressPurpose(String addressPurpose) {
        DropDownMenu menu = getAddressPurposeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(addressPurpose);
        return getAddressPurpose();
    }

    private AutocompleteMenu getAddressAutocompleteMenu()
    {
        return new AutocompleteMenu(
                selenium_,
                By.cssSelector(AUTOCOMPLETE_FIELD_CSS),
                By.cssSelector("span#form\\:autoComplete_panel")
        );
    }

    /**
     * Fills in the Address Autocomplete field and selects an option from the resulting menu
     * @param addressAutocompleteField the full address to select from the autocomplete menu
     * @param addressAutocompletePrefix the text to fill in the autocomplete field (can be a partial address)
     * @return a string of the full address selected from the autocomplete menu
     */
    public String fillAddressAutocomplete(String addressAutocompleteField, String addressAutocompletePrefix)
    {
        return getAddressAutocompleteMenu().fillItem(addressAutocompleteField, addressAutocompletePrefix);
    }

    private AutocompleteMenu getCityMenu()
    {
        return new AutocompleteMenu(
                selenium_,
                By.cssSelector(CITY_FIELD_CSS),
                By.cssSelector("span#form\\:city_panel")
        );
    }

    /**
     * Fills in the City field and selects an option from the resulting menu, using the full city name as both the text to fill and the option to select
     * @param city the city to select from the autocomplete menu (also used as the text to fill in the city field)
     * @return a string of the city selected from the autocomplete menu
     */
    public String fillCity(String city) { return getCityMenu().fillItem(city, null); }

    /**
     * Fills in the City field and selects an option from the resulting menu
     * @param cityField the city to select from the autocomplete menu
     * @param cityPrefix the text to fill in the city field (can be a partial city name)
     * @return a string of the city selected from the autocomplete menu
     */
    public String fillCityAutocomplete(String cityField, String cityPrefix)
    {
        return getCityMenu().fillItem(cityField, cityPrefix);
    }

    /**
     * Grabs the text currently filled in the Address Line 1 field
     * @return a string of the text currently filled in the Address Line 1 field
     */
    public String getAddressLine1() { return selenium_.findElementByCss(ADDRESS_LINE_1_FIELD_CSS).getAttribute("value"); }

    /**
     * Grabs the text currently filled in the Address Line 2 field
     * @return a string of the text currently filled in the Address Line 2 field
     */
    public String getAddressLine2() { return selenium_.findElementByCss(ADDRESS_LINE_2_FIELD_CSS).getAttribute("value"); }

    /**
     * Grabs the text currently filled in the Address Line 3 field
     * @return a string of the text currently filled in the Address Line 3 field
     */
    public String getAddressLine3() { return selenium_.findElementByCss(ADDRESS_LINE_3_FIELD_CSS).getAttribute("value"); }

    /**
     * Grabs the text currently filled in the Postal Code field
     * @return a string of the text currently filled in the Postal Code field
     */
    public String getPostalCode() { return selenium_.findElementByCss(POSTAL_CODE_FIELD_CSS).getAttribute("value"); }

    /**
     * Fills in the Address Line 1 field with the provided text
     * @param addressLine1 the text to fill in the Address Line 1 field
     */
    public void fillAddressLine1(String addressLine1) { selenium_.fillFieldByCss(ADDRESS_LINE_1_FIELD_CSS, addressLine1); }

    /**
     * Fills in the Address Line 2 field with the provided text
     * @param addressLine2 the text to fill in the Address Line 2 field
     */
    public void fillAddressLine2(String addressLine2) { selenium_.fillFieldByCss(ADDRESS_LINE_2_FIELD_CSS, addressLine2); }

    /**
     * Fills in the Address Line 3 field with the provided text
     * @param addressLine3 the text to fill in the Address Line 3 field
     */
    public void fillAddressLine3(String addressLine3) { selenium_.fillFieldByCss(ADDRESS_LINE_3_FIELD_CSS, addressLine3); }

    /**
     * Fills in the Postal Code field with the provided text
     * @param postalCode the text to fill in the Postal Code field
     */
    public void fillPostalCode(String postalCode) { selenium_.fillFieldByCss(POSTAL_CODE_FIELD_CSS, postalCode); }

    private DropDownMenu getProvinceStateMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector(PROVINCE_STATE_FIELD_CSS),
                By.cssSelector("div#form\\:province_drop_panel")
        );
    }

    /**
     * Gets the currently selected Province/State
     * @return a string of the currently selected Province/State option
     */
    public String getProvinceState() { return getProvinceStateMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Province/State dropdown
     * @return a list of strings of all Province/State options
     */
    public List<String> getProvinceStateOptions()
    {
        DropDownMenu menu = getProvinceStateMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects an option in the Province/State field
     * @param province the Province/State option to select
     * @return a string of the currently selected Province/State option after selection
     */
    public String selectProvinceState(String province)
    {
        DropDownMenu menu = getProvinceStateMenu();
        menu.expandItemPanel(true);
        menu.selectItem(province);
        return getProvinceState();
    }

    private DropDownMenu getCountryMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector(COUNTRY_FIELD_CSS),
                By.cssSelector("div#form\\:country_panel")
        );
    }

    /**
     * Gets the currently selected Country
     * @return a string of the currently selected Country option
     */
    public String getCountry() { return getCountryMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Country dropdown
     * @return a list of strings of all Country options
     */
    public List<String> getCountryOptions()
    {
        DropDownMenu menu = getCountryMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects an option in the Country field
     * @param country the Country option to select
     * @return a string of the currently selected Country option after selection
     */
    public String selectCountry(String country)
    {
        DropDownMenu menu = getCountryMenu();
        menu.expandItemPanel(true);
        menu.selectItem(country);
        return getCountry();
    }

    /**
     * Finds the desired widget form based on a given prefix
     *
     * @param widgetTitlePrefix     a string of (unique) characters to be found in the desired widget title
     * @return                      a WebElement of a widget form matching the widgetTitlePrefix
     */
    private WebElement getWidget(String widgetTitlePrefix)
    {
        for (WebElement elem : selenium_.findElementsByCss("div[role='dialog']")) {
            String title;
            try { title = elem.findElement(By.cssSelector(WIDGET_TITLE_SPAN_CSS)).getAttribute("innerHTML"); }
            catch (org.openqa.selenium.NoSuchElementException ignore) { continue; }
            if (title == null || title.isEmpty()) continue;
            if (!title.contains(widgetTitlePrefix)) continue;
            // Skip hidden/inactive dialogs
            String ariaHidden = elem.getAttribute("aria-hidden");
            if ("true".equals(ariaHidden) || !elem.isDisplayed()) continue;
            return elem;
        }
        throw new IllegalStateException("Visible widget with title containing '" + widgetTitlePrefix + "' not found.");
    }

    /**
     * Finds and clicks the button to close the widget
     *
     * @param errorWidget   string of the type of widget (same as in waitForWidgetVisibility)
     */
    public void handleWidgetButton(String errorWidget)
    {
        String buttonCSS = "div.ui-widget-content > ";
        buttonCSS += "button:first-of-type";

        WebElement widget = getWidget(errorWidget);
        By buttonSelector = By.cssSelector(buttonCSS);

        try {
            widget.findElement(buttonSelector).click();
        } catch (org.openqa.selenium.NoSuchElementException e) {

            // Generic fallback: first displayed & enabled button
            for (WebElement btn : widget.findElements(By.tagName("button"))) {
                if (btn.isDisplayed() && btn.isEnabled()) { btn.click(); return; }
            }
            throw new IllegalStateException("No interactable button found in visible widget '" + errorWidget + "'.");
        }
    }
}
