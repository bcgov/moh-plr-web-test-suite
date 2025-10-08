package ca.bc.gov.health.qa.autotest.plr.web.tests;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRExecutor;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.ResourceType;
import java.util.HashMap;
import java.util.Map;
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

        FHIRExecutor executor = new FHIRExecutor(UserType.ADMIN);
        
        Map<String,String> prac = new HashMap<>();
        prac.put("identifierType", IdentifierType.IPC.name());
        prac.put("identifierValue", "24354284341");
        prac.put("familyName", "Smith");
        prac.put("givenName", "Jhon");
        prac.put("gender", "male");
        prac.put("birthDate", "1900-04-01");
        prac.put("roleType", "RN");
        prac.put("addressType", "postal");
        prac.put("addressPurpose", "BC");
        prac.put("addressLine1", "1755 Douglas St");
        prac.put("addressCity", "Victoria");
        prac.put("addressPostalCode", "V6D 1B9");

        String prac_id = executor.submitMaintainRequest(ResourceType.PRACTITIONER, prac);

        Map<String,String> facility = new HashMap<>();
        facility.put("identifier", "87081632128");
        facility.put("name", "Test Facility");
        facility.put("description", "This is a test facility");
        facility.put("addressLine1", "810 Griffiths Wy");
        facility.put("addressCity", "Vancouver");
        facility.put("addressPostal", "V6B 6G1");

        String facility_id = executor.submitMaintainRequest(ResourceType.FACILITY, facility);


        LOG.info("Created practitioner id {}, facility id {}.", prac_id, facility_id);

        String pracQueryResponse = executor.queryByIdentifier(ResourceType.PRACTITIONER, IdentifierType.IPC, prac_id).toString();
        String facQueryResponse = executor.queryByIdentifier(ResourceType.FACILITY, IdentifierType.IFC, facility_id).toString();

        LOG.info("Practitioner query response: {}", pracQueryResponse);
        LOG.info("Facility query response: {}", facQueryResponse);
        
        executor.close();
           
    }

}
