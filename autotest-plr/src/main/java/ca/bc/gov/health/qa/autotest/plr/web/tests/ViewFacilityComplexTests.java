package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static org.testng.Assert.*;

public class ViewFacilityComplexTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public ViewFacilityComplexTests() {}

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
        final Pattern BLOCK_TYPE_PATTERN = Pattern.compile("\\((.*)\\)");

        ViewFacilityPage viewFacility = viewFacilityByIdentifier(workflowManager_,
                "IFC.00006365.BC.PRS", UserType.ADMIN);

        assertEquals(viewFacility.grabDataBlockCount(FacilitySection.IDENTIFIERS), 1,
                "Facility unexpectedly has more than 1 registry identifier data block");
        assertEquals(viewFacility.grabDataBlockCount(FacilitySection.NAMES), 1,
                "Facility unexpectedly has more than 1 facility name data block");

        assertTrue(viewFacility.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS) > 1,
                "Facility unexpectedly has less than 2 organization relationships");
        assertTrue(viewFacility.grabDataBlockCount(FacilitySection.NOTES) > 1,
                "Facility unexpectedly has less than 2 notes");

        // telecommunication / e-address verification
        List<String> expectedTelecomTypes = Arrays.asList("FAX", "M", "MB", "PG", "T");
        assertEquals(viewFacility.grabDataBlockCount(FacilitySection.TELECOMMUNICATIONS), expectedTelecomTypes.size(),
                "Facility has an unexpected amount of telecommunications records");

        List<String> telecomTypes = new ArrayList<>();
        for (int index = 0; index < 5; index++)
        {
            LinkedHashMap<String,String> infoMap = viewFacility.grabDataBlockContent(
                    FacilitySection.TELECOMMUNICATIONS, index);
            String telecomType = infoMap.get("Type");
            Matcher resultMatcher = BLOCK_TYPE_PATTERN.matcher(telecomType);
            resultMatcher.find();
            telecomTypes.add(resultMatcher.group(1));
        }
        Collections.sort(telecomTypes);

        assertEquals(telecomTypes, expectedTelecomTypes,
                "Facility is missing expected telecommunications record types");

        List<String> expectedEAddressTypes = Arrays.asList("E", "F", "H");
        assertEquals(viewFacility.grabDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES), expectedEAddressTypes.size(),
                "Facility has an unexpected amount of electronic address records");

        List<String> eAddressTypes = new ArrayList<>();
        for (int index = 0; index < 3; index++)
        {
            LinkedHashMap<String,String> infoMap = viewFacility.grabDataBlockContent(
                    FacilitySection.ELECTRONIC_ADDRESSES, index);
            String eAddressType = infoMap.get("Type");
            Matcher resultMatcher = BLOCK_TYPE_PATTERN.matcher(eAddressType);
            resultMatcher.find();
            eAddressTypes.add(resultMatcher.group(1));
        }
        Collections.sort(eAddressTypes);

        assertEquals(eAddressTypes, expectedEAddressTypes,
                "Facility is missing expected electronic address record types");

        assertEquals(viewFacility.grabDataBlockCount(FacilitySection.CIVIC_ADDRESSES), 1,
                "Facility unexpectedly has more than 1 civic address data block");

        assertEquals(viewFacility.grabCivicAddressBlockContent().get("Province / State"),
                "BC - British Columbia", "Civic Address is not located in British Columbia");

        assertEquals(viewFacility.grabDataBlockCount(FacilitySection.OTHER_ADDRESS), 1,
                "Facility unexpectedly has more than 1 other address data block");
    }

    @Test
    // F2-007. Limiting Number of Records For View Facility Details Screen
    public void testLimitNumberRecords()
    {
        ViewFacilityPage viewFacility = viewFacilityByIdentifier(workflowManager_,
                "IFC.00000001.BC.PRS", UserType.ADMIN);

        assertTrue(viewFacility.grabDataBlockCount(FacilitySection.NOTES) < 50,
                "Facility unexpectedly has 50 or more notes");

        for (int index = 0; index < viewFacility.grabDataBlockCount(FacilitySection.NOTES); index++)
        {
            String noteIdentifier = viewFacility.grabDataBlockContent(
                    FacilitySection.NOTES, index).get("Note Identifier");
            assertFalse(noteIdentifier.isEmpty(), "Note block " + index + " is missing identifier");
        }

        viewFacility = viewFacilityByIdentifier(workflowManager_,
                "IFC.00006365.BC.PRS", UserType.ADMIN);

        for (int index = 0; index < viewFacility.grabDataBlockCount(FacilitySection.NOTES); index++)
        {
            String noteIdentifier = viewFacility.grabDataBlockContent(
                    FacilitySection.NOTES, index).get("Note Identifier");
            assertFalse(noteIdentifier.isEmpty(),
                    "Note block " + index + " is missing identifier");
        }

        assertTrue(viewFacility.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS) >= 50,
                "Facility unexpectedly has less than 50 organization relationships");

        for (int index = 0; index < viewFacility.grabDataBlockCount(
                FacilitySection.ORGANIZATION_RELATIONSHIPS); index++)
        {
            String relationshipIdentifier = viewFacility.grabDataBlockContent(
                    FacilitySection.ORGANIZATION_RELATIONSHIPS, index).get("Relationship Identifier");
            assertFalse(relationshipIdentifier.isEmpty(),
                    "Organization Relationship block " + index + " is missing identifier");
        }
    }

    @Test
    // F2-008. View Facility Details Screen - Organization Relationships Block
    public void testOrgRelationshipBlock()
    {
        ViewFacilityPage viewFacility = viewFacilityByIdentifier(workflowManager_,
                "IFC.00000061.BC.PRS", UserType.ADMIN);

        assertTrue(viewFacility.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS) > 1,
                "Facility unexpectedly has only one or no organization relationships");

        List<String> orgIdentifiers = new ArrayList<>();

        for (int index = 0; index < viewFacility.grabDataBlockCount(
                FacilitySection.ORGANIZATION_RELATIONSHIPS); index++)
        {
            String orgIdentifier = viewFacility.grabDataBlockContent(
                    FacilitySection.ORGANIZATION_RELATIONSHIPS, index).get("Related Organization Identifier");
            assertFalse(orgIdentifier.isEmpty(),
                    "Organization Relationship block " + index + " is missing Organization Identifier");
            orgIdentifiers.add(orgIdentifier);
        }
        List<String> sortedOrgIdentifiers = new ArrayList<>(orgIdentifiers);
        Collections.sort(sortedOrgIdentifiers);

        assertEquals(orgIdentifiers, sortedOrgIdentifiers,
                "Organization Relationships are not sorted by Related Organization Identifier");
    }

    @Test
    // F2-010. UI Display Providers Related To Facility
    public void testUIDisplayProviders()
    {
        // TODO: modify for both organization and provider
        /*
        Source Provider Org Name       (provider org name / provider name)
        Source Relationship Type                                        (that it is a facility relationship for source (provider))
        Target Relationship Type                                        (that it is a organization relationship for target (facility)
        Source Provider Relationship Locations??                        (the locations for provider)
         */
        String facIdentifier = "IFC.00000061.BC.PRS";
        ViewFacilityPage viewFacility = viewFacilityByIdentifier(workflowManager_,
                facIdentifier, UserType.ADMIN);
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();

        int orgRelationshipCount = viewFacility.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);

        for (int index = 0; index < orgRelationshipCount; index++)
        {
            viewFacility = viewFacilityByIdentifier(workflowManager_,
                    facIdentifier, UserType.ADMIN);
            LinkedHashMap<String,String> orgRelMap = viewFacility.grabDataBlockContent(
                    FacilitySection.ORGANIZATION_RELATIONSHIPS, index);
            String relIdentifier = viewFacility.grabDataBlockContent(
                    FacilitySection.ORGANIZATION_RELATIONSHIPS, index).get("Relationship Identifier");


            ViewProviderPage orgPage = workflow.getViewFacilityActions().transferToOrg(viewFacility, index);

            LinkedHashMap<String,String> facRelMap = null;
            for (int facRelIndex = 0; facRelIndex < orgPage.grabDataBlockCount(
                    ProviderSection.FACILITY_RELATIONSHIPS); facRelIndex++)
            {
                facRelMap = orgPage.grabDataBlockContent(ProviderSection.FACILITY_RELATIONSHIPS, facRelIndex);
                if (facRelMap.get("Relationship Identifier").equals(relIdentifier)) break;
            }
            assertEquals(orgRelMap.get("Relationship Identifier"), facRelMap.get("Relationship Identifier"),
                    "Relationship Identifiers do not match between Organization/Facility");

            int identifierIndex = 0;
            String orgIdentifier;
            do
            {
                orgIdentifier = orgPage.grabDataBlockContent(
                        ProviderSection.IDENTIFIERS, identifierIndex).get("Identifier");
                identifierIndex++;
            } while (!orgIdentifier.contains("IPC"));

            assertEquals(facRelMap.get("Related Facility Identifier"), facIdentifier,
                    "Examined relationship has facility identifiers that do not match");
            assertEquals(orgRelMap.get("Related Organization Identifier"), orgIdentifier,
                    "Examined relationship has organization identifiers that do not match");
        }
    }

    @Test
    // F2-012. Facility Relationship Summary Line
    public void testFacilityRelationshipSummary()
    {
        // TODO: Clarify - is the facility name limit 255 characters? It seems to be 100 at the moment
        // TODO: Current behavior will check 100 characters as normal (if possible)
        List<String> providerDetails = Arrays.asList("IPC", "IPC.00083115.BC.PRS");
        List<String> expectedFacInfo = Arrays.asList("Building", "AZ F009", "Location of (LOCATION)", "CPS");
        ViewProviderPage viewProvider = viewProviderByIdentifier(workflowManager_, providerDetails, UserType.ADMIN);

        LinkedHashMap<String,String> facRelMap = viewProvider.grabDataBlockContent(
                ProviderSection.FACILITY_RELATIONSHIPS, 0);

        assertEquals(facRelMap.get("Facility Type"), expectedFacInfo.get(0),
                "Unexpected Facility Type for facility with name <100 characters.");
        assertEquals(facRelMap.get("Related Facility Name"), expectedFacInfo.get(1),
                "Unexpected Facility Name for facility with name <100 characters.");

        assertTrue(facRelMap.get("Related Facility Name").length() < 100,
                "Facility Name is the maximum length of 100 characters unexpectedly");

        assertEquals(facRelMap.get("Relationship Type"), expectedFacInfo.get(2),
                "Unexpected Relationship Type for facility with name <100 characters.");
        assertEquals(facRelMap.get("Data Owner Code"), expectedFacInfo.get(3),
                "Unexpected Facility Type for facility with name <100 characters.");

        // TODO find facility with maximum length and relationships
    }

    @Test
    // F2-013. Viewing a Related Provider
    public void testViewRelatedProvider()
    {
        // TODO: Clarify - why is the name for related facility not updated / wrong?
        List<String> orgRelDetails = Arrays.asList("RELN.74951.PRS", "AZ R0010", "IPC.00082972.BC.PRS", "CPS");
        List<String> facRelDetails = Arrays.asList("IFC.00000081.BC.PRS", "ABCDEF");
        ViewFacilityPage viewFacility = viewFacilityByIdentifier(workflowManager_,
                facRelDetails.getFirst(), UserType.ADMIN);
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();

        LinkedHashMap<String,String> orgRelMap = viewFacility.grabDataBlockContent(
                FacilitySection.ORGANIZATION_RELATIONSHIPS, 0);

        assertEquals(orgRelMap.get("Relationship Identifier"), orgRelDetails.get(0),
                "Relationship Identifier does not match expected result");
        assertEquals(orgRelMap.get("Related Organization Name"), orgRelDetails.get(1),
                "Organization Name does not match expected result");
        assertEquals(orgRelMap.get("Related Organization Identifier"), orgRelDetails.get(2),
                "Organization Identifier does not match expected result");
        assertEquals(orgRelMap.get("Data Owner Code"), orgRelDetails.get(3),
                "Data Owner Code does not match expected result");

        ViewProviderPage orgPage = workflow.getViewFacilityActions().transferToOrg(viewFacility, 0);

        LinkedHashMap<String,String> facRelMap = orgPage.grabDataBlockContent(
                ProviderSection.FACILITY_RELATIONSHIPS, 0);

        assertEquals(facRelMap.get("Relationship Identifier"), orgRelDetails.get(0),
                "Relationship Identifier does not match result in facility page");
        assertEquals(facRelMap.get("Related Facility Name"), facRelDetails.get(1),
                "Facility Name does not match expected result");
        assertEquals(facRelMap.get("Related Facility Identifier"), facRelDetails.get(0),
                "Facility Identifier does not match expected result");
        assertEquals(facRelMap.get("Data Owner Code"), orgRelDetails.get(3),
                "Data Owner Code does not match expected result in facility page");
    }
}
