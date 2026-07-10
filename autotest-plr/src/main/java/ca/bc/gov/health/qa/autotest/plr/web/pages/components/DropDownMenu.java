package ca.bc.gov.health.qa.autotest.plr.web.pages.components;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * Fragment class for dropdown menu components.
 */
public class DropDownMenu
extends BasicWebPageFragment
{
    private static final Pattern ITEM_LABEL_PATTERN =
            Pattern.compile("[\\x20-\\x7E&&[^\"\\\\]]+");

    private final By itemPanelLocator_;

    /**
     * Initializes the dropdown menu fragment with locators to the item panel and dropdown selection
     *
     * @param selenium
     *        the current SeleniumSession
     *
     * @param mainLocator
     *        the main locator, which should be set to the dropdown's label (dropdown selection)
     *
     * @param itemPanelLocator
     *        the item panel locator, which should be set to the dropdown div (full item panel/dropdown)
     */
    public DropDownMenu(SeleniumSession selenium, By mainLocator, By itemPanelLocator)
    {
        super(selenium, mainLocator);
        itemPanelLocator_ = requireNonNull(itemPanelLocator, "Null item panel locator.");
    }

    /**
     * Expands or collapses the dropdown menu panel
     *
     * @param expand    whether the menu panel should be expanded (true) or collapsed (false)
     */
    public void expandItemPanel(boolean expand)
    {
        if (grabItemPanelExpanded() != expand)
        {
        	Actions actions = new Actions(selenium_.getDriver());
        	actions.moveToElement(selenium_.findElement(mainLocator_)).click().perform();
			//selenium_.findElement(mainLocator_).click();
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
        expandItemPanel(false); // to handle accidental click interceptions
        return itemList;
    }

    /**
     * Determines whether the dropdown's item menu panel is currently displayed or not
     *
     * @return  boolean of whether the panel is displayed (true) or not (false)
     */
    public boolean grabItemPanelExpanded()
    {
        return selenium_.findElement(itemPanelLocator_).isDisplayed();
    }

    /**
     * Gets the item in the dropdown that is currently selected
     *
     * @return  A string of the name of the selected item
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
        if(!grabItemPanelExpanded())
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
        //selenium_.scrollIntoView(item);
        selenium_.waitUntil(ExpectedConditions.elementToBeClickable(item));
        String itemLabel = item.getText();
        //Actions actions = new Actions(selenium_.getDriver());
    	//actions.moveToElement(item).click().perform();
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
        By menuItemCss = By.cssSelector("li.ui-selectonemenu-item[data-label^=\"" + itemPrefix + "\"]");

        WebElement item;
        List<WebElement> itemList =
                selenium_.findElement(itemPanelLocator_).findElements(menuItemCss);
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
     * Guard function that verifies the drop-down item panel is in an expected state.
     *
     * @param expanded  whether the item panel should be expanded (true) or collapsed (false)
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

    /**
     * Waits for the dropdown menu panel to expand/collapse
     *
     * @param expanded  whether the dropdown is currently being expanded (true) or collapsed (false)
     */
    private void waitForItemPanelExpanded(boolean expanded)
    {
        if (expanded)
        {
        	try{
            selenium_.waitUntil(
                    ExpectedConditions.visibilityOfElementLocated(itemPanelLocator_));
        	}
        	catch(org.openqa.selenium.TimeoutException e){
        		
        	}

            // Wait for the expand animation to complete.
            // NOTE: The value of the CSS property "opacity" is changing
            //       while the transition animation is in progress,
            //       and "1" when the animation completes.
            selenium_.waitUntil(
                    ExpectedConditions.attributeToBe(itemPanelLocator_, "opacity", "1"));
            waitSeconds(2);
            // wait for dropdown to be open (i.e. animation to be done and dropdown to be in final open state)
            selenium_.waitUntil(ExpectedConditions.attributeContains(
            		
                    itemPanelLocator_, "class", "ui-connected-overlay-enter-done"));
        }
        else
        {
            selenium_.waitUntil(
                    ExpectedConditions.invisibilityOfElementLocated(itemPanelLocator_));
        }
    }
    /**
     * Tries to wait some number of seconds. Will fail the test used in if interrupted.
	 * TODO this should be used as little as possible in favour of selenium implicit waits.
     *
     * @param second the number of seconds to wait.
     */
        public void waitSeconds(int second) {
		try {
			Thread.sleep(1000L * second);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}
}
