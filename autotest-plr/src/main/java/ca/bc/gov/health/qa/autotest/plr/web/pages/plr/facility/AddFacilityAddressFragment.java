package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.AutocompleteMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DateMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.openqa.selenium.By;

public class AddFacilityAddressFragment extends BasicWebPageFragment {

    private static final String AUTOCOMPLETE_FIELD_CSS = "input#form\\:autoComplete_input";

    private static final String ADDRESS_LINE_1_FIELD_CSS = "input#form\\:addressLine1";

    private static final String ADDRESS_LINE_2_FIELD_CSS = "input#form\\:addressLine2";

    private static final String ADDRESS_LINE_3_FIELD_CSS = "input#form\\:addressLine3";

    private static final String CITY_FIELD_CSS = "input#form\\:city_input";

    private static final String POSTAL_CODE_FIELD_CSS = "input#form\\:postalCode";

    private static final String DATE_FIELD_CSS = "span#form\\:effectiveFromDate_address";

    public AddFacilityAddressFragment(SeleniumSession selenium)
    {
        super(selenium, By.xpath("//table//tbody//tr//td//div//div//span[contains(text(),'Address')]"));
    }

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
     * The field will be filled using addressAutocompleteField to setup autocomplete and
     * addressAutcompletePrefix to select an autocomplete option.
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
     * Otherwise, the field will be filled using cityField to setup autocomplete and
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
     * Creates a DateMenu reference for the Effective From Date field
     *
     * @return  a DateMenu reference to the Effective From Date menu
     */
    public DateMenu getEffectiveFromDateMenu()
    {
        return new DateMenu(selenium_, By.cssSelector(DATE_FIELD_CSS), "address");
    }

    /**
     * Picks the current date as the Effective From Date.
     *
     * @return  the date picked within the field as a string.
     */
    public String effectiveFromCurrentDate()
    {
        return getEffectiveFromDateMenu().pickCurrentDate();
    }

    /**
     * Picks a specific date as the Effective From Date.
     *
     * @param effectiveYear     the year of the date to pick.
     * @param effectiveMonth    the month of the date to pick (1-12, 1 being January, 12 being December)
     * @param effectiveDay      the day of the date to pick (expects 1-31)
     * @return                  the date picked within the field as a string.
     */
    public String effectiveFromSpecificDate(int effectiveYear, int effectiveMonth, int effectiveDay)
    {
        return getEffectiveFromDateMenu().pickSpecificDate(effectiveYear, effectiveMonth, effectiveDay);
    }

    public String getAddressLine1()
    {
        return selenium_.findElementByCss(ADDRESS_LINE_1_FIELD_CSS).getAttribute("value");
    }

    public String getAddressLine2()
    {
        return selenium_.findElementByCss(ADDRESS_LINE_2_FIELD_CSS).getAttribute("value");
    }

    public String getAddressLine3()
    {
        return selenium_.findElementByCss(ADDRESS_LINE_3_FIELD_CSS).getAttribute("value");
    }

    public String getCity() { return selenium_.findElementByCss(CITY_FIELD_CSS).getAttribute("value"); }

    public String getPostalCode()
    {
        return selenium_.findElementByCss(POSTAL_CODE_FIELD_CSS).getAttribute("value");
    }
}
