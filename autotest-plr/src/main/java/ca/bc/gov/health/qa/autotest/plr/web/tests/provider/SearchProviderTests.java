package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.collect.Ordering;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class SearchProviderTests implements SimpleTest{
	private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private MaintainFacilityBuilder dummyFacility;
    private SearchProviderTests(){}

    static final int SEARCH_PROVIDER_MAX_RESULTS=20;
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
//
// 	Registry Identifiers UI Suffix is Implied
    @Test(groups = { "SearchProvider"})
 	public void testRegistryIdentifiersUISuffix() {
 		
 	}
//
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
//
// 	Search - Individual Provider
    @Test(groups = { "SearchProvider"})
 	public void testSearchIndividualProvider() {
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
        SearchProviderResultsFragment searchResults =
                searchProvider.searchByCriteria("DEN", null, null, null, "Victoria", null, null);
        List<String> warningMessageList =
                searchProvider.waitForAlertMessagesFragment().grabWarningMessageList();
 	}
//
// 	Search - Minimum Data
    @Test(groups = { "SearchProvider"})
 	public void testSearchMinimumData() {
    	String errorMsg = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Provider ID'. Your transaction has not been processed. Correct and resubmit. Your transaction has not been processed. Correct and resubmit. ";
    	String errorMsgRegId = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transactionGRS. The following fields must be supplied: 'CPN or IPC'. Your transaction has not been processed. Correct and resubmit.";
    	String errorMsg03="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Identifier Type'. Your transaction has not been processed. Correct and resubmit. GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'College ID or MPID'. Your transaction has not been processed. Correct and resubmit.";
    	String errorMsg04="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Registry Identifier Type'. Your transaction has not been processed. Correct and resubmit. GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'CPN or IPC'. Your transaction has not been processed. Correct and resubmit.\r\n"
    			+ "";
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
        SearchProviderResultsFragment searchResults =
                searchProvider.searchByIdentifier("DENID", null);
        String errMsg = searchResults.grabEmptyResultsMessage();
        assertTrue(errMsg.equals(errorMsg));
        searchResults =
                searchProvider.searchByIdentifier("DENID", "54542432424243");
        assertTrue(searchResults.grabResultsRowCount()>0);
           	
        searchResults =
                searchProvider.searchByRegistryIdentifier("CPN", null);
        errMsg = searchResults.grabEmptyResultsMessage();
        assertTrue(errMsg.equals(errorMsg));
        searchResults =
                searchProvider.searchByRegistryIdentifier("CPN", "00059686");
        assertTrue(searchResults.grabResultsRowCount()>0);
               
        searchResults =
                searchProvider.searchByIdentifier("Select One", null);
        errMsg = searchResults.grabEmptyResultsMessage();
        assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")&&errMsg.contains("Identifier Type")&&errMsg.contains("Provider ID"));
        searchResults =
                searchProvider.searchByRegistryIdentifier("Select One", null);
        errMsg = searchResults.grabEmptyResultsMessage();
        assertTrue(errMsg.contains("GRS.SYS.UNK.UNK.1.0.5000")&&errMsg.contains("Registry Identifier Type")&&errMsg.contains("Registry Identifier Value"));
        
 	}
//
// 	Search - Organization Provider
    @Test(groups = { "SearchProvider"})
 	public void testSearchOrganizationProvider() {
    	
    	String error01 = "GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Name'. Your transaction has not been processed. Correct and resubmit.";
    	String error02 ="GRS.SYS.UNK.UNK.1.0.5000: Entry error. Some mandatory data is missing in your transaction. The following fields must be supplied: 'Organizational Provider Role Type'. Your transaction has not been processed. Correct and resubmit.";
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
        //Step 1
        SearchProviderResultsFragment searchResults = searchProvider.searchForOrganization("ORG", "Royal Columbian Hospital", null, null,
				"Harrison Hot Springs");
        assertTrue(searchResults.grabResultsRowCount()>0);
        //Step 2
        searchResults = searchProvider.searchForOrganization("ORG", null, null, null,null);
        String errMsg = searchResults.grabEmptyResultsMessage();
        assertTrue(errMsg.equals(error01));
       //Step 3
        searchResults = searchProvider.searchForOrganization("ORG", "Royal Columbian Hospital", null, null,
				null);
        assertTrue(searchResults.grabResultsRowCount()>0);
        
        //Step 4: not able to only search by nmae-role type default value "ORG"
        //Step 5
        searchResults = searchProvider.searchForOrganization("ORG", "Royal Columbian Hospital", null, null,
				null);
        assertTrue(searchResults.grabResultsRowCount()>0);
        //Step 6
        searchResults = searchProvider.searchForOrganization("ORG", null, null, null,"143 Noack Turnpike");
        assertTrue(searchResults.grabResultsRowCount()>0);
 	}
//
// 	Search - Search Results Limit
    @Test(groups = { "SearchProvider"})
 	public void testSearchResultsLimit() {
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
        SearchProviderResultsFragment searchResults =
                searchProvider.searchByCriteria("DEN", null, null, null, "Victoria", null, null);
        assertTrue(searchResults.grabResultsRowCount()==SEARCH_PROVIDER_MAX_RESULTS);
        searchResults = searchProvider.searchForOrganization("ORG", null, null, null,
				"Victoria");
        assertTrue(searchResults.grabResultsRowCount()==SEARCH_PROVIDER_MAX_RESULTS);
 	}
//
// 	Search - Zero Results
	@Test(groups = { "SearchProvider" })
	public void testSearchZeroResultss() {

		final String expectedMessage = "No records found.";
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
		SearchProviderResultsFragment searchResults = searchProvider.searchByIdentifier("IPC", "ABC.123");
		assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage);
		searchResults = searchProvider.searchByRegistryIdentifier("CPN", "DEF.4567");
		assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage);
		searchResults = searchProvider.searchByCriteria("MD", "Nonexistent", "Provider", "M", "Victoria", "TERMINATED",
				"RET", List.of("AMD1 ", "AMD49 "), List.of("A01 ", "A09 "));
		assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage);
		searchResults = searchProvider.searchForOrganization("ORG", "Nonexistent", "Organization", "123 Some Street",
				"Victoria");
		assertEquals(searchResults.grabEmptyResultsMessage(), expectedMessage);

	}
