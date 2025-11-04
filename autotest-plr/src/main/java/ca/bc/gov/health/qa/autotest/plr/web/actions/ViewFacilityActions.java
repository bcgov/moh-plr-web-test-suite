package ca.bc.gov.health.qa.autotest.plr.web.actions;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * Actions class for the View Facility page/functions
 */
public class ViewFacilityActions {
    private final SeleniumSession selenium_;

    /**
     * Initializes class and SeleniumSession.
     *
     * @param selenium  the current SeleniumSession
     */
    public ViewFacilityActions(SeleniumSession selenium) { selenium_ = selenium; }

    /**
     * Opens an organization (View Provider) page from a Facility to Organization Relationship block
     *
     * @param viewFacility      the ViewFacilityPage reference to the currently viewed facility
     * @param dataBlockIndex    the index of organization relationship data block to open the organization from
     * @return                  a ViewProviderPage reference to the opened organization page
     */
    public ViewProviderPage transferToOrg(ViewFacilityPage viewFacility, int dataBlockIndex)
    {
        viewFacility.expandDataBlock(FacilitySection.ORGANIZATION_RELATIONSHIPS, dataBlockIndex, true);
        viewFacility.openOrg(dataBlockIndex);
        ViewProviderPage viewProvider = new ViewProviderPage(selenium_);
        viewProvider.waitForReady();
        return viewProvider;
    }
}
