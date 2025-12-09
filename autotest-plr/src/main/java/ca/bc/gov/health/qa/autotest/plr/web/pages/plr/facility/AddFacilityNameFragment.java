package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Fragment class for the Facility Name section (second step) when creating a new facility
 */
public class AddFacilityNameFragment extends AddFacilityStepFragment {

    private static final String NAME_FIELD_CSS = "input#form\\:faciName";

    private static final String DESCRIPTION_FIELD_CSS = "input#form\\:faciLongName";

    /**
     * Initializes fragment and changes selenium's main locator to header of the Facility Name form
     *
     * @param selenium      the current SeleniumSession
     */
    public AddFacilityNameFragment(SeleniumSession selenium)
    {
        super(selenium, "Facility");
        STEP_PREFIX = "FacilityName";
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
}
