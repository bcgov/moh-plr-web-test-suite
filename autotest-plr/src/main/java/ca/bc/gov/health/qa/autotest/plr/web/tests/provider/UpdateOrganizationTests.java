package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static org.testng.Assert.assertEquals;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateOrganizationPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.EndReason;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.provider.OrgNameType;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class UpdateOrganizationTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    public static JSONObject errorList;
    String defaultOrg;

    private UpdateOrganizationTests()
    {
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

        MaintainOrgBuilder org = fhirController.createOrganization(new OrganizationMaintainConfig(OrgRoleType.ORG));
        defaultOrg = org.getIdentifier(IdentifierType.IPC);

        fhirController.close();
    }

    // Update Provider - Validate Organization Name (Add)
    @Test
    public void testValidateOrgNameAdd() {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        UpdateOrganizationPage page = viewByIdentifierAsUpdateOrg(defaultOrg, workflowManager_);

        String error = page.addOrganizationNameDataBlock(OrgNameType.CURR, "", "Test Desc", true);

        assertEquals(error, errorList.get("errorMsgName"), "Error message did not match expected value.");

        error = page.addOrganizationNameDataBlock(OrgNameType.CURR, generateAlphabetString(101), "Test Desc", true);

        assertEquals(error, errorList.get("errorMaxOrgNameLength"), "Error message did not match expected value.");

        error = page.addOrganizationNameDataBlock(OrgNameType.CURR, "Test Name", generateAlphabetString(201), true);

        assertEquals(error, errorList.get("errorMaxOrgLongNameLength"), "Error message did not match expected value.");

        error = page.addOrganizationNameDataBlock(OrgNameType.CURR, generateAlphabetString(101), generateAlphabetString(201), true);

        assertEquals(error, errorList.get("errorMaxOrgNameLength") + "\n" + errorList.get("errorMaxOrgLongNameLength"),
                "Did not receive expected multiple errors.");

        final String expectedName = generateAlphabetString(100);
        final String expectedDesc = generateAlphabetString(200);

        page.addOrganizationNameDataBlock(OrgNameType.CURR, expectedName, expectedDesc, false);

        assertEquals(page.grabActiveDataBlockCount(ProviderSection.ORGANIZATION_NAMES, true), 2,
                "Expected 2 active data blocks after adding valid organization name.");
    }

    // Update Provider - Validate Organization Name (Update)
    @Test
    public void testValidateOrgNameUpdate() {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(UserType.ADMIN);

        UpdateOrganizationPage page = viewByIdentifierAsUpdateOrg(defaultOrg, workflowManager_);

        String error = page.updateOrganizationNameDataBlock("", "Test Desc", EndReason.CHG, 0, true);

        assertEquals(error, errorList.get("errorMsgName"), "Error message did not match expected value.");

        error = page.updateOrganizationNameDataBlock(generateAlphabetString(101), "Test Desc", EndReason.CHG, 0, true);

        assertEquals(error, errorList.get("errorNameTooLong"), "Error message did not match expected value.");

        error = page.updateOrganizationNameDataBlock("Test Name", generateAlphabetString(201), EndReason.CHG, 0, true);

        assertEquals(error, errorList.get("errorLongNameTooLong"), "Error message did not match expected value.");

        final String expectedName = generateAlphabetString(100);
        final String expectedDesc = generateAlphabetString(200);

        page.updateOrganizationNameDataBlock(expectedName, expectedDesc, EndReason.CHG, 0, false);

        Map<String,String> orgName = page.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0);
        assertEquals(orgName.get("Name"), expectedName, "Organization name was not updated as expected.");
        assertEquals(orgName.get("Description"), expectedDesc, "Organization long name was not updated as expected.");
    }
}
