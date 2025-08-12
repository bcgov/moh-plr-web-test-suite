package ca.bc.gov.health.qa.autotest.plr.web.pages.components;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * TODO (AZ) - doc
 */
public class ListBoxMenu
extends BasicWebPageFragment
{
    private static final Pattern ITEM_LABEL_PATTERN =
            Pattern.compile("[\\x20-\\x7E&&[^\"\\\\]]+");

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     *
     * @param mainLocator
     *        ???
     */
    public ListBoxMenu(SeleniumSession selenium, By mainLocator)
    {
        super(selenium, mainLocator);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public List<String> grabItemList()
    {
        return grabItemList(false);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param selectedOnly
     *        ???
     *
     * @return ???
     */
    public List<String> grabItemList(boolean selectedOnly)
    {
        By itemLocator;
        if (!selectedOnly)
        {
            itemLocator = By.cssSelector("li.ui-selectlistbox-item");
        }
        else
        {
            itemLocator = By.cssSelector("li.ui-selectlistbox-item.ui-state-highlight");
        }
        List <String> itemList = new ArrayList<>();
        WebElement selectionMenu = selenium_.findElement(mainLocator_);
        List<WebElement> itemElementList = selectionMenu.findElements(itemLocator);
        for (WebElement itemElement : itemElementList)
        {
            itemList.add(itemElement.getText());
        }
        return itemList;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param itemPrefix
     *        ???
     *
     * @return ???
     */
    public boolean grabItemSelected(String itemPrefix)
    {
        return grabItemSelected(findItem(itemPrefix));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param itemPrefix
     *        ???
     *
     * @return ???
     */
    public String selectItem(String itemPrefix)
    {
        return selectItem(itemPrefix, true);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param itemPrefix
     *        ???
     *
     * @param select
     *        ???
     *
     * @return ???
     */
    public String selectItem(String itemPrefix, boolean select)
    {
        WebElement item = findItem(itemPrefix);
        selenium_.scrollIntoView(item);
        if (grabItemSelected(item) != select)
        {
            item.findElement(By.cssSelector("div.ui-chkbox-box")).click();
        }
        if (grabItemSelected(item) != select)
        {
            String msg = String.format(
                    "Failed to %s menu item (%s).", select ? "select" : "deselect", itemPrefix);
            throw new IllegalStateException(msg);
        }
        return item.getText();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param itemPrefix
     *        the prefix of the item to find
     *        (i.e. to use with starts with matching)
     *
     * @return the element containing the item found
     *
     * @throws IllegalArgumentException
     *         if {@code itemPrefix} is not valid
     *
     * @throws IllegalStateException
     *         if the menu item is not found,
     *         or too many menu items are found matching {@code itemPrefix}
     */
    private WebElement findItem(String itemPrefix)
    {
        requireNonNull(itemPrefix, "Null menu item prefix.");
        if (!ITEM_LABEL_PATTERN.matcher(itemPrefix).matches())
        {
            String msg = String.format("Invalid menu item prefix (%s).", itemPrefix);
            throw new IllegalArgumentException(msg);
        }

        // NOTE: The menu item prefix may contain apostrophes (').
        By menuItemXPath = By.xpath(
                new StringBuilder(".//li[contains(@class, 'ui-selectlistbox-item')]")
                        .append("[starts-with(text(),\"")
                        .append(itemPrefix)
                        .append("\")]")
                        .toString());

        WebElement item;
        List<WebElement> itemList =
                selenium_.findElement(mainLocator_).findElements(menuItemXPath);
        if (itemList.size() == 1)
        {
            item = itemList.get(0);
        }
        else if (itemList.size() == 0)
        {
            String msg = String.format("Menu item not found (%s).", itemPrefix);
            throw new IllegalStateException(msg);
        }
        else
        {
            String msg = String.format(
                    "Too many menu items found (%s) (%s).",
                    itemList.size(),
                    itemPrefix);
            throw new IllegalStateException(msg);
        }

        return item;
    }

    private boolean grabItemSelected(WebElement item)
    {
        return SeleniumUtils.grabElementClassSet(item).contains("ui-state-highlight");
    }
}
