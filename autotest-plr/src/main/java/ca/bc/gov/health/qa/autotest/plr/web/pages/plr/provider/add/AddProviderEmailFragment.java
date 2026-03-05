package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.RadioMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;

/**
 * Page fragment for the Email step of the Add Provider workflow.
 */
public class AddProviderEmailFragment extends AddProviderStepFragment {

    private static final String EMAIL_ADDRESS_FIELD_CSS = "input#form\\:emailAddress";

    /**
     * Initializes page object and changes selenium's main locator to the Email Address heading
     * @param selenium the selenium session
     */
    public AddProviderEmailFragment(SeleniumSession selenium)
    {
        super(selenium, "Email");
        STEP_PREFIX = "Email";

        enableEmail(true);
    }

    private RadioMenu getEmailChoiceMenu()
    {
        return new RadioMenu(selenium_, By.cssSelector("table#form\\:emailGridRadio"));
    }

    /**
     * Selects whether to add an email address for the provider or not.
     * @param enable true to enable email address fields, false to leave the email address fields blank and disabled
     */
    public void enableEmail(boolean enable)
    {
        if (enable) {
            getEmailChoiceMenu().selectItem("Yes");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithoutClass(
                    By.cssSelector(EMAIL_ADDRESS_FIELD_CSS), "ui-state-disabled"));
        } else {
            getEmailChoiceMenu().selectItem("No");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                    By.cssSelector(EMAIL_ADDRESS_FIELD_CSS), "ui-state-disabled"));
        }
    }

    /**
     * Gets the current value of the Email Address field.
     * @return the current value of the Email Address field
     */
    public String getEmailAddress() { return selenium_.findElementByCss(EMAIL_ADDRESS_FIELD_CSS).getAttribute("value"); }

    /**
     * Fills in the Email Address field.
     * @param emailAddress the email address to fill in
     */
    public void fillEmailAddress(String emailAddress) { selenium_.fillFieldByCss(EMAIL_ADDRESS_FIELD_CSS, emailAddress); }
}
