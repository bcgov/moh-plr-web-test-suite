package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * Fragment class for the search results when searching by provider
 */
public class SearchProviderResultsFragment
extends BasicWebPageFragment
{
    private static final String RESULTS_FORM_CSS       = "form#searchResultsForm";
    private static final String RESULTS_TABLE_DATA_CSS = "tbody#searchResultsForm\\:tbl_data";

    /**
     * Initializes fragment and changes selenium's main locator to point to the search results container
     *
     * @param selenium  The current SeleniumSession
     */
    public SearchProviderResultsFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("span#searchResultsGroup"));
    }

    /**
     * Grab a specific row from the results table when searching by provider
     *
     * @param index the index of the row to grab from the results table
     * @return      A list of strings representing the data in the specified row index
     */
    public List<String> grabResultsRow(int index)
    {
        List<String> dataList = new ArrayList<>();
        WebElement row = findResultsRow(index);
        for (WebElement entry: row.findElements(By.cssSelector("td")))
        {
            dataList.add(entry.getText());
        }
        return dataList;
    }

    /**
     * Grab the number of rows in the results table when searching by provider
     *
     * @return The number of rows in the results table
     */
	public int grabResultsRowCount() {
		int even = findResultsTableData().findElements(By.cssSelector("tr.ui-datatable-even")).size();
		int odd = findResultsTableData().findElements(By.cssSelector("tr.ui-datatable-odd")).size();
		// return
		// findResultsTableData().findElements(By.cssSelector("tr[role='row']")).size();
		return even + odd;
	}

    /**
     * Grabs the message displayed when no results are found in the results table
     *
     * @return The string message displayed in the results table if no results are returned
     * @throws IllegalStateException If the number of rows in the results table is nonzero
     */
    public String grabEmptyResultsMessage()
    {
        if (grabResultsRowCount() > 0)
        {
            String msg = "Results are non-empty.";
            throw new IllegalStateException(msg);
        }
        return findResultsTableData()
                .findElement(By.cssSelector("tr.ui-datatable-empty-message"))
                .getText();
    }

    /**
     * Grabs the results summary text (number of results + time taken)
     *
     * @return                       The results summary string
     * @throws IllegalStateException If unable to retrieve the results summary
     */
    public String grabResultsSummary()
    {
        String resultsSummary;
        String[] textLines = selenium_.grabText(By.cssSelector(RESULTS_FORM_CSS)).split("\\R");
        if (textLines.length > 0)
        {
            resultsSummary = textLines[0];
        }
        else
        {
            throw new IllegalStateException("Failed to retrieve search results summary.");
        }
        return resultsSummary;
    }

    /**
     * Opens a result from the results table when searching by provider
     *
     * @param index the index of the row to open from the results table
     */
    public void openResults(int index)
    {
        WebElement row = findResultsRow(index);
        row.findElement(By.cssSelector("td > a")).click();
        waitForAbsent();
    }

    private WebElement findResultsRow(int index)
    {
        WebElement row;
        By rowLocator = By.cssSelector("tr[data-ri='" + index + "']");
        List<WebElement> rowList = findResultsTableData().findElements(rowLocator);
        if (rowList.size() == 1)
        {
            row = rowList.getFirst();
        }
        else
        {
            String msg = String.format(
                    "Failed to retrieve search results (row index: %s).", index);
            throw new IllegalStateException(msg);
        }
        return row;
    }

    private WebElement findResultsTableData()
    {
        return selenium_.findElement(By.cssSelector(RESULTS_TABLE_DATA_CSS));
    }
}
