package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Ordering;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.HdsType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class SearchProviderTests implements SimpleTest{
	private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    
    String errorMsgName = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Name'. Your transaction has not been processed. Correct and resubmit.";
	String errorMsgOrgRoleType ="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Organizational Provider Role Type'. Your transaction has not been processed. Correct and resubmit.";
    String errorMsgProiderID = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Provider ID'. Your transaction has not been processed. Correct and resubmit.";
    String errorMsgCPNIPC = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transactionGRS. The following fields must be supplied: 'CPN or IPC'. Your transaction has not been processed. Correct and resubmit.";
    String errorMsgIdType="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Identifier Type'. Your transaction has not been processed. Correct and resubmit.";
	String errorMsgRegIdType="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Registry Identifier Type'. Your transaction has not been processed. Correct and resubmit.";
	String errorMsgRegIdValue="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Registry Identifier Value'. Your transaction has not been processed. Correct and resubmit.";
	String errorMsgCPSID="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'College ID or MPID'. Your transaction has not been processed. Correct and resubmit.";
	
	String errorEntryError="Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Name or Description or Address Line 1 or City'"; 
	String warningMAxResult="Maximum search results returned. Please refine your search criteria.";
    
    private SearchProviderTests(){}

    static final int SEARCH_PROVIDER_MAX_RESULTS=20;
    private final String NORECORDFOUND = "No records found.";
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


   
    

// 	Alphabetical Sorting of Registry User search Results
    //@Test(groups = { "SearchProvider"})
 	public void testAlphabeticalSortingRegistryUserSearchResults() {
    	 //This is admin test
 	}
 	//todo
// 	Case Insensitive Search
    @Test(groups = { "SearchProvider"})
 	public void testCaseInsensitiveSearch() {
    	
		TestHelper.logIn(workflowManager_, UserType.ADMIN);
		//Upper. low. mix 
 		//TODO: create test data FHIR
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
		SearchProviderResultsFragment searchResults = searchProvider.searchByCriteria("DEN", "Wayne".toUpperCase(),
				"Ables".toUpperCase(), null, "Victoria".toUpperCase(), null, null);
		
		List<String> upperCaseSearch=searchResults.grabResultsRow(0);
		
		searchResults = searchProvider.searchByCriteria("DEN", "Wayne",
				"Ables", null, "Victoria", null, null);
		List<String> mixedCaseSearch=searchResults.grabResultsRow(0);
		
		searchResults = searchProvider.searchByCriteria("DEN", "Wayne".toLowerCase(),
				"Ables".toLowerCase(), null, "Victoria".toLowerCase(), null, null);
		List<String> lowerCaseSearch=searchResults.grabResultsRow(0);
		
		assertTrue(upperCaseSearch.equals(lowerCaseSearch),"The search results are not equal");
		assertTrue(mixedCaseSearch.equals(lowerCaseSearch),"The search results are not equal");
		//TODO:create org
		searchResults = searchProvider.searchForOrganization("ORG", "Royal*", null, null,"Victoria");
		List<String>  orgMixedCaseSearch=searchResults.grabResultsRow(0);
		searchResults = searchProvider.searchForOrganization("ORG", "Royal*".toUpperCase(), null, null,"Victoria".toUpperCase());
		List<String>  orgUpperCaseSearch=searchResults.grabResultsRow(0);
		searchResults = searchProvider.searchForOrganization("ORG", "Royal*".toLowerCase(), null, null,"Victoria".toLowerCase());
		List<String> orgLowerCaseSearch=searchResults.grabResultsRow(0);
		assertTrue(orgMixedCaseSearch.equals(orgUpperCaseSearch),"The search results are not equal");
		assertTrue(orgUpperCaseSearch.equals(orgLowerCaseSearch),"The search results are not equal");
 	}
//todo
// 	Provider Search
    @Test(groups = { "SearchProvider"})
 	public void testProviderSearch() {
    	//TestHelper.logIn(workflowManager_, UserType.ADMIN);
    	//TODO: create providr by FHIR
    	 PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
	        
    	 SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
	       
    	 SearchProviderResultsFragment 
	        searchResults =
	                searchProvider.searchByIdentifier("DENID", "54542432424243");
	     assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
	     
	     searchResults =
	                searchProvider.searchByRegistryIdentifier("CPN", "00059686");
	     assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
	     
	     searchResults =
	                searchProvider.searchByCriteria("DEN", null, null, null, "Victoria", null, null);
	     assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
	     
	     searchResults = searchProvider.searchForOrganization("ORG", null, null, null,
					"Victoria");
	     assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
	     
 	}
//todo
// 	Registry Identifiers UI Suffix is Implied
    @Test(groups = { "SearchProvider"})
 	public void testRegistryIdentifiersUISuffix() {
 		
 	}
//todo
// 	Search - Alphabetical Sorting
    @Test(groups = { "SearchProvider"})
 	public void testAlphabeticalSorting() {
    	final String expectedWarningMessage =
                "Maximum search results returned. Please refine your search criteria.";
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
        SearchProviderResultsFragment searchResults =
                searchProvider.searchByCriteria("DEN", null, null, null, "Victoria", null, null);
        List<String> warningMessageList =
                searchProvider.waitForAlertMessagesFragment().grabWarningMessageList();
        assertTrue(searchResults.grabResultsRowCount()>1,"search result has too less rows");
        List<String> names = new ArrayList<String>();
        for(int i=0;i<searchResults.grabResultsRowCount();i++) {
       	 List<String> resultRow = searchResults.grabResultsRow(i);
       	 String name = resultRow.getFirst();
       	 if (name.contains("Link to View"))
            {
                continue;
            }
            else
            {
           	 names.add(name);
            }
        }
        assertTrue(names.size()>1,"search result has too less sortable rows");
        assertTrue(Ordering.natural().isOrdered(names),"Search Results is not Alphabetically sorted");
        
        
        searchResults = searchProvider.searchForOrganization("ORG", null, null, null,	"Victoria");
        
         names = new ArrayList<String>();
        for(int i=0;i<searchResults.grabResultsRowCount();i++) {
       	 List<String> resultRow = searchResults.grabResultsRow(i);
       	 String name = resultRow.getFirst();
       	 if (name.contains("Link to View"))
            {
                continue;
            }
            else
            {
           	 names.add(name);
            }
        }
        assertTrue(names.size()>1,"search result has too less sortable rows");
        assertTrue(Ordering.natural().isOrdered(names),"Search Results is not Alphabetically sorted");
 		
 	}
//TODO
// 	Search - Individual Provider
    @Test(groups = { "SearchProvider"})
 	public void testSearchIndividualProvider() {
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
    	SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
    	 //FHIR
        FHIRController fhirController = new FHIRController(UserType.ADMIN);
        IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(IndividualDataGenerator.getInstance());
       
        IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.MD);
        MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
        individual = fhirController.submitIndividual(individual);
		MaintainIndividualBuilder queriedProvider = fhirController.queryIndividualByIdentifier(IdentifierType.IPC,
				individual.getIdentifier(IdentifierType.IPC));
		fhirController.close();
		
		IndividualRoleType roleType = queriedProvider.getRoleType();
		String surname = queriedProvider.getFamilyName();
		String[] names = queriedProvider.getNames();
		Map<String, String> address = queriedProvider.getAddressList().get(0);
		String city = address.get("city");
		String addressline1 = address.get("line1");
		
        //step 1
        SearchProviderResultsFragment searchResults =
        		searchProviderPage.searchByCriteria(roleType.name(), null, surname, null, city, null, null);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
      //step 2
        searchResults =
        		searchProviderPage.searchByCriteria(roleType.name(), null, null, null, null, null, null);
        
       
      //step 3
        searchResults =
        		searchProviderPage.searchByCriteria(null, null, null, null, city, null, null);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
      //step 4
        searchResults =
        		searchProviderPage.searchByCriteria(null, names.toString(), null, null, null, null, null);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
      //step 5
        searchResults =
        		searchProviderPage.searchByCriteria(null, null, null, null, city, null, null);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
      //step 6
        searchResults = searchProviderPage.searchByCriteria(null, null, null, "M", null, null,
				null, null, null);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
      //step 7
        searchResults = searchProviderPage.searchByCriteria(null, null, null, null, null, null,null, List.of("AMD1 ", "AMD49 "), null);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
      //step 8
      
        searchResults = searchProviderPage.searchByCriteria(null, null, null, null, null, null,
				null, null, List.of("A01 ", "A09 "));
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
 	}

    // 	Search - Minimum Data
    @Test(groups = { "SearchProvider"})
 	public void testSearchMinimumData() {
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
        //FHIR
        FHIRController fhirController = new FHIRController(UserType.ADMIN);
        /*IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(IndividualDataGenerator.getInstance());
       
        IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.MD);
        MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
        individual = fhirController.submitIndividual(individual);*/
        MaintainIndividualBuilder queriedProvider = fhirController.queryIndividualByIdentifier(IdentifierType.IPC, "IPC.00117991.BC.PRS");
        fhirController.close();
		String cpsId = queriedProvider.getIdentifier(IdentifierType.DENID);
		String cpnNum=UpdateSimpleHelper.getRegIdString("CPN",queriedProvider.getIdentifier(IdentifierType.CPN));
        //step1
        SearchProviderResultsFragment searchResults =
        		searchProviderPage.searchByIdentifier(IdentifierType.DENID.name(), null,false);
        String errMsg = searchProviderPage.grabPageErrorMessage();
        assertTrue(errMsg.equals(errorMsgProiderID),"Expected error message not found");
        //step2
        searchResults =
        		searchProviderPage.searchByIdentifier(IdentifierType.DENID.name(), cpsId);
        assertTrue(searchResults.grabResultsRowCount()>0);
        //step3
        searchResults =
        		searchProviderPage.searchByRegistryIdentifier(IdentifierType.CPN.name(), null,false);
        errMsg = searchProviderPage.grabPageErrorMessage();
        assertTrue(errMsg.equals(errorMsgRegIdValue),"Expected error message not found");
        //step4
        searchResults =
        		searchProviderPage.searchByRegistryIdentifier(IdentifierType.CPN.name(), cpnNum);
        assertTrue(searchResults.grabResultsRowCount()>0);
        //step5
        searchResults =
        		searchProviderPage.searchByIdentifier("Select One", null,false);
        errMsg = searchProviderPage.grabPageErrorMessage();
        assertTrue(errMsg.contains(errorMsgIdType)&&errMsg.contains(errorMsgProiderID),"Expected error messages not found");
        //step6
        searchResults =
        		searchProviderPage.searchByRegistryIdentifier("Select One", null,false);
        errMsg = searchProviderPage.grabPageErrorMessage();
        assertTrue(errMsg.contains(errorMsgRegIdType)&&errMsg.contains(errorMsgRegIdValue),"Expected error messages not found");
        
 	}
    
    // 	Search - Organization Provider
    @Test(groups = { "SearchProvider"})
 	public void testSearchOrganizationProvider() {
    	
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
        //FHIR
        FHIRController fhirController = new FHIRController(UserType.ADMIN);
        OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG).withAlias();
        MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);
        MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC, org.getIdentifier(IdentifierType.IPC));
        fhirController.close();
        OrgRoleType roleType = orgQueried.getRoleType();
        String name=orgQueried.getName();       
        Map<String, String> address = orgQueried.getAddressList().get(0);
        String city=address.get("city");
        String addressline1=address.get("line1");
        String desp=orgQueried.getAlias();
        
        //Step 1
        SearchProviderResultsFragment searchResults = searchProviderPage.searchForOrganization(roleType.name(), name, null, addressline1,
        		city);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
        //Step 2
        searchResults = searchProviderPage.searchForOrganization(roleType.name(), null, null, null,null);
        String errMsg = searchProviderPage.grabPageErrorMessage();
        assertTrue(errMsg.equals(errorEntryError),"Expected error message not found");
       //Step 3
        searchResults = searchProviderPage.searchForOrganization(roleType.name(), name, null, null,
				null);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
        
        //Step 4: not able to only search by name. Role type has default value "ORG"
        //Step 5: description not support FHIR
        searchResults = searchProviderPage.searchForOrganization(OrgRoleType.ORG.name(), null, desp
        		, null,	null);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
        //Step 6
        searchResults = searchProviderPage.searchForOrganization(roleType.name(), null, null, addressline1,null);
        assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
 	}

    //TODO
    // 	Search - Search Results Limit
    @Test(groups = { "SearchProvider"})
 	public void testSearchResultsLimit() {
    	
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
    	
    	//create 22 individual DEN in victoria? TODO
    	
    	//test
        SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
        SearchProviderResultsFragment searchResults =
        		searchProviderPage.searchByCriteria("DEN", null, null, null, "Victoria", null, null);
        assertTrue(searchResults.grabResultsRowCount()==SEARCH_PROVIDER_MAX_RESULTS);
        String errMsg = searchProviderPage.grabWarningErrorMessage();
        assertTrue(errMsg.equals(warningMAxResult),"Expected warning message not found");
        // create 22 ORG in vicotria 
       /* FHIRController fhirController = new FHIRController(UserType.ADMIN);
        OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.HDS);
        MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);
        MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC, org.getIdentifier(IdentifierType.IPC));
        fhirController.close();*/
        //test
        searchResults = searchProviderPage.searchForOrganization("ORG", null, null, null,
				"Victoria");
        assertTrue(searchResults.grabResultsRowCount()==SEARCH_PROVIDER_MAX_RESULTS);
        assertTrue(errMsg.equals(warningMAxResult),"Expected warning message not found");
 	}
