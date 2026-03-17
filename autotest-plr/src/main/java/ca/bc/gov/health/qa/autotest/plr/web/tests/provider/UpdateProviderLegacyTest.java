package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static org.testng.Assert.assertEquals;
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
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.EndReasonCode;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.UpdateProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
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
	    //MaintainRequestBuilder

	   // private static final Map<ProviderType, MaintainIndividualBuilder> defaultProviders = new HashMap<>();
	private UpdateProviderLegacyTest() {
        try
        {
            errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read JSON data (%s).", errorPath);
            throw new IllegalStateException(msg, e);
        }
    }

    @AfterClass
    public void teardown() {
    	 workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }

    @BeforeMethod
    public void before(Object[] parameters) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
        if (!workflow.isLoggedIn()) {
            workflow.login().openPlr();
        }
    }

    @BeforeTest
    public void beforeTest() {
        FHIRController fhirController = new FHIRController(UserType.ADMIN);
         defaultBC = fhirController
                .createIndividual(new IndividualMaintainConfig(IndividualRoleType.DEN));
        LOG.info("Created default BC provider with IPC: {}", defaultBC.getIdentifier(IdentifierType.IPC));
      
        /*MaintainIndividualBuilder defaultOOP = fhirController
                .createIndividual(new IndividualMaintainConfig(IndividualRoleType.OOP_RECT));
        LOG.info("Created default OOP provider with IPC: {}", defaultBC.getIdentifier(IdentifierType.IPC));*/
        
         defaultOrg = fhirController.createOrganization(OrgRoleType.ORG);
        LOG.info("Created default Organization provider with IPC: {}", defaultBC.getIdentifier(IdentifierType.IPC));
        fhirController.close();

        defaultProviders.put(ProviderType.BC_PRACTITIONER, defaultBC);
        defaultProviders.put(ProviderType.OOP_PRACTITIONER, defaultOrg);
        
    }

   
//	==============PLR 596	
//	#Then Add Identifiers
    @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
    public void testAddIdentifiers(ProviderType providerType) {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
        UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
        String identifier="";
        if(providerType.equals(ProviderType.BC_PRACTITIONER))
        		identifier = defaultBC.getIdentifier(IdentifierType.IPC);
        else if(providerType.equals(ProviderType.ORGANIZATION))
        		identifier = defaultOrg.getIdentifier(IdentifierType.IPC);
        String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
        String idString=UpdateSimpleHelper.generateNumericString(8);
        UpdateProviderPage page = actions.openProvider(pauthId);
        int count=page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
        page.addIdentifiersDataBlock("IPC",idString ,UpdateSimpleHelper.effective_date(),
        		UpdateSimpleHelper.increment_year_for_effective_date() , false);
        assertEquals(page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true),count+1);
        
        page.ceaseDataBlockByKey(ProviderSection.IDENTIFIERS, "Identifier",idString);
        
        
    }
//
//	#Then Add Registry Identifier
    @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
    public void testAddRegistryIdentifiers(ProviderType providerType) {
    	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
         UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
         String identifier="";
         if(providerType.equals(ProviderType.BC_PRACTITIONER))
     		identifier = defaultBC.getIdentifier(IdentifierType.IPC);
     else if(providerType.equals(ProviderType.ORGANIZATION))
     		identifier = defaultOrg.getIdentifier(IdentifierType.IPC);
         String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
         String idString=UpdateSimpleHelper.generateNumericString(8);
         UpdateProviderPage page = actions.openProvider(pauthId);
         
         //int count=page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
         page.ceaseDataBlockByKey(ProviderSection.REGISTRY_IDENTIFIERS, "Identifier",identifier);
         page.addIdentifiersDataBlock("IPC",identifier ,UpdateSimpleHelper.effective_date(),
         		UpdateSimpleHelper.increment_year_for_effective_date() , false);
         LinkedHashMap<String, String> content = page.grabDataBlockByKey(ProviderSection.REGISTRY_IDENTIFIERS, "Type","Internal Provider ID (IPC)");
         String newIPC = content.get("Identifier");
         assertEquals(newIPC,identifier);
         
       
    }
//
//	#Then Add Statuses
    @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
    public void testAddStatuses(ProviderType providerType) {
    	String errorMsg="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Status Reason Code'. Your transaction has not been processed. Correct and resubmit.";
    			
    	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
         UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
         String identifier = defaultBC.getIdentifier(IdentifierType.IPC);
         String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
         String idString=UpdateSimpleHelper.generateNumericString(8);
         UpdateProviderPage page = actions.openProvider(pauthId);
       //Provider: no status reason code
         page.ceaseAllDataBlockUnderSection(ProviderSection.STATUSES);
         String msg=page.addStatusDataBlock("LIC", "ACTIVE", null,
        		 UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(),true);
              assertEquals(errorMsg,msg);
              msg=page.addStatusDataBlock("LIC", "ACTIVE", "GS",
             		 UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(),false);
              assertTrue(StringUtils.isEmpty(msg));
            
    }
    

