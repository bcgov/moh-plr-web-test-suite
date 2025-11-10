package ca.bc.gov.health.qa.autotest.plr.web.tests;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class FHIRSampleTest
implements SimpleTest
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public FHIRSampleTest()
    {}

    @AfterClass
    public void teardown()
    {
        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
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
    public void test0()
    {
        FHIRController fhirController = new FHIRController(UserType.ADMIN);

        FacilityMaintainConfig cfg = new FacilityMaintainConfig()
            //.withAddress()       added by default as is a required attribute
            //.withIdentifier()    added by default as is a required attribute
            //.withName()          added by default as is a required attribute
            .withPhone()
            .withEmail()
            .withFax()
            .withFtp()
            .withMobile()
            .withModem()
            .withPager()
            .withWebsite()
            .withDescription()
            .withNotes(3)
            .withOrgRelationships(3);


        //MaintainFacilityBuilder facility2 = fhirController.createFacility(cfg);


        //LOG.info("Created facility id {}, name {}, address {}, description {}, telecoms {}, notes {}, relationships {}.", facility2.getIdentifier(), facility2.getName(), facility2.getAddress().toString(), facility2.getDescription(), facility2.getTelecomList(), facility2.getNoteList(), facility2.getOrgRelationshipList());

        //MaintainFacilityBuilder queriedFacility = fhirController.queryFacilityByIdentifier(IdentifierType.IFC, facility2.getIdentifier());

        //LOG.info("Queried facility id {}, name {}, address {}, description {}, telecoms {}, notes {}, relationships {}.", queriedFacility.getIdentifier(), queriedFacility.getName(), queriedFacility.getAddress().toString(), queriedFacility.getDescription(), queriedFacility.getTelecomList(), queriedFacility.getNoteList(), queriedFacility.getOrgRelationshipList());

        //MaintainOrgBuilder org = fhirController.createOrganization(OrgRoleType.HDS);

        //fhirController.queryOrganizationByIdentifier(IdentifierType.IPC, org.getIdentifier());

        //MaintainFacilityBuilder facility2noRelationships = fhirController.ceaseFacilityRelationships(facility2);
        
        //LOG.info("updated facility to not have relationships id {}, name {}, address {}.", facility2noRelationships.getIdentifier(), facility2noRelationships.getName(), facility2noRelationships.getAddress().toString());
        
        
        //MaintainOrgBuilder orgBuilder = fhirController.createOrganization(OrgRoleType.HDS);
        //LOG.info("Created organization id {}, name {}.", orgBuilder.getIdentifier(), orgBuilder.getName());

        
        MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC, "IPC.00123847.BC.PRS");
        LOG.info("Queried organization id {}, RoleType {}, HDS Type {},  Status {}, Name {}, Alias {}, Address {}, telecoms {}, notes {}.", orgQueried.getIdentifier(), orgQueried.getRoleType(), orgQueried.getHdsType(), orgQueried.getStatusList(), orgQueried.getName(), orgQueried.getAlias(), orgQueried.getAddressList(), orgQueried.getTelecomList(), orgQueried.getNoteList());

        fhirController.close();
           
    }

}
