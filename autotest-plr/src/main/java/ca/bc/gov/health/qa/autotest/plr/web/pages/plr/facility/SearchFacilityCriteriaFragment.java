package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.AutocompleteMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchSectionFragment;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Fragment class for the Search by Criteria section when searching by facility
 */
public class SearchFacilityCriteriaFragment extends SearchSectionFragment
{
    private static final String FACILITY_NAME_FIELD_CSS = "input#accordian\\:searchByCriteriaForm\\:facilityName";

    private static final String CIVIC_ADDRESS_FIELD_CSS = "input#accordian\\:searchByCriteriaForm\\:line1";

    private static final String OTHER_ADDRESS_FIELD_CSS = "input#accordian\\:searchByCriteriaForm\\:oline1";

    private static final String SEARCH_BUTTON_CSS = "button#accordian\\:searchByCriteriaForm\\:searchButton";

    private static final String CLEAR_BUTTON_CSS = "button#accordian\\:searchByCriteriaForm\\:clearButton";

    /**
     * Initializes fragment and changes selenium's main locator to the search by criteria tab container
     *
     * @param selenium  the current SeleniumSession
     */
    public SearchFacilityCriteriaFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:criteriaTab"));
    }

    /**
     * Fills the Facility Name field
     *
     * @param facilityName  the string to fill the Facility Name field with
     */
    public void fillFacilityName(String facilityName)
    {
        selenium_.fillFieldByCss(FACILITY_NAME_FIELD_CSS, facilityName);
    }

    /**
     * Fills the Civic Address Line 1 field
     *
     * @param civicAddress  the string to fill the Civic Address Line 1 field with
     */
    public void fillCivicAddress(String civicAddress)
    {
        selenium_.fillFieldByCss(CIVIC_ADDRESS_FIELD_CSS, civicAddress);
    }

    /**
     * Fills the Other Address Line 1 field
     *
     * @param otherAddress  the string to fill the Other Address Line 1 field with
     */
    public void fillOtherAddress(String otherAddress)
    {
        selenium_.fillFieldByCss(OTHER_ADDRESS_FIELD_CSS, otherAddress);
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
                By.cssSelector("input#accordian\\:searchByCriteriaForm\\:city_input"),
                By.cssSelector("span#accordian\\:searchByCriteriaForm\\:city_panel")
        );
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
     * Constructs a DropDownMenu component for the Facility Type
     *
     * @return  a DropDownMenu component for the Facility Type
     */
    public DropDownMenu getFacilityTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByCriteriaForm\\:facilityType_label"),
                By.cssSelector("div#accordian\\:searchByCriteriaForm\\:facilityType_panel")
        );
    }

    /**
     * Selects the facility type in the Facility Type dropdown based on a prefix
     *
     * @param facilityTypePrefix    the first few characters to match when selecting the menu option
     * @return  A string of the full matched facility type
     */
    public String selectFacilityType(String facilityTypePrefix)
    {
        return getFacilityTypeMenu().selectItem(facilityTypePrefix);
    }

    /**
     * Constructs an AutocompleteMenu component for the Service Delivery Area field
     *
     * @return  an AutocompleteMenu component for the Service Delivery Area field
     */
    public AutocompleteMenu getServiceDeliveryAreaMenu()
    {
        return new AutocompleteMenu(
                selenium_,
                By.cssSelector("input#accordian\\:searchByCriteriaForm\\:sda_input"),
                By.cssSelector("span#accordian\\:searchByCriteriaForm\\:sda_panel")
        );
    }

    /**
     * Fills the service delivery area field, either directly or using the autocomplete feature.
     * If serviceDeliveryAreaPrefix is set to null, the field will be directly filled with serviceDeliveryAreaField.
     * Otherwise, the field will be filled using serviceDeliveryAreaField to setup autocomplete and
     * serviceDeliveryAreaPrefix to select an autocomplete option.
     *
     * @param serviceDeliveryAreaField     the initial characters to fill the service delivery area field with
     * @param serviceDeliveryAreaPrefix    the first few characters to match when selecting an autocomplete option
     * @return                              the selected autocomplete option / filled service delivery area field
     */
    public String fillServiceDeliveryArea(String serviceDeliveryAreaField, String serviceDeliveryAreaPrefix)
    {
        return getServiceDeliveryAreaMenu().fillItem(serviceDeliveryAreaField, serviceDeliveryAreaPrefix);
    }

    /**
     * Clicks the clear button
     */
    public void clickClearButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(CLEAR_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * Clicks the search button to submit the search by criteria query
     */
    public void clickSearchButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(SEARCH_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }
}
