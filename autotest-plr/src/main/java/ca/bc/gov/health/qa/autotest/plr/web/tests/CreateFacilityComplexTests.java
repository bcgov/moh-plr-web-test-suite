package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.AddFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.navigateToAddFacilityPage;

public class CreateFacilityComplexTests implements SimpleTest {

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    @BeforeMethod
    public void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) workflow.login().openPlr();
    }

    @Test
    // F3-006. Validate Facility Identifiers
    public void testValidateFacIdentifiers()
    {
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_, UserType.ADMIN);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "", List.of(2001, 5, 25));
        addFacility.nextStage();
    }
}
