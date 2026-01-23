package ca.bc.gov.health.qa.autotest.plr.fhir;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.plr.fhir.actions.FHIRSession;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationBuilderFactory;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.PlrFhirResourceType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.PractitionerRelationshipCode;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.FacilityQueryResponseMapper;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.query.IndividualQueryCriteriaParams;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.query.IndividualQueryResponseMapper;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.query.OrgQueryResponseMapper;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.query.OrgQueryCriteriaParams;
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
    private final IndividualBuilderFactory individualFactory;


    /**
     * Constructs a controller bound to a specific user role (credential profile).
     * Sets up factories with data generators.
     * @param userType role whose credentials will be used for FHIR calls
     */
    public FHIRController(UserType userType) {

        facilityFactory = new FacilityBuilderFactory(FacilityDataGenerator.getInstance());
        organizationFactory = new OrganizationBuilderFactory(OrganizationDataGenerator.getInstance());
        individualFactory = new IndividualBuilderFactory(IndividualDataGenerator.getInstance());
        
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
            MaintainOrgBuilder org = createOrganization(roleType);
            String orgIPCId = org.getIdentifier(IdentifierType.IPC);
            String orgIPCName = org.getName();
            builder.addOrganizationRelationship(IdentifierType.IPC, orgIPCId, orgIPCName);
            //LOG.info("Created organization {} for facility relationship (id={})", i + 1, orgIPCId);
        }

        if (config.getRelationshipNames() != null) {
            for (String name : config.getRelationshipNames())
            {
                OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig().withName(name);
                MaintainOrgBuilder org = createOrganization(orgConfig);
                String orgIPCId = org.getIdentifier(IdentifierType.IPC);
                String orgIPCName = org.getName();
                builder.addOrganizationRelationship(IdentifierType.IPC, orgIPCId, orgIPCName);

                relCount++;
            }

        }

        String id = executor.submitMaintain(builder);
        LOG.info("Created facility (id={}) using config{}", id, relCount > 0 ? " with " + relCount + " org relationship(s)" : "");
        builder.identifier(id); // overwrite with server returned id (Should be an IFC identifier)
        return builder;
    }

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
        return facility.copyWithoutOrgRelationships();
    }

    
    /**
     * Generates organization data with only required fields and submits a maintain request.
     *  @param roleType role type to assign to the organization
     * @return created organization values as a MaintainOrgBuilder
     */
    public MaintainOrgBuilder createOrganization(OrgRoleType roleType) {
        return createOrganization(new OrganizationMaintainConfig(roleType));
    }

    /**
     * Generates organization data with customizable fields and submits a maintain request.
     *  @param config configuration of the organization to create
     * @return created organization values as a MaintainOrgBuilder
     */
    public MaintainOrgBuilder createOrganization(OrganizationMaintainConfig config) {
        MaintainOrgBuilder builder = organizationFactory.build(config);

        int facilityRelCount = config.getFacilityRelationshipCount();

        // If facilityRelationshipCount > 0 create that many facilities first and attach relationships
        for (int i = 0; i < facilityRelCount; i++) {
            //Create a facility and save the identifier
            MaintainFacilityBuilder facility = createFacility();
            String facilityIFCId = facility.getIdentifier();
            String facilityName = facility.getName();
            builder.addFacilityRelationship(IdentifierType.IFC, facilityIFCId, facilityName);
        }

        if (config.getFacilityRelationshipNames() != null) {
            for (String name : config.getFacilityRelationshipNames())
            {
                FacilityMaintainConfig facilityConfig = new FacilityMaintainConfig().withName(name);
                MaintainFacilityBuilder facility = createFacility(facilityConfig);
                String facilityIFCId = facility.getIdentifier();
                String facilityName = facility.getName();
                builder.addFacilityRelationship(IdentifierType.IFC, facilityIFCId, facilityName);

                facilityRelCount++;
            }
        }

        int orgRelCount = config.getOrganizationRelationshipCount();
        
        // If organizationRelationshipCount > 0 create that many organizations first and attach relationships
        for (int i = 0; i < orgRelCount; i++) {
            //Create a related organization and save the identifier
            OrgRoleType roleType = OrganizationDataGenerator.getInstance().randomOrgRoleType();
            MaintainOrgBuilder relatedOrg = createOrganization(roleType);
            String relatedOrgId = relatedOrg.getIdentifier(IdentifierType.IPC);
            PractitionerRelationshipCode relationshipCode = OrganizationDataGenerator.getInstance().generatePractitionerRelationshipCode();
            builder.addOrganizationRelationship(IdentifierType.IPC, relatedOrgId, relationshipCode);
        }

        int individualRelCount = config.getIndividualRelationshipCount();
        
        // If individualRelationshipCount > 0 create that many individuals first and attach relationships
        for (int i = 0; i < individualRelCount; i++) {
            //Create a related individual and save the identifier
            MaintainIndividualBuilder relatedIndividual = createIndividual(IndividualDataGenerator.getInstance().randomRoleType(false));
            IdentifierType relatedIndividualIdType = relatedIndividual.getRoleType().getIdentifierType();
            String relatedIndividualId = relatedIndividual.getIdentifier(relatedIndividualIdType);
            PractitionerRelationshipCode relationshipCode = OrganizationDataGenerator.getInstance().generatePractitionerRelationshipCode();
            builder.addIndividualRelationship(relatedIndividualIdType, relatedIndividualId, relationshipCode);
        }


        String id = executor.submitMaintain(builder);
        String logMsg = "";
        if (facilityRelCount > 0 || orgRelCount > 0 || individualRelCount > 0) {
            logMsg = " with";
            if (facilityRelCount > 0) logMsg += " " + facilityRelCount + " facility relationship(s)";
            if (orgRelCount > 0) logMsg += (facilityRelCount > 0 ? " and" : "") + " " + orgRelCount + " organization relationship(s)";
            if (individualRelCount > 0) logMsg += ((facilityRelCount > 0 || orgRelCount > 0) ? " and" : "") + " " + individualRelCount + " individual relationship(s)";
        }
        LOG.info("Created organization (id={}){}", id, logMsg);

        //Set the actual id created by the service (should be an IPC identifier)
        builder.addIdentifier(IdentifierType.IPC, id);
        return builder;
    }

    /**
     * Submits a pre-configured organization builder directly without generating new data.
     * Useful when developers want full control over the builder data.
     * @param builder pre-configured organization builder to submit
     * @return the submitted builder with updated identifier
     */
    public MaintainOrgBuilder submitOrganization(MaintainOrgBuilder builder) {
        String id = executor.submitMaintain(builder);
        LOG.info("Submitted organization (id={})", id);
        builder.addIdentifier(IdentifierType.IPC, id);
        return builder;
    }

    /**
     * Ceases all organization relationships currently configured on the provided organization builder.
     * @param organization existing organization builder whose relationships should be ceased
     * @return same builder instance (for fluent chaining)
     */
    public MaintainOrgBuilder ceaseOrganizationRelationships(MaintainOrgBuilder organization) {
        organization.ceaseRelationships();
        String id = executor.submitMaintain(organization);
        LOG.info("Ceased all relationships for organization (organizationId={}).", id);
        
        // Return a copy without organization relationships to reflect post‑cease state.
        return organization.copyWithoutRelationships();
    }

    /**
     * Creates an individual with a specific role type and default configuration.
     * @param roleType individual role type
     * @return created individual values as a MaintainIndividualBuilder
     */
    public MaintainIndividualBuilder createIndividual(IndividualRoleType roleType) {
        return createIndividual(new IndividualMaintainConfig(roleType));
    }

    /**
     * Generates individual provider data with customizable fields and submits a maintain request.
     *  @param config configuration of the individual to create
     * @return created individual values as a MaintainIndividualBuilder
     */
    public MaintainIndividualBuilder createIndividual(IndividualMaintainConfig config) {
        MaintainIndividualBuilder builder = individualFactory.build(config);

        int orgRelCount = config.getOrganizationRelationshipCount();
        
        // If organizationRelationshipCount > 0 create that many organizations first and attach relationships
        for (int i = 0; i < orgRelCount; i++) {
            //Create a related organization and save the identifier
            OrgRoleType roleType = OrganizationDataGenerator.getInstance().randomOrgRoleType();
            MaintainOrgBuilder relatedOrg = createOrganization(roleType);
            String relatedOrgId = relatedOrg.getIdentifier(IdentifierType.IPC);
            PractitionerRelationshipCode relationshipCode = IndividualDataGenerator.getInstance().generatePractitionerRelationshipCode();
            builder.addOrganizationRelationship(IdentifierType.IPC, relatedOrgId, relationshipCode);
        }

        int individualRelCount = config.getIndividualRelationshipCount();
        
        // If individualRelationshipCount > 0 create that many individuals first and attach relationships
        for (int i = 0; i < individualRelCount; i++) {
            //Create a related individual and save the identifier
            MaintainIndividualBuilder relatedIndividual = createIndividual(IndividualDataGenerator.getInstance().randomRoleType(false));
            IdentifierType relatedIndividualIdType = relatedIndividual.getRoleType().getIdentifierType();
            String relatedIndividualId = relatedIndividual.getIdentifier(relatedIndividualIdType);
            PractitionerRelationshipCode relationshipCode = IndividualDataGenerator.getInstance().generatePractitionerRelationshipCode();
            builder.addIndividualRelationship(relatedIndividualIdType, relatedIndividualId, relationshipCode);
        }

        String id = executor.submitMaintain(builder);
        String logMsg = "";
        if (orgRelCount > 0 || individualRelCount > 0) {
            logMsg = " with";
            if (orgRelCount > 0) logMsg += " " + orgRelCount + " organization relationship(s)";
            if (individualRelCount > 0) logMsg += (orgRelCount > 0 ? " and" : "") + " " + individualRelCount + " individual relationship(s)";
        }
        LOG.info("Created Individual (id={}){}", id, logMsg);

        //Server will automatically assign an IPC identifier
        //Note that some individual roletypes will not generate IPC identifiers on the backend
        builder.addIdentifier(IdentifierType.IPC, id);

        return builder;
    }

    /**
     * Submits a pre-configured individual builder directly without generating new data.
     * Useful when developers want full control over the builder data.
     * @param builder pre-configured individual builder to submit
     * @return the submitted builder with updated identifier
     */
    public MaintainIndividualBuilder submitIndividual(MaintainIndividualBuilder builder) {
        String id = executor.submitMaintain(builder);
        LOG.info("Submitted individual (id={})", id);
        return builder;
    }

    /**
     * Ceases all relationships for a practitioner by setting the end reason code to CEASE and afterwards, sending a maintain request.
     * @param practitioner the practitioner builder with relationships to cease
     * @return updated practitioner builder with relationships marked for cessation
     */
    public MaintainIndividualBuilder ceasePractitionerRelationships(MaintainIndividualBuilder practitioner) {
        practitioner.ceaseRelationships();

        String id = executor.submitMaintain(practitioner);
        LOG.info("Ceased all relationships for individual (individualId={}).", id);
        // Return a copy without organization relationships to reflect post‑cease state.
        return practitioner.copyWithoutRelationships();
    }

    /**
     * Queries FHIR for a facility by identifier type and value.
     *
     * @param idType the identifier system/type to search by
     * @param idValue the identifier value to match
     * @return a builder populated from the FHIR response
     */
    public MaintainFacilityBuilder queryFacilityByIdentifier(IdentifierType idType, String idValue) {

        JSONObject response = executor.queryByIdentifier(PlrFhirResourceType.FACILITY, idType, idValue);
        LOG.info("Facility identifier query result={}", response);

        return FacilityQueryResponseMapper.fromQueryBundle(response);
    }

    /**
     * Queries FHIR for an organization by identifier type and value.
     *
     * @param idType the identifier system/type to search by
     * @param idValue the identifier value to match
     * @return a builder populated from the FHIR response
     */
    public MaintainOrgBuilder queryOrganizationByIdentifier(IdentifierType idType, String idValue) {

        JSONObject response = executor.queryByIdentifier(PlrFhirResourceType.ORGANIZATION, idType, idValue);
        LOG.info("Organization identifier query result={}", response);

        return OrgQueryResponseMapper.fromQueryBundle(response);
    }

    /**
     * Overload: Queries FHIR for an organization by optional criteria using a DTO.
     * @param criteria criteria container; only provided values are sent
     * @return a list of organization builders populated from the FHIR response that match the criteria
     */
    public List<MaintainOrgBuilder> queryOrganizationByCriteria(OrgQueryCriteriaParams criteria) {

        JSONObject response = executor.queryOrganizationByCriteria(criteria.getName(),
                                                                   criteria.getDescription(),
                                                                   criteria.getRoleType(),
                                                                   criteria.getAddressCity(),
                                                                   criteria.getAddressLine1(),
                                                                   criteria.isWithHistory());

        

        LOG.info("Organization criteria query result={}", response);
        return OrgQueryResponseMapper.fromQueryBundleAll(response);
    }

    /**
     * Queries FHIR for an individual by identifier type and value.
     *
     * @param idType the identifier system/type to search by
     * @param idValue the identifier value to match
     * @return a builder populated from the FHIR response
     */
    public MaintainIndividualBuilder queryIndividualByIdentifier(IdentifierType idType, String idValue) {

        JSONObject response = executor.queryByIdentifier(PlrFhirResourceType.INDIVIDUAL, idType, idValue);
        LOG.info("Individual identifier query result={}", response);

        return IndividualQueryResponseMapper.fromQueryBundle(response);
    }

    /**
     * Queries FHIR for individuals (practitioners) by optional criteria using a DTO.
     * @param criteria criteria container; only provided values are sent
     * @return a list of individual builders populated from the FHIR response that match the criteria
     */
    public List<MaintainIndividualBuilder> queryIndividualByCriteria(IndividualQueryCriteriaParams criteria) {

        JSONObject response = executor.queryIndividualByCriteria(
                                                                   criteria.getRoleType(),
                                                                   criteria.getAddressCity(),
                                                                   criteria.getFamily(),
                                                                   criteria.getExpertise(),
                                                                   criteria.getCommunication(),
                                                                   criteria.getGiven(),
                                                                   criteria.getStatusReason(),
                                                                   criteria.getStatus(),
                                                                   criteria.getGender(),
                                                                   criteria.isWithHistory());

        LOG.info("Individual criteria query result={}", response);
        return IndividualQueryResponseMapper.fromQueryBundleAll(response);
    }

    @Override
    public void close() {
        try { executor.close(); } catch (Exception ignore) {}
    }
}