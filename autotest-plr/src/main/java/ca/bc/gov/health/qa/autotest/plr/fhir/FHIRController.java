package ca.bc.gov.health.qa.autotest.plr.fhir;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.plr.fhir.actions.FHIRSession;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.OrganizationBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.OrganizationDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.PlrFhirResourceType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.FacilityQueryResponseMapper;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.OrgQueryResponseMapper;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * High-level facade for creating and querying FHIR resources used in tests.
 * Abstracts away data generator + fhir session wiring so tests can stay concise.
 */
public class FHIRController implements AutoCloseable {

    private static final Logger LOG = ExecutionLogManager.getLogger();

    private FHIRSession executor;
    private final FacilityBuilderFactory facilityFactory;    
    private final OrganizationBuilderFactory organizationFactory;

    /**
     * Constructs a controller bound to a specific user role (credential profile).
     * Sets up factories with data generators.
     * @param userType role whose credentials will be used for FHIR calls
     */
    public FHIRController(UserType userType) {

        facilityFactory = new FacilityBuilderFactory(FacilityDataGenerator.getInstance());
        organizationFactory = new OrganizationBuilderFactory(OrganizationDataGenerator.getInstance());

        changeFHIRSession(userType);
    }

    /**
     * Convenience method to (re)initialize the underlying {@link FHIRSession} using the
     * credentials associated with the supplied {@link UserType}. Existing session (if any)
     * is discarded and a new one created; subsequent maintain / query operations will use
     * this security context.
     *
     * @param userType user / role whose credentials should back the FHIR session
     */
    public void changeFHIRSession(UserType userType) {
        this.executor = new FHIRSession(userType);
    }

    /**
     * Generates facility data with only required fields and submits a maintain request.
     *
     * @return created facility values as a MaintainFacilityBuilder
     */
    public MaintainFacilityBuilder createFacility() {
        MaintainFacilityBuilder builder = facilityFactory.build();

        String id = executor.submitMaintain(builder);
        LOG.info("Created facility (id={})", id);

        //Set the actual id created by the service
        builder.identifier(id);
        return builder;
    }


    /**
     * Creates a facility using a configuration object that can specify counts (e.g. notes, relationships)
     * in addition to optional scalar fields.
     *
     * @param config configuration describing optional fields and counts
     * @return created facility values as a MaintainFacilityBuilder
     */
    public MaintainFacilityBuilder createFacility(FacilityMaintainConfig config) {
        if (config == null) {
            return createFacility();
        }
        MaintainFacilityBuilder builder = facilityFactory.build(config);

        int relCount = config.getRelationshipCount();

        // If relationshipCount > 0 create that many organizations first and attach relationships
        for (int i = 0; i < relCount; i++) {
            //Create an organization and save the identifier
            OrgRoleType roleType = OrganizationDataGenerator.getInstance().randomOrgRoleType();
            String orgIPCId = createOrganization(roleType).getIdentifier();
            builder.addOrganizationRelationship(IdentifierType.IPC, orgIPCId);
            //LOG.info("Created organization {} for facility relationship (id={})", i + 1, orgIPCId);
        }

        String id = executor.submitMaintain(builder);
        LOG.info("Created facility (id={}) using config{}", id, relCount > 0 ? " with " + relCount + " org relationship(s)" : "");
        builder.identifier(id); // overwrite with server returned id (Should be an IFC identifier)
        return builder;
    }

    /**
     * Generates organization data with only required fields and submits a maintain request.
     *  @param roleType role type to assign to the organization
     * @return created organization values as a MaintainOrgBuilder
     */
    public MaintainOrgBuilder createOrganization(OrgRoleType roleType) {
        MaintainOrgBuilder builder = organizationFactory.build(new OrganizationMaintainConfig(roleType));

        String id = executor.submitMaintain(builder);
        LOG.info("Created organization (id={})", id);

        //Set the actual id created by the service (should be an IPC identifier)
        builder.identifier(id);
        return builder;
    }

    //TODO: createPractitioner()

    //TODO: createOrganization(OrganizationMaintainConfig config)

    //TODO: ceaseFacility(MaintainFacilityBuilder facility)

    /**
     * Ceases all organization relationships currently configured on the provided facility builder.
     * The builder is submitted and its identifier updated with the returned IFC id.
     * @param facility existing facility builder whose relationships should be ceased
     * @return same builder instance (for fluent chaining)
     */
    public MaintainFacilityBuilder ceaseFacilityRelationships(MaintainFacilityBuilder facility) {
        facility.ceaseOrganizationRelationships();
        String id = executor.submitMaintain(facility);
        LOG.info("Ceased facility relationships (facilityId={}).", id);
        facility.identifier(id);
        // Return a copy without organization relationships to reflect post‑cease state.
        // TODO: When more cease/correction operations emerge, introduce a FacilityMutatorConfig parameter
        //       to make this method delegate to a generic maintain+mutate pipeline
        return facility.copyWithoutOrgRelationships();
    }

    //TODO: ceaseFacility(IdentifierType identifier)

    //TODO: ceaseOrganization(MaintainOrgBuilder org)

    //TODO: ceaseOrganization(IdentifierType identifier)

    //TODO: ceasePractitioner(MaintainPracBuilder prac)

    //TODO: ceasePractitioner(IdentifierType identifier)

    public MaintainFacilityBuilder queryFacilityByIdentifier(IdentifierType idType, String idValue) {

        JSONObject response = executor.queryByIdentifier(PlrFhirResourceType.FACILITY, idType, idValue);
        LOG.info("Facility query result={}", response);

        return FacilityQueryResponseMapper.fromQueryBundle(response);
    }

    public MaintainOrgBuilder queryOrganizationByIdentifier(IdentifierType idType, String idValue) {

        JSONObject response = executor.queryByIdentifier(PlrFhirResourceType.ORGANIZATION, idType, idValue);
        LOG.info("Organization query result={}", response);

        return OrgQueryResponseMapper.fromQueryBundle(response);
    }

    @Override
    public void close() {
        try { executor.close(); } catch (Exception ignore) {}
    }
}