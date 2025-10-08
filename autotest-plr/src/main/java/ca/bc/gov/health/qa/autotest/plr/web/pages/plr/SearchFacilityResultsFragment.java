package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

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
     * @param index     the index of row to search for in the search results
     * @return  a WebElement reference of the row at the requested index
     * @throws  IllegalStateException   if no row at the requested index is found, or another error has occurred with finding a unique row at the specified index
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
}
