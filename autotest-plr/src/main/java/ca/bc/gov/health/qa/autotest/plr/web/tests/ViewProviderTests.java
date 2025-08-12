package ca.bc.gov.health.qa.autotest.plr.web.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.ViewProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewHeaderFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewMode;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class ViewProviderTests
implements SimpleTest
{
    // NOTE: The following test cases *WILL NOT* be automated:
    // - View Provider : Print Provider Details

    private static final Logger LOG = ExecutionLogManager.getLogger();

    private PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public ViewProviderTests()
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

    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    // View Provider : Default Provider Detail
    // View Provider : Indicating Current Data Objects
    public void testDefaultProviderDetail(ProviderType providerType)
    {
        JSONObject provider = PlrData.getProvider(providerType, "default");
        ViewProviderActions actions =
                workflowManager_.getSelectedWorkflow().getViewProviderActions();
        actions.openProvider(provider.getString("pauth"));
        actions.verifySectionsWithActiveDataBlocks(providerType, false);
    }

    @Test(dataProvider = "allPlrUserTypesProviderTypes", dataProviderClass = InjectableData.class)
    // View Provider : Optional Provider Detail Screen
    // View Provider : Rules Indicating Current Records
    // View Provider : Visibility of Incorrect Data
    public void testOptionalProviderDetail(UserType userType, ProviderType providerType)
    {
        JSONObject provider = PlrData.getProvider(providerType, "default");
        ViewProviderActions actions =
                workflowManager_.getSelectedWorkflow().getViewProviderActions();
        ViewProviderPage viewProvider = actions.openProvider(provider.getString("pauth"));
        ViewHeaderFragment viewHeader = viewProvider.getViewHeader();

        viewHeader.expandViewModeMenu(true);
        assertTrue(viewHeader.grabViewModeDisplayed(ViewMode.CURRENT), "Current View option");
        assertTrue(viewHeader.grabViewModeDisplayed(ViewMode.HISTORY), "History View option");
        assertEquals(
                viewHeader.grabViewModeDisplayed(ViewMode.AUDIT),
                !userType.equals(UserType.CONSUMER),
                "Audit View option");
        viewHeader.expandViewModeMenu(false);

        viewHeader.selectViewMode(ViewMode.HISTORY);
        actions.verifySectionsWithActiveDataBlocks(providerType, true);
        if (!userType.equals(UserType.CONSUMER))
        {
            viewHeader.selectViewMode(ViewMode.AUDIT);
            actions.verifySectionsWithActiveDataBlocks(providerType, true);
        }
        viewHeader.selectViewMode(ViewMode.CURRENT);
        actions.verifySectionsWithActiveDataBlocks(providerType, false);
    }

    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    // View Provider : Sort Order On View Provider
    public void testSortOrder(ProviderType providerType)
    {
        JSONObject provider = PlrData.getProvider(providerType, "default");
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();
        ViewProviderActions actions = workflow.getViewProviderActions();
        ViewProviderPage viewProvider = actions.openProvider(provider.getString("pauth"));
        ViewHeaderFragment viewHeader = viewProvider.getViewHeader();
        viewHeader.expandAll(true);
        actions.verifyDataBlockSortOrder(providerType);
        viewHeader.selectViewMode(ViewMode.HISTORY);
        actions.verifyDataBlockSortOrder(providerType);
        if (!workflow.getUserType().equals(UserType.CONSUMER))
        {
            viewHeader.selectViewMode(ViewMode.AUDIT);
            actions.verifyDataBlockSortOrder(providerType);
        }
    }

    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    // View Provider : Viewing Empty Data Objects
    public void testViewingEmptyData(ProviderType providerType)
    {
        JSONObject provider = PlrData.getProvider(providerType, "minimum");
        ViewProviderActions actions =
                workflowManager_.getSelectedWorkflow().getViewProviderActions();
        actions.openProvider(provider.getString("pauth"));
        actions.verifyRequiredSections(providerType);
        // TODO (AZ) - Elaborate on the no-permission sections.
    }

    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    // View Provider : Viewing Provider Details
    public void testViewingProviderDetails(ProviderType providerType)
    {
        // NOTE: Step 7 (click "Print") will not be automated.
        JSONObject provider = PlrData.getProvider(providerType, "default");
        ViewProviderActions actions =
                workflowManager_.getSelectedWorkflow().getViewProviderActions();
        ViewHeaderFragment viewHeader = actions
                .openProvider(provider.getString("pauth"))
                .getViewHeader();
        assertEquals(
                viewHeader.grabViewTitle(),
                getViewTitle(providerType, provider),
                "Provider View Title");
        assertEquals(viewHeader.grabViewMode(), ViewMode.CURRENT, "Current View");
        assertFalse(viewHeader.grabExpandedAll(), "Collapsed All");
        assertTrue(viewHeader.grabPrintButtonDisplayed(), "Print button displayed");
        actions.verifySectionTitles(providerType);
        actions.verifyDataBlocksExpanded(providerType, false);
        viewHeader.expandAll(true);
        viewHeader.selectViewMode(ViewMode.AUDIT);
        actions.verifyDataBlocksExpanded(providerType, true);
        actions.verifySectionDataFieldNames(providerType);
        viewHeader.expandAll(false);
        actions.verifyDataBlocksExpanded(providerType, false);
    }

    private String getViewTitle(ProviderType providerType, JSONObject provider)
    {
        String title;
        switch(providerType)
        {
            case BC_PRACTITIONER:
            case OOP_PRACTITIONER:
                JSONObject name = provider.getJSONObject("name");
                title = new StringBuilder()
                        .append(name.getString("last"))
                        .append(", ")
                        .append(name.getString("first"))
                        .append(" - ")
                        .append(provider.getString("cpn"))
                        .append("(")
                        .append(provider.getString("owner"))
                        .append(") - ")
                        .append(provider.getString("status"))
                        .toString();
                break;

            case ORGANIZATION:
                title = new StringBuilder()
                        .append(provider.getString("name"))
                        .append("(")
                        .append(provider.getString("owner"))
                        .append(") - ")
                        .append(provider.getString("status"))
                        .toString();
                break;

            default:
                String msg = String.format("Unsupported provider type (%s).", providerType);
                throw new IllegalStateException(msg);
        }
        return title;
    }
}
