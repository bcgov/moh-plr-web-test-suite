package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.UpdateProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ElectronicAddressType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.TelecommunicationType;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

public class UpdateProviderLegacyTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
	private static final Config config_ = ConfigProvider.get().getConfig();
	private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
	public static JSONObject errorList;
	private MaintainIndividualBuilder defaultBC;
	MaintainOrgBuilder defaultOrg;
	private Map<ProviderType, MaintainRequestBuilder> defaultProviders = new HashMap<>();

	private UpdateProviderLegacyTest() {
		try {
			errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
		} catch (IOException e) {
			String msg = String.format("Failed to read JSON data (%s).", errorPath);
			throw new IllegalStateException(msg, e);
		}
	}

	@AfterClass
	public void teardown() {
		//workflowManager_.logoutAllAndClose();
		LOG.info("Done.");
	}

	@BeforeMethod
	public void before(Object[] parameters) {
		/*PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
		if (!workflow.isLoggedIn()) {
			workflow.login().openPlr();
		}*/
	}

	@BeforeTest
	public void beforeTest() {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainIndividualBuilder individual = fhirController.createIndividual(
				new IndividualMaintainConfig(IndividualRoleType.DEN).withNotes(1).withStatuses(1).withEmail().withPhone()
				);
		LOG.info("Created default BC provider with IPC: {}", individual.getIdentifier(IdentifierType.IPC));
		defaultBC=fhirController.queryIndividualByIdentifier(IdentifierType.IPC, individual.getIdentifier(IdentifierType.IPC));

		OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG)
				.withNotes(1).withStatuses(1).withEmail().withPhone();
		MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);
		defaultOrg = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC, org.getIdentifier(IdentifierType.IPC));
		LOG.info("Created default Organization provider with IPC: {}", org.getIdentifier(IdentifierType.IPC));
		fhirController.close();

		defaultProviders.put(ProviderType.BC_PRACTITIONER, defaultBC);
		defaultProviders.put(ProviderType.OOP_PRACTITIONER, defaultOrg);

	}

	private String getTestProvideridentifier(ProviderType providerType) {
		String identifier = "";
		if (providerType.equals(ProviderType.BC_PRACTITIONER))
			identifier = defaultBC.getIdentifier(IdentifierType.IPC);
		else if (providerType.equals(ProviderType.ORGANIZATION))
			identifier = defaultOrg.getIdentifier(IdentifierType.IPC);
		return identifier;
	}

	private String getTestProviderId(ProviderType providerType) {
		String identifier = "";
		if (providerType.equals(ProviderType.BC_PRACTITIONER))
			identifier = defaultBC.getIdentifier(IdentifierType.DENID);
		else if (providerType.equals(ProviderType.ORGANIZATION))
			identifier = defaultOrg.getIdentifier(IdentifierType.ORGID);
		return identifier;
	}

//	==============PLR 596=================	
//	#Then Add Identifiers
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testAddIdentifiers(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);
		int count = page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
		page.addIdentifiersDataBlock("IPC", idString, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), false);
		assertEquals(page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true), count + 1);

		page.ceaseDataBlockByKey(ProviderSection.IDENTIFIERS, "Identifier", idString);

	}

//
//	#Then Add Registry Identifier
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testAddRegistryIdentifiers(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);

		// int count=page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
		page.ceaseDataBlockByKey(ProviderSection.REGISTRY_IDENTIFIERS, "Identifier", identifier);
		page.addRegIdentifiersDataBlock("IPC", pauthId, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), false);
		LinkedHashMap<String, String> content = page.grabDataBlockByKey(ProviderSection.REGISTRY_IDENTIFIERS, "Type",
				"Internal Provider ID (IPC)");
		String newIPC = content.get("Identifier");
		assertEquals(newIPC, identifier);

	}

//
//	#Then Add Statuses
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testAddStatuses(ProviderType providerType) {
		String errorMsg = errorList.getString("errorStatusReasonCodeMissing");
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);
		// Provider: no status reason code
		page.ceaseAllDataBlockUnderSection(ProviderSection.STATUSES);
		String msg = page.addStatusDataBlock("LIC", "ACTIVE", null, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), true);
		assertEquals(errorMsg, msg);
		msg = page.addStatusDataBlock("LIC", "ACTIVE", "GS", UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), false);
		assertTrue(StringUtils.isEmpty(msg));

	}