//	#Then Assign New CPN to an Existing Provider Record--not applicable
     
//	#Then Code Restriction Validation - Identifier
  
    @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
    public void testCodeRestrictionValidationIdentifier(ProviderType providerType) {
    	List<String> expectList = new ArrayList<>(Arrays.asList("CPN - Common Party Number", 
    			"IPC - Internal Provider Code", 
    			"DENID - Dentist ID Number"));
    	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
         UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
         String identifier = defaultBC.getIdentifier(IdentifierType.IPC);
         String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
         String idString=UpdateSimpleHelper.generateNumericString(8);
         UpdateProviderPage page = actions.openProvider(pauthId);
         List<String> typeList = page.getAddIdentifierTypeList();
         assertTrue( UpdateSimpleHelper.haveSameElements(expectList, typeList) );
         
    }
    private Map<String, List<String>> getStatusCodeToReasonCodeMap() {
    	
    	String[] cancelArray= {"AU - Address Unknown", "INNONPRAC - Initial Non Practicing", 
    			"LAP - License Lapsed on Request", "DEN - Licensed Denied", "MEDSTUD - Medical Student", 
    			"ORG - Organization Provider", "OOP - Out of Province", "RESDISC - Resigned - disciplinary action", "RET - Retired", "UNK - Unknown", "VW - Voluntary Withdrawa"};
    	
    	String[] activeArray= {};
    	String[] terminatedArray= {};
    	String[] inactiveArray= {};
    	String[] suspendedArray= {};
    	String[] nullifiedArray= {};
    	String[] pendingArray= {};
    	String[] unknownArray= {};
    	
    	
    	
    	Map<String, List<String>> map = new HashMap<>();
    	
    	map.put("CANCELLED - Cancelled",Arrays.asList(cancelArray));
    	map.put("ACTIVE - Active",Arrays.asList(activeArray));
    	map.put("TERMINATED - Terminated",Arrays.asList(terminatedArray));
    	map.put("INACTIVE - Inactive",Arrays.asList(inactiveArray));
    	map.put("SUSPENDED - Suspended",Arrays.asList(suspendedArray));
    	map.put("NULLIFIED - Nullified",Arrays.asList(nullifiedArray));
    	map.put("PENDING - Pending",Arrays.asList(pendingArray));
    	map.put("UNKNOWN - Unknown",Arrays.asList(unknownArray));
    	return map;
    }
//	#Then Code Restriction Validation - Status Code
 @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
    public void testCodeRestrictionValidationStatusCode(ProviderType providerType) {	
	 Map<String, List<String>> map=getStatusCodeToReasonCodeMap();
	 
   	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
        UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
        String identifier = defaultBC.getIdentifier(IdentifierType.IPC);
        String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
        String idString=UpdateSimpleHelper.generateNumericString(8);
        UpdateProviderPage page = actions.openProvider(pauthId);
     
        List<String> statusCodeList = page.getAddDataBloackDropdownMenuList(ProviderSection.STATUSES, "statusCode");
        
        for(String code :statusCodeList) {
        	List<String> reasonCodeList = page.getStatusReasonCodeList(code);
        }
           
   }


