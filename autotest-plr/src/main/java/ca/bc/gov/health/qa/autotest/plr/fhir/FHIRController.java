package ca.bc.gov.health.qa.autotest.plr.fhir;

import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.plr.fhir.actions.FHIRSession;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.MaintainFacilityFields;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * High-level facade for creating and querying FHIR resources used in tests.
 * Abstracts away data generator + fhir session wiring so tests can stay concise.
 */
public class FHIRController implements AutoCloseable {

    private static final Logger LOG = ExecutionLogManager.getLogger();

    private FHIRSession executor;
    private final FacilityDataGenerator facilityGen = FacilityDataGenerator.getInstance();

    /**
     * Constructs a controller bound to a specific user role (credential profile).
     * @param userType role whose credentials will be used for FHIR calls
     */
    public FHIRController(UserType userType) {
        changeFHIRSession(userType);
    }

    /*
     * Convenience method to renew the FHIR Session or change the user role 
     * (credential profile) used for subsequent FHIR calls.
     * @param userType new role whose credentials will be used for FHIR calls
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
        MaintainFacilityBuilder builder = facilityGen.generateFacilityBuilder();

        String id = executor.submitMaintain(builder);
        LOG.info("Created facility (id={})", id);

        //Set the actual id created by the service
        builder.identifier(id);
        return builder;
    }

    /**
     * Generates facility data with specified optional fields and submits a maintain request.
     * Convenience method for variable arguments.
     *
     * @param optionalFields variable arguments of optional fields to include
     * @return created facility values as a MaintainFacilityBuilder
     */
    public MaintainFacilityBuilder createFacility(MaintainFacilityFields... optionalFields) {
        MaintainFacilityBuilder builder = facilityGen.generateFacilityBuilder(optionalFields);

        String id = executor.submitMaintain(builder);
        LOG.info("Created facility (id={})", id);

        //Set the actual id created by the service
        builder.identifier(id);
        return builder;
    }

    //TODO: createOrganization()

    //TODO: createPractitioner()

    //TODO: ceaseFacility(MaintainFacilityBuilder facility)

    //TODO: ceaseFacility(IdentifierType identifier)

    //TODO: ceaseOrganization(MaintainOrgBuilder org)

    //TODO: ceaseOrganization(IdentifierType identifier)

    //TODO: ceasePractitioner(MaintainPracBuilder prac)

    //TODO: ceasePractitioner(IdentifierType identifier)

    @Override
    public void close() {
        try { executor.close(); } catch (Exception ignore) {}
    }
}