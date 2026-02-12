package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.RadioMenu;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

public class AddProviderPhoneFragment extends AddProviderStepFragment {

    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final String AREA_CODE_FIELD_CSS = "input#form\\:areaCode";

    private static final String PHONE_NUMBER_FIELD_CSS = "input#form\\:phoneNumber";

    private static final String EXTENSION_FIELD_CSS = "input#form\\:extensionNumber";

    /**
     * Initializes page object and changes selenium's main locator to the Phone Number heading
     * @param selenium the selenium session
     */
    public AddProviderPhoneFragment(SeleniumSession selenium)
    {
        super(selenium, "Phone Number");
        STEP_PREFIX = "phoneNum";

        enablePhone(true);
    }

    private RadioMenu getPhoneChoiceMenu()
    {
        return new RadioMenu(selenium_, By.cssSelector("table#form\\:phoneGridRadio"));
    }

    /**
     * Selects whether to add a phone number for the provider or not.
     * @param enable true to enable phone number fields, false to leave the phone number fields blank and disabled
     */
    public void enablePhone(boolean enable)
    {
        if (enable) {
            getPhoneChoiceMenu().selectItem("Yes");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithoutClass(
                    By.cssSelector("input#form\\:areaCode"), "ui-state-disabled"));
        } else {
            getPhoneChoiceMenu().selectItem("No");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                    By.cssSelector("input#form\\:areaCode"), "ui-state-disabled"));
        }
    }

    /**
     * Gets the current value of the Area Code field.
     * @return the current value of the Area Code field
     */
    public String getAreaCode() { return selenium_.findElementByCss(AREA_CODE_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the current value of the Phone Number field.
     * @return the current value of the Phone Number field
     */
    public String getPhoneNumber() { return selenium_.findElementByCss(PHONE_NUMBER_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the current value of the Extension field.
     * @return the current value of the Extension field
     */
    public String getExtension() { return selenium_.findElementByCss(EXTENSION_FIELD_CSS).getAttribute("value"); }

    /**
     * Fills in the Area Code field.
     * @param areaCode the area code to fill in
     */
    public void fillAreaCode(String areaCode) { selenium_.fillFieldByCss(AREA_CODE_FIELD_CSS, areaCode); }

    /**
     * Fills in the Phone Number field.
     * @param phoneNumber the phone number to fill in
     */
    public void fillPhoneNumber(String phoneNumber) { selenium_.fillFieldByCss(PHONE_NUMBER_FIELD_CSS, phoneNumber); }

    /**
     * Fills in the Extension field.
     * @param extension the extension to fill in
     */
    public void fillExtension(String extension) { selenium_.fillFieldByCss(EXTENSION_FIELD_CSS, extension); }
}
