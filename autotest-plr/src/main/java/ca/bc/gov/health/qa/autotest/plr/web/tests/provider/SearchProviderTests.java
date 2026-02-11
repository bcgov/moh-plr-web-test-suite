package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Ordering;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
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
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.query.IndividualQueryCriteriaParams;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.HdsType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.query.OrgQueryCriteriaParams;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.components.DropDownMenu;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderCriteriaFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderIdFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.SearchProviderResultsFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateOrganizationPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderIdentifierTypeConsumerOptions;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderIdentifierTypeOptions;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderIdentifierTypeSecondaryOptions;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderRoleTypeConsumerOptions;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderRoleTypeOptions;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderRoleTypeSecondaryOptions;
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
		//TODO: create provider and ord

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
		Map<String, String> address = queriedIndividual.getAddressList().getFirst();
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
        assertEquals(lowerCaseSearch, upperCaseSearch, "The search results are not equal");
        assertEquals(lowerCaseSearch, mixedCaseSearch, "The search results are not equal");

		// FHIR-organization
		fhirController = new FHIRController(UserType.ADMIN);
		OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG).withAlias();
		MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);
		MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC,
				org.getIdentifier(IdentifierType.IPC));
		fhirController.close();
		String orgName = orgQueried.getName();
		address = orgQueried.getAddressList().getFirst();
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
        assertEquals(orgUpperCaseSearch, orgMixedCaseSearch, "The search results are not equal");
        assertEquals(orgLowerCaseSearch, orgUpperCaseSearch, "The search results are not equal");
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
		Map<String, String> address = queriedIndividual.getAddressList().getFirst();
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
		address = orgQueried.getAddressList().getFirst();
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
		IndividualQueryCriteriaParams param= new IndividualQueryCriteriaParams().setAddressCity("Custom City").setRoleType(IndividualRoleType.DEN) ;
		List<MaintainIndividualBuilder> providerList = fhirController.queryIndividualByCriteria(param);
		if(providerList.size()<5) {
			IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
					IndividualDataGenerator.getInstance());
			int roandomNumber = UpdateSimpleHelper.getRandomNumber(3, 5);
			for (int i = 0; i < roandomNumber; i++) {
				IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.DEN);
				MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
				Map<String, String> individualAddress = individual.getAddressList().getFirst();
				individualAddress.put("line1", UpdateSimpleHelper.getRandomNumber(2, 500) + " Oak Avenue");
				individualAddress.put("city", "Custom City");
				individual.setAddressList(List.of(individualAddress));
				individual = fhirController.submitIndividual(individual);
			}
		}
		// test
		SearchProviderResultsFragment searchResults = searchProvider.searchByCriteria(IndividualRoleType.DEN.name(), null, null, null,
				"Custom City", null, null);
		assertTrue(searchResults.grabResultsRowCount() > 1, "search result has too less rows");
		List<String> names = new ArrayList<>();
		for (int i = 0; i < searchResults.grabResultsRowCount(); i++) {
			List<String> resultRow = searchResults.grabResultsRow(i);
			String name = resultRow.getFirst();
			if (!name.contains("Link to View")) {
				names.add(name);
			}
		}
		assertTrue(names.size() > 1, "search result has too less sortable rows");
		assertTrue(Ordering.natural().isOrdered(names), "Search Results is not Alphabetically sorted");

		// FHIR
		OrgQueryCriteriaParams paramOrg= new OrgQueryCriteriaParams().setAddressCity("Sample City").setRoleType(OrgRoleType.ORG) ;
		List<MaintainOrgBuilder> orgList = fhirController.queryOrganizationByCriteria(paramOrg);
		if(orgList.size()<5) {
			OrganizationBuilderFactory organizationFactory = new OrganizationBuilderFactory(OrganizationDataGenerator.getInstance());
			int roandomNumber = UpdateSimpleHelper.getRandomNumber(3, 5);
			for (int i = 0; i < roandomNumber; i++) {
				OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG);
				MaintainOrgBuilder org = organizationFactory.build(orgConfig);
				Map<String, String> address = org.getAddressList().getFirst();
				address.put("line1", UpdateSimpleHelper.getRandomNumber(2,500) + " Main St");
				address.put("city", "Sample City");
				org.setAddressList(List.of(address));
				org = fhirController.submitOrganization(org);
				}
		}
		fhirController.close();
		//test
		searchResults = searchProvider.searchForOrganization(OrgRoleType.ORG.name(), null, null, null, "Sample City");
		assertTrue(searchResults.grabResultsRowCount() > 1, "search result has too less rows");
		names = new ArrayList<>();
		for (int i = 0; i < searchResults.grabResultsRowCount(); i++) {
			List<String> resultRow = searchResults.grabResultsRow(i);
			String name = resultRow.getFirst();
			if (!name.contains("Link to View")) {
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
		Map<String, String> address = queriedIndividual.getAddressList().getFirst();
		String city = address.get("city");
		List<Map<String, String>> expertisesList = queriedIndividual.getExpertiseList();
		String expertise = expertisesList.getFirst().get("code");
		// step 1
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByCriteria(roleType.name(), firstname,
				surname, null, city, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// step 2
		searchResults = searchProviderPage.searchByCriteria(roleType.name(), null, null, null, null, null, null);
		String errMsg = searchProviderPage.grabPageErrorMessage();
        assertEquals(errorMissingData, errMsg, "Expected error message not found");
		searchProviderPage.clearRoleType();
		// step 3
		searchResults = searchProviderPage.searchByCriteria(null,null,surname, null,null, null, null);
		errMsg = searchProviderPage.grabPageErrorMessage();
        assertEquals(errorMissingData, errMsg, "Expected error message not found");
		// step 4
		searchResults = searchProviderPage.searchByCriteria(null, firstname,null, null, null, null, null);
		errMsg = searchProviderPage.grabPageErrorMessage();
        assertEquals(errorMissingData, errMsg, "Expected error message not found");
		// step 5
		searchResults = searchProviderPage.searchByCriteria(null, null, null, null, city, null, null);
		errMsg = searchProviderPage.grabPageErrorMessage();
		//search with "City Only" will return providers in that city 
		//assertTrue(errMsg.equals(errorMissingData), "Expected error message not found");
		// step 6
		searchResults = searchProviderPage.searchByCriteria(null, null, null, "M", null, null, null);
		errMsg = searchProviderPage.grabPageErrorMessage();
        assertEquals(errorMissingData, errMsg, "Expected error message not found");
		searchProviderPage.clearGender();
		// step 7
		searchResults = searchProviderPage.searchByCriteria(null, null, null, null, null, null, null,
				List.of("DPH"), null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		searchProviderPage.clearExpertise(List.of("DPH"));
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
        assertEquals(errorMsgProiderID, errMsg, "Expected error message not found");
		// step2
		searchResults = searchProviderPage.searchByIdentifier(IdentifierType.DENID.name(), providerId);
		assertTrue(searchResults.grabResultsRowCount() > 0);
		// step3
		searchResults = searchProviderPage.searchByRegistryIdentifier(IdentifierType.CPN.name(), null, false);
		errMsg = searchProviderPage.grabPageErrorMessage();
        assertEquals(errorMsgRegIdValue, errMsg, "Expected error message not found");
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
		Map<String, String> address = orgQueried.getAddressList().getFirst();
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
        assertEquals(errorEntryError, errMsg, "Expected error message not found");
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
		// search to see if have enough test data
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualQueryCriteriaParams param= new IndividualQueryCriteriaParams().setAddressCity("Custom City").setRoleType(IndividualRoleType.MD) ;
		List<MaintainIndividualBuilder> providerList = fhirController.queryIndividualByCriteria(param);
		if (providerList.size() <= SEARCH_PROVIDER_MAX_RESULTS) {
			// create providers for testing
			IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
					IndividualDataGenerator.getInstance());
			for (int i = providerList.size(); i < SEARCH_PROVIDER_MAX_RESULTS + 1; i++) {
				IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.MD);
				MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
				Map<String, String> individualAddress = individual.getAddressList().getFirst();
				individualAddress.put("line1", UpdateSimpleHelper.getRandomNumber(2, 500) + " Main Avenue");
				individualAddress.put("city", "Custom City");
				individual.setAddressList(List.of(individualAddress));
				individual = fhirController.submitIndividual(individual);
			}
		}
		// test
		
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByCriteria(IndividualRoleType.MD.name(), null, null, null,
				"Custom City", null, null);
        assertEquals(searchResults.grabResultsRowCount(), SEARCH_PROVIDER_MAX_RESULTS);
		String errMsg = searchProviderPage.grabWarningErrorMessage();
        assertEquals(warningMaxResult, errMsg, "Expected warning message not found");
		// FHIR
        OrgQueryCriteriaParams paramOrg = new OrgQueryCriteriaParams().setAddressCity("Sample City").setRoleType(OrgRoleType.ORG);
        List<MaintainOrgBuilder> orgList = fhirController.queryOrganizationByCriteria(paramOrg);
        
        if (orgList.size() <= SEARCH_PROVIDER_MAX_RESULTS) {
        	OrganizationBuilderFactory organizationFactory = new OrganizationBuilderFactory(
				OrganizationDataGenerator.getInstance());
        	for (int i = providerList.size(); i < SEARCH_PROVIDER_MAX_RESULTS+1; i++) {
        		OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG);
        		MaintainOrgBuilder org = organizationFactory.build(orgConfig);
        		Map<String, String> address = org.getAddressList().getFirst();
        		address.put("line1", UpdateSimpleHelper.getRandomNumber(2, 500) + " Oak St");
        		address.put("city", "Sample City");
        		org.setAddressList(List.of(address));
        		org = fhirController.submitOrganization(org);
        		}
        }
		fhirController.close();
		// test
		searchResults = searchProviderPage.searchForOrganization(OrgRoleType.ORG.name(), null, null, null,"Sample City");
        assertEquals(searchResults.grabResultsRowCount(), SEARCH_PROVIDER_MAX_RESULTS);
        assertEquals(warningMaxResult, errMsg, "Expected warning message not found");
	}

