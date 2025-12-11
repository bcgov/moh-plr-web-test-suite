package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.search;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchSectionFragment;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment class for the Search by Identifier section when searching by facility
 */
public class SearchFacilityIdFragment extends SearchSectionFragment
{

    private static final String FACILITY_ID_FIELD_CSS = "input#accordian\\:searchByIdForm\\:identifier";
    private static final String ID_TYPE_FIELD_CSS = "label#accordian\\:searchByIdForm\\:identifierType_label";

    /**
     * Initializes fragment and changes selenium's main locator to the search by identifier tab container
     *
     * @param selenium  The current SeleniumSession
     */
    public SearchFacilityIdFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#accordian\\:searchByIdTab"));
    }

    /**
     * Finds and clicks the search button to submit the search by identifier query
     */
    public void clickSearchButton()
    {
        WebElement button = selenium_.findElement(mainLocator_)
                .findElement(By.cssSelector("button[type='submit']"));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
    }

    /**
     * Constructs the DropDownMenu component for the Identifier Type
     *
     * @return  a DropDownMenu component for the Identifier Type
     */
    public DropDownMenu getIdentifierTypeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector(ID_TYPE_FIELD_CSS),
                By.cssSelector("div#accordian\\:searchByIdForm\\:identifierType_panel"));
    }

    /**
     * Selects the identifier type in the Facility Identifier Type dropdown based on a prefix
     *
     * @param identifierTypePrefix  the first few characters to match when selecting the menu option
     * @return                      a string of the full matched identifier type
     */
    public String selectIdentifierType(String identifierTypePrefix)
    {
        return getIdentifierTypeMenu().selectItem(identifierTypePrefix);
    }

    /**
     * Fills the Facility Identifier field
     *
     * @param facilityId    the string to fill the Facility Identifier field with
     */
    public void fillFacilityId(String facilityId)
    {
        selenium_.fillFieldByCss(FACILITY_ID_FIELD_CSS, facilityId);
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

    /**
     * Ensures certain details of the identifier tab exist (instructions about mandatory field,
     * labels for each possible field/attribute, and search button)
     *
     * @return  a list of string text from each detail of the identifier tab [instruction, (fields), search button]
     */
    public List<String> getIdentifierTab()
    {
        List<String> tabDetails = new ArrayList<>();

        String mandatoryInstruction = selenium_.findElementByCss(
                "form#accordian\\:searchByIdForm > table > tbody > tr > td[colspan]").getText();
        tabDetails.add(mandatoryInstruction);

        List<WebElement> fieldList = selenium_.findElementsByCss(
                "form#accordian\\:searchByIdForm label.ui-outputlabel.ui-widget");
        for (WebElement fieldElement : fieldList) { tabDetails.add(fieldElement.getText()); }

        String searchButton = selenium_.findElement(mainLocator_)
                .findElement(By.cssSelector("button[type='submit']")).getText();
        tabDetails.add(searchButton);

        return tabDetails;
    }

    /**
     * Gets the values currently input into each field (pre-search).
     *
     * @return  a list of strings of each field's current value, ordered [facility identifier type, facility identifier]
     */
    public List<String> getCurrentFieldValues()
    {
        String identifierField = "";
        String identifierTypeField = "";
        identifierField += selenium_.findElementByCss(FACILITY_ID_FIELD_CSS).getAttribute("value");
        identifierTypeField += selenium_.findElementByCss(ID_TYPE_FIELD_CSS).getText();
        return Arrays.asList(identifierTypeField, identifierField);
    }
}
