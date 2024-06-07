package ca.bc.gov.health.qa.autotest.plr.web.pages.common;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * TODO (AZ) - doc
 */
public class FooterFragment
extends BasicWebPageFragment
{
    private static final String FOOTER_CSS = "div#footer";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public FooterFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("div#footer"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String openAccessibility()
    {
        return openFooterLink("Accessibility");
    }

    /**
     * TODO (AZ) - doc
     */
    public void openContactUs()
    {
        WebElement contactUsLink =
                selenium_.findElementByCss(FOOTER_CSS).findElement(By.linkText("Contact Us"));
        contactUsLink.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(contactUsLink));
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String openCopyright()
    {
        return openFooterLink("Copyright");
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String openDisclaimer()
    {
        return openFooterLink("Disclaimer");
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String openPrivacy()
    {
        return openFooterLink("Privacy");
    }

    /**
     * TODO (AZ) - doc
     *
     * @param linkText
     *        ???
     *
     * @return ???
     */
    private String openFooterLink(String linkText)
    {
        selenium_.findElementByCss(FOOTER_CSS).findElement(By.linkText(linkText)).click();
        // TODO (AZ) - Opens in a new tab. Wait for the new tab to open, and return its handle.
        return null;
    }
}
