package ca.bc.gov.health.qa.autotest.plr.web.pages.common;

import org.openqa.selenium.By;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;

/**
 * TODO (AZ) - doc
 */
public class ContactUsPage
extends BasicWebPage
{
    private static final String CONTACT_INFO_CSS   = "form#helpContact";
    private static final String GO_BACK_BUTTON_CSS = "form#helpContact button[type='submit']";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public ContactUsPage(SeleniumSession selenium)
    {
        super(selenium,
              By.cssSelector(CONTACT_INFO_CSS),
              "Provider Location Registry - Contact Information");
    }

    /**
     * TODO (AZ) - doc
     */
    public void goBack()
    {
        selenium_.clickByCss(GO_BACK_BUTTON_CSS);
        waitForAbsent();
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String grabContactInfo()
    {
        return selenium_.grabTextByCss(CONTACT_INFO_CSS);
    }
}
