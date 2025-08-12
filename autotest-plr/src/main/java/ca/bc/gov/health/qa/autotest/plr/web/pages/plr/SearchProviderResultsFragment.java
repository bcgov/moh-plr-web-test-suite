package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * TODO (AZ) - doc
 */
public class SearchProviderResultsFragment
extends BasicWebPageFragment
{
    private static final String RESULTS_FORM_CSS       = "form#searchResultsForm";
    private static final String RESULTS_TABLE_DATA_CSS = "tbody#searchResultsForm\\:tbl_data";

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public SearchProviderResultsFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector("span#searchResultsGroup"));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param index
     *        ???
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public int grabResultsRowCount()
    {
        return findResultsTableData().findElements(By.cssSelector("tr[role='row']")).size();
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     *
     * @throws IllegalStateException
     *         ???
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
     * TODO (AZ) - doc
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @param index
     *        ???
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
        By rowLocator = By.cssSelector(
                new StringBuilder("tr[data-ri='")
                    .append(index)
                    .append("']")
                    .toString());
        List<WebElement> rowList = findResultsTableData().findElements(rowLocator);
        if (rowList.size() == 1)
        {
            row = rowList.get(0);
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
