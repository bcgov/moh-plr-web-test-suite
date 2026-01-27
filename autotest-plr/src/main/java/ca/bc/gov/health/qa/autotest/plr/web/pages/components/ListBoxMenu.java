package ca.bc.gov.health.qa.autotest.plr.web.pages.components;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * Fragment class representing a select list box menu component.
 */
public class ListBoxMenu
extends BasicWebPageFragment
{
    private static final Pattern ITEM_LABEL_PATTERN =
            Pattern.compile("[\\x20-\\x7E&&[^\"\\\\]]+");

    /**
     * Initializes a list box menu fragment.
     *
     * @param selenium     the current selenium session
     * @param mainLocator  the locator for the main div element of the list box menu
     */
    public ListBoxMenu(SeleniumSession selenium, By mainLocator)
    {
        super(selenium, mainLocator);
    }

    /**
     * Grabs the list of all item labels in the list box menu (including unselected and selected).
     *
     * @return the list of all item labels (as strings)
     */
    public List<String> grabItemList()
    {
        return grabItemList(false);
    }

    /**
     * Grabs the list of item labels in the list box menu.
     *
     * @param selectedOnly  whether to grab only selected items (true) or not (false)
     * @return              the list of item labels (as strings)
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
     * Grabs whether the item specified is selected.
     *
     * @param itemPrefix  the first few characters of the item to check
     * @return            whether the specified item is selected (true) or not (false)
     */
    public boolean grabItemSelected(String itemPrefix)
    {
        return grabItemSelected(findItem(itemPrefix));
    }

    /**
     * Selects the specified menu item.
     *
     * @param itemPrefix the first few characters of the item to select
     * @return           the full label of the item selected
     */
    public String selectItem(String itemPrefix)
    {
        return selectItem(itemPrefix, true);
    }

    /**
     * Deselects the specified menu item.
     *
     * @param itemPrefix the first few characters of the item to deselect
     */
    public void clearItem(String itemPrefix) {
    	 selectItem(itemPrefix, false);
	}

    /**
     * Selects or deselects the specified menu item.
     *
     * @param itemPrefix    the first few characters of the item to select/deselect
     * @param select        whether to select (true) or deselect (false) the item
     * @return              the full label of the item selected/deselected
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
     * Finds the menu item element matching the specified prefix.
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
                ".//li[contains(@class, 'ui-selectlistbox-item')]" +
                        "[starts-with(text(),\"" +
                        itemPrefix +
                        "\")]");

        WebElement item;
        List<WebElement> itemList =
                selenium_.findElement(mainLocator_).findElements(menuItemXPath);
        if (itemList.size() == 1)
        {
            item = itemList.getFirst();
        }
        else if (itemList.isEmpty())
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

    /**
     * Grabs whether the specified item element is selected.
     *
     * @param item the item element to check
     * @return     whether the item is selected (true) or not (false)
     */
    private boolean grabItemSelected(WebElement item)
    {
        return SeleniumUtils.grabElementClassSet(item).contains("ui-state-highlight");
    }

    /**
     * Clear all selected items in the list box menu.
     *
     * @return the null string
     */
	public String clearAllItem() {
		List<String> itemList = this.grabItemList();
		for(String exp:itemList ){
			clearItem(UpdateSimpleHelper.grabPrefix(exp));
		}
		return null;
	}

	
}
