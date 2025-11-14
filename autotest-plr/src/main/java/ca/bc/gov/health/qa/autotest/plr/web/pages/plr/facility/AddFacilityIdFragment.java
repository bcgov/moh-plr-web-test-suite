package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DateMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class AddFacilityIdFragment extends BasicWebPageFragment {

    private static final String FACTYPE_FIELD_CSS = "label#form\\:facilityType_label";

    private static final String IDTYPE_FIELD_CSS = "label#form\\:identifierType_label";

    private static final String IDENTIFIER_FIELD_CSS = "input#form\\:identifier";

    private static final String DATE_FIELD_CSS = "span#form\\:effectiveFromDate_identifier";

    public AddFacilityIdFragment(SeleniumSession selenium)
    {
        super(selenium, By.xpath("//table//tbody//tr//td//div//div//span[contains(text(),'Identifier')]"));
    }

    /**
     * Constructs a DropDownMenu component for the Facility Type
     *
     * @return  a DropDownMenu component for the Facility Type
     */
    public DropDownMenu getFacilityTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector(FACTYPE_FIELD_CSS),
                By.cssSelector("div#form\\:facilityType_panel")
        );
    }

    /**
     * Constructs a DropDownMenu component for the Identifier Type
     *
     * @return  a DropDownMenu component for the Identifier Type
     */
    public DropDownMenu getIdentifierTypeMenu()
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

    /**
     * Creates a DateMenu reference for the Effective From Date field
     *
     * @return  a DateMenu reference to the Effective From Date menu
     */
    public DateMenu getEffectiveFromDateMenu()
    {
        return new DateMenu(selenium_, By.cssSelector(DATE_FIELD_CSS), "identifier");
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

    public List<String> getHighlightedFields()
    {
        List<String> highlightedFields = new ArrayList<>();
        By highlightedSelector = By.cssSelector("label.ui-outputlabel.ui-widget.ui-state-error");
        List<WebElement> webElementList = selenium_.findElements(highlightedSelector);
        for (WebElement fieldElement : webElementList) { highlightedFields.add(fieldElement.getText()); }
        return highlightedFields;
    }
}
