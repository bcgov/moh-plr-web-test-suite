package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * Fragment class for sections of search pages
 */
public class SearchSectionFragment
extends BasicWebPageFragment
{
    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        The current SeleniumSession
     *
     * @param mainLocator
     *        ???
     *
     * @throws NullPointerException
     *         if either {@code driver} or {@code mainLocator} is {@code null}
     */
    protected SearchSectionFragment(SeleniumSession selenium, By mainLocator)
    {
        super(selenium, mainLocator);
    }

    /**
     * Waits for search facility to be visible and expanded
     */
    @Override
    public void waitForReady()
    {
        super.waitForReady();

        // Wait for the expand animation to complete.
        // NOTE: The value of the CSS property "overflow" is "hidden"
        //       while the transition animation is in progress,
        //       and "auto" when the animation completes.
        selenium_.waitUntil(ExpectedConditions.attributeToBe(mainLocator_, "overflow", "auto"));
    }
}
