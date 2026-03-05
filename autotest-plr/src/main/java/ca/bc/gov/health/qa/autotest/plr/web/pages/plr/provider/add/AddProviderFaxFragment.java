package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.RadioMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;

/**
 * Page fragment for the Fax Number step of the Add Provider workflow.
 */
public class AddProviderFaxFragment extends AddProviderStepFragment {

    private static final String AREA_CODE_FIELD_CSS = "input#form\\:areaCodeFax";

    private static final String FAX_NUMBER_FIELD_CSS = "input#form\\:faxNumber";

    /**
     * Initializes page object and changes selenium's main locator to the Fax Number heading
     * @param selenium the selenium session
     */
    public AddProviderFaxFragment(SeleniumSession selenium)
    {
        super(selenium, "Fax Number");
        STEP_PREFIX = "faxNum";

        enableFax(true);
    }

    private RadioMenu getFaxChoiceMenu()
    {
        return new RadioMenu(selenium_, By.cssSelector("table#form\\:faxGridRadio"));
    }

    /**
     * Selects whether to add a fax number for the provider or not.
     * @param enable true to enable fax number fields, false to leave the fax number fields blank and disabled
     */
    public void enableFax(boolean enable)
    {
        if (enable) {
            getFaxChoiceMenu().selectItem("Yes");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithoutClass(
                    By.cssSelector(AREA_CODE_FIELD_CSS), "ui-state-disabled"));
        } else {
            getFaxChoiceMenu().selectItem("No");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                    By.cssSelector(AREA_CODE_FIELD_CSS), "ui-state-disabled"));
        }
    }

    /**
     * Gets the current value of the Area Code field.
     * @return the current value of the Area Code field
     */
    public String getAreaCode() { return selenium_.findElementByCss(AREA_CODE_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the current value of the Fax Number field.
     * @return the current value of the Fax Number field
     */
    public String getFaxNumber() { return selenium_.findElementByCss(FAX_NUMBER_FIELD_CSS).getAttribute("value"); }

    /**
     * Fills in the Area Code field.
     * @param areaCode the area code to fill in
     */
    public void fillAreaCode(String areaCode) { selenium_.fillFieldByCss(AREA_CODE_FIELD_CSS, areaCode); }

    /**
     * Fills in the Fax Number field.
     * @param faxNumber the Fax number to fill in
     */
    public void fillFaxNumber(String faxNumber) { selenium_.fillFieldByCss(FAX_NUMBER_FIELD_CSS, faxNumber); }
}
