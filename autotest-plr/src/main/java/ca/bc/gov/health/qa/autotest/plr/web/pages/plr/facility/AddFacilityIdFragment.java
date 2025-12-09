package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

import java.util.List;

import org.openqa.selenium.By;

/**
 * Fragment class for the Identifier section (first step) when creating a new facility
 */
public class AddFacilityIdFragment extends AddFacilityStepFragment {

    private static final String FACTYPE_FIELD_CSS = "label#form\\:facilityType_label";

    private static final String IDTYPE_FIELD_CSS = "label#form\\:identifierType_label";

    private static final String IDENTIFIER_FIELD_CSS = "input#form\\:identifier";

    /**
     * Initializes fragment and changes selenium's main locator to header of the Identifier form
     *
     * @param selenium      the current SeleniumSession
     */
    public AddFacilityIdFragment(SeleniumSession selenium)
    {
        super(selenium, "Identifier");
        STEP_PREFIX = "identifier";

    }

    /**
     * Constructs a DropDownMenu component for the Facility Type
     *
     * @return  a DropDownMenu component for the Facility Type
     */
    private DropDownMenu getFacilityTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector(FACTYPE_FIELD_CSS),
                By.cssSelector("div#form\\:facilityType_panel")
        );
    }

    /**
     * Gets the value currently selected in the Facility Type field
     *
     * @return  a string of the value currently selected as Facility Type
     */
    public String getFacilityType() { return getFacilityTypeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Facility Type dropdown
     *
     * @return  a list of strings of all facility type options
     */
    public List<String> getFacilityTypeOptions()
    {
        DropDownMenu menu = getFacilityTypeMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Constructs a DropDownMenu component for the Identifier Type
     *
     * @return  a DropDownMenu component for the Identifier Type
     */
    private DropDownMenu getIdentifierTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector(IDTYPE_FIELD_CSS),
                By.cssSelector("div#form\\:identifierType_panel")
        );
    }

    /**
     * Selects the facility type in the Facility Type dropdown based on a prefix
     *
     * @param facilityTypePrefix    the first few characters to match when selecting the menu option
     * @return  A string of the full matched facility type
     */
    public String selectFacilityType(String facilityTypePrefix)
    {
        return getFacilityTypeMenu().selectItem(facilityTypePrefix);
    }

    /**
     * Selects the identifier type in the Identifier Type dropdown based on a prefix
     *
     * @param identifierTypePrefix    the first few characters to match when selecting the menu option
     * @return  A string of the full matched identifier type
     */
    public String selectIdentifierType(String identifierTypePrefix)
    {
        return getIdentifierTypeMenu().selectItem(identifierTypePrefix);
    }

    /**
     * Fills the Identifier field
     *
     * @param identifier  the string to fill the Identifier field with
     */
    public void fillIdentifier(String identifier)
    {
        if (identifier != null) selenium_.fillFieldByCss(IDENTIFIER_FIELD_CSS, identifier);
    }
}
