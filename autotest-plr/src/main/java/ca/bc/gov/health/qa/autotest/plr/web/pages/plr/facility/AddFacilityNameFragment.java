package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DateMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.openqa.selenium.By;

/**
 * Fragment class for the Facility Name section (second step) when creating a new facility
 */
public class AddFacilityNameFragment extends BasicWebPageFragment {

    private static final String NAME_FIELD_CSS = "input#form\\:faciName";

    private static final String DESCRIPTION_FIELD_CSS = "input#form\\:faciLongName";

    private static final String DATE_FIELD_CSS = "span#form\\:effectiveFromDate_FacilityName";

    /**
     * Initializes fragment and changes selenium's main locator to header of the Facility Name form
     *
     * @param selenium      the current SeleniumSession
     */
    public AddFacilityNameFragment(SeleniumSession selenium)
    {
        super(selenium, By.xpath("//table//tbody//tr//td//div//div//span[contains(text(),'Facility')]"));
    }

    /**
     * Fills the name field
     *
     * @param name  the string to fill the Name field with
     */
    public void fillName(String name)
    {
        if (name != null) selenium_.fillFieldByCss(NAME_FIELD_CSS, name);
    }

    /**
     * Fills the description field
     *
     * @param desc  the string to fill the Description field with
     */
    public void fillDescription(String desc)
    {
        if (desc != null) selenium_.fillFieldByCss(DESCRIPTION_FIELD_CSS, desc);
    }

    /**
     * Creates a DateMenu reference for the Effective From Date field
     *
     * @return  a DateMenu reference to the Effective From Date menu
     */
    public DateMenu getEffectiveFromDateMenu()
    {
        return new DateMenu(selenium_, By.cssSelector(DATE_FIELD_CSS), "FacilityName");
    }

    /**
     * Picks the current date as the Effective From Date.
     *
     * @return  the date picked within the field as a string.
     */
    public String effectiveFromCurrentDate()
    {
        return getEffectiveFromDateMenu().pickCurrentDate();
    }

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
}
