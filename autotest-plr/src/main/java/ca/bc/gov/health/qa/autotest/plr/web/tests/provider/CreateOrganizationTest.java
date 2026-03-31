package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.HdsSubType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateOrganizationPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderAddressFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderIdFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.HdsType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.OrganizationProperties;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.OrganizationalProviderRoleType;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderIdentifierTypeOptions;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.StatusCodeOption;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.StatusReasonCodeOption;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

import static org.testng.Assert.*;

public class CreateOrganizationTest implements SimpleTest {
	private static final Logger LOG = ExecutionLogManager.getLogger();

	private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();


	private static final Config config_ = ConfigProvider.get().getConfig();
	private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
	private static JSONObject errorList, warningList;

	private CreateOrganizationTest() {

		try {
			errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
			warningList = new JSONObject(Files.readString(errorPath)).getJSONObject("warnings");
		} catch (IOException e) {
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

		PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
		if (!workflow.isLoggedIn()) {
			workflow.login().openPlr();
		}
	}

//	
//	Add Organization
	@Test(groups = { "CreateOrganization" })
	public void testAddOrganization() {
		MaintainOrgBuilder providerBuilder = getOrgBuilder();
		Map<String, String> orgAddress = providerBuilder.getAddressList().getFirst();
		String orgName = providerBuilder.getName();
		String orgDesc = providerBuilder.getAlias();
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		AddProviderPage page = workflow.getPlrWebAccessActions().openAddOrganization()
				.openProviderPage(ProviderType.ORGANIZATION);
		AddProviderIdFragment fragment = page.fillOrganizationIdentifier(OrganizationalProviderRoleType.ORG, null, null,
				"ORGID", UpdateSimpleHelper.generateNumericString(8));
		// assertTrue(fragment.isHdsTYpeDisplayed());
		page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);
		clickFirstNext(page);

		page.fillOrganizationName(orgName, orgDesc);
		clickSecondNext(page);

		AddProviderAddressFragment address = page.fillAddress("P", orgAddress.get("purpose"),
				List.of(orgAddress.get("line1"), "", ""), orgAddress.get("city"), "BC", "CA",
				orgAddress.get("postalCode"));
		page.fillPhone("250", UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(3));
		page.fillFax("250", UpdateSimpleHelper.generateNumericString(7));
		page.fillEmail(UpdateSimpleHelper.generateEmail());
		clickThirdNext(page, address);

		page.fillCredentials("BD", "Test", "5358", "TestInst", "Victoria", "CA", "BC", true, "2001");
		page.fillExpertise("ENG", "2500");

		ViewProviderPage viewPage = page.clickSubmitButton();

		LinkedHashMap<String, String> content = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0);
        assertEquals(orgName, content.get("Name"));
        assertEquals(orgDesc, content.get("Description"));
	}

