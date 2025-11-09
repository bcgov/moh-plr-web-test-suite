package ca.bc.gov.health.qa.autotest.plr.web.actions;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.*;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewMode;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.core.util.text.TextUtils;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;

public class ViewFacilitySimpleActions {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private static final Pattern VALUE_CODE_PATTERN = Pattern.compile("^.*\\((?<code>[^()]+)\\)\\s*$");

	private final SeleniumSession selenium_;
	private final URI uri_;
	private final UserType userType_;

	/*
	 * private ViewFacilityPage viewFacilityPage;
	 * 
	 * public ViewFacilityPage getViewFacilityPage() { return viewFacilityPage; }
	 * 
	 * public void setViewFacilityPage(ViewFacilityPage viewFacilityPage) {
	 * this.viewFacilityPage = viewFacilityPage; }
	 */

	public ViewFacilitySimpleActions(SeleniumSession selenium, URI uri, UserType userType) {
		selenium_ = selenium;
		uri_ = uri;
		userType_ = userType;
	}

	public ViewFacilityPage openFacility(String fauthId) {
		LOG.info("Open facility({}).", fauthId);
		ViewFacilityPage viewFacilityPage = new ViewFacilityPage(selenium_, uri_.resolve("plr/FacilityDetails.xhtml"));
		viewFacilityPage.openFacility(fauthId);
		viewFacilityPage.waitForReady();
		return viewFacilityPage;
	}

	/**
	 * verify all facility section blocks displayed verify name and identifier
	 * blocks not empty
	 * 
	 * @param viewFacilityPage
	 *
	 * @param
	 * 
	 */
	public void verifyFacilitySectionsDisplayed(ViewFacilityPage viewFacilityPage) {
		ViewMode viewMode = viewFacilityPage.getViewHeader().grabViewMode();

		for (ProviderSection section : getFacilitySectionSet(ProviderType.FACILITY, userType_)) {
			viewFacilityPage.scrollToSection(section);
			assertTrue(viewFacilityPage.grabSectionDisplayed(section),
					String.format("Section displayed (%s)", section));
			if (section.name().equalsIgnoreCase("IDENTIFIERS") || section.name().equalsIgnoreCase("NAMES")) {
				assertTrue(viewFacilityPage.grabActiveDataBlockCount(section, true) > 0,
						String.format("Presence of active data blocks (%s)", section));
			}

		}

	}

