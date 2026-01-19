package ca.bc.gov.health.qa.autotest.plr.web.actions.facility;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants.EAddressField;
import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants.TelecomField;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.RelatedProviderIdentifierType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.CivicAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.ElectronicAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Identifier;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Name;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Note;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.OtherAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Relationship;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Telecommunication;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ElectronicAddressType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.IdentifierTypeName;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.RelationshipType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.TelecommunicationType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.json.JSONObject;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper.effective_date;
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
	private static final String ID_NONEXIST = "IPC.00999999.BC.PRS";
	private static final String ID_PERSON = "IPC.00124841.BC.PRS";

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
	 * @return an UpdateFacilityPage reference to the opened organization page
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
	 * @param idOrgOne
	 * @param idOrgSecond
	 */
	public void validateFacilityDataBlockMultiplicity(UpdateFacilityPage updatePage, String idOrgOne, String idOrgSecond) {
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

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), idOrgOne,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), idOrgOne,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", true);
		assertEquals(errMsg, errMsg7033Dup);

		ceaseRelatedOrganizationDataBlockByRelatedId(updatePage, idOrgOne);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), idOrgOne,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), idOrgOne,
				RelationshipType.LOCATED.getText(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), idOrgSecond,
				RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
	}

	/**
	 * cease Note Data Block By Id
	 *
	 * @param updatePage the facility view/update page already opened and ready
	 * @param noteId	 the ID of the note data block to cease
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
	 * @param ID_ORG
	 */
	public void ValidateRelatedOrganizationID(UpdateFacilityPage updatePage, String ID_ORG) {
		String errMsg = "";
		int maxRelatedId = 50;

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
	 * @param updatePage the facility view/update page already opened and ready
	 * @param endReason	 the end reason code to fill the update fields with
	 */
	public void validateUpdatingFacility(UpdateFacilityPage updatePage, EndReason endReason,MaintainOrgBuilder org) {
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
	 * @param updatePage 	the facility view/update page already opened and ready
	 * @param endReason 	the end reason code to fill the update fields with
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
	 * @param updatePage the facility view/update page already opened and ready
	 *
	 */
	public void validateAddingFacilityID(UpdateFacilityPage updatePage) {
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
	 * @param updatePage the facility view/update page already opened and ready
	 *
	 */
	public void validateUpdatingFacilityAddress(UpdateFacilityPage updatePage) {
		// not able to update address
		assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.CIVIC_ADDRESSES, 0));
		assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0));

	}

	/**
	 * validate Updating Facility Address
	 *
	 * @param updatePage the facility view/update page already opened and ready
	 */
	public void validateFacilityAddressRecognition(UpdateFacilityPage updatePage) {
		// not able to update address.
		assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0));
	}

	/**
	 * validate Facility Civic Address Latitude and Longitude
	 *
	 * @param updatePage the facility view/update page already opened and ready
	 *
	 */
	public static void validateFacilityCivicAddressLatitudeLongitude(UpdateFacilityPage updatePage) {
		LinkedHashMap<String, String> resultContent = updatePage.grabCivicAddressBlockContent();
		CivicAddress address = new CivicAddress(resultContent);
		updatePage.clickCHSAButton();
		resultContent = updatePage.grabCivicAddressBlockContent();
		CivicAddress addressAfter = new CivicAddress(resultContent);
        assertEquals(addressAfter.getLatitude(), address.getLatitude());
        assertEquals(addressAfter.getLongitude(), address.getLongitude());
	}

	/**
	 * validate civic address Health Boundary Update
	 *
	 * @param updatePage the facility view/update page already opened and ready
	 *
	 */
	public void validateHealthBoundaryUpdate(UpdateFacilityPage updatePage) {
		// not able to update civic address.
		assertFalse(updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.CIVIC_ADDRESSES, 0));

	}

	/**
	 * validate Data Block Unique Keys
	 *
	 * @param updatePage the facility view/update page already opened and ready
	 *
	 */
	public void validateDataBlockUniqueKeys(UpdateFacilityPage updatePage) {
		// Identifier- 'duplicate record' error message
		String errMsg = updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),
				"IFC." + UpdateSimpleHelper.generateNumericString(8) + ".BC.PRS", UpdateSimpleHelper.effective_date(),
				"", true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.7033")
				&& errMsg.contains("Cannot create duplicate record. Check the following field: Facility Identifier"));
		// civic address - not editable

		// other address - not editable

		// telecommunication
		int count = updatePage.grabDataBlockCount(FacilitySection.TELECOMMUNICATIONS);
		assertTrue(count>0);
		boolean find = updatePage.findUpdateDialogDropdownList(FacilitySection.TELECOMMUNICATIONS, 0, "telecomType");
		assertFalse(find);
		// e-address
		count = updatePage.grabDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES);
		assertTrue(count>0);
		find = updatePage.findUpdateDialogDropdownList(FacilitySection.ELECTRONIC_ADDRESSES, 0, "type");
		assertFalse(find);
		// relationship
		count = updatePage.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);
		assertTrue(count>0);
		find = updatePage.findUpdateDialogDropdownList(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0, "providerType");
		assertFalse(find);
		find = updatePage.findUpdateDialogInput(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0, "rpi");
		assertFalse(find);

		// note
		count = updatePage.grabDataBlockCount(FacilitySection.NOTES);
		assertTrue(count>0);
		find = updatePage.findUpdateDialogInput(FacilitySection.NOTES, 0, "identifier");
		assertFalse(find);

	}

	/**
	 * validate Data Owner Code
	 *
	 * @param updatePage the facility view/update page already opened and ready
	 * @param ownerCode
	 * @param org
	 *
	 */
	public void validateDataOwnerCode(UpdateFacilityPage updatePage, String ownerCode,MaintainOrgBuilder org) {
		// Identifier
		LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.IDENTIFIERS, 0);
		Identifier identifier = new Identifier(resultContent);
        assertEquals(ownerCode, identifier.getDataOwnerCode());
		// name
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.NAMES);

		String errMsg = updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "");
		assertTrue(StringUtils.isEmpty(errMsg));

		resultContent = updatePage.grabDataBlockContent(FacilitySection.NAMES, 0);
		Name facilityName = new Name(resultContent);
        assertEquals(ownerCode, facilityName.getDataOwnerCode());
		// telecommunication
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.TELECOMMUNICATIONS);
		errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		resultContent = updatePage.grabDataBlockContent(FacilitySection.TELECOMMUNICATIONS, 0);
		Telecommunication telecommunication = new Telecommunication(resultContent);
        assertEquals(ownerCode, telecommunication.getDataOwnerCode());
		// e-address
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.ELECTRONIC_ADDRESSES);
		errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),
				UpdateSimpleHelper.generateEmail(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		resultContent = updatePage.grabDataBlockContent(FacilitySection.ELECTRONIC_ADDRESSES, 0);
		ElectronicAddress electronicAddress = new ElectronicAddress(resultContent);
        assertEquals(ownerCode, electronicAddress.getDataOwnerCode());
		// relationship
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.ORGANIZATION_RELATIONSHIPS);

		errMsg = updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),
				org.getIdentifier(), RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(), "",
				false);
		assertTrue(StringUtils.isEmpty(errMsg));

		resultContent = updatePage.grabDataBlockContent(FacilitySection.ORGANIZATION_RELATIONSHIPS, 0);
		Relationship relationship = new Relationship(resultContent);

        assertEquals(ownerCode, relationship.getDataOwnerCode());
		// note
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.NOTES);

		errMsg = updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		resultContent = updatePage.grabDataBlockContent(FacilitySection.NOTES, 0);
		Note note = new Note(resultContent);
        assertEquals(ownerCode, note.getDataOwnerCode());
	}

	/**
	 * validate Updating Facility Telecommunication
	 *
	 * @param updatePage the facility view/update page already opened and ready
	 */
	public void validateUpdatingFacilityTelecommunication(UpdateFacilityPage updatePage) {
		String messageGRS5000 = errorList.getString("messageGRS5000");

		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.TELECOMMUNICATIONS);
		String errMsg = updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),
				UpdateSimpleHelper.generateNumericString(3), UpdateSimpleHelper.generateNumericString(7),
				UpdateSimpleHelper.generateNumericString(4), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));

		boolean find = updatePage.findUpdateDialogDropdownList(FacilitySection.TELECOMMUNICATIONS, 0, "telecomType");
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
	 * @param updatePage the facility view/update page already opened and ready
	 */
	public void validateUpdatingFacilityElectronicAddress(UpdateFacilityPage updatePage) {
		String messageGRS5000 = errorList.getString("messageGRS5000");

		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.ELECTRONIC_ADDRESSES);
		String errMsg = updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),
				UpdateSimpleHelper.generateEmail(), UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		boolean find = updatePage.findUpdateDialogDropdownList(FacilitySection.ELECTRONIC_ADDRESSES, 0, "type");
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
	 * @param updatePage the facility view/update page already opened and ready
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
	 * @param updatePage the facility view/update page already opened and ready
	 */
	public void validateCreateFacilityOrganizationRelationship(UpdateFacilityPage updatePage,MaintainOrgBuilder orgQueried) {

		String messageGRS7036 = errorList.getString("messageGRS7036");

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
	 * @param updatePage the facility view/update page already opened and ready
	 */
	public void validateFacilityOrganizationRelationshipValidation(UpdateFacilityPage updatePage,MaintainOrgBuilder orgQueried) {
		String erromMessageGRS5000IdType = errorList.getString("erromMessageGRS5000IdType");
		String erromMessageGRS5000Id = errorList.getString("erromMessageGRS5000Id");
		String erromMessageGRS5000RelationType = errorList.getString("erromMessageGRS5000RelationType");
		String erromMessageGRS5000EffectiveFrom = errorList.getString("erromMessageGRS5000EffectiveFrom");
		String erromMessageGRS5000EndReason = errorList.getString("erromMessageGRS5000EndReason");

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
        assertEquals(orgQueried.getIdentifier(), relationship.getRelatedOrganizationIdentifier());
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
        assertEquals(effectiveFrom, relationship.getEffectiveFrom());

	}

	/**
	 * validate Organization Name Auto CompleteSelection
	 *
	 * @param updatePage the facility view/update page already opened and ready
	 */
	public void validateOrganizationNameAutoCompleteSelection(UpdateFacilityPage updatePage,MaintainOrgBuilder org) {
		String errMsg = updatePage.addRelationshipByAutoCompletion(org, RelationshipType.LOCATION.getText(),
		UpdateSimpleHelper.effective_date(), "", false);
		assertTrue(StringUtils.isEmpty(errMsg));
		int count = updatePage.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);
		LinkedHashMap<String, String> resultContent = updatePage
				.grabDataBlockContent(FacilitySection.ORGANIZATION_RELATIONSHIPS, count - 1);
		Relationship relationship = new Relationship(resultContent);
        assertEquals(org.getName(), relationship.getRelatedOrganizationName());

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
	
	/**
	 * Adds a telecommunication data block to a page.
	 * Based in a list of strings representing the number instead of separate fields.
	 *
	 * @param page				the update facility page reference
	 * @param telecomNumber		telecom number: list of strings: should be formatted
	 *                             [type, area code, phone number, extension, effective from, effective to]
	 * @param expectError		whether an error is expected or not
	 * @return					the error message if an error is expected to occur
	 */
	public String addTelecommunicationNumber(UpdateFacilityPage page, List<String> telecomNumber, boolean expectError)
	{
		return page.addTelecommunicationDataBlock(telecomNumber.get(0),
				telecomNumber.get(1), telecomNumber.get(2), telecomNumber.get(3),
				telecomNumber.get(4), telecomNumber.get(5), expectError);
	}

	/**
	 * Updates a telecommunication data block in a page.
	 * Based in a list of strings representing the number instead of separate fields.
	 *
	 * @param page				the update facility page reference
	 * @param type				the telecommunication type to update
	 * @param telecomNumber		telecom number: list of strings should be formatted [area code, phone number, extension]
	 * @param expectError		whether an error is expected or not
	 * @return					the error message if an error is expected to occur
	 */
	public String updateTelecommunicationNumber(UpdateFacilityPage page, TelecommunicationType type,
												List<String> telecomNumber, boolean expectError)
	{
		return page.updateTelecommunicationDataBlock(telecomNumber.get(1), telecomNumber.get(2), telecomNumber.get(3),
				telecomNumber.get(4), telecomNumber.get(5),EndReason.CHG,
				Integer.parseInt(getTelecomInfo(page, type).get("index")), expectError);
	}
	
	/**
	 * Verifies the mandatory telecommunication attributes have been changed and match as expected
	 *
	 * @param page			the update facility page reference
	 * @param telecomType	the telecommunication type to get content for
	 * @param areaCode		the area code to assert the telecom area code has been changed to
	 * @param phoneNumber	the phone number to assert the telecom phone number has been changed to
	 */
	public void verifyMandatoryAttributesTelecom(UpdateFacilityPage page, TelecommunicationType telecomType,
												  String areaCode, String phoneNumber)
	{
		LinkedHashMap<String,String> telecomInfo = getTelecomInfo(page, telecomType);

		assertEquals(telecomInfo.get(TelecomField.TYPE.getString()), telecomType.getDataField(),
				"Unexpected telecommunication type");
		assertEquals(telecomInfo.get(TelecomField.AREA_CODE.getString()), areaCode,
				"Unexpected area code field result");
		assertEquals(telecomInfo.get(TelecomField.NUMBER.getString()), phoneNumber,
				"Unexpected phone number field result");
		assertEquals(telecomInfo.get(TelecomField.EFFECTIVE_FROM.getString()), effective_date(),
				"Unexpected effective from data field result");
	}
	
	/**
	 * Gets the info of a data block for the telecommunication type
	 *
	 * @param page			the update facility page reference
	 * @param telecomType	the telecommunication type to get content for
	 * @return				a map of strings for the data block corresponding to the desired telecommunication type
	 */
	public LinkedHashMap<String,String> getTelecomInfo(UpdateFacilityPage page, TelecommunicationType telecomType)
	{
		LinkedHashMap<String,String> telecomInfo = new LinkedHashMap<>();

		int telecomIndex = page.grabActiveDataBlockCount(FacilitySection.TELECOMMUNICATIONS, true);
		for (int index = 0; index < telecomIndex; index++)
		{
			telecomInfo = page.grabTelecommunicationsBlockContent(index);
			if (telecomInfo.get(TelecomField.TYPE.getString()).equals(telecomType.getDataField())) {
				telecomInfo.put("index", Integer.toString(index));
				break;
			}
		}

		return telecomInfo;
	}
	/**
	 * Adds an electronic address data block to a page.
	 * Based in a list of strings representing the number instead of separate fields.
	 *
	 * @param page			the update facility page reference
	 * @param eAddress		e-address: list of strings, should be formatted
	 *                         [type, address, effective from, effective to]
	 * @param expectError	whether an error is expected or not
	 * @return				the error message if an error is expected to occur
	 */
	public String addEAddress(UpdateFacilityPage page, List<String> eAddress, boolean expectError)
	{
		return page.addElectronicAddressDataBlock(eAddress.get(0),
				eAddress.get(1), eAddress.get(2), eAddress.get(3), expectError);
	}
	/**
	 * Gets the info of a data block for the electronic address type
	 *
	 * @param page			the update facility page reference
	 * @param eaType		the electronic address type to get content for
	 * @return				a map of strings for the data block corresponding to the desired e-address type
	 */
	public LinkedHashMap<String,String> getEAddressInfo(UpdateFacilityPage page, ElectronicAddressType eaType)
	{
		LinkedHashMap<String,String> eAddressInfo = new LinkedHashMap<>();

		int telecomIndex = page.grabActiveDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES, true);
		for (int index = 0; index < telecomIndex; index++)
		{
			eAddressInfo = page.grabElectronicAddressesBlockContent(index);
			if (eAddressInfo.get(EAddressField.TYPE.getString()).equals(eaType.getDataField())) {
				eAddressInfo.put("index", Integer.toString(index));
				break;
			}
		}

		return eAddressInfo;
	}
	/**
	 * Updates an electronic address data block to a page.
	 * Based in a list of strings representing the number instead of separate fields.
	 *
	 * @param page			the update facility page reference
	 * @param type			the e-address type to update
	 * @param eAddress		e-address: list of strings, should be formatted
	 *                         [type, address, effective from, effective to]
	 * @param expectError	whether an error is expected or not
	 * @return				the error message if an error is expected to occur
	 */
	public String updateEAddress(UpdateFacilityPage page, ElectronicAddressType type,
								 List<String> eAddress, boolean expectError)
	{
		return page.updateElectronicAddressDataBlock(eAddress.get(1), eAddress.get(2), eAddress.get(3),
				EndReason.CHG,Integer.parseInt(getEAddressInfo(page, type).get("index")), expectError);
	}

	/**
	 * Verifies the mandatory e-address attributes have been changed and match as expected
	 *
	 * @param page			the update facility page reference
	 * @param eaType		the electronic address type to get content for
	 * @param address		the address to assert the electronic address field has been changed to
	 */
	public void verifyMandatoryAttributesEAddress(UpdateFacilityPage page, ElectronicAddressType eaType, String address)
	{
		LinkedHashMap<String,String> eaInfo = getEAddressInfo(page, eaType);

		assertEquals(eaInfo.get(EAddressField.TYPE.getString()), eaType.getDataField(),
				"Unexpected telecommunication type");
		assertEquals(eaInfo.get(EAddressField.ADDRESS.getString()), address,
				"Unexpected area code field result");
		assertEquals(eaInfo.get(EAddressField.EFFECTIVE_FROM.getString()), effective_date(),
				"Unexpected effective from data field result");
	}
}
