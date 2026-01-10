package ca.bc.gov.health.qa.autotest.plr.web.tests.provider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import ca.bc.gov.health.qa.autotest.plr.data.InjectableData;
import ca.bc.gov.health.qa.autotest.plr.data.PlrData;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.provider.ViewProviderActions;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewHeaderFragment;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewMode;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class ViewProviderTests
implements SimpleTest
{
    // NOTE: The following test cases *WILL NOT* be automated:
    // - View Provider : Print Provider Details
    // - View Provider : Visibility of Incorrect Data

    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

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
        // fhirController = new FHIRController(UserType.ADMIN);
    }

    // View Provider : Default Provider Detail Screen Record Display
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testDefaultProviderDetail(ProviderType providerType)
    {
        final JSONObject provider = PlrData.getProvider(providerType, "default");

        // Step 1: Login into the Web App and navigate to the View Providers Details Screen by submitting a search
        ViewProviderPage page = viewByIdentifier(providerType, provider, workflowManager_);
        final ViewProviderActions actions = workflowManager_.getSelectedWorkflow().getViewProviderActions();

        // Step 2: Verify Default Provider Detail Screen Record Display
        ViewHeaderFragment viewHeader = page.getViewHeader();
        assertEquals(viewHeader.grabViewMode(), ViewMode.CURRENT, "Default view mode not current");
    }

    // View Provider : Indicating Current Data Objects
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testIndicatingCurrentDataObj(ProviderType providerType)
    {
        final JSONObject provider = PlrData.getProvider(providerType, "default");

        // Step 1: Login into the Web App and navigate to the View Providers Details Screen by submitting a search
        viewByIdentifier(providerType, provider, workflowManager_);
        final ViewProviderActions actions = workflowManager_.getSelectedWorkflow().getViewProviderActions();

        // Step 3: Verify active data objects
        actions.verifySectionsWithActiveDataBlocks(providerType, false);
    }

    // View Provider : Optional Provider Detail Screen Views - History and Audit
    @Test(dataProvider = "allPlrUserTypesProviderTypes", dataProviderClass = InjectableData.class)
    public void testOptionalDetailScreenViews(UserType userType, ProviderType providerType)
    {
        final JSONObject provider = PlrData.getProvider(providerType, "default");

        // Step 1: Login into the Web App and navigate to the View Providers Details Screen by submitting a search
        ViewProviderPage page = viewByIdentifier(providerType, provider, workflowManager_);

        ViewHeaderFragment viewHeader = page.getViewHeader();

        // Step 2: Select Current View
        viewHeader.selectViewMode(ViewMode.CURRENT);

        // Step 3: Select History View
        viewHeader.selectViewMode(ViewMode.HISTORY);

        // Step 4: Select Audit View (skipped if consumer)
        if (!userType.equals(UserType.CONSUMER)) viewHeader.selectViewMode(ViewMode.AUDIT);

        // Step 5-7: Verify Viewable View Modes
        viewHeader.expandViewModeMenu(true);
        assertTrue(viewHeader.grabViewModeDisplayed(ViewMode.CURRENT), "Current View option");
        assertTrue(viewHeader.grabViewModeDisplayed(ViewMode.HISTORY), "History View option");
        assertEquals(
                viewHeader.grabViewModeDisplayed(ViewMode.AUDIT),
                !userType.equals(UserType.CONSUMER),
                "Audit View option");
        viewHeader.expandViewModeMenu(false);
    }

    // View Provider : Rules Indicating Current Records
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testRulesCurrentRecords(ProviderType providerType)
    {
        final JSONObject provider = PlrData.getProvider(providerType, "default");

        // Step 1: Login into the Web App and navigate to the View Providers Details Screen by submitting a search
        ViewProviderPage page = viewByIdentifier(providerType, provider, workflowManager_);
        final ViewProviderActions actions = workflowManager_.getSelectedWorkflow().getViewProviderActions();

        ViewHeaderFragment viewHeader = page.getViewHeader();

        // Step 3: Verify Current View
        viewHeader.selectViewMode(ViewMode.CURRENT);
        actions.verifySectionsWithActiveDataBlocks(providerType, false);

        // Step 4: Verify History View
        viewHeader.selectViewMode(ViewMode.HISTORY);
        actions.verifySectionsWithActiveDataBlocks(providerType, true);

        // Step 5: Verify Audit View
        viewHeader.selectViewMode(ViewMode.AUDIT);
        actions.verifySectionsWithActiveDataBlocks(providerType, true);
    }

    // View Provider : Sort Order On View Provider Details Screen
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testSortOrder(ProviderType providerType)
    {
        final JSONObject provider = PlrData.getProvider(providerType, "default");

        // Step 1: Login into the Web App and navigate to the View Providers Details Screen by submitting a search
        ViewProviderPage page = viewByIdentifier(providerType, provider, workflowManager_);
        final ViewProviderActions actions = workflowManager_.getSelectedWorkflow().getViewProviderActions();

        // Step 3: Current View
        ViewHeaderFragment viewHeader = page.getViewHeader();
        viewHeader.expandAll(true);
        actions.verifyDataBlockSortOrder(providerType);

        // Step 4: History View
        viewHeader.selectViewMode(ViewMode.HISTORY);
        actions.verifyDataBlockSortOrder(providerType);

        // Step 5: Audit View
        if (!workflowManager_.getSelectedWorkflow().getUserType().equals(UserType.CONSUMER))
        {
            viewHeader.selectViewMode(ViewMode.AUDIT);
            actions.verifyDataBlockSortOrder(providerType);
        }
    }

    // View Provider : Viewing Empty Data Objects
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testViewingEmptyData(ProviderType providerType)
    {
        /* Step 15 is **not** automated, ensure reg admin DPS is set to CGITEST_WRITE for:
         *  MD (MOH) + MD (CPS)             bc-practitioners
         *  OOP-MD (MOH) + OOP-MD (CPS)     oop-practitioners
         *  ORG (MOH) + ORG (CPS)           organizations
         * before running the testcase.
         * After running, ensure these DPS are returned to their original values
         * (likely CGITEST_READWRITE_ALL)
         */
        final JSONObject provider = PlrData.getProvider(providerType, "minimum");

        // Step 1: Navigate to the View Providers Details Screen by submitting a search
        viewByIdentifier(providerType, provider, workflowManager_);
        final ViewProviderActions actions = workflowManager_.getSelectedWorkflow().getViewProviderActions();

        // Step 15: Setup DPS to check no permissions to view (not automated, ensure admin is set to CGI_WRITE)
        // Step 2-14, 16-31: Verify Required sections blocks / No Permission to view record blocks
        actions.verifyRequiredSections(providerType, true);
    }

    // View Provider : Viewing Provider Details
    @Test(dataProvider = "allProviderTypes", dataProviderClass = InjectableData.class)
    public void testViewingProviderDetails(ProviderType providerType)
    {
        final JSONObject provider = PlrData.getProvider(providerType, "default");

        // Step 1: Login into the Web App and navigate to the View Providers Details Screen by submitting a search
        ViewProviderPage page = viewByIdentifier(providerType, provider, workflowManager_);
        final ViewProviderActions actions = workflowManager_.getSelectedWorkflow().getViewProviderActions();

        // Step 2: Verify Title
        ViewHeaderFragment viewHeader = page.getViewHeader();
        assertEquals(viewHeader.grabViewTitle(), actions.getViewTitle(providerType, provider),
                "Provider View Title does not match expected result");

        // Step 3: Verify Links
        assertTrue(actions.verifyLinks(), "One of Expand All, Print, or View Mode buttons is not visible");

        // Step 4: Verify all Provider details are displayed
        actions.verifySectionTitles(providerType);
        actions.verifyDataBlocksExpanded(providerType, false);
        actions.verifySectionDataFieldNames(providerType);

        // Step 5: Click "Expand All"
        assertFalse(viewHeader.grabExpandedAll(), "Collapse All button visible when it should be Expand All");
        viewHeader.expandAll(true);
        actions.verifyDataBlocksExpanded(providerType, true);

        viewHeader.expandAll(false);
        actions.verifyDataBlocksExpanded(providerType, false);

        // Step 6: Click on "Show audit"
        assertEquals(viewHeader.grabViewMode(), ViewMode.CURRENT, "Current View is not displayed as expected");
        viewHeader.selectViewMode(ViewMode.AUDIT);
        assertEquals(viewHeader.grabViewMode(), ViewMode.AUDIT, "Audit View is not displayed as expected");

        // Step 7: Print (will not be automated)
    }
}