//	#Then Generating a Default Note ID
 @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
 public void testGeneratingDefaultNoteID(ProviderType providerType) {
		
	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
     UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
     String identifier = defaultBC.getIdentifier(IdentifierType.IPC);
     String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
     String idString=UpdateSimpleHelper.generateNumericString(8);
     UpdateProviderPage page = actions.openProvider(pauthId);
  
    //      assertTrue(StringUtils.isEmpty(msg));
        
}
//
//	#Then Validate Note
 @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
 public void testValidateNote(ProviderType providerType) {
	 String errorMsg01="GRS.SYS.UNK.UNK.1.0.5000: Entry Error. Some mandatory data is missing in your transaction. Your transaction has not been processed. Correct and resubmit. The following fields must be supplied.";
	 String errorMsg02="GRS.SYS.UNK.UNK.1.0.5003: Entry Error. Field length must be between 0 and 255. Your transaction has not been processed. Correct and resubmit.";
	 String errorMsg03="GRS.SYS.UNK.UNK.1.0.5000: Entry Error. Some mandatory data is missing in your transaction. Your transaction has not been processed. Correct and resubmit. The following fields must be supplied.";
	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
     UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
     String identifier = defaultBC.getIdentifier(IdentifierType.IPC);
     String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
     String idString=UpdateSimpleHelper.generateNumericString(8);
     UpdateProviderPage page = actions.openProvider(pauthId);
     
     page.ceaseAllDataBlockUnderSection(ProviderSection.NOTES);
     String noteId="NoteId-"+UpdateSimpleHelper.generateAlphabetString(6);
     String noteText="NoteText:"+UpdateSimpleHelper.generateAlphabetString(10);
     String msg=page.addNoteDataBlock(noteId, null, UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(),true);
     assertEquals(msg,errorMsg01);
     page.addNoteDataBlock(noteId, noteText, UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(),false);
     LinkedHashMap<String, String> content = page.grabDataBlockByKey(ProviderSection.NOTES, "Note Identifier",noteId);
     assertEquals(noteText,content.get("Note Text"));
     
     msg=page.addNoteDataBlock("NoteId-"+UpdateSimpleHelper.generateAlphabetString(6)
     , UpdateSimpleHelper.generateAlphabetString(256),
  		 UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(),true);
     assertEquals(msg,errorMsg02);
     
     msg=page.addNoteDataBlock("","", UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(),true);
     assertEquals(msg,errorMsg03);
     
     page.addNoteDataBlock("NoteId-"+UpdateSimpleHelper.generateAlphabetString(6), "NoteText:"+UpdateSimpleHelper.generateAlphabetString(10)
     , UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(),false);
     assertTrue(StringUtils.isEmpty(msg));
        
}
//
//	#Then Validate Status Class Code
 @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
 public void testValidateStatusClassCode(ProviderType providerType) {
	 String errorMsg01="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Status Class Code'. Your transaction has not been processed. Correct and resubmit.";
		
	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
     UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
     String identifier = defaultBC.getIdentifier(IdentifierType.IPC);
     String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
     String idString=UpdateSimpleHelper.generateNumericString(8);
     UpdateProviderPage page = actions.openProvider(pauthId);
  
     //     assertTrue(StringUtils.isEmpty(msg));
        
}
//
//	#Then Validate Status Reason Code
 @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
 public void testValidateStatusReasonCode(ProviderType providerType) {
	 String errorMsg01="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Status Reason Code'. Your transaction has not been processed. Correct and resubmit.";
		
	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
     UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
     String identifier = defaultBC.getIdentifier(IdentifierType.IPC);
     String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
     String idString=UpdateSimpleHelper.generateNumericString(8);
     UpdateProviderPage page = actions.openProvider(pauthId);
  
     //     assertTrue(StringUtils.isEmpty(msg));
        
}
//
//	#Then Validate Status Type Code
 @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
 public void testValidateStatusTypeCode(ProviderType providerType) {
	 String errorMsg01=	"GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Status Code'. Your transaction has not been processed. Correct and resubmit.";
	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
     UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
     String identifier = defaultBC.getIdentifier(IdentifierType.IPC);
     String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
     String idString=UpdateSimpleHelper.generateNumericString(8);
     UpdateProviderPage page = actions.openProvider(pauthId);
  
     //     assertTrue(StringUtils.isEmpty(msg));
        
}
//
//	#Then Add Notes
 @Test(dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
 public void testAddNotes(ProviderType providerType) {
		
	 PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);
     UpdateProviderActions actions = workflowManager_.getSelectedWorkflow().getUpdateProviderActions();
     String identifier = defaultBC.getIdentifier(IdentifierType.IPC);
     String pauthId=UpdateSimpleHelper.getRegIdString("IPC",identifier);
     String idString=UpdateSimpleHelper.generateNumericString(8);
     UpdateProviderPage page = actions.openProvider(pauthId);
     
     //int count=page.grabActiveDataBlockCount(ProviderSection.IDENTIFIERS, true);
     String noteId="NoteId-"+UpdateSimpleHelper.generateAlphabetString(6);
     String noteText="NoteText:"+UpdateSimpleHelper.generateAlphabetString(10);
     page.addNoteDataBlock(noteId, noteText, UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(),false);
     
     LinkedHashMap<String, String> content = page.grabDataBlockByKey(ProviderSection.NOTES, "Note Identifier",noteId);
     assertEquals(noteText,content.get("Note Text"));
     
     noteText="NoteTextUpdate:"+UpdateSimpleHelper.generateAlphabetString(10);
     int index=page.findDataBloackIndexByKey(ProviderSection.NOTES,"Note Identifier",noteId);
     page.updateNoteDataBlock(noteText, UpdateSimpleHelper.effective_date(),UpdateSimpleHelper.increment_year_for_effective_date(),
    		 EndReason.CHG, index,false);   
}
	
	
//	==============PLR 606
//
//			Then Ceasing Last Active Provider Identifier
//
//			Then Logical Deletion of Providers
//
//			Then Reactivating Logically Deleted Providers
//
//			Then Registry Identifiers UI Suffix Is Implied
//
//			Then Update Identifiers
//
//			Then Update Notes
//
//			Then Update Registry Identifier
//
//			Then Update Statuses
//
//			Then Validate Note
//
//			Then Validate Provider Identifiers
//
//			Then Validate Provider Identifiers For Update
//
//			Then Validate Status Class Code
//
//			Then Validate Status Reason Code
//
//			Then Validate Status Type Code

}
