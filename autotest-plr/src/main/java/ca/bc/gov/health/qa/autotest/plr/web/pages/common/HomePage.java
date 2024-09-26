package ca.bc.gov.health.qa.autotest.plr.web.pages.common;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;

/**
 * TODO (AZ) - doc
 */
public class HomePage
extends BasicWebPage
{
    private static final Pattern APPLICATION_VERSION_PATTERN =
            Pattern.compile("Application Version: (?<version>[0-9]+(?:\\.[0-9]+)*)");

    private static final String APPLICATION_VERSION_CSS = "table.layout tr.layout:nth-of-type(3)";
    private static final String DSR_BUTTON_CSS          = "button#form\\:dsr";
    private static final String PLR_BUTTON_CSS          = "button#form\\:plr";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     *
     * @param uri
     *        ???
     */
    public HomePage(SeleniumSession selenium, URI uri)
    {
        super(selenium,
              By.cssSelector("table#home"),
              "Provider Location Registry - Home",
              uri);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String grabApplicationVersion()
    {
        String applicationVersionText = selenium_.grabTextByCss(APPLICATION_VERSION_CSS);
        Matcher matcher = APPLICATION_VERSION_PATTERN.matcher(applicationVersionText);
        String applicationVersion;
        if (matcher.matches())
        {
            applicationVersion = matcher.group("version");
        }
        else
        {
            String msg = String.format(
                    "Invalid application version text (%s).", applicationVersionText);
            throw new IllegalStateException(msg);
        }
        return applicationVersion;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabDsrButtonEnabled()
    {
        return selenium_.findElementByCss(DSR_BUTTON_CSS).getAttribute("disabled") == null;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabPlrButtonEnabled()
    {
        return selenium_.findElementByCss(PLR_BUTTON_CSS).getAttribute("disabled") == null;
    }

    /**
     * TODO (AZ) - doc
     */
    public void openDsr()
    {
        if (grabDsrButtonEnabled())
        {
            selenium_.clickByCss(DSR_BUTTON_CSS);
        }
        else
        {
            throw new IllegalStateException("DSR button is disabled.");
        }
        waitForAbsent();
    }

    /**
     * TODO (AZ) - doc
     */
    public void openPlr()
    {
        if (grabPlrButtonEnabled())
        {
            selenium_.clickByCss(PLR_BUTTON_CSS);
        }
        else
        {
            throw new IllegalStateException("PLR button is disabled.");
        }
        waitForAbsent();
    }
}
