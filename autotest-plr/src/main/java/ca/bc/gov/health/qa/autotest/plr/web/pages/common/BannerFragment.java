package ca.bc.gov.health.qa.autotest.plr.web.pages.common;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * TODO (AZ) - doc
 */
public class BannerFragment
extends BasicWebPageFragment
{
    private static final String APP_TITLE_CSS = "div#banner span.appTitle";
    private static final String DATE_CSS      = "div#datetime";
    private static final String SERVICES_CSS  = "div#services";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public BannerFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#banner"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String grabApplicationTitle()
    {
        return selenium_.grabTextByCss(APP_TITLE_CSS);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String grabDate()
    {
        return selenium_.grabTextByCss(DATE_CSS);
    }

    /**
     * TODO (AZ) - doc
     */
    public void logout()
    {
        findServicesElement().findElement(By.linkText("Logout")).click();
        waitForAbsent();
    }

    /**
     * TODO (AZ) - doc
     */
    public void openContactUs()
    {
        WebElement contactUsLink = findServicesElement().findElement(By.linkText("Contact Us"));
        contactUsLink.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(contactUsLink));
    }

    /**
     * TODO (AZ) - doc
     */
    public void openHome()
    {
        findServicesElement().findElement(By.linkText("Home")).click();
    }

    private WebElement findServicesElement()
    {
        return selenium_.findElementByCss(SERVICES_CSS);
    }
}
