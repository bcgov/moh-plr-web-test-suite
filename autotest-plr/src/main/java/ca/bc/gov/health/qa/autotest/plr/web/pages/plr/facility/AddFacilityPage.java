package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.common.AlertMessagesFragment;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
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
     * Overloaded method: Effective From is filled with the current date when not specified.
     *
     * @param facilityTypePrefix    the first few characters to match when selecting the Facility Type field
     * @param identifierTypePrefix  the first few characters to match when selecting the Identifer Type field
     * @param identifier            string to fill the identifier field with
     * @return                      a reference to the identifier fragment on the add facility page
     */
    public AddFacilityIdFragment fillIdentifierSection(
            String facilityTypePrefix, String identifierTypePrefix, String identifier)
    {
        AddFacilityIdFragment identifierFragment = new AddFacilityIdFragment(selenium_);

        identifierFragment.selectFacilityType(facilityTypePrefix);
        identifierFragment.selectIdentifierType(identifierTypePrefix);
        identifierFragment.fillIdentifier(identifier);
        identifierFragment.effectiveFromCurrentDate();
        return identifierFragment;
    }

    /**
     * Fills the first Identifier section of the Add Facility flow.
     * Requires the state of the add facility page to be in the identifier stage.
     *
     * @param facilityTypePrefix        the first few characters to match when selecting the Facility Type field
     * @param identifierTypePrefix      the first few characters to match when selecting the Identifer Type field
     * @param identifier                string to fill the identifier field with
     * @param effectiveFrom             the date the identifier is effective from, as a list of integers [Y, M, D]
     * @return                          a reference to the identifier fragment on the add facility page
     */
    public AddFacilityIdFragment fillIdentifierSection(
            String facilityTypePrefix, String identifierTypePrefix, String identifier, List<Integer> effectiveFrom)
    {
        AddFacilityIdFragment identifierFragment = new AddFacilityIdFragment(selenium_);

        identifierFragment.selectFacilityType(facilityTypePrefix);
        identifierFragment.selectIdentifierType(identifierTypePrefix);
        identifierFragment.fillIdentifier(identifier);
        identifierFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return identifierFragment;
    }

    /**
     * Fills the second Facility Name section of the Add Facility flow.
     * Requires the state of the add facility page to be in the name stage.
     *
     * @param name              string to fill the name field with
     * @param description       string to fill the description field with
     * @param effectiveFrom     the date the facility name is effective from, as a list of integers [Y, M, D]
     * @return                  a reference to the name fragment on the add facility page
     */
    public AddFacilityNameFragment fillFacilitySection(String name, String description, List<Integer> effectiveFrom)
    {
        AddFacilityNameFragment nameFragment = new AddFacilityNameFragment(selenium_);

        nameFragment.fillName(name);
        nameFragment.fillDescription(description);
        nameFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return nameFragment;
    }

    /**
     * Fills the second Facility Name section of the Add Facility flow.
     * Requires the state of the add facility page to be in the name stage.
     * Overloaded method: Effective From is filled with the current date when not specified.
     *
     * @param name              string to fill the name field with
     * @param description       string to fill the description field with
     * @return                  a reference to the name fragment on the add facility page
     */
    public AddFacilityNameFragment fillFacilitySection(String name, String description)
    {
        AddFacilityNameFragment nameFragment = new AddFacilityNameFragment(selenium_);

        nameFragment.fillName(name);
        nameFragment.fillDescription(description);
        nameFragment.effectiveFromCurrentDate();

        return nameFragment;
    }

    /**
     * Waits for a step in the Add Facility flow to be available
     *
     * @param step      the title of the step to be locating (header of the div form, e.g. Identifier, Facility, etc.)
     * @param next      whether we are waiting for the step to be available (true) or the step to be unavailable (false)
     */
    public void waitForAddFacilityStep(String step, boolean next)
    {
        By stepLocator = By.xpath(String.format("//table//tbody//tr//td//div//div//span[contains(text(),'%s')]", step));
        if (next)
        {
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                    stepLocator, "ui-panel-title"));
        }
        else selenium_.waitUntil(SeleniumExpectedConditions.absenceOfElementLocated(stepLocator));
    }

    /**
     * Advances to the next stage of the Add Facility flow with the Next button.
     *
     * @param currentState     the title of the previous form in the Add Facility flow.
     * @param expectedError    whether clicking next is expected to return an error (true) or not (false)
     */
    public void clickNext(String currentState, boolean expectedError)
    {
        selenium_.findElementsByCss("div.ui-wizard-navbar.ui-helper-clearfix > button").getLast().click();

        if (!expectedError) waitForAddFacilityStep(currentState, false);
    }

    /**
     * Return to the previous stage of the Add Facility flow with the Back button.
     * Requires the Back button to be visible/displayed.
     *
     * @param currentState      the title of the previous form in the Add Facility flow.
     * @param expectedError     whether clicking next is expected to return an error (true) or not (false)
     */
    public void clickBack(String currentState, boolean expectedError)
    {
        selenium_.findElementsByCss("div.ui-wizard-navbar.ui-helper-clearfix > button").getFirst().click();

        if (!expectedError) waitForAddFacilityStep(currentState, false);
    }

    public String getStep()
    {
        return selenium_.findElementByCss("div.ui-wizard.ui-widget > ul > li.ui-state-highlight").getText();
    }
}