//
//	Organization Provider Minimum Data Requirements- not applicable
//
//	Provider Role Types for Organization Providers
	@Test(groups = { "CreateOrganization" })
	public void testProviderRoleTypesforOrganizationProviders() {
		MaintainOrgBuilder providerBuilder = getOrgBuilder();
		Map<String, String> orgAddress = providerBuilder.getAddressList().get(0);
		String orgName = providerBuilder.getName();
		String orgDesc = providerBuilder.getAlias();
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();

		// Add org
		AddProviderPage page = workflow.getPlrWebAccessActions().openAddOrganization()
				.openProviderPage(ProviderType.ORGANIZATION);
		AddProviderIdFragment fragment = page.fillOrganizationIdentifier(OrganizationalProviderRoleType.ORG, null, null,
				"ORGID", UpdateSimpleHelper.generateNumericString(8));

		page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);
		clickFirstNext(page);

		page.fillOrganizationName(orgName, orgDesc);
		clickSecondNext(page);
		AddProviderAddressFragment address = page.fillAddress("P", orgAddress.get("purpose"),
				List.of(orgAddress.get("line1"), "", ""), orgAddress.get("city"), "BC", "CA",
				orgAddress.get("postalCode"));
		page.fillPhone("250", UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(3));
		page.fillFax("250", UpdateSimpleHelper.generateNumericString(7));
		page.fillEmail(UpdateSimpleHelper.generateEmail());
		clickThirdNext(page, address);
		ViewProviderPage viewPage = page.clickSubmitButton();
		LinkedHashMap<String, String> content = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0);
        assertEquals(orgName, content.get("Name"));
        assertEquals(orgDesc, content.get("Description"));

		// add business
		orgName = providerBuilder.getName() + UpdateSimpleHelper.generateNumericString(2);
		orgDesc = providerBuilder.getAlias() + UpdateSimpleHelper.generateNumericString(2);

		page = workflow.getPlrWebAccessActions().openAddOrganization().openProviderPage(ProviderType.ORGANIZATION);
		fragment = page.fillOrganizationIdentifier(OrganizationalProviderRoleType.BUSINESS, null, null, "ORGID",
				UpdateSimpleHelper.generateNumericString(8));

		page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);
		clickFirstNext(page);

		page.fillOrganizationName(orgName, orgDesc);
		clickSecondNext(page);
		address = page.fillAddress("P", orgAddress.get("purpose"), List.of(orgAddress.get("line1"), "", ""),
				orgAddress.get("city"), "BC", "CA", orgAddress.get("postalCode"));
		page.fillPhone("250", UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(3));
		page.fillFax("250", UpdateSimpleHelper.generateNumericString(7));
		page.fillEmail(UpdateSimpleHelper.generateEmail());
		clickThirdNext(page, address);
		viewPage = page.clickSubmitButton();
		content = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0);
        assertEquals(orgName, content.get("Name"));
        assertEquals(orgDesc, content.get("Description"));

		// add clinic
		orgName = providerBuilder.getName() + UpdateSimpleHelper.generateNumericString(2);
		orgDesc = providerBuilder.getAlias() + UpdateSimpleHelper.generateNumericString(2);

		page = workflow.getPlrWebAccessActions().openAddOrganization().openProviderPage(ProviderType.ORGANIZATION);
		fragment = page.fillOrganizationIdentifier(OrganizationalProviderRoleType.CLINIC, null, null, "ORGID",
				UpdateSimpleHelper.generateNumericString(8));

		page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);
		clickFirstNext(page);

		page.fillOrganizationName(orgName, orgDesc);
		clickSecondNext(page);
		address = page.fillAddress("P", orgAddress.get("purpose"), List.of(orgAddress.get("line1"), "", ""),
				orgAddress.get("city"), "BC", "CA", orgAddress.get("postalCode"));
		page.fillPhone("250", UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(3));
		page.fillFax("250", UpdateSimpleHelper.generateNumericString(7));
		page.fillEmail(UpdateSimpleHelper.generateEmail());
		clickThirdNext(page, address);
		viewPage = page.clickSubmitButton();
		content = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0);
        assertEquals(orgName, content.get("Name"));
        assertEquals(orgDesc, content.get("Description"));

		// add HDS
		viewPage = createHDSProvider(HdsType.CLINIC, HdsSubType.LNWIC);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		;
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("CLINIC"));
	}
//
//	Validate Organization name- PLR 608

//	Organization Name and Long Name Accepted Characters
	@Test(groups = { "CreateOrganization" })
	public void testOrganizationNameAcceptedCharacters() {
		
		String fullSetChars="&()+-./0123456789:ABCDEFGHIJKLMNOPQRSTUVWXYZ\\abcdefghijklmnopqrstuvwxyz";
		String invalidNams="$test";
		String errorInvalidChar7081=errorList.getString("errorInvalidChar7081");
		MaintainOrgBuilder providerBuilder = getOrgBuilder();
		Map<String, String> orgAddress = providerBuilder.getAddressList().get(0);
		String orgName = providerBuilder.getName();
		String orgDesc = providerBuilder.getAlias();
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddOrganization().openProviderPage(ProviderType.ORGANIZATION);
        

        page.fillOrganizationIdentifier(OrganizationalProviderRoleType.ORG, null, null, "ORGID", UpdateSimpleHelper.generateNumericString(6));
        page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);
        
        page.clickNext("Status", "");
        page.waitForAddProviderStep("Organization", true);
        
        page.fillOrganizationName(invalidNams, fullSetChars);
        page.clickNext("Organization");
        String errMsg=page.grabPageErrorMessage();
        assertEquals(errorInvalidChar7081, errMsg, "Expected error message not found");
       
 
        page.fillOrganizationName(orgName,fullSetChars);
        page.clickNext("Organization", "");
        page.waitForAddProviderStep("Address", true);
        
        
        AddProviderAddressFragment address = page.fillAddress("P", orgAddress.get("purpose"), 
        		List.of( orgAddress.get("line1"), "", ""),
        		orgAddress.get("city") , "BC", "CA",
        		orgAddress.get("postalCode"));
        page.fillPhone("250", UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(3));
        page.fillFax("250", UpdateSimpleHelper.generateNumericString(7));
        page.fillEmail(UpdateSimpleHelper.generateEmail());
        clickThirdNext(page,address);
        page.waitForAddProviderStep("Credential", true);

        
        ViewProviderPage viewPage = page.clickSubmitButton();
        LinkedHashMap<String, String> resultName = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0);
        assertEquals(resultName.get("Description"), fullSetChars, "Excepted acceptable charset not supported");
	}
	



