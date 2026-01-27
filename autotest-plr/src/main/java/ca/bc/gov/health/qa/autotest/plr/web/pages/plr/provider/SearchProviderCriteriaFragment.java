package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchSectionFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.ListBoxMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Fragment class for the Search by Criteria section when searching by provider
 */
public class SearchProviderCriteriaFragment
extends SearchSectionFragment
{
    private static final String CITY_FIELD_CSS =
            "input#accordian\\:searchByCriteriaForm\\:city_input";

    private static final String CITY_PANEL_CSS =
            "span#accordian\\:searchByCriteriaForm\\:city_panel";

    private static final String CLEAR_BUTTON_CSS =
            "button#accordian\\:searchByCriteriaForm\\:clearButton";

    private static final String EXPERTISE_MENU_CSS =
            "div#accordian\\:searchByCriteriaForm\\:expertise";

    private static final String FIRST_NAME_FIELD_CSS =
            "input#accordian\\:searchByCriteriaForm\\:firstName";

    private static final String LAST_NAME_FIELD_CSS =
            "input#accordian\\:searchByCriteriaForm\\:lastName";

    private static final String SEARCH_BUTTON_CSS =
            "button#accordian\\:searchByCriteriaForm\\:searchButton";

    private static final String STATUS_REASON_CODE_MENU_CSS =
            "label#accordian\\:searchByCriteriaForm\\:licenseStatusReasonCdId_label";

    /**
     * Initializes fragment and changes selenium's main locator to the search by criteria tab container
     *
     * @param selenium the current SeleniumSession
     */
    public SearchProviderCriteriaFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:criteriaTab"));
    }

    /**
     * Clicks the Clear button
     */
    public void clickClearButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(CLEAR_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * Clicks the Search button
     */
    public void clickSearchButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(SEARCH_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * Fills the City field
     *
     * @param city the string to fill the City field with
     */
    public void fillCity(String city)
    {
        selenium_.fillFieldByCss(CITY_FIELD_CSS, city);
        if (!city.isEmpty())
        {
            waitForCityPanelVisible(true);
            selenium_.clickByCss(CITY_FIELD_CSS);
        }
        waitForCityPanelVisible(false);
    }

    /**
     * Clears the City field
     */
    public void clearCity() {
    	WebElement element=selenium_.findElementByCss(CITY_FIELD_CSS);
   	    element.clear();
    }

    /**
     * Fills the First Name field
     *
     * @param firstName the string to fill the First Name field with
     */
    public void fillFirstName(String firstName)
    {
        selenium_.fillFieldByCss(FIRST_NAME_FIELD_CSS, firstName);
    }

    /**
     * Clears the First Name field
     */
    public void clearFirstName()
    {
    	 WebElement element=selenium_.findElementByCss(FIRST_NAME_FIELD_CSS);
    	 element.clear();
    }

    /**
     * Fills the Last Name field
     *
     * @param lastName the string to fill the Last Name field with
     */
    public void fillLastName(String lastName)
    {
        selenium_.fillFieldByCss(LAST_NAME_FIELD_CSS, lastName);
    }

    /**
     * Clears the Last Name field
     */
    public void clearLastName(){
    	WebElement element=selenium_.findElementByCss(LAST_NAME_FIELD_CSS);
   	 element.clear();
    }

    /**
     * Gets the Expertise list box menu
     *
     * @return the ListBoxMenu reference for the Expertise menu
     */
    public ListBoxMenu getExpertiseMenu()
    {
    	 return new ListBoxMenu(
                 selenium_,
                 By.cssSelector("div#accordian\\:searchByCriteriaForm\\:expertise"));
    }

    /**
     * Gets the Gender drop-down menu
     *
     * @return the DropDownMenu reference for the Gender menu
     */
    public DropDownMenu getGenderMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByCriteriaForm\\:gender_label"),
                By.cssSelector("div#accordian\\:searchByCriteriaForm\\:gender_panel"));
    }

    /**
     * Gets the Language list box menu
     *
     * @return the ListBoxMenu reference for the Language menu
     */
    public ListBoxMenu getLanguageMenu()
    {
        return new ListBoxMenu(
                selenium_,
                By.cssSelector("div#accordian\\:searchByCriteriaForm\\:language"));
    }

    /**
     * Gets the Role Type drop-down menu
     *
     * @return the DropDownMenu reference for the Role Type menu
     */
    public DropDownMenu getRoleTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByCriteriaForm\\:providerType_label"),
                By.cssSelector("div#accordian\\:searchByCriteriaForm\\:providerType_panel"));
    }

    /**
     * Gets the Status Code drop-down menu
     *
     * @return the DropDownMenu reference for the Status Code menu
     */
    public DropDownMenu getStatusCodeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByCriteriaForm\\:licenseStatusCdId_label"),
                By.cssSelector("div#accordian\\:searchByCriteriaForm\\:licenseStatusCdId_panel"));
    }

    /**
     * Gets the Status Reason Code drop-down menu
     *
     * @return the DropDownMenu reference for the Status Reason Code menu
     */
    public DropDownMenu getStatusReasonCodeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector(STATUS_REASON_CODE_MENU_CSS),
                By.cssSelector(
                        "div#accordian\\:searchByCriteriaForm\\:licenseStatusReasonCdId_panel"));
    }

    /**
     * Selects an expertise item from the Expertise list box menu
     *
     * @param expertisePrefix the first few characters to match when selecting the menu option
     * @return                the selected expertise item as a string
     */
    public String selectExpertise(String expertisePrefix)
    {
        return getExpertiseMenu().selectItem(expertisePrefix);
    }


    /**
     * Clears all selected expertise items
     */
    public void clearAllExpertise()
    {
        getExpertiseMenu().clearAllItem();
    }

    /**
     * Clears a specific expertise item
     *
     * @param expertisePrefix the first few characters to match when selecting the menu option to clear
     */
    public void clearExpertise(String expertisePrefix) {
    	 getExpertiseMenu().clearItem(expertisePrefix);
	}

    /**
     * Selects a gender from the Gender drop-down menu
     *
     * @param genderPrefix the first few characters to match when selecting the menu option
     * @return             the selected gender as a string
     */
    public String selectGender(String genderPrefix)
    {
        return getGenderMenu().selectItem(genderPrefix);
    }

    /**
     * Sets the Gender drop-down menu to the default option
     */
    public void clearGender()
    {
        if(!getGenderMenu().grabSelectedItem().equals("Select One"))
        		getGenderMenu().selectItem("Select One");
    }

    /**
     * Selects a language from the Language list box menu
     *
     * @param languagePrefix the first few characters to match when selecting the menu option
     * @return               the selected language as a string
     */
    public String selectLanguage(String languagePrefix)
    {
        return getLanguageMenu().selectItem(languagePrefix);
    }

    /**
     * Selects a role type from the Role Type drop-down menu
     *
     * @param roleTypePrefix the first few characters to match when selecting the menu option
     * @return               the selected role type as a string
     */
    public String selectRoleType(String roleTypePrefix)
    {
        // NOTE: Selecting the provider role type causes the expertise menu to reload.
        WebElement expertiseMenu =
                selenium_.findElement(By.cssSelector(EXPERTISE_MENU_CSS));
        String selectedItem = getRoleTypeMenu().selectItem(roleTypePrefix);
		try {
			selenium_.waitUntil(ExpectedConditions.stalenessOf(expertiseMenu));
		} catch (org.openqa.selenium.TimeoutException ignored) {}
        return selectedItem;
    }

    /**
     * Sets the Role Type drop-down menu to the default option
     */
    public void clearRoleType() {
    	 WebElement expertiseMenu = selenium_.findElement(By.cssSelector(EXPERTISE_MENU_CSS));
         getRoleTypeMenu().selectItem("Select One");
         try {
 			selenium_.waitUntil(ExpectedConditions.stalenessOf(expertiseMenu));
 		 } catch (org.openqa.selenium.TimeoutException ignored) {}
    }

    /**
     * Selects a status code from the Status Code drop-down menu
     *
     * @param statusCodePrefix the first few characters to match when selecting the menu option
     * @return                 the selected status code as a string
     */
    public String selectStatusCode(String statusCodePrefix)
    {
        // NOTE: Selecting the status code causes the status reason code menu to reload.
        WebElement statusReasonCodeMenu = selenium_.findElement(By.cssSelector(STATUS_REASON_CODE_MENU_CSS));
        String selectedItem = getStatusCodeMenu().selectItem(statusCodePrefix);
        selenium_.waitUntil(ExpectedConditions.stalenessOf(statusReasonCodeMenu));
        return selectedItem;
    }

    /**
     * Selects a status reason code from the Status Reason Code drop-down menu
     *
     * @param statusReasonCodePrefix the first few characters to match when selecting the menu option
     * @return                       the selected status reason code as a string
     */
    public String selectStatusReasonCode(String statusReasonCodePrefix)
    {
        return getStatusReasonCodeMenu().selectItem(statusReasonCodePrefix);
    }

    /**
     * Waits for the City panel to be visible or not visible
     *
     * @param visible true to wait for visible, false to wait for invisible
     */
    public void waitForCityPanelVisible(boolean visible)
    {
        WebElement cityPanel = selenium_.findElementByCss(CITY_PANEL_CSS);
        if (visible)
        {
            selenium_.waitUntil(ExpectedConditions.visibilityOf(cityPanel));

            // Wait for the expand animation to complete.
            // NOTE: The value of the CSS property "opacity" is changing
            //       while the transition animation is in progress,
            //       and "1" when the animation completes.
            selenium_.waitUntil(ExpectedConditions.attributeToBe(cityPanel, "opacity", "1"));
        }
        else
        {
            selenium_.waitUntil(ExpectedConditions.invisibilityOf(cityPanel));
        }
    }

	
}
