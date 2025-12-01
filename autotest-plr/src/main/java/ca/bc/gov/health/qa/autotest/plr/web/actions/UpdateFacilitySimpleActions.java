package ca.bc.gov.health.qa.autotest.plr.web.actions;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.ElectronicAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Note;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.OtherAddress;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Relationship;
import ca.bc.gov.health.qa.autotest.plr.web.actions.model.facility.Telecommunication;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.ElectronicAddressType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.IdentifierTypeName;
import ca.bc.gov.health.qa.autotest.plr.web.tests.RelatedProviderIdentifierType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.RelationshipType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TelecommunicationType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

public class UpdateFacilitySimpleActions {
	private static final Logger LOG = ExecutionLogManager.getLogger();
	private final SeleniumSession selenium_;
	private final URI uri_;
	private final UserType userType_;
	
	public UpdateFacilitySimpleActions(SeleniumSession selenium, URI uri, UserType userType) {
		selenium_ = selenium;
		uri_ = uri;
		userType_ = userType;
	}
	
	

	public SeleniumSession getSelenium_() {
		return selenium_;
	}



	public URI getUri_() {
		return uri_;
	}



	public UserType getUserType_() {
		return userType_;
	}



	public UpdateFacilityPage openFacility(String fauthId) {
		LOG.info("Open facility({}).", fauthId);
		UpdateFacilityPage updateFacilityPage = new UpdateFacilityPage(selenium_, uri_.resolve("plr/FacilityDetails.xhtml"));
		updateFacilityPage.openFacility(fauthId);
		updateFacilityPage.waitForReady();
		return updateFacilityPage;
	}
	

