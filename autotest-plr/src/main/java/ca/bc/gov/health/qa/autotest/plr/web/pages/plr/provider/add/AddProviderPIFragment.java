package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Page object for the Personal Information step of the Add Provider workflow
 */
public class AddProviderPIFragment extends AddProviderStepFragment {

    private static final String PREFIX_FIELD_CSS = "input#form\\:prefix";

    private static final String FIRST_NAME_FIELD_CSS = "input#form\\:firstName";

    private static final String SECOND_NAME_FIELD_CSS = "input#form\\:secondName";

    private static final String THIRD_NAME_FIELD_CSS = "input#form\\:thirdName";

    private static final String SURNAME_FIELD_CSS = "input#form\\:surname";

    /**
     * Initializes page object and changes selenium's main locator to the Personal Information heading
     * @param selenium the selenium session
     */
    public AddProviderPIFragment(SeleniumSession selenium)
    {
        super(selenium, "Personal Information");
        STEP_PREFIX = "name";
    }

    /**
     * Fills the Prefix field with the provided string
     * @param prefix the string to fill the Prefix field with
     */
    public void fillPrefix(String prefix) { selenium_.fillFieldByCss(PREFIX_FIELD_CSS, prefix); }

    /**
     * Fills the First Name field with the provided string
     * @param firstName the string to fill the First Name field with
     */
    public void fillFirstName(String firstName) { selenium_.fillFieldByCss(FIRST_NAME_FIELD_CSS, firstName); }

    /**
     * Fills the Second Name field with the provided string
     * @param secondName the string to fill the Second Name field with
     */
    public void fillSecondName(String secondName) { selenium_.fillFieldByCss(SECOND_NAME_FIELD_CSS, secondName); }

    /**
     * Fills the Third Name field with the provided string
     * @param thirdName the string to fill the Third Name field with
     */
    public void fillThirdName(String thirdName) { selenium_.fillFieldByCss(THIRD_NAME_FIELD_CSS, thirdName); }

    /**
     * Fills the Surname field with the provided string
     * @param surname the string to fill the Surname field with
     */
    public void fillSurname(String surname) { selenium_.fillFieldByCss(SURNAME_FIELD_CSS, surname); }
}
