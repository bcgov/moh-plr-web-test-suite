package ca.bc.gov.health.qa.autotest.plr.web.pages.components;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * TODO (AZ) - doc
 */
public class DropDownMenu
extends BasicWebPageFragment
{
    private static final Pattern ITEM_LABEL_PATTERN =
            Pattern.compile("[\\x20-\\x7E&&[^\"\\\\]]+");

    private final By itemPanelLocator_;

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     *
     * @param mainLocator
     *        ???
     *
     * @param itemPanelLocator
     *        ???
     */
    public DropDownMenu(SeleniumSession selenium, By mainLocator, By itemPanelLocator)
    {
        super(selenium, mainLocator);
        itemPanelLocator_ = requireNonNull(itemPanelLocator, "Null item panel locator.");
    }

    /**
     * TODO (AZ) - doc
     *
     * @param expand ???
     */
    public void expandItemPanel(boolean expand)
    {
        if (grabItemPanelExpanded() != expand)
        {
            selenium_.findElement(mainLocator_).click();
            waitForItemPanelExpanded(expand);
        }
    }

    /**
     * Returns a list of menu items.
     * <p>
     * The menu item panel is required to be already expanded.
     *
     * @return a list of menu items
     *
     * @throws IllegalStateException
     *         if the menu item panel is not expanded
     */
    public List<String> grabItemList()
    {
        verifyItemPanelExpanded(true);
        List <String> itemList = new ArrayList<>();
        WebElement itemPanel = selenium_.findElement(itemPanelLocator_);
        List<WebElement> itemElementList =
                itemPanel.findElements(By.cssSelector("li.ui-selectonemenu-item"));
        for (WebElement itemElement : itemElementList)
        {
            itemList.add(itemElement.getText());
        }
        return itemList;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabItemPanelExpanded()
    {
        return selenium_.findElement(itemPanelLocator_).isDisplayed();
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String grabSelectedItem()
    {
        return selenium_.findElement(mainLocator_).getText();
    }

    /**
     * Selects an item from the drop-down menu.
     *
     * @param itemPrefix
     *        the prefix of the item to select
     *        (i.e. to use with starts with matching)
     *
     * @throws IllegalArgumentException
     *         if {@code itemPrefix} is not valid
     *
     * @throws IllegalStateException
     *         if the item is not found,
     *         too many items are found matching {@code itemPrefix},
     *         or a failure occurs while selecting the item
     *
     * @return the selected item
     */
    public String selectItem(String itemPrefix)
    {
        expandItemPanel(true);
        return selectItemFromPanel(itemPrefix);
    }

    /**
     * Selects an item from the drop-down menu item panel.
     * <p>
     * The menu item panel is required to be already expanded.
     *
     * @param itemPrefix
     *        the prefix of the item to select
     *        (i.e. to use with starts with matching)
     *
     * @throws IllegalArgumentException
     *         if {@code itemPrefix} is not valid
     *
     * @throws IllegalStateException
     *         if the menu item panel is not expanded,
     *         the item is not found,
     *         too many items are found matching {@code itemPrefix},
     *         or a failure occurs while selecting the item
     *
     * @return the selected item
     */
    public String selectItemFromPanel(String itemPrefix)
    {
        WebElement item = findItem(itemPrefix);
        selenium_.scrollIntoView(item);
        String itemLabel = item.getText();
        item.click();
        waitForItemPanelExpanded(false);
        String selectedItem = grabSelectedItem();
        if (!selectedItem.equals(itemLabel))
        {
            String msg = String.format(
                    "Failed to select menu item (actual: \"%s\", expected: \"%s\").",
                    selectedItem,
                    itemLabel);
            throw new IllegalStateException(msg);
        }
        return selectedItem;
    }

    /**
     * Finds an item from the drop-down menu item panel.
     * <p>
     * The menu item panel is required to be already expanded.
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
     *         if the menu item panel is not expanded,
     *         the menu item is not found,
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
        verifyItemPanelExpanded(true);

        // NOTE: The menu item prefix may contain apostrophes (').
        By menuItemCss = By.cssSelector(
                new StringBuilder("li.ui-selectonemenu-item[data-label^=\"")
                        .append(itemPrefix)
                        .append("\"]")
                        .toString());

        WebElement item;
        List<WebElement> itemList =
                selenium_.findElement(itemPanelLocator_).findElements(menuItemCss);
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

    /**
     * TODO (AZ) - doc
     *
     * @param expanded ???
     *
     * @throws IllegalStateException
     *         if the verification fails.
     */
    private void verifyItemPanelExpanded(boolean expanded)
    {
        if (grabItemPanelExpanded() != expanded)
        {
            String msg = String.format(
                    "Menu item panel is not %s.",
                    expanded ? "expanded" : "collapsed");
            throw new IllegalStateException(msg);
        }
    }

    private void waitForItemPanelExpanded(boolean expanded)
    {
        if (expanded)
        {
            selenium_.waitUntil(
                    ExpectedConditions.visibilityOfElementLocated(itemPanelLocator_));

            // Wait for the expand animation to complete.
            // NOTE: The value of the CSS property "opacity" is changing
            //       while the transition animation is in progress,
            //       and "1" when the animation completes.
            selenium_.waitUntil(
                    ExpectedConditions.attributeToBe(itemPanelLocator_, "opacity", "1"));
        }
        else
        {
            selenium_.waitUntil(
                    ExpectedConditions.invisibilityOfElementLocated(itemPanelLocator_));
        }
    }
}
