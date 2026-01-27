package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchSectionFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Fragment class for the Search by Identifier section when searching by provider
 */
public class SearchProviderIdFragment
extends SearchSectionFragment
{
    private static final String PROVIDER_ID_FIELD_CSS = "input#accordian\\:searchByIdForm\\:identifier";

    /**
     * Initializes fragment and changes selenium's main locator to the search by identifier tab container
     *
     * @param selenium The current SeleniumSession
     */
    public SearchProviderIdFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:searchByIdTab"));
    }

    /**
     * Finds and clicks the search button to submit the search by identifier query
     */
    public void clickSearchButton()
    {
        WebElement button = selenium_.findElement(mainLocator_).
                findElement(By.cssSelector("button[type='submit']"));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * Fills in the Provider ID field
     *
     * @param providerId The provider ID to fill in
     */
    public void fillProviderId(String providerId)
    {
        selenium_.fillFieldByCss(PROVIDER_ID_FIELD_CSS, providerId);
    }

    /**
     * Clears the Provider ID field
     */
    public void clearProviderId()
    {
        WebElement providerId = selenium_.findElementByCss(PROVIDER_ID_FIELD_CSS);
        providerId.clear();
    }

    /**
     * Gets the Identifier Type dropdown menu component
     *
     * @return a DropDownMenu component for the Identifier Type
     */
    public DropDownMenu getIdentifierTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByIdForm\\:identifierType_label"),
                By.cssSelector("div#accordian\\:searchByIdForm\\:identifierType_panel"));
    }

    /**
     * Selects the identifier type in the Provider Identifier Type dropdown based on a prefix
     *
     * @param identifierTypePrefix the first few characters to match when selecting the menu option
     * @return                     a string of the full matched identifier type
     */
    public String selectIdentifierType(String identifierTypePrefix)
    {
        return getIdentifierTypeMenu().selectItem(identifierTypePrefix);
    }
}
