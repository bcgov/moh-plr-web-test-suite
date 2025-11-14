package ca.bc.gov.health.qa.autotest.plr.web.tests;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.navigateToSearchFacilityPage;
import static java.util.Objects.requireNonNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.core.util.text.TextUtils;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.ViewFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Identifier;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Relationship;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewMode;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.SearchFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class ViewFacilitySimpleTests implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

	public ViewFacilitySimpleTests() {
	}

	@AfterClass
	public void teardown() {
		workflowManager_.logoutAllAndClose();
		LOG.info("Done.");
	}

	@BeforeMethod
	public void before(Object[] parameters) {

		/*
		 * PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters,
		 * UserType.ADMIN); if (!workflow.isLoggedIn()) { workflow.login().openPlr(); }
		 */

	}

	/*
	 * Test F2-005 verify the expan-all button of view facility page
	 */
	@Test
	public void testExpandAllFacilityDetails() {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);

		JSONObject facility = PlrData.getFacility("test005");

		ViewFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getViewFacilitySimpleActions();
		ViewFacilityPage viewFacilityPage = actions.openFacility(facility.getString("fauth"));
		actions.expandAll(viewFacilityPage, true);

		actions.checkDataBlocksExpanded(viewFacilityPage, ProviderSection.IDENTIFIERS, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, ProviderSection.OTHER_ADDRESSES, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, ProviderSection.CIVIC_ADDRESSES, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, ProviderSection.TELECOMMUNICATIONS, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, ProviderSection.ELECTRONIC_ADDRESSES, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, ProviderSection.ORGANIZATION_RELATIONSHIPS, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, ProviderSection.NOTES, true);

		actions.expandAll(viewFacilityPage, false);
		actions.checkDataBlocksCollapsed(viewFacilityPage, ProviderSection.IDENTIFIERS, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, ProviderSection.NAMES, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, ProviderSection.OTHER_ADDRESSES, false);
		actions.checkDataBlocksCollapsed(viewFacilityPage, ProviderSection.CIVIC_ADDRESSES, false);
		actions.checkDataBlocksCollapsed(viewFacilityPage, ProviderSection.TELECOMMUNICATIONS, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, ProviderSection.ELECTRONIC_ADDRESSES, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, ProviderSection.ORGANIZATION_RELATIONSHIPS, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, ProviderSection.NOTES, true);
	}

	/*
	 * Test F2-001 verify details of view facility page with particular user type
	 */
	@Test(dataProvider = "facilityTestUserTypes", dataProviderClass = InjectableData.class)
	public void testViewFacilityDetails(UserType userType) {

		JSONObject testFacility = PlrData.getFacility("test001");
		JSONObject expectedFacility = PlrData.getFacility("default-test");

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, userType);
		ViewFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getViewFacilitySimpleActions();
		ViewFacilityPage viewFacilityPage = actions.openFacility(testFacility.getString("fauth"));

		// verify all facility section blocks displayed ,both name and id are not empty
		actions.verifyFacilitySectionsDisplayed(viewFacilityPage);
		// verify the Expand/collapse Button
		actions.verifyAllFacilityDataBlockExpandButtonDisplayed(viewFacilityPage);
		// verify active displayed
		actions.verifyAllFacilityDataBlockActiveMarkDisplayed(viewFacilityPage, userType);
		// verify update displayed (for user type admin)
		actions.verifyAllFacilityDataBlockUpdateButtonDisplayed(viewFacilityPage, userType);
		// verify all summary and content
		actions.verifyAllSectionsDataBlockAndsummary(viewFacilityPage, expectedFacility);

	}

	
	/*
	 * Test F2-011 verify details of view facility page with SecondarySrc user type
	 */
	@Test
	public void testProviderRelationshipSummaryLine() {

		JSONObject testFacility = PlrData.getFacility("test011");
		

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		ViewFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getViewFacilitySimpleActions();
		ViewFacilityPage viewFacilityPage = actions.openFacility(testFacility.getString("fauth"));

		JSONArray orgArray = testFacility.getJSONArray("Organizations");

		int count = viewFacilityPage.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		for (int i = 0; i < count; i++) {
			LinkedHashMap<String, String> resultContent = viewFacilityPage.grabOrgRelationshipsBlockContent(i);
			String resultSummaryLineText = viewFacilityPage
					.grabDataBlockSummaryLine(FacilitySection.ORGANIZATION_RELATIONSHIPS, i);
			Relationship reResult = new Relationship(resultContent);
			for (Object organization : orgArray) {
				JSONObject organizationJson = (JSONObject) organization;
				String rlnId = organizationJson.getString("Relationship Identifier");
				if (reResult.getRelationshipIdentifier().equals(rlnId)) {
					String orgName = organizationJson.getString("Organization Name");

					if (orgName.length() <= 30)
						assertTrue(resultSummaryLineText.contains(orgName));
					else {
						String orgNameFirst30 = orgName.substring(0, Math.min(orgName.length(), 30));
						assertTrue(resultSummaryLineText.contains(orgNameFirst30));
						assertTrue(!resultSummaryLineText.contains(orgName));

					}

					break;
				}
			}
		}

	}

	/*
	 * Test F2-003 verify details of view facility page with SecondarySrc user type
	 */
	@Test
	public void testSortOrderViewFacilityDetailsScreen() {

		JSONObject testFacility = PlrData.getFacility("test003");
		

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		ViewFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getViewFacilitySimpleActions();
		ViewFacilityPage viewFacilityPage = actions.openFacility(testFacility.getString("fauth"));

		actions.verifyDataBlockSortOrder(FacilitySection.ELECTRONIC_ADDRESSES, viewFacilityPage);
		actions.verifyDataBlockSortOrder(FacilitySection.IDENTIFIERS, viewFacilityPage);
		actions.verifyDataBlockSortOrder(FacilitySection.TELECOMMUNICATIONS, viewFacilityPage);
		actions.verifyDataBlockSortOrder(FacilitySection.NOTES, viewFacilityPage);
		actions.verifyDataBlockSortOrder(FacilitySection.ORGANIZATION_RELATIONSHIPS, viewFacilityPage);
	}

}
