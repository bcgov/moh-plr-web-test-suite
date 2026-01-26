package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Ordering;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationDataGenerator;
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

public class SearchProviderTests implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
	private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList,warningList;
    
	private SearchProviderTests() {
		
		try
        {
            errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
            warningList = new JSONObject(Files.readString(errorPath)).getJSONObject("warnings");
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read JSON data (%s).", errorPath);
            throw new IllegalStateException(msg, e);
        }
	}

	static final int SEARCH_PROVIDER_MAX_RESULTS = 20;
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

	// Case Insensitive Search
	@Test(groups = { "SearchProvider" })
	public void testCaseInsensitiveSearch() {

		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());

		IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.MD);
		MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
		individual = fhirController.submitIndividual(individual);
		MaintainIndividualBuilder queriedIndividual = fhirController.queryIndividualByIdentifier(IdentifierType.MPID,
				individual.getIdentifier(IdentifierType.MPID));
		fhirController.close();
		IndividualRoleType roleType = queriedIndividual.getRoleType();
		String surname = queriedIndividual.getFamilyName();
		String firstname = queriedIndividual.getNames()[0];
		Map<String, String> address = queriedIndividual.getAddressList().get(0);
		String city = address.get("city");

		// step1
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByCriteria(roleType.name(),
				firstname.toUpperCase(), surname.toUpperCase(), null, city.toUpperCase(), null, null);
		List<String> upperCaseSearch = searchResults.grabResultsRow(0);
		// step2
		searchResults = searchProviderPage.searchByCriteria(roleType.name(), firstname.toLowerCase(), surname, null,
				city.toLowerCase(), null, null);
		List<String> lowerCaseSearch = searchResults.grabResultsRow(0);
		// step3
		searchResults = searchProviderPage.searchByCriteria(roleType.name(), firstname, surname, null, city, null,
				null);
		List<String> mixedCaseSearch = searchResults.grabResultsRow(0);
		assertTrue(upperCaseSearch.equals(lowerCaseSearch), "The search results are not equal");
		assertTrue(mixedCaseSearch.equals(lowerCaseSearch), "The search results are not equal");

		// FHIR-organization
		fhirController = new FHIRController(UserType.ADMIN);
		OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG).withAlias();
		MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);
		MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				org.getIdentifier(IdentifierType.IPC));
		fhirController.close();
		String orgName = orgQueried.getName();
		address = orgQueried.getAddressList().get(0);
		String orgCity = address.get("city");
		String orgAddressline1 = address.get("line1");
		String orgDesp = orgQueried.getAlias();

		// step4
		searchResults = searchProviderPage.searchForOrganization("ORG", orgName.toUpperCase(), orgDesp.toUpperCase(),
				orgAddressline1.toUpperCase(), orgCity.toUpperCase());
		List<String> orgUpperCaseSearch = searchResults.grabResultsRow(0);
		// step5
		searchResults = searchProviderPage.searchForOrganization("ORG", orgName.toLowerCase(), orgDesp.toLowerCase(),
				orgAddressline1.toLowerCase(), orgCity.toLowerCase());
		List<String> orgLowerCaseSearch = searchResults.grabResultsRow(0);
		// step 6
		searchResults = searchProviderPage.searchForOrganization("ORG", orgName, orgDesp, orgAddressline1, orgCity);
		List<String> orgMixedCaseSearch = searchResults.grabResultsRow(0);
		assertTrue(orgMixedCaseSearch.equals(orgUpperCaseSearch), "The search results are not equal");
		assertTrue(orgUpperCaseSearch.equals(orgLowerCaseSearch), "The search results are not equal");
	}

	// Provider Search
	@Test(groups = { "SearchProvider" })
	public void testProviderSearch() {
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());

		IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.DEN);
		MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
		individual = fhirController.submitIndividual(individual);
		MaintainIndividualBuilder queriedIndividual = fhirController.queryIndividualByIdentifier(IdentifierType.DENID,
				individual.getIdentifier(IdentifierType.DENID));
		fhirController.close();
		String providerId = queriedIndividual.getIdentifier(IdentifierType.DENID);
		String surname = queriedIndividual.getFamilyName();
		String firstname = queriedIndividual.getNames()[0];
		Map<String, String> address = queriedIndividual.getAddressList().get(0);
		String city = address.get("city");
		String cpnString = queriedIndividual.getIdentifier(IdentifierType.CPN);
		String cpnNum = UpdateSimpleHelper.getRegIdString(IdentifierType.CPN.name(), cpnString);
		IndividualRoleType roleType = queriedIndividual.getRoleType();
		// step1
		SearchProviderResultsFragment searchResults = searchProvider.searchByIdentifier(IdentifierType.DENID.name(),
				providerId);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// step 2
		searchResults = searchProvider.searchByRegistryIdentifier(IdentifierType.CPN.name(), cpnNum);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// step 3
		searchResults = searchProvider.searchByCriteria(roleType.name(), firstname, surname, null, city, null,
				null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");

		// FHOR - organization
		fhirController = new FHIRController(UserType.ADMIN);
		OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG).withAlias();
		MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);
		MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				org.getIdentifier(IdentifierType.IPC));
		fhirController.close();

		String oegName = orgQueried.getName();
		address = orgQueried.getAddressList().get(0);
		String orgCity = address.get("city");
		String orgAddressline1 = address.get("line1");
		String orgDesp = orgQueried.getAlias();
		// step 4
		searchResults = searchProvider.searchForOrganization("ORG", oegName, orgDesp, orgAddressline1, orgCity);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");

	}

	// Registry Identifiers UI Suffix is Implied
	@Test(groups = { "SearchProvider" })
	public void testRegistryIdentifiersUISuffix() {
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());

		IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.DEN);
		MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
		individual = fhirController.submitIndividual(individual);
		MaintainIndividualBuilder queriedIndividual = fhirController.queryIndividualByIdentifier(IdentifierType.DENID,
				individual.getIdentifier(IdentifierType.DENID));
		fhirController.close();
		String cpnString = queriedIndividual.getIdentifier(IdentifierType.CPN);
		String cpnNum = UpdateSimpleHelper.getRegIdString("CPN", cpnString);
		// step1
		SearchProviderResultsFragment searchResults = searchProviderPage
				.searchByRegistryIdentifier(IdentifierType.CPN.name(), cpnNum);
		assertTrue(searchResults.grabResultsRowCount() > 0);
	}
	
	// Search - Alphabetical Sorting
	@Test(groups = { "SearchProvider" })
	public void testAlphabeticalSorting() {
		
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());
		int roandomNumber=getRandomNumber(3,5);
		for(int i=0;i<roandomNumber;i++) {
			IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.DEN);			
			MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
			  Map<String,String> individualAddress = individual.getAddressList().get(0);
		        individualAddress.put("line1", String.valueOf(getRandomNumber(2,500))+" Oak Avenue");
		        individualAddress.put("city", "Custom City");
		        individual.setAddressList(List.of(individualAddress));
			individual = fhirController.submitIndividual(individual);
		}
		// test
		SearchProviderResultsFragment searchResults = searchProvider.searchByCriteria(IndividualRoleType.DEN.name(), null, null, null,
				"Custom City", null, null);
		assertTrue(searchResults.grabResultsRowCount() > 1, "search result has too less rows");
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < searchResults.grabResultsRowCount(); i++) {
			List<String> resultRow = searchResults.grabResultsRow(i);
			String name = resultRow.getFirst();
			if (name.contains("Link to View")) {
				continue;
			} else {
				names.add(name);
			}
		}
		assertTrue(names.size() > 1, "search result has too less sortable rows");
		assertTrue(Ordering.natural().isOrdered(names), "Search Results is not Alphabetically sorted");
		
		// FHIR
		OrganizationBuilderFactory organizationFactory = new OrganizationBuilderFactory(OrganizationDataGenerator.getInstance());
		for (int i = 0; i < roandomNumber; i++) {
			OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG);
			MaintainOrgBuilder org = organizationFactory.build(orgConfig);
			Map<String, String> address = org.getAddressList().get(0);
			address.put("line1", String.valueOf(getRandomNumber(2,500))+" Main St");
			address.put("city", "Sample City");
			org.setAddressList(List.of(address));
			org = fhirController.submitOrganization(org);
		}
		fhirController.close();
		//test
		searchResults = searchProvider.searchForOrganization(OrgRoleType.ORG.name(), null, null, null, "Sample City");
		assertTrue(searchResults.grabResultsRowCount() > 1, "search result has too less rows");
		names = new ArrayList<String>();
		for (int i = 0; i < searchResults.grabResultsRowCount(); i++) {
			List<String> resultRow = searchResults.grabResultsRow(i);
			String name = resultRow.getFirst();
			if (name.contains("Link to View")) {
				continue;
			} else {
				names.add(name);
			}
		}
		assertTrue(names.size() > 1, "search result has too less sortable rows");
		assertTrue(Ordering.natural().isOrdered(names), "Search Results is not Alphabetically sorted");

	}

	// Search - Individual Provider
	@Test(groups = { "SearchProvider" })
	public void testSearchIndividualProvider() {
		final String errorMissingData = errorList.getString("errorMissingData");
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());

		IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.DEN).withExpertise(1);
		MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
		individual = fhirController.submitIndividual(individual);
		MaintainIndividualBuilder queriedIndividual = fhirController.queryIndividualByIdentifier(IdentifierType.DENID,
				individual.getIdentifier(IdentifierType.DENID));
		fhirController.close();
		IndividualRoleType roleType = queriedIndividual.getRoleType();
		String surname = queriedIndividual.getFamilyName();
		String firstname = queriedIndividual.getNames()[0];
		Map<String, String> address = queriedIndividual.getAddressList().get(0);
		String city = address.get("city");
		List<Map<String, String>> expertisesList = queriedIndividual.getExpertiseList();
		String expertise = expertisesList.get(0).get("code");
		// step 1
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByCriteria(roleType.name(), firstname,
				surname, null, city, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// step 2
		searchResults = searchProviderPage.searchByCriteria(roleType.name(), null, null, null, null, null, null);
		String errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg.equals(errorMissingData), "Expected error message not found");
		searchProviderPage.clearRoleType();
		// step 3
		searchResults = searchProviderPage.searchByCriteria(null,null,surname, null,null, null, null);
		errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg.equals(errorMissingData), "Expected error message not found");
		// step 4
		searchResults = searchProviderPage.searchByCriteria(null, firstname,null, null, null, null, null);
		errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg.equals(errorMissingData), "Expected error message not found");
		// step 5
		searchResults = searchProviderPage.searchByCriteria(null, null, null, null, city, null, null);
		errMsg = searchProviderPage.grabPageErrorMessage();
		//search with "City Only" will return providers in that city 
		//assertTrue(errMsg.equals(errorMissingData), "Expected error message not found");
		// step 6
		searchResults = searchProviderPage.searchByCriteria(null, null, null, "M", null, null, null);
		errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg.equals(errorMissingData), "Expected error message not found");
		searchProviderPage.clearGender();
		// step 7
		searchResults = searchProviderPage.searchByCriteria(null, null, null, null, null, null, null,
				List.of("DPH"), null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		searchProviderPage.clearExprtise(List.of("DPH"));
		// step 8
		searchResults = searchProviderPage.searchByCriteria(null, null, null, null, null, null, null, null,
				List.of("ENG"));
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
	}

	// Search - Minimum Data
	@Test(groups = { "SearchProvider" })
	public void testSearchMinimumData() {
		final String errorMsgProiderID=errorList.getString("errorMsgProiderID");
		final String errorMsgRegIdValue=errorList.getString("errorMsgRegIdValue");
		final String errorMsgIdType=errorList.getString("errorMsgIdType");
		final String errorMsgRegIdType=errorList.getString("errorMsgRegIdType");
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());

		IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.DEN);
		MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
		individual = fhirController.submitIndividual(individual);
		MaintainIndividualBuilder queriedIndividual = fhirController.queryIndividualByIdentifier(IdentifierType.DENID,
				individual.getIdentifier(IdentifierType.DENID));
		fhirController.close();
		String providerId = queriedIndividual.getIdentifier(IdentifierType.DENID);
		String cpnString = queriedIndividual.getIdentifier(IdentifierType.CPN);
		String cpnNum = UpdateSimpleHelper.getRegIdString("CPN", cpnString);
		// step1
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByIdentifier(IdentifierType.DENID.name(),
				null, false);
		String errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg.equals(errorMsgProiderID), "Expected error message not found");
		// step2
		searchResults = searchProviderPage.searchByIdentifier(IdentifierType.DENID.name(), providerId);
		assertTrue(searchResults.grabResultsRowCount() > 0);
		// step3
		searchResults = searchProviderPage.searchByRegistryIdentifier(IdentifierType.CPN.name(), null, false);
		errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg.equals(errorMsgRegIdValue), "Expected error message not found");
		// step4
		searchResults = searchProviderPage.searchByRegistryIdentifier(IdentifierType.CPN.name(), cpnNum);
		assertTrue(searchResults.grabResultsRowCount() > 0);
		// step5
		searchResults = searchProviderPage.searchByIdentifier("Select One", null, false);
		errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg.contains(errorMsgIdType) && errMsg.contains(errorMsgProiderID),
				"Expected error messages not found");
		// step6
		searchResults = searchProviderPage.searchByRegistryIdentifier("Select One", null, false);
		errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg.contains(errorMsgRegIdType) && errMsg.contains(errorMsgRegIdValue),
				"Expected error messages not found");

	}

	// Search - Organization Provider
	@Test(groups = { "SearchProvider" })
	public void testSearchOrganizationProvider() {
		final String errorEntryError=errorList.getString("errorEntryError");
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG).withAlias();
		MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);
		MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				org.getIdentifier(IdentifierType.IPC));
		fhirController.close();
		OrgRoleType roleType = orgQueried.getRoleType();
		String name = orgQueried.getName();
		Map<String, String> address = orgQueried.getAddressList().get(0);
		String city = address.get("city");
		String addressline1 = address.get("line1");
		String desp = orgQueried.getAlias();

		// Step 1
		SearchProviderResultsFragment searchResults = searchProviderPage.searchForOrganization(roleType.name(), name,
				null, addressline1, city);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// Step 2
		searchResults = searchProviderPage.searchForOrganization(roleType.name(), null, null, null, null);
		String errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg.equals(errorEntryError), "Expected error message not found");
		// Step 3
		searchResults = searchProviderPage.searchForOrganization(roleType.name(), name, null, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");

		// Step 4: not able to only search by name. Role type has default value "ORG"
		// Step 5: description not support FHIR
		searchResults = searchProviderPage.searchForOrganization(OrgRoleType.ORG.name(), null, desp, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// Step 6
		searchResults = searchProviderPage.searchForOrganization(roleType.name(), null, null, addressline1, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
	}


	// Search - Search Results Limit
	@Test(groups = { "SearchProvider" })
	public void testSearchResultsLimit() {
	    final String warningMaxResult = warningList.getString("maximumResults");
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());

		for (int i = 0; i < SEARCH_PROVIDER_MAX_RESULTS+1; i++) {
			IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.MD);
			MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
			Map<String, String> individualAddress = individual.getAddressList().get(0);
			individualAddress.put("line1", String.valueOf(getRandomNumber(2, 500)) + " Main Avenue");
			individualAddress.put("city", "Custom City");
			individual.setAddressList(List.of(individualAddress));
			individual = fhirController.submitIndividual(individual);
		}
		// test
		
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByCriteria(IndividualRoleType.MD.name(), null, null, null,
				"Custom City", null, null);
		assertTrue(searchResults.grabResultsRowCount() == SEARCH_PROVIDER_MAX_RESULTS);
		String errMsg = searchProviderPage.grabWarningErrorMessage();
		assertTrue(errMsg.equals(warningMaxResult), "Expected warning message not found");
		// FHIR
		OrganizationBuilderFactory organizationFactory = new OrganizationBuilderFactory(
				OrganizationDataGenerator.getInstance());
		for (int i = 0; i < SEARCH_PROVIDER_MAX_RESULTS+1; i++) {
			OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG);
			MaintainOrgBuilder org = organizationFactory.build(orgConfig);
			Map<String, String> address = org.getAddressList().get(0);
			address.put("line1", String.valueOf(getRandomNumber(2, 500)) + " Oak St");
			address.put("city", "Sample City");
			org.setAddressList(List.of(address));
			org = fhirController.submitOrganization(org);
		}
		fhirController.close();
		// test
		searchResults = searchProviderPage.searchForOrganization(OrgRoleType.ORG.name(), null, null, null,"Sample City");
		assertTrue(searchResults.grabResultsRowCount() == SEARCH_PROVIDER_MAX_RESULTS);
		assertTrue(errMsg.equals(warningMaxResult), "Expected warning message not found");
	}

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
	@Test(groups = { "SearchProvider" })
	public void testSearchHDS() {
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.HDS).withAlias();
		MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);
		MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				org.getIdentifier(IdentifierType.IPC));
		fhirController.close();
		HdsType hdsType = orgQueried.getHdsType();
		String name = orgQueried.getName();
		Map<String, String> address = orgQueried.getAddressList().get(0);
		String city = address.get("city");
		String addressline1 = address.get("line1");
		String desp = orgQueried.getAlias();
		// test1
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		SearchProviderResultsFragment searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), name,
				desp, city, addressline1);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// test2
		searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), null, null, null, null);
		String errMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(errMsg
				.contains("The following fields must be supplied: 'Name or Description or Address Line 1 or City'"));
		// test3
		searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), name, desp, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// test4
		searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), null, null, city, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// test5
		searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), null, desp, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// test 6
		searchResults = searchProviderPage.searchHDSOrganization(hdsType.name(), null, null, null, addressline1);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");

	}
	
	/**
	 * get Random Number
	 * @param min
	 * @param max
	 * @return
	 */
	private int getRandomNumber(int min, int max) {
		
	        // Create a Random object
	        Random random = new Random();

	        // Generate the random number
	        // nextInt((max - min) + 1) generates a number between 0 and 4
	        // Adding min (2) shifts the range to be between 2 and 6 (exclusive of 6)
	        int randomNumber = random.nextInt((max - min) + 1) + min;
			return randomNumber;

	}

}
