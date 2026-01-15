package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewHeaderFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.core.util.net.UriUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;

/**
 * A page object class for the View Provider page.
 */
public class ViewProviderPage
extends BasicWebPage
{
    private static final Pattern DATA_KEY_SUFFIX_PATTERN = Pattern.compile(":$");

    private final ViewHeaderFragment viewHeader_;

    /**
     * Initializes page object, overloaded constructor for no specified URL
     *
     * @param selenium the current SeleniumSession
     */
    public ViewProviderPage(SeleniumSession selenium)
    {
        this(selenium, null);
    }

    /**
     * Initializes page object and changes selenium's main locator to View Provider
     * Details heading
     *
     * @param selenium the current SeleniumSession
     * @param uri      the URL to navigate to in inherited methods if applicable
     */
    public ViewProviderPage(SeleniumSession selenium, URI uri)
    {
        super(selenium,
              By.cssSelector("span#providerDetailsGroup"),
              "View Provider Details",
              uri);
        viewHeader_ = new ViewHeaderFragment(selenium);
    }

    /**
     * Expands/collapses a "data block" or a specific instance of data within a
     * provider section
     *
     * @param section the provider section to select
     * @param index   the index of the data block within the provider section to
     *                select
     * @param expand  whether to expand (true) or collapse (false) the data block
     */
    public void expandDataBlock(ProviderSection section, int index, boolean expand)
    {
        if (grabDataBlockExpanded(section, index) != expand)
        {
            // Expand/collapse the data block.
            WebElement expandCollapseButton = selenium_.findElement(
                    By.cssSelector(getDataBlockHeaderExpandSelector(section, index)));
            selenium_.scrollIntoView(expandCollapseButton);
            expandCollapseButton.click();

            // Wait for the content to be expanded/collapsed.
            WebElement content = findDataBlockContent(section, index);
            if (expand)
            {
                selenium_.waitUntil(ExpectedConditions.visibilityOf(content));

                // Wait for the expand animation to complete.
                // NOTE: The value of the CSS property "overflow" is "hidden"
                //       while the transition animation is in progress,
                //       and "visible" when the animation completes.
                selenium_.waitUntil(
                        ExpectedConditions.attributeToBe(content, "overflow", "visible"));
            }
            else
            {
                selenium_.waitUntil(ExpectedConditions.invisibilityOf(content));
            }
        }
    }

    /**
     * Expands a work locations' inner data block
     *
     * @param workEntity    a WebElement reference to the work entity subpanel
     */
    public void expandWorkEntityBlock(WebElement workEntity)
    {
        WebElement expandCollapseButton = workEntity.findElement(By.cssSelector(
                "div.ui-widget-header > a[title='Expand/Collapse']"));
        selenium_.scrollIntoView(expandCollapseButton);
        expandCollapseButton.click();

        WebElement content = workEntity.findElement(By.cssSelector("div.ui-widget-content"));

        selenium_.waitUntil(ExpectedConditions.visibilityOf(content));
        selenium_.waitUntil(
                ExpectedConditions.attributeToBe(content, "overflow", "visible"));

    }

    /**
     * Gets the view header
     *
     * @return a ViewHeaderFragment reference for the current page
     */
    public ViewHeaderFragment getViewHeader()
    {
        return viewHeader_;
    }

    /**
     * Gets the count of active data blocks within a provider section
     *
     * @param section   the provider section to count within
     *
     * @param active    whether to count active (true) or inactive (false) blocks
     *
     * @return          a count of active/inactive data blocks within a provider section
     */
    public int grabActiveDataBlockCount(ProviderSection section, boolean active)
    {
        int count = 0;
        for (int i = 0; i < grabDataBlockCount(section); i++)
        {
            if (grabDataBlockActive(section, i))
            {
                if (active)
                {
                    count++;
                }
            }
            else
            {
                if (!active)
                {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Gets whether a data block is active (true) or inactive (false)
     *
     * @param section   the provider section to check
     *
     * @param index     the index of data block to check
     *
     * @return          whether the specified index and section of data block is active (true) or inactive (false)
     */
    public boolean grabDataBlockActive(ProviderSection section, int index)
    {
        By locator = By.cssSelector(getDataBlockHeaderActiveSelector(section, index));
        return selenium_.searchElement(locator) != null;
    }

    /**
     * Adds inner content of a Work Location data block to a data map used in grabDataBlockContent
     *
     * @param index     the index of work location data block to consider
     * @param dataMap   the dataMap of the work location, from grabDataBlockContent
     */
    private void grabWorkLocationContent(int index, LinkedHashMap<String,String> dataMap)
    {
        final ProviderSection section = ProviderSection.WORK_LOCATIONS;

        List<WebElement> wlEntityList = selenium_.findElements(By.cssSelector(getWorkLocationSubpanelsSelector(index)));
        for (WebElement subPanel : wlEntityList)
        {
            String subPanelName = subPanel.findElement(By.cssSelector(
                    "div.ui-widget-header > span.ui-panel-title")).getText() + "-";
            List<WebElement> workEntityBlockList = subPanel.findElements(By.cssSelector(
                    "div.ui-widget-content > div.ui-widget-content > table > tbody > tr > td > div.ui-subpanel"
            ));
            int workEntityIndex = 0;
            for (WebElement workEntity : workEntityBlockList)
            {
                List<WebElement> dataRowElementList = workEntity.findElements(By.cssSelector(
                        "div.ui-widget-content > table > tbody > tr"
                ));

                expandWorkEntityBlock(workEntity);

                for (WebElement dataRow : dataRowElementList)
                {
                    List<WebElement> dataEntryList = dataRow.findElements(By.cssSelector("td"));
                    int dataColumnCount = dataEntryList.size();
                    switch (dataColumnCount)
                    {
                        case 4:
                            dataMap.put(formatDataKey(subPanelName + workEntityIndex + "-"
                                            + dataEntryList.get(2).getText()),
                                    dataEntryList.get(3).getText());
                        case 2:
                            dataMap.put(formatDataKey(subPanelName + workEntityIndex + "-"
                                            + dataEntryList.get(0).getText()),
                                    dataEntryList.get(1).getText());
                            break;
                        default:
                            String msg = String.format(
                                    "Invalid data row (%s: %d: %s).",
                                    section.getTitle(),
                                    index,
                                    dataRow.getText());
                            throw new IllegalStateException(msg);
                    }
                }
                workEntityIndex++;
            }
        }
    }

    /**
     * Gets the content from a specific data block within a provider section.
     *
     * @param section the provider section to get content from
     * @param index   the index of data block within the facility section to get
     *                content from
     * @return a hash map mapping data block fields (String) to its associated
     *         values (String)
     * @throws IllegalStateException If a specific row of data in the block is
     *                               formatted unexpectedly
     */
    public LinkedHashMap<String,String> grabDataBlockContent(ProviderSection section, int index)
    {
        LinkedHashMap<String,String> dataMap = new LinkedHashMap<>();
        expandDataBlock(section, index, true);
        List<WebElement> dataRowElementList = selenium_.findElements(By.cssSelector(
                getDataBlockContentSelector(section, index) + " > table > tbody > tr"));
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
                dataMap.put(
                        formatDataKey(dataEntryList.get(0).getText()),
                        dataEntryList.get(1).getText());
                if (dataColumnCount == 4)
                {
                    dataMap.put(
                            formatDataKey(dataEntryList.get(2).getText()),
                            dataEntryList.get(3).getText());
                }
            }
            else
            {
                String msg = String.format(
                        "Invalid data row (%s: %d: %s).",
                        section.getTitle(),
                        index,
                        dataRow.getText());
                throw new IllegalStateException(msg);
            }
        }
        if (section.equals(ProviderSection.WORK_LOCATIONS)) grabWorkLocationContent(index, dataMap);

        return dataMap;
    }

    /**
     * Determines the number of data blocks in a specific provider section.
     *
     * @param section the provider section to find the number of records for.
     * @return an integer of the number of records for a particular facility
     *         section.
     */
    public int grabDataBlockCount(ProviderSection section)
    {
        return selenium_.findElements(By.cssSelector(getDataBlocksSelector(section))).size();
    }

    /**
     * Determines whether a specific "data block"'s content panel in a provider
     * section is displayed or not
     *
     * @param section the provider section to select
     * @param index   the index of data block to specifically select
     * @return whether the data block is displayed (true) or not displayed (false)
     */
    public boolean grabDataBlockExpanded(ProviderSection section, int index)
    {
        return findDataBlockContent(section, index).isDisplayed();
    }

    /**
     * Gets the title of a data block
     *
     * @param section   the provider section to check
     *
     * @param index     the index of data block within the section to retrieve the data block title from
     *
     * @return          a string of the title of the data block at the specified index within the provider section
     */
    public String grabDataBlockTitle(ProviderSection section, int index)
    {
        WebElement element =
                selenium_.findElement(By.cssSelector(getDataBlockHeaderSelector(section, index)));
        selenium_.scrollIntoView(element);
        return element.getText();
    }

    /**
     * Gets a list of the titles of each data block within a section
     *
     * @param section   the provider section to collect data block titles from
     *
     * @return          a list of data block titles from the specified provider section
     */
    public List<String> grabDataBlockTitleList(ProviderSection section)
    {
        List<String> titleList = new ArrayList<>();
        List<WebElement> titleBarList =
                selenium_.findElementsByCss(getDataBlocksHeaderSelector(section));
        for (WebElement titleBar : titleBarList)
        {
            titleList.add(titleBar.getText());
        }
        return titleList;
    }

    /**
     * Gets whether a provider section is displayed (true) or not (false)
     *
     * @param section   the provider section to check
     *
     * @return          whether the provider section is visible (true) or not (false)
     */
    public boolean grabSectionDisplayed(ProviderSection section)
    {
        return selenium_.grabElementVisibleByCss(getSectionSelector(section));
    }

    /**
     * Gets the text within the notice that typically details that no records are available
     *
     * @param section   the section to check for the no records notice within
     *
     * @return          a string of the notice (typically either no records found or no permission to view)
     */
    public String grabSectionNoRecordsNotice(ProviderSection section)
    {
        String text;
        WebElement element = selenium_.searchElementByCss(
                getSectionContentSelector(section)
                + " > table.recordDetailsPanels > tbody > tr > td > span.ui-norecords-found");
        if (element != null)
        {
            selenium_.scrollIntoView(element);
            text = element.getText();
        }
        else
        {
            text = null;
        }
        return text;
    }

    /**
     * Gets the title of a provider section
     *
     * @param section   the provider section to get the title of
     *
     * @return          a string of the provider section title
     */
    public String grabSectionTitle(ProviderSection section)
    {
        WebElement element = selenium_.findElementByCss(getSectionHeaderSelector(section));
        selenium_.scrollIntoView(element);
        return element.getText();
    }

    /**
     * Opens a provider given its internal provider ID
     *
     * @param authId                internal provider ID
     *
     * @throws NullPointerException if {@code pauthId} is {@code null}
     */
    public void openProvider(String authId)
    {
        requireNonNull(authId);
        if (uri_ != null)
        {
            selenium_.getDriver().get(UriUtils.getUriWithQuery(uri_, "p=" + authId).toString());
            waitForReady();
        }
        else
        {
            throw new UnsupportedOperationException("Page URL is not specified.");
        }
    }

    /**
     * Scrolls the view to a specific provider section
     *
     * @param section   the provider section to scroll to
     */
    public void scrollToSection(ProviderSection section)
    {
        selenium_.scrollIntoView(selenium_.findElementByCss(getSectionSelector(section)));
    }

    /**
     * Overrides normal waitForReady to ensure both the view header and page itself are ready on the page.
     */
    @Override
    public void waitForReady()
    {
        viewHeader_.waitForReady();
        super.waitForReady();
    }

    private WebElement findDataBlockContent(ProviderSection section, int index)
    {
        return selenium_.findElementByCss(getDataBlockContentSelector(section, index));
    }

    private static String formatDataKey(String key)
    {
        return DATA_KEY_SUFFIX_PATTERN.matcher(key).replaceAll("");
    }

    private String getWorkLocationSubpanelsSelector(int index)
    {
        return getDataBlockContentSelector(ProviderSection.WORK_LOCATIONS, index) +
                " > div.ui-widget-content > div.ui-widget-content > div.ui-subpanel";
    }

    private String getDataBlockContentSelector(ProviderSection section, int index)
    {
        return getDataBlockSelector(section, index) + " > div.ui-panel-content";
    }

    private String getDataBlockHeaderActiveSelector(ProviderSection section, int index)
    {
        return getDataBlockHeaderSelector(section, index)
                + " > div.ui-panel-actions > span > img[title='Active']";
    }

    private String getDataBlockHeaderExpandSelector(ProviderSection section, int index)
    {
        return getDataBlockHeaderSelector(section, index)
                + " > a[title='Expand/Collapse']";
    }

    private String getDataBlockHeaderSelector(ProviderSection section, int index)
    {
        return getDataBlockSelector(section, index) + " > div.ui-panel-titlebar";
    }

    private String getDataBlockSelector(ProviderSection section, int index)
    {
        if (index < 0)
        {
            String msg = String.format("Negative index (%d).", index);
            throw new IllegalArgumentException(msg);
        }
        return getDataBlocksSelector(section) + ":nth-of-type(" + (index + 1) + ")";
    }

    private String getDataBlocksHeaderSelector(ProviderSection section)
    {
        return getDataBlocksSelector(section) + " > div.ui-panel-titlebar";
    }

    private String getDataBlocksSelector(ProviderSection section)
    {
        return getSectionContentSelector(section)
                + " > table.recordDetailsPanels > tbody > tr > td > div.ui-panel";
    }

    private String getSectionContentSelector(ProviderSection section)
    {
        return getSectionSelector(section) + "_content";
    }

    private String getSectionHeaderSelector(ProviderSection section)
    {
        return getSectionSelector(section) + "_header";
    }

    private String getSectionSelector(ProviderSection section)
    {
        return "div#" + section.getPanelId();
    }
}
