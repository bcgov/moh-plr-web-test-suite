package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TODO
 */
public class ViewFacilityPage extends BasicWebPage {
    private static final Pattern DATA_KEY_SUFFIX_PATTERN = Pattern.compile(":$");
    private static final Pattern DATA_KEY_PARENS_PATTERN = Pattern.compile(" \\(.*\\)");
    private final ViewHeaderFragment viewHeader_;

    /**
     * TODO
     *
     * @param selenium
     */
    public ViewFacilityPage(SeleniumSession selenium) { this(selenium, null); }


    /**
     * TODO
     *
     * @param selenium
     * @param uri
     */
    public ViewFacilityPage(SeleniumSession selenium, URI uri)
    {
        super(selenium, By.cssSelector("span#facilityDetailsGroup"), "View Facility Details", uri);
        viewHeader_ = new ViewHeaderFragment(selenium);
    }

    /**
     * TODO
     *
     * @return
     */
    public ViewHeaderFragment getViewHeader() { return viewHeader_; }

    /**
     * TODO
     *
     * @param section
     * @return
     */
    private String getSectionSelector(FacilitySection section) { return "div#" + section.getPanelId_(); }

    /**
     * TODO
     *
     * @param section
     * @return
     */
    private String getSectionContentSelector(FacilitySection section)
    {
        return getSectionSelector(section) + "_content";
    }

    /**
     * TODO
     *
     * @param section
     * @return
     */
    private String getDataBlocksSelector(FacilitySection section)
    {
        return getSectionContentSelector(section) + " > table.recordDetailsPanels > tbody > tr > td > div.ui-panel";
    }

    /**
     * TODO
     *
     * @param section
     * @param index
     * @return
     */
    private String getDataBlockSelector(FacilitySection section, int index)
    {
        if (index < 0)
        {
            String msg = String.format("Negative index (%d).", index);
            throw new IllegalArgumentException(msg);
        }
        return getDataBlocksSelector(section) + ":nth-of-type(" + (index + 1) + ")";
    }

    /**
     * TODO
     *
     * @param section
     * @param index
     * @return
     */
    public String getDataBlockContentSelector(FacilitySection section, int index)
    {
        return getDataBlockSelector(section, index) + "> div.ui-panel-content";
    }

    /**
     * TODO
     *
     * @param section
     * @param index
     * @return
     */
    private WebElement findDataBlockContent(FacilitySection section, int index)
    {
        return selenium_.findElementByCss(getDataBlockContentSelector(section, index));
    }

    /**
     * TODO
     *
     * @param section
     * @param index
     * @return
     */
    public boolean grabDataBlockExpanded(FacilitySection section, int index)
    {
        return findDataBlockContent(section, index).isDisplayed();
    }

    /**
     * TODO
     *
     * @param section
     * @param index
     * @return
     */
    private String getDataBlockHeaderSelector(FacilitySection section, int index)
    {
        return getDataBlockSelector(section, index) + " > div.ui-panel-titlebar";
    }

    /**
     * TODO
     *
     * @param section
     * @param index
     * @return
     */
    private String getDataBlockHeaderExpandSelector(FacilitySection section, int index)
    {
        return getDataBlockHeaderSelector(section, index) + " > a[title='Expand/Collapse']";
    }

    /**
     * TODO
     *
     * @param section
     * @param index
     * @param expand
     */
    public void expandDataBlock(FacilitySection section, int index, boolean expand)
    {
        if (grabDataBlockExpanded(section, index) != expand)
        {
            WebElement expandCollapseButton = selenium_.findElement(By.cssSelector(getDataBlockHeaderExpandSelector(section, index)));
            selenium_.scrollIntoView(expandCollapseButton);
            expandCollapseButton.click();

            WebElement content = findDataBlockContent(section, index);
            if (expand)
            {
                selenium_.waitUntil(ExpectedConditions.visibilityOf(content));

                selenium_.waitUntil(ExpectedConditions.attributeToBe(content, "overflow", "visible"));
            }
            else
            {
                selenium_.waitUntil(ExpectedConditions.invisibilityOf(content));
            }
        }
    }

