package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.getRandomNumber;
import static org.testng.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.query.IndividualQueryCriteriaParams;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.query.OrgQueryCriteriaParams;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.SearchProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
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
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

/** Tests class for the Search Provider page */
public class SearchProviderTests implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
	private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList,warningList;

	private static FHIRController fhirController;
    
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
	private void teardown() {
		workflowManager_.logoutAllAndClose();
		LOG.info("Done.");
	}

	@BeforeMethod
	private void before(Object[] parameters) {
		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
		if (!workflow.isLoggedIn()) {
			workflow.login().openPlr();
		}
	}

	@BeforeTest
	private void beforeTest() {
		fhirController = new FHIRController(UserType.ADMIN);
	}

	// Case Insensitive Search
	@Test(groups = { "SearchProvider" })
	public void testCaseInsensitiveSearch() {

		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
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
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());
		int roandomNumber=getRandomNumber(3,5);
		for(int i=0;i<roandomNumber;i++) {
			IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.DEN);			
			MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
			  Map<String,String> individualAddress = individual.getAddressList().getFirst();
		        individualAddress.put("line1", getRandomNumber(2,500) + " Oak Avenue");
		        individualAddress.put("city", "Custom City");
		        individual.setAddressList(List.of(individualAddress));
			individual = fhirController.submitIndividual(individual);
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
		OrganizationBuilderFactory organizationFactory = new OrganizationBuilderFactory(OrganizationDataGenerator.getInstance());
		for (int i = 0; i < roandomNumber; i++) {
			OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG);
			MaintainOrgBuilder org = organizationFactory.build(orgConfig);
			Map<String, String> address = org.getAddressList().getFirst();
			address.put("line1", getRandomNumber(2,500) + " Main St");
			address.put("city", "Sample City");
			org.setAddressList(List.of(address));
			org = fhirController.submitOrganization(org);
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

	// Search - Confidential Mask
	@Test(groups = { "SearchProvider" }, dataProvider = "indOrgBuilderTypes", dataProviderClass = InjectableData.class)
	public void testConfidentialMask(ProviderType providerType, MaintainRequestBuilder confidentialRecord)
	{
		final PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.SECONDARY);
		if (!workflow.isLoggedIn()) { workflow.login().openPlr(); }
		final SearchProviderActions actions = workflowManager_.getSelectedWorkflow().getSearchProviderActions();

		// FHIR Prep - if confidentialRecord is set use that record instead of creating anything new
		boolean isOrganization;
		MaintainIndividualBuilder confInd = null;
		MaintainOrgBuilder confOrg = null;
		if (confidentialRecord == null) {
			switch (providerType) {
				case BC_PRACTITIONER -> confInd = fhirController.createIndividual(
						new IndividualMaintainConfig(IndividualRoleType.MD).withConfidentiality());
				case ORGANIZATION -> confOrg = fhirController.createOrganization(
						new OrganizationMaintainConfig(OrgRoleType.ORG).withConfidentiality());
				default -> throw new IllegalStateException("Unsupported provider type " + providerType.name());
			}
			isOrganization = !Objects.isNull(confOrg);
		} else {
			isOrganization = confidentialRecord instanceof MaintainOrgBuilder;
			if (isOrganization) confOrg = (MaintainOrgBuilder) confidentialRecord;
			else confInd = (MaintainIndividualBuilder) confidentialRecord;
		}

		// Test Start
		SearchProviderPage provider = workflow.getPlrWebAccessActions().openSearchProvider();

		String identifier;
		if (isOrganization) identifier = confOrg.getIdentifier(IdentifierType.IPC);
		else identifier = confInd.getIdentifier(IdentifierType.IPC);

		SearchProviderResultsFragment results = provider.searchByIdentifier("IPC", identifier);
		List<String> resultInfo = results.grabResultsRow(0);
		assertEquals(resultInfo.getFirst(), "Link to View Provider", "Record name not confidentially masked");

		ViewProviderPage page = actions.openSearchResults(0);

		assertEquals(page.getViewHeader().grabViewTitle().split(" ")[0], "Confidential",
				"Record name in title not confidentially masked");

		// Verify confidential sections are masked
		for (ProviderSection section : ProviderSection.getProviderSectionSet(providerType))
		{
			switch (section)
			{
				case IDENTIFIERS, ROLE_TYPE:
					continue;
				case PRACTITIONER_NAMES, ORGANIZATION_NAMES:
					String nameField = section.equals(ProviderSection.ORGANIZATION_NAMES) ? "Name" : "Surname";
					String name = page.grabDataBlockContent(section, 0).get(nameField);
					assertEquals(name, "Confidential", "Surname not set as confidential");
					continue;
			}
			assertEquals(page.grabDataBlockCount(section), 0,
					"Confidential record data found in section: " + section.name());
		}
	}

	// Search - Confidential Record Attribute Search
	@Test(groups = { "SearchProvider" })
	public void testConfidentialRecordAttributeSearch()
	{
		final PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		final IndividualDataGenerator dataGen = IndividualDataGenerator.getInstance();

		// FHIR Prep (if needed)
		List<MaintainOrgBuilder> orgQuery = fhirController.queryOrganizationByCriteria(
				new OrgQueryCriteriaParams().setName("ConfidentialRecord"));

		if (orgQuery.isEmpty())
		{
			fhirController.createOrganization(new OrganizationMaintainConfig(OrgRoleType.ORG)
					.withName("ConfidentialRecord").withConfidentiality());
		}

		List<MaintainIndividualBuilder> indQuery = fhirController.queryIndividualByCriteria(
				new IndividualQueryCriteriaParams().setFamily("ConfidentialRecord").setExpertise("ENG"));

		if (indQuery.isEmpty())
		{
			MaintainIndividualBuilder ind = new IndividualBuilderFactory(dataGen)
					.build(new IndividualMaintainConfig(IndividualRoleType.MD).withConfidentiality())
					.familyName("ConfidentialRecord").addExpertise("ENG", dataGen.shortText());
			fhirController.submitIndividual(ind);
		}

		SearchProviderPage provider = workflow.getPlrWebAccessActions().openSearchProvider();

		// Search by Criteria
		provider.searchByCriteria(IndividualRoleType.MD.name(), null, "ConfidentialRecord",
				null, null, null, null,
				null, List.of("ENG"));
		assertEquals(warningList.get("confidentialRecordFound"), provider.grabWarningErrorMessage(),
				"Expected warning message not found");

		// Search by Organization
		provider.searchForOrganization(null, "ConfidentialRecord", null,
				null, null);
		assertEquals(warningList.get("confidentialRecordFound"), provider.grabWarningErrorMessage(),
				"Expected warning message not found");
	}

	// Search - Confidential Record ID Search
	@Test(groups = { "SearchProvider" })
	public void testConfidentialRecordIDSearch()
	{
		final PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();

		// FHIR Prep
		MaintainIndividualBuilder confidentialInd = fhirController.createIndividual(
				new IndividualMaintainConfig(IndividualRoleType.MD).withConfidentiality());

		SearchProviderPage provider = workflow.getPlrWebAccessActions().openSearchProvider();

		// Search by Identifier
		SearchProviderResultsFragment results = provider.searchByIdentifier("IPC",
				confidentialInd.getIdentifier(IdentifierType.IPC));
		List<String> resultInfo = results.grabResultsRow(0);
		assertTrue(Objects.nonNull(resultInfo),
				"Search by Identifier for confidential record was unsuccessful");

		// Search by Registry Identifier
		String ipcID = UpdateSimpleHelper.getRegIdString(IdentifierType.IPC.name(),
				confidentialInd.getIdentifier(IdentifierType.IPC));
		results = provider.searchByRegistryIdentifier("IPC", ipcID);
		resultInfo = results.grabResultsRow(0);
		assertTrue(Objects.nonNull(resultInfo),
				"Search by Registry Identifier for confidential record was unsuccessful");

		// Search by Criteria / Search by Organization
		testConfidentialRecordAttributeSearch();
	}

	// Search - Individual Provider
	@Test(groups = { "SearchProvider" })
	public void testSearchIndividualProvider() {
		final String errorMissingData = errorList.getString("errorMissingData");
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		SearchProviderPage searchProviderPage = workflow.getPlrWebAccessActions().openSearchProvider();
		// FHIR
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
		IndividualBuilderFactory individualFactory = new IndividualBuilderFactory(
				IndividualDataGenerator.getInstance());

		for (int i = 0; i < SEARCH_PROVIDER_MAX_RESULTS+1; i++) {
			IndividualMaintainConfig individualConfig = new IndividualMaintainConfig(IndividualRoleType.MD);
			MaintainIndividualBuilder individual = individualFactory.build(individualConfig);
			Map<String, String> individualAddress = individual.getAddressList().getFirst();
			individualAddress.put("line1", getRandomNumber(2, 500) + " Main Avenue");
			individualAddress.put("city", "Custom City");
			individual.setAddressList(List.of(individualAddress));
			individual = fhirController.submitIndividual(individual);
		}
		// test
		
		SearchProviderResultsFragment searchResults = searchProviderPage.searchByCriteria(IndividualRoleType.MD.name(), null, null, null,
				"Custom City", null, null);
        assertEquals(searchResults.grabResultsRowCount(), SEARCH_PROVIDER_MAX_RESULTS);
		String errMsg = searchProviderPage.grabWarningErrorMessage();
        assertEquals(warningMaxResult, errMsg, "Expected warning message not found");
		// FHIR
		OrganizationBuilderFactory organizationFactory = new OrganizationBuilderFactory(
				OrganizationDataGenerator.getInstance());
		for (int i = 0; i < SEARCH_PROVIDER_MAX_RESULTS+1; i++) {
			OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG);
			MaintainOrgBuilder org = organizationFactory.build(orgConfig);
			Map<String, String> address = org.getAddressList().getFirst();
			address.put("line1", getRandomNumber(2, 500) + " Oak St");
			address.put("city", "Sample City");
			org.setAddressList(List.of(address));
			org = fhirController.submitOrganization(org);
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
		searchResults = searchProvider.searchForOrganization("ORG", "Nonexistent", "Organization", "123 Some Street",
				"Victoria");
		assertEquals(searchResults.grabEmptyResultsMessage(), NORECORDFOUND);

	}

