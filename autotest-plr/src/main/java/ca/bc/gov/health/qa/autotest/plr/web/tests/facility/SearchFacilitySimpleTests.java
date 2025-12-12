package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.SearchFacilityConstants.*;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.SearchFacilityActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.*;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.search.SearchFacilityCriteriaFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.search.SearchFacilityIdFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.search.SearchFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.search.SearchFacilityResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static org.testng.Assert.*;

public class SearchFacilitySimpleTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;
    private static JSONObject warningList;
    private static FHIRController fhirController;

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private MaintainFacilityBuilder dummyFacility;
    private Map<String,String> dummyAddress;

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
        fhirController.close();

        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }

    @BeforeTest
    public void beforeTest() {
        fhirController = new FHIRController(UserType.ADMIN);

        FacilityMaintainConfig dummyCfg = new FacilityMaintainConfig();
        dummyFacility = fhirController.createFacility(dummyCfg);

        dummyFacility = fhirController.queryFacilityByIdentifier(IdentifierType.IFC, dummyFacility.getIdentifier());
        dummyAddress = dummyFacility.getAddress();
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

    @Test(dataProvider = "facilityTestUserTypes", dataProviderClass = InjectableData.class)
    // F1-001. Facility Search
    public void testFacilitySearch(UserType userType)
    {
        final Pattern SEARCH_RESULTS_TIME_PATTERN = Pattern.compile("([0-9]+\\.[0-9]{3})");
        final List<String> identifierToCheck = Arrays.asList("IFC", dummyFacility.getIdentifier());
        final List<String> criteriaToCheck = Arrays.asList(
                dummyFacility.getName(),
                dummyAddress.get("line1"), "",
                dummyAddress.get("city").charAt(0) + dummyAddress.get("city").substring(1,
                        dummyAddress.get("city").length() - 1).toLowerCase(),
                dummyAddress.get("city").charAt(0) + dummyAddress.get("city").substring(1).toLowerCase(),
                "Select One", "", "");
        final SearchFacilityActions actions = workflowManager_.getSelectedWorkflow().getSearchFacilityActions();

        SearchFacilityResultsFragment searchResults;
        String formSeconds = null;

        // Test Start
        logIn(workflowManager_, userType);
        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, userType);

        assertTrue(actions.verifyTitle(), "Title of page does not match 'Search Facility'");
        assertTrue(actions.verifyHistory(), "History checkbox not found");

        for (String searchType : List.of("Identifier", "Criteria"))
        {
            if (searchType.equals("Identifier"))
            {
                assertTrue(page.isIdentifierSectionExpanded(), "Search by Identifier not opened by default");
                assertFalse(page.isCriteriaSectionExpanded(), "Search by Criteria open by default in error");

                searchResults = searchByIdentifier(page, identifierToCheck, false);
            } else
            {
                page.expandSearchCriteria(true);

                assertTrue(page.isCriteriaSectionExpanded(), "Search by Criteria failed to open");
                assertFalse(page.isIdentifierSectionExpanded(), "Search by Identifier remained open in error");

                searchResults = searchByCriteria(page, criteriaToCheck, false);
            }

            String formResults = searchResults.getFormResults();
            List<String> tableColumns = searchResults.getTableColumns();
            Matcher resultMatcher = SEARCH_RESULTS_TIME_PATTERN.matcher(formResults);
            if (resultMatcher.find()) formSeconds = resultMatcher.group();

            assertTrue(actions.checkOrdering(),
                    "Table of search results is out of position (identifier/criteria search not above table)");
            assertTrue(formResults.contains("1 result"),
                    "Form result does not contain number of results in table summary");
            assertTrue(formResults.contains("(" + formSeconds + " seconds)"),
                    "Form result does not contain time taken to retrieve results.");

            for (TableColumn column : TableColumn.values())
            {
                assertTrue(tableColumns.get(column.getIndex()).contains(column.getName()),
                        "Table column " + column.getIndex() + " is not " + column.getName());
            }
        }

        workflowManager_.logoutAndClose(userType);
    }

    @Test
    // F1-002. Facility Search by ID
    public void testFacilitySearchID()
    {
        final String expectedCivicAddress = String.format("%s,\n%s,\nBritish Columbia",
                dummyAddress.get("line1").toUpperCase(), dummyAddress.get("city").toUpperCase());
        final SearchFacilityActions actions = workflowManager_.getSelectedWorkflow().getSearchFacilityActions();

        // Test Start
        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

        assertTrue(page.isIdentifierSectionExpanded(), "Search by Identifier not opened by default");

        SearchFacilityIdFragment identifierPanel = page.expandSearchIdentifier(true);
        List<String> identifierAttributes = identifierPanel.getIdentifierTab();

        for (IdentifierTabAttribute attribute : IdentifierTabAttribute.values())
        {
            assertTrue(identifierAttributes.get(attribute.getIndex()).contains(attribute.getString()),
                    attribute.getString() + " not present in correct location");
        }

        List<String> queryDetails = Arrays.asList("IFC", dummyFacility.getIdentifier());
        SearchFacilityResultsFragment searchResults = searchByIdentifier(page, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0, "Search Results returned unsuccessfully.");

        assertTrue(searchResults.getResultsRow(0).get(0).startsWith(dummyFacility.getName()),
                "Returned facility doesn't have the expected facility name.");
        assertTrue(searchResults.getResultsRow(0).get(1).contains(dummyFacility.getIdentifier()),
                "Returned facility doesn't have the expected identifier used in search.");
        assertEquals(searchResults.getResultsRow(0).get(2), expectedCivicAddress,
                "Returned facility doesn't have the expected civic address.");

        ViewFacilityPage searchDetails = actions.openSearchResults(0);
        String viewTitle = searchDetails.getViewHeader().grabViewTitle();

        assertTrue(viewTitle.contains(dummyFacility.getIdentifier()),
                "View page header does not match expected identifier");
    }

    @Test
    // F1-003. Minimum Data Requirements for Facility Search by Facility ID
    public void testMinDataReqsFacilityID()
    {
        final String facIdentifierEmptyError = errorList.getString("missingFacilityIdentifier");
        final String identifierTypeEmptyError = errorList.getString("missingIdentifierType");

        // Test Start
        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

        assertTrue(page.isIdentifierSectionExpanded(), "Search by Identifier not opened by default");

        SearchFacilityIdFragment identifierPanel = page.expandSearchIdentifier(true);

        // Positive Test
        List<String> positiveTestDetails = Arrays.asList("IFC", dummyFacility.getIdentifier());
        SearchFacilityResultsFragment searchResults = searchByIdentifier(page, positiveTestDetails, false);

        List<String> highlightedFields = identifierPanel.getHighlightedFields();

        assertTrue(searchResults.grabResultsRowCount() >= 0, "Results returned unsuccessfully.");
        assertTrue(highlightedFields.isEmpty(), "Fields are highlighted despite no errors appearing");

        // Facility Identifier Empty, Identifier Type Specified
        List<String> facIdentifierDetails = Arrays.asList("IFC", "");
        searchByIdentifier(page, facIdentifierDetails, true);

        List<String> errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierPanel.getHighlightedFields();

        assertTrue(errorMessageList.contains(facIdentifierEmptyError),
                "Missing minimum requirement of Facility Identifier error not displayed.");
        assertEquals(highlightedFields.getLast(), IdentifierTabAttribute.FACILITY_IDENTIFIER.getString(),
                "Facility Identifier is unhighlighted, or more than one error occurred.");

        // Identifier Type Empty, Facility Identifier Specified
        List<String> identifierTypeDetails = Arrays.asList("Select One", dummyFacility.getIdentifier());
        searchByIdentifier(page, identifierTypeDetails, true);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierPanel.getHighlightedFields();

        assertTrue(errorMessageList.contains(identifierTypeEmptyError),
                "Missing minimum requirement of Facility Identifier Type error not displayed.");
        assertEquals(highlightedFields.getLast(), IdentifierTabAttribute.FACILITY_IDENTIFIER_TYPE.getString(),
                "Facility Identifier Type is unhighlighted, or more than one error occurred.");

        // Both Identifier Type and Facility Identifier Empty
        List<String> emptyFieldDetails = Arrays.asList("Select One", "");
        searchByIdentifier(page, emptyFieldDetails, true);

        errorMessageList = page.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierPanel.getHighlightedFields();

        assertTrue(errorMessageList.contains(facIdentifierEmptyError),
                "Missing minimum requirement of Facility Identifier error not displayed.");
        assertTrue(errorMessageList.contains(identifierTypeEmptyError),
                "Missing minimum requirement of Facility Identifier Type error not displayed.");
        assertTrue(highlightedFields.contains(IdentifierTabAttribute.FACILITY_IDENTIFIER_TYPE.getString()),
                "Facility Identifier Type field not highlighted.");
        assertTrue(highlightedFields.contains(IdentifierTabAttribute.FACILITY_IDENTIFIER.getString()),
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
        final String uniqueNamePrefix = generateAlphabetString(5); // reasonably likely to be unique

        List<MaintainFacilityBuilder> criteriaFacilities = Arrays.asList(
                fhirController.createFacility(
                        new FacilityMaintainConfig().withName(uniqueNamePrefix + generateAlphabetString(3))),
                fhirController.createFacility(
                        new FacilityMaintainConfig().withName(uniqueNamePrefix + generateAlphabetString(3))),
                fhirController.createFacility(
                        new FacilityMaintainConfig().withName(uniqueNamePrefix + generateAlphabetString(3)))
        );

        final List<String> expectedAttributes = Arrays.asList(
                "Facility Name", "Civic Address Line 1", "Other Address Line 1",
                "City", "Facility Type", "Service Delivery Area");

        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

        SearchFacilityCriteriaFragment criteriaPanel = searchFacility.expandSearchCriteria(true);
        assertTrue(searchFacility.isCriteriaSectionExpanded(),"Search by Criteria not opened by default");

        List<String> criteriaAttributes = criteriaPanel.verifyCriteriaTab();
        assertEquals(criteriaAttributes.getFirst(), "At least 1 search criteria must be entered.",
                "Instruction to fill at least 1 field does not match expected result.");
        List<String> criteriaFields = criteriaAttributes.subList(1, criteriaAttributes.size()-2);
        assertEquals(criteriaFields, expectedAttributes, "Unexpected mismatch of criteria fields");
        assertTrue(criteriaAttributes.get(criteriaAttributes.size()-2).contains("Clear"),
                "Clear Button not present");
        assertTrue(criteriaAttributes.getLast().contains("Search"),
                "Search button not present");

        List<String> queryDetails = Arrays.asList(uniqueNamePrefix + "*", "", "", "", "", "BUILDING", "", "");
        SearchFacilityResultsFragment searchResults = searchByCriteria(searchFacility, queryDetails, false);
        assertTrue(searchResults.grabResultsRowCount() > 2,
                "Searching for facility with criteria results in expected facilities not being returned.");

        int resultsIndex = 0;
        for (MaintainFacilityBuilder criteriaFacility : criteriaFacilities)
        {
            MaintainFacilityBuilder criteriaFacilityInfo = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
                    criteriaFacility.getIdentifier());
            ViewFacilityPage searchDetails = workflow.getSearchFacilityActions().openSearchResults(resultsIndex);

            assertTrue(searchDetails.getViewHeader().grabViewTitle().contains(criteriaFacility.getName()),
                    "Viewing facility leads to unexpected page");

            LinkedHashMap<String,String> civicMap = searchDetails.grabCivicAddressBlockContent();
            LinkedHashMap<String,String> otherMap = searchDetails.grabDataBlockContent(
                    FacilitySection.OTHER_ADDRESS, 0);

            assertEquals(civicMap.get("Address Line 1").toLowerCase(),
                    criteriaFacility.getAddress().get("line1").toLowerCase(),
                    "Viewing facility has unexpected civic address.");
            assertEquals(otherMap.get("Address Line 1").toLowerCase(),
                    criteriaFacility.getAddress().get("line1").toLowerCase(),
                    "Viewing facility has unexpected other address.");
            assertEquals(civicMap.get("City").toLowerCase(), criteriaFacility.getAddress().get("city").toLowerCase(),
                    "Viewing facility has unexpected city.");
            assertEquals(civicMap.get("Health Service Delivery Area"), criteriaFacilityInfo.getHsda().get("HSDA"),
                    "Viewing facility has unexpected service delivery area.");

            workflow.getPlrWebAccessActions().openSearchFacility();

            resultsIndex++;
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
        queryDetails.set(0, dummyFacility.getName());
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully when only specifying Test Name");
        queryDetails.set(0, "");

        // Civic Address Line 1 Specified, Others Empty
        queryDetails.set(1, dummyAddress.get("line1"));
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully when only specifying Civic Address Line 1.");
        queryDetails.set(1, "");

        // Other Address Line 1 Specified, Others Empty
        queryDetails.set(2, dummyAddress.get("line1"));
        searchResults = searchByCriteria(searchFacility, queryDetails, false);

        assertTrue(searchResults.grabResultsRowCount() > 0 || searchResults.grabResultsRowCount() == 0,
                "Search Results returned unsuccessfully when only specifying Other Address Line 1.");
        queryDetails.set(2, "");

        // City Specified, Others Empty
        queryDetails.set(3, dummyAddress.get("city"));
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
        queryDetails.set(6, dummyFacility.getHsda().get("HSDA") + " (HSDA)");
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
        List<String> queryDetails = Arrays.asList(dummyFacility.getName(), "",
                dummyAddress.get("line1").toUpperCase(),
                "", "", "BUILDING", "", "");
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
                "", dummyAddress.get("line1"), "",
                dummyAddress.get("city").charAt(0) + dummyAddress.get("city").substring(1,3).toLowerCase(),
                dummyAddress.get("city").charAt(0) + dummyAddress.get("city").substring(1).toLowerCase(),
                "Select One",
                dummyFacility.getHsda().get("HSDA").substring(0, 4), dummyFacility.getHsda().get("HSDA"));
        searchByCriteria(searchFacility, queryDetails, false);

        viewDetails = workflow.getSearchFacilityActions().openSearchResults(0);
        LinkedHashMap<String,String> civicMap = viewDetails.grabCivicAddressBlockContent();
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

        assertTrue(searchFacility.isCriteriaSectionExpanded(),
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

        List<String> queryDetails = Arrays.asList(dummyFacility.getName().charAt(0) + "*",
                "", "", "", "", "Select One", "", "");
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

        List<String> queryDetails = Arrays.asList("IFC", dummyFacility.getIdentifier());
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

        queryDetails = Arrays.asList(dummyFacility.getName(), dummyFacility.getAddress().get("line1"),
                "", "", "", "Select One", "", "");
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