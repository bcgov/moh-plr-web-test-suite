package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.RadioMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AddProviderPhoneFragment extends AddProviderStepFragment {

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
                    By.cssSelector("input#form\\:areaCode"), "disabled"));
        } else {
            getPhoneChoiceMenu().selectItem("No");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                    By.cssSelector("input#form\\:areaCode"), "disabled"));
        }
    }
}
