package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.SearchFacilityConstants;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.SearchFacilityActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.PlrNavigationMenuFragment;
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
import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.searchByIdentifier;
import static org.testng.Assert.*;

/** Tests class (complex) for the Search Facility page */
public class SearchFacilityComplexTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject warningList;
    private static FHIRController fhirController;

    private MaintainFacilityBuilder dummyFacility;
    private Map<String,String> dummyAddress;

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    private SearchFacilityComplexTests() {
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
    private void teardown() {
        fhirController.close();

        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }

    @BeforeTest
    private void beforeTest() {
        fhirController = new FHIRController(UserType.ADMIN);

        FacilityMaintainConfig dummyCfg = new FacilityMaintainConfig();
        dummyFacility = fhirController.createFacility(dummyCfg);
        dummyAddress = dummyFacility.getAddress();
    }

    @BeforeMethod
    private void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn())
        {
            workflow.login().openPlr();
        }
    }

    /** F1-010. Facility Search Rules */
    @Test
    public void testFacilitySearchRules()
    {
        final SearchFacilityActions actions = workflowManager_.getSelectedWorkflow().getSearchFacilityActions();
        final String expectedName = dummyFacility.getName();
        final String expectedCivicAddress = dummyAddress.get("line1") + ",\n" + dummyAddress.get("city");
        final String expectedOtherAddress = dummyAddress.get("line1");
        final String expectedWarningMessage = warningList.getString("missingCriteria");
        final List<String> wildcardTypes = Arrays.asList(
                "trailingWildcard", "precedingWildcard", "middleWildcard", "multipleWildcard");

        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();

        // Facility Name Steps
        LinkedHashMap<String,String> wildcardQueries = actions.setupWildcards(expectedName);
        List<String> queryDetails = Arrays.asList(expectedName, "", "", "", "", "Select One", "", "");

        for (String wildcardType : wildcardTypes)
            actions.wildcardNameCheck(searchFacility, wildcardType, queryDetails, wildcardQueries);

        // Civic Address Steps
        wildcardQueries = actions.setupWildcards(expectedCivicAddress);
        queryDetails = Arrays.asList("", expectedCivicAddress, "", "", "", "Select One", "", "");

        for (String wildcardType : wildcardTypes)
            actions.wildcardCivicCheck(searchFacility, wildcardType, queryDetails, wildcardQueries);

        // Other Address Steps
        wildcardQueries = actions.setupWildcards(expectedOtherAddress);
        queryDetails = Arrays.asList("", "", expectedOtherAddress, "", "", "Select One", "", "");

        for (String wildcardType : wildcardTypes)
        {
            actions.wildcardOtherCheck(searchFacility, wildcardType, queryDetails, wildcardQueries);
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

    /** F1-012. Alphabetical Sorting of Facility Search Results */
    @Test
    public void testAlphabeticalSorting()
    {
        final String uniqueSuffix = generateAlphabetString(5);
        final MaintainFacilityBuilder numberFacility1 = fhirController.createFacility(
                new FacilityMaintainConfig().withName("Number 2 Facility " + uniqueSuffix));
        final MaintainFacilityBuilder numberFacility2 = fhirController.createFacility(
                new FacilityMaintainConfig().withName("Number 5 Facility " + uniqueSuffix));
        final MaintainFacilityBuilder uppercaseFacility1 = fhirController.createFacility(
                new FacilityMaintainConfig().withName("Facility " + uniqueSuffix));
        final MaintainFacilityBuilder uppercaseFacility2 = fhirController.createFacility(
                new FacilityMaintainConfig().withName("Uppercase Facility " + uniqueSuffix));
        final MaintainFacilityBuilder lowercaseFacility1 = fhirController.createFacility(
                new FacilityMaintainConfig().withName("facility " + uniqueSuffix));
        final MaintainFacilityBuilder lowercaseFacility2 = fhirController.createFacility(
                new FacilityMaintainConfig().withName("lowercase facility " + uniqueSuffix));
        final List<String> queryDetails = Arrays.asList("*" + uniqueSuffix, "", "", "", "", "Select One", "", "");

        SearchFacilityResultsFragment searchResults;

        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        searchResults = searchByCriteria(page, queryDetails, false);

        List<String> facilityNameList = searchResults.getFacilityNamesList();

        // Facilities with no name not covered by test cases - manual removal from consideration for sorting
        while (facilityNameList.contains(SearchFacilityConstants.noNameFacility))
            facilityNameList.remove(SearchFacilityConstants.noNameFacility);

        List<String> sortedNameList = new ArrayList<>(facilityNameList);
        Collections.sort(sortedNameList);

        assertTrue(facilityNameList.contains(numberFacility1.getName()),
                "Facility with number as starting character not present");
        assertTrue(facilityNameList.contains(numberFacility2.getName()),
                "Facility with number as starting character not present");
        assertTrue(facilityNameList.contains(uppercaseFacility1.getName()),
                "Facility with uppercase character as starting character not present");
        assertTrue(facilityNameList.contains(uppercaseFacility2.getName()),
                "Facility with uppercase character as starting character not present");
        assertTrue(facilityNameList.contains(lowercaseFacility1.getName()),
                "Facility with lowercase character as starting character not present");
        assertTrue(facilityNameList.contains(lowercaseFacility2.getName()),
                "Facility with lowercase character as starting character not present");

        assertEquals(facilityNameList, sortedNameList,
                "Returned search results and sorted search results do not match.");
    }

    /** F1-015. Maximum Search Results */
    @Test
    public void testMaximumResults()
    {
        /* TODO: very costly - a fhir endpoint query for criteria would save a lot on unnecessary facility creation
        for (int count = 0; count <= SearchFacilityConstants.maximumResults; count++)
        {
            fhirController.createFacility(new FacilityMaintainConfig().withName("A" + generateAlphabetString(8)));
        }
         */
        final String maxResultsWarning = warningList.getString("maximumResults");
        final List<String> queryDetails = Arrays.asList("A*", "", "", "", "", "Select One", "", "");

        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, UserType.ADMIN);
        SearchFacilityResultsFragment searchResults = searchByCriteria(page, queryDetails, false);

        List<String> warningMessageList = page.waitForAlertMessagesFragment().grabWarningMessageList();

        assertEquals(searchResults.grabResultsRowCount(), SearchFacilityConstants.maximumResults,
                "Returned search result does not match the expected maximum number of search results.");
        assertTrue(warningMessageList.contains(maxResultsWarning),
                "Maximum search results warning not displayed.");
        assertTrue(searchResults.getFormResults().contains(SearchFacilityConstants.maximumResults + " results"),
                "Form result does not match expected maximum search results.");
    }

    /** F1-016. Search Results Limited By Data Permissions */
    @Test(dataProvider = "facilityTestUserTypes", dataProviderClass = InjectableData.class)
    public void testDataPermissions(UserType userType)
    {
        final List<String> queryFields = Arrays.asList("IFC", dummyFacility.getIdentifier());
        SearchFacilityResultsFragment searchResults;

        // Test Start
        PlrWebWorkflow workflow = logIn(workflowManager_, userType);
        PlrNavigationMenuFragment menu = workflow.getPlrWebAccessActions().waitForPlrNavigationMenuFragment();

        assertTrue(menu.grabItemVisible(PlrNavigationMenuFragment.Item.SEARCH_FACILITY),
                "Search Facility not visible as a menu option for User Type" + userType);

        if (userType.equals(UserType.ADMIN))
            assertTrue(menu.grabItemVisible(PlrNavigationMenuFragment.Item.ADD_FACILITY),
                    "Add Facility not visible as a menu option for Reg Admin User");
        else
            assertFalse(menu.grabItemVisible(PlrNavigationMenuFragment.Item.ADD_FACILITY),
                    "Add Facility unexpectedly visible as a menu option for " + userType);

        SearchFacilityPage page = navigateToSearchFacilityPage(workflowManager_, userType);
        searchResults = searchByIdentifier(page, queryFields, false);

        assertTrue(searchResults.grabResultsRowCount() > 0,
                "Search results did not return for User Type " + userType);

        workflowManager_.logoutAndClose(userType);
    }
}
