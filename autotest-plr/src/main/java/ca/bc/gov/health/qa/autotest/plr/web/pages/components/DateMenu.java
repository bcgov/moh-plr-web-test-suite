package ca.bc.gov.health.qa.autotest.plr.web.pages.components;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Fragment class for Date/Datepicker Menu components.
 */
public class DateMenu extends BasicWebPageFragment {

    private final By datepickerLocator_ = By.cssSelector("div#ui-datepicker-div");

    private final By inputLocator_;

    /**
     * Initializes a date menu fragment with locators to the date field and the hidden datepicker menu
     *
     * @param selenium              the current selenium session
     * @param mainLocator           the main locator, which should be set to the span encompassing the field/menu button
     * @param stepPrefix            the
     */
    public DateMenu(SeleniumSession selenium, By mainLocator, String stepPrefix)
    {
        super(selenium, mainLocator);
        stepPrefix = requireNonNull(stepPrefix, "Missing step prefix.");
        inputLocator_ = By.cssSelector(String.format("input#form\\:effectiveFromDate_%s_input", stepPrefix));
    }

    /**
     * Determines whether the date picker menu window is currently displayed or not
     *
     * @return  whether the panel is displayed (true) or not (false)
     */
    public boolean grabDatePanelActive() { return selenium_.findElement(datepickerLocator_).isDisplayed(); }

    /**
     * Waits for the date picker menu window to open or close
     *
     * @param isLoading     whether the panel should be opening (true) or closing (false)
     */
    private void waitForPanelLoad(boolean isLoading)
    {
        if (isLoading) {
            selenium_.waitUntil(
                    ExpectedConditions.visibilityOfElementLocated(datepickerLocator_)
            );
        }
        else
        {
            selenium_.waitUntil(
                    ExpectedConditions.invisibilityOfElementLocated(datepickerLocator_)
            );
        }
    }

    /**
     * Opens the datepicker menu by clicking the button (if it is not already open)
     *
     * @param active    whether the menu is currently active or not
     */
    public void displayDatepicker(boolean active)
    {
        if (grabDatePanelActive() != active)
        {
            WebElement dateFieldSpan = selenium_.findElement(mainLocator_);
            selenium_.scrollIntoView(selenium_.findElement(mainLocator_));
            dateFieldSpan.findElement(By.cssSelector("button.ui-datepicker-trigger")).click();
            waitForPanelLoad(active);
        }
    }

    /**
     * Closes the date picker menu with the close button
     */
    public void closeDatepicker()
    {
        selenium_.findElement(datepickerLocator_).findElement(By.cssSelector("button.ui-datepicker-close")).click();
    }

    /**
     * Picks the current date in the date picker menu
     *
     * @return  the value in the date picker field after the current date is selected
     */
    public String pickCurrentDate()
    {
        displayDatepicker(true);
        selenium_.waitUntil(ExpectedConditions.elementToBeClickable(By.cssSelector("button.ui-datepicker-current")));
        selenium_.findElement(datepickerLocator_).findElement(By.cssSelector("button.ui-datepicker-current")).click();
        return selenium_.findElement(mainLocator_).findElement(inputLocator_).getAttribute("value");
    }

    /**
     * Picks a specific date in the date picker menu
     *
     * @param dateYear      the year to select
     * @param dateMonth     the month to select (expects 1-12, 1 is January, 12 is December)
     * @param dateDay       the day to select (expects 1-31)
     * @return              the value in the date picker field after the date has been selected
     */
    public String pickSpecificDate(int dateYear, int dateMonth, int dateDay) {
        displayDatepicker(true);
        List<WebElement> yearElements;
        List<Integer> yearOptions = new ArrayList<>();
        int yearIndex = 0;
        do {
            selenium_.findElement(datepickerLocator_).findElement(By.cssSelector("select.ui-datepicker-year")).click();
            yearElements = selenium_.findElementsByCss("select.ui-datepicker-year > option");
            for (WebElement elem : yearElements)
            {
                yearOptions.add(Integer.parseInt(elem.getText()));
                if (elem.getText().equals(String.valueOf(dateYear))) yearIndex = yearElements.indexOf(elem);
            }
            if (dateYear < Integer.parseInt(yearElements.getFirst().getText())) yearElements.getFirst().click();
            else if (dateYear > Integer.parseInt(yearElements.getLast().getText())) yearElements.getLast().click();
            else yearElements.get(yearIndex).click();
        } while (!yearOptions.contains(dateYear));

        selenium_.findElementsByCss("select.ui-datepicker-month > option").get(dateMonth-1).click();

        List<WebElement> dayElements;
        dayElements = selenium_.findElementsByCss("table.ui-datepicker-calendar > tbody > tr > td > a");
        dayElements.get(dateDay-1).click();

        return selenium_.findElement(mainLocator_).findElement(inputLocator_).getText();
    }
}
