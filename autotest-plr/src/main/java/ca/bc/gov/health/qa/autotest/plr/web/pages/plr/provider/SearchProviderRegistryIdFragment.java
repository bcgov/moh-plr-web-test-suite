package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchSectionFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Fragment class for the Provider Search by Registry Identifier section.
 */
public class SearchProviderRegistryIdFragment
extends SearchSectionFragment
{
    private static final String REGISTRY_ID_FIELD_CSS =
            "input#accordian\\:searchByRegistryIdForm\\:registryIdentifier";

    private static final String REGISTRY_ID_SUFFIX_FIELD_CSS =
            "input#accordian\\:searchByRegistryIdForm\\:registryIdentifierPostfix";

    private static final String REGISTRY_ID_TYPE_LABEL_CSS =
            "label#accordian\\:searchByRegistryIdForm\\:registryIdentifierTypeCode_label";

    private static final String REGISTRY_ID_TYPE_PANEL_CSS =
            "div#accordian\\:searchByRegistryIdForm\\:registryIdentifierTypeCode_panel";

    /**
     * Initializes fragment and changes selenium's main locator to the search by registry identifier tab container
     *
     * @param selenium the current SeleniumSession
     */
    public SearchProviderRegistryIdFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:searchByRegistryIdTab"));
    }

    /**
     * Clicks the search button to submit the search by registry identifier query
     */
    public void clickSearchButton()
    {
        WebElement button = selenium_.findElement(mainLocator_).
                findElement(By.cssSelector("button[type='submit']"));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * Fills in the Registry ID field
     *
     * @param registryId the registry ID to fill in
     */
    public void fillRegistryId(String registryId)
    {
        selenium_.fillFieldByCss(REGISTRY_ID_FIELD_CSS, registryId);
    }

    /**
     * Clears the Registry ID field
     */
    public void clearRegistryId()
    {
    	WebElement element=selenium_.findElementByCss(REGISTRY_ID_FIELD_CSS);
    	element.clear();
    }

    /**
     * Fills in the Registry ID Suffix field
     *
     * @param registryIdSuffix the registry ID suffix to fill in
     */
    public void fillRegistryIdSuffix(String registryIdSuffix)
    {
        selenium_.fillFieldByCss(REGISTRY_ID_SUFFIX_FIELD_CSS, registryIdSuffix);
    }

    /**
     * Gets the Registry Identifier Type dropdown menu component
     *
     * @return a DropDownMenu component for the Registry Identifier Type
     */
    public DropDownMenu getRegistryIdentifierTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector(REGISTRY_ID_TYPE_LABEL_CSS),
                By.cssSelector(REGISTRY_ID_TYPE_PANEL_CSS));
    }

    /**
     * Selects an item from the Registry Identifier Type dropdown menu
     *
     * @param registryIdentifierTypePrefix the first few characters to match when selecting the menu option
     * @return                             a string of the full matched registry identifier type
     */
    public String selectRegistryIdentifierType(String registryIdentifierTypePrefix)
    {
        return getRegistryIdentifierTypeMenu().selectItem(registryIdentifierTypePrefix);
    }
}
