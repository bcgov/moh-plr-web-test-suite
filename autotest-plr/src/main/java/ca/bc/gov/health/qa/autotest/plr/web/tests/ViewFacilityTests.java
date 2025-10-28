package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;

public class ViewFacilityTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final Config config_ = ConfigProvider.get().getConfig();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public ViewFacilityTests() {}

    @BeforeMethod
    public void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) workflow.login().openPlr();
    }

    @Test
    // F2-007. Limiting Number of Records For View Facility Details Screen
    public void testLimitRecordDetails()
    {
        ViewFacilityPage viewFacility = viewFacilityByIdentifier(workflowManager_, "IFC.00006365.BC.PRS", UserType.ADMIN);
    }
}
