package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumUtils;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * TODO (AZ) - doc
 */
public class ViewHeaderFragment
extends BasicWebPageFragment
{
    private static final String HEADER_CSS = "span#headerGroup";

    private static final String COLLAPSE_ALL_CSS     = HEADER_CSS + " img[src*='collapse.png']";
    private static final String EXPAND_ALL_CSS       = HEADER_CSS + " img[src*='expand.png']";
    private static final String PRINT_CSS            = HEADER_CSS + " img[src*='printer.png']";
    private static final String VIEW_MODE_BUTTON_CSS = HEADER_CSS + " button";
    private static final String VIEW_TITLE_CSS       = HEADER_CSS + " > form > div";

    private static final Logger LOG = ExecutionLogManager.getLogger();

    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
     */
    public ViewHeaderFragment(SeleniumSession selenium)
    {
        super(selenium, By.cssSelector(HEADER_CSS));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param expand
     *        ???
     */
    public void expandAll(boolean expand)
    {
        if (grabExpandedAll() != expand)
        {
            By expectedButtonLocator;
            if (expand)
            {
                selenium_.findElement(By.cssSelector(EXPAND_ALL_CSS)).click();
                expectedButtonLocator = By.cssSelector(COLLAPSE_ALL_CSS);
            }
            else
            {
                selenium_.findElement(By.cssSelector(COLLAPSE_ALL_CSS)).click();
                expectedButtonLocator = By.cssSelector(EXPAND_ALL_CSS);
            }
            selenium_.waitUntil(
                    ExpectedConditions.visibilityOfElementLocated(expectedButtonLocator));
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param expand
     *        ???
     */
    public void expandViewModeMenu(boolean expand)
    {
        if (grabViewModeMenuExpanded() != expand)
        {
            findViewModeButton().click();
            waitForViewModeMenuExpanded(expand);
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabExpandedAll()
    {
        boolean collapseAllVisible =
                selenium_.grabElementVisible(By.cssSelector(COLLAPSE_ALL_CSS));
        boolean expandAllVisible =
                selenium_.grabElementVisible(By.cssSelector(EXPAND_ALL_CSS));
        if (collapseAllVisible == expandAllVisible)
        {
            throw new IllegalStateException("Failed to determine expand all state.");
        }
        return collapseAllVisible;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String grabViewTitle()
    {
        return selenium_.findElement(By.cssSelector(VIEW_TITLE_CSS)).getText();
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public ViewMode grabViewMode()
    {
        return ViewMode.parse(findViewModeButton().getText());
    }

    /**
     * TODO (AZ) - doc
     *
     * @param viewMode
     *        ???
     *
     * @return ???
     */
    public boolean grabViewModeDisplayed(ViewMode viewMode)
    {
        boolean displayed;
        if (!grabViewModeMenuExpanded())
        {
            throw new IllegalStateException("View mode menu is not expanded");
        }
        WebElement viewModeItem = searchViewModeItem(viewMode);
        if (viewModeItem != null)
        {
            displayed = viewModeItem.isDisplayed();
        }
        else
        {
            displayed = false;
        }
        return displayed;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public boolean grabPrintButtonDisplayed()
    {
        return selenium_.grabElementVisible(By.cssSelector(PRINT_CSS));
    }

    /**
     * TODO (AZ) - doc
     *
     * @param viewMode
     *        ???
     *
     * @throws IllegalStateException
     *         ???
     */
    public void selectViewMode(ViewMode viewMode)
    {
        LOG.info("Select view mode ({}).", viewMode);
        WebElement viewModeButton = findViewModeButton();
        expandViewModeMenu(true);
        WebElement viewModeItem = searchViewModeItem(viewMode);
        if (viewModeItem != null)
        {
            viewModeItem.click();
        }
        else
        {
            String msg = String.format("View mode not found (%s).", viewMode);
            throw new IllegalStateException(msg);
        }
        waitForViewModeMenuExpanded(false);
        selenium_.waitUntil(ExpectedConditions.stalenessOf(viewModeButton));
        ViewMode currentViewMode = grabViewMode();
        if (!currentViewMode.equals(viewMode))
        {
            String msg = String.format("Failed to select view (%s).", viewMode);
            throw new IllegalStateException(msg);
        }
    }

    private WebElement findViewModeButton()
    {
        return selenium_.findElement(By.cssSelector(VIEW_MODE_BUTTON_CSS));
    }

    private By getViewModeMenuLocator()
    {
        String buttonName = findViewModeButton().getAttribute("name");
        String menuPanelId = buttonName.replaceAll("_button$", "_menu").replace(":","\\:");
        return By.cssSelector("div#" + menuPanelId);
    }

    private boolean grabViewModeMenuExpanded()
    {
        return selenium_.findElement(getViewModeMenuLocator()).isDisplayed();
    }

    /**
     * TODO (AZ) - doc
     *
     * @param viewMode
     *        ???
     *
     * @return ???
     */
    private WebElement searchViewModeItem(ViewMode viewMode)
    {
        requireNonNull(viewMode, "Null view mode.");
        WebElement viewModeMeu = selenium_.findElement(getViewModeMenuLocator());
        return SeleniumUtils.searchElement(viewModeMeu, By.linkText(viewMode.getItemText()));
    }

    private void waitForViewModeMenuExpanded(boolean expanded)
    {
        By viewModeMenuLocator = getViewModeMenuLocator();
        if (expanded)
        {
            selenium_.waitUntil(
                    ExpectedConditions.visibilityOfElementLocated(viewModeMenuLocator));

            // Wait for the expand animation to complete.
            // NOTE: The value of the CSS property "opacity" is changing
            //       while the transition animation is in progress,
            //       and "1" when the animation completes.
            selenium_.waitUntil(
                    ExpectedConditions.attributeToBe(viewModeMenuLocator, "opacity", "1"));
        }
        else
        {
            selenium_.waitUntil(
                    ExpectedConditions.invisibilityOfElementLocated(viewModeMenuLocator));
        }
    }
}