//	#Then Assign New CPN to an Existing Provider Record--not applicable

//	#Then Code Restriction Validation - Identifier

	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testCodeRestrictionValidationIdentifier(ProviderType providerType) {
		List<String> expectList = new ArrayList<>(Arrays.asList("CPN - Common Party Number",
				"IPC - Internal Provider Code", "DENID - Dentist ID Number"));
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);
		List<String> typeList = page.getAddDataBloackDropdownMenuList(ProviderSection.IDENTIFIERS, "providerType");
		assertTrue(UpdateSimpleHelper.haveSameElements(expectList, typeList));

	}

//	#Then Code Restriction Validation - Status Code (PLR596 and PLR 609)
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testCodeRestrictionValidationStatusCode(ProviderType providerType) {

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);;
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		Map<String, List<String>> map = actions.getStatusCodeToReasonCodeMap();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);

		List<String> statusCodeList = page.getAddDataBloackDropdownMenuList(ProviderSection.STATUSES, "statusCode");

		for (String code : statusCodeList) {
			List<String> reasonCodeList = page.getStatusReasonCodeList(code);
			List<String> expectList = map.get(code);
			assertTrue(UpdateSimpleHelper.haveSameElements(expectList, reasonCodeList));
		}

	}

//	#Then Generating a Default Note ID
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testGeneratingDefaultNoteID(ProviderType providerType) {

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);;
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);

		page.ceaseAllDataBlockUnderSection(ProviderSection.NOTES);
		String msg = page.addNoteDataBlock(null, "NoteText:" + UpdateSimpleHelper.generateAlphabetString(10),
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date(), false);
		assertTrue(StringUtils.isEmpty(msg));

		LinkedHashMap<String, String> content = page.grabDataBlockContent(ProviderSection.NOTES, 0);
		String noteId = content.get("Note Identifier");
		assertTrue(noteId.startsWith("NC") && noteId.endsWith("PRS"));

		int startIndex = noteId.indexOf('.');
		int endIndex = noteId.indexOf('.', startIndex + 1); // Start searching after the first char
		String extractedPart = noteId.substring(startIndex + 1, endIndex);

		assertTrue(UpdateSimpleHelper.isStringPositiveInteger(extractedPart));
	}

//
//	#Then Validate Note (PLR596 and PLR 609)
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateNote(ProviderType providerType) {
		String errorMsg01 = errorList.getString("errorNoteTextMissing");
		String errorMsg02 = errorList.getString("errorNoteTextLength");
		// String errorMsg03="GRS.SYS.UNK.UNK.1.0.5000: Entry Error. Some mandatory data
		// is missing in your transaction. Your transaction has not been processed.
		// Correct and resubmit. The following fields must be supplied.";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);;
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);

		page.ceaseAllDataBlockUnderSection(ProviderSection.NOTES);
		String noteId = "NoteId-" + UpdateSimpleHelper.generateAlphabetString(6);
		String noteText = "NoteText:" + UpdateSimpleHelper.generateAlphabetString(10);
		String msg = page.addNoteDataBlock(noteId, null, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), true);
		assertEquals(msg, errorMsg01);
		page.addNoteDataBlock(noteId, noteText, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), false);
		LinkedHashMap<String, String> content = page.grabDataBlockByKey(ProviderSection.NOTES, "Note Identifier",
				noteId);
		assertEquals(noteText, content.get("Note Text"));

		msg = page.addNoteDataBlock("NoteId-" + UpdateSimpleHelper.generateAlphabetString(6),
				UpdateSimpleHelper.generateAlphabetString(256), UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), true);
		assertEquals(msg, errorMsg02);

		msg = page.addNoteDataBlock("", "", UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), true);
		assertEquals(msg, errorMsg01);

		msg = page.addNoteDataBlock("NoteId-" + UpdateSimpleHelper.generateAlphabetString(6),
				"NoteText:" + UpdateSimpleHelper.generateAlphabetString(10), UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), false);
		assertTrue(StringUtils.isEmpty(msg));

	}

//
//	#Then Validate Status Class Code (PLR596 and PLR 609)
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateStatusClassCode(ProviderType providerType) {
		String[] expctArray = { "LIC - Licensure", "AE - Assigned Entity" };

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);;
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);

		List<String> codeList = page.getAddDataBloackDropdownMenuList(ProviderSection.STATUSES, "statusClassCode");
		List<String> expectList = Arrays.asList(expctArray);
		assertTrue(UpdateSimpleHelper.haveSameElements(expectList, codeList));

	}

