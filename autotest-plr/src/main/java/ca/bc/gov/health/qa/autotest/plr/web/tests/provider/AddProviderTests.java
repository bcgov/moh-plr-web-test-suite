package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.*;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AddProviderTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    private AddProviderTests() {}

    @BeforeMethod
    public void before(Object[] parameters) {

        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) {
            workflow.login().openPlr();
        }
    }

    @Test
    public void testAddProviderPageLoads() {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        page = page.changeProviderType(ProviderType.ORGANIZATION);

        page.fillIdentifier(OrganizationalProviderRoleType.HDS, HdsType.CLINIC, "BHACH", "ORGID", "252525", null);
        page.fillStatus("AE", StatusCodeOption.CANCELLED, StatusReasonCodeOption.LAP, null);

        page.clickNext("Status", "");
    }
}
