package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchSectionFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Fragment class for the Search by Organization section when searching by provider
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
     * Initializes fragment and changes selenium's main locator to the search by organization tab container
     *
     * @param selenium The current SeleniumSession
     */
    public SearchProviderOrganizationFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:organizationTab"));
    }

    /**
     * Clicks the search button to submit the search by organization query
     */
    public void clickSearchButton()
    {
        WebElement button = selenium_.findElement(mainLocator_).
                findElement(By.cssSelector("button[type='submit']"));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * Fills in the Address Line 1 field
     *
     * @param addressLine1 The address line 1 to fill in
     */
    public void fillAddressLine1(String addressLine1)
    {
        selenium_.fillFieldByCss(ADDRESS_LINE_1_FIELD, addressLine1);
    }

    /**
     * Fills in the City field
     *
     * @param city The city to fill in
     */
    public void fillCity(String city)
    {
        selenium_.fillFieldByCss(CITY_FIELD_CSS, city);
        if (!city.isEmpty())
        {
        	try {
        		waitForCityPanelVisible(true);
        		selenium_.clickByCss(CITY_FIELD_CSS);
            }
        	catch (org.openqa.selenium.TimeoutException ignored) {}
        }
        waitForCityPanelVisible(false);
    }

    /**
     * Fills in the Description field
     *
     * @param description The description to fill in
     */
    public void fillDescription(String description)
    {
        selenium_.fillFieldByCss(DESCRIPTION_FIELD_CSS, description);
    }

    /**
     * Fills in the Name field
     *
     * @param name The name to fill in
     */
    public void fillName(String name)
    {
        selenium_.fillFieldByCss(NAME_FIELD_CSS, name);
    }

    /**
     * Gets the Role Type dropdown menu component
     *
     * @return a DropDownMenu component for the Role Type
     */
    public DropDownMenu getRoleTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByOrganizationForm\\:providerType_label"),
                By.cssSelector("div#accordian\\:searchByOrganizationForm\\:providerType_panel"));
    }
    
    /**
     * Gets the HDS Type dropdown menu component
     *
     * @return a DropDownMenu component for the HDS Type
     */
    public DropDownMenu getHdsTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByOrganizationForm\\:hds_label"),
                By.cssSelector("div#accordian\\:searchByOrganizationForm\\:hds_panel"));
    }

    /**
     * Select role type code
     * 
     * @param roleTypePrefix the first few characters of the role type to select
     * @return               the role type selected as a string
     */
    public String selectRoleType(String roleTypePrefix)
    {
        return getRoleTypeMenu().selectItem(roleTypePrefix);
    }
    
    /**
     * Select HDS Type code
     * 
     * @param hdsType   the first few characters of the HDS type to select
     * @return          the HDS type selected as a string
     */
    public String selectHdsType(String hdsType)
    {
        return getHdsTypeMenu().selectItem(hdsType);
    }

    /**
     * Waits for the city panel to be visible or not visible
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

    /**
     * Clears the Name field
     */
	public void clearName() {
		WebElement name=selenium_.findElementByCss(NAME_FIELD_CSS);
		name.clear();
	}

    /**
     * Clears the Description field
     */
	public void clearDescription() {
		WebElement des=selenium_.findElementByCss(DESCRIPTION_FIELD_CSS);
		des.clear();
		
	}

    /**
     * Clears the Address Line 1 field
     */
	public void clearAddressLine1() {
		WebElement addrLine1=selenium_.findElementByCss(ADDRESS_LINE_1_FIELD);
		addrLine1.clear();
		
	}

    /**
     * Clears the City field
     */
	public void clearCity() {
		WebElement city=selenium_.findElementByCss(CITY_FIELD_CSS);
		city.clear();
	}
}