// 
//	#Then Validate Status Reason Code (PLR596 and PLR 609)
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateStatusReasonCode(ProviderType providerType) {
		String errorMsg = errorList.getString("errorStatusReasonCodeMissing");
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);;
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);
		page.ceaseAllDataBlockUnderSection(ProviderSection.STATUSES);
		String msg = page.addStatusDataBlock("LIC", "ACTIVE", null, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), true);
		assertEquals(msg, errorMsg);
		msg = page.addStatusDataBlock("LIC", "ACTIVE", "GS", UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), false);
		assertTrue(StringUtils.isEmpty(msg));
	}

//
//	#Then Validate Status Type Code (PLR596 and PLR 609)
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateStatusTypeCode(ProviderType providerType) {
		String errorMsg = errorList.getString("errorStatusCodeMissing");
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);

		page.ceaseAllDataBlockUnderSection(ProviderSection.STATUSES);
		String msg = page.addStatusDataBlock("LIC", "Select One", "UNK - Unknown", UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), true);
		assertEquals(msg, errorMsg);
		msg = page.addStatusDataBlock("LIC", "ACTIVE", "GS", UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), false);
		assertTrue(StringUtils.isEmpty(msg));
	}

//
//	#Then Add Notes
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testAddNotes(ProviderType providerType) {

		PlrWebWorkflow workflow =TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);

		// int count=page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
		String noteId = "NoteId-" + UpdateSimpleHelper.generateAlphabetString(6);
		String noteText = "NoteText:" + UpdateSimpleHelper.generateAlphabetString(10);
		page.addNoteDataBlock(noteId, noteText, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), false);

		LinkedHashMap<String, String> content = page.grabDataBlockByKey(ProviderSection.NOTES, "Note Identifier",
				noteId);
		assertEquals(noteText, content.get("Note Text"));

		noteText = "NoteTextUpdate:" + UpdateSimpleHelper.generateAlphabetString(10);
		int index = page.findDataBloackIndexByKey(ProviderSection.NOTES, "Note Identifier", noteId);
		page.updateNoteDataBlock(noteText, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
	}

