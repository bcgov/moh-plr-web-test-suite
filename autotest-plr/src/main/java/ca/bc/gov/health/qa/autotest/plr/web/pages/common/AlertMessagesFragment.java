package ca.bc.gov.health.qa.autotest.plr.web.pages.common;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * Fragment class for the message alerts section of PLR pages
 */
public class AlertMessagesFragment
extends BasicWebPageFragment
{
    private static final String MESSAGES_SECTION_CSS = "div#messages";

    private static final String ERROR_MESSAGES_CSS =
            MESSAGES_SECTION_CSS + " > div.ui-messages-error";

    private static final String WARNING_MESSAGES_CSS =
            MESSAGES_SECTION_CSS + " > div.ui-messages-warn";

    /**
     * Initializes alert messages fragment and sets main locator to the messages container
     *
     * @param selenium
     *        The current SeleniumSession
     */
    public AlertMessagesFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector(MESSAGES_SECTION_CSS));
    }

    /**
     * Gets a list of errors in the message list
     *
     * @return a list of strings, where each string is an error message
     */
    public List<String> grabErrorMessageList()
    {
        return grabAlertMessageListByCss(ERROR_MESSAGES_CSS);
    }

    /**
     * Gets a list of warnings in the message list
     *
     * @return a list of strings, where each string is a warning message
     */
    public List<String> grabWarningMessageList()
    {
        return grabAlertMessageListByCss(WARNING_MESSAGES_CSS);
    }

    /**
     * Gets a list of strings inside a specific message section
     *
     * @param messageSectionCss     a CSS selector to the message section based in the fragment main locator
     *
     * @return  a list of strings, where each string is a message in the specified message section
     */
    private List<String> grabAlertMessageListByCss(String messageSectionCss)
    {
        List<String> messageList = new ArrayList<>();
        List<WebElement> elementList =
                selenium_.findElementsByCss(messageSectionCss + " > ul > li");
        for (WebElement element : elementList)
        {
            messageList.add(element.getText());
        }
        return messageList;
    }
}
