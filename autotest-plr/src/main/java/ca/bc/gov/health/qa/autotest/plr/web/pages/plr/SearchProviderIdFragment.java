package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * TODO (AZ) - doc
 */
public class SearchProviderIdFragment
extends SearchSectionFragment
{
    private static final String PROVIDER_ID_FIELD_CSS =
            "input#accordian\\:searchByIdForm\\:identifier";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public SearchProviderIdFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:searchByIdTab"));
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
     * @param providerId
     *        ???
     */
    public void fillProviderId(String providerId)
    {
        selenium_.fillFieldByCss(PROVIDER_ID_FIELD_CSS, providerId);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public DropDownMenu getIdentifierTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#accordian\\:searchByIdForm\\:identifierType_label"),
                By.cssSelector("div#accordian\\:searchByIdForm\\:identifierType_panel"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param identifierTypePrefix
     *        ???
     *
     * @return ???
     */
    public String selectIdentifierType(String identifierTypePrefix)
    {
        return getIdentifierTypeMenu().selectItem(identifierTypePrefix);
    }
}