//	==============PLR 609================
//
//			Then Ceasing Last Active Provider Identifier
	
	
	@Test(dataProvider = "providerTestUserTypesNonAdmin", dataProviderClass = InjectableData.class, groups = {
			"UpdateProviderLegacy" })
	public void testCeasingLastActiveProviderIdentifier(UserType userType) {
		
		String errorMsg = errorList.getString("errorCeaselastActiveProviderIdentifier");
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainIndividualBuilder defaultTestBC = fhirController
				.createIndividual(new IndividualMaintainConfig(IndividualRoleType.DEN).withNotes(1));

		MaintainOrgBuilder defaultTestOrg = fhirController.createOrganization(OrgRoleType.ORG);
		fhirController.close();
		// PLR login
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_,userType);
		UpdateProviderActions actions = workflow.getUpdateProviderActions();
		String identifier = defaultTestBC.getIdentifier(IdentifierType.IPC);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);
		int count = page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
		if(UserType.PRIMARY.equals(userType)) {
			for (int i = 0; i < count - 1; i++) {
				page.ceaseDataBlock(ProviderSection.IDENTIFIERS, i);
			}
			String msg = page.ceaseDataBlockWithError(ProviderSection.IDENTIFIERS, 0);
			assertTrue(msg.equals(errorMsg));
		}
		if(UserType.SECONDARY.equals(userType)) {
			// secondary has no permission to cease CPN/IPN
			int index = page.findDataBloackIndexByKey(ProviderSection.IDENTIFIERS, "Type",
					"Common Provider NUMBER (CPN)");
			String msg = page.ceaseDataBlockWithError(ProviderSection.IDENTIFIERS, index);
			assertTrue(msg.contains("GRS.DPS.UNK.UPD.1.0.7050: You don't have access to update this record"));
			
		}
		if(UserType.CONSUMER.equals(userType)) {
			// CONSUMER has no permission to update/cease identifier
			for (int i = 0; i < count - 1; i++)
				assertFalse(page.isDataBlockUpdateButtonDisplayed(ProviderSection.IDENTIFIERS, i));
		}
		PlrWebWorkflow workflowAdm = PlrWebWorkflow.create(UserType.ADMIN);
		workflowAdm.login().openPlr();
		actions = workflowAdm.getUpdateProviderActions();
		page = actions.openProvider(pauthId);
		page.ceaseAllDataBlockUnderSection(ProviderSection.IDENTIFIERS);
		count = page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
		assertTrue(count == 0);
		workflowAdm.logout();
		workflowAdm.close();
		// ORG
		//workflow = workflowManager_.selectWorkflow(userType);
        //workflow.login().openPlr();
		identifier = defaultTestOrg.getIdentifier(IdentifierType.IPC);
		pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		actions = workflow.getUpdateProviderActions();
		page = actions.openProvider(pauthId);
		count = page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
		if(UserType.PRIMARY.equals(userType)) {
			for (int i = 0; i < count - 1; i++) {
				page.ceaseDataBlock(ProviderSection.IDENTIFIERS, i);
			}
			String msg = page.ceaseDataBlockWithError(ProviderSection.IDENTIFIERS, 0);
			assertTrue(msg.equals(errorMsg));
		}
		if(UserType.SECONDARY.equals(userType)) {
			// secondary has no permission to cease CPN/IPN
			int index = page.findDataBloackIndexByKey(ProviderSection.IDENTIFIERS, "Type",
					"Common Provider NUMBER (CPN)");
			String msg = page.ceaseDataBlockWithError(ProviderSection.IDENTIFIERS, index);
			assertTrue(msg.contains("GRS.DPS.UNK.UPD.1.0.7050: You don't have access to update this record"));
		}
		if(UserType.CONSUMER.equals(userType)) {
			// CONSUMER has no permission to update/cease identifier
			for (int i = 0; i < count - 1; i++)
				assertFalse(page.isDataBlockUpdateButtonDisplayed(ProviderSection.IDENTIFIERS, i));
		}
		//workflow.logout();
		workflowAdm = PlrWebWorkflow.create(UserType.ADMIN);
		workflowAdm.login().openPlr();
		actions = workflowAdm.getUpdateProviderActions();
		page = actions.openProvider(pauthId);
		page.ceaseAllDataBlockUnderSection(ProviderSection.IDENTIFIERS);
		count = page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
		assertTrue(count == 0);
		workflowAdm.logout();
		workflowAdm.close();
		workflowManager_.logoutAllAndClose();
	}

	// Then Logical Deletion of Providers
	// step 5-7: delete CPN from Registry identifier and Identifier block and search
	// with CPN and check
	// Step 6 is not applicable
	@Test(dataProvider = "providerTestUserTypes", dataProviderClass = InjectableData.class, groups = {
			"UpdateProviderLegacy" })
	public void testLogicalDeletionProviders(UserType userType) {
		// errorCeaselastActiveName
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainIndividualBuilder defaultTestBC = fhirController
				.createIndividual(new IndividualMaintainConfig(IndividualRoleType.DEN).withNotes(1));
		MaintainOrgBuilder defaultTestOrg = fhirController.createOrganization(OrgRoleType.ORG);
		fhirController.close();
		//
		// PLR login
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, userType);
		UpdateProviderActions actions = workflow.getUpdateProviderActions();
		String identifier = defaultTestBC.getIdentifier(IdentifierType.IPC);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);
		String cpnString = defaultTestBC.getIdentifier(IdentifierType.CPN);
		// name for user type admin
		if (UserType.ADMIN.equals(userType)) {
			page.ceaseAllDataBlockUnderSection(ProviderSection.NAMES);
		}
		// cease CPN from id and reg id section
		if (UserType.ADMIN.equals(userType)) {
			int index = page.findDataBloackIndexByKey(ProviderSection.IDENTIFIERS, "Type", "Common Provider NUMBER (CPN)");
			page.ceaseDataBlock(ProviderSection.IDENTIFIERS, index);
			index = page.findDataBloackIndexByKey(ProviderSection.REGISTRY_IDENTIFIERS, "Type",
				"Common Provider NUMBER (CPN)");
			page.ceaseDataBlock(ProviderSection.REGISTRY_IDENTIFIERS, index);
		}else {
			// non-admin user type cannot see the reg id
			int count=page.grabDataBlockCount(ProviderSection.REGISTRY_IDENTIFIERS);
			assertTrue(count==0);
		}
		// search by CPN and return zero result--provider
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		SearchProviderResultsFragment searchResults = searchProviderPage
				.searchByRegistryIdentifier(IdentifierType.CPN.name(), pauthId);
		if (UserType.ADMIN.equals(userType))assertTrue(searchResults.grabResultsRowCount() == 0);
		else assertTrue(searchResults.grabResultsRowCount() > 0);
		// ORG
		cpnString = defaultTestOrg.getIdentifier(IdentifierType.CPN);
		identifier = defaultTestOrg.getIdentifier(IdentifierType.IPC);
		pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		page = actions.openProvider(pauthId);
		if (UserType.ADMIN.equals(userType)) {
			page.ceaseAllDataBlockUnderSection(ProviderSection.ORGANIZATION_NAMES);
		}
		if (UserType.ADMIN.equals(userType)) {
			int index = page.findDataBloackIndexByKey(ProviderSection.IDENTIFIERS, "Type", "Common Provider NUMBER (CPN)");
			page.ceaseDataBlock(ProviderSection.IDENTIFIERS, index);
			index = page.findDataBloackIndexByKey(ProviderSection.REGISTRY_IDENTIFIERS, "Type",
				"Common Provider NUMBER (CPN)");
			page.ceaseDataBlock(ProviderSection.REGISTRY_IDENTIFIERS, index);
		}else {
			// non-admin user type cannot see the reg id
			int count=page.grabDataBlockCount(ProviderSection.REGISTRY_IDENTIFIERS);
			assertTrue(count==0);
		}
		// search by CPN and return zero result--org
		searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		searchResults = searchProviderPage.searchByRegistryIdentifier(IdentifierType.CPN.name(), pauthId);
		if (UserType.ADMIN.equals(userType))assertTrue(searchResults.grabResultsRowCount() == 0);
		else assertTrue(searchResults.grabResultsRowCount() > 0);
		workflowManager_.logoutAndClose(userType);

	}

