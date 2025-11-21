package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.*;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Helper class with commonly-used flows to more easily orchestrate tests across the PLR site
 */
public final class TestHelper {

    private static final SecureRandom RNG = new SecureRandom();
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
     * Navigate to the "Search Provider" page
     *
     * @param workflowManager   the workflow manager from the test class
     * @param userType          the UserType to log in as and navigate to the Search Provider Page with
     * @return                  a SearchProviderPage reference to the workflow's search provider page component
     */
    public static SearchProviderPage navigateToSearchProviderPage(
            PlrWebWorkflowManager workflowManager, UserType userType)
    {
        PlrWebWorkflow workflow = logIn(workflowManager, userType);
        SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();

        // System displays Search by Facility page correctly
        searchProvider.waitForReady();

        return searchProvider;
    }

    public static AddFacilityPage navigateToAddFacilityPage(PlrWebWorkflowManager workflowManager)
    {
        PlrWebWorkflow workflow = logIn(workflowManager, UserType.ADMIN);
        AddFacilityPage addFacility = workflow.getPlrWebAccessActions().openAddFacility();

        addFacility.waitForReady();

        return addFacility;
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

    /**
     * Searches by Identifier in the Search Provider page.
     *
     * @param searchProvider    the search provider page reference
     * @param queryFields       a list of strings of query details to fill fields with.
     *                          Index 0: Identifier Type
     *                          Index 1: Provider ID
     * @return                  a SearchProviderResultsFragment reference to the search results of the identifier query
     */
    public static SearchProviderResultsFragment searchProviderByIdentifier(
            SearchProviderPage searchProvider, List<String> queryFields)
    {
        return searchProvider.searchByIdentifier(queryFields.getFirst(), queryFields.get(1));
    }

    /**
     * Navigates to a provider page by its identifier.
     *
     * @param workflowManager   the workflow manager from the test class
     * @param queryFields       the identifier fields (identifier type, then provider ID) to input into search
     * @param userType          the user type to login to PLR as
     * @return                  a ViewProviderPage reference to the provider page
     */
    public static ViewProviderPage viewProviderByIdentifier(
            PlrWebWorkflowManager workflowManager, List<String> queryFields, UserType userType)
    {
        PlrWebWorkflow workflow = workflowManager.selectWorkflow(userType);
        SearchProviderPage searchProvider = navigateToSearchProviderPage(workflowManager, userType);
        searchProviderByIdentifier(searchProvider, queryFields);

        return workflow.getSearchProviderActions().openSearchResults(0);
    }

    /**
     * Creates and submits a new test facility by going through the full Add Facility flow.
     * The address specified should ideally have minimal duplicates within the system already.
     * Set recommended to false only if you know for *certain* the address name will never run into a
     * recommended correction widget. This will speed up the method but will cause errors if a recommended widget
     * unexpectedly appears
     *
     * @param workflowManager   the workflow manager from the test class
     * @param addressData       a list of strings of the data needed for the civic address.
     *                          the first two elements should be the lower and upper limits for a
     *                          randomly generated address number, then the final element should be
     *                          "{ADDRESS_NAME}, {CITY PREFIX}" e.g. DOUGLAS ST, VICTORIA.
     * @param testType          a string to be used in the facility name - used to specify the type of
     *                          test being run currently
     * @param maxAttempts       the maximum amount of types to attempt finding a usable non-duplicate address
     * @return                  a ViewFacilityPage reference to the newly submitted facility
     */
    public static ViewFacilityPage createAndSubmitFacility(
            PlrWebWorkflowManager workflowManager,List<String> addressData, String testType, int maxAttempts)
    {
        final int ADDRESS_LOWER_LIMIT = Integer.parseInt(addressData.get(0));
        final int ADDRESS_UPPER_LIMIT = Integer.parseInt(addressData.get(1));
        final String CIVIC_ADDRESS = addressData.get(2);

        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.fillFacilitySection(
                String.format("%s Test Facility", testType), String.format("%s Test Description", testType));
        addFacility.clickNext("Facility", "");

        AddFacilityAddressFragment addressInfo = null;
        int addressAttempts = 0;
        while (addressAttempts < maxAttempts)
        {
            while (addressInfo == null)
            {
                try
                {
                    int ADDRESS_NUM = ADDRESS_LOWER_LIMIT +
                            RNG.nextInt(ADDRESS_UPPER_LIMIT - ADDRESS_LOWER_LIMIT + 1);
                    String ADDRESS = String.format("%d %s", ADDRESS_NUM, CIVIC_ADDRESS);
                    addressInfo = addFacility.fillAddressSection(ADDRESS, ADDRESS);
                } catch (IllegalStateException ignored) {}
            }

            try
            {
                addFacility.clickNext("Address", "Civic");
                addressInfo.handleWidgetButton("Civic");
            } catch (IllegalStateException ignored) {}

            try
            {
                addFacility.waitForWidgetVisibility("Unknown");
                addressInfo.handleWidgetButton("Unknown");
                addressInfo = null;
                addressAttempts++;
                continue;
            } catch (IllegalStateException ignored) {}

            try
            {
                addFacility.waitForWidgetVisibility("Duplicate");
                addressInfo.handleWidgetButton("Duplicate");
                addressAttempts++;
                addressInfo = null;
                continue;
            } catch (IllegalStateException ignored) {}

            break;
        }
        if (addressAttempts == maxAttempts)
        {
            throw new IllegalStateException("No available civic address found after " + maxAttempts + " attempts");
        }
        workflowManager.getSelectedWorkflow().getSeleniumSession().setWaitTimeout(Duration.ofSeconds(15));

        addFacility.waitForAddFacilityStep("Address", false);
        return addFacility.getFacilitySummary().clickSubmitButton();
    }
}
