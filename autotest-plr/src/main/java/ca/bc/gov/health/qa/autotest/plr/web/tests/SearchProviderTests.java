package ca.bc.gov.health.qa.autotest.plr.web.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.common.BannerFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchProviderCriteriaFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class SearchProviderTests
implements SimpleTest
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public SearchProviderTests()
    {}

    @AfterClass
    public void teardown()
    {
        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }

    @BeforeMethod
    public void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn())
        {
            workflow.login().openPlr();
        }
    }

    @Test
    public void test0()
    {
        // FIXME
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
 
        SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
        BannerFragment banner = new BannerFragment(workflow.getSeleniumSession());
        banner.waitForReady();
        LOG.info("APP> ({})", banner.grabApplicationTitle());
        LOG.info("DAT> ({})", banner.grabDate());

        SearchProviderCriteriaFragment search = searchProvider.expandSearchCriteria(true);

        searchProvider.expandSearchCriteria(false);
        searchProvider.expandSearchCriteria(true);
        searchProvider.expandSearchCriteria(false);
        searchProvider.expandSearchCriteria(true);
        searchProvider.enableIncludeHistory(true);
        searchProvider.enableIncludeHistory(false);

        search.selectRoleType("RNP");
        search.selectRoleType("DEN");
        search.selectRoleType("RN ");

        search.selectStatusCode("INACTIVE");
        search.selectStatusReasonCode("MEDSTUD");

        search.clickClearButton();
        search.clickSearchButton();

        SearchProviderResultsFragment searchResults =
                new SearchProviderResultsFragment(workflow.getSeleniumSession());
        LOG.info("RS> ({})", searchResults.grabResultsSummary());
        // FIXME
    }

    @Test
    // FIXME - reference the relevant test case
    public void testMaximumResults()
    {
        final String expectedWarningMessage =
                "Maximum search results returned. Please refine your search criteria.";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
        SearchProviderResultsFragment searchResults =
                searchProvider.searchByCriteria(null, null, null, null, "Victoria", null, null);
        List<String> warningMessageList =
                searchProvider.waitForAlertMessagesFragment().grabWarningMessageList();
        assertTrue(
                warningMessageList.contains(expectedWarningMessage),
                "Maximum results warning message is displayed.");
        assertTrue(
                searchResults.grabResultsSummary().startsWith("20 results"),
                "Results summary message is displayed.");
    }

    @Test
    // Search Provider : Search - Zero Results
    public void testZeroResults()
    {
        final String expectedMessage = "No records found.";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
        SearchProviderResultsFragment searchResults;
        searchResults = searchProvider.searchByIdentifier("IPC", "ABC.123");
        assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage);
        searchResults = searchProvider.searchByRegistryIdentifier("CPN", "DEF.4567");
        assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage);
        searchResults = searchProvider.searchByCriteria(
                "MD",
                "Nonexistent",
                "Provider",
                "M",
                "Victoria",
                "TERMINATED",
                "RET",
                List.of("AMD1 ", "AMD49 "),
                List.of("A01 ", "A09 "));
        assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage);
        searchResults = searchProvider.searchForOrganization(
                "ORG",
                "Nonexistent",
                "Organization",
                "123 Some Street",
                "Victoria");
        assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage);
    }
}
