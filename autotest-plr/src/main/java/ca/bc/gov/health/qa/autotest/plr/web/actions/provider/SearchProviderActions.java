package ca.bc.gov.health.qa.autotest.plr.web.actions.provider;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Actions class for the Search Provider page/functions
 */
public class SearchProviderActions
{
    private final SeleniumSession selenium_;

    /**
     * Initializes class and SeleniumSession.
     *
     * @param selenium  the current SeleniumSession
     */
    public SearchProviderActions(SeleniumSession selenium)
    {
        selenium_ = selenium;
    }

    /**
     * Opens the view provider page in the table of search results at the given index.
     *
     * @param index     the index of the search result to open
     * @return          the ViewProviderPage object for the associated facility
     */
    public ViewProviderPage openSearchResults(int index)
    {
        SearchProviderResultsFragment results = new SearchProviderResultsFragment(selenium_);
        results.waitForReady();
        results.openResults(index);
        ViewProviderPage viewProvider = new ViewProviderPage(selenium_);
        viewProvider.waitForReady();
        return viewProvider;
    }
}
