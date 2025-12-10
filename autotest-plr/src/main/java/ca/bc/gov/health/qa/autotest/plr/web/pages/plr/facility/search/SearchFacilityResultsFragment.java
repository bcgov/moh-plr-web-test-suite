package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.search;

import java.util.List;
import java.util.ArrayList;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Fragment class for the search results when searching by facility
 */
public class SearchFacilityResultsFragment extends BasicWebPageFragment
{
    private static final String RESULTS_TABLE_DATA_CSS = "tbody#searchResultsForm\\:tbl_data";

    /**
     * Initializes fragment and changes selenium's main locator to point to the search results container
     *
     * @param   selenium    The current SeleniumSession
     */
    public SearchFacilityResultsFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("span#searchResultsGroup"));
    }

    /**
     * Find the table of search results with Selenium
     *
     * @return  The table of search results as a WebElement
     */
    private WebElement findResultsTableData()
    {
        return selenium_.findElement(By.cssSelector(RESULTS_TABLE_DATA_CSS));
    }

    /**
     * Get the number of rows in the search results table
     *
     * @return  The number of rows in the table of search results
     */
    public int grabResultsRowCount()
    {
        return findResultsTableData().findElements(By.cssSelector("tr[data-ri]")).size();
    }

    /**
     * Get the message displayed when no search results are found
     *
     * @return  The string message displayed in the table of search results if no results are returned
     * @throws  IllegalStateException   If the number of rows in the table of search results is nonzero
     */
    public String grabEmptyResultsMessage()
    {
        if (grabResultsRowCount() > 0)
        {
            String msg = "Results are non-empty.";
            throw new IllegalStateException(msg);
        }
        return findResultsTableData().findElement(By.cssSelector("tr.ui-datatable-empty-message")).getText();
    }

    /**
     * Finds a row in the table of search results when searching by facility
     *
     * @param index                     the index of row to search for in the search results
     *
     * @return                          a WebElement reference of the row at the requested index
     *
     * @throws  IllegalStateException   if no row at the requested index is found,
     *                                  or another error has occurred with finding a unique row at the specified index
     */
    public WebElement findResultsRow(int index)
    {
        WebElement row;
        By rowLocator = By.cssSelector(
                new StringBuilder("tr[data-ri='").append(index).append("']").toString()
        );
        List<WebElement> rowList = findResultsTableData().findElements(rowLocator);
        if (rowList.size() == 1) row = rowList.get(0);
        else
        {
            String msg = String.format("Failed to retrieve search results (row index: %s).", index);
            throw new IllegalStateException(msg);
        }
        return row;
    }

    /**
     * Gets a row in the table of search results when searching by facility.
     *
     * @param index     the index of row to get from the table of search results
     * @return  a list of strings for each cell in the search results row at index
     */
    public List<String> getResultsRow(int index)
    {
        List<String> dataList = new ArrayList<>();
        WebElement row = findResultsRow(index);
        for (WebElement entry : row.findElements(By.cssSelector("td")))
        {
            dataList.add(entry.getText());
        }
        return dataList;
    }

    /**
     * Clicks the link to view a facility in the table of search results
     *
     * @param index     the index of row to click the facility link
     */
    public void openResults(int index)
    {
        WebElement row = findResultsRow(index);
        row.findElement(By.cssSelector("td > a")).click();
        waitForAbsent();
    }

    /**
     * Gets the full search results as text, including the form message of result count and time taken
     *
     * @return  a string of the full search results as text
     */
    public String getFormResults()
    {
        return selenium_.findElementByCss("form#searchResultsForm").getText();
    }

    /**
     * Gets the facility names of each record in the search results
     * The facility description is discarded in the results
     *
     * @return  a list of strings with each facility name in the search results
     */
    public List<String> getFacilityNamesList()
    {
        List<String> facNameList = new ArrayList<>();
        for (int facilityCount = 0; facilityCount < grabResultsRowCount(); facilityCount++)
        {
            String facName = getResultsRow(facilityCount).getFirst();
            if (facName.contains(",")) facName = facName.substring(0, facName.indexOf(','));
            facNameList.add(facName);
        }
        return facNameList;
    }

    /**
     * Gets the civic addresses for each record in the search results
     * The country is discarded in the results, see comments for explanation
     *
     * @return  a list of strings with each civic address in the search results
     */
    public List<String> getCivicAddressList()
    {
        List<String> civicAddressList = new ArrayList<>();
        for (int facilityCount = 0; facilityCount < grabResultsRowCount(); facilityCount++)
        {
            String civicAddress = getResultsRow(facilityCount).get(2);
            /* Slight inaccuracy in testing - the country in the Civic Address is searchable, but the output in search
               results does not match what is required in the search query.
               e.g. To search for British Columbia facilities, a reference to "BC" is required in the search. The
                    search results after this will display the civic address as "British Columbia", not "BC".
                    Attempting to search for "British Columbia" will result in no results being returned. */
            civicAddressList.add(civicAddress.substring(0, civicAddress.lastIndexOf(',')));
        }
        return civicAddressList;
    }

    /**
     * Gets the header values for each column in the table of search results
     *
     * @return  a list of strings for the name of each column in the table of search results
     */
    public List<String> getTableColumns()
    {
        List<String> headerList = new ArrayList<>();
        List<WebElement> webElementList = selenium_.findElementsByCss(
                "thead#searchResultsForm\\:tbl_head > tr > th");
        for (WebElement headerElement : webElementList) headerList.add(headerElement.getText());
        return headerList;
    }

    /**
     * Determines whether a row of the table of search results contains the CSS styles necessary to word wrap
     * (so all text content is visible on the page)
     *
     * @param rowIndex  the index of the row in the table of search results to check for word wrapping
     * @return          whether the row of table of search results has
     *                  white-space set to "normal" and word-break set to "break-all".
     */
    public boolean verifyWordWrapStyle(int rowIndex)
    {
        WebElement test = findResultsRow(rowIndex);
        boolean expectedWhiteSpace = test.findElement(By.cssSelector("td")).getAttribute("style")
                .contains("white-space: normal");
        boolean expectedWordBreak = test.findElement(By.cssSelector("td")).getAttribute("style")
                .contains("word-break: break-all");
        return expectedWhiteSpace && expectedWordBreak;
    }
}
