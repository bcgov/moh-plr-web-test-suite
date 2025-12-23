package ca.bc.gov.health.qa.autotest.plr.web.tests;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.UpdateFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.ViewFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class UpdateFacilitySimpleSetTwoTests implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
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
			// FHIRController fhirController = new FHIRController(UserType.ADMIN);
			// FacilityMaintainConfig cfg = new
			// FacilityMaintainConfig().withPhone().withEmail();
			// facility = fhirController.createFacility(cfg);
			// fhirController.close();
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

		actions.validateUpdatingFacility(updatePage, EndReason.CHG);

	}

	/*
	 * F4-002. Restrict Facility Access by User Role Simple Set 2
	 */
	@Test(groups = { "UpdateFacility",
			"UpdateFacilitySetTwo" }, dataProvider = "facilityTestUserTypes", dataProviderClass = InjectableData.class)
	public void testFacilityAccessbyUserRole(UserType userType) {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, userType);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateFacilityAccessbyUserRole(updatePage, EndReason.CHG, userType);

		workflowManager_.logoutAndClose(userType);
	}

	/*
	 * F4-003. Adding a Facility ID Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testAddingFacilityID() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facility);

		actions.validateAddingFacilityID(updatePage, EndReason.CHG);

	}

	/*
	 * F4-007. Updating Facility Address Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testUpdatingFacilityAddress() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateUpdatingFacilityAddress(updatePage, EndReason.CHG);

	}

	/*
	 * F4-008. Facility Address Recognition Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testFacilityAddressRecognition() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateFacilityAddressRecognition(updatePage, EndReason.CHG);

	}

	/*
	 * F4-011. Facility Civic Address Latitude and Longitude Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testFacilityCivicAddressLatitudeLongitude() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		// FacilityMaintainConfig cfg = new
		// FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		UpdateFacilitySimpleActions.validateFacilityCivicAddressLatitudeLongitude(updatePage, EndReason.CHG);

	}

	/*
	 * F4-012. Health Boundary Update Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testHealthBoundaryUpdate() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

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
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateDataOwnerCode(updatePage, "MOH");

	}

	/*
	 * F4-026. Updating Facility Telecommunication Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testUpdatingFacilityTelecommunication() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateUpdatingFacilityTelecommunication(updatePage);

	}

	/*
	 * F4-032. Updating Facility Electronic Address Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testUpdatingFacilityElectronicAddress() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);

		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateUpdatingFacilityElectronicAddress(updatePage);

	}

	/*
	 * F4-039. Generating a Default Note ID SImple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testGeneratingDefaultNoteID() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateGeneratingDefaultNoteID(updatePage);

	}

	/*
	 * F4-040. Create Facility to Organization Relationship - ORGID Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testCreateFacilityOrganizationRelationship() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateCreateFacilityOrganizationRelationship(updatePage);

	}

	/*
	 * F4-042. Facility to Organization Relationship Validation Simple Set 2
	 *
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testFacilityOrganizationRelationshipValidation() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.createFacility(cfg);
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateFacilityOrganizationRelationshipValidation(updatePage);

	}

	/*
	 * F4-043. Organization Name Auto Complete Selection Mapping Simple Set 2
	 */
	@Test(groups = { "UpdateFacility", "UpdateFacilitySetTwo" })
	public void testOrganizationNameAutoCompleteSelection() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		FacilityMaintainConfig cfg = new FacilityMaintainConfig().withPhone().withEmail();
		MaintainFacilityBuilder facilitytest = fhirController.queryFacilityByIdentifier(IdentifierType.IFC,
				"IFC.00006933.BC.PRS");
		fhirController.close();
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();
		UpdateFacilityPage updatePage = actions.openFacility(facilitytest);

		actions.validateOrganizationNameAutoCompleteSelection(updatePage);

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
