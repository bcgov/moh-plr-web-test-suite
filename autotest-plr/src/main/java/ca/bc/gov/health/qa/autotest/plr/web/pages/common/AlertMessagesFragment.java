package ca.bc.gov.health.qa.autotest.plr.web.pages.common;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * TODO (AZ) - doc
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
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public AlertMessagesFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector(MESSAGES_SECTION_CSS));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public List<String> grabErrorMessageList()
    {
        return grabAlertMessageListByCss(ERROR_MESSAGES_CSS);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public List<String> grabWarningMessageList()
    {
        return grabAlertMessageListByCss(WARNING_MESSAGES_CSS);
    }

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
