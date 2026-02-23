package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.openqa.selenium.By;

import java.util.List;

public class AddProviderStatusFragment extends AddProviderStepFragment {

    /**
     * Initializes page object and changes selenium's main locator to the Identifier heading
     * @param selenium the selenium session
     */
    public AddProviderStatusFragment(SeleniumSession selenium) {
        super(selenium, "Status");
        STEP_PREFIX = "status";
    }

    /**
     * Constructs a DropDownMenu component for the Status Class Code field
     * @return a DropDownMenu component for the Status Class Code field
     */
    private DropDownMenu getStatusClassCodeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#form\\:statusClassCode_label"),
                By.cssSelector("div#form\\:statusClassCode_panel")
        );
    }

    /**
     * Gets the value currently selected in the Status Class Code field
     * @return a string of the value currently selected as Status Class Code
     */
    public String getStatusClassCode() { return getStatusClassCodeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Status Class Code dropdown
     * @return a list of strings of all status class code options
     */
    public List<String> getStatusClassCodeOptions()
    {
        DropDownMenu menu = getStatusClassCodeMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects an option in the Status Class Code field
     * @param statusClassCode the status class code option to select
     * @return a string of the value currently selected as Status Class Code after selection
     */
    public String selectStatusClassCode(String statusClassCode)
    {
        // Scroll dropdown into view before expanding to ensure panel appears in visible area
        selenium_.scrollIntoView(selenium_.findElement(By.cssSelector("label#form\\:statusClassCode_label")));
        DropDownMenu menu = getStatusClassCodeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(statusClassCode);
        return getStatusClassCode();
    }

    /**
     * Constructs a DropDownMenu component for the Status Code field
     * @return a DropDownMenu component for the Status Code field
     */
    private DropDownMenu getStatusCodeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#form\\:statusCode_label"),
                By.cssSelector("div#form\\:statusCode_panel")
        );
    }

    /**
     * Gets the value currently selected in the Status Code field
     * @return a string of the value currently selected as Status Code
     */
    public String getStatusCode() { return getStatusCodeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Status Code dropdown
     * @return a list of strings of all status code options
     */
    public List<String> getStatusCodeOptions()
    {
        DropDownMenu menu = getStatusCodeMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects an option in the Status Code field
     * @param statusCode the status code option to select
     * @return a string of the value currently selected as Status Code after selection
     */
    public String selectStatusCode(String statusCode)
    {
        // Scroll dropdown into view before expanding to ensure panel appears in visible area
        selenium_.scrollIntoView(selenium_.findElement(By.cssSelector("label#form\\:statusCode_label")));
        DropDownMenu menu = getStatusCodeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(statusCode);
        return getStatusCode();
    }

    /**
     * Constructs a DropDownMenu component for the Status Reason Code field
     * @return a DropDownMenu component for the Status Reason Code field
     */
    private DropDownMenu getStatusReasonCodeMenu()
    {
        return new DropDownMenu(
                selenium_,
                By.cssSelector("label#form\\:statusReasonCode_label"),
                By.cssSelector("div#form\\:statusReasonCode_panel")
        );
    }

    /**
     * Gets the value currently selected in the Status Reason Code field
     * @return a string of the value currently selected as Status Reason Code
     */
    public String getStatusReasonCode() { return getStatusReasonCodeMenu().grabSelectedItem(); }

    /**
     * Gets all available options in the Status Reason Code dropdown
     * @return a list of strings of all status reason code options
     */
    public List<String> getStatusReasonCodeOptions()
    {
        DropDownMenu menu = getStatusReasonCodeMenu();
        menu.expandItemPanel(true);
        return menu.grabItemList();
    }

    /**
     * Selects an option in the Status Reason Code field
     * @param statusReasonCode the status reason code option to select
     * @return a string of the value currently selected as Status Reason Code after selection
     */
    public String selectStatusReasonCode(String statusReasonCode)
    {
        // Scroll dropdown into view before expanding to ensure panel appears in visible area
        selenium_.scrollIntoView(selenium_.findElement(By.cssSelector("label#form\\:statusReasonCode_label")));
        DropDownMenu menu = getStatusReasonCodeMenu();
        menu.expandItemPanel(true);
        menu.selectItem(statusReasonCode);
        return getStatusReasonCode();
    }
}