//
//			Then Reactivating Logically Deleted Providers -- not applicable
//
//			Then Registry Identifiers UI Suffix Is Implied-- saearch, i,mplemented at 'SearchProviderTests
//
//			Then Update Identifiers
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testUpdateIdentifiers(ProviderType providerType) {

		PlrWebWorkflow workflow=TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);

		// update
		String providerId = getTestProviderId(providerType);
		int index = page.findDataBloackIndexByKey(ProviderSection.IDENTIFIERS, "Identifier", providerId);
		String newProviderId = UpdateSimpleHelper.generateNumericString(10);
		String msg = page.updateIdentifiersDataBlock(newProviderId, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		LinkedHashMap<String, String> content = page.grabDataBlockContent(ProviderSection.IDENTIFIERS, index);
		assertEquals(newProviderId, content.get("Identifier"));
		// cancel update
		String newProviderIdCancel = UpdateSimpleHelper.generateNumericString(10);
		page.updateIdentifierDataBlockCancel(newProviderIdCancel, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		content = page.grabDataBlockContent(ProviderSection.IDENTIFIERS, index);
		assertEquals(newProviderId, content.get("Identifier"));

	}

//
//			Then Update Notes
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testUpdateNotes(ProviderType providerType) {

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);

		 int count=page.grabActiveDataBlockCount(ProviderSection.NOTES, true);
		 if(count==0) {
			 page.addNoteDataBlock(null, "Test Note", UpdateSimpleHelper.effective_date(),
					 UpdateSimpleHelper.increment_year_for_effective_date(), false);
		 }

		String noteText = "NoteText:" + UpdateSimpleHelper.generateAlphabetString(10);
		String msg = page.updateNoteDataBlock(noteText, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, 0, false);
		LinkedHashMap<String, String> content = page.grabDataBlockContent(ProviderSection.NOTES, 0);
		assertEquals(noteText, content.get("Note Text"));

		String noteTextnew = "NoteTextUpdate:" + UpdateSimpleHelper.generateAlphabetString(10);
		// int index=page.findDataBloackIndexByKey(ProviderSection.NOTES,"Note
		// Identifier",noteId);
		msg = page.updateNoteDataBlockCancel(noteTextnew, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, 0, false);
		content = page.grabDataBlockContent(ProviderSection.NOTES, 0);
		assertEquals(noteText, content.get("Note Text"));
		assertTrue(!noteTextnew.equals(content.get("Note Text")));

	}

