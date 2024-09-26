package ca.bc.gov.health.qa.autotest.plr.web.actions;

import java.net.URI;

import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.plr.web.pages.common.HomePage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.common.KeycloakLoginPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.common.LoginPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.PlrNavigationMenuFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.PlrNavigationMenuFragment.Item;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchProviderPage;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * TODO (AZ) - doc
 */
public class PlrWebAccessActions
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final SeleniumSession selenium_;
    private final URI             uri_;

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     *
     * @param uri
     *        ???
     */
    public PlrWebAccessActions(SeleniumSession selenium, URI uri)
    {
        selenium_ = selenium;
        uri_      = uri;
    }

    /**
     * TODO (AZ) - doc
     *
     *
     * @param username
     *        ???
     *
     * @param password
     *        ???
     *
     * @return ???
     */
    public HomePage login(String username, String password)
    {
        LOG.info("PLR Web Keycloak login ({}).", username);

        LoginPage login = new LoginPage(selenium_, uri_);
        login.openWebPage();
        login.openKeycloakLogin();

        KeycloakLoginPage keycloakLogin = new KeycloakLoginPage(selenium_);
        keycloakLogin.waitForReady();
        keycloakLogin.login(username, password);

        HomePage home = new HomePage(selenium_, null);
        home.waitForReady();
        return home;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public SearchProviderPage openPlr()
    {
        HomePage home = new HomePage(selenium_, uri_);
        home.waitForReady();
        LOG.info("Application version ({}).", home.grabApplicationVersion());
        home.openPlr();
        return waitForSearchProviderPage();
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public SearchProviderPage openSearchProvider()
    {
        waitForPlrNavigationMenuFragment().openItem(Item.SEARCH_PROVIDER);
        return waitForSearchProviderPage();
    }

    private PlrNavigationMenuFragment waitForPlrNavigationMenuFragment()
    {
        PlrNavigationMenuFragment fragment = new PlrNavigationMenuFragment(selenium_);
        fragment.waitForReady();
        return fragment;
    }

    private SearchProviderPage waitForSearchProviderPage()
    {
        SearchProviderPage searchProvider = new SearchProviderPage(selenium_);
        searchProvider.waitForReady();
        return searchProvider;
    }
}