//
// 	Search - Zero Results
	@Test(groups = { "SearchProvider" })
	public void testSearchZeroResultss() {

	
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
		SearchProviderResultsFragment searchResults = searchProvider.searchByIdentifier("IPC", "ABC.123");
		assertEquals(searchResults.grabEmptyResultsMessage(), NORECORDFOUND);
		searchResults = searchProvider.searchByRegistryIdentifier("CPN", "DEF.4567");
		assertEquals(searchResults.grabEmptyResultsMessage(), NORECORDFOUND);
		searchResults = searchProvider.searchByCriteria("MD", "Nonexistent", "Provider", "M", "Victoria", "TERMINATED",
				"RET", List.of("AMD1 ", "AMD49 "), List.of("A01 ", "A09 "));
		assertEquals(searchResults.grabEmptyResultsMessage(), NORECORDFOUND);
		searchResults = searchProvider.searchForOrganization("ORG", "Nonexistent", "Organization", "123 Some Street",
				"Victoria");
		assertEquals(searchResults.grabEmptyResultsMessage(), NORECORDFOUND);

	}
//
// 	Search by HDS is not in ALM yes, need to be added based on Legacy selenium
    @Test(groups = { "SearchProvider"})
 	public void testSearchHDS() {
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
    	FHIRController fhirController = new FHIRController(UserType.ADMIN);
        OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.HDS).withAlias();
        MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);
        MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC, org.getIdentifier(IdentifierType.IPC));
        fhirController.close();
        HdsType hdsType = orgQueried.getHdsType();
        String name=orgQueried.getName();       
        Map<String, String> address = orgQueried.getAddressList().get(0);
        String city=address.get("city");
        String addressline1=address.get("line1");
        String desp=orgQueried.getAlias();
    	//test1
    	SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
    	SearchProviderResultsFragment searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), 
    			name, desp, city, addressline1);
    	 assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
    	//test2
    	 searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), 
      			null, null, null, null); 
     	  String errMsg = searchProviderPage.grabPageErrorMessage();
     	 assertTrue( errMsg.contains("The following fields must be supplied: 'Name or Description or Address Line 1 or City'"));
     	//test3
     	 searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), 
     			name, desp, null, null); 
     	assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
     	//test4
    	 searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), 
    			null, null, city, null); 
    	assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
     	 //test5 
    	searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), 
     			null, desp, null, null); 
     	assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
     	//test 6
      	searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), 
      			null, null, null, addressline1);
    	 assertTrue(searchResults.grabResultsRowCount()>0,"search result has too less rows");
    	 
 	}

}
