package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.RadioMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Page fragment for the Expertise step of the Add Provider workflow.
 */
public class AddProviderExpertiseFragment extends AddProviderStepFragment {

    private static final String SOURCE_CODE_FIELD_CSS = "input#form\\:sourceCode";

    /**
     * Initializes the Expertise step fragment and updating the selenium locator to the step header
     * @param selenium the current SeleniumSession
     */
    public AddProviderExpertiseFragment(SeleniumSession selenium) {
        super(selenium, "Expertise");
        STEP_PREFIX = "Expertise";

        enableExpertise(true);
    }

    private RadioMenu getExpertiseChoiceMenu()
    {
        return new RadioMenu(selenium_, By.cssSelector("table#form\\:expertGridRadio"));
    }

    /**
     * Selects whether to add expertise to the provider or not
     * If enabled, the fields for entering expertise information will be enabled and can be filled in
     * If disabled, the fields for entering expertise information will be left blank and disabled
     * @param enable true to enable adding expertise, false to disable adding expertise
     */
    public void enableExpertise(boolean enable)
    {
        if (enable) {
            getExpertiseChoiceMenu().selectItem("Yes");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithoutClass(
                    By.cssSelector(SOURCE_CODE_FIELD_CSS), "ui-state-disabled"));
        } else {
            getExpertiseChoiceMenu().selectItem("No");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                    By.cssSelector(SOURCE_CODE_FIELD_CSS), "ui-state-disabled"));
        }
    }

    private DropDownMenu getExpertiseMenu()
    {
        return new DropDownMenu(selenium_,
                                By.cssSelector("label#form\\:expertise_label"),
                                By.cssSelector("div#form\\:expertise_panel"));
    }

    /**
     * Gets the value currently selected in the Expertise drop down menu
     * @return a string of the value currently selected as Expertise
     */
    public String getExpertise() { return getExpertiseMenu().grabSelectedItem(); }

    /**
     * Gets the list of options available in the Expertise drop down menu
     * @return a list of strings of the options available in the Expertise drop down menu
     */
    public List<String> getExpertiseOptions()
    {
        DropDownMenu menu = getExpertiseMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects an expertise from the Expertise drop down menu
     * @param expertise the expertise to be selected from the drop down menu
     * @return a string of the value currently selected as Expertise after selecting the provided expertise
     */
    public String selectExpertise(String expertise)
    {
        return getExpertiseMenu().selectItem(expertise);
    }

    /**
     * Gets the value currently entered in the Source Code field
     * @return a string of the value currently entered in the Source Code field
     */
    public String getSourceCode() { return selenium_.findElementByCss(SOURCE_CODE_FIELD_CSS).getAttribute("value"); }

    /**
     * Fills in the Source Code field with the provided source code
     * @param sourceCode the source code to be entered into the Source Code field
     */
    public void fillSourceCode(String sourceCode) { selenium_.fillFieldByCss(SOURCE_CODE_FIELD_CSS, sourceCode); }
}
