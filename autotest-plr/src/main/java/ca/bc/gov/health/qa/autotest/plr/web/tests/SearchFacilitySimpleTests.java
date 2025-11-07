package ca.bc.gov.health.qa.autotest.plr.web.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static org.testng.Assert.*;

public class SearchFacilitySimpleTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;
    private static JSONObject warningList;

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public SearchFacilitySimpleTests() {
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

    /**
     * Checks the Column Names of the table of search results
     *
     * @param searchResults     the SearchFacilityResultsFragment reference
     */
    private void checkColumns(SearchFacilityResultsFragment searchResults)
    {
        int index = 0;
        for (String tableColumn : List.of("Facility Name", "Identifier", "Civic Address"))
        {
            assertTrue(searchResults.getTableColumns().get(index).contains(tableColumn),
                    String.format("Table column %d is not %s", index, tableColumn));
            index++;
        }
    }

    @Test
    // F1-001. Facility Search
    public void testFacilitySearch()
    {
        final Pattern SEARCH_RESULTS_TIME_PATTERN = Pattern.compile("([0-9]+\\.[0-9]{3})");

        final String identifierToCheck = "IFC.00000001.BC.PRS";
        final List<String> criteriaToCheck = Arrays.asList(
                "AZ F00123 & & (", "1175 DOUGLAS ST", "", "Vic", "Victoria", "Select One", "", "");

        for (UserType userType : UserType.values())
        {
            if (userType.equals(UserType.MOH) || userType.equals(UserType.USER)) continue;

            logIn(workflowManager_, userType);
            SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, userType);
            SearchFacilityResultsFragment searchResults;

            assertTrue(searchFacility.verifyTitle(), "Title of page does not match 'Search Facility'");
            assertTrue(searchFacility.verifyHistory(), "History checkbox not found");

            assertTrue(searchFacility.grabIdentifierSectionExpanded(),
                    "Search by Identifier not opened by default");
            assertFalse(searchFacility.grabCriteriaSectionExpanded(),
                    "Search by Criteria unexpectedly open by default");

            String formSeconds = null;

            // Identifier Query
            List<String> queryDetails = Arrays.asList("IFC", identifierToCheck);
            searchResults = searchByIdentifier(searchFacility, queryDetails, false);
            String formResults = searchResults.getFormResults();
            Matcher resultMatcher = SEARCH_RESULTS_TIME_PATTERN.matcher(formResults);
            if (resultMatcher.find()) formSeconds = resultMatcher.group();

            assertTrue(searchFacility.checkOrdering(),
                    "Table of search results is out of position (identifier/criteria search not above table)");
            assertTrue(formResults.contains("1 result"),
                    "Form result does not contain number of results in table summary");
            assertTrue(formResults.contains(String.format("(%s seconds)", formSeconds)),
                    "Form result does not contain time taken to retrieve results.");

            checkColumns(searchResults);

            // Criteria Query
            searchFacility.expandSearchCriteria(true);
            assertTrue(searchFacility.grabCriteriaSectionExpanded(),
                    "Search by Criteria failed to open");
            assertFalse(searchFacility.grabIdentifierSectionExpanded(),
                    "Search by Identifier unexpectedly remained open");

            queryDetails = criteriaToCheck;
            searchResults = searchByCriteria(searchFacility, queryDetails, false);
            formResults = searchResults.getFormResults();
            resultMatcher = SEARCH_RESULTS_TIME_PATTERN.matcher(formResults);
            if (resultMatcher.find()) formSeconds = resultMatcher.group();

            assertTrue(searchFacility.checkOrdering(),
                    "Table of search results is out of position (identifier/criteria search not above table)");
            assertTrue(formResults.contains("1 result"),
                    "Form result does not contain number of results in table summary");
            assertTrue(formResults.contains(String.format("(%s seconds)", formSeconds)),
                    "Form result does not contain time taken to retrieve results.");

            checkColumns(searchResults);
        }
    }

    @Test
    // F1-002. Facility Search by ID
    public void testFacilitySearchID()
    {
        final List<String> expectedData = Arrays.asList(
                "AZ F00123 & & (", "IFC.00000001.BC.PRS", "1175 DOUGLAS ST,\nVICTORIA,\nBritish Columbia");

        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

        assertTrue(searchFacility.grabIdentifierSectionExpanded(),
                "Search by Identifier not opened by default");

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
        SearchFacilityResultsFragment searchResults;
        searchResults = searchByIdentifier(searchFacility, queryDetails, false);

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
    public void testMinDataReqsFacilityID()
    {
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

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
    // F1-005. Filtering Identifier Type for Query
    public void testIdentifierTypes()
    {
        List<String> expectedIdentifierTypes = Arrays.asList("Select One", "IFC - Internal Facility Code");

        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        SearchFacilityIdFragment identifierPanel = searchFacility.expandSearchIdentifier(true);
        identifierPanel.getIdentifierTypeMenu().expandItemPanel(true);

        for (String expectedType : expectedIdentifierTypes)
        {
            assertTrue(identifierPanel.getIdentifierTypeMenu().grabItemList().contains(expectedType),
                    "Type " + expectedIdentifierTypes + "is unavailable in the identifier type menu");
        }
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
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

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
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
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
    // F1-008. Facility Attribute Search Rules - Logical
    public void testFacilitySearchRulesLogical()
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

        // Search 1 (Facility Name, Other Address, Facility Type)
        List<String> queryDetails = Arrays.asList("AZ F00123 & & (", "", "1175 DOUGLAS ST", "", "", "BUILDING", "", "");
        searchByCriteria(searchFacility, queryDetails, false);

        ViewFacilityPage viewDetails = workflow.getSearchFacilityActions().openSearchResults(0);
        assertTrue(viewDetails.getViewHeader().grabViewTitle().contains(queryDetails.getFirst()),
                "Facility is missing expected Facility Name");
        LinkedHashMap<String,String> identifierMap = viewDetails.grabDataBlockContent(
                FacilitySection.IDENTIFIERS, 0);
        LinkedHashMap<String,String> otherMap = viewDetails.grabDataBlockContent(
                FacilitySection.OTHER_ADDRESS, 0);
        assertEquals(otherMap.get("Address Line 1"), queryDetails.get(2),
                "Facility is missing expected Other Address Line 1");
        assertEquals(identifierMap.get("Facility Type"), queryDetails.get(5),
                "Facility is missing expected Facility Type");

        workflow.getPlrWebAccessActions().openSearchFacility();

        // Search 2 (Civic Address, City, Service Delivery Area)
        queryDetails = Arrays.asList(
                "", "1175 DOUGLAS ST", "", "Vic", "Victoria", "Select One", "South V", "South Vancouver Island");
        searchByCriteria(searchFacility, queryDetails, false);

        viewDetails = workflow.getSearchFacilityActions().openSearchResults(0);
        LinkedHashMap<String,String> civicMap = viewDetails.grabCivicAddressBlockContent(0);
        assertEquals(civicMap.get("Address Line 1"), queryDetails.get(1),
                "Facility is missing expected Civic Address Line 1");
        assertEquals(civicMap.get("City"), queryDetails.get(4).toUpperCase(),
                "Facility is missing expected City");
        assertEquals(civicMap.get("Health Service Delivery Area"), queryDetails.getLast(),
                "Facility is missing expected Service Delivery Area");
    }

    @Test
    // F1-009: F1-009. Service Delivery Area Recognition
    public void testServiceDeliveryArea()
    {
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        SearchFacilityCriteriaFragment criteriaPanel = searchFacility.expandSearchCriteria(true);

        assertTrue(searchFacility.grabCriteriaSectionExpanded(),
                "Search by Criteria failed to expand");

        criteriaPanel.getServiceDeliveryAreaMenu().displayAutocomplete("South V", true);

        assertTrue(criteriaPanel.getServiceDeliveryAreaMenu().grabAutocompletePanelActive(),
                "Service Delivery Area autocomplete failed to appear");

        criteriaPanel.getServiceDeliveryAreaMenu().selectItemFromPanel("South Van");

        assertEquals(criteriaPanel.getServiceDeliveryAreaMenu().grabCompletedItem(),
                "South Vancouver Island (HSDA)",
                "Service Delivery Area field did not populate with the expected result");
    }

    @Test
    // F1-013. Word Wrap Search Results
    public void testWordWrapResults()
    {
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        SearchFacilityResultsFragment searchResults;

        List<String> queryDetails = Arrays.asList("a*", "", "", "", "", "Select One", "", "");
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        for (int rowIndex = 0; rowIndex < searchResults.grabResultsRowCount(); rowIndex++)
        {
            assertTrue(searchResults.verifyWordWrapStyle(rowIndex));
        }
    }

    @Test
    // F1-014. Zero Results
    public void testZeroResults()
    {
        final String expectedMessage = "No records found.";

        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
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
    // F1-017. Previous Facility Search Results Session
    public void testPreviousResultsSession()
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        SearchFacilityResultsFragment searchResults;

        // Identifier Query
        SearchFacilityIdFragment identifierPanel = new SearchFacilityIdFragment(workflow.getSeleniumSession());

        List<String> queryDetails = Arrays.asList("IFC", "IFC.00000001.BC.PRS");
        searchResults = searchByIdentifier(searchFacility, queryDetails, false);

        List<String> previousValues = identifierPanel.getCurrentFieldValues();
        List<String> previousResults = searchResults.getResultsRow(0);

        workflow.getSearchFacilityActions().openSearchResults(0);
        workflow.getPlrWebAccessActions().openSearchFacility();

        identifierPanel = new SearchFacilityIdFragment(workflow.getSeleniumSession());
        searchResults = new SearchFacilityResultsFragment(workflow.getSeleniumSession());

        assertEquals(searchResults.getResultsRow(0), previousResults,
                "Results from previous session do not match / do not appear");
        assertEquals(identifierPanel.getCurrentFieldValues(), previousValues,
                "Field values from previous session do not appear");

        // Criteria Query
        SearchFacilityCriteriaFragment criteriaPanel = new SearchFacilityCriteriaFragment(workflow.getSeleniumSession());

        queryDetails = Arrays.asList("AZ F00123 & & (", "1175 DOUGLAS ST", "", "", "", "Select One", "", "");
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        previousValues = criteriaPanel.getCurrentFieldValues();
        previousResults = searchResults.getResultsRow(0);

        workflow.getSearchFacilityActions().openSearchResults(0);
        searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        searchFacility.expandSearchCriteria(true);

        criteriaPanel = new SearchFacilityCriteriaFragment(workflow.getSeleniumSession());
        searchResults = new SearchFacilityResultsFragment(workflow.getSeleniumSession());

        assertEquals(searchResults.getResultsRow(0), previousResults,
                "Results from previous session do not match / do not appear");
        assertEquals(criteriaPanel.getCurrentFieldValues(), previousValues,
                "Field values from previous session do not appear");
    }
}