//
//			Then Update Registry Identifier
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testUpdateRegistryIdentifier(ProviderType providerType) {

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);

		// update
		int index = page.findDataBloackIndexByKey(ProviderSection.REGISTRY_IDENTIFIERS, "Identifier", identifier);
		String newProviderId = UpdateSimpleHelper.generateNumericString(10);
		String msg = page.updateRegIdentifiersDataBlock(newProviderId, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		LinkedHashMap<String, String> content = page.grabDataBlockContent(ProviderSection.REGISTRY_IDENTIFIERS, index);
		assertEquals("IPC."+newProviderId+".BC.PRS", content.get("Identifier"));
		// cancel update
		String newProviderIdCancel = UpdateSimpleHelper.generateNumericString(10);
		page.updateRegIdentifiersDataBlockCancel(newProviderIdCancel, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		content = page.grabDataBlockContent(ProviderSection.REGISTRY_IDENTIFIERS, index);
		assertEquals("IPC."+newProviderId+".BC.PRS", content.get("Identifier"));

		//restore updated IPC
		msg = page.updateRegIdentifiersDataBlock(pauthId, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CORR, index, false);
	}

//
//			Then Update Statuses
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testUpdateStatuses(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);

		// update
		int index = 0;
		String msg = page.updateStatusDataBlock("ACTIVE", "GS", UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		assertTrue(StringUtils.isEmpty(msg));

		// cancel update
		msg = page.updateStatusDataBlockCancel("ACTIVE", "UNK", UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		LinkedHashMap<String, String> content = page.grabDataBlockContent(ProviderSection.STATUSES, index);
		assertTrue(content.get("Reason").contains("GS"));
	}

//
//			Then Validate Provider Identifiers
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateProviderIdentifiers(ProviderType providerType) {
		String errorMsg01 = errorList.getString("foreignCharacterIdentifier");
		String errorMsg02 = errorList.getString("missingBCProviderIdentifier");

		String foreignID = "δ00000001";
		String nonnumericID = "TestPRS!@$";
		String nonalphaID = "*0000001";
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);

		// update with foreign char, non-alpha char,blank id
		String providerId = getTestProviderId(providerType);
		int index = page.findDataBloackIndexByKey(ProviderSection.IDENTIFIERS, "Identifier", providerId);
		
		String msg = page.updateIdentifiersDataBlock(foreignID, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg01));
		msg = page.updateIdentifiersDataBlock(nonnumericID, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg01));
		msg = page.updateIdentifiersDataBlock(nonalphaID, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg01));
		msg = page.updateIdentifiersDataBlock(null, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg02));
	}

//Then Validate Provider Identifiers For Update
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateProviderIdentifiersForUpdate(ProviderType providerType) {
		String errorMsg01 = errorList.getString("invalidDateFormatEffectiveFrom");
		String errorMsg02 = errorList.getString("missingBCProviderIdentifier");
		String errorMsg03 = errorList.getString("erromMessageGRS5000EffectiveFrom");
		String errorMsg04 = errorList.getString("errMsg5000EndReason");

		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		String idString = UpdateSimpleHelper.generateNumericString(8);
		UpdateProviderPage page = actions.openProvider(pauthId);

		String providerId = getTestProviderId(providerType);
		int index = page.findDataBloackIndexByKey(ProviderSection.IDENTIFIERS, "Identifier", providerId);
		String newProviderId = UpdateSimpleHelper.generateNumericString(10);
		String msg = page.updateIdentifiersDataBlock(newProviderId, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		assertTrue(StringUtils.isEmpty(msg));
		// invalid effective from
		msg = page.updateIdentifiersDataBlock(newProviderId, "2026.03.24th",
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg01));
		// blank id
		msg = page.updateIdentifiersDataBlock(null, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg02));
		// blank effective from
		String newProviderIdCancel = UpdateSimpleHelper.generateNumericString(10);
		msg = page.updateIdentifiersDataBlock(newProviderIdCancel, null,
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg03));
		// null end reason
		msg = page.updateIdentifiersDataBlock(newProviderIdCancel, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), null, index, true);
		assertTrue(msg.equals(errorMsg04));
	}
	
	
