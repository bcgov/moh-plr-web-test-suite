package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.common.AlertMessagesFragment;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * A page object class for the Add Facility page.
 */
public class AddFacilityPage extends BasicWebPage {

    final String FORM_TITLE_XPATH = "//div//table//tbody//tr//td//div//div//span[contains(text(),'%s')]";

    /**
     * Initializes page object and changes selenium's main locator to Add Facility heading
     *
     * @param selenium  the current SeleniumSession
     */
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
     * Fills the third Address section of the Add Facility flow.
     * Requires the state of the add facility page to be in the address stage.
     * Overloaded method: Effective From is filled with the current date when not specified.
     *
     * @param addressLines      list of strings to fill the address lines fields with (expects list of 3 strings)
     * @param cityField         the string to fill the City field with (with autocomplete)
     * @param cityPrefix        the first few characters to match when selecting an autocompleted City option.
     * @param postalCode        string to fill the postal code field with
     * @return                  a reference to the address fragment on the add facility page
     */
    public AddFacilityAddressFragment fillAddressSection(
            List<String> addressLines, String cityField, String cityPrefix, String postalCode)
    {
        AddFacilityAddressFragment addressFragment = new AddFacilityAddressFragment(selenium_);

        addressFragment.fillAddressLine1(addressLines.get(0));
        addressFragment.fillAddressLine2(addressLines.get(1));
        addressFragment.fillAddressLine3(addressLines.get(2));
        addressFragment.fillCity(cityField, cityPrefix);
        addressFragment.fillPostalCode(postalCode);
        addressFragment.effectiveFromCurrentDate();

        return addressFragment;
    }

    /**
     * Fills the third Address section of the Add Facility flow.
     * Requires the state of the add facility page to be in the address stage.
     *
     * @param addressLines      list of strings to fill the address lines fields with (expects list of 3 strings)
     * @param cityField         the string to fill the City field with (with autocomplete)
     * @param cityPrefix        the first few characters to match when selecting an autocompleted City option.
     * @param postalCode        string to fill the postal code field with
     * @param effectiveFrom     the date the facility name is effective from, as a list of integers [Y, M, D]
     * @return                  a reference to the address fragment on the add facility page
     */
    public AddFacilityAddressFragment fillAddressSection(
            List<String> addressLines, String cityField, String cityPrefix,
            String postalCode, List<Integer> effectiveFrom)
    {
        AddFacilityAddressFragment addressFragment = new AddFacilityAddressFragment(selenium_);

        addressFragment.fillAddressLine1(addressLines.get(0));
        addressFragment.fillAddressLine2(addressLines.get(1));
        addressFragment.fillAddressLine3(addressLines.get(2));
        addressFragment.fillCity(cityField, cityPrefix);
        addressFragment.fillPostalCode(postalCode);

        addressFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return addressFragment;
    }

    /**
     * Fills the third Address section of the Add Facility flow.
     * Requires the state of the add facility page to be in the address stage.
     * Overloaded method: This method autocompletes all fields with Address Autocomplete, and picks the current date for
     *                    Effective From.
     *
     * @param addressAutocompleteField      the string to fill the address autocomplete field (with autocomplete)
     * @param addressAutocompletePrefix     the first few characters to match when selecting an
     *                                      Address Autocomplete option.
     * @return                              a reference to the address fragment on the add facility page
     */
    public AddFacilityAddressFragment fillAddressSection(
            String addressAutocompleteField, String addressAutocompletePrefix)
    {
        AddFacilityAddressFragment addressFragment = new AddFacilityAddressFragment(selenium_);

        addressFragment.fillAddressAutocomplete(addressAutocompleteField, addressAutocompletePrefix);

        addressFragment.effectiveFromCurrentDate();

        return addressFragment;
    }

