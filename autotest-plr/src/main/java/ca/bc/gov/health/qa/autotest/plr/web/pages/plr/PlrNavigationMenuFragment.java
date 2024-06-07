package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * TODO (AZ) - doc
 */
public class PlrNavigationMenuFragment
extends BasicWebPageFragment
{
    /**
     * TODO (AZ) - doc
     */
    public static enum Item
    {
        /**
         * TODO (AZ) - doc
         */
        SEARCH_PROVIDER("headerForm\\:searchLink"),

        /**
         * TODO (AZ) - doc
         */
        ADD_PROVIDER("headerForm\\:addProviderLink"),

        /**
         * TODO (AZ) - doc
         */
        SEARCH_FACILITY("headerForm\\:searchFacilitiesLink"),

        /**
         * TODO (AZ) - doc
         */
        ADD_FACILITY("headerForm\\:addFacilityLink"),

        /**
         * TODO (AZ) - doc
         */
        ADMINISTRATION("headerForm\\:adminLink");

        private final String cssSelector_;

        private Item(String cssId)
        {
            cssSelector_ = "a#" + cssId;
        }

        private By getLocator()
        {
            return By.cssSelector(cssSelector_);
        }
    }

    private static final String ACTIVE_ITEM_CLASS_NAME = "selectedMenu";
    private static final String MENU_CSS                = "div#navigation div.subthemes menu";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public PlrNavigationMenuFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#navigation div.subthemes menu"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param item
     *        ???
     *
     * @return ???
     */
    public boolean grabItemActive(Item item)
    {
        return SeleniumUtils.grabElementClassSet(findMenu().findElement(item.getLocator()))
                .contains(ACTIVE_ITEM_CLASS_NAME);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param item
     *        ???
     *
     * @return ???
     */
    public boolean grabItemVisibile(Item item)
    {
        return SeleniumUtils.grabElementVisible(findMenu(), item.getLocator());
    }

    /**
     * TODO (AZ) - doc
     *
     * @param item
     *        ???
     */
    public void openItem(Item item)
    {
        if (grabItemVisibile(item))
        {
            findMenu().findElement(item.getLocator()).click();
        }
        else
        {
            String msg = String.format("Menu item is not visible (%s).", item);
            throw new IllegalStateException(msg);
        }
        waitForItemActive(item);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param item
     *        ???
     */
    public void waitForItemActive(Item item)
    {
        selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                item.getLocator(), ACTIVE_ITEM_CLASS_NAME));
    }

    private WebElement findMenu()
    {
        return selenium_.findElementByCss(MENU_CSS);
    }
}
