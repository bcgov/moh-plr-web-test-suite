package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.core.util.net.UriUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;

/**
 * TODO (AZ) - doc
 */
public class ViewProviderPage
extends BasicWebPage
{
    private static final Pattern DATA_KEY_SUFFIX_PATTERN = Pattern.compile(":$");

    private final ViewHeaderFragment viewHeader_;

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public ViewProviderPage(SeleniumSession selenium)
    {
        this(selenium, null);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     *
     * @param uri
     *        ???
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
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @param index
     *        ???
     *
     * @param expand
     *        ???
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
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public ViewHeaderFragment getViewHeader()
    {
        return viewHeader_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @param active
     *        ???
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @param index
     *        ???
     *
     * @return ???
     */
    public boolean grabDataBlockActive(ProviderSection section, int index)
    {
        By locator = By.cssSelector(getDataBlockHeaderActiveSelector(section, index));
        return selenium_.searchElement(locator) != null;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @param index
     *        ???
     *
     * @return ???
     */
    public LinkedHashMap<String,String> grabDataBlockContent(ProviderSection section, int index)
    {
        LinkedHashMap<String,String> dataMap = new LinkedHashMap<>();
        expandDataBlock(section, index, true);
        List<WebElement> dataRowElementList = selenium_.findElements(By.cssSelector(
                getDataBlockContentSelector(section, index) + " > table > tbody > tr"));
        if (!dataRowElementList.isEmpty())
        {
            selenium_.scrollIntoView(dataRowElementList.get(0));
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
        return dataMap;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @return ???
     */
    public int grabDataBlockCount(ProviderSection section)
    {
        return selenium_.findElements(By.cssSelector(getDataBlocksSelector(section))).size();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @param index
     *        ???
     *
     * @return ???
     */
    public boolean grabDataBlockExpanded(ProviderSection section, int index)
    {
        return findDataBlockContent(section, index).isDisplayed();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @param index
     *        ???
     *
     * @return ???
     */
    public String grabDataBlockTitle(ProviderSection section, int index)
    {
        WebElement element =
                selenium_.findElement(By.cssSelector(getDataBlockHeaderSelector(section, index)));
        selenium_.scrollIntoView(element);
        return element.getText();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @return ???
     */
    public boolean grabSectionDisplayed(ProviderSection section)
    {
        return selenium_.grabElementVisibleByCss(getSectionSelector(section));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @return ???
     */
    public String grabSectionTitle(ProviderSection section)
    {
        WebElement element = selenium_.findElementByCss(getSectionHeaderSelector(section));
        selenium_.scrollIntoView(element);
        return element.getText();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param pauthId
     *        internal provider ID
     *
     * @throws NullPointerException
     *         if {@code pauthId} is {@code null}
     */
    public void openProvider(String pauthId)
    {
        requireNonNull(pauthId);
        if (uri_ != null)
        {
            selenium_.getDriver().get(UriUtils.getUriWithQuery(uri_, "p=" + pauthId).toString());
            waitForReady();
        }
        else
        {
            throw new UnsupportedOperationException("Page URL is not specified.");
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     */
    public void scrollToSection(ProviderSection section)
    {
        selenium_.scrollIntoView(selenium_.findElementByCss(getSectionSelector(section)));
    }

    /**
     * TODO (AZ) - doc
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
        String selector = getSectionContentSelector(section)
                + " > table.recordDetailsPanels > tbody > tr > td > div.ui-panel";
        return selector;
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
