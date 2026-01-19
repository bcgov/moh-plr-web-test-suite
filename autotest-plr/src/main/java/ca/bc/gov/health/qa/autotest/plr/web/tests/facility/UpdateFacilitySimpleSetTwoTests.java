package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.*;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class UpdateFacilitySimpleSetTwoTests implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
	private MaintainFacilityBuilder facility;

	public UpdateFacilitySimpleSetTwoTests() {

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
			 FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail().withNotes(1);
			 facility = fhirController.createFacility(cfg);
			 fhirController.close();
		}

	}

	/*
	 * F4-001. Update Facility Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testUpdateFacility() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder org = fhirController.createOrganization(OrgRoleType.HDS);
		fhirController.close();

		actions.validateUpdatingFacility(updatePage, EndReason.CHG,org);

	}

	/*
	 * F4-002. Restrict Facility Access by User Role Simple Set 2
	 */
	@Test(groups = { "UpdateFacility",
			"UpdateFacilitySetTwo" }, dataProvider = "facilityTestUserTypes", dataProviderClass = InjectableData.class)
	public void testFacilityAccessbyUserRole(UserType userType) {

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, userType);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityAccessbyUserRole(updatePage, EndReason.CHG, userType);

		workflowManager_.logoutAndClose(userType);
	}

	/*
	 * F4-003. Adding a Facility ID Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testAddingFacilityID() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateAddingFacilityID(updatePage);

	}

	/*
	 * F4-007. Updating Facility Address Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testUpdatingFacilityAddress() {
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateUpdatingFacilityAddress(updatePage);

	}

	/*
	 * F4-008. Facility Address Recognition Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testFacilityAddressRecognition() {
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateFacilityAddressRecognition(updatePage);

	}

	/*
	 * F4-011. Facility Civic Address Latitude and Longitude Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testFacilityCivicAddressLatitudeLongitude() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		UpdateFacilitySimpleActions.validateFacilityCivicAddressLatitudeLongitude(updatePage);

	}

	/*
	 * F4-012. Health Boundary Update Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testHealthBoundaryUpdate() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateHealthBoundaryUpdate(updatePage);

	}

	/*
	 * F4-024. Data Block Unique Keys Can Not Be Changed Simple Set 2
	 *
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testDataBlockUniqueKeys() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);

		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail().withNotes(1)
				.withOrgRelationships(1);
		MaintainFacilityBuilder facilitytest = fhirController.createFacility(cfg);
		fhirController.close();

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateDataBlockUniqueKeys(updatePage);

	}

	/*
	 * F4-025. Data Owner Code for Facility( MOH) Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testDataOwnerCode() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder org = fhirController.createOrganization(OrgRoleType.HDS);
		fhirController.close();
		actions.validateDataOwnerCode(updatePage, "MOH",org);

	}

	/*
	 * F4-026. Updating Facility Telecommunication Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testUpdatingFacilityTelecommunication() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateUpdatingFacilityTelecommunication(updatePage);

	}

	/*
	 * F4-032. Updating Facility Electronic Address Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testUpdatingFacilityElectronicAddress() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateUpdatingFacilityElectronicAddress(updatePage);

	}

	/*
	 * F4-039. Generating a Default Note ID SImple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testGeneratingDefaultNoteID() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateGeneratingDefaultNoteID(updatePage);

	}

	/*
	 * F4-040. Create Facility to Organization Relationship - ORGID Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testCreateFacilityOrganizationRelationship() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder org = fhirController.createOrganization(OrgRoleType.HDS);
		MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				org.getIdentifier());
		fhirController.close();

		actions.validateCreateFacilityOrganizationRelationship(updatePage,orgQueried);

	}

	/*
	 * F4-042. Facility to Organization Relationship Validation Simple Set 2
	 *
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testFacilityOrganizationRelationshipValidation() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder orgbuild = fhirController.createOrganization(OrgRoleType.HDS);
		MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				orgbuild.getIdentifier());
		fhirController.close();

		actions.validateFacilityOrganizationRelationshipValidation(updatePage,orgQueried);

	}

	/*
	 * F4-043. Organization Name Auto Complete Selection Mapping Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testOrganizationNameAutoCompleteSelection() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder org = fhirController.createOrganization(OrgRoleType.HDS);
		fhirController.close();

		actions.validateOrganizationNameAutoCompleteSelection(updatePage,org);

	}

	/*
	 * F4-044. Retrieve Related Organization Simple Set 2
	 * (TODO: update test case, since the validation target "verify" button is not applicable)
	 * (comments in BCMOHAD-30665)
	 *
	 *
	 */
	public void testRetrieveRelatedOrganization() {

		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateRetrieveRelatedOrganization(updatePage);

	}

}
