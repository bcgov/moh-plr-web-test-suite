package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DateMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.RadioMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;

/**
 * Page object for the Demographic Details step of the Add Provider workflow
 */
public class AddProviderDemographicFragment extends AddProviderStepFragment {

    public static final String DOB_PREFIX_CSS = "span#form\\:dob";

    /**
     * Initializes page object and changes selenium's main locator to the Demographic Details heading
     * @param selenium the selenium session
     */
    public AddProviderDemographicFragment(SeleniumSession selenium) {
        super(selenium, "Demographic Details");
        STEP_PREFIX = "demographics";
    }

    /**
     * Creates a DateMenu reference for the Date of Birth field
     * @return a DateMenu reference to the Date of Birth field
     */
    public DateMenu getDateOfBirthMenu()
    {
        return new DateMenu(selenium_, By.cssSelector(DOB_PREFIX_CSS), "input#form\\:dob_input");
    }

    /**
     * Picks the current date as the Date of Birth.
     * @param year the year to set in the Date of Birth field
     * @param month the month to set in the Date of Birth field
     * @param day the day to set in the Date of Birth field
     * @return the date picked within the field as a string.
     */
    public String dateOfBirthSpecificDate(int year, int month, int day)
    {
        return getDateOfBirthMenu().pickSpecificDate(year, month, day);
    }

    /**
     * Gets the current value of the Date of Birth field as a string.
     * @return the current value of the Date of Birth field as a string
     */
    public String getDateOfBirth()
    {
        return selenium_.findElementByCss("input#form\\:dob_input").getAttribute("value");
    }

    /**
     * Gets the CSS selector for the Date of Birth field.
     * @return the CSS selector for the Date of Birth field
     */
    public RadioMenu getGenderMenu() {
        return new RadioMenu(selenium_, By.cssSelector("table#form\\:gender"));
    }

    /**
     * Selects the given gender option in the Gender field and returns the selected value as a string.
     * @param gender the first few characters of the gender option to select
     * @return the selected gender option as a string
     */
    public String selectGender(String gender)
    {
        return getGenderMenu().selectItem(gender);
    }

    /**
     * Grabs the currently selected gender
     * @return the currently selected gender
     */
    public String getGender()
    {
        return getGenderMenu().grabSelectedItem();
    }
}
