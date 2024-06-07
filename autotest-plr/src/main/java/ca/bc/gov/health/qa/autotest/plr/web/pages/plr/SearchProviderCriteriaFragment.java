package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.ListBoxMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * TODO (AZ) - doc
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
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public SearchProviderCriteriaFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:criteriaTab"));
    }

    /**
     * TODO (AZ) - doc
     */
    public void clickClearButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(CLEAR_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * TODO (AZ) - doc
     */
    public void clickSearchButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(SEARCH_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param city
     *        ???
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
     * TODO (AZ) - doc
     *
     * @param firstName
     *        ???
     */
    public void fillFirstName(String firstName)
    {
        selenium_.fillFieldByCss(FIRST_NAME_FIELD_CSS, firstName);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param lastName
     *        ???
     */
    public void fillLastName(String lastName)
    {
        selenium_.fillFieldByCss(LAST_NAME_FIELD_CSS, lastName);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public ListBoxMenu getExpertiseMenu()
    {
        return new ListBoxMenu(selenium_, By.cssSelector(EXPERTISE_MENU_CSS));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public DropDownMenu getGenderMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByCriteriaForm\\:gender_label"),
                By.cssSelector("div#accordian\\:searchByCriteriaForm\\:gender_panel"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public ListBoxMenu getLanguageMenu()
    {
        return new ListBoxMenu(
                selenium_,
                By.cssSelector("div#accordian\\:searchByCriteriaForm\\:language"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public DropDownMenu getRoleTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByCriteriaForm\\:providerType_label"),
                By.cssSelector("div#accordian\\:searchByCriteriaForm\\:providerType_panel"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public DropDownMenu getStatusCodeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByCriteriaForm\\:licenseStatusCdId_label"),
                By.cssSelector("div#accordian\\:searchByCriteriaForm\\:licenseStatusCdId_panel"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @param expertisePrefix
     *        ???
     *
     * @return ???
     */
    public String selectExpertise(String expertisePrefix)
    {
        return getExpertiseMenu().selectItem(expertisePrefix);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param genderPrefix
     *        ???
     *
     * @return ???
     */
    public String selectGender(String genderPrefix)
    {
        return getGenderMenu().selectItem(genderPrefix);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param languagePrefix
     *        ???
     *
     * @return ???
     */
    public String selectLanguage(String languagePrefix)
    {
        return getLanguageMenu().selectItem(languagePrefix);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param roleTypePrefix
     *        ???
     *
     * @return ???
     */
    public String selectRoleType(String roleTypePrefix)
    {
        // NOTE: Selecting the provider role type causes the expertise menu to reload.
        WebElement expertiseMenu =
                selenium_.findElement(By.cssSelector(EXPERTISE_MENU_CSS));
        String selectedItem = getRoleTypeMenu().selectItem(roleTypePrefix);
        selenium_.waitUntil(ExpectedConditions.stalenessOf(expertiseMenu));
        return selectedItem;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param statusCodePrefix
     *        ???
     *
     * @return ???
     */
    public String selectStatusCode(String statusCodePrefix)
    {
        // NOTE: Selecting the status code causes the status reason code menu to reload.
        WebElement statusReasonCodeMenu =
                selenium_.findElement(By.cssSelector(STATUS_REASON_CODE_MENU_CSS));
        String selectedItem = getStatusCodeMenu().selectItem(statusCodePrefix);
        selenium_.waitUntil(ExpectedConditions.stalenessOf(statusReasonCodeMenu));
        return selectedItem;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param statusReasonCodePrefix
     *        ???
     *
     * @return ???
     */
    public String selectStatusReasonCode(String statusReasonCodePrefix)
    {
        return getStatusReasonCodeMenu().selectItem(statusReasonCodePrefix);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param visible
     *        ???
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