	public void verifySectionsWithActiveDataBlocks(boolean expectedInactive) {
		ViewFacilityPage viewFacilityPage = waitForViewFacilityPage();
		ViewMode viewMode = viewFacilityPage.getViewHeader().grabViewMode();

		for (ProviderSection section : getFacilitySectionSet(ProviderType.FACILITY, userType_)) {
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

	}

	public static Set<ProviderSection> getFacilitySectionSet(ProviderType providerType, UserType userType) {
		Set<ProviderSection> set = EnumSet.copyOf(ProviderSection.getProviderSectionSet(providerType));
		if (!userType.equals(UserType.ADMIN)) {
			set.remove(ProviderSection.REGISTRY_IDENTIFIERS);
		}
		return Collections.unmodifiableSet(set);
	}

	private ViewFacilityPage waitForViewFacilityPage() {
		ViewFacilityPage page = new ViewFacilityPage(selenium_);
		page.waitForReady();
		return page;
	}

	public void expandAll(ViewFacilityPage viewFacilityPage, boolean expand) {
		assertNotNull(viewFacilityPage);
		viewFacilityPage.expandAll(expand);
		try {
			viewFacilityPage.wait(1000);
		} catch (InterruptedException e) {
			fail("unexpected interruption");
		}
	}

	public void checkDataBlocksExpanded(ViewFacilityPage viewFacilityPage, ProviderSection section, boolean active) {
		assertNotNull(viewFacilityPage);
		int count = viewFacilityPage.grabActiveDataBlockCount(section, active);
		for (int i = 0; i < count; i++) {
			assertTrue(viewFacilityPage.grabDataBlockExpanded(section, i));
		}
	}

	public void checkDataBlocksCollapsed(ViewFacilityPage viewFacilityPage, ProviderSection section, boolean active) {
		assertNotNull(viewFacilityPage);
		int count = viewFacilityPage.grabActiveDataBlockCount(section, active);
		for (int i = 0; i < count; i++) {
			assertTrue(!viewFacilityPage.grabDataBlockExpanded(section, i));
		}

	}

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

	public void verifySectionDataBlockActiveMarkDisplayed(ViewFacilityPage viewFacilityPage, FacilitySection section) {
		int count = viewFacilityPage.grabActiveDataBlockCount(section, true);
		for (int i = 0; i < count; i++) {
			assertTrue(viewFacilityPage.isDataBlockActiveMarkDisplayed(section, i));

		}

	}

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

	public void verifySectionDataBlockUpdateButtonDisplayed(ViewFacilityPage viewFacilityPage,
			FacilitySection section) {
		int count = viewFacilityPage.grabActiveDataBlockCount(section, true);
		for (int i = 0; i < count; i++) {
			assertTrue(viewFacilityPage.isDataBlockUpdateButtonDisplayed(section, i));

		}

	}

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
			assertTrue(idResult.equals(idExpected));
			
			assertTrue(resultSummaryLineText.contains(idExpected.getIdType())
					&& resultSummaryLineText.contains(idExpected.getIdentifier())
					&& resultSummaryLineText.contains(idExpected.getDataOwnerCode()));
			break;
		case FacilitySection.NAMES:
			Name nameResult = new Name(resultContent);
			Name nameExpected = new Name(expectedJson);
			assertTrue(nameResult.equals(nameExpected));
			
			assertTrue(resultSummaryLineText.contains(nameExpected.getName()));
					
			break;
		case FacilitySection.NOTES:
			Note noteResult = new Note(resultContent);
			Note noteExpected = new Note(expectedJson);
			assertTrue(noteResult.equals(noteExpected));
			
			assertTrue(resultSummaryLineText.contains(noteExpected.getNoteIdentifier())
					&& resultSummaryLineText.contains(noteExpected.getDataOwnerCode())
					);
			break;
		case FacilitySection.ELECTRONIC_ADDRESSES:
			ElectronicAddress eAddrResult = new ElectronicAddress(resultContent);
			ElectronicAddress eAddrExpected = new ElectronicAddress(expectedJson);
			assertTrue(eAddrResult.equals(eAddrExpected));
			
			assertTrue(resultSummaryLineText.contains(eAddrExpected.getPurposeSummary())
					&& resultSummaryLineText.contains(eAddrExpected.getTypeSummary())
					&& resultSummaryLineText.contains(eAddrExpected.getAddress())
					&& resultSummaryLineText.contains(eAddrExpected.getDataOwnerCode()));
			break;
		case FacilitySection.CIVIC_ADDRESSES:
			CivicAddress cAddrResult = new CivicAddress(resultContent);
			CivicAddress cAddrExpected = new CivicAddress(expectedJson);
			assertTrue(cAddrResult.equals(cAddrExpected));
			
			assertTrue(resultSummaryLineText.contains(cAddrExpected.getAddressLine1())
					&& resultSummaryLineText.contains(cAddrExpected.getCity())
					&& resultSummaryLineText.contains(cAddrExpected.getProvinceStateSummary())
					&& resultSummaryLineText.contains(cAddrExpected.getDataOwnerCode()));
			break;
		case FacilitySection.ORGANIZATION_RELATIONSHIPS:
			Relationship reResult = new Relationship(resultContent);
			Relationship reExpected = new Relationship(expectedJson);
			assertTrue(reResult.equals(reExpected));
			
			assertTrue(resultSummaryLineText.contains(reExpected.getRelatedProviderRoleType())
					&& resultSummaryLineText.contains(reExpected.getRelationshipTypeSummary())
					&& resultSummaryLineText.contains(reExpected.getRelatedOrganizationName())
					&& resultSummaryLineText.contains(reExpected.getDataOwnerCode()));
			break;
		case FacilitySection.OTHER_ADDRESS:
			OtherAddress oAddrResult = new OtherAddress(resultContent);
			OtherAddress oAddExpected = new OtherAddress(expectedJson);
			assertTrue(oAddrResult.equals(oAddExpected));
			
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
			assertTrue(teleResult.equals(teleExpected));
			
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
		return;
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
			return (result.containsKey("Facility Type") && result.containsKey("Identifier")
					&& result.containsKey("Identifier Type") && result.containsKey("Effective From")
					&& result.containsKey("Effective To") && result.containsKey("End Reason")
					&& result.containsKey("Data Source") && result.containsKey("DB Created")
					&& result.containsKey("DB Expired") && result.containsKey("Data Owner Code"));
			
		case FacilitySection.NOTES:
			return (result.containsKey("Facility Type") && result.containsKey("Identifier")
					&& result.containsKey("Identifier Type") && result.containsKey("Effective From")
					&& result.containsKey("Effective To") && result.containsKey("End Reason")
					&& result.containsKey("Data Source") && result.containsKey("DB Created")
					&& result.containsKey("DB Expired") && result.containsKey("Data Owner Code"));
		case FacilitySection.ELECTRONIC_ADDRESSES:
			return (result.containsKey("Facility Type") && result.containsKey("Identifier")
					&& result.containsKey("Identifier Type") && result.containsKey("Effective From")
					&& result.containsKey("Effective To") && result.containsKey("End Reason")
					&& result.containsKey("Data Source") && result.containsKey("DB Created")
					&& result.containsKey("DB Expired") && result.containsKey("Data Owner Code"));
		case FacilitySection.CIVIC_ADDRESSES:
			return (result.containsKey("Facility Type") && result.containsKey("Identifier")
					&& result.containsKey("Identifier Type") && result.containsKey("Effective From")
					&& result.containsKey("Effective To") && result.containsKey("End Reason")
					&& result.containsKey("Data Source") && result.containsKey("DB Created")
					&& result.containsKey("DB Expired") && result.containsKey("Data Owner Code"));
		case FacilitySection.ORGANIZATION_RELATIONSHIPS:
			return (result.containsKey("Facility Type") && result.containsKey("Identifier")
					&& result.containsKey("Identifier Type") && result.containsKey("Effective From")
					&& result.containsKey("Effective To") && result.containsKey("End Reason")
					&& result.containsKey("Data Source") && result.containsKey("DB Created")
					&& result.containsKey("DB Expired") && result.containsKey("Data Owner Code"));
		case FacilitySection.OTHER_ADDRESS:
			return (result.containsKey("Facility Type") && result.containsKey("Identifier")
					&& result.containsKey("Identifier Type") && result.containsKey("Effective From")
					&& result.containsKey("Effective To") && result.containsKey("End Reason")
					&& result.containsKey("Data Source") && result.containsKey("DB Created")
					&& result.containsKey("DB Expired") && result.containsKey("Data Owner Code"));
		case FacilitySection.TELECOMMUNICATIONS:
			return (result.containsKey("Facility Type") && result.containsKey("Identifier")
					&& result.containsKey("Identifier Type") && result.containsKey("Effective From")
					&& result.containsKey("Effective To") && result.containsKey("End Reason")
					&& result.containsKey("Data Source") && result.containsKey("DB Created")
					&& result.containsKey("DB Expired") && result.containsKey("Data Owner Code"));
		default:
			return false;
		}
	
	}

}
