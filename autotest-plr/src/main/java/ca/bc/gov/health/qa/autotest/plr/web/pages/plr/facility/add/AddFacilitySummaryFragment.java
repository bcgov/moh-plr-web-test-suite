package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Fragment class for the Facility Summary section (final step) when creating a new facility
 */
public class AddFacilitySummaryFragment extends AddFacilityStepFragment {

    private static final String SUBMIT_BUTTON_CSS = "button#form\\:addProviderSubmit";

    private static final String FACTYPE_FIELD_CSS = "input#form\\:facilityTypeCon";

    private static final String FACNAME_FIELD_CSS = "input#form\\:facilityNameCon";

    private static final String DESCRIPTION_FIELD_CSS = "input#form\\:facilityDescriptionCon";

    private static final String ADDRESS_TABLES_CSS = "table#form\\:panelGr > tbody > tr > td";

    /**
     * Initializes fragment and changes selenium's main locator to header of the Facility Summary form
     *
     * @param selenium      the current SeleniumSession
     */
    public AddFacilitySummaryFragment(SeleniumSession selenium)
    {
        super(selenium, "Facility Summary");
    }

    /**
     * Finds and clicks the submit button to submit the facility.
     *
     * @return      a ViewFacilityPage reference to the newly created facility.
     */
    public ViewFacilityPage clickSubmitButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(SUBMIT_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));

        return new ViewFacilityPage(selenium_);
    }

    /**
     * Gets the Facility Type in the facility summary
     *
     * @return  the facility type as a string
     */
    public String getFacilityType()
    {
        return selenium_.findElement(By.cssSelector(FACTYPE_FIELD_CSS)).getAttribute("value");
    }

    /**
     * Gets the Facility Name in the facility summary
     *
     * @return  the facility name as a string
     */
    public String getFacilityName()
    {
        return selenium_.findElement(By.cssSelector(FACNAME_FIELD_CSS)).getAttribute("value");
    }

    /**
     * Gets the Facility Description in the facility summary
     *
     * @return  the facility description as a string
     */
    public String getDescription()
    {
        return selenium_.findElement(By.cssSelector(DESCRIPTION_FIELD_CSS)).getAttribute("value");
    }

    /**
     * Gets the Civic Address information from the facility summary
     *
     * @return  a list of strings of the civic address info - generally of the form
     *          [Civic Address, Address Lines, City, Province/State, Country]
     */
    public List<String> getCivicAddress()
    {
        final String civicAddressSelector = ADDRESS_TABLES_CSS + ":first-child > table > tbody > tr > td";
        return selenium_.findElementsByCss(civicAddressSelector).stream().map(WebElement::getText).toList();
    }

    /**
     * Gets the Mailing Address information from the facility summary
     *
     * @return  a list of strings of the mailing address info - generally of the form
     *          [Mailing Address, Address Lines, City, Province/State, Country, Postal Code]
     */
    public List<String> getMailingAddress()
    {
        final String mailingAddressSelector = ADDRESS_TABLES_CSS + ":last-child > table > tbody > tr > td";
        return selenium_.findElementsByCss(mailingAddressSelector).stream().map(WebElement::getText).toList();
    }
}
