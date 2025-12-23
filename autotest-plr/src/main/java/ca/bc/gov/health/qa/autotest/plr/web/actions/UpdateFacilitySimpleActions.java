package ca.bc.gov.health.qa.autotest.plr.web.actions;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.CivicAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.ElectronicAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Identifier;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Name;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Note;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.OtherAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Relationship;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Telecommunication;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.AutocompleteMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.RelatedProviderIdentifierType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ElectronicAddressType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.IdentifierTypeName;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.RelationshipType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.TelecommunicationType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.json.JSONObject;
import org.openqa.selenium.By;

import static org.testng.Assert.*;

public class UpdateFacilitySimpleActions {
	private static final Logger LOG = ExecutionLogManager.getLogger();
	private final SeleniumSession selenium_;
	private final URI uri_;
	private final UserType userType_;

	private static final Config config_ = ConfigProvider.get().getConfig();
	private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
	private static JSONObject errorList;
	private static JSONObject warningList;

	private static final String ID_SPECIAL_CHAR = "IFC.0012479#.BC.PRS";
	private static final String ID_FOREIGN_CHAR = "IFC.0012479À.BC.PRS";

	/**
	 * Initializes class and SeleniumSession.
	 *
	 * @param selenium
	 * @param uri
	 * @param userType
	 */
	public UpdateFacilitySimpleActions(SeleniumSession selenium, URI uri, UserType userType) {
		selenium_ = selenium;
		uri_ = uri;
		userType_ = userType;

		try {
			errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
			warningList = new JSONObject(Files.readString(errorPath)).getJSONObject("warnings");
		} catch (IOException e) {
			String msg = String.format("Failed to read JSON data (%s).", errorPath);
			throw new IllegalStateException(msg, e);
		}
	}
	/**
	 * Gets the selenium_ value.
	 *
	 * @return the selenium_
	 */
	public SeleniumSession getSelenium_() {
		return selenium_;
	}

	/**
	 * Gets the uri_ value.
	 *
	 * @return the uri_
	 */
	public URI getUri_() {
		return uri_;
	}

	/**
	 * Gets the userType_ value.
	 *
	 * @return the userType_
	 */
	public UserType getUserType_() {
		return userType_;
	}

	/**
	 * Opens an organization (View Provider) page from a Facility to Organization Relationship block
	 * Update operations ar performed on view organization(provider) page
	 *
	 * @param facility
	 * @return UpdateFacilityPagea reference to the opened organization page
	 */
	public UpdateFacilityPage openFacility(MaintainFacilityBuilder facility) {
		String fauthId = UpdateSimpleHelper.getFaultId(facility.getIdentifier());
		LOG.info("Open facility({}).", fauthId);
		UpdateFacilityPage updateFacilityPage = new UpdateFacilityPage(selenium_,
				uri_.resolve("plr/FacilityDetails.xhtml"));
		updateFacilityPage.openFacility(fauthId);
		updateFacilityPage.waitForReady();
		return updateFacilityPage;

	}

	/**
	 * validate Facility Identifiers
	 *
	 * @param updatePage
	 */
	public void validateFacilityIdentifiers(UpdateFacilityPage updatePage) {
		String errMsg = "";

		errMsg = updatePage.addIdentifierDataBlock("", "IFC." + UpdateSimpleHelper.generateNumericString(8) + ".BC.PRS",
				UpdateSimpleHelper.effective_date(), "", true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")
				&& errMsg.contains("The following fields must be supplied: 'Identifier Type'."));

		errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(), "",
				UpdateSimpleHelper.effective_date(), "", true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")
				&& errMsg.contains("The following fields must be supplied: 'Identifier'."));

		errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(), ID_FOREIGN_CHAR,
				UpdateSimpleHelper.effective_date(), "", true);
		assertTrue(errMsg.contains("GRS.SYS.IDE.UNK.1.0.7006")
				&& errMsg.contains("Identifiers can contain only numbers, the English alphabet and periods"));

		errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(), ID_SPECIAL_CHAR,
				UpdateSimpleHelper.effective_date(), "", true);
		assertTrue(errMsg.contains("GRS.SYS.IDE.UNK.1.0.7006")
				&& errMsg.contains("Identifiers can contain only numbers, the English alphabet and periods"));

		errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),
				"IFC." + UpdateSimpleHelper.generateNumericString(10) + ".BC.PRS", UpdateSimpleHelper.effective_date(),
				"", true);
		// assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") &&
		// errMsg.contains("The following fields must be supplied: ''Identifier''."));

		errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),
				"IFC." + UpdateSimpleHelper.generateNumericString(8) + ".BC.PRS", UpdateSimpleHelper.effective_date(),
				"", true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.7033")
				&& errMsg.contains("Cannot create duplicate record. Check the following field: Facility Identifier"));

	}

	/**
	 * validate Facility Name
	 *
	 * @param updatePage
	 */
	public void validateFacilityName(UpdateFacilityPage updatePage, EndReason endReason) {
		String errMsg = "";
		int maxName = 100;
		int index = 0;
		if (updatePage.grabActiveDataBlockCount(FacilitySection.NAMES, true) > 0)
			updatePage.ceaseDataBlock(FacilitySection.NAMES, 0);

		errMsg = updatePage.addNameDataBlock("NAME-" + UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-" + UpdateSimpleHelper.generateAlphabetString(5), "", "");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")
				&& errMsg.contains("The following fields must be supplied: 'Effective From'."));

		errMsg = updatePage.addNameDataBlock("NAME-" + UpdateSimpleHelper.generateAlphabetString(maxName),
				"DESC-" + UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003")
				&& errMsg.contains("'Facility Name' length must be between 0 and 100"));

		errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(maxName),
				"DESC-" + UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "");
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateNameDataBlock("NAME-" + UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-" + UpdateSimpleHelper.generateAlphabetString(5), "", "", endReason, index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")
				&& errMsg.contains("The following fields must be supplied: 'Effective From'."));
		errMsg = updatePage.updateNameDataBlock("NAME-" + UpdateSimpleHelper.generateAlphabetString(maxName),
				"DESC-" + UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "",
				endReason, index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003")
				&& errMsg.contains("'Facility Name' length must be between 0 and 100"));
		errMsg = updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(maxName),
				"DESC-" + UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "",
				endReason, index);
		assertTrue(StringUtils.isEmpty(errMsg));
	}

	/**
	 * validate Facility Description
	 *
	 * @param updatePage
	 */
	public void validateFacilityDescription(UpdateFacilityPage updatePage, EndReason endReason) {
		String errMsg = "";
		int maxDesc = 200;
		int index = 0;
		if (updatePage.grabActiveDataBlockCount(FacilitySection.NAMES, true) > 0)
			updatePage.ceaseDataBlock(FacilitySection.NAMES, 0);

		errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-" + UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "");
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5), "",
				UpdateSimpleHelper.effective_date(), "", endReason, index);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.NAMES, 0);

		errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5), "",
				UpdateSimpleHelper.effective_date(), "");
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.NAMES, 0);

		errMsg = updatePage.addNameDataBlock("", "DESC-" + UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(), "");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")
				&& errMsg.contains(" The following fields must be supplied: 'Name'"));

		errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-" + UpdateSimpleHelper.generateAlphabetString(5), "", "");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")
				&& errMsg.contains("The following fields must be supplied: 'Effective From'."));

		errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-" + UpdateSimpleHelper.generateAlphabetString(maxDesc), UpdateSimpleHelper.effective_date(), "");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003")
				&& errMsg.contains("'Facility Description' length must be between 0 and 200"));

		errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(maxDesc), UpdateSimpleHelper.effective_date(), "");
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateNameDataBlock("", UpdateSimpleHelper.generateAlphabetString(10),
				UpdateSimpleHelper.effective_date(), "", endReason, index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")
				&& errMsg.contains(" The following fields must be supplied: 'Name'"));

		errMsg = updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(10), "", "", endReason, index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")
				&& errMsg.contains("The following fields must be supplied: 'Effective From'."));

		errMsg = updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(maxDesc + 1), UpdateSimpleHelper.effective_date(), "",
				endReason, index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003")
				&& errMsg.contains("'Facility Description' length must be between 0 and 200"));

		errMsg = updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(maxDesc), UpdateSimpleHelper.effective_date(), "", endReason,
				index);
		assertTrue(StringUtils.isEmpty(errMsg));

	}

	/**
	 * validate Facility Mailing AddressType
	 *
	 * @param updatePage
	 */
	public void validateFacilityMailingAddressType(UpdateFacilityPage updatePage) {
		String expectType = "Physical location (P)";
		LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.OTHER_ADDRESS, 0);
		OtherAddress oAddrResult = new OtherAddress(resultContent);
		assertEquals(oAddrResult.getAddressType(), expectType);
		boolean is = updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0);
		assertFalse(is);

	}

	/**
	 * validate Facility Mailing Address Type
	 *
	 * @param updatePage
	 */
	public void validateFacilityMailingAddressPurpose(UpdateFacilityPage updatePage) {
		String expectPurpose = "Facility Contact (FC)";
		LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.OTHER_ADDRESS, 0);
		OtherAddress oAddrResult = new OtherAddress(resultContent);
		assertEquals(oAddrResult.getAddressPurpose(), expectPurpose);
		boolean is = updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0);
		assertFalse(is);

	}

	/**
	 * validate Facility Data Block Multiplicity
	 *
	 * @param updatePage
	 * @param ID_ORG01
	 * @param ID_ORG02
	 */
	public void validateFacilityDataBlockMultiplicity(UpdateFacilityPage updatePage, String ID_ORG01, String ID_ORG02) {
		String errMsg = "";
		String errMsg2201Tel = errorList.getString("errMsg2201Tel");
		String errMsg2201EAddr = errorList.getString("errMsg2201EAddr");
		String errMsg7033Dup = errorList.getString("errMsg7033Dup");
		String errMsg7033DupNote = errorList.getString("errMsg7033DupNote");

		errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),
				"IFC." + UpdateSimpleHelper.generateNumericString(8) + ".BC.PRS", UpdateSimpleHelper.effective_date(),
				"", true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.7033")
				&& errMsg.contains("Cannot create duplicate record. Check the following field: Facility Identifier"));
		// name

		if (updatePage.grabActiveDataBlockCount(FacilitySection.NAMES, true) > 0)
			updatePage.ceaseDataBlock(FacilitySection.NAMES, 0);

		errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "");
		assertTrue(StringUtils.isEmpty(errMsg));

		/*
		 * After name is added, the 'add' button on header is disappeared. this step is
		 * not applicable.
		 * errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(
		 * 5), UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.
		 * effective_date(),"");
		 */

		updatePage.ceaseDataBlock(FacilitySection.NAMES, 0);

		errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "");
		assertTrue(StringUtils.isEmpty(errMsg));

		// telecom
		int count = updatePage.grabActiveDataBlockCount(FacilitySection.TELECOMMUNICATIONS, true);
		for (int i = 0; i < count; i++) {
			updatePage.ceaseDataBlock(FacilitySection.TELECOMMUNICATIONS, 0);
		}

		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMsg2201Tel);

		ceaseTelecommunicationByType(updatePage);

		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.FAX.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7), "",
				UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		// e-address
		count = updatePage.grabActiveDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES, true);
		for (int i = 0; i < count; i++) {
			updatePage.ceaseDataBlock(FacilitySection.ELECTRONIC_ADDRESSES, 0);
		}

		errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),
				UpdateSimpleHelper.generateEmail(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),
				UpdateSimpleHelper.generateEmail(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMsg2201EAddr);

		ceaseElectronicAddressByType(updatePage);

		errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),
				UpdateSimpleHelper.generateEmail(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.HTTP.getText(),
				UpdateSimpleHelper.generateHTTP(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		// note
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.NOTES);

		String noteId = UpdateSimpleHelper.generateAlphabetString(5);
		errMsg = updatePage.addNoteDataBlock(noteId, UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addNoteDataBlock(noteId, UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMsg7033DupNote);

		ceaseNoteDataBlockById(updatePage, noteId);

		errMsg = updatePage.addNoteDataBlock(noteId, UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		String noteId02 = UpdateSimpleHelper.generateAlphabetString(5);
		errMsg = updatePage.addNoteDataBlock(noteId02, UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		// relationship
		count = updatePage.grabActiveDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS, true);
		for (int i = 0; i < count; i++) {
			updatePage.ceaseDataBlock(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0);
			updatePage.waitSeconds(5);
		}

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), ID_ORG01,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), ID_ORG01,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMsg7033Dup);

		ceaseRelatedOrganizationDataBlockByRelatedId(updatePage, ID_ORG01);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), ID_ORG01,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), ID_ORG01,
				RelationshipType.LOCATED.getText(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), ID_ORG02,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
	}

	/**
	 * cease Note Data Block By Id
	 *
	 * @param updatePage
	 * @param noteId
	 */
	private void ceaseNoteDataBlockById(UpdateFacilityPage updatePage, String noteId) {
		int index = updatePage.grabActiveDataBlockCount(FacilitySection.NOTES, true);

		for (int i = 0; i < index; i++) {
			LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.NOTES, i);
			Note result = new Note(resultContent);
			if (result.getNoteIdentifier().equals(noteId)) {
				updatePage.ceaseDataBlock(FacilitySection.NOTES, i);

			}

		}

	}

	/**
	 * cease Related Organization Data Block By RelatedId
	 *
	 * @param updatePage
	 * @param key
	 */
	private void ceaseRelatedOrganizationDataBlockByRelatedId(UpdateFacilityPage updatePage, String key) {

		int index = updatePage.grabActiveDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS, true);

		for (int i = 0; i < index; i++) {
			LinkedHashMap<String, String> resultContent = updatePage.grabOrgRelationshipsBlockContent(i);
			Relationship result = new Relationship(resultContent);
			if (result.getRelatedOrganizationIdentifier().equals(key)) {
				updatePage.ceaseDataBlock(FacilitySection.ORGANIZATION_RELATIONSHIPS, i);

			}

		}

	}

	/**
	 * cease Electronic Address By Type
	 *
	 * @param updatePage
	 */
	private void ceaseElectronicAddressByType(UpdateFacilityPage updatePage) {
		String key = "Email ";
		int index = updatePage.grabActiveDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES, true);
		for (int i = 0; i < index; i++) {
			LinkedHashMap<String, String> resultContent = updatePage
					.grabDataBlockContent(FacilitySection.ELECTRONIC_ADDRESSES, i);
			ElectronicAddress result = new ElectronicAddress(resultContent);
			if (result.getType().contains(key)) {
				updatePage.ceaseDataBlock(FacilitySection.ELECTRONIC_ADDRESSES, i);
				break;
			}

		}

	}

	/**
	 * cease Telecommunication By Type
	 *
	 * @param updatePage
	 */
	private void ceaseTelecommunicationByType(UpdateFacilityPage updatePage) {
		String key = "Telephone";
		int index = updatePage.grabActiveDataBlockCount(FacilitySection.TELECOMMUNICATIONS, true);
		for (int i = 0; i < index; i++) {
			LinkedHashMap<String, String> resultContent = updatePage
					.grabDataBlockContent(FacilitySection.TELECOMMUNICATIONS, i);
			Telecommunication result = new Telecommunication(resultContent);
			if (result.getType().contains(key)) {
				updatePage.ceaseDataBlock(FacilitySection.TELECOMMUNICATIONS, i);
				break;
			}

		}

	}

	/**
	 * validate Facility Notes Texts
	 *
	 * @param updatePage
	 */
	public void validateFacilityNotesTexts(UpdateFacilityPage updatePage, EndReason endReason) {
		String errMsg = "";
		int maxNoteId = 30;
		int maxNoteText = 255;
		int index = 0;
		String errMessage5003NoteIdentifier = errorList.getString("errMsg5003NoteIdentifier");
		String errMessage5000 = errorList.getString("errMsg5000NoteText");
		String errMessage5003NoteText = errorList.getString("errMsg5003NoteText");

		index = updatePage.grabActiveDataBlockCount(FacilitySection.NOTES, true);

		errMsg = updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(maxNoteId + 1),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMessage5003NoteIdentifier);

		errMsg = updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(maxNoteId),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		index++;

		errMsg = updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(6), "",
				UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMessage5000);

		errMsg = updatePage.updateNoteDataBlock("", UpdateSimpleHelper.effective_date(), "", endReason, index - 1,
				true);
		assertEquals(errMsg, errMessage5000);

		errMsg = updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(3),
				UpdateSimpleHelper.generateAlphabetString(maxNoteText + 1), UpdateSimpleHelper.effective_date(), "",
				true);
		assertEquals(errMsg, errMessage5003NoteText);

		errMsg = updatePage.updateNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(maxNoteText + 1),
				UpdateSimpleHelper.effective_date(), "", endReason, index - 1, true);
		assertEquals(errMsg, errMessage5003NoteText);

		errMsg = updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(maxNoteText), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(maxNoteText), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		index++;

		errMsg = updatePage.updateNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(maxNoteText),
				UpdateSimpleHelper.effective_date(), "", endReason, index - 1, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		// cease test data

	}

	/**
	 * validate Facility Notes Texts
	 *
	 * @param updatePage
	 * @param ID_ORG
	 */
	public void ValidateRelatedOrganizationID(UpdateFacilityPage updatePage, String ID_ORG) {
		String errMsg = "";
		int maxRelatedId = 50;

		String ID_SPECIAL_CHAR = "IPC.0012479#.BC.PRS";
		String ID_NONEXIST = "IPC.00999999.BC.PRS";
		String ID_PERSON = "IPC.00124841.BC.PRS";

		String errMessage5000 = errorList.getString("errMsg5000RelatedProviderIdentifier");
		String errMessage5003 = errorList.getString("errMsg5003ProviderIdentifier");
		String errMessage7036 = errorList.getString("errMsg7036");
		String errMessage9026 = errorList.getString("errMsg9026");

		int count = updatePage.grabActiveDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS, true);
		for (int i = 0; i < count; i++) {
			updatePage.ceaseDataBlock(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0);
			updatePage.waitSeconds(5);
		}

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), "",
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMessage5000);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
				UpdateSimpleHelper.generateAlphabetNumericString(maxRelatedId + 1), RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMessage5003);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
				ID_SPECIAL_CHAR, RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMessage7036);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), ID_NONEXIST,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMessage7036);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), ID_PERSON,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMessage9026);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), ID_ORG,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
	}

	/**
	 * validate Updating Facility
	 *
	 * @param updatePage
	 * @param endReason
	 */
	public void validateUpdatingFacility(UpdateFacilityPage updatePage, EndReason endReason) {
		String errMsg = "";
		// identifier- Not able to be updated
		errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),
				"IFC." + UpdateSimpleHelper.generateNumericString(8) + ".BC.PRS", UpdateSimpleHelper.effective_date(),
				"", true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.7033")
				&& errMsg.contains("Cannot create duplicate record. Check the following field: Facility Identifier"));
		// name - cease, add, update, cease

		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.NAMES);

		errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "");
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "", endReason, 0);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.NAMES, 0);
		// telecom- cease, add, update, cease
		// telecom type- Phone, Pager,Fax, Mobile, Modem
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.TELECOMMUNICATIONS);

		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.TELECOMMUNICATIONS, 0);

		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PAGER.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.TELECOMMUNICATIONS, 0);

		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.FAX.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.TELECOMMUNICATIONS, 0);

		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.MOBILE.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.TELECOMMUNICATIONS, 0);

		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.MODEM.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.TELECOMMUNICATIONS, 0);

		// e-address - cease, add, update, cease
		// e-address type - EMail, HTTP, FTP
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.ELECTRONIC_ADDRESSES);

		errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),
				UpdateSimpleHelper.generateEmail(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		errMsg = updatePage.updateElectronicAddressDataBlock(UpdateSimpleHelper.generateEmail(),
				UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.ELECTRONIC_ADDRESSES, 0);

		errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.FTP.getText(),
				UpdateSimpleHelper.generateFTP(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		errMsg = updatePage.updateElectronicAddressDataBlock(UpdateSimpleHelper.generateFTP(),
				UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.ELECTRONIC_ADDRESSES, 0);

		errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.HTTP.getText(),
				UpdateSimpleHelper.generateHTTP(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		errMsg = updatePage.updateElectronicAddressDataBlock(UpdateSimpleHelper.generateHTTP(),
				UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.ELECTRONIC_ADDRESSES, 0);

		// note- cease, add, update, cease
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.NOTES);

		String noteId = UpdateSimpleHelper.generateAlphabetString(5);
		errMsg = updatePage.addNoteDataBlock(noteId, UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.NOTES, 0);

		// relationship- cease, add, update, cease
		// relation type - Location, Located
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder org = fhirController.createOrganization(OrgRoleType.HDS);
		fhirController.close();

		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
				org.getIdentifier(), RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "",
				false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateRelatedOrganizationDataBlock(UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
				org.getIdentifier(), RelationshipType.LOCATED.getText(), UpdateSimpleHelper.effective_date(), "",
				false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.updateRelatedOrganizationDataBlock(UpdateSimpleHelper.effective_date(),
				UpdateSimpleHelper.increment_year_for_effective_date(), endReason, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		updatePage.ceaseDataBlock(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0);

	}

	/**
	 * validate Facility Access by User Role
	 *
	 * @param updatePage
	 * @param endReason
	 * @param userType
	 */
	public void validateFacilityAccessbyUserRole(UpdateFacilityPage updatePage, EndReason endReason,
			UserType userType) {
		String errMsg = "";
		if (UserType.ADMIN.equals(userType)) {
			assertTrue(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.IDENTIFIERS));
			assertTrue(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.ELECTRONIC_ADDRESSES));
			assertTrue(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.TELECOMMUNICATIONS));
			int count = updatePage.grabDataBlockCount(FacilitySection.NAMES);
			if (count > 0)
				assertFalse(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.NAMES));
			else
				assertTrue(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.NAMES));
			assertTrue(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.NOTES));
			assertTrue(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.ORGANIZATION_RELATIONSHIPS));

			count = updatePage.grabDataBlockCount(FacilitySection.IDENTIFIERS);
			if (count > 0)
				// IDENTIFIERS always not editable
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.IDENTIFIERS, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES);
			if (count > 0)
				assertTrue(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.ELECTRONIC_ADDRESSES, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.TELECOMMUNICATIONS);
			if (count > 0)
				assertTrue(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.TELECOMMUNICATIONS, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.CIVIC_ADDRESSES);
			if (count > 0)
				// CIVIC_ADDRESSES always not editable
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.CIVIC_ADDRESSES, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.OTHER_ADDRESS);
			if (count > 0)
				// OTHER_ADDRESS always not editable
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.NAMES);
			if (count > 0)
				assertTrue(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.NAMES, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.NOTES);
			if (count > 0)
				assertTrue(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.NOTES, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);
			if (count > 0)
				assertTrue(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0));
			// Add a new dada block to any section
			updatePage.ceaseAllDataBlockUnderSection(FacilitySection.NOTES);

			String noteId = UpdateSimpleHelper.generateAlphabetString(5);
			errMsg = updatePage.addNoteDataBlock(noteId, UpdateSimpleHelper.generateAlphabetString(5),
					UpdateSimpleHelper.effective_date(), "", false);
			assertTrue(StringUtils.isEmpty(errMsg));
			// Update an existing data block
			errMsg = updatePage.updateNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
					UpdateSimpleHelper.effective_date(), "", endReason, 0, false);
			assertTrue(StringUtils.isEmpty(errMsg));
			// cease a data block
			updatePage.ceaseDataBlock(FacilitySection.NOTES, 0);
		} else {
			assertFalse(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.IDENTIFIERS));
			assertFalse(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.ELECTRONIC_ADDRESSES));
			assertFalse(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.TELECOMMUNICATIONS));
			assertFalse(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.NAMES));
			assertFalse(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.NOTES));
			assertFalse(updatePage.isHeaderAddDateBlockButtonDisplayed(FacilitySection.ORGANIZATION_RELATIONSHIPS));

			int count = updatePage.grabDataBlockCount(FacilitySection.IDENTIFIERS);
			if (count > 0)
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.IDENTIFIERS, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES);
			if (count > 0)
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.ELECTRONIC_ADDRESSES, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.TELECOMMUNICATIONS);
			if (count > 0)
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.TELECOMMUNICATIONS, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.CIVIC_ADDRESSES);
			if (count > 0)
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.CIVIC_ADDRESSES, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.OTHER_ADDRESS);
			if (count > 0)
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.NAMES);
			if (count > 0)
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.NAMES, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.NOTES);
			if (count > 0)
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.NOTES, 0));
			count = updatePage.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);
			if (count > 0)
				assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0));

		}

	}

	/**
	 * validate Adding Facility ID
	 *
	 * @param updatePage
	 * @param chg
	 */
	public void validateAddingFacilityID(UpdateFacilityPage updatePage, EndReason chg) {
		// identifier- Not able to be added(duplicated identifier type
		// identifier- Not able to be updated( Update is not enabled)
		String errMsg = "";
		errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),
				"IFC." + UpdateSimpleHelper.generateNumericString(8) + ".BC.PRS", UpdateSimpleHelper.effective_date(),
				"", true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.7033")
				&& errMsg.contains("Cannot create duplicate record. Check the following field: Facility Identifier"));

	}

	/**
	 * validate Updating Facility Address
	 *
	 * @param updatePage
	 * @param chg
	 */
	public void validateUpdatingFacilityAddress(UpdateFacilityPage updatePage, EndReason chg) {
		// not able to update address
		assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.CIVIC_ADDRESSES, 0));
		assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0));

	}

	/**
	 * validate Updating Facility Address
	 *
	 * @param updatePage
	 * @param chg
	 */
	public void validateFacilityAddressRecognition(UpdateFacilityPage updatePage, EndReason chg) {
		// not able to update address.
		assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0));
	}

	/**
	 * validate Facility Civic Address Latitude and Longitude
	 *
	 * @param updatePage
	 * @param chg
	 */
	public static void validateFacilityCivicAddressLatitudeLongitude(UpdateFacilityPage updatePage, EndReason chg) {
		LinkedHashMap<String, String> resultContent = updatePage.grabCivicAddressBlockContent();
		CivicAddress address = new CivicAddress(resultContent);
		updatePage.clickCHSAButton();
		resultContent = updatePage.grabCivicAddressBlockContent();
		CivicAddress addressAfter = new CivicAddress(resultContent);
		assertTrue(address.getLatitude().equals(addressAfter.getLatitude()));
		assertTrue(address.getLongitude().equals(addressAfter.getLongitude()));
	}

	/**
	 * validate civic address Health Boundary Update
	 *
	 * @param updatePage
	 * @param chg
	 */
	public void validateHealthBoundaryUpdate(UpdateFacilityPage updatePage) {
		// not able to update civic address.
		assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.CIVIC_ADDRESSES, 0));

	}

	/**
	 * validate Data Block Unique Keys
	 *
	 * @param updatePage
	 * @param chg
	 */
	public void validateDataBlockUniqueKeys(UpdateFacilityPage updatePage) {
		// Identifier- 'duplicate record' error message
		String errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),
				"IFC." + UpdateSimpleHelper.generateNumericString(8) + ".BC.PRS", UpdateSimpleHelper.effective_date(),
				"", true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.7033")
				&& errMsg.contains("Cannot create duplicate record. Check the following field: Facility Identifier"));
		// civic address- not editable

		// other address - not editable

		// telecommunication
		int count = updatePage.grabDataBlockCount(FacilitySection.TELECOMMUNICATIONS);
		if (count == 0) {
			errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),
					UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
					UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
			assertTrue(StringUtils.isEmpty(errMsg));
		}
		boolean find = updatePage.findUpdateDialogDorpdownList(FacilitySection.TELECOMMUNICATIONS, 0, "telecomType");
		assertFalse(find);
		// e-address
		count = updatePage.grabDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES);
		if (count == 0) {
			errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),
					UpdateSimpleHelper.generateEmail(), UpdateSimpleHelper.effective_date(), "", false);
			assertTrue(StringUtils.isEmpty(errMsg));
		}

		find = updatePage.findUpdateDialogDorpdownList(FacilitySection.ELECTRONIC_ADDRESSES, 0, "type");
		assertFalse(find);
		// relationship
		count = updatePage.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);
		if (count == 0) {
			FHIRController fhirController = new FHIRController(UserType.ADMIN);
			MaintainOrgBuilder org = fhirController.createOrganization(OrgRoleType.HDS);
			fhirController.close();

			errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
					org.getIdentifier(), RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "",
					false);
			assertTrue(StringUtils.isEmpty(errMsg));
		}
		find = updatePage.findUpdateDialogDorpdownList(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0, "providerType");
		assertFalse(find);
		find = updatePage.findUpdateDialogInput(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0, "rpi");
		assertFalse(find);

		// note
		count = updatePage.grabDataBlockCount(FacilitySection.NOTES);
		if (count == 0) {

			errMsg = updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
					UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "", false);
			assertTrue(StringUtils.isEmpty(errMsg));
		}

		find = updatePage.findUpdateDialogInput(FacilitySection.NOTES, 0, "identifier");
		assertFalse(find);

	}

	/**
	 * validate Data Owner Code
	 *
	 * @param updatePage
	 * @param ownerCode
	 */
	public void validateDataOwnerCode(UpdateFacilityPage updatePage, String ownerCode) {
		// Identifier
		LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.IDENTIFIERS, 0);
		Identifier identifier = new Identifier(resultContent);
		assertTrue(identifier.getDataOwnerCode().equals(ownerCode));
		// name
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.NAMES);

		String errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "");
		assertTrue(StringUtils.isEmpty(errMsg));

		resultContent = updatePage.grabDataBlockContent(FacilitySection.NAMES, 0);
		Name facilityName = new Name(resultContent);
		assertTrue(facilityName.getDataOwnerCode().equals(ownerCode));
		// telecommunication
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.TELECOMMUNICATIONS);
		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		resultContent = updatePage.grabDataBlockContent(FacilitySection.TELECOMMUNICATIONS, 0);
		Telecommunication telecommunication = new Telecommunication(resultContent);
		assertTrue(telecommunication.getDataOwnerCode().equals(ownerCode));
		// e-address
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.ELECTRONIC_ADDRESSES);
		errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),
				UpdateSimpleHelper.generateEmail(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		resultContent = updatePage.grabDataBlockContent(FacilitySection.ELECTRONIC_ADDRESSES, 0);
		ElectronicAddress electronicAddress = new ElectronicAddress(resultContent);
		assertTrue(electronicAddress.getDataOwnerCode().equals(ownerCode));
		// relationship
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder org = fhirController.createOrganization(OrgRoleType.HDS);
		fhirController.close();

		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
				org.getIdentifier(), RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "",
				false);
		assertTrue(StringUtils.isEmpty(errMsg));

		resultContent = updatePage.grabDataBlockContent(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0);
		Relationship relationship = new Relationship(resultContent);

		assertTrue(relationship.getDataOwnerCode().equals(ownerCode));
		// note
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.NOTES);

		errMsg = updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		resultContent = updatePage.grabDataBlockContent(FacilitySection.NOTES, 0);
		Note note = new Note(resultContent);
		assertTrue(note.getDataOwnerCode().equals(ownerCode));
	}

	/**
	 * validate Updating Facility Telecommunication
	 *
	 * @param updatePage
	 */
	public void validateUpdatingFacilityTelecommunication(UpdateFacilityPage updatePage) {
		String messageGRS5000 = errorList.getString("messageGRS5000");

		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.TELECOMMUNICATIONS);
		String errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		boolean find = updatePage.findUpdateDialogDorpdownList(FacilitySection.TELECOMMUNICATIONS, 0, "telecomType");
		assertFalse(find);
		errMsg = updatePage.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date(),
				EndReason.CHG, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));
		errMsg = updatePage.updateTelecommunicationDataBlock(UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date(), null, 0,
				true);
		assertEquals(errMsg, messageGRS5000);

	}

	/**
	 * validate Updating Facility Electronic Address
	 *
	 * @param updatePage
	 */
	public void validateUpdatingFacilityElectronicAddress(UpdateFacilityPage updatePage) {
		String messageGRS5000 = errorList.getString("messageGRS5000");

		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.ELECTRONIC_ADDRESSES);
		String errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),
				UpdateSimpleHelper.generateEmail(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		boolean find = updatePage.findUpdateDialogDorpdownList(FacilitySection.ELECTRONIC_ADDRESSES, 0, "type");
		assertFalse(find);
		errMsg = updatePage.updateElectronicAddressDataBlock(UpdateSimpleHelper.generateEmail(),
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date(),
				EndReason.CHG, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));
		errMsg = updatePage.updateElectronicAddressDataBlock(UpdateSimpleHelper.generateEmail(),
				UpdateSimpleHelper.effective_date(), UpdateSimpleHelper.increment_year_for_effective_date(), null, 0,
				true);
		assertEquals(errMsg, messageGRS5000);

	}

	/**
	 * validate Generating Default Note ID
	 *
	 * @param updatePage
	 */
	public void validateGeneratingDefaultNoteID(UpdateFacilityPage updatePage) {
		String errMsg = updatePage.addNoteDataBlock("", UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.NOTES, 0);
		Note note = new Note(resultContent);
		String noteId = note.getNoteIdentifier();
		assertTrue(noteId.endsWith(".PRS"));
		assertTrue(noteId.startsWith("NC."));
		String trimmed = noteId.substring(3, noteId.length() - 4);
		assertTrue(UpdateSimpleHelper.isStringPositiveInteger(trimmed));
	}

	/**
	 * validate Create Facility Organization Relationship
	 *
	 * @param updatePage
	 */
	public void validateCreateFacilityOrganizationRelationship(UpdateFacilityPage updatePage) {

		String messageGRS7036 = errorList.getString("messageGRS7036");

		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder org = fhirController.createOrganization(OrgRoleType.HDS);
		MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				org.getIdentifier());
		fhirController.close();
		String randomOrgId = UpdateSimpleHelper.generateNumericString(orgQueried.getOrgIdentifier().length());

		String errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.ORGID.getText(),
				randomOrgId, RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, messageGRS7036);
		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.ORGID.getText(),
				orgQueried.getOrgIdentifier(), RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(),
				"", false);
		assertTrue(StringUtils.isEmpty(errMsg));

	}

	/**
	 * validate Facility Organization Relationship Validation
	 *
	 * @param updatePage
	 */
	public void validateFacilityOrganizationRelationshipValidation(UpdateFacilityPage updatePage) {
		String erromMessageGRS5000IdType = errorList.getString("erromMessageGRS5000IdType");
		String erromMessageGRS5000Id = errorList.getString("erromMessageGRS5000Id");
		String erromMessageGRS5000RelationType = errorList.getString("erromMessageGRS5000RelationType");
		String erromMessageGRS5000EffectiveFrom = errorList.getString("erromMessageGRS5000EffectiveFrom");
		String erromMessageGRS5000EndReason = errorList.getString("erromMessageGRS5000EndReason");

		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder orgbuild = fhirController.createOrganization(OrgRoleType.HDS);
		MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				orgbuild.getIdentifier());
		fhirController.close();
		// Add
		// empty ID type
		String errMsg = updatePage.addRelatedOrganizationDataBlock("", orgQueried.getIdentifier(),
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, erromMessageGRS5000IdType);
		// empty ID
		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), "",
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, erromMessageGRS5000Id);
		// empty relation type
		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
				orgQueried.getIdentifier(), "", UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, erromMessageGRS5000RelationType);
		// empty effectiveFrom
		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
				orgQueried.getIdentifier(), RelationshipType.LOCATION.getText(), "", "", true);
		assertEquals(errMsg, erromMessageGRS5000EffectiveFrom);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
				orgQueried.getIdentifier(), RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(),
				"", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		int count = updatePage.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);
		LinkedHashMap<String, String> resultContent = updatePage
				.grabDataBlockContent(FacilitySection.ORGANIZATION_RELATIONSHIPS, count - 1);
		Relationship relationship = new Relationship(resultContent);
		assertTrue(relationship.getRelatedOrganizationIdentifier().equals(orgQueried.getIdentifier()));
		// Update
		String effectiveFrom = UpdateSimpleHelper.effective_date();
		// empty end reason
		errMsg = updatePage.updateRelatedOrganizationDataBlock(effectiveFrom,
				UpdateSimpleHelper.increment_year_for_effective_date(), null, 0, true);
		assertEquals(errMsg, erromMessageGRS5000EndReason);
		// empty effective from
		errMsg = updatePage.updateRelatedOrganizationDataBlock("",
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, 0, true);
		assertEquals(errMsg, erromMessageGRS5000EffectiveFrom);

		errMsg = updatePage.updateRelatedOrganizationDataBlock(effectiveFrom,
				UpdateSimpleHelper.increment_year_for_effective_date(), EndReason.CHG, 0, false);
		assertTrue(StringUtils.isEmpty(errMsg));

		resultContent = updatePage.grabDataBlockContent(FacilitySection.ORGANIZATION_RELATIONSHIPS, count - 1);
		relationship = new Relationship(resultContent);
		assertTrue(relationship.getEffectiveFrom().equals(effectiveFrom));

	}

	/**
	 * validate Organization Name Auto CompleteSelection
	 *
	 * @param updatePage
	 */
	public void validateOrganizationNameAutoCompleteSelection(UpdateFacilityPage updatePage) {
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		MaintainOrgBuilder org = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				"IPC.00125085.BC.PRS");
		fhirController.close();

		String errMsg = updatePage.addRelationshipByAutoCompletion(org, RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		int count = updatePage.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);
		LinkedHashMap<String, String> resultContent = updatePage
				.grabDataBlockContent(FacilitySection.ORGANIZATION_RELATIONSHIPS, count - 1);
		Relationship relationship = new Relationship(resultContent);
		assertTrue(relationship.getRelatedOrganizationName().equals(org.getName()));

	}

	/**
	 *validate Retrieve Related Organization
	 *
	 *(TODO: update test case, since the validation target "verify" button is not applicable)
	 *
	 * @param updatePage
	 */
	public void validateRetrieveRelatedOrganization(UpdateFacilityPage updatePage) {
		// TODO: update test case, since the validation target "verify" button is not applicable

	}

}
