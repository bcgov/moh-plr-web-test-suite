package ca.bc.gov.health.qa.autotest.plr.web.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.SearchFacilityActions;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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

        // All Criteria Empty
        final String expectedMessage = warningList.getString("missingCriteria");

        searchByCriteria(searchFacility, queryDetails, true);
        List<String> warningMessageList = searchFacility.waitForAlertMessagesFragment().grabWarningMessageList();

        assertTrue(warningMessageList.contains(expectedMessage),
                "Missing minimum requirements message warning not displayed.");
    }

    @Test
    // Search Facility : No Results - Identifier
    public void testNoResultsIdentifier()
    {
        final String expectedMessage = "No records found.";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        SearchFacilityResultsFragment searchResults = searchFacility.searchByIdentifier("IFC", "ABC.123", false);

        assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage, "Empty results message not returned when searching for nonexistent facility through identifier.");
    }

    @Test
    // Search Facility : No Results - Criteria
    public void testNoResultsCriteria()
    {
        final String expectedMessage = "No records found.";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        SearchFacilityResultsFragment searchResults = searchFacility.searchByCriteria(
                "ABC.123", "1234 Fake St", "5678 Unknown Rd", "Town", null,
                "BUILDING", "Greater Victoria (LHA)", null, false
        );
        assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage, "Empty results message not returned when searching for nonexistent facility with criteria.");
    }

    @Test
    // Search Facility : Identifier Search
    public void testIdentifierSearch()
    {
        final List<String> expectedData = Arrays.asList("ABCDEF", "IFC.00000081.BC.PRS", "1175 DOUGLAS ST,\nVICTORIA,\nBritish Columbia");
        final String expectedIdentifier = expectedData.get(1).concat(" (IFC)");
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        SearchFacilityResultsFragment searchResults = searchFacility.searchByIdentifier("IFC", expectedData.get(1), false);

        assertTrue(searchResults.grabResultsRowCount() > 0, "Searching for facility with identifier results in no facilities being returned.");
        assertEquals(searchResults.getResultsRow(0).get(1), expectedIdentifier, "Returned facility doesn't have the expected identifier used in search.");
        assertTrue(searchResults.getResultsRow(0).get(0).startsWith(expectedData.get(0)), "Returned facility doesn't have the expected facility name.");
        assertEquals(searchResults.getResultsRow(0).get(2), expectedData.get(2), "Returned facility doesn't have the expected civic address.");
    }

    @Test
    // Search Facility : Facility Search by Criteria
    public void testCriteriaSearch()
    {
        final List<String> expectedFields = Arrays.asList("1175 DOUGLAS ST", "1175 DOUGLAS ST", "VICTORIA", "South Vancouver Island");
        final String expectedName1 = "ABCDEF";
        final String expectedName2 = "AZ F00123 & & (";
        final String expectedName3 = "AZ F003 &&, fytfy & (";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        SearchFacilityResultsFragment searchResults = searchFacility.searchByCriteria(
                "", "1175 DOUGLAS ST", "1175 DOUGLAS ST", "Victoria", null,
                "BUILDING", "South Vancouver Island (HSDA)", null, false);
        SearchFacilityActions facilityActions = workflow.getSearchFacilityActions();

        assertTrue(searchResults.grabResultsRowCount() > 2, "Searching for facility with criteria results in some expected facilities not being returned.");

        final List<String> testData1 = searchResults.getResultsRow(0);
        assertTrue(testData1.getFirst().startsWith(expectedName1), "First returned facility doesn't have the expected facility name.");
        assertTrue(testData1.get(2).startsWith(expectedFields.getFirst()), "First returned facility doesn't have the expected civic address.");
        ViewFacilityPage viewFacility1 = facilityActions.openSearchResults(0);

        LinkedHashMap<String,String> civicMap = viewFacility1.grabCivicAddressBlockContent(0);
        LinkedHashMap<String,String> otherMap = viewFacility1.grabDataBlockContent(FacilitySection.OTHER_ADDRESS, 0);
        assertEquals(otherMap.get("Address Line 1"), expectedFields.get(1), "First returned facility doesn't have the expected other address.");
        assertEquals(civicMap.get("City"), expectedFields.get(2), "First returned facility doesn't have the expected city.");
        assertEquals(civicMap.get("Health Service Delivery Area"), expectedFields.get(3), "First returned facility missing the expected service delivery area.");

        workflow.getPlrWebAccessActions().openSearchFacility();

        final List<String> testData2 = searchResults.getResultsRow(1);
        assertTrue(testData2.getFirst().startsWith(expectedName2), "Second returned facility doesn't have the expected facility name.");
        assertTrue(testData2.get(2).startsWith(expectedFields.getFirst()), "Second returned facility doesn't have the expected civic address.");
        ViewFacilityPage viewFacility2 = facilityActions.openSearchResults(1);

        civicMap = viewFacility2.grabCivicAddressBlockContent(0);
        otherMap = viewFacility2.grabDataBlockContent(FacilitySection.OTHER_ADDRESS, 0);
        assertEquals(otherMap.get("Address Line 1"), expectedFields.get(1), "Second returned facility doesn't have the expected other address.");
        assertEquals(civicMap.get("City"), expectedFields.get(2), "Second returned facility doesn't have the expected city.");
        assertEquals(civicMap.get("Health Service Delivery Area"), expectedFields.get(3), "Second returned facility missing the expected service delivery area.");

        workflow.getPlrWebAccessActions().openSearchFacility();

        final List<String> testData3 = searchResults.getResultsRow(2);
        assertTrue(testData3.getFirst().startsWith(expectedName3), "Third returned facility doesn't have the expected facility name.");
        assertTrue(testData3.get(2).startsWith(expectedFields.getFirst()), "Third returned facility doesn't have the expected civic address.");
        ViewFacilityPage viewFacility3 = facilityActions.openSearchResults(2);

        civicMap = viewFacility3.grabCivicAddressBlockContent(0);
        otherMap = viewFacility3.grabDataBlockContent(FacilitySection.OTHER_ADDRESS, 0);
        assertEquals(otherMap.get("Address Line 1"), expectedFields.get(1), "Third returned facility doesn't have the expected other address.");
        assertEquals(civicMap.get("City"), expectedFields.get(2), "Third returned facility doesn't have the expected city.");
        assertEquals(civicMap.get("Health Service Delivery Area"), expectedFields.get(3), "Third returned facility missing the expected service delivery area.");
    }

    @Test
    // Search Facility: Facility Search with Wildcard - Match Ending Character
    public void testCriteriaSearchWildcardMatchEnding()
    {
        final String expectedFacilityName = "ABCDEF";
        final String facilityNameField = expectedFacilityName.replace(expectedFacilityName.substring(expectedFacilityName.length() - 1), "*");
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        SearchFacilityResultsFragment searchResults = searchFacility.searchByCriteria(
                facilityNameField, "", "", "", null,
                "Select One", "", null, false);

        assertTrue(searchResults.grabResultsRowCount() > 0, "Searching for facility with wildcard to match 1 ending character results in no facilities being returned.");
        assertTrue(searchResults.getResultsRow(0).getFirst().startsWith(expectedFacilityName), "Returned facility doesn't have the expected facility name used in search.");
    }

    @Test
    // Search Facility: Facility Search with Wildcard - Match All but First Character
    public void testCriteriaSearchWildcardMatchMany()
    {
        final String expectedFacilityName = "yates 580 postal cd";
        final String facilityNameField = expectedFacilityName.charAt(0) + "*";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        SearchFacilityResultsFragment searchResults = searchFacility.searchByCriteria(
                facilityNameField, "", "", "", null,
                "Select One", "", null,false);

        assertTrue(searchResults.grabResultsRowCount() > 0, "Searching for facility with wildcard to match all but starting character results in no facilities being returned.");
        assertTrue(searchResults.getResultsRow(0).getFirst().startsWith(expectedFacilityName), "Returned facility doesn't have the expected facility name used in search.");
    }
}