//	======================PLR 608-02============================
//			Then Update Electronic Addresses
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testUpdateElectronicAddresses(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);

		int count=page.grabActiveDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES, true);
		 if(count==0) {
			 page.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),"BC","test@test.com",  UpdateSimpleHelper.effective_date(),
					 UpdateSimpleHelper.increment_year_for_effective_date(), false);
		 }
		 
		// update
		int index = 0;
		String emailAddrss=UpdateSimpleHelper.generateAlphabetNumericString(6)+"@test.com";
		String msg = page.updateElectronicAddressDataBlock(emailAddrss, UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		assertTrue(StringUtils.isEmpty(msg));

		// cancel update
		page.updateElectronicAddressDataBlockCancel("newtest@test.com", UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		LinkedHashMap<String, String> content = page.grabDataBlockContent(ProviderSection.ELECTRONIC_ADDRESSES, index);
        assertEquals(emailAddrss, content.get("Address"));
	}
//			Then Update Telecommunicatons
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testUpdateTelecommunicatons(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);

		int count=page.grabActiveDataBlockCount(ProviderSection.TELECOMMUNICATIONS, true);
		 if(count==0) {
			 page.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),"BC",
					 UpdateSimpleHelper.generateNumericString(3),UpdateSimpleHelper.generateNumericString(7),UpdateSimpleHelper.generateNumericString(4),
					 UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date(), false);
		 }
		 
		// update
		int index = 0;
		String phoneNumber=UpdateSimpleHelper.generateNumericString(7);
		String areaCode=UpdateSimpleHelper.generateNumericString(3);
		String msg = page.updateTelecommunicationDataBlock(areaCode, phoneNumber,null,
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		assertTrue(StringUtils.isEmpty(msg));

		// cancel update
		String phoneNumberCancel=UpdateSimpleHelper.generateNumericString(7);
		String areaCodeCancel=UpdateSimpleHelper.generateNumericString(3);
		page.updateTelecommunicationDataBlockCancel(areaCodeCancel, phoneNumberCancel,null,
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		
		LinkedHashMap<String, String> content = page.grabDataBlockContent(ProviderSection.TELECOMMUNICATIONS, index);
        assertEquals(phoneNumber, content.get("Number"));
        assertEquals(areaCode, content.get("Area Code"));
	}
//			Then Validate Communication Purpose Type code
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateCommunicationPurposeTypeCode(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);

		page.addWorkLocationDataBlock("1", false, "Work Location", "CC", "Info", false);

		page.clickWLHeaderAddButton(ProviderSection.ADDRESSES, 0);
	}
//
//			Then Validate Electronic Address Txt
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateElectronicAddressTxt(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);
		
		int maxElectronicAddressesFieldLength=500;
		String errorMsg01=errorList.getString("errMsg5003ElectronicAddress");
		String errorMsg02=errorList.getString("errMsg5000EAddress");
		String errorMsg03="The following business rules have failed: Invalid email address format.";
		String invalidEmail="test:http:test.com";
		String emailStr=UpdateSimpleHelper.generateEmail();
		String emailStr01=UpdateSimpleHelper.generateAlphabetString(maxElectronicAddressesFieldLength-emailStr.length()+1) +emailStr;

		
		String msg=page.updateElectronicAddressDataBlock(emailStr01, 
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date()
				, EndReason.CHG, 0, true);
        assertEquals(errorMsg01, msg);
		
		msg=page.updateElectronicAddressDataBlock(null, 
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date()
				, EndReason.CHG, 0, true);
        assertEquals(errorMsg02, msg);
		
		page.updateElectronicAddressDataBlock(invalidEmail,
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date()
				, EndReason.CHG, 0, false);
		// Email was not updated while no any error message displayed
		//assertTrue(msg.equals(errorMsg03));//no expe
		
		msg=page.updateElectronicAddressDataBlock("test.test@test.com", 
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date()
				, EndReason.CHG, 0, false);
		assertTrue(StringUtils.isEmpty(msg));
		
		emailStr01=UpdateSimpleHelper.generateAlphabetString(maxElectronicAddressesFieldLength-emailStr.length()) +emailStr;
		msg=page.updateElectronicAddressDataBlock(emailStr01, 
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date()
				, EndReason.CHG, 0, false);
		assertTrue(StringUtils.isEmpty(msg));
	}
//
//			Then Validate Electronic Address Type Code
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateElectronicAddressTypeCode(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);
	
		String typeCode=page.grabUpdateDialogLabelText(ProviderSection.ELECTRONIC_ADDRESSES,0,"Type");
		assertFalse(page.isUpdateDialogSpanEditable(ProviderSection.ELECTRONIC_ADDRESSES,0,typeCode));
		
		List<String> options = Stream.of(ElectronicAddressType.values())
			    .map(ElectronicAddressType::getText) 
			    .toList();
		assertTrue(options.contains(typeCode));
		
		
	}
