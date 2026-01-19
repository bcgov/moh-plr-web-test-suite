package ca.bc.gov.health.qa.autotest.plr.web.actions.facility;

import ca.bc.gov.health.qa.autotest.core.util.text.TextUtils;
import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants.*;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.*;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilityDataFields;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;

import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static org.testng.Assert.*;

/**
 * Actions class for the View Facility page/functions
 */
public class ViewFacilityActions {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final SeleniumSession selenium_;
    private final URI uri_;
    private final UserType userType_;

    private static final Set<FacilitySection> facilitySectionSet = Collections.unmodifiableSet(EnumSet.of(
            FacilitySection.IDENTIFIERS,
            FacilitySection.NAMES,
            FacilitySection.CIVIC_ADDRESSES,
            FacilitySection.OTHER_ADDRESS,
            FacilitySection.TELECOMMUNICATIONS,
            FacilitySection.ELECTRONIC_ADDRESSES,
            FacilitySection.ORGANIZATION_RELATIONSHIPS,
            FacilitySection.NOTES
    ));

    /**
     * Initializes class and SeleniumSession.
     *
     * @param selenium  the current SeleniumSession
     */
    public ViewFacilityActions(SeleniumSession selenium, URI uri, UserType userType)
    {
        selenium_ = selenium;
        uri_ = uri;
        userType_ = userType;
    }

    /**
     * Opens the Facility Details page for the given facility authorization id
     * and waits until the page is ready.
     *
     * @param fauthId the facility authorization identifier (FAUTH ID) to open
     * @return a ready {@link ViewFacilityPage} instance for further interactions
     */
    public ViewFacilityPage openFacility(String fauthId) {
        LOG.info("Open facility({}).", fauthId);
        ViewFacilityPage page = new ViewFacilityPage(selenium_, uri_.resolve("plr/FacilityDetails.xhtml"));
        page.openFacility(fauthId);
        page.waitForReady();
        return page;
    }

    private ViewFacilityPage waitForViewFacilityPage() {
        ViewFacilityPage page = new ViewFacilityPage(selenium_);
        page.waitForReady();
        return page;
    }

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
            LinkedHashMap<String,String> infoMap = viewFacility.grabDataBlockContent(dataBlocksSection, index);
            String dataType = infoMap.get("Type");
            Matcher resultMatcher = BLOCK_TYPE_PATTERN.matcher(dataType);
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

    /**
     * Verifies all data blocks in a facility section on the page is sorted (by values and dates)
     *
     * @param viewFacilityPage  the view facility page reference
     * @param section           the facility section to check sorting in
     */
    public void verifyDataBlockSortOrder(ViewFacilityPage viewFacilityPage, FacilitySection section)
    {
        List<String> sortKeyList = FacilityDataFields.getSortKey(section);
        List<String> dateKeyList = new ArrayList<>();

        dateKeyList.add(IdentifierField.EFFECTIVE_FROM.getString());
        dateKeyList.add(IdentifierField.DB_CREATED.getString());
        List<String> previousValueList = null;
        List<String> previousDateList  = null;

        for (int i = 0; i < viewFacilityPage.grabDataBlockCount(section); i++)
        {
            Map<String,String> dataMap = viewFacilityPage.grabDataBlockContent(section, i);
            if (section.equals(FacilitySection.ORGANIZATION_RELATIONSHIPS))
                dataMap = viewFacilityPage.grabOrgRelationshipsBlockContent(i);
            List<String> valueList = extractDataValueList(sortKeyList, dataMap);
            List<String> dateList  = extractDataValueList(dateKeyList, dataMap);

            if (previousValueList != null)
            {
                // Compare values in ascending order.
                int result = TextUtils.compareStringLists(previousValueList, valueList);
                if (result == 0)
                {
                    // Compare dates in descending order.
                    result = TextUtils.compareStringLists(dateList, previousDateList);
                }
                if (result > 0)
                {
                    String msg = String.format("Incorrect data block order (%s:%d).", section, i);
                    throw new IllegalStateException(msg);
                }
            }
            previousDateList  = dateList;
            previousValueList = valueList;
        }
    }

    /**
     * Expands/Collapses every data block with the associated "Expand All"/"Collapse All" button.
     *
     * @param viewFacilityPage      the view facility page reference
     * @param expand                whether to expand (true) or collapse (false) all data blocks
     */
    public void expandAll(ViewFacilityPage viewFacilityPage, boolean expand)
    {
        assertNotNull(viewFacilityPage);
        viewFacilityPage.expandAll(expand);
        try {
            //TODO convert this sleep to an explicit wait in selenium if possible
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("unexpected interruption");
        }
    }

