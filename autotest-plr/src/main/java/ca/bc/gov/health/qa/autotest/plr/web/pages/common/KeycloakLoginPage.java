package ca.bc.gov.health.qa.autotest.plr.web.pages.common;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumExpectedConditions;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;

/**
 * TODO (AZ) - doc
 */
public class KeycloakLoginPage
extends BasicWebPage
{
    private static final String ALERT_MESSAGE_CSS  = "div.alert > span.kc-feedback-text";
    private static final String PASSWORD_FIELD_CSS = "input#password";
    private static final String SIGN_IN_BUTTON_CSS = "input#kc-login";
    private static final String USERNAME_FIELD_CSS = "input#username";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public KeycloakLoginPage(SeleniumSession selenium)
    {
        super(selenium,
              By.cssSelector("div.login-pf-page form#kc-form-login"),
              "Sign in to Ministry of Health - Keycloak IDP Realm");
    }

    /**
     * TODO (AZ) - doc
     *
     * @param username
     *        ???
     *
     * @param password
     *        ???
     */
    public void attemptToLogin(String username, String password)
    {
        selenium_.fillFieldByCss(USERNAME_FIELD_CSS, username);
        selenium_.fillFieldByCss(PASSWORD_FIELD_CSS, password);
        selenium_.clickByCss(SIGN_IN_BUTTON_CSS);
        selenium_.waitUntil(SeleniumExpectedConditions.pageToBeReady());
    }

    /**
     * TODO (AZ) - doc
     *
     * @param username
     *        ???
     *
     * @param password
     *        ???
     */
    public void login(String username, String password)
    {
        attemptToLogin(username, password);
        String alertText = grabAlertText();
        if (alertText == null)
        {
            waitForAbsent();
        }
        else
        {
            String msg = String.format("Keycloak login failed (%s).", alertText);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String grabAlertText()
    {
        WebElement element = selenium_.searchElementByCss(ALERT_MESSAGE_CSS);
        String alertText;
        if (element != null)
        {
            alertText = element.getText();
        }
        else
        {
            alertText = null;
        }
        return alertText;
    }
}
