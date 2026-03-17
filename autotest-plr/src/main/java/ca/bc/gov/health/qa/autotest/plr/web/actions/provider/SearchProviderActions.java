package ca.bc.gov.health.qa.autotest.plr.web.actions.provider;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

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

    public ViewProviderPage openConfidentialRecord(PlrWebWorkflow workflow, UserType userType, boolean isOrganization,
                                                   MaintainOrgBuilder confOrg, MaintainIndividualBuilder confInd)
    {
        SearchProviderPage provider = workflow.getPlrWebAccessActions().openSearchProvider();

        String identifier;
        if (isOrganization) identifier = confOrg.getIdentifier(IdentifierType.IPC);
        else identifier = confInd.getIdentifier(IdentifierType.IPC);

        SearchProviderResultsFragment results = provider.searchByIdentifier("IPC", identifier);
        List<String> resultInfo = results.grabResultsRow(0);
        if (userType.equals(UserType.SECONDARY))
            assertEquals(resultInfo.getFirst(), "Link to View Provider",
                    "Record name not confidentially masked");
        else // reg-admin or primary
            assertNotEquals(resultInfo.getFirst(), "Link to View Provider",
                    "Record name confidentially masked unexpectedly");

        ViewProviderPage page = openSearchResults(0);

        if (userType.equals(UserType.SECONDARY))
            assertEquals(page.getViewHeader().grabViewTitle().split(" ")[0], "Confidential",
                "Record name in title not confidentially masked");
        else // reg-admin or primary
            assertNotEquals(page.getViewHeader().grabViewTitle().split(" ")[0], "Confidential",
                    "Record name in title confidentially masked unexpectedly");

        return page;
    }
}
