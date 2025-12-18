package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants.*;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.util.RelatedProviderIdentifierType;
import ca.bc.gov.health.qa.autotest.plr.util.RelationshipType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.UpdateFacilitySimpleActions;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.ViewFacilityActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.UpdateFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.UpdateSimpleHelper;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.generateAlphabetString;
import static java.lang.Integer.parseInt;
import static org.testng.Assert.*;

public class UpdateFacilityComplexTests implements SimpleTest
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();
    private static FHIRController fhirController;
    private static MaintainFacilityBuilder dummyFacility;

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;
    private static JSONObject warningList;

    public UpdateFacilityComplexTests()
    {
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

    /*
    @AfterClass
    public void teardown()
    {
        fhirController.close();
        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }
     */

    @BeforeTest
    public void beforeTest()
    {
        fhirController = new FHIRController(UserType.ADMIN);

        final FacilityMaintainConfig config = new FacilityMaintainConfig()
                                                    .withAllAttributes(1,0);
        //dummyFacility = fhirController.queryFacilityByIdentifier(IdentifierType.IFC, "IFC.00006905.BC.PRS");
        dummyFacility = fhirController.createFacility(config);
    }

    @BeforeMethod
    public void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn())
        {
            workflow.login().openPlr();
        }
    }

    @Test
    // F4-022. Rejection of Non-Acceptable Characters
    public void rejectionNonAcceptableCharacters()
    {
        final ViewFacilityActions actions = workflowManager_.getSelectedWorkflow().getViewFacilityActions();
        actions.openFacility("6905");
        // attempt to add identifier
        // attempt to update identifier
        // attempt to add name
        // attempt to update name
        // attempt to update civic address
        // attempt to update other address
        // attempt to add new telecom
        // attempt to update telecom
        // attempt to add e-address
        // attempt to update e-address
        // attempt to add note
        // attempt to update note
    }

    @Test
    // F4-045. Generating Internal Relationship Identifier (RID)
    public void generateRelationshipIdentifier()
    {
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        final OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig();
        final MaintainOrgBuilder org1 = fhirController.createOrganization(orgConfig.withName(generateAlphabetString(15)));
        final MaintainOrgBuilder org2 = fhirController.createOrganization(orgConfig.withName(generateAlphabetString(15)));

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org1.getIdentifier(),
                                            RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(),
                                    "", false);
        String org1RelIdentifier = StringUtils.getDigits(page.grabOrgRelationshipsBlockContent(0)
                                        .get(OrgRelationshipField.RELATIONSHIP_IDENTIFIER.getString()));

        page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org2.getIdentifier(),
                                            RelationshipType.LOCATION.getText(), UpdateSimpleHelper.effective_date(),
                                    "", false);
        String org2RelIdentifier = StringUtils.getDigits(page.grabOrgRelationshipsBlockContent(1)
                                        .get(OrgRelationshipField.RELATIONSHIP_IDENTIFIER.getString()));

        assertNotEquals(org1RelIdentifier, org2RelIdentifier,
                "Organization Relationship Identifiers are unexpectedly equal");
        assertEquals(parseInt(org1RelIdentifier), parseInt(org2RelIdentifier) - 1,
                "Second relationship identifier is not immediately after the first");
    }

    @Test
    // F4-046. Validate Facility Relationship Type Code
    public void validateRelationshipTypeCode()
    {
        final String errMsg5000OrgRel = errorList.getString("errMsg5000OrgRelType");
        final UpdateFacilitySimpleActions actions = workflowManager_.getSelectedWorkflow().getUpdateFacilitySimpleActions();

        final OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig();
        final MaintainOrgBuilder org = fhirController.createOrganization(orgConfig.withName(generateAlphabetString(15)));

        UpdateFacilityPage page = actions.openFacility(dummyFacility);

        String error = page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org.getIdentifier(),
                "Select One", UpdateSimpleHelper.effective_date(), "", true);

        assertEquals(error, errMsg5000OrgRel, "Error message does not match expected result");

        for (RelationshipType relType : RelationshipType.values())
        {
            page.addRelatedOrganizationDataBlock(RelatedProviderIdentifierType.IPC.getText(), org.getIdentifier(),
                    relType.getText(), UpdateSimpleHelper.effective_date(), "", false);
            String orgRelType = page.grabOrgRelationshipsBlockContent(0)
                    .get(OrgRelationshipField.RELATIONSHIP_TYPE.getString());

            assertEquals(orgRelType, relType.getBlockText(),
                    "New org relationship has unexpected relationship type");
        }
    }
}