// 	Search - Zero Results
	@Test(groups = { "SearchProvider" })
	public void testSearchZeroResults() {

		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProvider = workflow.getPlrWebAccessActions().openSearchProvider();
		SearchProviderResultsFragment searchResults = searchProvider.searchByIdentifier("IPC", "ABC.123");
		assertEquals(searchResults.grabEmptyResultsMessage(), NORECORDFOUND);
		searchResults = searchProvider.searchByRegistryIdentifier("CPN", "DEF.4567");
		assertEquals(searchResults.grabEmptyResultsMessage(), NORECORDFOUND);
		searchResults = searchProvider.searchByCriteria("MD", "Nonexistent", "Provider", "M", "Victoria", "TERMINATED",
				"RET", List.of("AMD1 ", "AMD49 "), List.of("A01 ", "A09 "));
		assertEquals(searchResults.grabEmptyResultsMessage(), NORECORDFOUND);
		searchResults = searchProvider.searchForOrganization(OrgRoleType.ORG.name(), "Nonexistent", "Organization", "123 Some Street",
				"Victoria");
		assertEquals(searchResults.grabEmptyResultsMessage(), NORECORDFOUND);

	}

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
		Map<String, String> address = orgQueried.getAddressList().getFirst();
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
	
	
	
	//Search Rules: 22 
	@Test(groups = { "SearchProvider" })
	public void testSearchRules() {
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		
		String errorMsgName = errorList.getString("errorMsgName");
		String errorMsg7006 = errorList.getString("errorMsg7006");
		// FHIR
		// search to see if have enough test data
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualQueryCriteriaParams param = new IndividualQueryCriteriaParams().setAddressCity("Custom City")
				.setRoleType(IndividualRoleType.DEN);
		List<MaintainIndividualBuilder> providerList = fhirController.queryIndividualByCriteria(param);
		MaintainIndividualBuilder provider=providerList.get(0);
		String lastname = provider.getFamilyName();
		String firstname = provider.getNames()[0];
		String providerId=provider.getIdentifier(IdentifierType.DENID);
		String city = provider.getAddressList().getFirst().get("city");
		//String addressline1 = provider.getAddressList().getFirst().get("line1");
		OrgQueryCriteriaParams orgParam = new OrgQueryCriteriaParams().setAddressCity("Sample City").setRoleType(OrgRoleType.ORG);
		List<MaintainOrgBuilder> organizationList = fhirController.queryOrganizationByCriteria(orgParam );
		MaintainOrgBuilder organization=organizationList.get(0);
		String orgName=organization.getName();
		String orgCity=organization.getAddressList().get(0).get("city");
		String orgAddressline1 = organization.getAddressList().getFirst().get("line1");
		fhirController.close();
		//test1
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), firstname+"*", lastname, null, null, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		//test2
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), firstname, lastname+"*", null, null, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		//test3
		searchResults = searchProviderPage.searchByCriteria(IndividualRoleType.DEN.name(), firstname + "*" + "*",
				lastname, null, null, null, null);
		String pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("Only one wildcard (*) is allowed"), " Expect error message  not found");
		//test4
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), firstname, lastname+ "*" + "*", null, null, null, null);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("Only one wildcard (*) is allowed"), " Expect error message  not found");
		//test5
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(),  "*"+firstname,lastname, null, null, null, null);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("The wildcard (*) must trail all search characters")," Expect error message  not found");
		//test6
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), firstname, "*"+lastname, null, null, null, null);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("The wildcard (*) must trail all search characters")," Expect error message  not found");
		//test7
		searchResults = searchProviderPage.searchByIdentifier(IdentifierType.DENID.name(), providerId+"*",false);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains(errorMsg7006)," Expect error message  not found");
		//test8
		searchResults = searchProviderPage.searchByRegistryIdentifier(IdentifierType.CPN.name(), "1234*", false);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains(errorMsg7006)," Expect error message  not found");
		//test9
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), firstname, lastname, null, city+"*", null, null);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("Only the First Name and Last Name fields are allowed to use the wildcard (*)"),
				" Expect error message  not found");
		//test10
		searchResults = searchProviderPage.searchForOrganization
				(OrgRoleType.ORG.name(), orgName+"*", null, null, orgCity);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		//test11
		searchResults = searchProviderPage.searchForOrganization
				(OrgRoleType.ORG.name(), orgName, null, null, orgCity+"*");
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("Only the Name field is allowed to use the wildcard (*)"),
				" Expect error message  not found");
		//test12
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), firstname.toLowerCase(), lastname.toLowerCase(), null, city.toLowerCase(), null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		//test13
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), firstname.toLowerCase(), lastname.toLowerCase(), null, city.toUpperCase(), null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		
		//test14
		searchResults = searchProviderPage.searchForOrganization
				(OrgRoleType.ORG.name(), orgName.toLowerCase(), null, orgAddressline1.toLowerCase(), orgCity.toLowerCase());
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		//test15
		searchResults = searchProviderPage.searchForOrganization
				(OrgRoleType.ORG.name(), orgName.toUpperCase(), null,  orgAddressline1.toUpperCase(), orgCity.toUpperCase());
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		//test16
		searchResults = searchProviderPage.searchForOrganization
				(OrgRoleType.ORG.name(), orgName+"**", null, null, orgCity);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("Only one wildcard (*) is allowed in a field."),
				" Expect error message  not found");
		//test17
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), firstname, lastname, null, null, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		//test18
		searchResults = searchProviderPage.searchForOrganization
				(OrgRoleType.ORG.name(), orgName+"**", null, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		//test19
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), "*", lastname , null, city, null, null);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("The wildcard (*) must be preceded by at least one letter"),
				" Expect error message  not found");
		//test20
		searchResults = searchProviderPage.searchByCriteria
				(IndividualRoleType.DEN.name(), firstname, "*", null, city, null, null);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("The wildcard (*) must be preceded by at least one letter"),
				" Expect error message  not found");
		//test21
		searchResults = searchProviderPage.searchForOrganization
				(OrgRoleType.ORG.name(), "*", null, null, orgCity);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains("The wildcard (*) must be preceded by at least one letter"),
				" Expect error message  not found");
		//test22
		searchResults = searchProviderPage.searchForOrganization
				(OrgRoleType.ORG.name(), null, null, null, orgCity);
		pageerroMsg = searchProviderPage.grabPageErrorMessage();
		assertTrue(pageerroMsg.contains(errorMsgName),
				" Expect error message  not found");
	}

	//Search by ID
	@Test(groups = { "SearchProvider" })
	public void testSearchById() {
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
		String cpnNum = UpdateSimpleHelper.getRegIdString(IdentifierType.CPN.name(), queriedIndividual.getIdentifier(IdentifierType.CPN));
		// Step1
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByIdentifier(IdentifierType.DENID.name(),
				providerId);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// Step 2
		searchResults = searchProviderPage.searchByRegistryIdentifier(IdentifierType.CPN.name(), cpnNum);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		
	}

	//Searching With Provider History (create inactive provider from HFIR)
	@Test(groups = { "SearchProvider" })
	public void testSearchWithProviderHistory() {
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR provider
		FHIRController fhirController = new FHIRController(UserType.ADMIN);
		IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.DEN);
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());
		MaintainIndividualBuilder provider = individualFactory.build(individualConfig);
		provider = fhirController.submitIndividual(provider);
		
		//test 1 name
		String lastname = provider.getFamilyName();
		String firstname = provider.getNames()[0];
		String city = provider.getAddressList().get(0).get("city");
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByCriteria(IndividualRoleType.DEN.name(), 
				firstname, lastname, null, city, null, null);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		provider.familyName("CustomLastName");
		provider.setNames("CustomFirstName", "CustomMiddleName", null);
		
		provider.confidentiality(null);
		provider = fhirController.submitIndividual(provider);
		searchResults = searchProviderPage.searchByCriteria(IndividualRoleType.DEN.name(), 
				firstname, lastname, null, city, null, null);
		String resultMsg = searchResults.grabEmptyResultsMessage();
		assertTrue(resultMsg.equals(NORECORDFOUND));
		//test 2 identifier
		
		String providerId = provider.getIdentifier(IdentifierType.DENID);
		searchResults = searchProviderPage.searchByIdentifier(IdentifierType.DENID.name(), providerId);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		// update id through a web updating 
		updateProviderIdentifier(provider, 1);
		//resume the testing  
		searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		searchProviderPage.refreshPage();
		searchResults=searchProviderPage.searchByIdentifier(IdentifierType.DENID.name(), providerId);
		resultMsg = searchResults.grabEmptyResultsMessage();
		assertTrue(resultMsg.equals(NORECORDFOUND));
		//FHIR organization
		OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG).withAlias();
		OrganizationBuilderFactory organizationFactory = new OrganizationBuilderFactory(
				OrganizationDataGenerator.getInstance());
		MaintainOrgBuilder org = organizationFactory.build(orgConfig);
		org = fhirController.submitOrganization(org);
		String orgName = org.getName();
		String orgDesc = org.getAlias();
		String orgCity=org.getAddressList().getFirst().get("city");
		String orgAddressLine1 = org.getAddressList().getFirst().get("line1");
		//test 3 organization name
		searchProviderPage.searchForOrganization(OrgRoleType.ORG.name(), orgName, orgDesc, orgAddressLine1, orgCity);
		assertTrue(searchResults.grabResultsRowCount() > 0, "search result has too less rows");
		org.name(UpdateSimpleHelper.generateAlphabetString(10));
		org = fhirController.submitOrganization(org);
		searchProviderPage.searchForOrganization(OrgRoleType.ORG.name(), orgName, orgDesc, orgAddressLine1, orgCity);
		resultMsg = searchResults.grabEmptyResultsMessage();
		assertTrue(resultMsg.equals(NORECORDFOUND));
		fhirController.close();
	
	}
	
	//Web UI - Filtering Provider Query Role Type For Query test 1-3 for 4 user type, permission for role types)
	@Test(dataProvider = "facilityTestUserTypes", dataProviderClass = InjectableData.class,groups = { "SearchProvider" })
	public void testFilteringProviderQueryRoleType(UserType userType) {
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		SearchProviderCriteriaFragment fragment = searchProviderPage.expandSearchCriteria(true);
		DropDownMenu menu = fragment.getRoleTypeMenu();
		menu.expandItemPanel(true);
		List<String> providerRoleTypeList = menu.grabItemList();
		List<String> providerOptions =getProviderRoleTypeOptions(userType);
		assertTrue( UpdateSimpleHelper.haveSameElements(providerRoleTypeList, providerOptions) );
		workflowManager_.logoutAndClose(userType);		
	}

	//Web UI - Filtering Provider Identifier Type For Query  
	@Test(dataProvider = "facilityTestUserTypes", dataProviderClass = InjectableData.class,groups = { "SearchProvider" })
	public void testFilteringProviderIdentifierType(UserType userType) {
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		TestHelper.logIn(workflowManager_, userType);
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		SearchProviderIdFragment fragment = searchProviderPage.expandSearchIdentifier(true);
		DropDownMenu menu= fragment.getIdentifierTypeMenu();
		menu.expandItemPanel(true);
		List<String> providerIdentifierList = menu.grabItemList();
		List<String> providerIdentifierOptions= getProviderIdentifierTypeOptions(userType);
		assertTrue( UpdateSimpleHelper.haveSameElements(providerIdentifierList, providerIdentifierOptions));
		workflowManager_.logoutAndClose(userType);
	}

	private List<String> getProviderIdentifierTypeOptions(UserType userType) {
		List<String> providerIdentifierOptions = new ArrayList<String>();
		switch (userType) {
		case UserType.ADMIN:
			providerIdentifierOptions= Stream.of(ProviderIdentifierTypeOptions.values()).map(ProviderIdentifierTypeOptions::getText)
			.collect(Collectors.toList());			
			break;
		case UserType.PRIMARY:
			providerIdentifierOptions= Stream.of(ProviderIdentifierTypeOptions.values()).map(ProviderIdentifierTypeOptions::getText)
			.collect(Collectors.toList());
			break;
		case UserType.SECONDARY:
			providerIdentifierOptions= Stream.of(ProviderIdentifierTypeSecondaryOptions.values()).map(ProviderIdentifierTypeSecondaryOptions::getText)
			.collect(Collectors.toList());
			break;
		case UserType.CONSUMER:
			providerIdentifierOptions= Stream.of(ProviderIdentifierTypeConsumerOptions.values()).map(ProviderIdentifierTypeConsumerOptions::getText)
			.collect(Collectors.toList());
			break;
		default:
			
		}
		providerIdentifierOptions.add("Select One");
		return providerIdentifierOptions;
		
	}

	private List<String> getProviderRoleTypeOptions(UserType userType) {
		List<String> providerRoleTypeOptions = new ArrayList<String>();
		switch (userType) {
		case UserType.ADMIN:
			providerRoleTypeOptions = Stream.of(ProviderRoleTypeOptions.values()).map(ProviderRoleTypeOptions::getText)
			.collect(Collectors.toList());		
			break;
		case UserType.PRIMARY:
			providerRoleTypeOptions = Stream.of(ProviderRoleTypeOptions.values()).map(ProviderRoleTypeOptions::getText)
			.collect(Collectors.toList());
			break;
		case UserType.SECONDARY:
			providerRoleTypeOptions = Stream.of(ProviderRoleTypeSecondaryOptions.values()).map(ProviderRoleTypeSecondaryOptions::getText)
			.collect(Collectors.toList());
			break;
		case UserType.CONSUMER:
			 providerRoleTypeOptions = Stream.of(ProviderRoleTypeConsumerOptions.values()).map(ProviderRoleTypeConsumerOptions::getText)
			.collect(Collectors.toList());
			break;
		default:
			
		}
		providerRoleTypeOptions.add("Select One");
		return providerRoleTypeOptions;
		
	}
	
	
	private void updateProviderIdentifier(MaintainIndividualBuilder provider, int inxdex ) {
		// borrow organization identifier updating  
		UpdateOrganizationPage defaultOrgPage = TestHelper.viewByIdentifierAsUpdateOrg(provider.getIdentifier(IdentifierType.IPC), workflowManager_);
 		defaultOrgPage.updateIdentifierDataBlock(UpdateSimpleHelper.generateNumericString(10), EndReason.CORR, 1, false);
		
	}
	
	

}
