package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.common.AlertMessagesFragment;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.*;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Page object for the Add Provider page
 */
public class AddProviderPage extends BasicWebPage {

    final String FORM_TITLE_XPATH = "//div//table//tbody//tr//td//div//div//span[contains(text(),'%s')]";

    private final ProviderType providerType;

    /**
     * Initializes page object and changes selenium's main locator to the Add Provider Heading
     * @param selenium the selenium session
     */
    public AddProviderPage(SeleniumSession selenium, String expectedHeader) {
        super(selenium,
                By.xpath("//div[@id='content']//h2[contains(text(),'Add Provider')]"),
                "Add Provider - " + expectedHeader);
        this.providerType = switch (expectedHeader) {
            case "(BC Practitioner)" -> ProviderType.BC_PRACTITIONER;
            case "(OOP Practitioner)" -> ProviderType.OOP_PRACTITIONER;
            case "(Organization)" -> ProviderType.ORGANIZATION;
            default -> throw new IllegalArgumentException("Unexpected provider type: " + expectedHeader);
        };
    }

    /**
     * Changes the provider type in the Add Provider page by clicking the corresponding option in the provider type menu
     * @param providerType the provider type to change to
     * @return a new AddProviderPage object with the provider type changed if the provider type is different from the current one,
     *         and the same AddProviderPage object if the provider type is the same as the current one
     */
    public AddProviderPage changeProviderType(ProviderType providerType)
    {
        if (this.providerType != providerType) {
            List<WebElement> providerMenu = selenium_.findElements(
                    By.cssSelector("div#headerForm\\:subMenuPanelHolder > div > div > menu > li"));
            String expectedHeader = switch (providerType) {
                case BC_PRACTITIONER -> "(BC Practitioner)";
                case OOP_PRACTITIONER -> "(OOP Practitioner)";
                case ORGANIZATION -> "(Organization)";
            };
            providerMenu.get(providerType.ordinal()).click();
            selenium_.waitUntil(SeleniumExpectedConditions.pageToBeReady());
            return new AddProviderPage(selenium_, expectedHeader);
        }
        return this;
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
     * Fills the identifier form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param roleType the provider role type to select in the form, or null to not select any provider role type.
     *                 will be explicitly cast to either ProviderRoleType or OrganizationalProviderRoleType based on the provider type of the page
     * @param identifierType the identifier type to select in the form, or null to not select any identifier type
     * @param identifier the identifier to fill in the form, or null to not fill any identifier
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [day, month, year], or null to not fill an effective from date
     * @return the AddProviderIdFragment object after filling the form with the provided information
     */
    public AddProviderIdFragment fillIdentifier(Object roleType, HdsType hdsType, String hdsSubType,
                                                String identifierType, String identifier, List<Integer> effectiveFrom)
    {
        AddProviderIdFragment fragment = new AddProviderIdFragment(selenium_, providerType);

        if (roleType != null) {
            if (providerType.equals(ProviderType.ORGANIZATION))
                fragment.selectProviderRoleType((OrganizationalProviderRoleType) roleType);
            else fragment.selectProviderRoleType((ProviderRoleType) roleType);

            WebElement idType = selenium_.findElement(By.cssSelector("div#form\\:identifierType"));
            selenium_.waitUntil(ExpectedConditions.stalenessOf(idType));

            if (hdsType != null && (roleType.equals(OrganizationalProviderRoleType.HDS))) {
                selenium_.waitUntil(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#form\\:hdsTypeId")));

                fragment.selectHdsType(hdsType);
                if (hdsSubType != null) fragment.selectHdsSubType(hdsSubType);
            }
        }
        if (identifierType != null) fragment.selectIdentifierType(identifierType);
        if (identifier != null) fragment.fillIdentifier(identifier);
        if (effectiveFrom != null)
            fragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return fragment;
    }

    /**
     * Fills the identifier form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param roleType the provider role type to select in the form, or null to not select any provider role type.
     *                 will be explicitly cast to either ProviderRoleType or OrganizationalProviderRoleType based on the provider type of the page
     * @param identifierType the identifier type to select in the form, or null to not select any identifier type
     * @param identifier the identifier to fill in the form, or null to not fill any identifier
     * @return the AddProviderIdFragment object after filling the form with the provided information
     */
    public AddProviderIdFragment fillIdentifier(Object roleType, HdsType hdsType, String hdsSubType,
                                                String identifierType, String identifier)
    {
        AddProviderIdFragment fragment = new AddProviderIdFragment(selenium_, providerType);

        if (roleType != null) {
            if (providerType.equals(ProviderType.ORGANIZATION))
                fragment.selectProviderRoleType((OrganizationalProviderRoleType) roleType);
            else fragment.selectProviderRoleType((ProviderRoleType) roleType);

            WebElement idType = selenium_.findElement(By.cssSelector("div#form\\:identifierType"));
            selenium_.waitUntil(ExpectedConditions.stalenessOf(idType));

            if (hdsType != null && (roleType.equals(OrganizationalProviderRoleType.HDS))) {
                selenium_.waitUntil(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#form\\:hdsTypeId")));

                fragment.selectHdsType(hdsType);
                if (hdsSubType != null) fragment.selectHdsSubType(hdsSubType);
            }
        }
        if (identifierType != null) fragment.selectIdentifierType(identifierType);
        if (identifier != null) fragment.fillIdentifier(identifier);
        fragment.effectiveFromCurrentDate();

        return fragment;
    }

    /**
     * Fills the status form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param statusClassCode the status class code to select in the form, or null to not select any status class code
     * @param statusCode the status code to select in the form, or null to not select any status code
     * @param statusReasonCode the status reason code to select in the form, or null to not select any status reason code
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [day, month, year], or null to not fill an effective from date
     * @return the AddProviderStatusFragment object after filling the form with the provided information
     */
    public AddProviderStatusFragment fillStatus(String statusClassCode, StatusCodeOption statusCode, StatusReasonCodeOption statusReasonCode, List<Integer> effectiveFrom)
    {
        AddProviderStatusFragment fragment = new AddProviderStatusFragment(selenium_);

        if (statusClassCode != null) fragment.selectStatusClassCode(statusClassCode);
        if (statusCode != null) fragment.selectStatusCode(statusCode.getText());
        if (statusReasonCode != null) fragment.selectStatusReasonCode(statusReasonCode.getText());
        if (effectiveFrom != null)
            fragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return fragment;
    }

    /**
     * Fills the status form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param statusClassCode the status class code to select in the form, or null to not select any status class code
     * @param statusCode the status code to select in the form, or null to not select any status code
     * @param statusReasonCode the status reason code to select in the form, or null to not select any status reason code
     * @return the AddProviderStatusFragment object after filling the form with the provided information
     */
    public AddProviderStatusFragment fillStatus(String statusClassCode, StatusCodeOption statusCode, StatusReasonCodeOption statusReasonCode)
    {
        AddProviderStatusFragment fragment = new AddProviderStatusFragment(selenium_);

        if (statusClassCode != null) fragment.selectStatusClassCode(statusClassCode);
        if (statusCode != null) fragment.selectStatusCode(statusCode.getText());
        if (statusReasonCode != null) fragment.selectStatusReasonCode(statusReasonCode.getText());
        fragment.effectiveFromCurrentDate();

        return fragment;
    }

    /**
     * Fills the personal information form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param prefix the prefix to fill in the form, or null to not fill a prefix
     * @param firstName the first name to fill in the form, or null to not fill a first name
     * @param secondName the second name to fill in the form, or null to not fill a second name
     * @param thirdName the third name to fill in the form, or null to not fill a third name
     * @param surname the surname to fill in the form, or null to not fill a surname
     * @return the AddProviderPIFragment object after filling the form with the provided information
     */
    public AddProviderPIFragment fillPI(String prefix, String firstName, String secondName, String thirdName, String surname)
    {
        AddProviderPIFragment fragment = new AddProviderPIFragment(selenium_);

        if (prefix != null) fragment.fillPrefix(prefix);
        if (firstName != null) fragment.fillFirstName(firstName);
        if (secondName != null) fragment.fillSecondName(secondName);
        if (thirdName != null) fragment.fillThirdName(thirdName);
        if (surname != null) fragment.fillSurname(surname);
        fragment.effectiveFromCurrentDate();

        return fragment;
    }

    /**
     * Fills the personal information form in the Add Provider flow with the provided information, waiting for the form to be ready before filling.
     * @param prefix the prefix to fill in the form, or null to not fill a prefix
     * @param firstName the first name to fill in the form, or null to not fill a first name
     * @param secondName the second name to fill in the form, or null to not fill a second name
     * @param thirdName the third name to fill in the form, or null to not fill a third name
     * @param surname the surname to fill in the form, or null to not fill a surname
     * @param effectiveFrom the effective from date to fill in the form as a list of integers in the format
     *                      [day, month, year], or null to not fill an effective from date
     * @return the AddProviderPIFragment object after filling the form with the provided information
     */
    public AddProviderPIFragment fillPI(String prefix, String firstName, String secondName, String thirdName, String surname, List<Integer> effectiveFrom)
    {
        AddProviderPIFragment fragment = new AddProviderPIFragment(selenium_);

        if (prefix != null) fragment.fillPrefix(prefix);
        if (firstName != null) fragment.fillFirstName(firstName);
        if (secondName != null) fragment.fillSecondName(secondName);
        if (thirdName != null) fragment.fillThirdName(thirdName);
        if (surname != null) fragment.fillSurname(surname);
        if (effectiveFrom != null)
            fragment.effectiveFromSpecificDate(effectiveFrom.get(0), effectiveFrom.get(1), effectiveFrom.get(2));

        return fragment;
    }

    /**
     * Waits for a step in the Add Provider flow to be available
     *
     * @param step      the title of the step to be locating (header of the div form, e.g. Identifier, Status, etc.)
     * @param next      whether we are waiting for the step to be available (true) or the step to be unavailable (false)
     */
    public void waitForAddProviderStep(String step, boolean next)
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
     * Waits for a widget appearing after attempting to pass an Add Provider Step to be visible and interactable
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
     * Advances to the next stage of the Add Provider flow with the Next button.
     *
     * @param currentState     the title of the previous form in the Add Provider flow.
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
            if (errorWidget.isEmpty()) waitForAddProviderStep(currentState, false);
            else {
                try {
                    waitForWidgetVisibility(errorWidget);
                } catch (IllegalStateException e) {
                    throw new IllegalStateException("Search for widget " + errorWidget + " timed out");
                }
            }
        } else { waitForAddProviderStep(currentState, true); }
    }

    /**
     * Return to the previous stage of the Add Provider flow with the Back button.
     * Requires the Back button to be visible/displayed.
     *
     * @param currentState      the title of the previous form in the Add Provider flow.
     * @param expectedError     whether clicking next is expected to return an error (true) or not (false)
     */
    public void clickBack(String currentState, boolean expectedError)
    {
        selenium_.findElementsByCss("div.ui-wizard-navbar.ui-helper-clearfix > button").getFirst().click();

        if (!expectedError) waitForAddProviderStep(currentState, false);
    }

    /**
     * Gets the currently highlighted step in the Add Provider flow
     *
     * @return  a string of the highlighted step
     */
    public String getStep()
    {
        return selenium_.findElementByCss("div.ui-wizard.ui-widget > ul > li.ui-state-highlight").getText();
    }
}
