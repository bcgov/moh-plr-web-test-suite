package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.LinkedHashMap;

import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.ViewFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Relationship;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
//import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
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

		actions.checkDataBlocksExpanded(viewFacilityPage, FacilitySection.IDENTIFIERS, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, FacilitySection.OTHER_ADDRESS, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, FacilitySection.CIVIC_ADDRESSES, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, FacilitySection.TELECOMMUNICATIONS, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, FacilitySection.ELECTRONIC_ADDRESSES, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, FacilitySection.ORGANIZATION_RELATIONSHIPS, true);
		actions.checkDataBlocksExpanded(viewFacilityPage, FacilitySection.NOTES, true);

		actions.expandAll(viewFacilityPage, false);
		actions.checkDataBlocksCollapsed(viewFacilityPage, FacilitySection.IDENTIFIERS, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, FacilitySection.NAMES, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, FacilitySection.OTHER_ADDRESS, false);
		actions.checkDataBlocksCollapsed(viewFacilityPage, FacilitySection.CIVIC_ADDRESSES, false);
		actions.checkDataBlocksCollapsed(viewFacilityPage, FacilitySection.TELECOMMUNICATIONS, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, FacilitySection.ELECTRONIC_ADDRESSES, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, FacilitySection.ORGANIZATION_RELATIONSHIPS, true);
		actions.checkDataBlocksCollapsed(viewFacilityPage, FacilitySection.NOTES, true);
	}

	/*
	 * Test F2-001 View Facility Details
	 */
	@Test(dataProvider = "facilityTestUserTypes", dataProviderClass = InjectableData.class)
	public void testViewFacilityDetails(UserType userType) {

		JSONObject testFacility = PlrData.getFacility("test001");
		JSONObject expectedFacility = PlrData.getFacility("default-test");

		if(UserType.SECONDARY.equals(userType)||UserType.CONSUMER.equals(userType)) {
			expectedFacility = PlrData.getFacility("default-test-second");

		}
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
		
		workflowManager_.logoutAndClose(userType);

	}

	
	/*
	 * Test F2-011 verify Provider Relationship Summary Line
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
	 * Test F2-003  Sort Order - View Facility Details Screen
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