//
// 	Search by HDS is not in ALM yes, need to be added based on Legacy selenium
    @Test(groups = { "SearchProvider"})
 	public void testSearchHDS() {
    	PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
    	//test1
    	SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
    	SearchProviderResultsFragment searchResults = searchProviderPage.searchHDSOrganization("HOSPITAL", 
    			null, null, "Victoria", null);
    	 assertTrue(searchResults.grabResultsRowCount()>0);
    	 //test2
    	 searchResults = searchProviderPage.searchHDSOrganization(null, 
     			null, null, null, null); 
    	 String errMsg = searchProviderPage.grabPageMessage();
    	 assertTrue(errMsg.contains("The following fields must be supplied: 'Name or Description or Address Line 1 or City'"));
    	//test3
    	 searchResults = searchProviderPage.searchHDSOrganization("GENERAL_CARE", 
      			null, null, null, null); 
     	  errMsg = searchProviderPage.grabPageMessage();
     	 assertTrue( errMsg.contains("The following fields must be supplied: 'Name or Description or Address Line 1 or City'"));
     	//test4
     	 searchResults = searchProviderPage.searchHDSOrganization("GENERAL_CARE", 
       			null, null, "vaa", null); 
      	  errMsg = searchResults.grabEmptyResultsMessage();
      	 assertTrue( errMsg.contains("No records found"));
     	 //test5
      	searchResults = searchProviderPage.searchHDSOrganization("HOSPITAL", 
    			"Care*", null, "Victoria", null);
    	 assertTrue(searchResults.grabResultsRowCount()>0);
    	 /*boolean found=false;
    	 for(int i=0;i<searchResults.grabResultsRowCount();i++) {
    		 List<String> item = searchResults.grabResultsRow(i);
    		 if(item.contains("Royal Jubilee Hospital"))found=true;
    	 }*
    	 assertTrue(found,"The target Hds Organization is not found");*/
 	}

}
