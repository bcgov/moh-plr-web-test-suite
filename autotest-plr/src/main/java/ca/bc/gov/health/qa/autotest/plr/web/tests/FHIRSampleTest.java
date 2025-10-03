package ca.bc.gov.health.qa.autotest.plr.web.tests;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.fhir.PracMaintainExecutor;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.IdentifierType;
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

        PracMaintainExecutor executor = new PracMaintainExecutor(UserType.ADMIN);
        
        executor.submitPractitioner(
                IdentifierType.IPC, 
                "1231424324", 
                "Smith", 
                "Jhon", 
                "male", 
                "1900-04-01", 
                "RN", 
                "postal", 
                "BC", 
                "1755 Douglas St", 
                "Victoria", 
                "V6D 1B9");
        
        executor.close();
           
    }

}
