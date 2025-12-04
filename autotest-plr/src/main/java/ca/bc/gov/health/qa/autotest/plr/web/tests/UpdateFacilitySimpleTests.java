package ca.bc.gov.health.qa.autotest.plr.web.tests;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.OrganizationDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.UpdateFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class UpdateFacilitySimpleTests implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
	private MaintainFacilityBuilder facility;

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
		if (facility == null) {
			FHIRController fhirController = new FHIRController(UserType.ADMIN);
			FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
			facility = fhirController.createFacility(cfg);
			fhirController.close();
		}

	}

	@Test
	public void testValidateFacilityIdentifiers() {

		// PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityIdentifiers(updatePage);

	}

	@Test
	public void testValidateFacilityName() {
		// PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityName(updatePage);

	}

	@Test
	public void testValidateFacilityDescription() {
		// PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityDescription(updatePage);

	}

	@Test
	public void testValidateFacilityMailingAddressType() {

		// PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityMailingAddressType(updatePage);
	}

	@Test
	public void testValidateFacilityMailingAddressPurpose() {

		// PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityMailingAddressPurpose(updatePage);
	}

	@Test
	public void testValidateFacilityDataBlockMultiplicity() {

		FacilityMaintainConfig cfg = new FacilityMaintainConfig();
		// create facility for testing
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainFacilityBuilder facilityTest = fhirController.createFacility(cfg);
		OrgRoleType roleType = OrganizationDataGenerator.getInstance().randomOrgRoleType();
		String orgIPCId01 = fhirController.createOrganization(roleType).getIdentifier();
		String orgIPCId02 = fhirController.createOrganization(roleType).getIdentifier();
		fhirController.close();

		// PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilityTest);

		actions.validateFacilityDataBlockMultiplicity(updatePage, orgIPCId01, orgIPCId02);

	}

	@Test
	public void testValidateFacilityNotesTexts() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityNotesTexts(updatePage);

	}

	@Test
	public void testValidateRelatedOrganizationID() {

		FacilityMaintainConfig cfg = new FacilityMaintainConfig();
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		// MaintainFacilityBuilder facilityTest = fhirController.createFacility(cfg);
		OrgRoleType roleType = OrganizationDataGenerator.getInstance().randomOrgRoleType();
		String orgIPCId01 = fhirController.createOrganization(roleType).getIdentifier();
		fhirController.close();

		// PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.ValidateRelatedOrganizationID(updatePage, orgIPCId01);

	}

}
