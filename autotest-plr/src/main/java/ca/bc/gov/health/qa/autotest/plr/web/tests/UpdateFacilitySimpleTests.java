package ca.bc.gov.health.qa.autotest.plr.web.tests;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.UpdateFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.ViewFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class UpdateFacilitySimpleTests implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

	public UpdateFacilitySimpleTests() {
	}

	@AfterClass
	public void teardown() {
		workflowManager_.logoutAllAndClose();
		LOG.info("Done.");
	}

	@BeforeMethod
	public void before(Object[] parameters) {

		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
		if (!workflow.isLoggedIn()) {
			workflow.login().openPlr();
		}

	}

	@Test
	public void testValidateFacilityIdentifiers() {
		String errMsg = "";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);

		JSONObject facility = PlrData.getFacility("test-update");
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility.getString("fauth"));

		actions.validateFacilityIdentifiers(updatePage);

	}

	@Test
	public void testValidateFacilityName() {
		String errMsg = "";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		JSONObject facility = PlrData.getFacility("test-update");
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility.getString("fauth"));

		actions.validateFacilityName(updatePage);

	}

	@Test
	public void testValidateFacilityDescription() {
		String errMsg = "";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);

		JSONObject facility = PlrData.getFacility("test-update");
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility.getString("fauth"));

		actions.validateFacilityDescription(updatePage);

	}

	@Test
	public void testValidateFacilityMailingAddressType() {
		String errMsg = "";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);

		JSONObject facility = PlrData.getFacility("test-update");
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility.getString("fauth"));

		actions.validateFacilityMailingAddressType(updatePage);

	}

	@Test
	public void testValidateFacilityMailingAddressPurpose() {
		String errMsg = "";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);

		JSONObject facility = PlrData.getFacility("test-update");
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility.getString("fauth"));

		actions.validateFacilityMailingAddressPurpose(updatePage);

	}

	@Test
	public void testValidateFacilityDataBlockMultiplicity() {
		String errMsg = "";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);

		JSONObject facility = PlrData.getFacility("test-update23");
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility.getString("fauth"));

		actions.validateFacilityDataBlockMultiplicity(updatePage);

	}

	@Test
	public void testValidateFacilityNotesTexts() {
		String errMsg = "";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);

		JSONObject facility = PlrData.getFacility("test-update");
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility.getString("fauth"));

		actions.validateFacilityNotesTexts(updatePage);

	}

	@Test
	public void testValidateRelatedOrganizationID() {
		String errMsg = "";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);

		JSONObject facility = PlrData.getFacility("test-update41");
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility.getString("fauth"));

		actions.ValidateRelatedOrganizationID(updatePage);

	}

}