	public void validateFacilityIdentifiers(UpdateFacilityPage updatePage) {
		String errMsg="";
		String ID_SPECIAL_CHAR="IFC.0012479#.BC.PRS";
		String ID_FOREIGN_CHAR="IFC.0012479À.BC.PRS";
		
		errMsg=updatePage.addIdentifierDataBlock("","IFC."+UpdateSimpleHelper.generateNumericString(8)+".BC.PRS",
				UpdateSimpleHelper.effective_date() ,"",true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains("The following fields must be supplied: 'Identifier Type'."));
		
		errMsg=updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),"",
				UpdateSimpleHelper.effective_date() ,"",true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains("The following fields must be supplied: 'Identifier'."));
		
		errMsg=updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),ID_FOREIGN_CHAR,
				UpdateSimpleHelper.effective_date() ,"",true);
		assertTrue(errMsg.contains("GRS.SYS.IDE.UNK.1.0.7006") && errMsg.contains("Identifiers can contain only numbers, the English alphabet and periods"));
		
		errMsg=updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),ID_SPECIAL_CHAR,
				UpdateSimpleHelper.effective_date() ,"",true);
		assertTrue(errMsg.contains("GRS.SYS.IDE.UNK.1.0.7006") && errMsg.contains("Identifiers can contain only numbers, the English alphabet and periods"));
		
		errMsg=updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),"IFC."+UpdateSimpleHelper.generateNumericString(10)+".BC.PRS",
				UpdateSimpleHelper.effective_date() ,"",true);
		//assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains("The following fields must be supplied: ''Identifier''."));
		
		errMsg=updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),"IFC."+UpdateSimpleHelper.generateNumericString(8)+".BC.PRS",
				UpdateSimpleHelper.effective_date() ,"",true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.7033") && errMsg.contains("Cannot create duplicate record. Check the following field: Facility Identifier"));
		
	}



	public void validateFacilityName(UpdateFacilityPage updatePage) {
		String errMsg="";
		int maxName=100;
		int index=0;
		if(updatePage.grabActiveDataBlockCount(FacilitySection.NAMES, true)>0)
			updatePage.ceaseDataBlock(FacilitySection.NAMES,0);
		
		errMsg=updatePage.addNameDataBlock("NAME-"+UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-"+UpdateSimpleHelper.generateAlphabetString(5),"","");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains("The following fields must be supplied: 'Effective From'."));
		
		errMsg=updatePage.addNameDataBlock("NAME-"+UpdateSimpleHelper.generateAlphabetString(maxName),
				"DESC-"+UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.effective_date(),"");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003") && errMsg.contains("'Facility Name' length must be between 0 and 100"));
		
		errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(maxName),
				"DESC-"+UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.effective_date(),"");
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.updateNameDataBlock("NAME-"+UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-"+UpdateSimpleHelper.generateAlphabetString(5),"","",index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains("The following fields must be supplied: 'Effective From'."));
		errMsg=updatePage.updateNameDataBlock("NAME-"+UpdateSimpleHelper.generateAlphabetString(maxName),
				"DESC-"+UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.effective_date(),"",index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003") && errMsg.contains("'Facility Name' length must be between 0 and 100"));
		errMsg=updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(maxName),
				"DESC-"+UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.effective_date(),"",index);
		assertTrue(StringUtils.isEmpty(errMsg));
	}



	public void validateFacilityDescription(UpdateFacilityPage updatePage) {
		String errMsg="";
		int maxDesc=200;
		int index=0;
		if(updatePage.grabActiveDataBlockCount(FacilitySection.NAMES, true)>0)
			updatePage.ceaseDataBlock(FacilitySection.NAMES,0);
		
		errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-"+UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.effective_date(),"");
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				"",UpdateSimpleHelper.effective_date(),"",index);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		updatePage.ceaseDataBlock(FacilitySection.NAMES,0);
		

		errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),""
				,UpdateSimpleHelper.effective_date(),"");
		assertTrue(StringUtils.isEmpty(errMsg));
		
		updatePage.ceaseDataBlock(FacilitySection.NAMES,0);
		
		errMsg=updatePage.addNameDataBlock("","DESC-"+UpdateSimpleHelper.generateAlphabetString(5)
			,UpdateSimpleHelper.effective_date(),"");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains(" The following fields must be supplied: 'Name'"));
		
		errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-"+UpdateSimpleHelper.generateAlphabetString(5),"","");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains("The following fields must be supplied: 'Effective From'."));
		
		errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				"DESC-"+UpdateSimpleHelper.generateAlphabetString(maxDesc),UpdateSimpleHelper.effective_date(),"");
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003") && errMsg.contains("'Facility Description' length must be between 0 and 200"));
		

		errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(maxDesc),UpdateSimpleHelper.effective_date(),"");
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.updateNameDataBlock("",UpdateSimpleHelper.generateAlphabetString(10),UpdateSimpleHelper.effective_date(),"",index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains(" The following fields must be supplied: 'Name'"));
		
		errMsg=updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.generateAlphabetString(10),"","",index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains("The following fields must be supplied: 'Effective From'."));
		
		errMsg=updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.generateAlphabetString(maxDesc+1),UpdateSimpleHelper.effective_date(),"",index);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003") && errMsg.contains("'Facility Description' length must be between 0 and 200"));
		
		errMsg=updatePage.updateNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.generateAlphabetString(maxDesc),UpdateSimpleHelper.effective_date(),"",index);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		
	}



	public void validateFacilityMailingAddressType(UpdateFacilityPage updatePage) {
		String expectType="Physical location (P)";
		LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.OTHER_ADDRESS, 0);
		OtherAddress oAddrResult = new OtherAddress(resultContent);
		assertTrue(oAddrResult.getAddressType().equals(expectType));
		boolean is=updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0);
		assertFalse(is);
		
	}



	public void validateFacilityMailingAddressPurpose(UpdateFacilityPage updatePage) {
		String expectPurpose="Facility Contact (FC)";
		LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.OTHER_ADDRESS, 0);
		OtherAddress oAddrResult = new OtherAddress(resultContent);
		assertTrue(oAddrResult.getAddressPurpose().equals(expectPurpose));
		boolean is=updatePage.isDataBlockUpdateButtonDisplayed(FacilitySection.OTHER_ADDRESS, 0);
		assertFalse(is);
		
	}



	public void validateFacilityDataBlockMultiplicity(UpdateFacilityPage updatePage) {
		String errMsg="";
		String ID_ORG01="IPC.00000480.BC.PRS";
		String ID_ORG02="IPC.00000985.BC.PRS";
		String errMsg2201Tel="PRS.REG.UNK.MTN.1.0.2201: Entry Error. Each address, e-address and telecomm object that is related to a Facility must have a unique type and purpose combination unless the owner is different. Check the following fields:Telecommunication Type and Purpose";
		String errMsg2201EAddr="PRS.REG.UNK.MTN.1.0.2201: Entry Error. Each address, e-address and telecomm object that is related to a Facility must have a unique type and purpose combination unless the owner is different. Check the following fields:Electronic Address Type and Purpose";
		String errMsg7033Dup="GRS.SYS.UNK.UNK.1.0.7033: Cannot create duplicate record. Check the following field: facility relationship";
		String errMsg7033DupNote="GRS.SYS.UNK.UNK.1.0.7033: Cannot create duplicate record. Check the following field: note";
		
		errMsg=updatePage.addIdentifierDataBlock(IdentifierTypeName.IFC.getText(),"IFC."+UpdateSimpleHelper.generateNumericString(8)+".BC.PRS",
				UpdateSimpleHelper.effective_date() ,"",true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.7033") && errMsg.contains("Cannot create duplicate record. Check the following field: Facility Identifier"));
		//name
		
		if(updatePage.grabActiveDataBlockCount(FacilitySection.NAMES, true)>0)
			updatePage.ceaseDataBlock(FacilitySection.NAMES,0);
		
		errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.effective_date(),"");
		assertTrue(StringUtils.isEmpty(errMsg));
		
		/* After name is added, the 'add' button on header is disappeared. this step is not applicable. 
		errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.effective_date(),"");
		*/
		
		updatePage.ceaseDataBlock(FacilitySection.NAMES,0);
		
		errMsg=updatePage.addNameDataBlock(UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.effective_date(),"");
		assertTrue(StringUtils.isEmpty(errMsg));
		
		
		//telecom
		int count=updatePage.grabActiveDataBlockCount(FacilitySection.TELECOMMUNICATIONS, true);
		for(int i=0;i<count;i++) {
			updatePage.ceaseDataBlock(FacilitySection.TELECOMMUNICATIONS,0);
		}
		
		errMsg=updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7),UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7),UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.equals(errMsg2201Tel));
		
		ceaseTelecommunicationByType(updatePage,TelecommunicationType.PHONE);
	
		
		errMsg=updatePage.addTelecommunicationDataBlock(TelecommunicationType.PHONE.getText(),UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7),UpdateSimpleHelper.generateNumericString(4),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		

		errMsg=updatePage.addTelecommunicationDataBlock(TelecommunicationType.FAX.getText(),UpdateSimpleHelper.generateNumericString(3),
				UpdateSimpleHelper.generateNumericString(7),"",
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		//e-address
		count=updatePage.grabActiveDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES, true);
		for(int i=0;i<count;i++) { 
			updatePage.ceaseDataBlock(FacilitySection.ELECTRONIC_ADDRESSES,0);
		}
		
		errMsg=updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),UpdateSimpleHelper.generateEmail(),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),UpdateSimpleHelper.generateEmail(),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.equals(errMsg2201EAddr));
		
		ceaseElectronicAddressByType(updatePage,ElectronicAddressType.EMAIL);
		
		errMsg=updatePage.addElectronicAddressDataBlock(ElectronicAddressType.EMAIL.getText(),UpdateSimpleHelper.generateEmail(),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.addElectronicAddressDataBlock(ElectronicAddressType.HTTP.getText(),UpdateSimpleHelper.generateHTTP(),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		//relationship
		
		//ceaseRelatedOrganizationDataBlockByRelatedId(updatePage,ID_ORG01);
		//ceaseRelatedOrganizationDataBlockByRelatedId(updatePage,ID_ORG02);
		count=updatePage.grabActiveDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS, true);
		for(int i=0;i<count;i++) { 
			updatePage.ceaseDataBlock(FacilitySection.ORGANIZATION_RELATIONSHIPS,0);
		}
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),ID_ORG01,RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),ID_ORG01,RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.equals(errMsg7033Dup));
		
		ceaseRelatedOrganizationDataBlockByRelatedId(updatePage,ID_ORG01);
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),ID_ORG01,RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),ID_ORG01,RelationshipType.LOCATED.getText(),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),ID_ORG02,RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		
		
		//note
		updatePage.ceaseAllDataBlockUnderSection(FacilitySection.NOTES);
		
		String noteId=UpdateSimpleHelper.generateAlphabetString(5);
		errMsg=updatePage.addNoteDataBlock(noteId,UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.addNoteDataBlock(noteId,UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.equals(errMsg7033DupNote));
		
		ceaseNoteDataBlockById(updatePage,noteId);
		
		errMsg=updatePage.addNoteDataBlock(noteId,UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		String noteId02=UpdateSimpleHelper.generateAlphabetString(5);
		errMsg=updatePage.addNoteDataBlock(noteId02,UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
	
}



	private void ceaseNoteDataBlockById(UpdateFacilityPage updatePage, String noteId) {
int index=updatePage.grabActiveDataBlockCount(FacilitySection.NOTES, true);
		
		for (int i=0;i<index;i++) {
			LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.NOTES, i);
			Note result = new Note(resultContent);
			if(result.getNoteIdentifier().equals(noteId)) {
				updatePage.ceaseDataBlock(FacilitySection.NOTES, i);
				
			}
			
		}
		
	}



	private void ceaseRelatedOrganizationDataBlockByRelatedId(UpdateFacilityPage updatePage, String key) {

		int index=updatePage.grabActiveDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS, true);
		
		for (int i=0;i<index;i++) {
			LinkedHashMap<String, String> resultContent = updatePage.grabOrgRelationshipsBlockContent(i);
			Relationship result = new Relationship(resultContent);
			if(result.getRelatedOrganizationIdentifier().equals(key)) {
				updatePage.ceaseDataBlock(FacilitySection.ORGANIZATION_RELATIONSHIPS, i);
				
			}
			
		}
		
	}



	private void ceaseElectronicAddressByType(UpdateFacilityPage updatePage,ElectronicAddressType email) {
		String key="Email ";
		int index=updatePage.grabActiveDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES, true);
		for (int i=0;i<index;i++) {
			LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.ELECTRONIC_ADDRESSES, i);
			ElectronicAddress result = new ElectronicAddress(resultContent);
			if(result.getType().contains(key)) {
				updatePage.ceaseDataBlock(FacilitySection.ELECTRONIC_ADDRESSES, i);
				break;
			}
			
		}
		
	}



	private void ceaseTelecommunicationByType(UpdateFacilityPage updatePage,TelecommunicationType phone) {
		String key="Telephone";
		int index=updatePage.grabActiveDataBlockCount(FacilitySection.TELECOMMUNICATIONS, true);
		for (int i=0;i<index;i++) {
			LinkedHashMap<String, String> resultContent = updatePage.grabDataBlockContent(FacilitySection.TELECOMMUNICATIONS, i);
			Telecommunication result = new Telecommunication(resultContent);
			if(result.getType().contains(key)) {
				updatePage.ceaseDataBlock(FacilitySection.TELECOMMUNICATIONS, i);
				break;
			}
			
		}
		
	}



	public void validateFacilityNotesTexts(UpdateFacilityPage updatePage) {
		String errMsg="";
		int maxNoteId=30;
		int maxNoteText=255;
		int index=0;
		String errMessage01="GRS.SYS.UNK.UNK.1.0.5003: Entry Error. 'Note Identifier' length must be between 0 and 30. Your transaction has not been processed. Correct and resubmit.";
		String errMessage02="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Note Text'. Your transaction has not been processed. Correct and resubmit.";
		String errMessage03="GRS.SYS.UNK.UNK.1.0.5003: Entry Error. 'Note Text' length must be between 0 and 255. Your transaction has not been processed. Correct and resubmit.";
	
		
		
		index=updatePage.grabActiveDataBlockCount(FacilitySection.NOTES, true);
		
		errMsg=updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(maxNoteId+1),UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003") && errMsg.contains("'Note Identifier' length must be between 0 and 30"));
		
		errMsg=updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(maxNoteId),UpdateSimpleHelper.generateAlphabetString(5),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		index++;
		
		errMsg=updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(6),"",
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains("Some mandatory data is missing in your transaction. The following fields must be supplied: 'Note Text'"));
		
		errMsg=updatePage.updateNoteDataBlock("",
				UpdateSimpleHelper.effective_date(),"",index-1,true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000") && errMsg.contains("Some mandatory data is missing in your transaction. The following fields must be supplied: 'Note Text'"));
		
		errMsg=updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(3),UpdateSimpleHelper.generateAlphabetString(maxNoteText+1),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003") && errMsg.contains("'Note Text' length must be between 0 and 255"));
		
		errMsg=updatePage.updateNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(maxNoteText+1),
				UpdateSimpleHelper.effective_date(),"",index-1,true);
		assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5003") && errMsg.contains("'Note Text' length must be between 0 and 255"));
		
		errMsg=updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.generateAlphabetString(maxNoteText),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		errMsg=updatePage.addNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(5),UpdateSimpleHelper.generateAlphabetString(maxNoteText),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
		index++;
		
		errMsg=updatePage.updateNoteDataBlock(UpdateSimpleHelper.generateAlphabetString(maxNoteText),
				UpdateSimpleHelper.effective_date(),"",index-1,false);
		assertTrue(StringUtils.isEmpty(errMsg));
		
		//cease test data
		
	}



	public void ValidateRelatedOrganizationID(UpdateFacilityPage updatePage) {
		String errMsg="";
		int maxRelatedId=50;
		
		String ID_SPECIAL_CHAR="IPC.0012479#.BC.PRS";
		String ID_NONEXIST="IPC.00999999.BC.PRS";
		String ID_PERSON="IPC.00124841.BC.PRS";
		//String ID_ORG="IPC.00123986.BC.PRS";
		String ID_ORG="IPC.00082689A.BC.PRS";
		String errMessage01="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Related Provider Identifier'. Your transaction has not been processed. Correct and resubmit.";
		String errMessage02="GRS.SYS.UNK.UNK.1.0.5003: Entry Error. 'Provider Identifier' length must be between 0 and 50. Your transaction has not been processed. Correct and resubmit.";
		String errMessage03="GRS.SYS.IDE.UNK.1.0.7036: Identifier does not uniquely identify a Provider. Cannot create relationship.";
		String errMessage04="PRS.FAC.REL.MTN.1.0.9026: The Facility relationship with the Organization cannot be created as the Provider Identifier is not an Organization Identifier.";
		
		int index=updatePage.grabActiveDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS, true);
		
		ceaseRelatedOrganizationDataBlockByRelatedId(updatePage,ID_ORG);
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),"",RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.equals(errMessage01));
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),UpdateSimpleHelper.generateAlphabetNumericString(maxRelatedId+1),RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.equals(errMessage02));
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),ID_SPECIAL_CHAR,RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.equals(errMessage03));
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),ID_NONEXIST,RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.equals(errMessage03));
		
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),ID_PERSON,RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",true);
		assertTrue(errMsg.equals(errMessage04));
		
		errMsg=updatePage.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(),ID_ORG,RelationshipType.LOCATION.getText(),
				UpdateSimpleHelper.effective_date(),"",false);
		assertTrue(StringUtils.isEmpty(errMsg));
	}
	
	
}
