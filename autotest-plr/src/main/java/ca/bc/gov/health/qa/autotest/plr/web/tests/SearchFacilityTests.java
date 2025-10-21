package ca.bc.gov.health.qa.autotest.plr.web.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
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

import static org.testng.Assert.*;

public class SearchFacilityTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;
    private static JSONObject warningList;

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public SearchFacilityTests() {
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

    /*
    @AfterClass
    public void teardown() {
        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }
     */

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
     * Logs into PLR with a specific userType (if it hasn't been logged in already)
     *
     * @param userType      the user type to log into PLR as
     * @return              the PlrWebWorkflow reference to the workflow logged into PLR as the specified user type
     */
    public PlrWebWorkflow logIn(UserType userType)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(userType);
        if (!workflow.isLoggedIn()) workflow.login().openPlr();
        return workflow;
    }

    /**
     * Navigate to the "Search Facility" Page
     *
     * @param userType      the userType to log in as and navigate to the Search Facility Page with
     * @return              a SearchFacilityPage reference to the workflow's search facility page component
     */
    public SearchFacilityPage navigateToSearchFacilityPage(UserType userType)
    {
        PlrWebWorkflow workflow = logIn(userType);
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();

        // System displays Search by Facility page correctly
        searchFacility.waitForReady();

        return searchFacility;
    }

    /**
     * Searches by Identifier in the Search Facility page.
     *
     * @param searchFacility    the search facilty page reference
     * @param queryFields       a list of strings of query details to fill fields with.
     *                          Index 0: Facility Identifier Type
     *                          Index 1: Facility Identifier
     * @param expectedError     whether an error is anticipated when executing the query
     * @return                  a SearchFacilityResultsFragment reference to the search results of the identifier query
     */
    public SearchFacilityResultsFragment searchByIdentifier(
            SearchFacilityPage searchFacility, List<String> queryFields, boolean expectedError)
    {
        return searchFacility.searchByIdentifier(
                queryFields.getFirst(), queryFields.get(1),
                expectedError);
    }

    /**
     * Searches by Criteria in the Search Facility page.
     *
     * @param searchFacility    the search facility page reference
     * @param queryFields       a list of strings of query details to fill fields with.
     *                          Index 0: Facility Name
     *                          Index 1: Civic Address Line 1
     *                          Index 2: Other Address Line 2
     *                          Index 3: City Field
     *                          Index 4: City Prefix (for autocomplete, empty string becomes null)
     *                          Index 5: Facility Type Prefix
     *                          Index 6: Service Delivery Area Field
     *                          Index 7: Service Delivery Area Prefix (for autocomplete, empty string becomes null)
     * @param expectedError     whether an error is anticipated when executing the query
     * @return                  a SearchFacilityResultsRequest reference to the search results of the criteria query
     */
    public SearchFacilityResultsFragment searchByCriteria(
            SearchFacilityPage searchFacility, List<String> queryFields, boolean expectedError)
    {
        String cityPrefix = null;
        String sdaPrefix = null;
        if (!queryFields.get(4).isEmpty()) cityPrefix = queryFields.get(4);
        if (!queryFields.get(7).isEmpty()) sdaPrefix = queryFields.get(7);
        return searchFacility.searchByCriteria(
                queryFields.getFirst(), queryFields.get(1), queryFields.get(2), queryFields.get(3), cityPrefix,
                queryFields.get(5), queryFields.get(6), sdaPrefix, expectedError);
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
     * @param searchFacility    the search facility page reference
     * @param wildcardType      which wildcard type to test against (key for wildcardQueries)
     * @param queryDetails      list of default query details
     * @param wildcardQueries   map of wildcard queries to test against (based on wildcard type)
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

    private void wildcardOtherCheck(SearchFacilityPage searchFacility, String wildcardType,
                                    List<String> queryDetails, LinkedHashMap<String,String> wildcardQueries)
    {
        queryDetails.set(2, wildcardQueries.get(wildcardType));
        SearchFacilityResultsFragment searchResults = searchByCriteria(searchFacility, queryDetails, false);
        assertTrue(searchResults.grabResultsRowCount() > 0,
                "Searching civic address with " + wildcardType + " unexpectedly returns no testable results");
    }

    @Test
    // F1-002. Facility Search by ID
    public void testFacilitySearchID()
    {
        final List<String> expectedData = Arrays.asList("AZ F00123 & & (", "IFC.00000001.BC.PRS", "1175 DOUGLAS ST,\nVICTORIA,\nBritish Columbia");

        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(UserType.ADMIN);

        assertTrue(searchFacility.grabIdentifierSectionExpanded(), "Search by Identifier not opened by default");

        SearchFacilityIdFragment identifierPanel = searchFacility.expandSearchIdentifier(true);
        List<String> identifierAttributes = identifierPanel.verifyIdentifierTab();

        assertTrue(identifierAttributes.getFirst().contains("(*)"),
                "Instruction for mandatory fields not present");
        assertTrue(identifierAttributes.get(1).contains("Facility Identifier Type*"),
                "Facility Identifier Type attribute not present");
        assertTrue(identifierAttributes.get(2).contains("Facility Identifier*"),
                "Facility Identifier attribute not present");
        assertTrue(identifierAttributes.getLast().contains("Search"),
                "Search Button not present");

        List<String> queryDetails = Arrays.asList("IFC", expectedData.get(1));
        SearchFacilityResultsFragment searchResults = searchByIdentifier(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0,
                "Search Results returned unsuccessfully.");

        assertTrue(searchResults.getResultsRow(0).get(1).contains(expectedData.get(1)),
                "Returned facility doesn't have the expected identifier used in search.");
        assertTrue(searchResults.getResultsRow(0).get(0).startsWith(expectedData.get(0)),
                "Returned facility doesn't have the expected facility name.");
        assertEquals(searchResults.getResultsRow(0).get(2), expectedData.get(2),
                "Returned facility doesn't have the expected civic address.");

        ViewFacilityPage searchDetails = workflow.getSearchFacilityActions().openSearchResults(0);
        String viewTitle = searchDetails.getViewHeader().grabViewTitle();
        assertTrue(viewTitle.contains(expectedData.get(1)),
                "View page header does not match expected identifier");
    }

    @Test
    // F1-003. Minimum Data Requirements for Facility Search by Facility ID
    public void testMinDataReqsFacilityID() {
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(UserType.ADMIN);

        assertTrue(searchFacility.grabIdentifierSectionExpanded(), "Search by Identifier not opened by default");

        SearchFacilityIdFragment identifierPanel = searchFacility.expandSearchIdentifier(true);

        final String facIdentifierEmptyError = errorList.getString("missingFacilityIdentifier");
        final String identifierTypeEmptyError = errorList.getString("missingIdentifierType");

        // Positive Test
        List<String> positiveTestDetails = Arrays.asList("IFC", "IFC.00000000.BC.PRS");
        SearchFacilityResultsFragment searchResults = searchByIdentifier(searchFacility, positiveTestDetails, false);

        List<String> highlightedFields = identifierPanel.getHighlightedFields();

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully.");
        assertTrue(highlightedFields.isEmpty(), "Fields are highlighted despite no errors appearing");

        // Facility Identifier Empty, Identifier Type Specified
        List<String> facIdentifierDetails = Arrays.asList("IFC", "");
        searchByIdentifier(searchFacility, facIdentifierDetails, true);

        List<String> errorMessageList = searchFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierPanel.getHighlightedFields();

        assertTrue(errorMessageList.contains(facIdentifierEmptyError),
                "Missing minimum requirement of Facility Identifier error not displayed.");
        assertEquals(highlightedFields.getLast(), "Facility Identifier*",
                "Facility Identifier is unhighlighted, or more than one error occurred.");

        // Identifier Type Empty, Facility Identifier Specified
        List<String> identifierTypeDetails = Arrays.asList("Select One", "IFC.00000000.BC.PRS");
        searchByIdentifier(searchFacility, identifierTypeDetails, true);

        errorMessageList = searchFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierPanel.getHighlightedFields();

        assertTrue(errorMessageList.contains(identifierTypeEmptyError),
                "Missing minimum requirement of Facility Identifier Type error not displayed.");
        assertEquals(highlightedFields.getLast(), "Facility Identifier Type*",
                "Facility Identifier Type is unhighlighted, or more than one error occurred.");

        // Both Identifier Type and Facility Identifier Empty
        List<String> emptyFieldDetails = Arrays.asList("Select One", "");
        searchByIdentifier(searchFacility, emptyFieldDetails, true);

        errorMessageList = searchFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierPanel.getHighlightedFields();

        assertTrue(errorMessageList.contains(facIdentifierEmptyError),
                "Missing minimum requirement of Facility Identifier error not displayed.");
        assertTrue(errorMessageList.contains(identifierTypeEmptyError),
                "Missing minimum requirement of Facility Identifier Type error not displayed.");
        assertTrue(highlightedFields.contains("Facility Identifier Type*"),
                "Facility Identifier Type field not highlighted.");
        assertTrue(highlightedFields.contains("Facility Identifier*"),
                "Facility Identifier field not highlighted.");
        assertEquals(highlightedFields.size(), 2, "Unexpected amount of highlighted fields");
    }

    @Test
    // F1-006. Facility Search by Criteria
    public void testFacilitySearchCriteria()
    {
        final List<String> expectedAttributes = Arrays.asList(
                "Facility Name", "Civic Address Line 1", "Other Address Line 1",
                "City", "Facility Type", "Service Delivery Area");
        final List<String> expectedFacilities = Arrays.asList("ABCDEF", "AZ F00123 & & (", "AZ F003 && fytfy & (");
        final List<String> expectedFields = Arrays.asList("1175 DOUGLAS ST", "1175 DOUGLAS ST", "VICTORIA", "South Vancouver Island");

        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(UserType.ADMIN);

        SearchFacilityCriteriaFragment criteriaPanel = searchFacility.expandSearchCriteria(true);
        assertTrue(searchFacility.grabCriteriaSectionExpanded(),"Search by Criteria not opened by default");

        List<String> criteriaAttributes = criteriaPanel.verifyCriteriaTab();
        assertEquals(criteriaAttributes.getFirst(), "At least 1 search criteria must be entered.",
                "Instruction to fill at least 1 field does not match expected result.");
        List<String> criteriaFields = criteriaAttributes.subList(1, criteriaAttributes.size()-2);
        assertEquals(criteriaFields, expectedAttributes, "Unexpected mismatch of criteria fields");
        assertTrue(criteriaAttributes.get(criteriaAttributes.size()-2).contains("Clear"),
                "Clear Button not present");
        assertTrue(criteriaAttributes.getLast().contains("Search"),
                "Search button not present");

        List<String> queryDetails = Arrays.asList("A*", expectedFields.get(0), expectedFields.get(1), "Vic", "Victoria",
                "BUILDING", "South", "South Vancouver");
        SearchFacilityResultsFragment searchResults = searchByCriteria(searchFacility, queryDetails, false);
        assertTrue(searchResults.grabResultsRowCount() > 2,
                "Searching for facility with criteria results in expected facilities not being returned.");

        for (int resultsIndex = 0; resultsIndex < 3; resultsIndex++)
        {
            ViewFacilityPage searchDetails = workflow.getSearchFacilityActions().openSearchResults(resultsIndex);

            assertTrue(searchDetails.getViewHeader().grabViewTitle().contains(expectedFacilities.get(resultsIndex)),
                    "Viewing facility leads to unexpected page");
            LinkedHashMap<String,String> civicMap = searchDetails.grabCivicAddressBlockContent(0);
            LinkedHashMap<String,String> otherMap = searchDetails.grabDataBlockContent(
                    FacilitySection.OTHER_ADDRESS, 0);
            assertEquals(civicMap.get("Address Line 1"), expectedFields.get(0),
                    "Viewing facility has unexpected civic address.");
            assertEquals(otherMap.get("Address Line 1"), expectedFields.get(1),
                    "Viewing facility has unexpected other address.");
            assertEquals(civicMap.get("City"), expectedFields.get(2),
                    "Viewing facility has unexpected city.");
            assertEquals(civicMap.get("Health Service Delivery Area"), expectedFields.get(3),
                    "Viewing facility has unexpected service delivery area.");

            workflow.getPlrWebAccessActions().openSearchFacility();
        }

        criteriaPanel = searchFacility.expandSearchCriteria(true);
        criteriaPanel.clickClearButton();
        for (String fieldValue : criteriaPanel.getCurrentFieldValues())
        {
            assertTrue(fieldValue.isEmpty() || fieldValue.equals("Select One"),
                    "Clear button failed to reset field values");
        }
    }

    @Test
    // F1-007. Minimum Data Requirements for Facility Search with Criteria
    public void testMinDataReqsCriteria()
    {
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(UserType.ADMIN);
        SearchFacilityResultsFragment searchResults;

        List<String> queryDetails = Arrays.asList("", "", "", "", "", "Select One", "", "");

        // Facility Name Specified, Others Empty
        queryDetails.set(0, "Test Name");
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully when only specifying Test Name");
        queryDetails.set(0, "");

        // Civic Address Line 1 Specified, Others Empty
        queryDetails.set(1, "Test Civic Address");
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully when only specifying Civic Address Line 1.");
        queryDetails.set(1, "");

        // Other Address Line 1 Specified, Others Empty
        queryDetails.set(2, "Test Other Address");
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully when only specifying Other Addresss Line 1.");
        queryDetails.set(2, "");

        // City Specified, Others Empty
        queryDetails.set(3, "Test City");
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully when only specifying City.");
        queryDetails.set(3, "");

        // Facility Type Specified, Others Empty
        queryDetails.set(5, "BUILDING");
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully when only specifying Facility Type.");
        queryDetails.set(5, "Select One");

        // Service Delivery Area Specified, Others Empty
        queryDetails.set(6, "Test SDA");
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully when only specifying Service Delivery Area.");
        queryDetails.set(6, "");

        int searchResultsCount = searchResults.grabResultsRowCount();

        // All Criteria Empty
        final String expectedMessage = warningList.getString("missingCriteria");

        searchResults = searchByCriteria(searchFacility, queryDetails, true);
        List<String> warningMessageList = searchFacility.waitForAlertMessagesFragment().grabWarningMessageList();

        assertTrue(warningMessageList.contains(expectedMessage),
                "Missing minimum requirements message warning not displayed.");

        assertEquals(searchResults.grabResultsRowCount(), searchResultsCount,
                "Error query changed query results unexpectedly.");
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

        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(UserType.ADMIN);
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
    // F1-014. Zero Results
    public void testZeroResults()
    {
        final String expectedMessage = "No records found.";

        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(UserType.ADMIN);
        SearchFacilityResultsFragment searchResults;

        List<String> fakeFields;

        // Search by Identifier - Zero Results
        fakeFields = Arrays.asList("IFC", "ABC.123");
        searchResults = searchByIdentifier(searchFacility, fakeFields, false);

        assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage,
                "Empty results message not returned when searching nonexistent facility through identifier.");

        // Search by Criteria - Zero Results
        fakeFields = Arrays.asList("ABC.123", "1234 Fake St", "5678 Unknown Rd", "City", "", "BUILDING", "SDA", "");
        searchResults = searchByCriteria(searchFacility, fakeFields, false);

        assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage,
                "Empty results message not returned when searching nonexistent facility through criteria.");
    }

    @Test
    // F1-015. Maximum Search Results
    public void testMaximumResults()
    {
        final int expectedResults = 20;
        final String maxResultsWarning = warningList.getString("maximumResults");

        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(UserType.ADMIN);
        SearchFacilityResultsFragment searchResults = searchFacility.searchByCriteria(
                "A*", "", "", "", null,
                "Select One", "", null, false);
        List<String> warningMessageList = searchFacility.waitForAlertMessagesFragment().grabWarningMessageList();

        assertEquals(searchResults.grabResultsRowCount(), expectedResults,
                "Returned search result does not match the expected maximum number of search results.");
        assertTrue(warningMessageList.contains(maxResultsWarning),
                "Maximum search results warning not displayed.");
        assertTrue(searchResults.getFormResults().contains(String.format("%d results", expectedResults)),
                "Form result does not match expected maximum search results.");
    }

    @Test
    // Search Facility: Alphabetical Sorting
    public void testAlphabeticalSort()
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        SearchFacilityResultsFragment searchResults = searchFacility.searchByCriteria(
                "", "", "", "Victoria", null,
                "Select One", "", null, false);

        List<String> facNameList = searchResults.getFacilityNamesList();
        // No name facility edge-case handling - not covered by test case
        while (facNameList.contains("Link to View Facility")) facNameList.remove("Link to View Facility");

        List<String> sortedNameList = new ArrayList<>(facNameList);
        Collections.sort(sortedNameList);
        assertEquals(facNameList, sortedNameList, "Returned search results and sorted search results do not match.");

        // need to check edge-cases for same name different upper/lowercase, nonalphabetical characters
        // investigation / confirmation of expected behavior needed
    }
}
