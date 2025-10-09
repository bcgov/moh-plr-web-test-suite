package ca.bc.gov.health.qa.autotest.plr.web.tests;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.actions.FHIRSession;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainPracBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.PlrFhirResourceType;
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

        /*FHIRSession executor = new FHIRSession(UserType.ADMIN);

        MaintainPracBuilder prac = new MaintainPracBuilder();
        prac.identifier(IdentifierType.IPC, "243111184341")
            .firstName("Jhon")
            .familyName("Smith")
            .gender("male")
            .roleType("RN")
            .birthDate("1900-04-01")
            .addAddress("postal", "BC", "1755 Douglas St", "Victoria", "V6D 1B9")
            .addStatus("LIC", "ACTIVE", "GS");

        
        String prac_id = executor.submitMaintain(prac);

        MaintainFacilityBuilder facility = new MaintainFacilityBuilder();
        facility.identifier("842134232128")
            .name("Test Facility")
            .description("This is a test facility")
            .addAddress("814 Griffiths Wy", "Vancouver", "V6B 6G1");

        String facility_id = executor.submitMaintain(facility);


        LOG.info("Created practitioner id {}, facility id {}.", prac_id, facility_id);

        String pracQueryResponse = executor.queryByIdentifier(PlrFhirResourceType.PRACTITIONER, IdentifierType.IPC, prac_id).toString();
        String facQueryResponse = executor.queryByIdentifier(PlrFhirResourceType.FACILITY, IdentifierType.IFC, facility_id).toString();

        LOG.info("Practitioner query response: {}", pracQueryResponse);
        LOG.info("Facility query response: {}", facQueryResponse);
        
        executor.close();*/

        FHIRController fhirController = new FHIRController(UserType.ADMIN);

        MaintainFacilityBuilder facility = fhirController.createFacility();

        LOG.info("Created facility id {}, name {}, address {}.", facility.getIdentifier(), facility.getName(), facility.getAddress().toString());

        fhirController.close();

           
    }

}
