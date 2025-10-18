package ca.bc.gov.health.qa.autotest.plr.web.tests;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
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

        MaintainFacilityBuilder facility = fhirController.createFacility();

        LOG.info("Created facility id {}, name {}, address {}.", facility.getIdentifier(), facility.getName(), facility.getAddress().toString());


        FacilityMaintainConfig cfg = new FacilityMaintainConfig()
            .withPhone()
            .withEmail()
            .withFax()
            .withFtp()
            //.withAddress()       added by default as is a required attribute
            //.withIdentifier()    added by default as is a required attribute
            //.withName()          added by default as is a required attribute
            .withMobile()
            .withModem()
            .withPager()
            .withWebsite()
            .withNotes(3);


        MaintainFacilityBuilder facility2 = fhirController.createFacility(cfg);

        LOG.info("Created facility id {}, name {}, address {}.", facility2.getIdentifier(), facility2.getName(), facility2.getAddress().toString());


        fhirController.close();

           
    }

}