//			Then Validate Telcommunication Number
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateTelcommunicationNumber(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);
		
		int maxExtension=15, maxPhonenumber=30, maxArecode=15;
		String errorMsg01=errorList.getString("errMsg5003TelecomExtension");
		String errorMsg02=errorList.getString("errMsg5003TelecomPhoneNumber");
		String errorMsg03=errorList.getString("errMsg5003TelecomAreaCode");
		
		int index=0;
		String msg = page.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(maxArecode),
				UpdateSimpleHelper.generateNumericString(maxPhonenumber),
				UpdateSimpleHelper.generateNumericString(maxExtension+1),
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
        assertEquals(errorMsg01, msg);
		msg = page.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(maxArecode),
				UpdateSimpleHelper.generateNumericString(maxPhonenumber+1),
				UpdateSimpleHelper.generateNumericString(maxExtension),
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
        assertEquals(errorMsg02, msg);
		msg = page.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(maxArecode+1),
				UpdateSimpleHelper.generateNumericString(maxPhonenumber),
				UpdateSimpleHelper.generateNumericString(maxExtension),
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
        assertEquals(errorMsg03, msg);
		msg = page.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(maxArecode),
				UpdateSimpleHelper.generateNumericString(maxPhonenumber),
				UpdateSimpleHelper.generateNumericString(maxExtension),
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		assertTrue(StringUtils.isEmpty(msg));
	}
//			Then Validate Telecommunication Type Code
	@Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testValidateTelecommunicationTypeCode(ProviderType providerType) {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = getTestProvideridentifier(providerType);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);
	
		String typeCode=page.grabUpdateDialogLabelText(ProviderSection.TELECOMMUNICATIONS,0,"Type");
		assertFalse(page.isUpdateDialogSpanEditable(ProviderSection.TELECOMMUNICATIONS,0,typeCode));
		
		List<String> options = Stream.of(TelecommunicationType.values())
			    .map(TelecommunicationType::getText) 
			    .toList();
		assertTrue(options.contains(typeCode));
	}
//=========PLR 602==========
	//Validate Organization name
	//this is message based test case, step 6 not applicable
	//max org name is 100 (not 50), max org long name is 200 (not 150)
	@Test( groups = {"UpdateProviderLegacy" })
	public void testValidateOrganizationName() {
		PlrWebWorkflow workflow = TestHelper.logIn(workflowManager_, UserType.ADMIN);
		UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
		String identifier = identifier = defaultOrg.getIdentifier(IdentifierType.IPC);
		String pauthId = UpdateSimpleHelper.getRegIdString("IPC", identifier);
		UpdateProviderPage page = actions.openProvider(pauthId);
	
		String errorMsg01=errorList.getString("errorMsgName");
		String errorMsg02=errorList.getString("errorMaxOrgNameLength");
		String errorMsg03=errorList.getString("errorMaxOrgLongNameLength");
		int maxOrgName=100,maxOrgdesc=200;
		int index=0;
		
		String msg = page.updateOrganizationalNameDataBlock(null,
				UpdateSimpleHelper.generateAlphabetString(10),
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg01));
		msg = page.updateOrganizationalNameDataBlock(UpdateSimpleHelper.generateAlphabetString(maxOrgName+1),
				UpdateSimpleHelper.generateAlphabetString(maxOrgdesc),
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg02));
		msg = page.updateOrganizationalNameDataBlock(UpdateSimpleHelper.generateAlphabetString(maxOrgName),
				UpdateSimpleHelper.generateAlphabetString(maxOrgdesc+1),
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, true);
		assertTrue(msg.equals(errorMsg03));
		msg = page.updateOrganizationalNameDataBlock(UpdateSimpleHelper.generateAlphabetString(maxOrgName),
				UpdateSimpleHelper.generateAlphabetString(maxOrgdesc),
				UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, index, false);
		assertTrue(StringUtils.isEmpty(msg));
		
	}
//Organization Provider Minimum Data Requirements:
/*
 * this is test case for message based update, and minimal set "
Provider type (org or ind) 
Provider role type 
Jurisdiction 
Provider Identifier Type 
Provider Identifier 
End Reason Code for each data object changed 
Effective Start Date for each data object changed" is not applicable to web update;
web update identifier is covered in update ticket (PLR 608 and PLR 596);
	
 */
	
}
