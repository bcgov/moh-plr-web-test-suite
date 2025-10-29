package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.SearchFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.SearchFacilityResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;

import java.util.List;

public final class TestHelper {
    /**
     * Logs into PLR with a specific userType (if it hasn't been logged in already)
     *
     * @param workflowManager   the workflow manager from the test class
     * @param userType          the user type to log into PLR as
     * @return                  the PlrWebWorkflow reference to the workflow logged into PLR as the specified user type
     */
    public static PlrWebWorkflow logIn(PlrWebWorkflowManager workflowManager, UserType userType)
    {
        PlrWebWorkflow workflow = workflowManager.selectWorkflow(userType);
        if (!workflow.isLoggedIn()) workflow.login().openPlr();
        return workflow;
    }

    /**
     * Navigate to the "Search Facility" Page
     *
     * @param workflowManager   the workflow manager from the test class
     * @param userType          the userType to log in as and navigate to the Search Facility Page with
     * @return                  a SearchFacilityPage reference to the workflow's search facility page component
     */
    public static SearchFacilityPage navigateToSearchFacilityPage(
            PlrWebWorkflowManager workflowManager, UserType userType)
    {
        PlrWebWorkflow workflow = logIn(workflowManager, userType);
        SearchFacilityPage searchFacility = workflow.getPlrWebAccessActions().openSearchFacility();

        // System displays Search by Facility page correctly
        searchFacility.waitForReady();

        return searchFacility;
    }

    /**
     * Searches by Identifier in the Search Facility page.
     *
     * @param searchFacility    the search facility page reference
     * @param queryFields       a list of strings of query details to fill fields with.
     *                          Index 0: Facility Identifier Type
     *                          Index 1: Facility Identifier
     * @param expectedError     whether an error is anticipated when executing the query
     * @return                  a SearchFacilityResultsFragment reference to the search results of the identifier query
     */
    public static SearchFacilityResultsFragment searchByIdentifier(
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
    public static SearchFacilityResultsFragment searchByCriteria(
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
     * Navigates to a facility page by its identifier.
     *
     * @param workflowManager   the workflow manager from the test class
     * @param identifier        the facility identifier of the facility to view
     * @param userType          the user type to login to PLR as
     * @return                  a ViewFacilityPage reference to the facility page
     */
    public static ViewFacilityPage viewFacilityByIdentifier(
            PlrWebWorkflowManager workflowManager, String identifier, UserType userType)
    {
        PlrWebWorkflow workflow = workflowManager.selectWorkflow(userType);
        SearchFacilityPage searchFacility = navigateToSearchFacilityPage(workflowManager, userType);
        searchByIdentifier(searchFacility, List.of("IFC", identifier), false);

        return workflow.getSearchFacilityActions().openSearchResults(0);
    }
}