    /**
     * Fills the third Address section of the Add Facility flow.
     * Requires the state of the add facility page to be in the address stage.
     * Overloaded method: This method autocompletes all fields with Address Autocomplete.
     *
     * @param addressAutocompleteField      the string to fill the address autocomplete field (with autocomplete)
     * @param addressAutocompletePrefix     the first few characters to match when selecting an
     *                                      Address Autocomplete option.
     * @param effectiveFrom                 the date the facility name is effective from, as a list of integers [Y, M, D]
     * @return                              a reference to the address fragment on the add facility page
     */
    public AddFacilityAddressFragment fillAddressSection(
            String addressAutocompleteField, String addressAutocompletePrefix, List<Integer> effectiveFrom)
    {
        AddFacilityAddressFragment addressFragment = new AddFacilityAddressFragment(selenium_);

        addressFragment.fillAddressAutocomplete(addressAutocompleteField, addressAutocompletePrefix);

        addressFragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return addressFragment;
    }

    /**
     * Waits for the summary page to load and returns a reference to its fragment class
     *
     * @return  an AddFacilitySummaryFragment reference to the summary page
     */
    public AddFacilitySummaryFragment getFacilitySummary()
    {
        waitForAddFacilityStep("Facility Summary", true);
        return new AddFacilitySummaryFragment(selenium_);
    }

    /**
     * Waits for a step in the Add Facility flow to be available
     *
     * @param step      the title of the step to be locating (header of the div form, e.g. Identifier, Facility, etc.)
     * @param next      whether we are waiting for the step to be available (true) or the step to be unavailable (false)
     */
    public void waitForAddFacilityStep(String step, boolean next)
    {
        By stepLocator = By.xpath(String.format(FORM_TITLE_XPATH, step));
        if (next)
        {
            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(stepLocator,
                    "ui-panel-title"));
        }
        else selenium_.waitUntil(SeleniumExpectedConditions.absenceOfElementLocated(stepLocator));
    }

    /**
     * Waits for a widget appearing after attempting to pass an Add Facility Step to be visible and interactable
     *
     * @param widget                    a unique portion in the header of the widget to be located
     * @throws IllegalStateException    when searching for the desired widget times out (most likely not found)
     */
    public void waitForWidgetVisibility(String widget)
    {
        By widgetLocator = By.xpath(String.format(
                "//form//div//div//div//div//span[contains(text(), '%s')]//parent::div//parent::div", widget));

        try
        {
            selenium_.waitUntil(ExpectedConditions.attributeToBe(widgetLocator, "aria-hidden", "false"));
        }
        catch (org.openqa.selenium.TimeoutException e) { throw new IllegalStateException(e.getMessage()); }
        catch (org.openqa.selenium.StaleElementReferenceException e) { waitForWidgetVisibility(widget); }

    }

    /**
     * Advances to the next stage of the Add Facility flow with the Next button.
     *
     * @param currentState     the title of the previous form in the Add Facility flow.
     * @param errorWidget      the widget to wait for visibility for in the event of an error.
     *                         specify the widget if there will be an error with a widget, set to the empty string if
     *                         no error is expected, and set to null if an error with no widget is expected
     */
    public void clickNext(String currentState, String errorWidget)
    {
        WebElement stepTitle = selenium_.findElement(By.xpath(String.format(FORM_TITLE_XPATH, currentState)));

        selenium_.findElementsByCss("div.ui-wizard-navbar.ui-helper-clearfix > button").getLast().click();

        selenium_.waitUntil(ExpectedConditions.stalenessOf(stepTitle));

        if (errorWidget != null) {
            if (errorWidget.isEmpty()) waitForAddFacilityStep(currentState, false);
            else {
                try {
                    waitForWidgetVisibility(errorWidget);
                } catch (IllegalStateException e) {
                    throw new IllegalStateException("Search for widget" + errorWidget + " timed out");
                }
            }
        } else { waitForAddFacilityStep(currentState, true); }
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

    /**
     * Gets the currently highlighted step in the Add Facility flow
     *
     * @return  a string of the highlighted step
     */
    public String getStep()
    {
        return selenium_.findElementByCss("div.ui-wizard.ui-widget > ul > li.ui-state-highlight").getText();
    }

    /**
     * Gets the title of the form for the current step in the Add Facility flow
     *
     * @return  a string of the highlighted step
     */
    public String getStepTitle()
    {
        return selenium_.findElementByCss(
                "div.ui-panel > div > table > tbody > tr > td > div > div > span").getText();
    }
}