//	Validate HDS Type
	@Test(groups = { "CreateOrganization" })
	public void testValidateHDSType() {
		
		ViewProviderPage viewPage=createHDSProvider(HdsType.CLINIC,HdsSubType.LNWIC,ProviderIdentifierTypeOptions.ORGID);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
	
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("CLINIC"));
		
		if (viewPage.grabDataBlockCount(ProviderSection.ORGANIZATION_PROPERTIES) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_PROPERTIES, 0);
		assertTrue(resultProperty.get("Property Type").contains("HDS Sub Type"));
		assertTrue(resultProperty.get("Property Value").contains("Walk In Medical Clinics"));
		//update hds sub type
	
		UpdateOrganizationPage updatePage=new UpdateOrganizationPage(workflowManager_.getSelectedWorkflow().getSeleniumSession(), 
				workflowManager_.getSelectedWorkflow().getURUri().resolve("/plr/ProviderDetails.xhtml"));
	
		updatePage.updateOrganizationPropertyDataBlock(OrganizationProperties.HDS_SUB_TYPE, HdsSubType.EDU.getText(),EndReason.CHG , 0, false);
		if (viewPage.grabDataBlockCount(ProviderSection.ORGANIZATION_PROPERTIES) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_PROPERTIES, 0);
		assertTrue(resultProperty.get("Property Type").contains("HDS Sub Type"));
		assertTrue(resultProperty.get("Property Value").contains("Education"));
		
		updatePage.updateOrganizationPropertyDataBlock(OrganizationProperties.HDS_SUB_TYPE, HdsSubType.LNWIC.getText(),EndReason.CORR , 0, false);
		if (viewPage.grabDataBlockCount(ProviderSection.ORGANIZATION_PROPERTIES) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_PROPERTIES, 0);
		assertTrue(resultProperty.get("Property Type").contains("HDS Sub Type"));
		assertTrue(resultProperty.get("Property Value").contains("Walk In Medical Clinics"));
	}

//	Create HDS Organization
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganization() {
		MaintainOrgBuilder providerBuilder = getHdsBuilder();
		Map<String, String> orgAddress = providerBuilder.getAddressList().get(0);
		String orgName = providerBuilder.getName();
		String orgDesc = providerBuilder.getAlias();
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
		AddProviderPage page = workflow.getPlrWebAccessActions().openAddOrganization()
				.openProviderPage(ProviderType.ORGANIZATION);
		// Select a provider role type other than HDS.hds type hides
		AddProviderIdFragment fragment = page.fillOrganizationIdentifier(OrganizationalProviderRoleType.ORG, null, null, null, null);
        assertFalse(fragment.isHdsTYpeDisplayed());
		// Select provider role type of HDS., hds type appears and fill the form
		fragment = page.fillOrganizationIdentifier(OrganizationalProviderRoleType.HDS, HdsType.CLINIC, HdsSubType.LNWIC.getText(), "ORGID",
				UpdateSimpleHelper.generateNumericString(8));
		assertTrue(fragment.isHdsTYpeDisplayed());
		page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);

		clickFirstNext(page);

		page.fillOrganizationName(orgName, orgDesc);
		clickSecondNext(page);

		AddProviderAddressFragment address = page.fillAddress("P", orgAddress.get("purpose"),
				List.of(orgAddress.get("line1"), "", ""), orgAddress.get("city"), "BC", "CA",
				orgAddress.get("postalCode"));
		page.fillPhone("250", UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(3));
		page.fillFax("250", UpdateSimpleHelper.generateNumericString(7));
		page.fillEmail(UpdateSimpleHelper.generateEmail());
		clickThirdNext(page, address);

		ViewProviderPage viewPage = page.clickSubmitButton();

		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("CLINIC"));
		LinkedHashMap<String, String> resultName = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0);
        assertEquals(orgDesc, resultName.get("Description"), "Excepted acceptable charset not supported");
	}

