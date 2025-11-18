package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.*;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.navigateToAddFacilityPage;
import static org.testng.Assert.*;

public class CreateFacilityComplexTests implements SimpleTest {

    private static final Logger LOG = ExecutionLogManager.getLogger();
    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    private static final SecureRandom RNG = new SecureRandom();

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;

    public CreateFacilityComplexTests()
    {
        try
        {
            errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read JSON data (%s).", errorPath);
            throw new IllegalStateException(msg, e);
        }
    }

    @BeforeMethod
    public void before(Object[] parameters)
    {
        PlrWebWorkflow workflow = workflowManager_.selectWorkflow(parameters, UserType.ADMIN);
        if (!workflow.isLoggedIn()) workflow.login().openPlr();
    }

    @Test
    // F3-006. Validate Facility Identifiers
    public void testValidateFacIdentifiers()
    {
        final String identifierTypeRequired = errorList.getString("identifierTypeRequired");
        final String foreignCharacterIdentifier = errorList.getString("foreignCharacterIdentifier");

        // Identifier but no Identifier Type
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection(
                "BUILDING", "Select One", "IFC.25252525.BC.PRS");
        addFacility.clickNext("Identifier", null);

        List<String> errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();

        assertTrue(errorMessageList.contains(identifierTypeRequired),
                "Error for identifier included but no identifier type not displayed");

        // Foreign Character in Identifier
        addFacility = navigateToAddFacilityPage(workflowManager_);

        AddFacilityIdFragment identifierFields = addFacility.fillIdentifierSection(
                "BUILDING", "Select One", "δ.00000001.PRS");
        addFacility.clickNext("Identifier", "");

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        List<String> highlightedFields = identifierFields.getHighlightedFields();

        assertTrue(errorMessageList.contains(foreignCharacterIdentifier),
                "Error for identifier field includes foreign characters not displayed.");
        assertEquals(highlightedFields.getLast(), "Identifier:",
                "Facility Identifier Type is unhighlighted, or more than one error occurred.");

        // Nonalphanumeric Character in Identifier
        addFacility = navigateToAddFacilityPage(workflowManager_);

        identifierFields = addFacility.fillIdentifierSection(
                "BUILDING", "Select One", "IFC@00000001@PRS");
        addFacility.clickNext("Identifier", "");

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierFields.getHighlightedFields();

        assertTrue(errorMessageList.contains(foreignCharacterIdentifier),
                "Error for identifier field includes foreign characters not displayed.");
        assertEquals(highlightedFields.getLast(), "Identifier:",
                "Facility Identifier Type is unhighlighted, or more than one error occurred.");

        // Maximum Character Limit in Identifier
        addFacility = navigateToAddFacilityPage(workflowManager_);

        identifierFields = addFacility.fillIdentifierSection(
                "BUILDING", "Select One",
                "IFC.9999999999999999999999999999999999999999999.PRS");
        addFacility.clickNext("Identifier", "");

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierFields.getHighlightedFields();

        assertFalse(errorMessageList.isEmpty(), "Error for maximum character limit is not displayed.");
        assertEquals(highlightedFields.getLast(), "Identifier:",
                "Facility Identifier Type is unhighlighted, or more than one error occurred.");

        // Positive Test
        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection(
                "BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        assertEquals(addFacility.getStep(), "Name",
                "Current step in flow is unexpected - an error likely occurred.");
    }

    @Test
    // F3-010. Facility Address Recognition
    public void facilityAddressRecognition()
    {
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.fillFacilitySection("Test Facility", "Facility Description");
        addFacility.clickNext("Facility", "");

        assertEquals(addFacility.getStepTitle(), "Address",
                "Failed to reach the Address tab in Add Facility flow");

        AddFacilityAddressFragment addressInfo = addFacility.fillAddressSection(
                "2269 DOUGLAS ST, V", "2269 DOUGLAS ST, V");

        assertFalse(addressInfo.getAddressLine1().isEmpty(), "Address Line 1 field auto-population failed");
        assertFalse(addressInfo.getCity().isEmpty(), "City field auto-population failed");
        assertFalse(addressInfo.getPostalCode().isEmpty(), "Postal Code field auto-population failed");

        // check BC and Canada are fixed values in Province/State and Country
        assertTrue(addressInfo.provinceIsDisabled(), "Province / State field should be disabled");
        assertTrue(addressInfo.countryIsDisabled(), "Country field should be disabled");
        assertEquals(addressInfo.getProvinceState(), "BC - British Columbia",
                "Province/State field is unexpectedly not in BC");
        assertEquals(addressInfo.getCountry(), "CA - CANADA",
                "Country field is unexpectedly not in Canada");
    }

