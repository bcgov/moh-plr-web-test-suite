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
    private static JSONObject warningList;

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public SearchFacilityComplexTests() {
        try
        {
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

    /**
     * creates a map of wildcard queries - helper function for wildcard test case
     *
     * @param criteriaField     the string to be creating wildcard queries for
     * @return                  a map of wildcard queries to be used in a criteria field
     */
    private LinkedHashMap<String,String> setupWildcards(String criteriaField)
    {
        LinkedHashMap<String,String> wildcardMap = new LinkedHashMap<>();
        wildcardMap.put("trailingWildcard", criteriaField.charAt(0) + "*");
        wildcardMap.put("precedingWildcard", "*" + criteriaField.substring(1).replace("\n", " "));
        wildcardMap.put("middleWildcard", criteriaField.charAt(0) + "*" + criteriaField.charAt(criteriaField.length()-1));
        wildcardMap.put("multipleWildcard", "*" + criteriaField.substring(1,4).replace("\n", " ") + "*");
        wildcardMap.put("firstExpectedChar", String.valueOf(criteriaField.toLowerCase().charAt(0)));
        wildcardMap.put("lastExpectedChar", String.valueOf(criteriaField.toLowerCase().charAt(criteriaField.length()-1)));
        wildcardMap.put("middleExpectedChars", criteriaField.substring(1,4).toLowerCase());

        return wildcardMap;
    }

    /**
     * Helper function to assign correct test assertion(s) to run depending on wildcard query used.
     *
     * @param wildcardField     Field to verify the wildcard query returns a matching field
     * @param wildcardType      Type of wildcard query being executed (key for wildcardQueries)
     * @param wildcardQueries   Map of wildcard queries to test against (based on wildcardType)
     */
    private void wildcardCases(String wildcardField, String wildcardType, LinkedHashMap<String,String> wildcardQueries)
    {
        switch (wildcardType)
        {
            case "trailingWildcard":
                assertTrue(wildcardField.toLowerCase().startsWith(wildcardQueries.get("firstExpectedChar")),
                        "Wildcard field doesn't match the trailing wildcard case's starting characters");
                break;
            case "precedingWildcard":
                assertTrue(wildcardField.toLowerCase().endsWith(wildcardQueries.get("lastExpectedChar")),
                        "Wildcard field does not match the preceding wildcard case's ending characters");
                break;
            case "middleWildcard":
                assertTrue(wildcardField.toLowerCase().startsWith(wildcardQueries.get("firstExpectedChar")),
                        "A facility name does not match the middle wildcard case's starting characters");
                assertTrue(wildcardField.toLowerCase().endsWith(wildcardQueries.get("lastExpectedChar")),
                        "A facility name does not match the middle wildcard case's ending characters");
                break;
            case "multipleWildcard":
                assertTrue(wildcardField.toLowerCase().contains(wildcardQueries.get("middleExpectedChars")),
                        "A facility name does not match the multiple wildcard case's middle characters");
                break;
        }
    }

    /**
     * Helper function for handling facility name wildcard queries and assertions
     *
     * @param searchFacility    The search facility page reference
     * @param wildcardType      Which wildcard type to test against (key for wildcardQueries)
     * @param queryDetails      List of default query details
     * @param wildcardQueries   Map of wildcard queries to test against (based on wildcardType)
     */
    private void wildcardNameCheck(SearchFacilityPage searchFacility, String wildcardType,
                                   List<String> queryDetails, LinkedHashMap<String,String> wildcardQueries)
    {
        queryDetails.set(0, wildcardQueries.get(wildcardType));
        SearchFacilityResultsFragment searchResults = searchByCriteria(searchFacility, queryDetails, false);
        List<String> facilityNameList = searchResults.getFacilityNamesList();
        while (facilityNameList.contains("Link to View Facility")) facilityNameList.remove("Link to View Facility");

        assertFalse(facilityNameList.isEmpty(),
                "Searching facility name with " + wildcardType + " unexpectedly returns no testable results");
        for (String facilityName : facilityNameList)
        {
            wildcardCases(facilityName, wildcardType, wildcardQueries);
        }
    }

    /**
     * Helper function for handling civic address wildcard queries and assertions
     *
     * @param searchFacility    The search facility page reference
     * @param wildcardType      Which wildcard type to test against (key for wildcardQueries)
     * @param queryDetails      List of default query details
     * @param wildcardQueries   Map of wildcard queries to test against (based on wildcardType)
     */
    private void wildcardCivicCheck(SearchFacilityPage searchFacility, String wildcardType,
                                    List<String> queryDetails, LinkedHashMap<String,String> wildcardQueries)
    {
        queryDetails.set(1, wildcardQueries.get(wildcardType));
        SearchFacilityResultsFragment searchResults = searchByCriteria(searchFacility, queryDetails, false);
        assertTrue(searchResults.grabResultsRowCount() > 0,
                "Searching civic address with " + wildcardType + " unexpectedly returns no testable results");

        for (String civicAddress : searchResults.getCivicAddressList())
        {
            if (!wildcardType.equals("middleWildcard")) wildcardCases(civicAddress, wildcardType, wildcardQueries);
            else {
                assertTrue(civicAddress.toLowerCase().startsWith(wildcardQueries.get("firstExpectedChar")),
                        "A civic address does not match the middle wildcard case's starting characters");
                // Implicit wildcard exists at end of civic address so only check containment after first character
                assertTrue(civicAddress.substring(1).toLowerCase()
                                .contains(wildcardQueries.get("lastExpectedChar")),
                        "A civic address does not match the middle wildcard case's ending characters");
            }
        }
    }

    /**
     * Helper function for partially handling other address wildcard query and assertion
     *
     * @param searchFacility    The search facility page reference
     * @param wildcardType      Which wildcard type to test against (key for wildcardQueries)
     * @param queryDetails      List of default query details
     * @param wildcardQueries   Map of wildcard queries to test against (based on wildcardType)
     */
    private void wildcardOtherCheck(SearchFacilityPage searchFacility, String wildcardType,
                                    List<String> queryDetails, LinkedHashMap<String,String> wildcardQueries)
    {
        queryDetails.set(2, wildcardQueries.get(wildcardType));
        SearchFacilityResultsFragment searchResults = searchByCriteria(searchFacility, queryDetails, false);
        assertTrue(searchResults.grabResultsRowCount() > 0,
                "Searching civic address with " + wildcardType + " unexpectedly returns no testable results");
    }

    @Test
    // F1-010. Facility Search Rules
    public void testFacilitySearchRules()
    {
        final String expectedName = "ABCDEF";
        final String expectedCivicAddress = "1175 DOUGLAS ST,\nVICTORIA";
        final String expectedOtherAddress = "1175 DOUGLAS ST";
        final String expectedWarningMessage = warningList.getString("missingCriteria");
        final List<String> wildcardTypes = Arrays.asList(
                "trailingWildcard", "precedingWildcard", "middleWildcard", "multipleWildcard");

        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();

        // Facility Name Steps
        LinkedHashMap<String,String> wildcardQueries = setupWildcards(expectedName);
        List<String> queryDetails = Arrays.asList(expectedName, "", "", "", "", "Select One", "", "");

        for (String wildcardType : wildcardTypes) wildcardNameCheck(
                searchFacility, wildcardType, queryDetails, wildcardQueries);

        // Civic Address Steps
        wildcardQueries = setupWildcards(expectedCivicAddress);
        queryDetails = Arrays.asList("", expectedCivicAddress, "", "", "", "Select One", "", "");

        for (String wildcardType : wildcardTypes) wildcardCivicCheck(
                searchFacility, wildcardType, queryDetails, wildcardQueries);

        // Other Address Steps
        wildcardQueries = setupWildcards(expectedOtherAddress);
        queryDetails = Arrays.asList("", "", expectedOtherAddress, "", "", "Select One", "", "");

        for (String wildcardType : wildcardTypes)
        {
            wildcardOtherCheck(searchFacility, wildcardType, queryDetails, wildcardQueries);
            ViewFacilityPage searchDetails = workflow.getSearchFacilityActions().openSearchResults(0);
            LinkedHashMap<String,String> otherMap = searchDetails.grabDataBlockContent(
                    FacilitySection.OTHER_ADDRESS,0);
            wildcardCases(otherMap.get("Address Line 1"), wildcardType, wildcardQueries);
            workflow.getPlrWebAccessActions().openSearchFacility();
        }

        // Warning Steps
        queryDetails = Arrays.asList("", "", "", "", "", "Select One", "", "");
        for (int fieldIndex = 0; fieldIndex < 3; fieldIndex++)
        {
            queryDetails.set(fieldIndex, "*");
            searchByCriteria(searchFacility, queryDetails, false);
            List<String> warningMessageList = searchFacility.waitForAlertMessagesFragment().grabWarningMessageList();
            assertTrue(warningMessageList.contains(expectedWarningMessage),
                    "Warning not displayed for only wildcard case");
            queryDetails.set(fieldIndex, "");
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

            workflow.logout();
            workflow.close();
            workflowManager_.deselectWorkflow();
        }
    }
}