//	Create HDS Organization with Provider role type = EMERGENCY
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganizationTypeEmergency () {
		ViewProviderPage viewPage=createHDSProvider(HdsType.EMERGENCY,HdsSubType.LDEMC);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("EMERGENCY"));
		
		if (viewPage.grabDataBlockCount(ProviderSection.ORGANIZATION_PROPERTIES) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ORGANIZATION_PROPERTIES, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Property Type").contains("HDS Sub Type"));
		assertTrue(resultProperty.get("Property Value").contains("Emergency Medical Care"));
	}

//	 Create HDS Organization with Provider role type = GENERAL_CARE
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganizationTypeGeneralCare() {
		ViewProviderPage viewPage=createHDSProvider(HdsType.GENERAL_CARE,HdsSubType.LEGMC);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("GENERAL_CARE"));
	}

//	Create HDS Organization with Provider role type = HOSPITAL
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganizationTypeHospital() {
		ViewProviderPage viewPage=createHDSProvider(HdsType.HOSPITAL,HdsSubType.HOS);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("HOSPITAL"));
	}

//	Create HDS Organization with Provider role type = HOUSING
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganizationTypeHousing() {
		ViewProviderPage viewPage=createHDSProvider(HdsType.HOUSING,HdsSubType.LEGMC);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("HOUSING"));
	}

//	Create HDS Organization with Provider role type = INPATIENT
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganizationTypeInpatient() {
		ViewProviderPage viewPage=createHDSProvider(HdsType.INPATIENT,HdsSubType.LLIHF);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("INPATIENT"));
	}

//	Create HDS Organization with Provider role type = LAB
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganizationTypeLab() {
		ViewProviderPage viewPage=createHDSProvider(HdsType.LAB,HdsSubType.LEGMC);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("LAB"));
	}

//	Create HDS Organization with Provider role type = OUTPATIENT
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganizationTypeOutpatient() {
		ViewProviderPage viewPage=createHDSProvider(HdsType.OUTPATIENT,HdsSubType.LNOHF);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("OUTPATIENT"));
	}

//	Create HDS Organization with Provider role type = PHARMACY
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganizationTypePhymacy() {
		ViewProviderPage viewPage=createHDSProvider(HdsType.PHARMACY,HdsSubType.LEGMC);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("PHARMACY"));
	}