// 	Search by HDS is not in ALM yes, need to be added based on Legacy selenium
	@Test(groups = { "SearchProvider" })
	public void testSearchHDS() {
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
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

	// Viewing Permissions for Confidential Provider Records
	@Test(groups = { "SearchProvider" }, dataProvider = "indOrgTypes", dataProviderClass = InjectableData.class)
	public void testViewPermissionsConfidentialRecords(ProviderType providerType)
	{
		// FHIR Prep
		final FHIRController primaryController = new FHIRController(UserType.PRIMARY);
		MaintainIndividualBuilder confInd = null;
		MaintainOrgBuilder confOrg = null;
		switch (providerType)
		{
			case BC_PRACTITIONER -> confInd = primaryController.createIndividual(
					new IndividualMaintainConfig(IndividualRoleType.MD).withConfidentiality());
			case ORGANIZATION -> confOrg = primaryController.createOrganization(
					new OrganizationMaintainConfig(OrgRoleType.ORG).withConfidentiality());
			default -> throw new IllegalStateException("Unsupported provider type " + providerType.name());
		}
		boolean isOrganization = !Objects.isNull(confOrg);

		// Test Start (checking Reg-Admin access to anything and Primary access to its own confidential record)
		for (UserType userType : List.of(UserType.ADMIN, UserType.PRIMARY))
		{
			final PlrWebWorkflow workflow = workflowManager_.selectWorkflow(userType);
			if (!workflow.isLoggedIn()) { workflow.login().openPlr(); }
			final SearchProviderActions actions = workflowManager_.getSelectedWorkflow().getSearchProviderActions();

			SearchProviderPage provider = workflow.getPlrWebAccessActions().openSearchProvider();

			String identifier;
			if (isOrganization) identifier = confOrg.getIdentifier(IdentifierType.IPC);
			else identifier = confInd.getIdentifier(IdentifierType.IPC);

			SearchProviderResultsFragment results = provider.searchByIdentifier("IPC", identifier);
			List<String> resultInfo = results.grabResultsRow(0);
			assertNotEquals(resultInfo.getFirst(), "Link to View Provider",
					"Record name confidentially masked unexpectedly");

			ViewProviderPage page = actions.openSearchResults(0);

			assertNotEquals(page.getViewHeader().grabViewTitle().split(" ")[0], "Confidential",
					"Record name in title confidentially masked unexpectedly");

			for (ProviderSection section : ProviderSection.getProviderSectionSet(providerType))
			{
				if (!section.isRequired()) continue;
				if (userType.equals(UserType.PRIMARY) && section.equals(ProviderSection.REGISTRY_IDENTIFIERS)) continue;

				assertTrue(page.grabDataBlockCount(section) > 0,
						"Expected record data not found in section: " + section.name());
			}
		}

		// checking secondary
		testConfidentialMask(providerType, isOrganization ? confOrg : confInd);
	}
}
