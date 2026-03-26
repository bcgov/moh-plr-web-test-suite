package ca.bc.gov.health.qa.autotest.plr.web.actions.provider;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add.AddProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderRoleType;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

import java.util.List;
import java.util.Map;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.viewByIdentifierAsUpdateProvider;
import static ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper.generateNumericString;

public class UpdateProviderActions extends AddProviderActions {

    final List<ProviderRoleType> noPermRoles = List.of(
            ProviderRoleType.RPN,
            ProviderRoleType.RM,
            ProviderRoleType.PHARM,
            ProviderRoleType.HA);

    /**
     * Initializes class and SeleniumSession.
     *
     * @param selenium The current SeleniumSession
     */
    public UpdateProviderActions(SeleniumSession selenium) {
        super(selenium);
    }

    /**
     * Creates an individual with the given role type and returns the UpdateProviderPage for that individual.
     * @param workflowManager a PlrWebWorkflowManager to use for navigating the PLR web application
     * @param fhirController a FHIRController to use for creating the individual via FHIR
     * @param roleType the role type to create the individual with
     * @param providerType the provider type to create the individual with
     * @param defaultProviders a map of default providers to use for certain role types
     * @return UpdateProviderPage for the created individual
     */
    public UpdateProviderPage createIndividualByRoleType(PlrWebWorkflowManager workflowManager, FHIRController fhirController,
                                                         ProviderRoleType roleType, ProviderType providerType,
                                                         Map<ProviderType, MaintainIndividualBuilder> defaultProviders) {
        PlrWebWorkflow workflow = workflowManager.selectWorkflow(UserType.ADMIN);
        if (roleType.equals(ProviderRoleType.OPT) || roleType.equals(ProviderRoleType.OOPRECT)) {
            return viewByIdentifierAsUpdateProvider(defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC), workflowManager);
        } else if (noPermRoles.contains(roleType)) {
            IdentifierType idType = IndividualRoleType.resolveRoleType(roleType.getCode()).getIdentifierType();
            AddProviderPage rolePage = workflow.getPlrWebAccessActions().openAddProvider();
            rolePage.fillIdentifier(roleType, null, null, idType.name(), generateNumericString(15));
            finishCreateFlow(rolePage, providerType, "Status");

            return new UpdateProviderPage(workflow.getSeleniumSession(),
                    workflow.getURUri().resolve("/plr/ProviderDetails.xhtml"));
        } else {
            IndividualRoleType fhirType = IndividualRoleType.resolveRoleType(roleType.getCode());
            MaintainIndividualBuilder builder = fhirController.createIndividual(new IndividualMaintainConfig(fhirType));

            return viewByIdentifierAsUpdateProvider(builder.getIdentifier(IdentifierType.IPC), workflowManager);
        }
    }
}
