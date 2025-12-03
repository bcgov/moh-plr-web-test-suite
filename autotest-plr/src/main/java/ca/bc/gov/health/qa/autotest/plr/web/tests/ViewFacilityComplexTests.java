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
import org.testng.annotations.AfterClass;
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

    @AfterClass
    public void teardown() {
        workflowManager_.logoutAllAndClose();
        LOG.info("Done.");
    }

    @BeforeMethod
    public void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) workflow.login().openPlr();
    }

    /**
     * Helper function to get and sort all types of data blocks for telecommunications and electronic address sections
     *
     * @param viewFacility          the view facility page reference
     * @param dataBlocksSection     the facility section to get data blocks from
     *                              (expects TELECOMMUNICATIONS or ELECTRONIC_ADDRESSES)
     * @param expectedTypes         a sorted list of expected types of data blocks in the facility section
     * @return                      a sorted list of the actual types of data blocks in the facility section
     */
    private List<String> getDataBlockTypes(ViewFacilityPage viewFacility, FacilitySection dataBlocksSection,
                                           List<String> expectedTypes)
    {
        final Pattern BLOCK_TYPE_PATTERN = Pattern.compile("\\((.*)\\)");

        List<String> dataBlockTypes = new ArrayList<>();
        for (int index = 0; index < expectedTypes.size(); index++)
        {
            LinkedHashMap<String,String> infoMap = viewFacility.grabDataBlockContent(
                    dataBlocksSection, index);
            String telecomType = infoMap.get("Type");
            Matcher resultMatcher = BLOCK_TYPE_PATTERN.matcher(telecomType);
            if (resultMatcher.find()) dataBlockTypes.add(resultMatcher.group(1));
        }
        Collections.sort(dataBlockTypes);
        return dataBlockTypes;
    }

    /**
     * Helper function to check the existence of identifiers for each data block in a facility section
     *
     * @param viewFacility          the view facility page reference
     * @param dataBlocksSection     the facility section to get data blocks from
     * @param identifierField       the name of the identifier field within data blocks
     */
    private void checkDataBlockIdentifiers(ViewFacilityPage viewFacility, FacilitySection dataBlocksSection,
                                           String identifierField)
    {
        for (int dataBlockIndex = 0;
             dataBlockIndex < viewFacility.grabDataBlockCount(dataBlocksSection); dataBlockIndex++)
        {
            String dataIdentifier = viewFacility.grabDataBlockContent(
                    dataBlocksSection, dataBlockIndex).get(identifierField);

            assertFalse(dataIdentifier.isEmpty(),
                    dataBlocksSection.getTitle() + " block " + dataBlockIndex + " is missing identifier");
        }
    }

    @Test
    // F2-002. Validate Facility Data Block Multiplicity
    public void testValidateBlockMultiplicity()
    {
        final List<String> expectedTelecomTypes = Arrays.asList("FAX", "M", "MB", "PG", "T");
        final List<String> expectedEAddressTypes = Arrays.asList("E", "F", "H");

        Collections.sort(expectedTelecomTypes);
        Collections.sort(expectedEAddressTypes);

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

        assertEquals(viewFacility.grabDataBlockCount(FacilitySection.TELECOMMUNICATIONS), expectedTelecomTypes.size(),
                "Facility has an unexpected amount of telecommunications records");

        List<String> telecomTypes = getDataBlockTypes(viewFacility,
                FacilitySection.TELECOMMUNICATIONS, expectedTelecomTypes);

        assertEquals(telecomTypes, expectedTelecomTypes,
                "Facility is missing expected telecommunications record types");

        assertEquals(viewFacility.grabDataBlockCount(FacilitySection.ELECTRONIC_ADDRESSES), expectedEAddressTypes.size(),
                "Facility has an unexpected amount of electronic address records");

        List<String> eAddressTypes = getDataBlockTypes(viewFacility,
                FacilitySection.ELECTRONIC_ADDRESSES, expectedEAddressTypes);

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
        final String lowNoteCountIdentifier = "IFC.00000001.BC.PRS";
        final String highNoteCountIdentifier = "IFC.00006365.BC.PRS";
        final String highOrgRelCountIdentifier = "IFC.00006365.BC.PRS";

        ViewFacilityPage viewFacility = viewFacilityByIdentifier(workflowManager_,
                lowNoteCountIdentifier, UserType.ADMIN);

        assertTrue(viewFacility.grabDataBlockCount(FacilitySection.NOTES) < 50,
                "Facility unexpectedly has 50 or more notes");

        checkDataBlockIdentifiers(viewFacility, FacilitySection.NOTES, "Note Identifier");

        viewFacility = viewFacilityByIdentifier(workflowManager_,
                highNoteCountIdentifier, UserType.ADMIN);

        assertTrue(viewFacility.grabDataBlockCount(FacilitySection.NOTES) >= 50,
                "Facility unexpectedly has less than 50 notes");

        checkDataBlockIdentifiers(viewFacility, FacilitySection.NOTES, "Note Identifier");

        viewFacility = viewFacilityByIdentifier(workflowManager_,
                highOrgRelCountIdentifier, UserType.ADMIN);

        assertTrue(viewFacility.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS) >= 50,
                "Facility unexpectedly has less than 50 organization relationships");

        checkDataBlockIdentifiers(viewFacility,
                FacilitySection.ORGANIZATION_RELATIONSHIPS, "Relationship Identifier");
    }

    @Test
    // F2-008. View Facility Details Screen - Organization Relationships Block
    public void testOrgRelationshipBlock()
    {
        final String identifierToCheck = "IFC.00000061.BC.PRS";

        ViewFacilityPage viewFacility = viewFacilityByIdentifier(workflowManager_,
                identifierToCheck, UserType.ADMIN);

        assertTrue(viewFacility.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS) > 1,
                "Facility unexpectedly has only one or no organization relationships");

        List<String> orgIdentifiers = new ArrayList<>();

        for (int dataBlockIndex = 0; dataBlockIndex < viewFacility.grabDataBlockCount(
                FacilitySection.ORGANIZATION_RELATIONSHIPS); dataBlockIndex++)
        {
            String orgIdentifier = viewFacility.grabDataBlockContent(
                    FacilitySection.ORGANIZATION_RELATIONSHIPS, dataBlockIndex).get("Related Organization Identifier");

            assertFalse(orgIdentifier.isEmpty(),
                    "Organization Relationship block " + dataBlockIndex + " is missing Organization Identifier");

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
        Map<String,String> locationMap = Map.of("Located at (LOCATED)", "Location of (LOCATION)",
                "Location of (LOCATION)", "Located at (LOCATED)");

        String facIdentifier = "IFC.00000061.BC.PRS";
        ViewFacilityPage viewFacility = viewFacilityByIdentifier(workflowManager_,
                facIdentifier, UserType.ADMIN);
        PlrWebWorkflow workflow = workflowManager_.getSelectedWorkflow();

        int orgRelationshipCount = viewFacility.grabDataBlockCount(FacilitySection.ORGANIZATION_RELATIONSHIPS);

        for (int orgRelIndex = 0; orgRelIndex < orgRelationshipCount; orgRelIndex++)
        {
            viewFacility = viewFacilityByIdentifier(workflowManager_,
                    facIdentifier, UserType.ADMIN);
            LinkedHashMap<String,String> orgRelMap = viewFacility.grabDataBlockContent(
                    FacilitySection.ORGANIZATION_RELATIONSHIPS, orgRelIndex);
            LinkedHashMap<String,String> facNameMap = viewFacility.grabDataBlockContent(FacilitySection.NAMES, 0);

            ViewProviderPage orgPage = workflow.getViewFacilityActions().transferToOrg(viewFacility, orgRelIndex);
            LinkedHashMap<String,String> orgNameMap = null;
            String relIdentifier = orgRelMap.get("Relationship Identifier");

            LinkedHashMap<String,String> facRelMap = null;
            for (int facRelIndex = 0; facRelIndex < orgPage.grabDataBlockCount(
                    ProviderSection.FACILITY_RELATIONSHIPS); facRelIndex++)
            {
                facRelMap = orgPage.grabDataBlockContent(ProviderSection.FACILITY_RELATIONSHIPS, facRelIndex);
                orgNameMap = orgPage.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0);
                if (facRelMap.get("Relationship Identifier").equals(relIdentifier)) break;
            }

            assertNotNull(facRelMap);
            assertEquals(orgRelMap.get("Relationship Identifier"), facRelMap.get("Relationship Identifier"),
                    "Relationship Identifiers do not match between Organization/Facility");
            assertEquals(orgRelMap.get("Relationship Type"), locationMap.get(facRelMap.get("Relationship Type")),
                    "Provider Page Relationship Type is not reversed correctly");
            assertEquals(facRelMap.get("Relationship Type"), locationMap.get(orgRelMap.get("Relationship Type")),
                    "Facility Page Relationship Type is not reversed correctly");

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
            assertEquals(orgRelMap.get("Related Organization Name"), orgNameMap.get("Name"),
                    "Related Organization Name does not match on Organization Page");
            assertEquals(facRelMap.get("Related Facility Name"), facNameMap.get("Name"),
                    "Related Facility Name does not match on Facility Page");
        }
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
