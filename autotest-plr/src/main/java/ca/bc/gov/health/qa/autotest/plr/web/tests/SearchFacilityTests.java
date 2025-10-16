package ca.bc.gov.health.qa.autotest.plr.web.tests;

import java.util.*;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.SearchFacilityActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.SearchFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.SearchFacilityResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SearchFacilityTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public SearchFacilityTests() {}

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
    // Search Facility : Minimum Requirements Search by Identifier - No Criteria
    public void testMinReqsIdentifierNoCriteria()
    {
        final String expectedMessage1 = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Facility Identifier Type'. Your transaction has not been processed. Correct and resubmit.";
        final String expectedMessage2 = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Facility Identifier'. Your transaction has not been processed. Correct and resubmit.";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        searchFacility.searchByIdentifier("Select One", "", true);
        List<String> errorMessageList = searchFacility.waitForAlertMessagesFragment().grabErrorMessageList();

        assertTrue(errorMessageList.contains(expectedMessage1), "Missing minimum requirement of Facility Identifier Type error not displayed.");
        assertTrue(errorMessageList.contains(expectedMessage2), "Missing minimum requirement of Facility Identifier error not displayed.");
    }

    @Test
    // Search Facility : Minimum Requirements Search by Identifier - Identifier Type Only
    public void testMinReqsIdentifierType()
    {
        final String expectedMessage = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Facility Identifier'. Your transaction has not been processed. Correct and resubmit.";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        searchFacility.searchByIdentifier("IFC", "", true);
        List<String> errorMessageList = searchFacility.waitForAlertMessagesFragment().grabErrorMessageList();

        assertTrue(errorMessageList.contains(expectedMessage), "Missing minimum requirements error not displayed.");
    }

    @Test
    // Search Facility : Minimum Requirements Search by Identifier - Facility Identifier Only
    public void testMinReqsFacilityIdentifier()
    {
        final String expectedMessage = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Facility Identifier Type'. Your transaction has not been processed. Correct and resubmit.";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        searchFacility.searchByIdentifier("Select One", "ABC.123", true);
        List<String> errorMessageList = searchFacility.waitForAlertMessagesFragment().grabErrorMessageList();

        assertTrue(errorMessageList.contains(expectedMessage), "Missing minimum requirements error not displayed.");
    }

    @Test
    // Search Facility : Minimum Requirements Search by Criteria
    public void testMinReqsCriteriaNoCriteria()
    {
        final String expectedMessage = "Invalid or Incomplete form data provided, please see instructions on the right side.";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();
        searchFacility.searchByCriteria(
                "", "", "", "", null,
                "Select One", "", null, true);
        List<String> warningMessageList = searchFacility.waitForAlertMessagesFragment().grabWarningMessageList();

        assertTrue(warningMessageList.contains(expectedMessage), "Missing minimum requirements message warning not displayed.");
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
