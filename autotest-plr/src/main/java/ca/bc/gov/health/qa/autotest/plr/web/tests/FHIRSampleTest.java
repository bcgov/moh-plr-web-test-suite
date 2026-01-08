package ca.bc.gov.health.qa.autotest.plr.web.tests;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;

public class FHIRSampleTest
implements SimpleTest
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    public FHIRSampleTest()
    {}

    @Test
    public void test0()
    {

        //Start Controller - Passed parameter will determine user role for FHIR calls. 
        //To change user role, call fhirController.changeFHIRSession(UserType.<ROLE>).
        FHIRController fhirController = new FHIRController(UserType.ADMIN);

        /***************FACILITY****************/

        //Configuration to determine what data to include when creating a Facility with random values.
        // Required attributes are included by default.
        FacilityMaintainConfig cfg = new FacilityMaintainConfig()
            //.withAddress()       added by default as is a required attribute
            //.withIdentifier()    added by default as is a required attribute
            //.withName()          added by default as is a required attribute
            //.withAllTelecom()    convenience method to add all telecom types
            //.withAllAttributes(int notes, int orgRelationships) convenience method to add all attributes
            .withPhone()
            .withEmail()
            .withFax()
            .withFtp()
            .withMobile()
            .withModem()
            .withPager()
            .withWebsite()
            .withDescription()
            .withNotes(2)
            .withOrgRelationships(2);

        //Create Facility through FHIRController using the configuration above.
        //Resulting MaintainFacilityBuilder contains the created facility data.
        MaintainFacilityBuilder facility = fhirController.createFacility(cfg);

        LOG.info("Created facility id {}, name {}, address {}, description {}, telecoms {}, notes {}, relationships {}.", facility.getIdentifier(), facility.getName(), facility.getAddress().toString(), facility.getDescription(), facility.getTelecomList(), facility.getNoteList(), facility.getOrgRelationshipList());

        //Query the created facility by its identifier. 
        //Resulting MaintainFacilityBuilder contains the queried facility data.
        MaintainFacilityBuilder queriedFacility = fhirController.queryFacilityByIdentifier(IdentifierType.IFC, facility.getIdentifier());

        LOG.info("Queried facility id {}, name {}, address {}, description {}, telecoms {}, notes {}, relationships {}.", queriedFacility.getIdentifier(), queriedFacility.getName(), queriedFacility.getAddress().toString(), queriedFacility.getDescription(), queriedFacility.getTelecomList(), queriedFacility.getNoteList(), queriedFacility.getOrgRelationshipList());

        //Cease relationships for the facility by sending a FHIR request.
        //Returns an updated MaintainFacilityBuilder with no organization relationships.
        facility = fhirController.ceaseFacilityRelationships(facility);

        LOG.info("Ceased facility relationships for facility id {}, name {}, address {}, description {}, telecoms {}, notes {}, relationships {}.", facility.getIdentifier(), facility.getName(), facility.getAddress().toString(), facility.getDescription(), facility.getTelecomList(), facility.getNoteList(), facility.getOrgRelationshipList());

        /***************ORGANIZATION****************/

        //Create an organization with random data and specified role type.
        //Note that the saved organization identifier is an IPC identifier.
        OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.HDS)
            //.withAllAttributes(2, 2, 2, 2, 2, 2) convenience method to add all attributes including org properties
            //.withAllOrgProperties(0, 0, 0, 0) convenience method to add all organization properties
            //.withName() by default as is a required attribute
            //.withIdentifier() by default as is a required attribute
            //.withRoleType(OrgRoleType.HDS) already passed on constructor. HDS by default. also required attribute
            .withAlias()
            .withConfidentiality()
            //.withAddress() by default as is a required attribute
            .withAllTelecom()
            .withStatuses(2)
            .withNotes(2)
            .withClinicServices()
            .withClinicOwnerBusinessType()
            .withClinicType()
            .withClinicLegalBusinessName()
            //.withAddressUnit(2) TODO - Issues with address unit. Implement logic to query parse and use in maintain later
            //.withClinicHoursOfOperation(2) TODO - Missing logic. Implement logic to query parse and use in maintain later
            .withClinicOwnerNames(2)
            .withPayeeNumber(2)
            .withPciFlag();           

        MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);

        LOG.info("Created organization id {}, name {}.", org.getIdentifiers(), org.getName());

        //Query an organization by its IPC identifier.
        //Resulting MaintainOrgBuilder contains the queried organization data.
        MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC, org.getIdentifier(IdentifierType.IPC));

        LOG.info("Queried organization id {}, name {}, role type {}, HDS type {}, status {}, alias {}, address {}, telecoms {}, notes {}.", orgQueried.getIdentifiers(), orgQueried.getName(), orgQueried.getRoleType(), orgQueried.getHdsType(), orgQueried.getStatusList(), orgQueried.getAlias(), orgQueried.getAddressList(), orgQueried.getTelecomList(), orgQueried.getNoteList());

        //Close the FHIR session
        fhirController.close();
           
    }

}
