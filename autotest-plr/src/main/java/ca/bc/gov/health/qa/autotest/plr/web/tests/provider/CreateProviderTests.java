package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderIdFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderStatusFragment;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.*;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static ca.bc.gov.health.qa.autotest.plr.data.AddProviderConstants.*;
import static org.testng.Assert.assertTrue;

import java.util.List;

public class CreateProviderTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    private CreateProviderTests() {}

    @BeforeMethod
    public void before(Object[] parameters) {

        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) {
            workflow.login().openPlr();
        }
    }

    // Create Provider - Code Validation Restriction - Identifier
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testCodeRestrictionIdentifier(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

        AddProviderIdFragment id = page.fillIdentifier(null, null, null, null, null);
        List<String> roleOptions = id.getProviderRoleTypeOptions();
        for (ProviderRoleType roleType : ProviderRoleType.values())
        {
            if (!roleOptions.contains(roleType.getText())) continue;

            page.fillIdentifier(roleType, null, null, null, null);
            List<String> identifierTypeOptions = id.getIdentifierTypeOptions();

            IDENTIFIER_TYPE_OPTIONS_MAP.getOrDefault(roleType, List.of("OOPID - Out of Province Provider"))
                    .forEach(option -> { assertTrue(identifierTypeOptions.contains(option),
                            "Expected identifier type option '" + option + "' not found for provider role '" + roleType.getText() + "'");
            });
        }
    }

    // Create Provider - Code Validation Restriction - Status Code
    @Test(dataProvider = "practitioners", dataProviderClass = InjectableData.class)
    public void testCodeRestrictionStatusCode(ProviderType providerType)
    {
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        AddProviderPage page = workflow.getPlrWebAccessActions().openAddProvider();

        if (providerType.equals(ProviderType.OOP_PRACTITIONER)) page = page.changeProviderType(ProviderType.OOP_PRACTITIONER);

        AddProviderStatusFragment status;
        for (StatusCodeOption statusCode : StatusCodeOption.values())
        {
            status = page.fillStatus(null, statusCode, null);

            List<String> reasonCodeOptions = status.getStatusReasonCodeOptions();

            STATUS_REASON_CODE_OPTIONS_MAP.get(statusCode).forEach(option -> {
                    assertTrue(reasonCodeOptions.contains(option.getText()),
                            "Expected reason code option '" + option.getText() + "' not found for status code '" + statusCode.getText() + "'");
            });
        }
    }
}
