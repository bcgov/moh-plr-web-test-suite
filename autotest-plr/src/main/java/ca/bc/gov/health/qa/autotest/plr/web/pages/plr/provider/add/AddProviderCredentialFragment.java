package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.AutocompleteMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.ListBoxMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.RadioMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;

public class AddProviderCredentialFragment extends AddProviderStepFragment {

    private static final String DESIGNATION_FIELD_CSS = "input#form\\:designation";

    private static final String REGISTRATION_NUMBER_FIELD_CSS = "input#form\\:regNo";

    private static final String INSTITUTION_FIELD_CSS = "input#form\\:institution";

    private static final String CITY_FIELD_CSS = "input#form\\:cityCred_input";

    private static final String YEAR_FIELD_CSS = "input#form\\:yearIssued";

    /**
     * Initializes the Credential step fragment, setting the step type to "Credential" and the step prefix to "Cred"
     * @param selenium the current SeleniumSession
     */
    public AddProviderCredentialFragment(SeleniumSession selenium)
    {
        super(selenium, "Credential");
        STEP_PREFIX = "Cred";

        enableCredential(true);
    }

    private RadioMenu getCredentialChoiceMenu()
    {
        return new RadioMenu(selenium_, By.cssSelector("table#form\\:credGridRadio"));
    }

