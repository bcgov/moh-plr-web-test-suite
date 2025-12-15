package ca.bc.gov.health.qa.autotest.plr.web.tests.facility;

import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants;
import ca.bc.gov.health.qa.autotest.plr.data.ViewProviderConstants;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.ElectronicAddressType;
import ca.bc.gov.health.qa.autotest.plr.util.TelecommunicationType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.actions.facility.ViewFacilityActions;
import ca.bc.gov.health.qa.autotest.plr.data.SearchFacilityConstants.*;
import ca.bc.gov.health.qa.autotest.plr.data.ViewFacilityConstants.*;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.*;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static org.testng.Assert.*;

public class ViewFacilityComplexTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static FHIRController fhirController;
    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public ViewFacilityComplexTests() {}

    @AfterClass
    public void teardown() {
        fhirController.close();
        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }

    @BeforeTest
    public void beforeTest()
    {
        fhirController = new FHIRController(UserType.ADMIN);
    }

    @BeforeMethod
    public void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) workflow.login().openPlr();
    }

    @Test
    // F2-002. Validate Facility Data Block Multiplicity
    public void testValidateBlockMultiplicity()
    {
        final ViewFacilityActions actions = workflowManager_.getSelectedWorkflow().getViewFacilityActions();
        final List<String> expectedTelecomTypes = new ArrayList<>(Arrays.stream(TelecommunicationType.values())
                                                            .map(TelecommunicationType::getStartText).toList());
        final List<String> expectedEAddressTypes = new ArrayList<>(Arrays.stream(ElectronicAddressType.values())
                                                            .map(ElectronicAddressType::getStartText).toList());
        Collections.sort(expectedTelecomTypes);
        Collections.sort(expectedEAddressTypes);
        final FacilityMaintainConfig config = new FacilityMaintainConfig()
                                                    .withAllAttributes(2,2);
        final MaintainFacilityBuilder multiplicityFacility = fhirController.createFacility(config);

        // Test Start
        ViewFacilityPage page = viewFacilityByIdentifier(workflowManager_,
                multiplicityFacility.getIdentifier(), UserType.ADMIN);

        List<String> telecomTypes = actions.getDataBlockTypes(page,
                FacilitySection.TELECOMMUNICATIONS, expectedTelecomTypes);
        List<String> eAddressTypes = actions.getDataBlockTypes(page,
                FacilitySection.ELECTRONIC_ADDRESSES, expectedEAddressTypes);


        assertEquals(page.grabDataBlockCount(FacilitySection.IDENTIFIERS), 1,
                "Facility unexpectedly has more than 1 registry identifier data block");
        assertEquals(page.grabDataBlockCount(FacilitySection.NAMES), 1,
                "Facility unexpectedly has more than 1 facility name data block");
        assertTrue(page.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS) > 1,
                "Facility unexpectedly has less than 2 organization relationships");
        assertTrue(page.grabDataBlockCount(FacilitySection.NOTES) > 1,
                "Facility unexpectedly has less than 2 notes");
        assertEquals(page.grabDataBlockCount(FacilitySection.TELECOMMUNICATIONS), expectedTelecomTypes.size(),
                "Facility has an unexpected amount of telecommunications records");
        assertEquals(page.grabDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES), expectedEAddressTypes.size(),
                "Facility has an unexpected amount of electronic address records");
        assertEquals(page.grabDataBlockCount(FacilitySection.CIVIC_ADDRESSES), 1,
                "Facility unexpectedly has more than 1 civic address data block");
        assertEquals(page.grabDataBlockCount(FacilitySection.OTHER_ADDRESS), 1,
                "Facility unexpectedly has more than 1 other address data block");
        assertEquals(telecomTypes, expectedTelecomTypes,
                "Facility is missing expected telecommunications record types");
        assertEquals(eAddressTypes, expectedEAddressTypes,
                "Facility is missing expected electronic address record types");
        assertEquals(page.grabCivicAddressBlockContent().get(CivicAddressField.PROVINCE_STATE.getString()),
                "BC - British Columbia", "Civic Address is not located in British Columbia");

        multiplicityFacility.ceaseOrganizationRelationships();
    }

    @Test
    // F2-007. Limiting Number of Records For View Facility Details Screen
    public void testLimitNumberRecords()
    {
        final FacilityMaintainConfig lowNoteConfig = new FacilityMaintainConfig().withNotes(1);
        final FacilityMaintainConfig highCountConfig = new FacilityMaintainConfig()
                                                            .withAllAttributes(50,50);
        final MaintainFacilityBuilder lowNoteCountFacility = fhirController.createFacility(lowNoteConfig);
        final MaintainFacilityBuilder highCountFacility = fhirController.createFacility(highCountConfig);
        final ViewFacilityActions actions = workflowManager_.getSelectedWorkflow().getViewFacilityActions();

        // Test Start
        ViewFacilityPage page = viewFacilityByIdentifier(workflowManager_,
                lowNoteCountFacility.getIdentifier(), UserType.ADMIN);

        assertTrue(page.grabDataBlockCount(FacilitySection.NOTES) < 50,
                "Facility unexpectedly has 50 or more notes");

        actions.checkDataBlockIdentifiers(page, FacilitySection.NOTES, NoteField.NOTE_IDENTIFIER.getString());

        lowNoteCountFacility.ceaseOrganizationRelationships();
        page = viewFacilityByIdentifier(workflowManager_,
                highCountFacility.getIdentifier(), UserType.ADMIN);

        assertTrue(page.grabDataBlockCount(FacilitySection.NOTES) >= 50,
                "Facility unexpectedly has less than 50 notes");
        assertTrue(page.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS) >= 50,
                "Facility unexpectedly has less than 50 organization relationships");

        actions.checkDataBlockIdentifiers(page, FacilitySection.NOTES, NoteField.NOTE_IDENTIFIER.getString());
        actions.checkDataBlockIdentifiers(page,
                FacilitySection.ORGANIZATION_RELATIONSHIPS, OrgRelationshipField.RELATIONSHIP_IDENTIFIER.getString());

        highCountFacility.ceaseOrganizationRelationships();
    }

    @Test
    // F2-008. View Facility Details Screen - Organization Relationships Block
    public void testOrgRelationshipBlock()
    {
        final FacilityMaintainConfig orgConfig = new FacilityMaintainConfig().withOrgRelationships(2);
        final MaintainFacilityBuilder orgFacility = fhirController.createFacility(orgConfig);

        List<String> orgIdentifiers = new ArrayList<>();

        // Test Start
        ViewFacilityPage page = viewFacilityByIdentifier(workflowManager_, orgFacility.getIdentifier(), UserType.ADMIN);

        int orgBlockCount = page.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);

        assertTrue(orgBlockCount > 1,
                "Facility unexpectedly has only one or no organization relationships");

        for (int dataBlockIndex = 0; dataBlockIndex < orgBlockCount; dataBlockIndex++)
        {
            String orgIdentifier = page.grabOrgRelationshipsBlockContent(dataBlockIndex)
                    .get(OrgRelationshipField.RELATED_ORGANIZATION_IDENTIFIER.getString());

            assertFalse(orgIdentifier.isEmpty(),
                    "Org Relationship block " + dataBlockIndex + " is missing Organization Identifier");

            orgIdentifiers.add(orgIdentifier);
        }
        List<String> sortedOrgIdentifiers = new ArrayList<>(orgIdentifiers);
        Collections.sort(sortedOrgIdentifiers);

        assertEquals(orgIdentifiers, sortedOrgIdentifiers,
                "Organization Relationships are not sorted by Related Organization Identifier");

        orgFacility.ceaseOrganizationRelationships();
    }

    @Test
    // F2-010. UI Display Providers Related To Facility
    public void testUIDisplayProviders()
    {
        final String relIdentifierField = OrgRelationshipField.RELATIONSHIP_IDENTIFIER.getString();
        final String relTypeField = OrgRelationshipField.RELATIONSHIP_TYPE.getString();
        final String provFacIdField = ViewProviderConstants.FacRelationshipField.RELATED_FACILITY_IDENTIFIER.getString();
        final String provFacNameField = ViewProviderConstants.FacRelationshipField.RELATED_FACILITY_NAME.getString();
        final String provNameField = ViewProviderConstants.NameField.NAME.getString();
        final String provIdentifierField = ViewProviderConstants.IdentifierField.IDENTIFIER.getString();
        final ViewFacilityActions actions = workflowManager_.getSelectedWorkflow().getViewFacilityActions();
        final FacilityMaintainConfig orgConfig = new FacilityMaintainConfig().withOrgRelationships(2);
        final MaintainFacilityBuilder facility = fhirController.createFacility(orgConfig);

        String orgIdentifier;
        String facIdentifier = facility.getIdentifier();
        String orgName = null;
        String facName;
        LinkedHashMap<String,String> orgRelMap;
        LinkedHashMap<String,String> facRelMap = null;
        ViewProviderPage orgPage;

        // Test Start
        ViewFacilityPage facPage = viewFacilityByIdentifier(workflowManager_,
                facIdentifier, UserType.ADMIN);

        int orgRelationshipCount = facPage.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);

        for (int orgRelIndex = 0; orgRelIndex < orgRelationshipCount; orgRelIndex++)
        {
            int identifierIndex = 0;

            facPage = viewFacilityByIdentifier(workflowManager_, facIdentifier, UserType.ADMIN);

            orgRelMap = facPage.grabOrgRelationshipsBlockContent(orgRelIndex);
            facName = facPage.grabNamesBlockContent(0).get(NameField.NAME.getString());
            orgIdentifier = orgRelMap.get(relIdentifierField);

            orgPage = actions.transferToOrg(facPage, orgRelIndex);

            for (int facRelIndex = 0; facRelIndex < orgPage.grabDataBlockCount(
                    ProviderSection.FACILITY_RELATIONSHIPS); facRelIndex++)
            {
                facRelMap = orgPage.grabDataBlockContent(ProviderSection.FACILITY_RELATIONSHIPS, facRelIndex);
                orgName = orgPage.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0).get(provNameField);
                if (facRelMap.get(relIdentifierField).equals(orgIdentifier)) break;
            }

            assertNotNull(facRelMap);
            assertEquals(orgRelMap.get(relIdentifierField), facRelMap.get(relIdentifierField),
                    "Relationship Identifiers do not match between Organization/Facility");
            assertEquals(orgRelMap.get(relTypeField), ViewFacilityConstants.orgFacMap.get(facRelMap.get(relTypeField)),
                    "Provider Page Relationship Type is not reversed correctly");
            assertEquals(facRelMap.get(relTypeField), ViewFacilityConstants.orgFacMap.get(orgRelMap.get(relTypeField)),
                    "Facility Page Relationship Type is not reversed correctly");

            do
            {
                orgIdentifier = orgPage.grabDataBlockContent(
                        ProviderSection.IDENTIFIERS, identifierIndex).get(provIdentifierField);
                identifierIndex++;
            } while (!orgIdentifier.contains("IPC"));

            assertEquals(facRelMap.get(provFacIdField), facIdentifier,
                    "Examined relationship has facility identifiers that do not match");
            assertEquals(orgRelMap.get(OrgRelationshipField.RELATED_ORGANIZATION_IDENTIFIER.getString()), orgIdentifier,
                    "Examined relationship has organization identifiers that do not match");
            assertEquals(orgRelMap.get(OrgRelationshipField.RELATED_ORGANIZATION_NAME.getString()), orgName,
                    "Related Organization Name does not match on Organization Page");
            assertEquals(facRelMap.get(provFacNameField), facName,
                    "Related Facility Name does not match on Facility Page");
        }

        facility.ceaseOrganizationRelationships();
    }

    @Test
    // F2-012. Facility Relationship Summary Line
    public void testFacilityRelationshipSummary()
    {
        final List<String> nonMaxProviderDetails = Arrays.asList("IPC", "IPC.00083115.BC.PRS");
        final List<String> nonMaxProviderInfo = Arrays.asList("Building", "AZ F009", "Location of (LOCATION)", "CPS");
        final List<String> maxProviderDetails = Arrays.asList("IPC", "IPC.00124877.BC.PRS");
        final List<String> maxProviderInfo = Arrays.asList("Building",
                "maximumlengthaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "Location of (LOCATION)", "CPS");

        final List<String> facRelFields = Arrays.asList("Facility Type", "Related Facility Name",
                "Relationship Type", "Data Owner Code");

        // Non-Maximum Length Case
        List<String> providerDetails = nonMaxProviderDetails;
        List<String> expectedFacInfo = nonMaxProviderInfo;
        ViewProviderPage viewProvider = viewProviderByIdentifier(workflowManager_, providerDetails, UserType.ADMIN);

        LinkedHashMap<String,String> facRelMap = viewProvider.grabDataBlockContent(
                ProviderSection.FACILITY_RELATIONSHIPS, 0);

        assertTrue(facRelMap.get("Related Facility Name").length() < 100,
                "Facility Name is the maximum length of 100 characters unexpectedly");

        int facFieldIndex = 0;
        for (String facField : facRelFields)
        {
            assertEquals(facRelMap.get(facField), expectedFacInfo.get(facFieldIndex),
                    "Unexpected " + facField + " for facility with name <100 characters.");
            facFieldIndex++;
        }

        // Maximum Length case
        providerDetails = maxProviderDetails;
        expectedFacInfo = maxProviderInfo;

        viewProvider = viewProviderByIdentifier(workflowManager_, providerDetails, UserType.ADMIN);

        facRelMap = viewProvider.grabDataBlockContent(
                ProviderSection.FACILITY_RELATIONSHIPS, 0);

        assertEquals(facRelMap.get("Related Facility Name").length(), 100,
                "Length of Facility Name is not the maximum of 100 characters.");

        facFieldIndex = 0;
        for (String facField : facRelFields)
        {
            assertEquals(facRelMap.get(facField), expectedFacInfo.get(facFieldIndex),
                    "Unexpected " + facField + " for facility with name 100 characters.");
            facFieldIndex++;
        }
    }
}