//	Create HDS Organization with Provider role type =CLINIC
	@Test(groups = { "CreateOrganization" })
	public void testCreateHDSOrganizationTypeClinic() {
		ViewProviderPage viewPage=createHDSProvider(HdsType.CLINIC,HdsSubType.LNWIC);
		LinkedHashMap<String, String> resultProperty = new LinkedHashMap<String, String>();
		if (viewPage.grabDataBlockCount(ProviderSection.ROLE_TYPE) > 0)
			resultProperty = viewPage.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0);
        assertFalse(resultProperty.isEmpty());
		assertTrue(resultProperty.get("Role Type").contains("HDS"));
		assertTrue(resultProperty.get("HDS Type").contains("CLINIC"));
	}
	
	private MaintainOrgBuilder getHdsBuilder(){
		 OrganizationBuilderFactory organizationFactory = new OrganizationBuilderFactory(OrganizationDataGenerator.getInstance());
	        FHIRController fhirController = new FHIRController(UserType.ADMIN);
	        OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.HDS)
	            .withEmail().withPhone().withFax()
	            .withName().withAlias()
	            .withAddress();
	              
	        MaintainOrgBuilder hsdOrg = organizationFactory.build(orgConfig);
	        fhirController.close();
	        return hsdOrg;
	        
	}
	
	private MaintainOrgBuilder getOrgBuilder(){
		 OrganizationBuilderFactory organizationFactory = new OrganizationBuilderFactory(OrganizationDataGenerator.getInstance());
	        FHIRController fhirController = new FHIRController(UserType.ADMIN);
	        OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.ORG)
	            .withEmail().withPhone().withFax()
	            .withName().withAlias()
	            .withAddress();
	              
	        MaintainOrgBuilder hsdOrg = organizationFactory.build(orgConfig);
	        fhirController.close();
	        return hsdOrg;
	        
	}
	
	private void clickFirstNext(AddProviderPage page) {
		 page.clickNext("Status", "");
	        page.waitForAddProviderStep("Organization", true);
	}
	
	private void clickSecondNext(AddProviderPage page) {
		  page.clickNext("Organization", "");
	        page.waitForAddProviderStep("Address", true);
	}
	
	
	private void clickThirdNext(AddProviderPage page, AddProviderAddressFragment address ) {
		  page.clickNext("Address");
	       
		  try {
			address.closeAddressValidationDialogWithContinue();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		  page.waitForAddProviderStep("Credential", true);
	}
	
	private ViewProviderPage createHDSProvider(HdsType hdsType, HdsSubType hdsSubType) {
		MaintainOrgBuilder providerBuilder = getHdsBuilder();
		Map<String, String> orgAddress = providerBuilder.getAddressList().get(0);
		String orgName = providerBuilder.getName();
		String orgDesc = providerBuilder.getAlias();
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddOrganization().openProviderPage(ProviderType.ORGANIZATION);
        
        page.fillOrganizationIdentifier(OrganizationalProviderRoleType.HDS, hdsType, hdsSubType.getText(), 
        		"ORGID", UpdateSimpleHelper.generateNumericString(8));
        page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);
        
       clickFirstNext(page);
		
        page.fillOrganizationName(orgName,orgDesc);
        clickSecondNext(page);
        
        AddProviderAddressFragment address = page.fillAddress("P", orgAddress.get("purpose"), 
        		List.of( orgAddress.get("line1"), "", ""),
        		orgAddress.get("city") , "BC", "CA",
        		orgAddress.get("postalCode"));
        page.fillPhone("250", UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(3));
        page.fillFax("250", UpdateSimpleHelper.generateNumericString(7));
        page.fillEmail(UpdateSimpleHelper.generateEmail());
        clickThirdNext(page,address);

        ViewProviderPage viewPage = page.clickSubmitButton();
        return viewPage;

	}
	
	
	
	private ViewProviderPage createHDSProvider(HdsType hdsType, HdsSubType hdsSubType,ProviderIdentifierTypeOptions idType) {
		MaintainOrgBuilder providerBuilder = getHdsBuilder();
		Map<String, String> orgAddress = providerBuilder.getAddressList().getFirst();
		String orgName = providerBuilder.getName();
		String orgDesc = providerBuilder.getAlias();
		PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddOrganization().openProviderPage(ProviderType.ORGANIZATION);
        
        page.fillOrganizationIdentifier(OrganizationalProviderRoleType.HDS, hdsType, hdsSubType.getText(), 
        		idType.name(), UpdateSimpleHelper.generateNumericString(8));
        page.fillStatus("LIC", StatusCodeOption.ACTIVE, StatusReasonCodeOption.GS);
        
       clickFirstNext(page);
		
        page.fillOrganizationName(orgName,orgDesc);
        clickSecondNext(page);
        
        AddProviderAddressFragment address = page.fillAddress("P", orgAddress.get("purpose"), 
        		List.of( orgAddress.get("line1"), "", ""),
        		orgAddress.get("city") , "BC", "CA",
        		orgAddress.get("postalCode"));
        page.fillPhone("250", UpdateSimpleHelper.generateNumericString(7), UpdateSimpleHelper.generateNumericString(3));
        page.fillFax("250", UpdateSimpleHelper.generateNumericString(7));
        page.fillEmail(UpdateSimpleHelper.generateEmail());
        clickThirdNext(page,address);
        
        ViewProviderPage viewPage = page.clickSubmitButton();
        return viewPage;

	}
}

