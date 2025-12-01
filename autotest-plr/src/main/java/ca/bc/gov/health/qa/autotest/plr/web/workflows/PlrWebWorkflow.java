package ca.bc.gov.health.qa.autotest.plr.web.workflows;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.context.LocalContext;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.PlrWebAccessActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.SearchProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.ViewProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.common.BannerFragment;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * TODO (AZ) - doc
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
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     *
     * @param uri
     *        ???
     *
     * @param userType
     *        ???
     */
    private PlrWebWorkflow(SeleniumSession selenium, URI uri, UserType userType)
    {
        selenium_ = requireNonNull(selenium, "Null Selenium session.");
        uri_      = requireNonNull(uri,      "Null URI.");
        userType_ = requireNonNull(userType, "Null user type.");
    }

    /**
     * TODO (AZ) - doc
     */
    @Override
    public void close()
    {
        loggedIn_ = false;
        selenium_.close();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param userType
     *        ???
     *
     * @return ???
     */
    public static PlrWebWorkflow create(UserType userType)
    {
        Config config = LocalContext.get().getConfig();
        URI uri = URI.create(config.get("web.url"));
        LOG.info("URL ({}).", uri);
        SeleniumSession selenium = SeleniumSession.createChromeSeleniumSession();
        return new PlrWebWorkflow(selenium, uri, userType);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public PlrWebAccessActions getPlrWebAccessActions()
    {
        return new PlrWebAccessActions(selenium_, uri_);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public SearchProviderActions getSearchProviderActions()
    {
        return new SearchProviderActions(selenium_);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public SeleniumSession getSeleniumSession()
    {
        return selenium_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public URI getURUri()
    {
        return uri_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public UserType getUserType()
    {
        return userType_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public ViewProviderActions getViewProviderActions()
    {
        return new ViewProviderActions(selenium_, uri_, userType_);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean isLoggedIn()
    {
        return loggedIn_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public PlrWebAccessActions login()
    {
        Map<String,String> credentialsMap = PlrData.getCreadentials("plr.web", userType_);
        PlrWebAccessActions actions = getPlrWebAccessActions();
        actions.login(credentialsMap.get("username"), credentialsMap.get("password"));
        loggedIn_ = true;
        return actions;
    }

    /**
     * TODO (AZ) - doc
     */
    public void logout()
    {
        BannerFragment bannerFragment = new BannerFragment(selenium_);
        bannerFragment.waitForReady();
        bannerFragment.logout();
        loggedIn_ = false;
    }
}
