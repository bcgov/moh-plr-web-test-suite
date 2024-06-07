package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;

/**
 * TODO (AZ) - doc
 */
public class SearchSectionFragment
extends BasicWebPageFragment
{
    /**
     * TODO (AZ) - doc
     *
     * @param selenium
     *        ???
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
     * TODO (AZ) - doc
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
