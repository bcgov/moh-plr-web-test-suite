package ca.bc.gov.health.qa.autotest.plr.web.tests;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.query.IndividualQueryCriteriaParams;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.query.OrgQueryCriteriaParams;
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
    public void facilityTest()
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

        //Close the FHIR session
        fhirController.close();
           
    }

    @Test
    public void organizationTest()
    {

        //Start Controller - Passed parameter will determine user role for FHIR calls. 
        //To change user role, call fhirController.changeFHIRSession(UserType.<ROLE>).
        FHIRController fhirController = new FHIRController(UserType.ADMIN);

        /***************ORGANIZATION****************/

        //Create an organization with random data and specified role type.
        //Note that the saved organization identifier is an IPC identifier.
        OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.HDS)
            //.withAllAttributes(2, 2, 2, 2, 2, 2, 2) convenience method to add all attributes including org properties and relationships
            //.withAllOrgProperties(0, 0, 0, 0) convenience method to add all organization properties
            //.withName() by default as is a required attribute
            //.withIdentifier() by default as is a required attribute
            //.withRoleType(OrgRoleType.HDS) already passed on constructor. HDS by default. also required attribute
            .withAlias()
            //.withConfidentiality() //Note that editing the record will not be possible if confidentiality is set to true.
            //.withAddress() by default as is a required attribute
            .withAllTelecom()
            .withStatuses(2) //Note that right now the maximum amount of confidentiality that can be added is 2
            .withNotes(2)
            .withClinicServices()
            .withClinicOwnerBusinessType()
            .withClinicType()
            .withClinicLegalBusinessName()
            //.withAddressUnit(2) TODO - Issues with address unit. Implement logic to query parse and use in maintain later
            .withClinicHoursOfOperation(2)
            .withClinicOwnerNames(2)
            .withPayeeNumber(2)
            .withFacilityRelationships(2)
            .withOrganizationRelationships(2)
            .withIndividualRelationships(2)
            .withPciFlag();        

        MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);

        LOG.info("Created organization id {}, name {} fac relationships {}, org relationships {}, ind relationships {}.", org.getIdentifiers(), org.getName(), org.getFacilityRelationshipList(), org.getOrganizationRelationshipList(), org.getIndividualRelationshipList());

        //Query an organization by its IPC identifier.
        //Resulting MaintainOrgBuilder contains the queried organization data.
        MaintainOrgBuilder orgQueried = fhirController.queryOrganizationByIdentifier(IdentifierType.IPC, org.getIdentifier(IdentifierType.IPC));

        LOG.info("Queried organization id {}, name {}, role type {}, HDS type {}, status {}, alias {}, confidentiality {}, address {}, telecoms {}, notes {}, clinicServices {}, clinicOwnerBuisnessType {}, clinicType {}, clinicLegalBusinessName {}, clinicOwnerNames {}, payeeNumber {}, pciFlag {}, hoursOfOperation {}, fac relationships {}, org relationships {}, ind relationships {}.", orgQueried.getIdentifiers(), orgQueried.getName(), orgQueried.getRoleType(), orgQueried.getHdsType(), orgQueried.getStatusList(), orgQueried.getAlias(), orgQueried.getConfidentiality(), orgQueried.getAddressList(), orgQueried.getTelecomList(), orgQueried.getNoteList(), orgQueried.getOrganizationProperties().getClinicServices().getText(), orgQueried.getOrganizationProperties().getClinicOwnerBusinessType().getText(), orgQueried.getOrganizationProperties().getClinicType().getText(), orgQueried.getOrganizationProperties().getClinicLegalBusinessName(), orgQueried.getOrganizationProperties().getClinicOwnerNames(), orgQueried.getOrganizationProperties().getPayeeNumber(), orgQueried.getOrganizationProperties().getPciFlag(), orgQueried.getOrganizationProperties().getClinicHoursOfOperation(), orgQueried.getFacilityRelationshipList(), orgQueried.getOrganizationRelationshipList(), orgQueried.getIndividualRelationshipList());

        //cease all relationships that an organization has
        org = fhirController.ceaseOrganizationRelationships(org);

        LOG.info("Ceased relationships for organization id {}, name {}, role type {}, fac relationships {}, org relationships {}, ind relationships.", org.getIdentifiers(), org.getName(), org.getRoleType(), org.getFacilityRelationshipList(), org.getOrganizationRelationshipList(), org.getIndividualRelationshipList());

        
        List<MaintainOrgBuilder> orgQueriedbyCriteria = fhirController.queryOrganizationByCriteria(
            new OrgQueryCriteriaParams()
                .setRoleType(OrgRoleType.HDS)
                .setAddressCity("Vancouver")
                //.setName("ExampleName")
                //.setWithHistory(true) //example of boolean param
        );

        for (MaintainOrgBuilder orgByCriteria : orgQueriedbyCriteria) {
            LOG.info("Queried organization id {}, name {}, role type {}, HDS type {}, status {}, alias {}, confidentiality {}, address {}, telecoms {}, notes {}, clinicServices {}, clinicOwnerBuisnessType {}, clinicType {}, clinicLegalBusinessName {}, clinicOwnerNames {}, payeeNumber {}, pciFlag {}, hoursOfOperation {}.", orgByCriteria.getIdentifiers(), orgByCriteria.getName(), orgByCriteria.getRoleType(), orgByCriteria.getRoleType() == OrgRoleType.HDS ? orgByCriteria.getHdsType() : "null", orgByCriteria.getStatusList(), orgByCriteria.getAlias(), orgByCriteria.getConfidentiality(), orgByCriteria.getAddressList(), orgByCriteria.getTelecomList(), orgByCriteria.getNoteList(), orgByCriteria.getOrganizationProperties().getClinicServices(), orgByCriteria.getOrganizationProperties().getClinicOwnerBusinessType(), orgByCriteria.getOrganizationProperties().getClinicType(), orgByCriteria.getOrganizationProperties().getClinicLegalBusinessName(), orgByCriteria.getOrganizationProperties().getClinicOwnerNames(), orgByCriteria.getOrganizationProperties().getPayeeNumber(), orgByCriteria.getOrganizationProperties().getPciFlag(), orgByCriteria.getOrganizationProperties().getClinicHoursOfOperation());
        }

        //Close the FHIR session
        fhirController.close();
           
    }

    @Test
    public void individualTest(){

        //Start Controller - Passed parameter will determine user role for FHIR calls. 
        //To change user role, call fhirController.changeFHIRSession(UserType.<ROLE>).
        FHIRController fhirController = new FHIRController(UserType.ADMIN);

        /***************Individual****************/

         IndividualMaintainConfig individualConfig = new IndividualMaintainConfig()
            //.withAllAttributes(2, 2, 2, 2, 2, 2, 2, 2) convenience method to add all attributes
            //.withIdentifier() by default as is a required attribute
            //.withFamilyName() by default as is a required attribute
            //.withNames() by default as is a required attribute
            //.withRoleType() by default as is a required attribute
            //.withGivenNames() by default as is a required attribute
            //.withDemographics() by default as is a required attribute
            //.withAddress() by default as is a required attribute
            .withRoleType(IndividualRoleType.RN)
            .withAllTelecom()
            .withStatuses(2) //Note that right now the maximum amount of confidentiality that can be added is 2
            .withNotes(2)
            .withExpertise(2)
            .withCredentials(2)
            .withDisciplinaryActions(2)
            .withOrganizationRelationships(2)
            .withIndividualRelationships(2)
            .withConditions(2);
            //.withConfidentiality(); //Note that editing the record will not be possible if confidentiality is set to true.

        MaintainIndividualBuilder individual = fhirController.createIndividual(individualConfig);
        
        LOG.info(
            "Created Individual identifiers {}, addresses {}, conditions {}, confidentiality {}, credentials {}, disciplinaryActions {}, familyName {}, names {}, demographics {}, expertises {}, notes {}, roleType {}, statuses {}, telecoms {}, org relationships {}, ind relationships {}",
            individual.getIdentifiers(),
            individual.getAddressList(),
            individual.getConditionList(),
            individual.getConfidentiality(),
            individual.getCredentialList(),
            individual.getDisciplinaryActionList(),
            individual.getFamilyName(),
            Arrays.toString(individual.getNames()),
            individual.getDemographics(),
            individual.getExpertiseList(),
            individual.getNoteList(),
            individual.getRoleType(),
            individual.getStatusList(),
            individual.getTelecomList(),
            individual.getOrganizationRelationshipList(),
            individual.getIndividualRelationshipList()
        );

        //Other option to create an individual with default config
        //Note that between the IndividualRoleType enum, OOP role types are incluedd
        /*MaintainIndividualBuilder individual2 = fhirController.createIndividual(IndividualRoleType.OOP_MD);

        LOG.info(
            "Created Individual identifiers {}, addresses {}, roleType {}",
            individual2.getIdentifiers(),
            individual2.getAddressList(),
            individual2.getRoleType());*/

        MaintainIndividualBuilder queriedIndividual = fhirController.queryIndividualByIdentifier(IdentifierType.IPC, individual.getIdentifier(IdentifierType.IPC));

        LOG.info(
            "Queried Individual identifiers {}, addresses {}, conditions {}, confidentiality {}, credentials {}, disciplinaryActions {}, familyName {}, names {}, demographics {}, expertises {}, notes {}, roleType {}, statuses {}, telecoms {}, org relationships {}, ind relationships {}",
            queriedIndividual.getIdentifiers(),
            queriedIndividual.getAddressList(),
            queriedIndividual.getConditionList(),
            queriedIndividual.getConfidentiality(),
            queriedIndividual.getCredentialList(),
            queriedIndividual.getDisciplinaryActionList(),
            queriedIndividual.getFamilyName(),
            Arrays.toString(queriedIndividual.getNames()),
            queriedIndividual.getDemographics(),
            queriedIndividual.getExpertiseList(),
            queriedIndividual.getNoteList(),
            queriedIndividual.getRoleType(),
            queriedIndividual.getStatusList(),
            queriedIndividual.getTelecomList(),
            queriedIndividual.getOrganizationRelationshipList(),
            queriedIndividual.getIndividualRelationshipList()
        );

        individual = fhirController.ceasePractitionerRelationships(individual);

        LOG.info(
            "Ceased relationships for Queried Individual identifiers {}, addresses {}, conditions {}, confidentiality {}, credentials {}, disciplinaryActions {}, familyName {}, names {}, demographics {}, expertises {}, notes {}, roleType {}, statuses {}, telecoms {}, org relationships {}, ind relationships {}",
            individual.getIdentifiers(),
            individual.getAddressList(),
            individual.getConditionList(),
            individual.getConfidentiality(),
            individual.getCredentialList(),
            individual.getDisciplinaryActionList(),
            individual.getFamilyName(),
            Arrays.toString(individual.getNames()),
            individual.getDemographics(),
            individual.getExpertiseList(),
            individual.getNoteList(),
            individual.getRoleType(),
            individual.getStatusList(),
            individual.getTelecomList(),
            individual.getOrganizationRelationshipList(),
            individual.getIndividualRelationshipList()
        );

        queriedIndividual = fhirController.queryIndividualByIdentifier(IdentifierType.IPC, individual.getIdentifier(IdentifierType.IPC));

        LOG.info(
            "Queried Individual identifiers {}, addresses {}, conditions {}, confidentiality {}, credentials {}, disciplinaryActions {}, familyName {}, names {}, demographics {}, expertises {}, notes {}, roleType {}, statuses {}, telecoms {}, org relationships {}, ind relationships {}",
            queriedIndividual.getIdentifiers(),
            queriedIndividual.getAddressList(),
            queriedIndividual.getConditionList(),
            queriedIndividual.getConfidentiality(),
            queriedIndividual.getCredentialList(),
            queriedIndividual.getDisciplinaryActionList(),
            queriedIndividual.getFamilyName(),
            Arrays.toString(queriedIndividual.getNames()),
            queriedIndividual.getDemographics(),
            queriedIndividual.getExpertiseList(),
            queriedIndividual.getNoteList(),
            queriedIndividual.getRoleType(),
            queriedIndividual.getStatusList(),
            queriedIndividual.getTelecomList(),
            queriedIndividual.getOrganizationRelationshipList(),
            queriedIndividual.getIndividualRelationshipList()
        );

        //Query by criteria will return a list of individuals that match the criteria
        List<MaintainIndividualBuilder> individualQueriedByCriteria = fhirController.queryIndividualByCriteria(
            new IndividualQueryCriteriaParams() //Look at class to see available parameters
                .setAddressCity("Victoria"));

        for (MaintainIndividualBuilder individualByCriteria : individualQueriedByCriteria) {
            LOG.info(
                "Queried Individual identifiers {}, addresses {}, conditions {}, confidentiality {}, credentials {}, disciplinaryActions {}, familyName {}, names {}, demographics {}, expertises {}, notes {}, roleType {}, statuses {}, telecoms {}",
                individualByCriteria.getIdentifiers(),
                individualByCriteria.getAddressList(),
                individualByCriteria.getConditionList(),
                individualByCriteria.getConfidentiality(),
                individualByCriteria.getCredentialList(),
                individualByCriteria.getDisciplinaryActionList(),
                individualByCriteria.getFamilyName(),
                Arrays.toString(individualByCriteria.getNames()),
                individualByCriteria.getDemographics(),
                individualByCriteria.getExpertiseList(),
                individualByCriteria.getNoteList(),
                individualByCriteria.getRoleType(),
                individualByCriteria.getStatusList(),
                individualByCriteria.getTelecomList());
        }
    
        fhirController.close();

    }

    @Test
    public void testFunctionality(){
        //This test can be used to quickly test any new functionality added to the FHIRController or related classes.

        FHIRController fhirController = new FHIRController(UserType.ADMIN);

      //Create an organization with random data and specified role type.
        //Note that the saved organization identifier is an IPC identifier.
        OrganizationMaintainConfig orgConfig = new OrganizationMaintainConfig(OrgRoleType.HDS)
            .withAllTelecom()
            .withStatuses(2) //Note that right now the maximum amount of confidentiality that can be added is 2
            .withFacilityRelationships(2)
            .withPciFlag();        

        MaintainOrgBuilder org = fhirController.createOrganization(orgConfig);

        LOG.info("Created organization id {}, name {}.", org.getIdentifiers(), org.getName());

        fhirController.close();
    }

}
