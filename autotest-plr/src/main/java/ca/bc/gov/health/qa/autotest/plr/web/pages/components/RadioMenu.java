package ca.bc.gov.health.qa.autotest.plr.web.pages.components;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;

/**
 * Page object for a radio menu component, which is a table with labels in the cells and radio buttons as inputs.
 */
public class RadioMenu extends BasicWebPageFragment  {

    /**
     * Initializes a radio menu fragment.
     * @param selenium the current selenium session
     * @param mainLocator the locator for the main table element of the radio menu
     */
    public RadioMenu(SeleniumSession selenium, By mainLocator) {
        super(selenium, mainLocator);
    }

    /**
     * Grabs the list of all radio option labels in the radio menu.
     * @return the list of all radio option labels (as strings)
     */
    public List<String> grabRadioOptions() {
        WebElement radioTable = selenium_.findElement(mainLocator_);
        return radioTable.findElements(By.cssSelector("tbody > tr > td[role=\"radio\"] > label"))
                .stream().map(WebElement::getText).toList();
    }

    /**
     * Grabs the label of the currently selected radio option in the radio menu.
     * @return the label of the currently selected radio option in the radio menu, or null if no option is selected
     */
    public String grabSelectedItem()
    {
        WebElement radioTable = selenium_.findElement(mainLocator_);
        List<WebElement> radioItems = radioTable.findElements(By.cssSelector("tbody > tr > td[role=\"radio\"]"));
        for (WebElement item : radioItems)
        {
            if (grabItemSelected(item))
            {
                return item.findElement(By.cssSelector("label")).getText();
            }
        }
        return null;
    }

    /**
     * Selects the radio option with a label that starts with the given prefix and returns the selected label as a string.
     * @param itemPrefix the prefix of the label of the radio option to select
     * @return the label of the selected radio option as a string
     */
    public String selectItem(String itemPrefix)
    {
        return selectItem(findItem(itemPrefix));
    }

    private WebElement findItem(String itemPrefix)
    {
        WebElement radioTable = selenium_.findElement(mainLocator_);
        List<WebElement> radioItems = radioTable.findElements(By.cssSelector("tbody > tr > td[role=\"radio\"]"));
        for (WebElement item : radioItems)
        {
            String label = item.findElement(By.cssSelector("label")).getText();
            if (label.startsWith(itemPrefix))
            {
                return item;
            }
        }
        throw new IllegalArgumentException("No radio option with label starting with " + itemPrefix + " found in the radio menu.");
    }

    private String selectItem(WebElement item)
    {
        if (!grabItemSelected(item))
        {
            item.click();
        }
        return item.findElement(By.cssSelector("label")).getText();
    }

    private boolean grabItemSelected(WebElement item)
    {
        return Objects.equals(item.getAttribute("aria-checked"), "true");
    }
}