    /**
     * Selects whether to add a credential for the provider or not.
     * If enabled, the fields for entering credential information will be enabled and can be filled in.
     * If not enabled, the fields for entering credential information will be left blank and disabled.
     * @param enable true to enable credential information fields, false to leave credential information fields blank and disabled
     */
    public void enableCredential(boolean enable)
    {
        if (enable) {
            getCredentialChoiceMenu().selectItem("Yes");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithoutClass(
                    By.cssSelector(DESIGNATION_FIELD_CSS), "ui-state-disabled"));
        } else {
            getCredentialChoiceMenu().selectItem("No");

            selenium_.waitUntil(SeleniumExpectedConditions.presenceOfElementLocatedWithClass(
                    By.cssSelector(DESIGNATION_FIELD_CSS), "ui-state-disabled"));
        }
    }

    private DropDownMenu getCredentialTypeMenu()
    {
        return new DropDownMenu(selenium_,
                By.cssSelector("label#form\\:credentialType_label"),
                By.cssSelector("div#form\\:credentialType_panel"));
    }

    /**
     * Gets the value currently selected in the Credential Type field
     * @return a string of the value currently selected as Credential Type
     */
    public String getCredentialType() { return getCredentialTypeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Credential Type dropdown
     * @return a list of strings of all credential type options
     */
    public List<String> getCredentialTypeOptions()
    {
        DropDownMenu menu = getCredentialTypeMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects the given option in the Credential Type dropdown, then returns the value now selected in the dropdown
     * @param option the option to be selected in the Credential Type dropdown
     * @return a string of the value now selected in the Credential Type dropdown after selecting the given option
     */
    public String selectCredentialType(String option)
    {
        getCredentialTypeMenu().selectItem(option);
        return getCredentialType();
    }

    private AutocompleteMenu getCityMenu()
    {
        return new AutocompleteMenu(
                selenium_,
                By.cssSelector(CITY_FIELD_CSS),
                By.cssSelector("span#form\\:cityCred_panel")
        );
    }

    /**
     * Gets the value currently entered in the Designation field
     * @return a string of the value currently entered in the Designation field
     */
    public String getDesignation() { return selenium_.findElementByCss(DESIGNATION_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the value currently entered in the Registration Number field
     * @return a string of the value currently entered in the Registration Number field
     */
    public String getRegistrationNumber() { return selenium_.findElementByCss(REGISTRATION_NUMBER_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the value currently entered in the Institution field
     * @return a string of the value currently entered in the Institution field
     */
    public String getInstitution() { return selenium_.findElementByCss(INSTITUTION_FIELD_CSS).getAttribute("value"); }

    /**
     * Gets the value currently entered in the City field
     * @return a string of the value currently entered in the City field
     */
    public String getCity() { return selenium_.findElementByCss(CITY_FIELD_CSS).getAttribute("value"); }

    /**
     * Fills in the Designation field with the given designation
     * @param designation the designation to fill in the Designation field
     */
    public void fillDesignation(String designation) { selenium_.fillFieldByCss(DESIGNATION_FIELD_CSS, designation); }

    /**
     * Fills in the Registration Number field with the given registration number
     * @param regNumber the registration number to fill in the Registration Number field
     */
    public void fillRegistrationNumber(String regNumber) { selenium_.fillFieldByCss(REGISTRATION_NUMBER_FIELD_CSS, regNumber); }

    /**
     * Fills in the Institution field with the given institution
     * @param institution the institution to fill in the Institution field
     */
    public void fillInstitution(String institution) { selenium_.fillFieldByCss(INSTITUTION_FIELD_CSS, institution); }

    /**
     * Fills in the City field with the given city
     * @param city the city to fill in the City field
     */
    public void fillCity(String city) { getCityMenu().fillItem(city, null); }

    /**
     * Fills in the City field and selects an option from the resulting menu
     * @param cityField the city to select from the autocomplete menu
     * @param cityPrefix the text to fill in the city field (can be a partial city name)
     * @return a string of the city selected from the autocomplete menu
     */
    public String fillCityAutocomplete(String cityField, String cityPrefix)
    {
        return getCityMenu().fillItem(cityField, cityPrefix);
    }

    private DropDownMenu getCountryMenu()
    {
        return new DropDownMenu(selenium_,
                By.cssSelector("label#form\\:countryCred_label"),
                By.cssSelector("div#form\\:countryCred_panel"));
    }

    /**
     * Gets the value currently selected in the Country dropdown
     * @return a string of the value currently selected in the Country dropdown
     */
    public String getCountry() { return getCountryMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Country dropdown
     * @return a list of strings of all country options in the Country dropdown
     */
    public List<String> getCountryOptions() {
        DropDownMenu menu = getCountryMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects the given option in the Country dropdown, then returns the value now selected in the dropdown
     * @param country the option to be selected in the Country dropdown
     * @return a string of the value now selected in the Country dropdown after selecting the given option
     */
    public String selectCountry(String country) {
        getCountryMenu().selectItem(country);
        return getCountry();
    }

    private DropDownMenu getProvinceStateMenu()
    {
        return new DropDownMenu(selenium_,
                By.cssSelector("label#form\\:provinceCred_label"),
                By.cssSelector("div#form\\:provinceCred_panel"));
    }

    /**
     * Gets the value currently selected in the Province/State dropdown
     * @return a string of the value currently selected in the Province/State dropdown
     */
    public String getProvinceState() { return getProvinceStateMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Province/State dropdown
     * @return a list of strings of all province/state options in the Province/State dropdown
     */
    public List<String> getProvinceStateOptions() {
        DropDownMenu menu = getProvinceStateMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects the given option in the Province/State dropdown, then returns the value now selected in the dropdown
     * @param provinceState the option to be selected in the Province/State dropdown
     * @return a string of the value now selected in the Province/State dropdown after selecting the given option
     */
    public String selectProvinceState(String provinceState) {
        getProvinceStateMenu().selectItem(provinceState);
        return getProvinceState();
    }

    /**
     * Gets the value currently entered in the Year field
     * @return a string of the value currently entered in the Year field
     */
    public String getYear() { return selenium_.findElementByCss(YEAR_FIELD_CSS).getAttribute("value"); }

    /**
     * Fills in the Year field with the given year
     * @param year the year to fill in the Year field
     */
    public void fillYear(String year) { selenium_.fillFieldByCss(YEAR_FIELD_CSS, year); }

    /**
     * Enables/disables the equivalency flag for the credential.
     * @param enable true to enable the flag, false to disable the flag
     */
    public void enableEquivalency(boolean enable)
    {
        WebElement equivDiv = selenium_.findElement(By.cssSelector("div#form\\:equivalencyFlag > div.ui-chkbox-box"));

        if (enable)
        {
            if (!equivDiv.getCssValue("class").contains("ui-state-active")) {
                equivDiv.click();
            }
        } else if (equivDiv.getCssValue("class").contains("ui-state-active")) {
            equivDiv.click();
        }
    }
}
