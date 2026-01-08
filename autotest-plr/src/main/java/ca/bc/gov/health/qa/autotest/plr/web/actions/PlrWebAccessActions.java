package ca.bc.gov.health.qa.autotest.plr.web.actions;

import java.net.URI;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.add.AddFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.search.SearchFacilityPage;
import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.plr.web.pages.common.HomePage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.common.KeycloakLoginPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.common.LoginPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.PlrNavigationMenuFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.PlrNavigationMenuFragment.Item;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderPage;
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
     * Opens the PLR application (defaults to Search Provider Page)
     *
     * @return a SearchProviderPage object for the search provider page it ends up at
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
     * Opens the Search Provider page
     *
     * @return  a SearchProviderPage object for the search provider page
     */
    public SearchProviderPage openSearchProvider()
    {
        waitForPlrNavigationMenuFragment().openItem(Item.SEARCH_PROVIDER);
        return waitForSearchProviderPage();
    }

    /**
     * Opens the Search Facility page
     *
     * @return  a SearchFacilityPage object for the search facility page
     */
    public SearchFacilityPage openSearchFacility()
    {
        waitForPlrNavigationMenuFragment().openItem(Item.SEARCH_FACILITY);
        return waitForSearchFacilityPage();
    }

    /**
     * Opens the Add Facility page
     *
     * @return  an AddFacilityPage object for the add facility page
     */
    public AddFacilityPage openAddFacility()
    {
        waitForPlrNavigationMenuFragment().openItem(Item.ADD_FACILITY);
        return waitForAddFacilityPage();
    }

    /**
     * Waits for the Navigation Menu (at top of PLR page) to be present in the browser
     *
     * @return  a PlrNavigationMenuFragment object for the navigation menu
     */
    public PlrNavigationMenuFragment waitForPlrNavigationMenuFragment()
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

    private SearchFacilityPage waitForSearchFacilityPage()
    {
        SearchFacilityPage searchFacility = new SearchFacilityPage(selenium_);
        searchFacility.waitForReady();
        return searchFacility;
    }

    private AddFacilityPage waitForAddFacilityPage()
    {
        AddFacilityPage addFacility = new AddFacilityPage(selenium_);
        addFacility.waitForReady();
        return addFacility;
    }
}
