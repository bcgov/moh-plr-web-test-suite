package ca.bc.gov.health.qa.autotest.plr.web.actions.facility;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertFalse;

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

    /**
     * Helper function to get and sort all types of data blocks for telecommunications and electronic address sections
     *
     * @param viewFacility          the view facility page reference
     * @param dataBlocksSection     the facility section to get data blocks from
     *                              (expects TELECOMMUNICATIONS or ELECTRONIC_ADDRESSES)
     * @param expectedTypes         a sorted list of expected types of data blocks in the facility section
     * @return                      a sorted list of the actual types of data blocks in the facility section
     */
    public List<String> getDataBlockTypes(ViewFacilityPage viewFacility, FacilitySection dataBlocksSection,
                                           List<String> expectedTypes)
    {
        final Pattern BLOCK_TYPE_PATTERN = Pattern.compile("\\((.*)\\)");

        List<String> dataBlockTypes = new ArrayList<>();
        for (int index = 0; index < expectedTypes.size(); index++)
        {
            LinkedHashMap<String,String> infoMap = viewFacility.grabDataBlockContent(
                    dataBlocksSection, index);
            String telecomType = infoMap.get("Type");
            Matcher resultMatcher = BLOCK_TYPE_PATTERN.matcher(telecomType);
            if (resultMatcher.find()) dataBlockTypes.add(resultMatcher.group(1));
        }
        Collections.sort(dataBlockTypes);
        return dataBlockTypes;
    }

    /**
     * Helper function to check the existence of identifiers for each data block in a facility section
     *
     * @param viewFacility          the view facility page reference
     * @param dataBlocksSection     the facility section to get data blocks from
     * @param identifierField       the name of the identifier field within data blocks
     */
    public void checkDataBlockIdentifiers(ViewFacilityPage viewFacility, FacilitySection dataBlocksSection,
                                           String identifierField)
    {
        for (int dataBlockIndex = 0;
             dataBlockIndex < viewFacility.grabDataBlockCount(dataBlocksSection); dataBlockIndex++)
        {
            String dataIdentifier = viewFacility.grabDataBlockContent(
                    dataBlocksSection, dataBlockIndex).get(identifierField);

            assertFalse(dataIdentifier.isEmpty(),
                    dataBlocksSection.getTitle() + " block " + dataBlockIndex + " is missing identifier");
        }
    }
}
