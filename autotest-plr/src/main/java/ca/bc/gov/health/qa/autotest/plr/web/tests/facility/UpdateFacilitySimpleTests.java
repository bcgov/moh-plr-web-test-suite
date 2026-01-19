package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.UpdateFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
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


	/*
	 *  F4-004. Validate Facility Identifiers
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetOne" })
	public void testValidateFacilityIdentifiers() {
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityIdentifiers(updatePage);

	}

	/*
	 *  F4-005. Validate Facility Name
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetOne" })
	public void testValidateFacilityName() {
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityName(updatePage,EndReason.CHG);

	}

	/*
	 *  F4-006. Validate Facility Description
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetOne" })
	public void testValidateFacilityDescription() {
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityDescription(updatePage,EndReason.CHG);

	}

	/*
	 * F4-014. Validate Facility Mailing Address Type
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetOne" })
	public void testValidateFacilityMailingAddressType() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityMailingAddressType(updatePage);
	}

	/*
	 * F4-015. Validate Facility Mailing Address Purpose
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetOne" })
	public void testValidateFacilityMailingAddressPurpose() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityMailingAddressPurpose(updatePage);
	}

	/*
	 * F4-023. Validate Facility Data Block Multiplicity
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetOne" })
	public void testValidateFacilityDataBlockMultiplicity() {

		FacilityMaintainConfig cfg = new FacilityMaintainConfig();

		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainFacilityBuilder facilityTest = fhirController.createFacility(cfg);
		OrgRoleType roleType = OrganizationDataGenerator.getInstance().randomOrgRoleType();
		String orgIPCId01 = fhirController.createOrganization(roleType).getIdentifier();
		String orgIPCId02 = fhirController.createOrganization(roleType).getIdentifier();
		fhirController.close();

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilityTest);

		actions.validateFacilityDataBlockMultiplicity(updatePage, orgIPCId01, orgIPCId02);

	}

	/*
	 *  F4-038. Validate Facility Notes Text
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetOne" })
	public void testValidateFacilityNotesTexts() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityNotesTexts(updatePage,EndReason.CHG);

	}

	/*
	 * F4-041. Validate Related Organization ID
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetOne" })
	public void testValidateRelatedOrganizationID() {

		FacilityMaintainConfig cfg = new FacilityMaintainConfig();
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		OrgRoleType roleType = OrganizationDataGenerator.getInstance().randomOrgRoleType();
		String orgIPCId01 = fhirController.createOrganization(roleType).getIdentifier();
		fhirController.close();

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.ValidateRelatedOrganizationID(updatePage, orgIPCId01);

	}

}
