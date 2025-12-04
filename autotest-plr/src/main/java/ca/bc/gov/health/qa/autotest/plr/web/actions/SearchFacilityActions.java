package ca.bc.gov.health.qa.autotest.plr.web.actions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.SearchFacilityResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Actions class for the Search Facility page/functions
 */
public class SearchFacilityActions {
    private final SeleniumSession selenium_;

    /**
     * Initializes class and SeleniumSession.
     *
     * @param selenium  the current SeleniumSession
     */
    public SearchFacilityActions(SeleniumSession selenium) { selenium_ = selenium; }

    /**
     * Opens the view facility page for a facility in the table of search results.
     *
     * @param index     the index of row in the table of search results to view the facility of
     * @return  the view facility page for the associated facility
     */
    public ViewFacilityPage openSearchResults(int index)
    {
        SearchFacilityResultsFragment searchResults = new SearchFacilityResultsFragment(selenium_);
        searchResults.waitForReady();
        searchResults.openResults(index);
        ViewFacilityPage viewFacility = new ViewFacilityPage(selenium_);
        viewFacility.waitForReady();
        return viewFacility;
    }
}
