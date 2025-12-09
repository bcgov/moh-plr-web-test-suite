package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.AutocompleteMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DateMenu;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Fragment class for the Address section (third step) when creating a new facility
 */
public class AddFacilityAddressFragment extends AddFacilityStepFragment {

    private static final String AUTOCOMPLETE_FIELD_CSS = "input#form\\:autoComplete_input";

    private static final String ADDRESS_LINE_1_FIELD_CSS = "input#form\\:addressLine1";

    private static final String ADDRESS_LINE_2_FIELD_CSS = "input#form\\:addressLine2";

    private static final String ADDRESS_LINE_3_FIELD_CSS = "input#form\\:addressLine3";

    private static final String CITY_FIELD_CSS = "input#form\\:city_input";

    private static final String POSTAL_CODE_FIELD_CSS = "input#form\\:postalCode";

    private static final String PROVINCE_STATE_FIELD_CSS = "label#form\\:province_drop_label";

    private static final String COUNTRY_FIELD_CSS = "label#form\\:country_label";

    private static final String WIDGET_TITLE_SPAN_CSS = "div.ui-dialog-titlebar > span.ui-dialog-title";

    private static final Logger LOG = ExecutionLogManager.getLogger();

    /**
     * Initializes fragment and changes selenium's main locator to header of the Address form
     *
     * @param selenium      the current SeleniumSession
     */
    public AddFacilityAddressFragment(SeleniumSession selenium)
    {
        super(selenium, "Address");
        STEP_PREFIX = "address";
    }

    /**
     * Gets the Address Line 1 field
     *
     * @return  a string of the input to Address Line 1
     */
    public String getAddressLine1() { return selenium_.findElementByCss(ADDRESS_LINE_1_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the Address Line 2 field
     *
     * @return  a string of the input to Address Line 2
     */
    public String getAddressLine2() { return selenium_.findElementByCss(ADDRESS_LINE_2_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the Address Line 3 field
     *
     * @return  a string of the input to Address Line 3
     */
    public String getAddressLine3() { return selenium_.findElementByCss(ADDRESS_LINE_3_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the City field
     *
     * @return  a string of the input to the City field
     */
    public String getCity() { return selenium_.findElementByCss(CITY_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the Postal Code / Zip Code field
     *
     * @return  a string of the input to the Postal Code field
     */
    public String getPostalCode() {return selenium_.findElementByCss(POSTAL_CODE_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the Province / State field
     *
     * @return  a string of the input to the Province field
     */
    public String getProvinceState() { return selenium_.findElementByCss(PROVINCE_STATE_FIELD_CSS).getText(); }

    /**
     * Gets the Country field
     *
     * @return  a string of the input to the Country field
     */
    public String getCountry() { return selenium_.findElementByCss(COUNTRY_FIELD_CSS).getText(); }

    /**
     * Fills the Address Line 1 field
     *
     * @param addressLine1  the string to fill the Other Address Line 1 field with
     */
    public void fillAddressLine1(String addressLine1)
    {
        selenium_.fillFieldByCss(ADDRESS_LINE_1_FIELD_CSS, addressLine1);
    }

    /**
     * Fills the Address Line 2 field
     *
     * @param addressLine2  the string to fill the Other Address Line 1 field with
     */
    public void fillAddressLine2(String addressLine2)
    {
        if (addressLine2 != null) selenium_.fillFieldByCss(ADDRESS_LINE_2_FIELD_CSS, addressLine2);
    }

    /**
     * Fills the Address Line 3 field
     *
     * @param addressLine3  the string to fill the Other Address Line 1 field with
     */
    public void fillAddressLine3(String addressLine3)
    {
        if (addressLine3 != null) selenium_.fillFieldByCss(ADDRESS_LINE_3_FIELD_CSS, addressLine3);
    }

    /**
     * Constructs an AutocompleteMenu component for the City field
     *
     * @return  an AutocompleteMenu component for the City field
     */
    public AutocompleteMenu getCityMenu()
    {
        return new AutocompleteMenu(
                selenium_,
                By.cssSelector(CITY_FIELD_CSS),
                By.cssSelector("span#form\\:city_panel")
        );
    }

    /**
     * Constructs an AutocompleteMenu component for the Address Auto Complete field
     *
     * @return  an AutocompleteMenu component for the Address Auto Complete field
     */
    public AutocompleteMenu getAddressAutocompleteMenu()
    {
        return new AutocompleteMenu(
                selenium_,
                By.cssSelector(AUTOCOMPLETE_FIELD_CSS),
                By.cssSelector("span#form\\:autoComplete_panel")
        );
    }

    /**
     * Fills the Address Autocomplete field using the autocomplete feature.
     * The field will be filled using addressAutocompleteField to set up autocomplete and
     * addressAutocompletePrefix to select an autocomplete option.
     *
     * @param addressAutocompleteField     the initial characters to fill the address autocomplete field with
     * @param addressAutocompletePrefix    the first few characters to match when selecting an autocomplete option
     * @return                              the selected autocomplete option / filled autocomplete field
     */
    public String fillAddressAutocomplete(String addressAutocompleteField, String addressAutocompletePrefix)
    {
        return getAddressAutocompleteMenu().fillItem(addressAutocompleteField, addressAutocompletePrefix);
    }

    /**
     * Fills the city field, either directly or using the autocomplete feature.
     * If cityPrefix is set to null, the field will be directly filled with cityField.
     * Otherwise, the field will be filled using cityField to set up autocomplete and
     * cityPrefix to select an autocomplete option.
     *
     * @param cityField     the initial characters to fill the city field with
     * @param cityPrefix    the first few characters to match when selecting an autocomplete option
     * @return              the selected autocomplete option / filled city field
     */
    public String fillCity(String cityField, String cityPrefix)
    {
        return getCityMenu().fillItem(cityField, cityPrefix);
    }

    /**
     * Fills the Postal Code field
     *
     * @param postalCode  the string to fill the Other Address Line 1 field with
     */
    public void fillPostalCode(String postalCode)
    {
        if (postalCode != null) selenium_.fillFieldByCss(POSTAL_CODE_FIELD_CSS, postalCode);
    }

    /**
     * Returns true if the Province / State field has been disabled (locked as British Columbia)
     *
     * @return  boolean of whether the Province field is disabled (true) or not (false)
     */
    public boolean provinceIsDisabled()
    {
        return selenium_.findElementByCss("div#form\\:province_drop")
                .getAttribute("class").contains("ui-state-disabled");
    }

    /**
     * Returns true if the Country field has been disabled (locked as Canada)
     *
     * @return  boolean of whether the Country field is disabled (true) or not (false)
     */
    public boolean countryIsDisabled()
    {
        return selenium_.findElementByCss("div#form\\:country")
                .getAttribute("class").contains("ui-state-disabled");
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
        if (errorWidget.equals("Mailing")) buttonCSS += "table > tbody > tr > td > ";
        if (errorWidget.equals("Civic") || errorWidget.equals("Mailing"))
        {
            buttonCSS += "table > tbody > tr > td:first-child > ";
        }
        buttonCSS += "button";

        WebElement widget = getWidget(errorWidget);
        By buttonSelector = By.cssSelector(buttonCSS);

        try {
            widget.findElement(buttonSelector).click();
            return;
        } catch (org.openqa.selenium.NoSuchElementException e) {

            // Generic fallback: first displayed & enabled button
            for (WebElement btn : widget.findElements(By.tagName("button"))) {
                if (btn.isDisplayed() && btn.isEnabled()) { btn.click(); return; }
            }
            throw new IllegalStateException("No interactable button found in visible widget '" + errorWidget + "'.");
        }
    }
}
