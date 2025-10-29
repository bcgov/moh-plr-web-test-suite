package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.FacilitySection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.ViewFacilityPage;
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

public class ViewFacilityTests implements SimpleTest {
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    public ViewFacilityTests() {}

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

        for (int index = 0; index < viewFacility.grabDataBlockCount(
                FacilitySection.ORGANIZATION_RELATIONSHIPS); index++)
        {
            String relationshipIdentifier = viewFacility.grabDataBlockContent(
                    FacilitySection.ORGANIZATION_RELATIONSHIPS, index).get("Relationship Identifier");
            assertFalse(relationshipIdentifier.isEmpty(),
                    "Organization Relationship block " + index + " is missing identifier");
        }
    }
}
