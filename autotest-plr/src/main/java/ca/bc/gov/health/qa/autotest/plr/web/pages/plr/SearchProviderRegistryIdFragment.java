package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * TODO (AZ) - doc
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
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public SearchProviderRegistryIdFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:searchByRegistryIdTab"));
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
     * @param registryId
     *        ???
     */
    public void fillRegistryId(String registryId)
    {
        selenium_.fillFieldByCss(REGISTRY_ID_FIELD_CSS, registryId);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param registryIdSuffix
     *        ???
     */
    public void fillRegistryIdSuffix(String registryIdSuffix)
    {
        selenium_.fillFieldByCss(REGISTRY_ID_SUFFIX_FIELD_CSS, registryIdSuffix);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public DropDownMenu getRegistryIdentifierTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector(REGISTRY_ID_TYPE_LABEL_CSS),
                By.cssSelector(REGISTRY_ID_TYPE_PANEL_CSS));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param registryIdentifierTypePrefix
     *        ???
     *
     * @return ???
     */
    public String selectRegistryIdentifierType(String registryIdentifierTypePrefix)
    {
        return getRegistryIdentifierTypeMenu().selectItem(registryIdentifierTypePrefix);
    }
}
