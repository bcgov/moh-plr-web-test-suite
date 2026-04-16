
package ca.bc.gov.health.qa.autotest.plr.web.actions.provider;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.viewByIdentifierAsUpdateProvider;
import static ca.bc.gov.health.qa.autotest.plr.web.tests.helper.UpdateSimpleHelper.generateNumericString;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
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
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

public class UpdateProviderActions extends AddProviderActions {
	
	private static final Logger LOG = ExecutionLogManager.getLogger();
	private final SeleniumSession selenium_;
	private final URI uri_;
	private final UserType userType_;

	final List<ProviderRoleType> noPermRoles = List.of(ProviderRoleType.RPN, ProviderRoleType.RM,
			ProviderRoleType.PHARM, ProviderRoleType.HA);

	/**
	 * Initializes class and SeleniumSession.
	 *
	 * @param selenium The current SeleniumSession
	 */
	//public UpdateProviderActions(SeleniumSession selenium) {
//		super(selenium);
//	}
	
	public UpdateProviderActions(SeleniumSession selenium, URI uri, UserType userType) {
		super(selenium);
		selenium_ = selenium;
		uri_ = uri;
		userType_ = userType;

	}
	/**
	 * Creates an individual with the given role type and returns the
	 * UpdateProviderPage for that individual.
	 * 
	 * @param workflowManager  a PlrWebWorkflowManager to use for navigating the PLR
	 *                         web application
	 * @param fhirController   a FHIRController to use for creating the individual
	 *                         via FHIR
	 * @param roleType         the role type to create the individual with
	 * @param providerType     the provider type to create the individual with
	 * @param defaultProviders a map of default providers to use for certain role
	 *                         types
	 * @return UpdateProviderPage for the created individual
	 */
	public UpdateProviderPage createIndividualByRoleType(PlrWebWorkflowManager workflowManager,
			FHIRController fhirController, ProviderRoleType roleType, ProviderType providerType,
			Map<ProviderType, MaintainRequestBuilder> defaultProviders) {
		PlrWebWorkflow workflow = workflowManager.selectWorkflow(UserType.ADMIN);
		if (roleType.equals(ProviderRoleType.OPT) || roleType.equals(ProviderRoleType.OOPRECT)) {
			return viewByIdentifierAsUpdateProvider(
					defaultProviders.get(providerType).getIdentifier(IdentifierType.IPC), workflowManager);
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
	/**
	 * Gets the selenium_ value.
	 *
	 * @return the selenium_
	 */
	public SeleniumSession getSelenium_() {
		return selenium_;
	}

	/**
	 * Gets the uri_ value.
	 *
	 * @return the uri_
	 */
	public URI getUri_() {
		return uri_;
	}

	/**
	 * Gets the userType_ value.
	 *
	 * @return the userType_
	 */
	public UserType getUserType_() {
		return userType_;
	}

	/**
	 * Opens the provider page for a provider given their internal provider ID.
	 *
	 * @param authId internal provider ID
	 * @return a ViewProviderPage reference to the provider page specified by authId
	 *
	 * @throws NullPointerException if {@code pauthId} is {@code null}
	 */
	public UpdateProviderPage openProvider(String authId) {
		LOG.info("Open provider view ({}).", authId);
		UpdateProviderPage updateProvider = new UpdateProviderPage(selenium_,
				uri_.resolve("plr/ProviderDetails.xhtml"));
		updateProvider.openProvider(authId);
		return updateProvider;
	}

	/**
	 * get Status Code To Reason Code Map
	 * 
	 * @return a Map of key=String of Status Code, value=List String of Reason
	 *         Code
	 */
	public Map<String, List<String>> getStatusCodeToReasonCodeMap() {

		String[] cancelArray = { "AU - Address Unknown", "INNONPRAC - Initial Non Practicing",
				"LAP - License Lapsed on Request", "DEN - Licensed Denied", "MEDSTUD - Medical Student",
				"ORG - Organization Provider", "OOP - Out of Province", "RESDISC - Resigned - disciplinary action",
				"RET - Retired", "UNK - Unknown", "VW - Voluntary Withdrawal" };
		String[] activeArray = { "ASSOC - Associate", "GS - Good Standing", "LAP - License Lapsed on Request",
				"MEDSTUD - Medical Student", "NONPRAC - Non Practicing", "ORG - Organization Provider",
				"OOP - Out of Province", "PRAC - Practising", "RET - Retired", "SPE - Special Registry",
				"TEMPPER - Temporary Permit", "UNK - Unknown" };
		String[] terminatedArray = { "AU - Address Unknown", "DEC - Deceased", "ERSRES - Erased by Resolution",
				"HON - Honorary", "LTP - Left the Province", "LAP - License Lapsed on Request",
				"MEDSTUD - Medical Student", "NONPRAC - Non Practicing", "NR - Non-resident",
				"ORG - Organization Provider", "OOP - Out of Province", "RESDISC - Resigned - disciplinary action",
				"RET - Retired", "TI - Temporary Inactive", "TSF - Transfer", "UNK - Unknown" };
		String[] inactiveArray = { "MEDSTUD - Medical Student", "ORG - Organization Provider", "OOP - Out of Province",
				"UNK - Unknown" };
		String[] suspendedArray = { "AU - Address Unknown", "HON - Honorary", "LTP - Left the Province",
				"LAP - License Lapsed on Request", "MEDSTUD - Medical Student", "MIS - Missionary",
				"NONPAY - Non Payment of Fee", "NONPRAC - Non Practicing", "NR - Non-resident",
				"ORG - Organization Provider", "OOP - Out of Province", "RESDISC - Resigned - disciplinary action",
				"RET - Retired", "SUS - Suspended", "TI - Temporary Inactive", "UNK - Unknown",
				"VW - Voluntary Withdrawal" };
		String[] nullifiedArray = { "MEDSTUD - Medical Student", "ORG - Organization Provider", "OOP - Out of Province",
				"UNK - Unknown" };
		String[] pendingArray = { "INNONPRAC - Initial Non Practicing", "MEDSTUD - Medical Student",
				"NONPRAC - Non Practicing", "ORG - Organization Provider", "OOP - Out of Province", "UNK - Unknown" };
		String[] unknownArray = { "MEDSTUD - Medical Student", "ORG - Organization Provider", "OOP - Out of Province",
				"UNK - Unknown" };

		Map<String, List<String>> map = new HashMap<>();

		map.put("CANCELLED - Cancelled", Arrays.asList(cancelArray));
		map.put("ACTIVE - Active", Arrays.asList(activeArray));
		map.put("TERMINATED - Terminated", Arrays.asList(terminatedArray));
		map.put("INACTIVE - Inactive", Arrays.asList(inactiveArray));
		map.put("SUSPENDED - Suspended", Arrays.asList(suspendedArray));
		map.put("NULLIFIED - Nullified", Arrays.asList(nullifiedArray));
		map.put("PENDING - Pending", Arrays.asList(pendingArray));
		map.put("UNKNOWN - Unknown", Arrays.asList(unknownArray));
		return map;
	}


}
