package ca.bc.gov.health.qa.autotest.plr.web.workflows;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.plr.util.PlrUtils;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumContext;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

/**
 * TODO (AZ) - doc
 */
public class PlrWebWorkflowManager
implements AutoCloseable
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final Map<UserType,PlrWebWorkflow> workflowMap_;

    private PlrWebWorkflow selectedWorkflow_ = null;

    /**
     * TODO (AZ) - doc
     */
    public PlrWebWorkflowManager()
    {
        workflowMap_ = new EnumMap<>(UserType.class);
    }

    /**
     * TODO (AZ) - doc
     *
     * @throws IllegalStateException
     *         if at least one workflow fails to close
     */
    @Override
    public void close()
    {
        Throwable firstCause = null;
        for (Entry<UserType,PlrWebWorkflow> entry : workflowMap_.entrySet())
        {
            try
            {
                entry.getValue().close();
            }
            catch (Throwable t)
            {
                // NOTE: Ensure all workflows are closed, regardless of errors.
                if (firstCause == null)
                {
                    firstCause = t;
                }
                LOG.error("Failed to close workflow ({}).", entry.getKey(), t);
            }
        }
        if (firstCause != null)
        {
            throw new IllegalStateException("Failed to close all workflows.", firstCause);
        }
    }

    /**
     * TODO (AZ) - doc
     */
    public void deselectWorkflow()
    {
        selectedWorkflow_ = null;
        SeleniumContext.get().setSeleniumSession(null);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public PlrWebWorkflow getSelectedWorkflow()
    {
        return selectedWorkflow_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public EnumSet<UserType> getUserTypeSet()
    {
        EnumSet<UserType> userTypeSet;
        if (!workflowMap_.isEmpty())
        {
            userTypeSet = EnumSet.copyOf(workflowMap_.keySet());
        }
        else
        {
            userTypeSet = EnumSet.noneOf(UserType.class);
        }
        return userTypeSet;
    }

    /**
     * TODO (AZ) - doc
     * TODO (AZ) - add a note on the need to select the workflow (i.e. affects the local context)
     *
     * @return ???
     */
    public Set<PlrWebWorkflow> getWorkflowSet()
    {
        return Collections.unmodifiableSet(new HashSet<>(workflowMap_.values()));
    }

    /**
     * TODO (AZ) - doc
     */
    public void logoutAll()
    {
        Throwable firstCause = null;
        for (UserType userType : getUserTypeSet())
        {
            try
            {
                PlrWebWorkflow workflow = selectWorkflow(userType);
                if (workflow.isLoggedIn())
                {
                    workflow.logout();
                }
            }
            catch (Throwable t)
            {
                // NOTE: Ensure all workflows are logged out, regardless of errors.
                if (firstCause == null)
                {
                    firstCause = t;
                }
                LOG.error("Failed to logout workflow ({}).", userType, t);
            }
            finally
            {
                deselectWorkflow();
            }
        }
        if (firstCause != null)
        {
            throw new IllegalStateException("Failed to logout all workflows.", firstCause);
        }
    }

    /**
     * TODO (AZ) - doc
     */
    public void logoutAllAndClose()
    {
        try {
            logoutAll();
        }
        finally
        {
            close();
        }
    }

    /**
     * TODO (AZ) - doc
     *
     * @param parameters
     *        ???
     *
     * @return ???
     *
     * @throws IllegalArgumentException
     *         if the PLR user type cannot be found
     */
    public PlrWebWorkflow selectWorkflow(Object parameters[])
    {
        return selectWorkflow(parameters, null);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param parameters
     *        ???
     *
     * @param defaultUserType
     *        ???
     *
     * @return ???
     *
     * @throws IllegalArgumentException
     *         if the PLR user type cannot be found, and the default PLR user type is {@code null}
     */
    public PlrWebWorkflow selectWorkflow(Object parameters[], UserType defaultUserType)
    {
        UserType userType = PlrUtils.detectUserType(parameters);
        if (userType == null)
        {
            if (defaultUserType != null)
            {
                userType = defaultUserType;
            }
            else
            {
                throw new IllegalArgumentException("PLR user type not found.");
            }
        }
        return selectWorkflow(userType);
    }

    /**
     * TODO (AZ) - doc
     * TODO (AZ) - add a note about affecting the local context
     *
     * @param userType
     *        ???
     *
     * @return ???
     *
     * @throws NullPointerException
     *         if {@code userType} is {@code null}
     */
    public PlrWebWorkflow selectWorkflow(UserType userType)
    {
        requireNonNull(userType, "Null user type.");
        deselectWorkflow();
        LOG.info("User type ({}).", userType);
        PlrWebWorkflow workflow = workflowMap_.get(userType);
        if (workflow == null)
        {
            workflow = PlrWebWorkflow.create(userType);
            workflowMap_.put(userType, workflow);
        }
        SeleniumSession selenium = workflow.getSeleniumSession();
        SeleniumContext.get().setSeleniumSession(selenium);
        selenium.bringToFront();
        selectedWorkflow_ = workflow;
        return selectedWorkflow_;
    }
}
