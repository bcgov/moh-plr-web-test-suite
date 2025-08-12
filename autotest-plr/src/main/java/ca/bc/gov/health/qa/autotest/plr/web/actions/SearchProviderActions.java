package ca.bc.gov.health.qa.autotest.plr.web.actions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * TODO (AZ) - doc
 */
public class SearchProviderActions
{
    private final SeleniumSession selenium_;

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     *
     */
    public SearchProviderActions(SeleniumSession selenium)
    {
        selenium_ = selenium;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param index
     *        ???
     *
     * @return ???
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
