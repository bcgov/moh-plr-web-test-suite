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
import ca.bc.gov.health.qa.autotest.plr.data.SearchFacilityConstants;
import ca.bc.gov.health.qa.autotest.plr.data.SearchFacilityConstants.*;
import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants.*;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.util.IdentifierTypeName;
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
        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        SearchFacilityIdFragment identifierPanel = page.expandSearchIdentifier(true);
        identifierPanel.getIdentifierTypeMenu().expandItemPanel(true);

        for (IdentifierTypeName expectedType : IdentifierTypeName.values())
        {
            assertTrue(identifierPanel.getIdentifierTypeMenu().grabItemList().contains(expectedType.getText()),
                    "Type " + expectedType.getText() + "is unavailable in the identifier type menu");
        }
    }

    @Test
    // F1-006. Facility Search by Criteria
    public void testFacilitySearchCriteria()
    {
        final String uniqueNamePrefix = generateAlphabetString(5); // reasonably likely to be unique
        final List<MaintainFacilityBuilder> criteriaFacilities = Arrays.asList(
                fhirController.createFacility(
                        new FacilityMaintainConfig().withName(uniqueNamePrefix + generateAlphabetString(3))),
                fhirController.createFacility(
                        new FacilityMaintainConfig().withName(uniqueNamePrefix + generateAlphabetString(3))),
                fhirController.createFacility(
                        new FacilityMaintainConfig().withName(uniqueNamePrefix + generateAlphabetString(3)))
        );
        final List<String> queryDetails = Arrays.asList(uniqueNamePrefix + "*", "", "", "", "", "BUILDING", "", "");

        // Test Start
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

        SearchFacilityCriteriaFragment criteriaPanel = page.expandSearchCriteria(true);
        List<String> criteriaAttributes = criteriaPanel.getCriteriaTab();

        assertTrue(page.isCriteriaSectionExpanded(),"Search by Criteria not opened by default");
        assertEquals(criteriaAttributes.get(CriteriaTabAttribute.CRITERIA_INSTRUCTION.getIndex()),
                     CriteriaTabAttribute.CRITERIA_INSTRUCTION.getString(),
                "Instruction to fill at least 1 field does not match expected result.");
        for (CriteriaTabAttribute attribute : CriteriaTabAttribute.fieldList)
        {
            assertEquals(criteriaAttributes.get(attribute.getIndex()), attribute.getString(),
                    "Unexpected mismatch of criteria fields");
        }
        assertEquals(criteriaAttributes.get(CriteriaTabAttribute.CLEAR_BUTTON.getIndex()),
                     CriteriaTabAttribute.CLEAR_BUTTON.getString(),
                "Clear Button not present");
        assertEquals(criteriaAttributes.get(CriteriaTabAttribute.SEARCH_BUTTON.getIndex()),
                     CriteriaTabAttribute.SEARCH_BUTTON.getString(),
                "Search button not present");

        SearchFacilityResultsFragment searchResults = searchByCriteria(page, queryDetails, false);

        assertEquals(searchResults.grabResultsRowCount(), 3,
                "Searching for facility with criteria results in expected facilities not being returned.");

        int resultsIndex = 0;
        for (MaintainFacilityBuilder criteriaFacility : criteriaFacilities)
        {
            MaintainFacilityBuilder criteriaFacilityInfo = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
                    criteriaFacility.getIdentifier());
            ViewFacilityPage searchDetails = workflow.getSearchFacilityActions().openSearchResults(resultsIndex);
            LinkedHashMap<String,String> civicMap = searchDetails.grabCivicAddressBlockContent();
            LinkedHashMap<String,String> otherMap = searchDetails.grabOtherAddressBlockContent(0);

            assertTrue(searchDetails.getViewHeader().grabViewTitle().contains(criteriaFacility.getName()),
                    "Viewing facility leads to unexpected page");

            assertEquals(civicMap.get(CivicAddressField.ADDRESS_LINE_1.getString()).toLowerCase(),
                    criteriaFacility.getAddress().get("line1").toLowerCase(),
                    "Viewing facility has unexpected civic address.");
            assertEquals(otherMap.get(OtherAddressField.ADDRESS_LINE_1.getString()).toLowerCase(),
                    criteriaFacility.getAddress().get("line1").toLowerCase(),
                    "Viewing facility has unexpected other address.");
            assertEquals(civicMap.get(CivicAddressField.CITY.getString()).toLowerCase(),
                    criteriaFacility.getAddress().get("city").toLowerCase(),
                    "Viewing facility has unexpected city.");
            assertEquals(civicMap.get(CivicAddressField.HEALTH_SERVICE_DELIVERY_AREA.getString()),
                    criteriaFacilityInfo.getHsda().get("HSDA"),
                    "Viewing facility has unexpected service delivery area.");

            workflow.getPlrWebAccessActions().openSearchFacility();
            resultsIndex++;
        }

        criteriaPanel = page.expandSearchCriteria(true);
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
        final String expectedMessage = warningList.getString("missingCriteria");
        SearchFacilityResultsFragment searchResults = null;
        List<String> queryDetails = Arrays.asList("", "", "", "", "", "Select One", "", "");
        List<String> facilityData = Arrays.asList(
                dummyFacility.getName(),
                dummyAddress.get("line1"),
                dummyAddress.get("line1"),
                dummyAddress.get("city"), "",
                "BUILDING",
                dummyFacility.getHsda().get("HSDA") + " (HSDA)");

        // Test Start
        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

        for (int index = 0; index < facilityData.size(); index++)
        {
            if (index == 4) continue;
            queryDetails.set(index, facilityData.get(index));
            searchResults = searchByCriteria(page, queryDetails, false);

            assertTrue(searchResults.grabResultsRowCount() >= 0,
                    "Search Results returned unsuccessfully when only specifying " +
                            CriteriaTabAttribute.queryFieldList.get(index).getIndex());

            if (index == 5) queryDetails.set(index, "Select One");
            else queryDetails.set(index, "");
        }
        int searchResultsCount = searchResults.grabResultsRowCount();

        // All Criteria Empty
        searchResults = searchByCriteria(page, queryDetails, true);
        List<String> warningMessageList = page.waitForAlertMessagesFragment().grabWarningMessageList();

        assertTrue(warningMessageList.contains(expectedMessage),
                "Missing minimum requirements message warning not displayed.");
        assertEquals(searchResults.grabResultsRowCount(), searchResultsCount,
                "Error query changed query results unexpectedly.");
    }

    @Test
    // F1-008. Facility Attribute Search Rules - Logical
    public void testFacilitySearchRulesLogical()
    {
        final List<String> search1Details = Arrays.asList(
                dummyFacility.getName(), "",
                dummyAddress.get("line1").toUpperCase(),
                "", "", "BUILDING", "", "");
        final List<String> search2Details = Arrays.asList("",
                dummyAddress.get("line1"), "",
                dummyAddress.get("city").charAt(0) + dummyAddress.get("city").substring(1,3).toLowerCase(),
                dummyAddress.get("city").charAt(0) + dummyAddress.get("city").substring(1).toLowerCase(),
                "Select One",
                dummyFacility.getHsda().get("HSDA").substring(0, 4), dummyFacility.getHsda().get("HSDA"));
        final PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        final SearchFacilityActions actions = workflow.getSearchFacilityActions();
        ViewFacilityPage viewDetails;

        // Test Start
        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);

        // Search 1 (Facility Name, Other Address, Facility Type)
        searchByCriteria(page, search1Details, false);

        viewDetails = actions.openSearchResults(0);
        LinkedHashMap<String,String> identifierMap = viewDetails.grabIdentifiersBlockContent(0);
        LinkedHashMap<String,String> otherMap = viewDetails.grabOtherAddressBlockContent(0);

        assertTrue(viewDetails.getViewHeader().grabViewTitle().contains(search1Details.getFirst()),
                "Facility is missing expected " + CriteriaTabAttribute.FACILITY_NAME.getString());
        assertEquals(otherMap.get(OtherAddressField.ADDRESS_LINE_1.getString()), search1Details.get(2),
                "Facility is missing expected " + CriteriaTabAttribute.OTHER_ADDRESS.getString());
        assertEquals(identifierMap.get(IdentifierField.FACILITY_TYPE.getString()), search1Details.get(5),
                "Facility is missing expected " + CriteriaTabAttribute.FACILITY_TYPE.getString());

        workflow.getPlrWebAccessActions().openSearchFacility();

        // Search 2 (Civic Address, City, Service Delivery Area)
        searchByCriteria(page, search2Details, false);

        viewDetails = actions.openSearchResults(0);
        LinkedHashMap<String,String> civicMap = viewDetails.grabCivicAddressBlockContent();

        assertEquals(civicMap.get(CivicAddressField.ADDRESS_LINE_1.getString()), search2Details.get(1),
                "Facility is missing expected " + CriteriaTabAttribute.CIVIC_ADDRESS.getString());
        assertEquals(civicMap.get(CivicAddressField.CITY.getString()), search2Details.get(4).toUpperCase(),
                "Facility is missing expected " + CriteriaTabAttribute.CITY.getString());
        assertEquals(civicMap.get(CivicAddressField.HEALTH_SERVICE_DELIVERY_AREA.getString()), search2Details.getLast(),
                "Facility is missing expected " + CriteriaTabAttribute.SERVICE_DELIVERY_AREA.getString());
    }

    @Test
    // F1-009. Service Delivery Area Recognition
    public void testServiceDeliveryArea()
    {
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        SearchFacilityCriteriaFragment criteriaPanel = searchFacility.expandSearchCriteria(true);

        assertTrue(searchFacility.isCriteriaSectionExpanded(),
                "Search by Criteria failed to expand");

        criteriaPanel.getServiceDeliveryAreaMenu().displayAutocomplete(SearchFacilityConstants.sdaPrefix1, true);

        assertTrue(criteriaPanel.getServiceDeliveryAreaMenu().grabAutocompletePanelActive(),
                "Service Delivery Area autocomplete failed to appear");

        criteriaPanel.getServiceDeliveryAreaMenu().selectItemFromPanel(SearchFacilityConstants.sdaPrefix2);

        assertEquals(criteriaPanel.getServiceDeliveryAreaMenu().grabCompletedItem(), SearchFacilityConstants.sdaExpected,
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