package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.ViewFacilityActions;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Relationship;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;

public class ViewFacilitySimpleTests implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private static FHIRController fhirController;
	private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

	public ViewFacilitySimpleTests() {}

	@AfterClass
	public void teardown() {
		fhirController.close();
		workflowManager_.logoutAllAndClose();
		LOG.info("Done.");
	}

	@BeforeTest
	public void beforeTest()
	{
		fhirController = new FHIRController(UserType.ADMIN);
	}

	@BeforeMethod
	public void before(Object[] parameters) {
		 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
		 if (!workflow.isLoggedIn()) workflow.login().openPlr();
	}

	// F2-001. View Facility Details
	@Test(dataProvider = "facilityTestUserTypes", dataProviderClass = InjectableData.class)
	public void testViewFacilityDetails(UserType userType) {
		final JSONObject testFacility = PlrData.getFacility("test001");
		JSONObject expectedFacility = PlrData.getFacility("default-test");

		if (UserType.SECONDARY.equals(userType) || UserType.CONSUMER.equals(userType)) {
			expectedFacility = PlrData.getFacility("default-test-second");
		}

		logIn(workflowManager_, userType);
		final ViewFacilityActions actions = workflowManager_.getSelectedWorkflow().getViewFacilityActions();

		// Test Start
		ViewFacilityPage viewFacilityPage = actions.openFacility(testFacility.getString("fauth"));

		// Verify all facility section blocks displayed, both name and id are not empty
		actions.verifyFacilitySectionsDisplayed(viewFacilityPage);
		// Verify the Expand/collapse Button
		actions.verifyAllFacilityDataBlockExpandButtonDisplayed(viewFacilityPage);
		// Verify active displayed
		actions.verifyAllFacilityDataBlockActiveMarkDisplayed(viewFacilityPage);
		// Verify update displayed (for user type admin)
		actions.verifyAllFacilityDataBlockUpdateButtonDisplayed(viewFacilityPage, userType);
		// Verify all summary and content
		actions.verifyAllSectionsDataBlockAndSummary(viewFacilityPage, expectedFacility);

		workflowManager_.logoutAndClose(userType);
	}

	// F2-003. Sort Order - View Facility Details Screen
	@Test
	public void testSortOrderViewFacilityDetailsScreen() {
		JSONObject testFacility = PlrData.getFacility("test003");
		final ViewFacilityActions actions = workflowManager_.getSelectedWorkflow().getViewFacilityActions();
		final List<FacilitySection> sectionsToTest = Arrays.asList(
				FacilitySection.ELECTRONIC_ADDRESSES,
				FacilitySection.IDENTIFIERS,
				FacilitySection.TELECOMMUNICATIONS,
				FacilitySection.NOTES,
				FacilitySection.ORGANIZATION_RELATIONSHIPS
		);

		// Test Start
		ViewFacilityPage page = actions.openFacility(testFacility.getString("fauth"));

		for (FacilitySection section : sectionsToTest) actions.verifyDataBlockSortOrder(page, section);
	}

	// F2-005. Expand All Facility Details
	@Test
	public void testExpandAllFacilityDetails() {
		final JSONObject facility = PlrData.getFacility("test005");
		final ViewFacilityActions actions = workflowManager_.getSelectedWorkflow().getViewFacilityActions();
		List<FacilitySection> sectionsToTest = new ArrayList<>(Arrays.asList(
				FacilitySection.IDENTIFIERS,
				FacilitySection.OTHER_ADDRESS,
				FacilitySection.CIVIC_ADDRESSES,
				FacilitySection.TELECOMMUNICATIONS,
				FacilitySection.ELECTRONIC_ADDRESSES,
				FacilitySection.ORGANIZATION_RELATIONSHIPS,
				FacilitySection.NOTES
		));
		boolean active;

		// Test Start
		ViewFacilityPage page = actions.openFacility(facility.getString("fauth"));
		actions.expandAll(page, true);

		for (FacilitySection section : sectionsToTest) actions.checkDataBlocksExpanded(page, section, true);

		sectionsToTest.add(FacilitySection.NAMES);

		actions.expandAll(page, false);

		for (FacilitySection section : sectionsToTest)
		{
            active = !section.equals(FacilitySection.CIVIC_ADDRESSES) && !section.equals(FacilitySection.OTHER_ADDRESS);
			actions.checkDataBlocksCollapsed(page, section, active);
		}
	}

	
	// F2-011. Provider Relationship Summary Line
	@Test
	public void testProviderRelationshipSummaryLine() {
		final JSONObject testFacility = PlrData.getFacility("test011");
		final JSONArray orgArray = testFacility.getJSONArray("Organizations");
		final ViewFacilityActions actions = workflowManager_.getSelectedWorkflow().getViewFacilityActions();

		// Test Start
		ViewFacilityPage page = actions.openFacility(testFacility.getString("fauth"));

		int count = page.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		for (int i = 0; i < count; i++) {
			LinkedHashMap<String, String> resultContent = page.grabOrgRelationshipsBlockContent(i);
			String resultSummaryLineText = page.grabDataBlockSummaryLine(FacilitySection.ORGANIZATION_RELATIONSHIPS, i);
			Relationship reResult = new Relationship(resultContent);
			for (Object organization : orgArray) {
				JSONObject organizationJson = (JSONObject) organization;
				String rlnId = organizationJson.getString("Relationship Identifier");
				if (reResult.getRelationshipIdentifier().equals(rlnId)) {
					String orgName = organizationJson.getString("Organization Name");

					if (orgName.length() <= 30)
						assertTrue(resultSummaryLineText.contains(orgName), "Organization Name is missing");
					else {
						String orgNameFirst30 = orgName.substring(0, 30);
						assertTrue(resultSummaryLineText.contains(orgNameFirst30),
								"Organization name missing first 30 characters");
                        assertFalse(resultSummaryLineText.contains(orgName),
								"Organization Name includes more than 30 characters unexpectedly");
					}

					break;
				}
			}
		}

	}

}