    /**
     * Checks that each data block within a facility section has been expanded
     *
     * @param viewFacilityPage      the view facility page reference
     * @param section               the facility section to check expansion of data blocks within
     * @param active                whether the data blocks are active (true) or not (false)
     */
    public void checkDataBlocksExpanded(ViewFacilityPage viewFacilityPage, FacilitySection section, boolean active)
    {
        assertNotNull(viewFacilityPage);

        int count = viewFacilityPage.grabActiveDataBlockCount(section, active);

        for (int i = 0; i < count; i++) {
            assertTrue(viewFacilityPage.grabDataBlockExpanded(section, i));
        }
    }

    /**
     * Checks that each data block within a facility section has been collapsed
     *
     * @param viewFacilityPage      the view facility page reference
     * @param section               the facility section to check collapse of data blocks within
     * @param active                whether the data blocks are active (true) or not (false)
     */
    public void checkDataBlocksCollapsed(ViewFacilityPage viewFacilityPage, FacilitySection section, boolean active)
    {
        assertNotNull(viewFacilityPage);

        int count = viewFacilityPage.grabActiveDataBlockCount(section, active);

        for (int i = 0; i < count; i++) {
            assertFalse(viewFacilityPage.grabDataBlockExpanded(section, i));
        }
    }

    /**
     * Returns a set of facility sections - helper method for other verification actions
     *
     * @param userType  the user type to be logged in - some sections to be removed depending on permissions
     * @return          a set of facility sections
     */
    public static Set<FacilitySection> getFacilitySectionSet(UserType userType)
    {
        Set<FacilitySection> set = new HashSet<>(facilitySectionSet);
        if (!userType.equals(UserType.ADMIN)) {
            set.remove(FacilitySection.IDENTIFIERS);
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Verifies that all expected facility sections are displayed. For IDENTIFIERS
     * and NAMES sections, also verifies that at least one active data block is
     * present (not empty).
     *
     * @param viewFacilityPage the facility view page already opened and ready
     */
    public void verifyFacilitySectionsDisplayed(ViewFacilityPage viewFacilityPage)
    {
        viewFacilityPage.getViewHeader().grabViewMode();

        for (FacilitySection section : getFacilitySectionSet(userType_)) {
            viewFacilityPage.scrollToSection(section);

            assertTrue(viewFacilityPage.grabSectionDisplayed(section),
                    String.format("Section displayed (%s)", section));

            if (section.toString().equals("IDENTIFIERS") || section.toString().equals("NAMES")) {
                assertTrue(viewFacilityPage.grabActiveDataBlockCount(section, true) > 0,
                        String.format("Presence of active data blocks (%s)", section));
            }

        }
    }

    /**
     * Verifies the button to expand a data block in a section is displayed/interactable.
     * Both active and inactive data blocks are checked.
     *
     * @param viewFacilityPage      the view facility page reference
     * @param section               the facility section to check expand buttons in data blocks for
     */
    public void verifySectionDataBlockExpandButtonDisplayed(ViewFacilityPage viewFacilityPage, FacilitySection section)
    {
        int count = viewFacilityPage.grabActiveDataBlockCount(section, true);
        for (int i = 0; i < count; i++) {
            assertTrue(viewFacilityPage.isDataBlockExpandButtonDisplayed(section, i));

            viewFacilityPage.expandDataBlock(section, i, true);
            viewFacilityPage.expandDataBlock(section, i, false);
        }

        count = viewFacilityPage.grabActiveDataBlockCount(section, false);
        for (int i = 0; i < count; i++) {
            assertTrue(viewFacilityPage.isDataBlockExpandButtonDisplayed(section, i));

            viewFacilityPage.expandDataBlock(section, i, true);
            viewFacilityPage.expandDataBlock(section, i, false);
        }
    }

    /**
     * Verifies the expand button is displayed on each facility section.
     *
     * @param viewFacilityPage      the view facility page reference
     */
    public void verifyAllFacilityDataBlockExpandButtonDisplayed(ViewFacilityPage viewFacilityPage)
    {
        Set<FacilitySection> sectionSet = getFacilitySectionSet(userType_);

        for (FacilitySection section : sectionSet)
        {
            verifySectionDataBlockExpandButtonDisplayed(viewFacilityPage, section);
        }
    }

    /**
     * Verifies a section's data blocks have an "active" mark displayed corresponding to the count specified as active.
     *
     * @param viewFacilityPage      the view facility page reference
     * @param section               the facility section to check the data blocks for active marks for
     */
    public void verifySectionDataBlockActiveMarkDisplayed(ViewFacilityPage viewFacilityPage, FacilitySection section)
    {
        int count = viewFacilityPage.grabActiveDataBlockCount(section, true);
        for (int i = 0; i < count; i++) {
            assertTrue(viewFacilityPage.isDataBlockActiveMarkDisplayed(section, i));
        }
    }

    /*
	public void verifySectionsWithActiveDataBlocks(boolean expectedInactive) {
		ViewFacilityPage viewFacilityPage = waitForViewFacilityPage();
		ViewMode viewMode = viewFacilityPage.getViewHeader().grabViewMode();

		for (FacilitySection section : getFacilitySectionSet( userType_)) {
			viewFacilityPage.scrollToSection(section);
			assertTrue(viewFacilityPage.grabSectionDisplayed(section),
					String.format("Section displayed (%s)", section));
			assertTrue(viewFacilityPage.grabActiveDataBlockCount(section, true) > 0,
					String.format("Presence of active data blocks (%s)", section));
			if (!expectedInactive) {
				assertTrue(viewFacilityPage.grabActiveDataBlockCount(section, false) == 0,
						String.format("Absence of inactive data blocks (%s)", section));
			} else {
				if (viewMode.equals(ViewMode.HISTORY) && section.equals(ProviderSection.CONFIDENTIALITY)) {
					// NOTE: Special case
					assertTrue(viewFacilityPage.grabActiveDataBlockCount(section, false) == 0,
							String.format("Absence of inactive data blocks (%s)", section));
				} else {
					assertTrue(viewFacilityPage.grabActiveDataBlockCount(section, false) > 0,
							String.format("Presence of inactive data blocks (%s)", section));
				}
			}
		}
	}*/

    /**
     * Verifies all applicable data blocks have an "active" mark displayed corresponding to the known active count.
     * Note: this function originally had a userType, but no difference in permissions was found as of now.
     *
     * @param viewFacility      the view facility page reference
     */
    public void verifyAllFacilityDataBlockActiveMarkDisplayed(ViewFacilityPage viewFacility)
    {
        List<FacilitySection> sectionsToTest = Arrays.asList(
                FacilitySection.IDENTIFIERS,
                FacilitySection.NOTES,
                FacilitySection.TELECOMMUNICATIONS,
                FacilitySection.ELECTRONIC_ADDRESSES,
                FacilitySection.ORGANIZATION_RELATIONSHIPS,
                FacilitySection.NOTES
        );

        for (FacilitySection section : sectionsToTest) verifySectionDataBlockActiveMarkDisplayed(viewFacility, section);
    }

    /**
     * Verify a facility section's data blocks contain a button to update the data block
     *
     * @param viewFacilityPage      the view facility page reference
     * @param section               the facility section to check for update buttons on the data blocks for
     */
    public void verifySectionDataBlockUpdateButtonDisplayed(ViewFacilityPage viewFacilityPage, FacilitySection section)
    {
        int count = viewFacilityPage.grabActiveDataBlockCount(section, true);
        for (int i = 0; i < count; i++) {
            assertTrue(viewFacilityPage.isDataBlockUpdateButtonDisplayed(section, i));
        }
    }

    /**
     * Verify each applicable facility section's data blocks contain a button to update the data block
     *
     * @param page              the view facility page reference
     * @param userType          the user type: currently only admins are capable of updating data blocks
     */
    public void verifyAllFacilityDataBlockUpdateButtonDisplayed(ViewFacilityPage page, UserType userType)
    {
        List<FacilitySection> sectionsToTest = Arrays.asList(
                FacilitySection.NAMES,
                FacilitySection.TELECOMMUNICATIONS,
                FacilitySection.ELECTRONIC_ADDRESSES,
                FacilitySection.ORGANIZATION_RELATIONSHIPS,
                FacilitySection.NOTES
        );

        if (!userType.equals(UserType.ADMIN)) sectionsToTest = new ArrayList<>();

        for (FacilitySection section : sectionsToTest) verifySectionDataBlockUpdateButtonDisplayed(page, section);
    }

    private boolean checkSectionDataBlocksKeys(FacilitySection section, LinkedHashMap<String, String> result)
    {
        if (result == null)
            return false;
        return switch (section) {
            case FacilitySection.IDENTIFIERS -> Arrays.stream(IdentifierField.values())
                                                    .map(IdentifierField::getString).allMatch(result::containsKey);
            case FacilitySection.NAMES -> Arrays.stream(NameField.values())
                                                    .map(NameField::getString).allMatch(result::containsKey);
            case FacilitySection.NOTES -> Arrays.stream(NoteField.values())
                                                    .map(NoteField::getString).allMatch(result::containsKey);
            case FacilitySection.ELECTRONIC_ADDRESSES -> Arrays.stream(EAddressField.values())
                                                    .map(EAddressField::getString).allMatch(result::containsKey);
            case FacilitySection.CIVIC_ADDRESSES -> Arrays.stream(CivicAddressField.values())
                                                    .map(CivicAddressField::getString).allMatch(result::containsKey);
            case FacilitySection.ORGANIZATION_RELATIONSHIPS -> Arrays.stream(OrgRelationshipField.values())
                                                    .map(OrgRelationshipField::getString).allMatch(result::containsKey);
            case FacilitySection.OTHER_ADDRESS -> Arrays.stream(OtherAddressField.values())
                                                    .map(OtherAddressField::getString).allMatch(result::containsKey);
            case FacilitySection.TELECOMMUNICATIONS -> Arrays.stream(TelecomField.values())
                                                    .map(TelecomField::getString).allMatch(result::containsKey);
        };

    }

    private void checkSectionDataBlocksContentsAndSummaryLine(FacilitySection section, JSONObject expectedJson,
                                                              LinkedHashMap<String, String> resultContent,
                                                              String resultSummaryLineText)
    {
        assertTrue(expectedJson != null && resultContent != null && resultSummaryLineText != null);

        switch (section) {
            case FacilitySection.IDENTIFIERS:
                Identifier idResult = new Identifier(resultContent);
                Identifier idExpected = new Identifier(expectedJson);
                assertEquals(idExpected, idResult);

                assertTrue(resultSummaryLineText.contains(idExpected.getIdType())
                        && resultSummaryLineText.contains(idExpected.getIdentifier())
                        && resultSummaryLineText.contains(idExpected.getDataOwnerCode()));
                break;
            case FacilitySection.NAMES:
                Name nameResult = new Name(resultContent);
                Name nameExpected = new Name(expectedJson);
                assertEquals(nameExpected, nameResult);

                assertTrue(resultSummaryLineText.contains(nameExpected.getName()));

                break;
            case FacilitySection.NOTES:
                Note noteResult = new Note(resultContent);
                Note noteExpected = new Note(expectedJson);
                assertEquals(noteExpected, noteResult);

                assertTrue(resultSummaryLineText.contains(noteExpected.getNoteIdentifier())
                        && resultSummaryLineText.contains(noteExpected.getDataOwnerCode())
                );
                break;
            case FacilitySection.ELECTRONIC_ADDRESSES:
                ElectronicAddress eAddrResult = new ElectronicAddress(resultContent);
                ElectronicAddress eAddrExpected = new ElectronicAddress(expectedJson);
                assertEquals(eAddrExpected, eAddrResult);

                assertTrue(resultSummaryLineText.contains(eAddrExpected.getPurposeSummary())
                        && resultSummaryLineText.contains(eAddrExpected.getTypeSummary())
                        && resultSummaryLineText.contains(eAddrExpected.getAddress())
                        && resultSummaryLineText.contains(eAddrExpected.getDataOwnerCode()));
                break;
            case FacilitySection.CIVIC_ADDRESSES:
                CivicAddress cAddrResult = new CivicAddress(resultContent);
                CivicAddress cAddrExpected = new CivicAddress(expectedJson);
                assertEquals(cAddrExpected, cAddrResult);

                assertTrue(resultSummaryLineText.contains(cAddrExpected.getAddressLine1())
                        && resultSummaryLineText.contains(cAddrExpected.getCity())
                        && resultSummaryLineText.contains(cAddrExpected.getProvinceStateSummary())
                        && resultSummaryLineText.contains(cAddrExpected.getDataOwnerCode()));
                break;
            case FacilitySection.ORGANIZATION_RELATIONSHIPS:
                Relationship reResult = new Relationship(resultContent);
                Relationship reExpected = new Relationship(expectedJson);
                assertEquals(reExpected, reResult);

                assertTrue(resultSummaryLineText.contains(reExpected.getRelatedProviderRoleType())
                        && resultSummaryLineText.contains(reExpected.getRelationshipTypeSummary())
                        && resultSummaryLineText.contains(reExpected.getRelatedOrganizationName())
                        && resultSummaryLineText.contains(reExpected.getDataOwnerCode()));
                break;
            case FacilitySection.OTHER_ADDRESS:
                OtherAddress oAddrResult = new OtherAddress(resultContent);
                OtherAddress oAddExpected = new OtherAddress(expectedJson);
                assertEquals(oAddExpected, oAddrResult);

                assertTrue(resultSummaryLineText.contains(oAddExpected.getAddressPurposeSummary())
                        && resultSummaryLineText.contains(oAddExpected.getAddressTypeSummary())
                        && resultSummaryLineText.contains(oAddExpected.getDataOwnerCode())
                        && resultSummaryLineText.contains(oAddExpected.getAddressLine1())
                        && resultSummaryLineText.contains(oAddExpected.getCity())
                        && resultSummaryLineText.contains(oAddExpected.getStateProv()));
                break;
            case FacilitySection.TELECOMMUNICATIONS:
                Telecommunication teleResult = new Telecommunication(resultContent);
                Telecommunication teleExpected = new Telecommunication(expectedJson);
                assertEquals(teleExpected, teleResult);

                assertTrue(resultSummaryLineText.contains(teleExpected.getPurposeSummary())
                        && resultSummaryLineText.contains(teleExpected.getTypeSummary())
                        && resultSummaryLineText.contains(teleExpected.getAreaCode())
                        && resultSummaryLineText.contains(teleExpected.getNumber())
                        && resultSummaryLineText.contains(teleExpected.getExtension())
                        && resultSummaryLineText.contains(teleExpected.getDataOwnerCode()));
                break;
            default:
                fail("No expected facility section name found");
                break;
        }
    }

    /**
     * Verifies a facility section's data blocks have the expected fields and info within them
     *
     * @param viewFacilityPage      the view facility page reference
     * @param section               the facility section to check data blocks/summary of
     * @param active                whether to grab active or inactive data blocks to verify
     * @param idArray               the array of expected values to cross-reference with the section data blocks
     */
    public void verifySectionDataBlocksAndSummary(ViewFacilityPage viewFacilityPage, FacilitySection section,
                                                  boolean active, JSONArray idArray)
    {
        int count = viewFacilityPage.grabActiveDataBlockCount(section, active);

        assertEquals(count, idArray.length());

        for (int i = 0; i < count; i++) {
            LinkedHashMap<String, String> resultContent;
            if (section.equals(FacilitySection.CIVIC_ADDRESSES)) {
                resultContent = viewFacilityPage.grabCivicAddressBlockContent(i);
            } else if (section.equals(FacilitySection.ORGANIZATION_RELATIONSHIPS)) {
                resultContent = viewFacilityPage.grabOrgRelationshipsBlockContent(i);
            } else
                resultContent = viewFacilityPage.grabDataBlockContent(section, i);

            String resultSummaryLineText = viewFacilityPage.grabDataBlockSummaryLine(section, i);
            JSONObject expectedJson = (JSONObject) idArray.get(i);

            assertTrue(checkSectionDataBlocksKeys(section, resultContent));

            checkSectionDataBlocksContentsAndSummaryLine(section, expectedJson, resultContent, resultSummaryLineText);

        }
    }

    /**
     * Verifies each facility section's data blocks contain the expected fields and information
     *
     * @param viewFacilityPage      the view facility page reference
     * @param expectedFacility      a JSON object with arrays of expected info the data blocks should contain
     */
    public void verifyAllSectionsDataBlockAndSummary(ViewFacilityPage viewFacilityPage, JSONObject expectedFacility) {
        Map<String, FacilitySection> dataBlockMap = Map.of(
                "identifiers", FacilitySection.IDENTIFIERS,
                "names", FacilitySection.NAMES,
                "electronic Address", FacilitySection.ELECTRONIC_ADDRESSES,
                "civicAddress", FacilitySection.CIVIC_ADDRESSES,
                "relationships", FacilitySection.ORGANIZATION_RELATIONSHIPS,
                "otherAddress", FacilitySection.OTHER_ADDRESS,
                "telecommunication", FacilitySection.TELECOMMUNICATIONS
        );

        for (String dataKey : dataBlockMap.keySet())
        {
            boolean active = !dataKey.equals("civicAddress") && !dataKey.equals("otherAddress");

            JSONArray idArray = expectedFacility.getJSONArray(dataKey);
            verifySectionDataBlocksAndSummary(viewFacilityPage, dataBlockMap.get(dataKey), active, idArray);
        }
    }
}
