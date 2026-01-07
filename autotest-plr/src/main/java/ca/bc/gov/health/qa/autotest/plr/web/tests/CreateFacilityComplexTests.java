package ca.bc.gov.health.qa.autotest.plr.web.tests;

import ca.bc.gov.health.qa.autotest.core.util.config.Config;
import ca.bc.gov.health.qa.autotest.core.util.config.ConfigProvider;
import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility.*;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflow;
import ca.bc.gov.health.qa.autotest.plr.web.workflows.PlrWebWorkflowManager;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.testng.SimpleTest;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

import static ca.bc.gov.health.qa.autotest.plr.web.tests.TestHelper.*;
import static org.testng.Assert.*;

public class CreateFacilityComplexTests implements SimpleTest {

    private static final Logger LOG = ExecutionLogManager.getLogger();
    private final PlrWebWorkflowManager workflowManager_ = new PlrWebWorkflowManager();

    private static final Config config_ = ConfigProvider.get().getConfig();
    private static final Path errorPath = Path.of(config_.get("data.dir")).resolve("error-list.json");
    private static JSONObject errorList;
    private static JSONObject warningList;

    public CreateFacilityComplexTests()
    {
        try
        {
            errorList = new JSONObject(Files.readString(errorPath)).getJSONObject("errors");
            warningList = new JSONObject(Files.readString(errorPath)).getJSONObject("warnings");
        }
        catch (IOException e)
        {
            String msg = String.format("Failed to read JSON data (%s).", errorPath);
            throw new IllegalStateException(msg, e);
        }
    }

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

