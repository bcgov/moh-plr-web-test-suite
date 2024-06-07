package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * TODO (AZ) - doc
 */
public class SearchProviderOrganizationFragment
extends SearchSectionFragment
{
    private static final String ADDRESS_LINE_1_FIELD =
            "input#accordian\\:searchByOrganizationForm\\:addressLine1";

    private static final String CITY_FIELD_CSS =
            "input#accordian\\:searchByOrganizationForm\\:city_input";

    private static final String CITY_PANEL_CSS =
            "span#accordian\\:searchByOrganizationForm\\:city_panel";

    private static final String DESCRIPTION_FIELD_CSS =
            "input#accordian\\:searchByOrganizationForm\\:orgLongName";

    private static final String NAME_FIELD_CSS =
            "input#accordian\\:searchByOrganizationForm\\:orgName";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public SearchProviderOrganizationFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:organizationTab"));
    }

    /**
     * TODO (AZ) - doc
     */
    public void clickSearchButton()
    {
        WebElement button = selenium_.findElement(mainLocator_).
                findElement(By.cssSelector("button[type='submit']"));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param addressLine1
     *        ???
     */
    public void fillAddressLine1(String addressLine1)
    {
        selenium_.fillFieldByCss(ADDRESS_LINE_1_FIELD, addressLine1);
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
     * @param description
     *        ???
     */
    public void fillDescription(String description)
    {
        selenium_.fillFieldByCss(DESCRIPTION_FIELD_CSS, description);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param name
     *        ???
     */
    public void fillName(String name)
    {
        selenium_.fillFieldByCss(NAME_FIELD_CSS, name);
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
                By.cssSelector("label#accordian\\:searchByOrganizationForm\\:providerType_label"),
                By.cssSelector("div#accordian\\:searchByOrganizationForm\\:providerType_panel"));
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
        return getRoleTypeMenu().selectItem(roleTypePrefix);
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
