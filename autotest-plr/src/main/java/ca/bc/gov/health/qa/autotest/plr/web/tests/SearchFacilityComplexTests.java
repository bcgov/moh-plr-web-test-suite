package ca.bc.gov.health.qa.autotest.plr.web.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.PlrNavigationMenuFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.*;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.searchByIdentifier;
import static org.testng.Assert.*;

public class SearchFacilityComplexTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;
    private static JSONObject warningList;

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public SearchFacilityComplexTests() {
        try
        {
            errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
            warningList = new JSONObject(Files.readString(errorPath)).getJSONObject("warnings");
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read JSON data (%s).", errorPath);
            throw new IllegalStateException(msg, e);
        }
    }

    @AfterClass
    public void teardown() {
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
    // F1-012. Alphabetical Sorting of Facility Search Results
    public void testAlphabeticalSorting()
    {
        /* Address numbers known to include important generic cases/edge-cases
           (alphanumeric characters, case sensitivity, etc.) */
        final List<String> addressSpotCheck = Arrays.asList("1175", "1549", "119");
        final List<String> queryDetails = Arrays.asList("", "", "", "Victo", "Victoria", "Select One", "", "");

        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        SearchFacilityResultsFragment searchResults;

        for (String queryAddress : addressSpotCheck)
        {
            queryDetails.set(1, queryAddress);
            searchResults = searchByCriteria(searchFacility, queryDetails, false);

            List<String> facilityNameList = searchResults.getFacilityNamesList();
            // Facilities with no name not covered by test cases - manual removal from consideration for sorting
            while (facilityNameList.contains("Link to View Facility"))
                facilityNameList.remove("Link to View Facility");

            List<String> sortedNameList = new ArrayList<>(facilityNameList);
            Collections.sort(sortedNameList);

            assertEquals(facilityNameList, sortedNameList,
                    "Returned search results and sorted search results do not match.");
        }
    }

    @Test
    // F1-015. Maximum Search Results
    public void testMaximumResults()
    {
        final int expectedResults = 20;
        final String maxResultsWarning = warningList.getString("maximumResults");
        final List<String> queryDetails = Arrays.asList("A*", "", "", "", "", "Select One", "", "");

        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        SearchFacilityResultsFragment searchResults = searchByCriteria(searchFacility, queryDetails, false);

        List<String> warningMessageList = searchFacility.waitForAlertMessagesFragment().grabWarningMessageList();

        assertEquals(searchResults.grabResultsRowCount(), expectedResults,
                "Returned search result does not match the expected maximum number of search results.");
        assertTrue(warningMessageList.contains(maxResultsWarning),
                "Maximum search results warning not displayed.");
        assertTrue(searchResults.getFormResults().contains(String.format("%d results", expectedResults)),
                "Form result does not match expected maximum search results.");
    }

    @Test
    // F1-016. Search Results Limited By Data Permissions
    public void testDataPermissions()
    {
        final List<String> queryFields = Arrays.asList("IFC", "IFC.00000001.BC.PRS");

        for (UserType userType : UserType.values())
        {
            if (userType.equals(UserType.MOH) || userType.equals(UserType.USER)) continue;

            PlrWebWorkflow workflow = logIn(workflowManager_, userType);
            PlrNavigationMenuFragment menu = workflow.getPlrWebAccessActions().waitForPlrNavigationMenuFragment();

            assertTrue(menu.grabItemVisible(PlrNavigationMenuFragment.Item.SEARCH_FACILITY),
                    "Search Facility not visible as a menu option for User Type" + userType);

            if (userType.equals(UserType.ADMIN))
            {
                assertTrue(menu.grabItemVisible(PlrNavigationMenuFragment.Item.ADD_FACILITY),
                        "Add Facility not visible as a menu option for Reg Admin User");
            } else
            {
                assertFalse(menu.grabItemVisible(PlrNavigationMenuFragment.Item.ADD_FACILITY),
                        "Add Facility unexpectedly visible as a menu option for " + userType);
            }

            SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, userType);
            SearchFacilityResultsFragment searchResults;
            searchResults = searchByIdentifier(searchFacility, queryFields, false);

            assertTrue(searchResults.grabResultsRowCount() > 0,
                    "Search results did not return for User Type " + userType);
        }
    }
}
