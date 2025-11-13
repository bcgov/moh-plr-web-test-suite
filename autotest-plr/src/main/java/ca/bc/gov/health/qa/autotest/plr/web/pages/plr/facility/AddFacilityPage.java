package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.common.AlertMessagesFragment;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;
import org.openqa.selenium.By;

import java.util.List;

public class AddFacilityPage extends BasicWebPage {

    public AddFacilityPage(SeleniumSession selenium)
    {
        super(selenium, By.xpath("//div[@id='content']//h2[contains(text(),'Add Facility')]"),
                "Add Facility");
    }

    /**
     * Waits for the error/warning messages to appear
     *
     * @return  the alert message fragment class once the message appears
     */
    public AlertMessagesFragment waitForAlertMessagesFragment()
    {
        AlertMessagesFragment fragment = new AlertMessagesFragment(selenium_);
        fragment.waitForReady();
        return fragment;
    }

    /**
     * Fills the first Identifier section of the Add Facility flow.
     * Requires the state of the add facility page to be in the identifier stage.
     *
     * @param facilityTypePrefix        the first few characters to match when selecting the Facility Type field
     * @param identifierTypePrefix      the first few characters to match when selecting the Identifer Type field
     * @param identifier                string to fill the identifier field with
     * @param effectiveFrom             the date the faciltiy is effective from, as a list of integers [Y, M, D]
     */
    public void fillIdentifierSection(
            String facilityTypePrefix, String identifierTypePrefix, String identifier, List<Integer> effectiveFrom)
    {
        AddFacilityIdFragment identifierFragment = new AddFacilityIdFragment(selenium_);

        identifierFragment.selectFacilityType(facilityTypePrefix);
        identifierFragment.selectIdentifierType(identifierTypePrefix);
        identifierFragment.fillIdentifier(identifier);
        identifierFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));
    }

    /**
     * Advances to the next stage of the Add Facility flow with the Next button.
     */
    public void nextStage()
    {
        selenium_.findElementsByCss("div.ui-wizard-navbar.ui-helper-clearfix > button").getLast().click();
    }

    /**
     * Return to the previous stage of the Add Facility flow with the Back button.
     */
    public void previousStage()
    {
        selenium_.findElementsByCss("div.ui-wizard-navbar.ui-helper-clearfix > button").getFirst().click();
    }
}