        workflow.getSeleniumSession().setWaitTimeout(Duration.ofSeconds(6));
    }

    @Test
    // F3-006. Validate Facility Identifiers
    public void testValidateFacIdentifiers()
    {
        final String identifierTypeRequired = errorList.getString("identifierTypeRequired");
        final String foreignCharacterIdentifier = errorList.getString("foreignCharacterIdentifier");
        final String maximumIdentifierCharLimit = errorList.getString("maximumIdentifierCharLimit");

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
        addFacility.clickNext("Identifier", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        List<String> highlightedFields = identifierFields.getHighlightedFields();

        assertTrue(errorMessageList.contains(foreignCharacterIdentifier),
                "Error for identifier field includes foreign characters not displayed.");
        assertEquals(highlightedFields.getLast(), "Identifier:",
                "Facility Identifier is unhighlighted, or more than one error occurred.");

        // Nonalphanumeric Character in Identifier
        addFacility = navigateToAddFacilityPage(workflowManager_);

        identifierFields = addFacility.fillIdentifierSection(
                "BUILDING", "Select One", "IFC@00000001@PRS");
        addFacility.clickNext("Identifier", null);

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
        addFacility.clickNext("Identifier", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = identifierFields.getHighlightedFields();

        assertTrue(errorMessageList.contains(maximumIdentifierCharLimit),
                "Error for maximum character limit is not displayed.");
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
    // F3-012. Facility Civic Address Latitude and Longitude
    public void facilityAddressLatLong()
    {
        final double COORD_ERROR = 0.0001;
        final String geocoderBaseURI = "https://geocoder.api.gov.bc.ca/addresses.geojson?addressString=";
        final List<String> addressData = List.of("130", "875", "SEYMOUR ST, KAMLOOPS");

        ViewFacilityPage newFacility = createAndSubmitFacility(workflowManager_,
                addressData, "LatLong", 5);

        Double civicLat = Double.parseDouble(newFacility.grabCivicAddressBlockContent().get("Latitude"));
        Double civicLong = Double.parseDouble(newFacility.grabCivicAddressBlockContent().get("Longitude"));

        String civicAddress = newFacility.grabCivicAddressBlockContent().get("Address Line 1")
                .replaceAll(" ", "%20");
        String civicCity = newFacility.grabCivicAddressBlockContent().get("City");
        String civicProvince = newFacility.grabCivicAddressBlockContent().get("Province / State");
        civicProvince = civicProvince.substring(0, civicProvince.indexOf("-")-1);
        String fullAddress = String.format("%s%s, %s, %s", geocoderBaseURI, civicAddress, civicCity, civicProvince)
                .replaceAll(", ", "%2C%20");

        JSONObject geocodeJSON;
        try {
            geocodeJSON = new JSONObject(IOUtils.toString(URI.create(fullAddress), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Object> dataCoords = geocodeJSON.getJSONArray("features").getJSONObject(0)
                .getJSONObject("geometry").getJSONArray("coordinates").toList();
        Double dataLat = Double.parseDouble(dataCoords.getLast().toString());
        Double dataLong = Double.parseDouble(dataCoords.getFirst().toString());

        assertTrue(Math.abs(dataLat - civicLat) < COORD_ERROR,
                "Difference between Geocode Latitude and PLR Civic Address Latitude is too large");
        assertTrue(Math.abs(dataLong - civicLong) < COORD_ERROR,
                "Difference between Geocode Longitude and PLR Civic Address Longitude is too large");
    }

    @Test
    // F3-013. Request Facility Health Boundaries
    public void facilityHealthBoundaries()
    {
        FHIRController fhirController = new FHIRController(UserType.ADMIN);

        final List<String> addressData = List.of("610", "1250", "PLEASANT ST, KAMLOOPS");
        ViewFacilityPage newFacility = createAndSubmitFacility(workflowManager_,
                addressData, "Health Boundary", 5);
        String newIdentifier = newFacility.grabDataBlockContent(FacilitySection.IDENTIFIERS, 0).get("Identifier");
        MaintainFacilityBuilder queriedFacility = fhirController.queryFacilityByIdentifier(IdentifierType.IFC, newIdentifier);

        LinkedHashMap<String,String> newCivicAddress = newFacility.grabCivicAddressBlockContent();
        Map<String,String> fhirCivicAddress = queriedFacility.getHsda();

        assertEquals(newCivicAddress.get("Health Authority"), fhirCivicAddress.get("HA"),
                "Health Authority does not match between FHIR and PLR site");
        assertEquals(newCivicAddress.get("Health Service Delivery Area"), fhirCivicAddress.get("HSDA"),
                "Health Service Delivery Area does not match between FHIR and PLR site");
        assertEquals(newCivicAddress.get("Local Health Area"), fhirCivicAddress.get("LHA"),
                "Local Health Area does not match between FHIR and PLR site");
        assertEquals(newCivicAddress.get("Primary Care Network"), fhirCivicAddress.get("PCN"),
                "Primary Care Network does not match between FHIR and PLR site");
        assertEquals(newCivicAddress.get("Community Health Service Area"), fhirCivicAddress.get("CHSA"),
                "Community Health Service Area does not match between FHIR and PLR site");

        fhirController.close();
    }

    @Test
    // F3-015. Validate Facility Mailing Address Type
    public void facilityAddressType()
    {
        final List<String> addressData = List.of("120", "775", "VICTORIA ST, KAMLOOPS");

        ViewFacilityPage newFacility = createAndSubmitFacility(workflowManager_,
                addressData, "Name", 5);

        assertEquals(newFacility.grabDataBlockContent(FacilitySection.OTHER_ADDRESS,0).get("Address Type"),
                "Physical location (P)", "New Facility's other address has unexpected address type");
    }

    @Test
    // F3-016. Validate Facility Mailing Address Purpose
    public void facilityAddressPurpose()
    {
        final List<String> addressData = List.of("380", "550", "DAVIS RD, LADYSMITH");

        ViewFacilityPage newFacility = createAndSubmitFacility(workflowManager_,
                addressData, "Purpose", 5);

        assertEquals(newFacility.grabDataBlockContent(FacilitySection.OTHER_ADDRESS,0).get("Address Purpose"),
                "Facility Contact (FC)", "New Facility's other address has unexpected address purpose");
    }

    @Test
    // F3-019. Facility Address Validation Status
    public void facilityAddressValidationStatus()
    {
        final List<String> addressData = List.of("310", "560", "LINDEN AVE, KAMLOOPS");
        final String streetTypeEnding = "NUE";

        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.clickNext("Facility", "");

        fillAddressWithValidate(addFacility, addressData, 5);

        ViewFacilityPage newFacility = addFacility.getFacilitySummary().clickSubmitButton();

        String validationStatus = newFacility.grabDataBlockContent(
                FacilitySection.OTHER_ADDRESS, 0).get("Validation Status");
        assertEquals(validationStatus, "Valid (V)", "Validation Status is not Valid as expected");

        addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.clickNext("Facility", "");

        fillOutAddressSection(addFacility, addressData, 5);
        addFacility.clickBack("Facility Summary", false);
        AddFacilityAddressFragment addressInfo = new AddFacilityAddressFragment(
                workflowManager_.getSelectedWorkflow().getSeleniumSession());
        addressInfo.fillAddressLine1(addressInfo.getAddressLine1() + streetTypeEnding);

        addFacility.clickNext("Address", "Mailing");
        addressInfo.handleWidgetButton("Mailing");
        addFacility.waitForWidgetVisibility("Civic");
        addressInfo.handleWidgetButton("Civic");

        addFacility.waitForAddFacilityStep("Address", false);
        newFacility = addFacility.getFacilitySummary().clickSubmitButton();

        validationStatus = newFacility.grabDataBlockContent(
                FacilitySection.OTHER_ADDRESS,0).get("Validation Status");
        assertEquals(validationStatus, "Invalid (I)", "Validation Status is not Invalid as expected");
    }

    @Test
    // F3-020. Facility Address Correction With External Tool
    public void facilityAddressCorrection()
    {
        final String civicAddressRecommendation = warningList.getString("civicAddressRecommendation");
        final List<String> addressData = List.of("250", "300", "LANSDOWNE ST, KAMLOOPS");
        final String streetTypeEnding = "R";

        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.clickNext("Facility", "");

        fillAddressWithValidate(addFacility, addressData, 5);

        addFacility.clickBack("Facility Summary", false);
        AddFacilityAddressFragment addressInfo = new AddFacilityAddressFragment(
                workflowManager_.getSelectedWorkflow().getSeleniumSession());
        addressInfo.fillAddressLine1(addressInfo.getAddressLine1() + streetTypeEnding);
        addFacility.clickNext("Address", "Civic");
        addFacility.waitForWidgetVisibility("Civic");
        List<String> warningMessageList = addFacility.waitForAlertMessagesFragment().grabWarningMessageList();
        assertTrue(warningMessageList.contains(civicAddressRecommendation),
                "Civic Address Recommendation error does not appear unexpectedly");

        addressInfo.handleWidgetButton("Civic");

        addFacility.waitForAddFacilityStep("Address", false);
        ViewFacilityPage newFacility = addFacility.getFacilitySummary().clickSubmitButton();

        assertTrue(newFacility.grabCivicAddressBlockContent().get("Address Line 1").contains(
                addressData.getLast().substring(0, addressData.getLast().indexOf(","))),
                "Created facility does not match expected address name");
    }

    @Test
    // F3-021. Facility Address with Multi-Part Street Name
    public void addressMultiPartStreetName()
    {
        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");
        addFacility.fillFacilitySection("Multi Part Street Facility", "Multi Part Street Description");
        addFacility.clickNext("Facility", "");

        AddFacilityAddressFragment addressInfo = addFacility.fillAddressSection(
                "935 LYNN VALLEY RD, N", "935 LYNN VALLEY RD, N");
        String multiPartAddressLine = addressInfo.getAddressLine1();
        String multiPartCity = addressInfo.getCity();
        String multiPartCountry = addressInfo.getCountry().substring(
                addressInfo.getCountry().indexOf("-")+1).strip();


        addFacility.clickNext("Address", "Civic");

        addressInfo.handleWidgetButton("Civic");
        addFacility.waitForAddFacilityStep("Address", false);
        AddFacilitySummaryFragment confirmFacility = addFacility.getFacilitySummary();

        List<String> matchingCivicAddress = confirmFacility.getCivicAddress().stream().map(String::toLowerCase).toList();
        List<String> matchingMailingAddress = confirmFacility.getMailingAddress().stream().map(String::toLowerCase).toList();

        assertTrue(matchingCivicAddress.contains(multiPartAddressLine.toLowerCase()),
                "Civic Address in summary missing address line information");
        assertTrue(matchingMailingAddress.contains(multiPartAddressLine.toLowerCase()),
                "Mailing Address in summary missing address line information");

        assertTrue(matchingMailingAddress.contains(multiPartCity.toLowerCase()),
                "Mailing Address in summary missing city information");
        assertTrue(matchingMailingAddress.contains(multiPartCountry.toLowerCase()),
                "Mailing Address in summary missing country information");

        addFacility.clickBack("Facility Summary", false);

        addFacility.fillAddressSection(
                List.of("3331 DINGLE BINGLE HILL RD", "", ""), "Nanaimo", null, "V9T 3V6");
        multiPartAddressLine = addressInfo.getAddressLine1();
        multiPartCity = addressInfo.getCity();
        multiPartCountry = addressInfo.getCountry().substring(
                addressInfo.getCountry().indexOf("-")+1).strip();


        addFacility.clickNext("Address", "Civic");

        addressInfo.handleWidgetButton("Civic");
        addFacility.waitForAddFacilityStep("Address", false);
        confirmFacility = addFacility.getFacilitySummary();

        matchingCivicAddress = confirmFacility.getCivicAddress().stream().map(String::toLowerCase).toList();
        matchingMailingAddress = confirmFacility.getMailingAddress().stream().map(String::toLowerCase).toList();

        assertTrue(matchingCivicAddress.contains(multiPartAddressLine.toLowerCase()),
                "Civic Address in summary missing address line information");
        assertTrue(matchingMailingAddress.contains(multiPartAddressLine.toLowerCase()),
                "Mailing Address in summary missing address line information");

        assertTrue(matchingMailingAddress.contains(multiPartCity.toLowerCase()),
                "Mailing Address in summary missing city information");
        assertTrue(matchingMailingAddress.contains(multiPartCountry.toLowerCase()),
                "Mailing Address in summary missing country information");

    }

    @Test
    // F3-023. Rejection of Non-Acceptable Characters
    public void rejectionNonAcceptableCharacters()
    {
        final String foreignCharacterIdentifier = errorList.getString("foreignCharacterIdentifier");
        final String foreignCharacterFacility = errorList.getString("foreignCharacterFacility");
        final String foreignCharacterAddress = errorList.getString("foreignCharacterAddress");

        AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

        AddFacilityIdFragment identifierFields = addFacility.fillIdentifierSection(
                "BUILDING", "Select One", "%");
        addFacility.clickNext("Identifier", null);

        List<String> errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        List<String> highlightedFields = identifierFields.getHighlightedFields();

        assertTrue(errorMessageList.contains(foreignCharacterIdentifier),
                "Invalid character error does not appear unexpectedly");
        assertTrue(highlightedFields.contains("Identifier:"),
                "Facility Identifier field is unhighlighted");

        addFacility.fillIdentifierSection("BUILDING", "Select One", "");
        addFacility.clickNext("Identifier", "");

        AddFacilityNameFragment nameFields = addFacility.fillFacilitySection("A%", "");
        addFacility.clickNext("Facility", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = nameFields.getHighlightedFields();

        assertTrue(errorMessageList.contains(foreignCharacterFacility),
                "Invalid character error does not appear unexpectedly");
        assertTrue(highlightedFields.contains("Name:"), "Facility name field is unhighlighted");

        nameFields = addFacility.fillFacilitySection("", "A%");
        addFacility.clickNext("Facility", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = nameFields.getHighlightedFields();

        assertTrue(errorMessageList.contains(foreignCharacterFacility),
                "Invalid character error does not appear unexpectedly");
        assertTrue(highlightedFields.contains("Description:"),
                "Facility description field is unhighlighted");

        addFacility.fillFacilitySection("", "");
        addFacility.clickNext("Facility", "");

        for (int addressLineIndex = 1; addressLineIndex < 4; addressLineIndex++)
        {
            List<String> addressLines = Arrays.asList("", "", "");
            addressLines.set(addressLineIndex - 1, "A%");
            AddFacilityAddressFragment addressFields = addFacility.fillAddressSection(
                    addressLines, "Vic", "Victoria", "");
            addFacility.clickNext("Address", null);

            errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
            highlightedFields = addressFields.getHighlightedFields();

            assertTrue(errorMessageList.contains(foreignCharacterAddress),
                    "Invalid character error does not appear unexpectedly");
            if (addressLineIndex == 1) assertTrue(highlightedFields.contains("Address Line 1:*"),
                    "Address Line 1 is unhighlighted");
            else assertTrue(highlightedFields.contains("Address Line " + addressLineIndex + ":"),
                    "Address Line " + addressLineIndex + " field is unhighlighted");
        }

        AddFacilityAddressFragment addressFields = addFacility.fillAddressSection(
                List.of("1175 DOUGLAS ST", "", ""), "A^", null, "");
        addFacility.clickNext("Address", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = addressFields.getHighlightedFields();

        assertTrue(errorMessageList.contains(foreignCharacterAddress),
                "Invalid character error does not appear unexpectedly");
        assertTrue(highlightedFields.contains("City:*"), "City field is unhighlighted");

        addressFields = addFacility.fillAddressSection(
                List.of("1175 DOUGLAS ST", "", ""), "Vic", "Victoria", "A%");
        addFacility.clickNext("Address", null);

        errorMessageList = addFacility.waitForAlertMessagesFragment().grabErrorMessageList();
        highlightedFields = addressFields.getHighlightedFields();

        assertTrue(errorMessageList.contains(foreignCharacterAddress),
                "Invalid character error does not appear unexpectedly");
        assertTrue(highlightedFields.contains("Postal Code / Zip Code:"),
                "Postal Code field is unhighlighted");
    }

    @Test
    // F3-024. Data Owner Code for Facility
    public void dataOwnerCodeFacility()
    {
        final List<String> addressData = List.of("300", "930", "ST PAUL ST, KAMLOOPS");

        ViewFacilityPage newFacility = createAndSubmitFacility(workflowManager_,
                addressData, "Data Owner Check", 5);

        assertEquals(newFacility.grabDataBlockContent(FacilitySection.IDENTIFIERS, 0).get("Data Owner Code"),
                "MOH", "Data Owner Code for Identifier section is unexpectedly not MOH");
        assertEquals(newFacility.grabDataBlockContent(FacilitySection.NAMES, 0).get("Data Owner Code"),
                "MOH", "Data Owner Code for Name section is unexpectedly not MOH");
        assertNull(newFacility.grabCivicAddressBlockContent().get("Data Owner Code"),
                "Data Owner Code unexpectedly present for Civic Address section");
        assertEquals(newFacility.grabDataBlockContent(FacilitySection.OTHER_ADDRESS, 0).get("Data Owner Code"),
                "MOH", "Data Owner Code for Other Address section is unexpectedly not MOH");

    }

    @Test
    // F3-026 Facility address should be able to handle addresses with or without street types
    public void facilityAddressStreetTypes()
    {
        final List<String> streetTypes = List.of("ST", "RD", "HWY", "CRT", "AVE");
        final List<List<String>> addressData = List.of(List.of("370", "1070", "BATTLE, KAMLOOPS"),
                List.of("130", "430", "MCGILL, KAMLOOPS"),
                List.of("4442", "4570", "TRANS CANADA,DUNCAN"),
                List.of("100", "120", "CRANBERRY, PORT MOODY"),
                List.of("306", "630", "MCGOWAN, KAMLOOPS")
        );

        int streetTypeIndex = 0;
        for (List<String> addressLine : addressData)
        {
            AddFacilityPage addFacility = navigateToAddFacilityPage(workflowManager_);

            AddFacilityIdFragment identifierFields = addFacility.fillIdentifierSection(
                    "BUILDING", "Select One", "");
            String today = identifierFields.effectiveFromCurrentDate();

            assertEquals(identifierFields.getFacilityType(), "BUILDING - Building",
                    "Facility Type was not set to BUILDING as anticipated");
            assertEquals(identifierFields.getEffectiveFrom(),  today,
                    "Effective From Date in Identifier was not set to the current date as expected");

            addFacility.clickNext("Identifier", "");
            addFacility.clickNext("Facility", "");

            String fullAddress = fillOutAddressSection(addFacility, addressLine, 5);
            fullAddress = fullAddress.substring(0, fullAddress.indexOf(",")) + " " + streetTypes.get(streetTypeIndex);

            addFacility.waitForAddFacilityStep("Address", false);
            ViewFacilityPage newFacility = addFacility.getFacilitySummary().clickSubmitButton();

            String createdAddress = newFacility.grabCivicAddressBlockContent().get("Address Line 1");

            // ignore dashes
            createdAddress = createdAddress.replace("-", " ");
            fullAddress = fullAddress.replace("-", " ");

            assertTrue(createdAddress.contains(streetTypes.get(streetTypeIndex)),
                    "Desired Street Type not found in newly created facility address");
            assertEquals(createdAddress, fullAddress,
                    "Newly created facility address and address filled out in Add Facility flow do not match");

            streetTypeIndex++;
        }



    }
}
