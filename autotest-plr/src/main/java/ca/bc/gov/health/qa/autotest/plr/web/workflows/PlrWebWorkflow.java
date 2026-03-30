package ca.bc.gov.health.qa.autotest.plr.web.workflows;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Map;

import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.SearchFacilityActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.ViewFacilityActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.AddProviderActions;
import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;

import ca.bc.gov.health.qa.autotest.plr.web.actions.PlrWebAccessActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.SearchProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.UpdateProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.UpdateFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.ViewProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.common.BannerFragment;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * PLR Workflow class to handle creating a selenium session with the correct environment and credentials.
 */
public class PlrWebWorkflow
implements AutoCloseable
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final SeleniumSession selenium_;
    private final URI             uri_;
    private final UserType        userType_;

    private boolean loggedIn_ = false;

    /**
     * Initializes a PLR Workflow instance
     *
     * @param selenium
     *        the current SeleniumSession
     *
     * @param uri
     *        the base URI/URL for the instance
     *
     * @param userType
     *        the user type used to log in to PLR with
     */
    private PlrWebWorkflow(SeleniumSession selenium, URI uri, UserType userType)
    {
        selenium_ = requireNonNull(selenium, "Null Selenium session.");
        uri_      = requireNonNull(uri,      "Null URI.");
        userType_ = requireNonNull(userType, "Null user type.");
    }

    /**
     * Closes the selenium session / page
     */
    @Override
    public void close()
    {
        loggedIn_ = false;
        selenium_.close();
    }

    /**
     * Creates a PLR Workflow instance and an associated selenium session.
     *
     * @param userType
     *        the user type to draw credentials from during login
     *
     * @return A PlrWebWorkflow object setup with a selenium session and URI/URL
     */
    public static PlrWebWorkflow create(UserType userType)
    {
        Config config = ConfigProvider.get().getConfig();
        URI uri = URI.create(config.get("web.url"));
        LOG.info("URL ({}).", uri);
        SeleniumSession selenium = SeleniumSession.createChromeSeleniumSession();
        return new PlrWebWorkflow(selenium, uri, userType);
    }

    /**
     * Creates and gets an actions object for PLR Web Access
     *
     * @return a PlrWebAccessActions object
     */
    public PlrWebAccessActions getPlrWebAccessActions()
    {
        return new PlrWebAccessActions(selenium_, uri_);
    }

    /**
     * Creates and gets an actions object for the Search Provider page
     *
     * @return a SearchProviderActions object
     */
    public SearchProviderActions getSearchProviderActions()
    {
        return new SearchProviderActions(selenium_);
    }

    /**
     * Creates and gets an actions object for the Search Facility page
     *
     * @return  a SearchFacilityActions object
     */
    public SearchFacilityActions getSearchFacilityActions()
    {
        return new SearchFacilityActions(selenium_);
    }

    /**
     * Creates and gets an actions object for the View Facility page
     *
     * @return  a ViewFacilityActions object
     */
    public ViewFacilityActions getViewFacilityActions()
    {
        return new ViewFacilityActions(selenium_, uri_, userType_);
    }

    /**
     * Gets the current SeleniumSession
     *
     * @return  a reference to the current SeleniumSession
     */
    public SeleniumSession getSeleniumSession()
    {
        return selenium_;
    }

    /**
     * Gets the URI/URL
     *
     * @return A URI object with the base URL of the workflow
     */
    public URI getURUri()
    {
        return uri_;
    }

    /**
     * Gets the user type selenium is accessing PLR with
     *
     * @return a UserType object with the user type used
     */
    public UserType getUserType() { return userType_; }

    /**
     * Creates and gets an actions object for the View Provider page
     *
     * @return  a ViewProviderActions object
     */
    public ViewProviderActions getViewProviderActions()
    {
        return new ViewProviderActions(selenium_, uri_, userType_);
    }

    /**
     * Creates and gets an actions object for the Add Provider page
     * @return an AddProviderActions object
     */
    public AddProviderActions getAddProviderActions() { return new AddProviderActions(selenium_); }

    /**
     * Creates and gets an actions object for the Update Facility page corresponding to simple test cases
     *
     * @return  am UpdateFacilitySimpleActions object
     */
    public UpdateFacilitySimpleActions getUpdateFacilitySimpleActions()
    {
        return new UpdateFacilitySimpleActions(selenium_, uri_, userType_);
    }
    /**
     * Creates and gets an actions object for the Update provider page corresponding to test cases
     *
     * @return  an UpdateProviderActions object
     */
    
    public UpdateProviderActions getUpdateProviderActions()
    {
        return new UpdateProviderActions(selenium_, uri_, userType_);
    }



    /**
     * Determines whether the session is logged in
     *
     * @return whether the session is logged in (true) or not (false)
     */
    public boolean isLoggedIn()
    {
        return loggedIn_;
    }

    /**
     * Logs into the PLR site using given credentials
     *
     * @return a PlrWebAccessActions object with access to the PLR site (provided the credentials are valid)
     */
    public PlrWebAccessActions login()
    {
        Map<String,String> credentialsMap = PlrData.getCredentials("plr.web", userType_);
        PlrWebAccessActions actions = getPlrWebAccessActions();
        actions.login(credentialsMap.get("username"), credentialsMap.get("password"));
        loggedIn_ = true;
        return actions;
    }

    /**
     * Logs out of the PLR site
     */
    public void logout()
    {
        BannerFragment bannerFragment = new BannerFragment(selenium_);
        bannerFragment.waitForReady();
        bannerFragment.logout();
        loggedIn_ = false;
    }
}