    /**
     * TODO
     *
     * @param key
     * @return
     */
    private static String formatDataKey(String key)
    {
        String formattedKey = DATA_KEY_SUFFIX_PATTERN.matcher(key).replaceAll("");
        formattedKey = DATA_KEY_PARENS_PATTERN.matcher(formattedKey).replaceAll("");
        return formattedKey;
    }

    /**
     * TODO
     *
     * @param section
     * @param index
     * @return
     */
    public LinkedHashMap<String,String> grabDataBlockContent(FacilitySection section, int index)
    {
        LinkedHashMap<String,String> dataMap = new LinkedHashMap<>();
        expandDataBlock(section, index, true);
        List<WebElement> dataRowElementList = selenium_.findElements(By.cssSelector(getDataBlockContentSelector(section, index) + " > table > tbody > tr"));
        if (!dataRowElementList.isEmpty())
        {
            selenium_.scrollIntoView(dataRowElementList.getFirst());
        }
        for (WebElement dataRow : dataRowElementList)
        {
            List<WebElement> dataEntryList = dataRow.findElements(By.cssSelector("td"));
            int dataColumnCount = dataEntryList.size();
            if (dataColumnCount == 2 || dataColumnCount == 4)
            {
                dataMap.put(formatDataKey(dataEntryList.get(0).getText()), dataEntryList.get(1).getText());
                if (dataColumnCount == 4)
                {
                    dataMap.put(formatDataKey(dataEntryList.get(2).getText()), dataEntryList.get(3).getText());
                }
            }
            else {
                String msg = String.format("Invalid data row (%s: %d: %s).", section.getTitle(), index, dataRow.getText());
                throw new IllegalStateException(msg);
            }
        }
        return dataMap;
    }

    /**
     * TODO
     *
     * @param dataMap
     * @param dataEntryList
     */
    private void addFieldDataMap(LinkedHashMap<String,String> dataMap, List<WebElement> dataEntryList, int index)
    {
        dataMap.put(formatDataKey(dataEntryList.get(index).findElement(By.cssSelector("td")).getText()),
                dataEntryList.get(index+1).findElement(By.cssSelector("td")).getText());
    }

    public LinkedHashMap<String,String> grabCivicAddressBlockContent(int index)
    {
        LinkedHashMap<String,String> dataMap = new LinkedHashMap<>();
        expandDataBlock(FacilitySection.CIVIC_ADDRESSES, index, true);
        List<WebElement> dataRowElementList = selenium_.findElements(By.cssSelector(getDataBlockContentSelector(FacilitySection.CIVIC_ADDRESSES, index)
                + " > div.ui-outputpanel > table > tbody > tr"));
        if (!dataRowElementList.isEmpty())
        {
            selenium_.scrollIntoView(dataRowElementList.getFirst());
        }
        for (WebElement dataRow : dataRowElementList)
        {
            List<WebElement> dataEntryList = dataRow.findElements(By.cssSelector("td > table > tbody > tr"));
            int dataColumnCount = dataEntryList.size();
            if (dataColumnCount == 2)
            {
                addFieldDataMap(dataMap, dataEntryList, 0);
            }
            else if (dataColumnCount == 4 || dataColumnCount == 5 || dataColumnCount == 6)
            {
                addFieldDataMap(dataMap, dataEntryList.get(0).findElements(By.cssSelector("td > table > tbody > tr")), 0);
                if (dataColumnCount == 5) { addFieldDataMap(dataMap, dataEntryList.get(0).findElements(By.cssSelector("td > table > tbody > tr")), 2); }
            }
            else
            {
                String msg = String.format("Invalid data row (%d: %s).", dataColumnCount, dataRow.getText());
                throw new IllegalStateException(msg);
            }
        }
        return dataMap;
    }
}
