package ca.bc.gov.health.qa.autotest.plr.web.pages.common;

import java.net.URI;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;

/**
 * TODO (AZ) - doc
 */
public class LoginPage
extends BasicWebPage
{
    private static final String IDIR_LOGIN_BUTTON_CSS =
            "div#kc-social-providers a#zocial-idir";

    private static final String KEYCLOAK_LOGIN_BUTTON_CSS =
            "div#kc-social-providers a#zocial-moh_idp";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     *
     * @param uri
     *        ???
     */
    public LoginPage(SeleniumSession selenium, URI uri)
    {
        super(selenium,
              By.cssSelector("div.login-pf-page div#kc-social-providers"),
              "Sign in to Ministry of Health - Applications Realm",
              uri);
    }

    /**
     * TODO (AZ) - doc
     */
    public void openIdirLogin()
    {
        selenium_.clickByCss(IDIR_LOGIN_BUTTON_CSS);
        waitForAbsent();
    }

    /**
     * TODO (AZ) - doc
     */
    public void openKeycloakLogin()
    {
        selenium_.clickByCss(KEYCLOAK_LOGIN_BUTTON_CSS);
        waitForAbsent();
    }

    /**
     * TODO (AZ) - doc
     */
    @Override
    public void waitForReady()
    {
        super.waitForReady();
        selenium_.waitUntil(ExpectedConditions.elementToBeClickable(
                By.cssSelector(KEYCLOAK_LOGIN_BUTTON_CSS)));
    }
}
