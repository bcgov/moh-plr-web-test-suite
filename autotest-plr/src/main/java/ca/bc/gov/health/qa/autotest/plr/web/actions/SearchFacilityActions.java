package ca.bc.gov.health.qa.autotest.plr.web.actions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.search.SearchFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.search.SearchFacilityResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.searchByCriteria;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Actions class for the Search Facility page/functions
 */
public class SearchFacilityActions {
    private final SeleniumSession selenium_;

    private static final Logger LOG = ExecutionLogManager.getLogger();

    /**
     * Initializes class and SeleniumSession.
     *
     * @param selenium  the current SeleniumSession
     */
    public SearchFacilityActions(SeleniumSession selenium) { selenium_ = selenium; }

    /**
     * Opens the view facility page for a facility in the table of search results.
     *
     * @param index     the index of row in the table of search results to view the facility of
     * @return  the view facility page for the associated facility
     */
    public ViewFacilityPage openSearchResults(int index)
    {
        SearchFacilityResultsFragment searchResults = new SearchFacilityResultsFragment(selenium_);
        searchResults.waitForReady();
        searchResults.openResults(index);
        ViewFacilityPage viewFacility = new ViewFacilityPage(selenium_);
        viewFacility.waitForReady();
        return viewFacility;
    }

    /**
     * Checks the Column Names of the table of search results
     *
     * @param searchResults     the SearchFacilityResultsFragment reference
     */
    public void checkColumns(SearchFacilityResultsFragment searchResults)
    {
        int index = 0;
        for (String tableColumn : List.of("Facility Name", "Identifier", "Civic Address"))
        {
            assertTrue(searchResults.getTableColumns().get(index).contains(tableColumn),
                    String.format("Table column %d is not %s", index, tableColumn));
            index++;
        }
    }

    /**
     * creates a map of wildcard queries - helper function for wildcard test case
     *
     * @param criteriaField     the string to be creating wildcard queries for
     * @return                  a map of wildcard queries to be used in a criteria field
     */
    public LinkedHashMap<String,String> setupWildcards(String criteriaField)
    {
        LinkedHashMap<String,String> wildcardMap = new LinkedHashMap<>();
        wildcardMap.put("trailingWildcard", criteriaField.substring(0, 2) + "*");
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
                LOG.info("wildcard Field: " + wildcardField);
                LOG.info("expected ending: " + wildcardQueries.get("lastExpectedChar"));
                LOG.info("actual ending: " + wildcardField.charAt(wildcardField.length()-1));
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
    public void wildcardNameCheck(SearchFacilityPage searchFacility, String wildcardType,
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
    public void wildcardCivicCheck(SearchFacilityPage searchFacility, String wildcardType,
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
     * Helper function for handling other address wildcard query and assertion
     *
     * @param searchFacility    The search facility page reference
     * @param wildcardType      Which wildcard type to test against (key for wildcardQueries)
     * @param queryDetails      List of default query details
     * @param wildcardQueries   Map of wildcard queries to test against (based on wildcardType)
     */
    public void wildcardOtherCheck(SearchFacilityPage searchFacility, String wildcardType,
                                   List<String> queryDetails, LinkedHashMap<String,String> wildcardQueries)
    {
        queryDetails.set(2, wildcardQueries.get(wildcardType));
        SearchFacilityResultsFragment searchResults = searchByCriteria(searchFacility, queryDetails, false);
        assertTrue(searchResults.grabResultsRowCount() > 0,
                "Searching civic address with " + wildcardType + " unexpectedly returns no testable results");

        ViewFacilityPage searchDetails = openSearchResults(0);
        LinkedHashMap<String,String> otherMap = searchDetails.grabDataBlockContent(
                FacilitySection.OTHER_ADDRESS,0);
        wildcardCases(otherMap.get("Address Line 1"), wildcardType, wildcardQueries);
    }
}
