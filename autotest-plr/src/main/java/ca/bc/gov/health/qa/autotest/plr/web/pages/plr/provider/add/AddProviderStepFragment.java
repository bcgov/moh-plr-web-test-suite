package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DateMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Base fragment class for the Add Provider Steps (Identifier/Status, Name, Contact, Credential/Expertise)
 */
public class AddProviderStepFragment extends BasicWebPageFragment {

    /**
     * CSS prefix used to locate Effective From date fields within the form.
     */
    public static final String DATE_FIELD_PREFIX_CSS = "span#form\\:effectiveFromDate_";

    /**
     * The lowercase prefix used to construct element IDs for a specific step.
     */
    public String STEP_PREFIX;

    /**
     * Initializes fragment and changes selenium's main locator to header of the step form
     * @param selenium the current SeleniumSession
     * @param stepType the step to be searched for in the form header (to be supplied by subclass)
     */
    public AddProviderStepFragment(SeleniumSession selenium, String stepType)
    {
        super(selenium,
                By.xpath(String.format("//table//tbody//tr//td//div//div//span[contains(text(),'%s')]", stepType)));
    }

    /**
     * Gets the CSS selector for the date field associated with this step.
     * @return the CSS selector string for the date field
     */
    public String getDateFieldCss()
    {
        String dateFieldCss = DATE_FIELD_PREFIX_CSS;
        if (this.STEP_PREFIX.equals("Email") || this.STEP_PREFIX.equals("Cred"))
            dateFieldCss = dateFieldCss.replace("FromDate_", "StartDate");
        return dateFieldCss + this.STEP_PREFIX;
    }

    /**
     * Creates a DateMenu reference for the Effective From Date field
     *
     * @return  a DateMenu reference to the Effective From Date menu
     */
    public DateMenu getEffectiveFromDateMenu()
    {
        if (this.STEP_PREFIX.equals("Email") || this.STEP_PREFIX.equals("Cred")) {
            return new DateMenu(selenium_,
                    By.cssSelector(getDateFieldCss()),
                    String.format("input#form\\:effectiveStartDate%s_input", this.STEP_PREFIX));
        }

        return new DateMenu(selenium_,
                By.cssSelector(getDateFieldCss()),
                String.format("input#form\\:effectiveFromDate_%s_input", this.STEP_PREFIX));
    }

    /**
     * Picks the current date as the Effective From Date.
     *
     * @return  the date picked within the field as a string.
     */
    public String effectiveFromCurrentDate() { return getEffectiveFromDateMenu().pickCurrentDate(); }

    /**
     * Picks a specific date as the Effective From Date.
     *
     * @param effectiveYear     the year of the date to pick.
     * @param effectiveMonth    the month of the date to pick (1-12, 1 being January, 12 being December)
     * @param effectiveDay      the day of the date to pick (expects 1-31)
     * @return                  the date picked within the field as a string.
     */
    public String effectiveFromSpecificDate(int effectiveYear, int effectiveMonth, int effectiveDay)
    {
        return getEffectiveFromDateMenu().pickSpecificDate(effectiveYear, effectiveMonth, effectiveDay);
    }

    /**
     * Types a raw string into the Effective From date input (bypasses date picker UI)
     *
     * @param rawDate a date string to type directly
     */
    public void typeEffectiveFromRaw(String rawDate)
    {
        if (rawDate != null) selenium_.fillFieldByCss(getDateFieldCss() + " > input", rawDate);
    }

    /**
     * Gets the date selected in the Effective From date field.
     *
     * @return  a string of the date picked for the Effective From date field.
     */
    public String getEffectiveFrom() {
        return selenium_.findElementByCss(getDateFieldCss() + " > input").getAttribute("value");
    }

    /**
     * Gets highlighted fields (to be used when an error is expected)
     *
     * @return  a list of strings of each of the fields that are highlighted
     */
    public List<String> getHighlightedFields()
    {
        List<String> highlightedFields = new ArrayList<>();
        By highlightedSelector = By.cssSelector("label.ui-outputlabel.ui-widget.ui-state-error");
        List<WebElement> webElementList = selenium_.findElements(highlightedSelector);
        for (WebElement fieldElement : webElementList) { highlightedFields.add(fieldElement.getText()); }
        return highlightedFields;
    }
}
