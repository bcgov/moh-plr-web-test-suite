package ca.bc.gov.health.qa.autotest.plr.web.actions;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.text.TextUtils;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.CivicAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.ElectronicAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Identifier;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Name;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Note;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.OtherAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Relationship;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Telecommunication;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewMode;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilityDataFields;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

import static org.testng.Assert.*;

/**
 * Simple action helpers for interacting with the PLR Facility view page.
 * Provides high-level workflows used by tests to open a facility and
 * verify the presence and state of section data blocks.
 */
public class ViewFacilitySimpleActions {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private static final Pattern VALUE_CODE_PATTERN = Pattern.compile("^.*\\((?<code>[^()]+)\\)\\s*$");

	private final SeleniumSession selenium_;
	private final URI uri_;
	private final UserType userType_;
	
	
	private static Set<FacilitySection> facilitySectionSet=  Collections.unmodifiableSet(EnumSet.of(
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
     * TODO (KD) - doc
     *
     * @param selenium
     * @param uri
     * @param userType
     */
	public ViewFacilitySimpleActions(SeleniumSession selenium, URI uri, UserType userType) {
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
		ViewFacilityPage viewFacilityPage = new ViewFacilityPage(selenium_, uri_.resolve("plr/FacilityDetails.xhtml"));
		viewFacilityPage.openFacility(fauthId);
		viewFacilityPage.waitForReady();
		return viewFacilityPage;
	}

	/**
	 * Verifies that all expected facility sections are displayed. For IDENTIFIERS
	 * and NAMES sections, also verifies that at least one active data block is
	 * present (not empty).
	 *
	 * @param viewFacilityPage the facility view page already opened and ready
	 */
	public void verifyFacilitySectionsDisplayed(ViewFacilityPage viewFacilityPage) {
		ViewMode viewMode = viewFacilityPage.getViewHeader().grabViewMode();

		for (FacilitySection section : getFacilitySectionSet(userType_)) {
			viewFacilityPage.scrollToSection(section);
			assertTrue(viewFacilityPage.grabSectionDisplayed(section),
					String.format("Section displayed (%s)", section));
			if (section.name().equalsIgnoreCase("IDENTIFIERS") || section.name().equalsIgnoreCase("NAMES")) {
				assertTrue(viewFacilityPage.grabActiveDataBlockCount(section, true) > 0,
						String.format("Presence of active data blocks (%s)", section));
			}

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
     * TODO (KD) - doc
     *
     * @param userType
     * @return
     */
	public static Set<FacilitySection> getFacilitySectionSet( UserType userType) {
		Set<FacilitySection> set = new HashSet<FacilitySection>(facilitySectionSet);
		if (!userType.equals(UserType.ADMIN)) {
			set.remove(FacilitySection.IDENTIFIERS);
		}
		return Collections.unmodifiableSet(set);
	}

	private ViewFacilityPage waitForViewFacilityPage() {
		ViewFacilityPage page = new ViewFacilityPage(selenium_);
		page.waitForReady();
		return page;
	}

    /**
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     * @param expand
     */
	public void expandAll(ViewFacilityPage viewFacilityPage, boolean expand) {
		assertNotNull(viewFacilityPage);
		viewFacilityPage.expandAll(expand);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			fail("unexpected interruption");
		}
	}

    /**
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     * @param section
     * @param active
     */
	public void checkDataBlocksExpanded(ViewFacilityPage viewFacilityPage, FacilitySection section, boolean active) {
		assertNotNull(viewFacilityPage);
		int count = viewFacilityPage.grabActiveDataBlockCount(section, active);
		for (int i = 0; i < count; i++) {
			assertTrue(viewFacilityPage.grabDataBlockExpanded(section, i));
		}
	}

    /**
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     * @param section
     * @param active
     */
	public void checkDataBlocksCollapsed(ViewFacilityPage viewFacilityPage, FacilitySection section, boolean active) {
		assertNotNull(viewFacilityPage);
		int count = viewFacilityPage.grabActiveDataBlockCount(section, active);
		for (int i = 0; i < count; i++) {
			assertFalse(viewFacilityPage.grabDataBlockExpanded(section, i));
		}

	}

    /**
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     */
	public void verifyAllFacilityDataBlockExpandButtonDisplayed(ViewFacilityPage viewFacilityPage) {
		verifySectionDataBlockExpandButtonDisplayed(viewFacilityPage, FacilitySection.IDENTIFIERS);
		verifySectionDataBlockExpandButtonDisplayed(viewFacilityPage, FacilitySection.NAMES);
		verifySectionDataBlockExpandButtonDisplayed(viewFacilityPage, FacilitySection.CIVIC_ADDRESSES);
		verifySectionDataBlockExpandButtonDisplayed(viewFacilityPage, FacilitySection.OTHER_ADDRESS);
		verifySectionDataBlockExpandButtonDisplayed(viewFacilityPage, FacilitySection.TELECOMMUNICATIONS);
		verifySectionDataBlockExpandButtonDisplayed(viewFacilityPage, FacilitySection.ELECTRONIC_ADDRESSES);
		verifySectionDataBlockExpandButtonDisplayed(viewFacilityPage, FacilitySection.ORGANIZATION_RELATIONSHIPS);
		verifySectionDataBlockExpandButtonDisplayed(viewFacilityPage, FacilitySection.NOTES);

	}

    /**
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     * @param section
     */
	public void verifySectionDataBlockExpandButtonDisplayed(ViewFacilityPage viewFacilityPage,
			FacilitySection section) {
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
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     * @param userType
     */
	public void verifyAllFacilityDataBlockActiveMarkDisplayed(ViewFacilityPage viewFacilityPage, UserType userType) {
		switch (userType) {
		case UserType.ADMIN:
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.IDENTIFIERS);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.NAMES);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.TELECOMMUNICATIONS);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.ELECTRONIC_ADDRESSES);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.ORGANIZATION_RELATIONSHIPS);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.NOTES);
			break;
		default:
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.IDENTIFIERS);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.NAMES);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.TELECOMMUNICATIONS);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.ELECTRONIC_ADDRESSES);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.ORGANIZATION_RELATIONSHIPS);
			verifySectionDataBlockActiveMarkDisplayed(viewFacilityPage, FacilitySection.NOTES);
			break;
		}

	}

    /**
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     * @param section
     */
	public void verifySectionDataBlockActiveMarkDisplayed(ViewFacilityPage viewFacilityPage, FacilitySection section) {
		int count = viewFacilityPage.grabActiveDataBlockCount(section, true);
		for (int i = 0; i < count; i++) {
			assertTrue(viewFacilityPage.isDataBlockActiveMarkDisplayed(section, i));

		}

	}

    /**
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     * @param userType
     */
	public void verifyAllFacilityDataBlockUpdateButtonDisplayed(ViewFacilityPage viewFacilityPage, UserType userType) {
		switch (userType) {
		case UserType.ADMIN:
			verifySectionDataBlockUpdateButtonDisplayed(viewFacilityPage, FacilitySection.NAMES);
			verifySectionDataBlockUpdateButtonDisplayed(viewFacilityPage, FacilitySection.TELECOMMUNICATIONS);
			verifySectionDataBlockUpdateButtonDisplayed(viewFacilityPage, FacilitySection.ELECTRONIC_ADDRESSES);
			verifySectionDataBlockUpdateButtonDisplayed(viewFacilityPage, FacilitySection.ORGANIZATION_RELATIONSHIPS);
			verifySectionDataBlockUpdateButtonDisplayed(viewFacilityPage, FacilitySection.NOTES);

			break;
		default:

			break;
		}

	}

    /**
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     * @param section
     */
	public void verifySectionDataBlockUpdateButtonDisplayed(ViewFacilityPage viewFacilityPage,
			FacilitySection section) {
		int count = viewFacilityPage.grabActiveDataBlockCount(section, true);
		for (int i = 0; i < count; i++) {
			assertTrue(viewFacilityPage.isDataBlockUpdateButtonDisplayed(section, i));

		}

	}

    /**
     * TODO (KD) - doc
     *
     * @param viewFacilityPage
     * @param expectedFacility
     */
	public void verifyAllSectionsDataBlockAndsummary(ViewFacilityPage viewFacilityPage, JSONObject expectedFacility) {
		JSONArray idArray = expectedFacility.getJSONArray("identifiers");
		verifySectionDataBlocksAndSummary(FacilitySection.IDENTIFIERS, true, viewFacilityPage, idArray);

		idArray = expectedFacility.getJSONArray("names");
		verifySectionDataBlocksAndSummary(FacilitySection.NAMES, true, viewFacilityPage, idArray);

		idArray = expectedFacility.getJSONArray("notes");
		verifySectionDataBlocksAndSummary(FacilitySection.NOTES, true, viewFacilityPage, idArray);

		idArray = expectedFacility.getJSONArray("electronic Address");
		verifySectionDataBlocksAndSummary(FacilitySection.ELECTRONIC_ADDRESSES, true, viewFacilityPage, idArray);

		idArray = expectedFacility.getJSONArray("civicAddress");
		verifySectionDataBlocksAndSummary(FacilitySection.CIVIC_ADDRESSES, false, viewFacilityPage, idArray);

		idArray = expectedFacility.getJSONArray("relationships");
		verifySectionDataBlocksAndSummary(FacilitySection.ORGANIZATION_RELATIONSHIPS, true, viewFacilityPage, idArray);

		idArray = expectedFacility.getJSONArray("otherAddress");
		verifySectionDataBlocksAndSummary(FacilitySection.OTHER_ADDRESS, false, viewFacilityPage, idArray);

		idArray = expectedFacility.getJSONArray("telecommunication");
		verifySectionDataBlocksAndSummary(FacilitySection.TELECOMMUNICATIONS, true, viewFacilityPage, idArray);
	}

    /**
     * TODO (KD) - doc
     *
     * @param section
     * @param active
     * @param viewFacilityPage
     * @param idArray
     */
	public void verifySectionDataBlocksAndSummary(FacilitySection section, boolean active,
			ViewFacilityPage viewFacilityPage, JSONArray idArray) {
		int count = viewFacilityPage.grabActiveDataBlockCount(section, active);
		
		assertEquals(count, idArray.length());
		for (int i = 0; i < count; i++) {
			LinkedHashMap<String, String> resultContent = new LinkedHashMap();
			if (FacilitySection.CIVIC_ADDRESSES == section) {
				resultContent = viewFacilityPage.grabCivicAddressBlockContent(i);
			} else if (FacilitySection.ORGANIZATION_RELATIONSHIPS == section) {
				resultContent = viewFacilityPage.grabOrgRelationshipsBlockContent(i);
			} else
				resultContent = viewFacilityPage.grabDataBlockContent(section, i);

			String resultSummaryLineText = viewFacilityPage.grabDataBlockSummaryLine(section, i);
			JSONObject expectedJson = (JSONObject) idArray.get(i);

			assertTrue(checkSectionDataBlocksKeys(section, resultContent));
			
			checkSectionDataBlocksContentsAndSummaryLine(section,expectedJson, resultContent,resultSummaryLineText);
			
		}

	}

	private void checkSectionDataBlocksContentsAndSummaryLine(FacilitySection section,JSONObject expectedJson,
			LinkedHashMap<String, String> resultContent, String resultSummaryLineText) {
		assertTrue(expectedJson != null && resultContent!=null && resultSummaryLineText!=null );
		
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

	private boolean checkSectionDataBlocksKeys(FacilitySection section, LinkedHashMap<String, String> result) {
		
		if (result == null)
			return false;
		switch (section) {
		case FacilitySection.IDENTIFIERS:
			return (result.containsKey("Facility Type") && result.containsKey("Identifier")
					&& result.containsKey("Identifier Type") && result.containsKey("Effective From")
					&& result.containsKey("Effective To") && result.containsKey("End Reason")
					&& result.containsKey("Data Source") && result.containsKey("DB Created")
					&& result.containsKey("DB Expired") && result.containsKey("Data Owner Code"));
		case FacilitySection.NAMES:
			return (result.containsKey("Name") && result.containsKey("Description")
					&& result.containsKey("Effective From")	&& result.containsKey("Effective To")
					&& result.containsKey("End Reason")	&& result.containsKey("Data Source")
					&& result.containsKey("DB Created")	&& result.containsKey("DB Expired")
					&& result.containsKey("Data Owner Code"));			
		case FacilitySection.NOTES:
			return (result.containsKey("Note Identifier") && result.containsKey("Note Text")
					&& result.containsKey("Effective From")	&& result.containsKey("Effective To")
					&& result.containsKey("End Reason")	&& result.containsKey("Data Source")
					&& result.containsKey("DB Created")	&& result.containsKey("DB Expired")
					&& result.containsKey("Data Owner Code"));
		case FacilitySection.ELECTRONIC_ADDRESSES:
			return (result.containsKey("Type") && result.containsKey("Purpose")
					&& result.containsKey("Address")
					&& result.containsKey("Effective From")	&& result.containsKey("Effective To")
					&& result.containsKey("End Reason")	&& result.containsKey("Data Source")
					&& result.containsKey("DB Created")	&& result.containsKey("DB Expired")
					&& result.containsKey("Data Owner Code"));
		case FacilitySection.CIVIC_ADDRESSES:
			return (result.containsKey("Latitude") && result.containsKey("Longitude")
					&& result.containsKey("Address Line 1")	&& result.containsKey("Address Line 2")
					&& result.containsKey("Address Line 3")	&& result.containsKey("City")
					&& result.containsKey("Province / State")	&& result.containsKey("Country")
					&& result.containsKey("Health Authority")&& result.containsKey("Health Service Delivery Area")
					&& result.containsKey("Local Health Area") && result.containsKey("Primary Care Network")
					&& result.containsKey("Community Health Service Area"));
		case FacilitySection.ORGANIZATION_RELATIONSHIPS:
			return (result.containsKey("Relationship Identifier") && result.containsKey("Relationship Type")
					&& result.containsKey("Related Organization Name")	&& result.containsKey("Related Organization Identifier")
					&& result.containsKey("Effective From")	&& result.containsKey("Effective To")
					&& result.containsKey("End Reason")	&& result.containsKey("Data Source")
					&& result.containsKey("DB Created")	&& result.containsKey("DB Expired")
					&& result.containsKey("Data Owner Code"));
		case FacilitySection.OTHER_ADDRESS:
			return (result.containsKey("Validation Status") && result.containsKey("Address Type")
					&& result.containsKey("Address Purpose") && result.containsKey("Country")
					&& result.containsKey("Address Line 1")	&& result.containsKey("Address Line 2")
					&& result.containsKey("Address Line 3")	&& result.containsKey("City")
					&& result.containsKey("State/Prov") && result.containsKey("Postal/Zip Code")
					&& result.containsKey("Effective From")	&& result.containsKey("Effective To")
					&& result.containsKey("End Reason")	&& result.containsKey("Data Source")
					&& result.containsKey("DB Created")	&& result.containsKey("DB Expired")
					&& result.containsKey("Data Owner Code"));
		case FacilitySection.TELECOMMUNICATIONS:
			return (result.containsKey("Type") && result.containsKey("Purpose")
					&& result.containsKey("Area Code") && result.containsKey("Number")
					&& result.containsKey("Extension") 
					&& result.containsKey("Effective From")	&& result.containsKey("Effective To")
					&& result.containsKey("End Reason")	&& result.containsKey("Data Source")
					&& result.containsKey("DB Created")	&& result.containsKey("DB Expired")
					&& result.containsKey("Data Owner Code"));
		default:
			return false;
		}
	
	}

    /**
     * TODO (KD) - doc
     *
     * @param section
     * @param viewFacilityPage
     */
	public void verifyDataBlockSortOrder(FacilitySection section,ViewFacilityPage viewFacilityPage)
    {
        List<String> sortKeyList = FacilityDataFields.getSortKey(section);
        List<String> dateKeyList = new ArrayList<>();
       
        dateKeyList.add("Effective From");
        dateKeyList.add("DB Created");
        List<String> previousValueList = null;
        List<String> previousDateList  = null;
       
        for (int i = 0; i < viewFacilityPage.grabDataBlockCount(section); i++)
        {
            Map<String,String> dataMap = viewFacilityPage.grabDataBlockContent(section, i);
            if (FacilitySection.ORGANIZATION_RELATIONSHIPS == section) 
            	dataMap = viewFacilityPage.grabOrgRelationshipsBlockContent(i);
            List<String> valueList = TestHelper.extractDataValueList(sortKeyList, dataMap);
            List<String> dateList  = TestHelper.extractDataValueList(dateKeyList, dataMap);
            
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
	
	
	 

}
