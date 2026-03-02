package ca.bc.gov.health.qa.autotest.plr.web.pages.components;

import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Fragment class for Autocomplete list components.
 */
public class AutocompleteMenu extends BasicWebPageFragment {

    private static final Logger LOG = ExecutionLogManager.getLogger();

    // TODO: input validation similar to DropDownMenu

    private final By autocompletePanelLocator_;

    /**
     * Initializes the autocomplete menu fragment with locators to the item panel
     *
     * @param selenium                      the current SeleniumSession
     * @param mainLocator                   the main locator, which should be set to the autocomplete input tag
     * @param autocompletePanelLocator      the panel locator, which should be set to the autocomplete panel span tag
     */
    public AutocompleteMenu(SeleniumSession selenium, By mainLocator, By autocompletePanelLocator)
    {
        super(selenium, mainLocator);
        autocompletePanelLocator_ = requireNonNull(autocompletePanelLocator, "Null autocomplete panel locator.");
    }

    /**
     * Determines whether the autocomplete panel window is currently displayed or not
     *
     * @return  whether the panel is displayed (true) or not (false)
     */
    public boolean grabAutocompletePanelActive() { return selenium_.findElement(autocompletePanelLocator_).isDisplayed(); }

    /**
     * Waits for the autocomplete panel window to open or close
     *
     * @param isLoading     whether the panel should be opening (true) or closing (false)
     */
    private void waitForPanelLoad(boolean isLoading)
    {
        if (isLoading) {
            selenium_.waitUntil(
                    ExpectedConditions.visibilityOfElementLocated(autocompletePanelLocator_)
            );
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(autocompletePanelLocator_,
                    "ui-connected-overlay-enter-done"));
        }
        else
        {
            selenium_.waitUntil(
                    ExpectedConditions.invisibilityOfElementLocated(autocompletePanelLocator_)
            );
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(autocompletePanelLocator_,
                    "ui-connected-overlay-exit-done"));
        }
    }

    /**
     * Performs the initial filling of the autocomplete field to trigger the autocomplete panel window, provided the
     * window is not already opened.
     *
     * @param autocompleteField     a string of characters to fill the autocomplete field with initially
     * @param active                whether the autocomplete window is currently active or not
     */
    public void displayAutocomplete(String autocompleteField, boolean active)
    {
        if (grabAutocompletePanelActive() != active)
        {
            selenium_.fillField(mainLocator_, autocompleteField);
            waitForPanelLoad(active);
        }
    }

    /**
     * Ensures the autocomplete panel window is currently open/closed, and throws an error if in an unexpected state.
     *
     * @param active                    whether the autocomplete window should be active (true) or inactive (false)
     * @throws IllegalStateException    if the autocomplete window does not match the expected activity state.
     */
    public void verifyAutocompletePanelActive(boolean active)
    {
        if (grabAutocompletePanelActive() != active)
        {
            String msg = String.format(
                    "Autocomplete item panel is not %s.", active ? "active" : "inactive"
            );
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Gets every possible autocomplete option to choose in the currently active autocomplete panel window.
     *
     * @return      a list of strings of each autocomplete option available
     */
    public List<String> grabItemList()
    {
        verifyAutocompletePanelActive(true);
        List<String> itemList = new ArrayList<>();
        WebElement itemPanel = selenium_.findElement(autocompletePanelLocator_);
        List<WebElement> itemElementList = itemPanel.findElements(By.cssSelector("li.ui-autocomplete-list-item"));
        for (WebElement itemElement : itemElementList)
        {
            itemList.add(itemElement.getText());
        }
        return itemList;
    }

    /**
     * Gets the value currently in the autocomplete field, whether the autocomplete feature was used or not.
     * This value is what is finally passed to the criteria query.
     *
     * @return  a string of the value currently in the autocomplete field
     */
    public String grabCompletedItem() { return selenium_.findElement(mainLocator_).getAttribute("value"); }

    /**
     * Finds the item in the autocomplete list that matches the first few characters of the item prefix if available.
     *
     * @param itemPrefix                the first few characters to select when finding the desired autocomplete item
     * @return                          a WebElement reference of the unique autocomplete item matching itemPrefix.
     *
     * @throws IllegalStateException    if the Autocomplete item cannot be found ("No results" appears), the
     *                                  Autocomplete menu results in no elements, or Multiple elements match itemPrefix
     *                                  meaning no unique autocomplete can be selected.
     */
    public WebElement findItem(String itemPrefix)
    {
        requireNonNull(itemPrefix, "Null item prefix.");
        verifyAutocompletePanelActive(true);

        By autocompleteListCss = By.cssSelector(
            new StringBuilder("li.ui-autocomplete-item[data-item-label^=\""
            ).append(itemPrefix).append("\"]").toString()
        );

        WebElement item;
        selenium_.waitUntil(ExpectedConditions.elementToBeClickable(autocompleteListCss));
        List<WebElement> itemList = selenium_.findElements(autocompleteListCss);
        if (itemList.size() == 1)
        {
            item = itemList.getFirst();
            if (item.getText().equals("No results"))
            {
                String msg = String.format("Autocomplete item not found (%S).", itemPrefix);
                throw new IllegalStateException(msg);
            }
        }
        else if (itemList.isEmpty())
        {
            String msg = String.format("Autocomplete item not found (%s).", itemPrefix);
            throw new IllegalStateException(msg);
        }
        else
        {
            String msg = String.format("Too many autocomplete items found (%s) (%s).", itemList.size(), itemPrefix);
            throw new IllegalStateException(msg);
        }
        return item;
    }

    /**
     * Selects an autocomplete option from the autocomplete menu panel matching itemPrefix.
     *
     * @param itemPrefix                the first few characters to match when selecting the autocomplete option.
     * @return                          the selected autocompleted option from the list
     * @throws IllegalStateException    if the selected autocomplete option fails to fill the input field
     */
    public String selectItemFromPanel(String itemPrefix)
    {
        WebElement item = findItem(itemPrefix);
        selenium_.scrollIntoView(item);
        String itemLabel = item.getText();
        item.click();
        waitForPanelLoad(false);
        String completedItem = grabCompletedItem();
        if (!completedItem.equals(itemLabel))
        {
            if (!completedItem.isEmpty())
            {
                String msg = String.format("Failed to select autocomplete item (actual: \"%s\", expected: \"%s\").", completedItem, itemLabel);
                throw new IllegalStateException(msg);
            } else { return itemLabel; }
        }
        return completedItem;
    }

    /**
	 * Fills the autocomplete field with perfix, find and select the first item that contains a
	 * certain string from the list
	 *
	 * @param prefix the first few characters to match when selecting an
	 *               autocomplete option. Leave null to fill the field directly with
	 *               autocompleteField.
	 * @param match  a string that used to find matched item from the list
	 *
	 * @return a string of the completed item in the autocomplete field, ot empty string if not find matched item
	 */
	public String selectItemFromPanelMatch(String prefix, String match) {
		selenium_.fillField(mainLocator_, prefix);
		WebElement item = findItemMatch(prefix, match);
		if (item == null) {
			return "";
		}
		selenium_.scrollIntoView(item);
		String itemLabel = item.getText();
		item.click();
		String completedItem = grabCompletedItem();
		if (!completedItem.equals(itemLabel)) {
			String msg = String.format("Failed to select autocomplete item (actual: \"%s\", expected: \"%s\").",
					completedItem, itemLabel);
			throw new IllegalStateException(msg);
		}
		return completedItem;
	}

	/**
	 * Fills the autocomplete field with perfix, find the web element having certain string from the list
	 *
	 * @param prefix the first few characters to match when selecting an
	 *               autocomplete option. Leave null to fill the field directly with
	 *               autocompleteField.
	 * @param match  a string that used to find matched item from the list
	 *
	 * @return a WebElement of the completed item in the autocomplete field, or null if not find
	 */
	private WebElement findItemMatch(String prefix, String match) {
		requireNonNull(prefix, "Null item prefix.");
		requireNonNull(match, "Null item match.");
		verifyAutocompletePanelActive(true);

		By autocompleteListCss = By.cssSelector(new StringBuilder("li.ui-autocomplete-item[data-item-label^=\"")
				.append(prefix).append("\"]").toString());

		WebElement item = null;
		selenium_.waitUntil(ExpectedConditions.elementToBeClickable(autocompleteListCss));
		List<WebElement> itemList = selenium_.findElements(autocompleteListCss);
		if (itemList.size() == 1) {
			item = itemList.getFirst();
			if (item.getText().equals("No results")) {
				String msg = String.format("Autocomplete item not found (%S).", prefix);
				throw new IllegalStateException(msg);
			}
		} else if (itemList.isEmpty()) {
			String msg = String.format("Autocomplete item not found (%s).", prefix);
			throw new IllegalStateException(msg);
		} else {
			for (WebElement element : itemList) {
				if (element.getText().contains(match)) {
					item = element;
					break;
				}
			}
		}
		return item;
	}

    /**
     * Fills the autocomplete field, either directly or by selecting an autocomplete option.
     *
     * @param autocompleteField     the initial string to fill the autocomplete field with
     * @param itemPrefix            the first few characters to match when selecting an autocomplete option.
     *                              Leave null to fill the field directly with autocompleteField.
     * @return                      a string of the completed item in the autocomplete field
     */
    public String fillItem(String autocompleteField, String itemPrefix)
    {
        if (itemPrefix == null)
        {
            selenium_.fillField(mainLocator_, autocompleteField);

            selenium_.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.ui-icon-loading")));
            WebElement spinner = selenium_.findElement(By.cssSelector("span.ui-icon-loading"));
            selenium_.waitUntil(ExpectedConditions.stalenessOf(spinner));

            if (selenium_.findElement(autocompletePanelLocator_).isDisplayed())
            {
                waitForPanelLoad(true);
                selenium_.click(mainLocator_);
                waitForPanelLoad(false);
            }
            return grabCompletedItem();
        }
        else
        {
            displayAutocomplete(autocompleteField, true);
            return selectItemFromPanel(itemPrefix);
        }
    }
}