    @Test
    // F3-021. Facility Address with Multi-Part Street Name
    public void addressMultiPartStreetName()
    {
        final int TWO_WORD_ADDRESS_LOWER_LIMIT = 800;
        final int TWO_WORD_ADDRESS_UPPER_LIMIT = 1650;

        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.fillFacilitySection("Multi Part Street Facility", "Multi Part Street Description");
        addFacility.clickNext("Facility", "");

        AddFacilityAddressFragment addressInfo = null;
        while (addressInfo == null)
        {
            try
            {
                int TWO_WORD_ADDRESS_NUM = TWO_WORD_ADDRESS_LOWER_LIMIT +
                        RNG.nextInt(TWO_WORD_ADDRESS_UPPER_LIMIT - TWO_WORD_ADDRESS_LOWER_LIMIT + 1);
                String TWO_WORD_ADDRESS = "";
                TWO_WORD_ADDRESS = String.format("%d LYNN VALLEY RD", TWO_WORD_ADDRESS_NUM);
                addressInfo = addFacility.fillAddressSection(
                        TWO_WORD_ADDRESS, TWO_WORD_ADDRESS + ", NORTH");
            } catch (IllegalStateException ignored) {}
        }
        String multiPartAddressLine = addressInfo.getAddressLine1();
        String multiPartCity = addressInfo.getCity();
        String multiPartCountry = addressInfo.getCountry().substring(
                addressInfo.getCountry().indexOf("-")+1).strip();


        addFacility.clickNext("Facility", "Civic");

        addressInfo.clickContinueRecommended();
        addFacility.waitForAddFacilityStep("Address", false);
        AddFacilitySummaryFragment confirmFacility = addFacility.getFacilitySummary();

        List<String> matchingCivicAddress = confirmFacility.getCivicAddress().stream().map(String::toLowerCase).toList();
        List<String> matchingMailingAddress = confirmFacility.getMailingAddress().stream().map(String::toLowerCase).toList();

        assertTrue(matchingCivicAddress.contains(multiPartAddressLine.toLowerCase()),
                "Civic Address in summary missing address line information");
        assertTrue(matchingMailingAddress.contains(multiPartAddressLine.toLowerCase()),
                "Mailing Address in summary missing address line information");

        assertTrue(matchingMailingAddress.contains(multiPartCity.toLowerCase()),
                "Maiing Address in summary missing city information");
        assertTrue(matchingMailingAddress.contains(multiPartCountry.toLowerCase()),
                "Mailing Address in summary missing country information");

        addFacility.clickBack("Facility Summary", false);

        addFacility.fillAddressSection(
                List.of("3331 DINGLE BINGLE HILL RD", "", ""), "Nanaimo", null, "V9T 3V6");
        multiPartAddressLine = addressInfo.getAddressLine1();
        multiPartCity = addressInfo.getCity();
        multiPartCountry = addressInfo.getCountry().substring(
                addressInfo.getCountry().indexOf("-")+1).strip();


        addFacility.clickNext("Facility", "Civic");

        addressInfo.clickContinueRecommended();
        addFacility.waitForAddFacilityStep("Address", false);
        confirmFacility = addFacility.getFacilitySummary();

        matchingCivicAddress = confirmFacility.getCivicAddress().stream().map(String::toLowerCase).toList();
        matchingMailingAddress = confirmFacility.getMailingAddress().stream().map(String::toLowerCase).toList();

        assertTrue(matchingCivicAddress.contains(multiPartAddressLine.toLowerCase()),
                "Civic Address in summary missing address line information");
        assertTrue(matchingMailingAddress.contains(multiPartAddressLine.toLowerCase()),
                "Mailing Address in summary missing address line information");

        assertTrue(matchingMailingAddress.contains(multiPartCity.toLowerCase()),
                "Maiing Address in summary missing city information");
        assertTrue(matchingMailingAddress.contains(multiPartCountry.toLowerCase()),
                "Mailing Address in summary missing country information");

    }
}
