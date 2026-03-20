package ca.bc.gov.health.qa.autotest.plr.web.actions.provider;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.bc.gov.health.qa.autotest.plr.fhir.FHIRController;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.facility.MaintainFacilityBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.MaintainOrgBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrganizationProperties;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewHeaderFragment;
import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.core.util.text.TextUtils;
import ca.bc.gov.health.qa.autotest.plr.data.ViewProviderConstants.*;
import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderDataFields;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.ViewMode;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ViewProviderPage;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import org.json.JSONObject;
import org.testng.Assert;

/**
 * Actions class for the View Provider page/functions
 */
public class ViewProviderActions
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final Pattern VALUE_CODE_PATTERN =
            Pattern.compile("^.*\\((?<code>[^()]+)\\)\\s*$");
    private static final Pattern DATA_KEY_PARENS_PATTERN = Pattern.compile("\\((.*)\\)");

    private final SeleniumSession selenium_;
    private final URI             uri_;
    private final UserType        userType_;

    /**
     * Initializes class and SeleniumSession.
     *
     * @param selenium  the current SeleniumSession
     * @param uri       the URI of the current workflow
     * @param userType  the user type accessing the current workflow
     */
    public ViewProviderActions(SeleniumSession selenium, URI uri, UserType userType)
    {
        selenium_ = selenium;
        uri_      = uri;
        userType_ = userType;
    }

    /**
     * Opens the provider page for a provider given their internal provider ID.
     *
     * @param authId                internal provider ID
     * @return                      a ViewProviderPage reference to the provider page specified by authId
     *
     * @throws NullPointerException if {@code pauthId} is {@code null}
     */
    public ViewProviderPage openProvider(String authId)
    {
        LOG.info("Open provider view ({}).", authId);
        ViewProviderPage viewProvider =
                new ViewProviderPage(selenium_, uri_.resolve("plr/ProviderDetails.xhtml"));
        viewProvider.openProvider(authId);
        return viewProvider;
    }

    /**
     * Verifies the data blocks in each provider section are expanded or collapsed
     *
     * @param providerType      the provider type being checked (determines the sections to be checked)
     * @param expectExpanded    whether to expect expanded (true) or collapsed (false) data blocks
     */
    public void verifyDataBlocksExpanded(ProviderType providerType, boolean expectExpanded)
    {
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            verifyDataBlocksExpanded(section, expectExpanded);
        }
    }

    /**
     * Verifies the data blocks within a specific section are expanded or collapsed
     *
     * @param section           the provider section to check data blocks within
     * @param expectExpanded    whether to expect expanded (true) or collapsed (false) data blocks
     */
    public void verifyDataBlocksExpanded(ProviderSection section, boolean expectExpanded)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (int i = 0; i < viewProvider.grabDataBlockCount(section); i++)
        {
            assertEquals(
                    viewProvider.grabDataBlockExpanded(section, i),
                    expectExpanded,
                    String.format("Data block expanded (%s:%d)", section, i)
                    );
        }
    }

    /**
     * Verifies the data blocks on a provider page are sorted in each provider section (by values and dates)
     *
     * @param providerType  the provider type under testing - determines the sections to check sorting in
     */
    public void verifyDataBlockSortOrder(ProviderType providerType)
    {
        final Set<ProviderSection> singleDataBlock = new HashSet<>(Arrays.asList(
                ProviderSection.ROLE_TYPE,
                ProviderSection.CONFIDENTIALITY,
                ProviderSection.DEMOGRAPHICS
        ));

        for (ProviderSection section : ProviderSection.getProviderSectionSet(providerType))
        {
            if (!singleDataBlock.contains(section)) verifyDataBlockSortOrder(section);
        }
    }

    /**
     * Verifies all data blocks in a provider section on the page is sorted (by values and dates)
     *
     * @param section           the provider section to check sorting in
     */
    public void verifyDataBlockSortOrder(ProviderSection section)
    {
        final Set<ProviderSection> effectiveFromExclude = new HashSet<>(Arrays.asList(
                ProviderSection.WORK_LOCATIONS,
                ProviderSection.ORGANIZATION_PROPERTIES,
                ProviderSection.COMMUNICATION_PREFERENCE));

        List<String> sortKeyList = ProviderDataFields.getSortKey(section);
        List<String> dateKeyList = new ArrayList<>();
        boolean useActive = section.equals(ProviderSection.STATUSES);

        if (!effectiveFromExclude.contains(section)) dateKeyList.add(IdentifierField.EFFECTIVE_FROM.getString());
        dateKeyList.add(IdentifierField.DB_CREATED.getString());

        List<String> previousValueList = null;
        List<String> previousDateList  = null;
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (int i = 0; i < viewProvider.grabDataBlockCount(section); i++)
        {
            Map<String,String> dataMap = viewProvider.grabDataBlockContent(section, i);
            List<String> valueList = extractDataValueList(sortKeyList, dataMap);
            List<String> dateList  = extractDataValueList(dateKeyList, dataMap);
            if (useActive)
            {
                valueList.addFirst(
                        viewProvider.grabDataBlockActive(section, i) ? "ACTIVE" : "INACTIVE");
            }
            if (previousValueList != null)
            {
                int result;

                // Compare values in ascending order (except if it's a facility relationship)
                if (section.equals(ProviderSection.FACILITY_RELATIONSHIPS))
                    result = TextUtils.compareStringLists(valueList, previousValueList);
                else result = TextUtils.compareStringLists(previousValueList, valueList);

                if (result == 0) result = TextUtils.compareStringLists(dateList, previousDateList);
                if (result > 0)
                {
                    String msg = String.format("Incorrect data block order (%s:%d).", section, i);
                    throw new IllegalStateException(msg);
                }
            }
            previousDateList  = dateList;
            previousValueList = valueList;
        }
    }

    /**
     * Verifies data blocks on the page have expected end reason values depending on the view mode used
     *
     * @param viewMode  the View Mode used on the page
     */
    @SuppressWarnings("fallthrough")
    public void verifyEndReason(ProviderType providerType, ViewMode viewMode)
    {
        Set<String> endReasons = new HashSet<>(Set.of(""));
        // every case is a fall through as each view mode is more and more strict
        switch (viewMode) {
            case AUDIT:
                endReasons.add("CORR");
            case HISTORY:
                endReasons.add("CHG");
                endReasons.add("CEASE");
            case CURRENT:
            default:
        }

        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            if (section.equals(ProviderSection.ROLE_TYPE)) continue;

            for (int i = 0; i < viewProvider.grabDataBlockCount(section); i++)
            {
                Map<String,String> dataMap = viewProvider.grabDataBlockContent(section, i);
                if (section.equals(ProviderSection.WORK_LOCATIONS))
                {
                    List<String> endReasonKeys = dataMap.keySet()
                            .stream().filter(s -> s.contains("End Reason")).toList();

                    for (String wlEntityKey : endReasonKeys)
                    {
                        assertTrue(endReasons.contains(dataMap.get(wlEntityKey)), "Work Locations' " +
                                wlEntityKey + "contains unexpected End Reason in View Mode" + viewMode.getItemText());
                    }
                    continue;
                }
                assertTrue(endReasons.contains(dataMap.get("End Reason")),
                        "Section " + section.getTitle() + " contains unexpected End Reason in View Mode "
                                + viewMode.getItemText());
            }
        }
    }

    /**
     * Verifies the required sections include data and that other sections do not contain data
     * (intended to be used on a Minimum Data Set provider)
     *
     * @param providerType  the provider type under testing - determines set of required sections
     * @param noPerms       whether the session has no permissions to view fields (true) or not (false)
     */
    public void verifyRequiredSections(ProviderType providerType, boolean noPerms)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            String noRecordsNotice = viewProvider.grabSectionNoRecordsNotice(section);
            if (section.isRequired())
            {
                if (!noPerms || section.equals(ProviderSection.REGISTRY_IDENTIFIERS)) assertNull(noRecordsNotice, "Section contains data.");
                else assertEquals(noRecordsNotice, "No permissions to view this record.");
            }
            else
            {
                LOG.info(section);
                assertTrue(noRecordsNotice.startsWith("There are no "), "No records to display.");
            }
        }
    }

    /**
     * Verifies the data field names of each provider section
     *
     * @param providerType  the provider type under testing - determines the set of provider sections to check
     */
    public void verifySectionDataFieldNames(ProviderType providerType)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            for (int i = 0; i < viewProvider.grabDataBlockCount(section); i++)
            {
                Map<String,String> dataMap = viewProvider.grabDataBlockContent(section, i);
                List<String> fieldNameList = new ArrayList<>(dataMap.keySet());
                List<String> expectedFieldNameList = ProviderDataFields.getFieldNameList(section);
                if (section.equals(ProviderSection.IDENTIFIERS)
                    && !fieldNameList.contains("Hds Type"))
                {
                    expectedFieldNameList = new ArrayList<>(expectedFieldNameList);
                    expectedFieldNameList.remove("Hds Type");
                }
                if (section.equals(ProviderSection.WORK_LOCATIONS))
                {
                    assertTrue(new HashSet<>(fieldNameList).containsAll(expectedFieldNameList),
                            "Work Location does not contain expected base data field names");
                    continue;
                }
                assertEquals(fieldNameList, expectedFieldNameList,
                        String.format("Data field name list (%s)", section));
            }
        }
    }

    /**
     * Gets the expected view header title for a view provider page based on a JSONObject
     *
     * @param providerType  the provider type to model the title after
     * @param provider      the provider JSONObject with expected information
     * @return              the expected view title for the view provider page as a string
     */
    @Deprecated
    public String getViewTitle(ProviderType providerType, JSONObject provider)
    {
        return switch (providerType) {
            case BC_PRACTITIONER, OOP_PRACTITIONER -> {
                JSONObject name = provider.getJSONObject("name");
                yield name.getString("last") +
                        ", " +
                        name.getString("first") +
                        " - " +
                        provider.getString("cpn") +
                        "(" +
                        provider.getString("owner") +
                        ") - " +
                        provider.getString("status");
            }
            case ORGANIZATION -> provider.getString("name") +
                    "(" +
                    provider.getString("owner") +
                    ") - " +
                    provider.getString("status");
            default -> {
                String msg = String.format("Unsupported provider type (%s).", providerType);
                throw new IllegalStateException(msg);
            }
        };
    }

    /**
     * Gets the expected view header title for the view provider page for an organization
     *
     * @param org   a MaintainOrgBuilder reference (FHIR) to model the title after
     * @return      the expected view header title
     */
    public String getViewTitle(MaintainOrgBuilder org)
    {
        String status = org.getStatusList().getFirst().get("status");
        status = " - " + status.charAt(0) + status.substring(1).toLowerCase();

        return org.getName() +
                "(" + org.getIdentifierOwners().get(IdentifierType.ORGID) + ")" + status;
    }

    /**
     * Gets the expected view header title for the view provider page for an organization
     *
     * @param ind   a MaintainIndividualBuilder reference (FHIR) to model the title after
     * @return      the expected view header title
     */
    public String getViewTitle(MaintainIndividualBuilder ind)
    {
        String status = ind.getStatusList().getFirst().get("status");
        status = " - " + status.charAt(0) + status.substring(1).toLowerCase();


        String name = ind.getFamilyName() + ", " + ind.getNames()[0];
        if (!ind.getNames()[1].isEmpty()) name = name + " " + ind.getNames()[1];

        return name + " - " + ind.getIdentifier(IdentifierType.CPN) + "(CDS)" + status;
    }

    /**
     * Verifies the provider section titles are their expected values
     *
     * @param providerType  the provider type under testing - determines section set to check
     */
    public void verifySectionTitles(ProviderType providerType)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            assertEquals(
                    viewProvider.grabSectionTitle(section),
                    section.getTitle(),
                    "Provider section title");
        }
    }

    /**
     * Verifies a section contains active or inactive data blocks.
     *
     * @param providerType      the provider type under testing - determines sections to check
     *
     * @param expectedInactive  whether we expect inactive data blocks (true) or not
     */
    public void verifySectionsWithActiveDataBlocks(
            ProviderType providerType,
            boolean expectedInactive)
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        ViewMode viewMode = viewProvider.getViewHeader().grabViewMode();
        for (ProviderSection section : getProviderSectionSet(providerType, userType_))
        {
            if (section.equals(ProviderSection.ROLE_TYPE)) continue;

            //TODO: the following sections should be addressed later when update provider page objects have been developed
            if (section.equals(ProviderSection.COMMUNICATION_PREFERENCE)) continue;
            if (section.equals(ProviderSection.REGISTRY_USER_RELATIONSHIPS)) continue;

            viewProvider.scrollToSection(section);
            assertTrue(
                    viewProvider.grabSectionDisplayed(section),
                    String.format("Section displayed (%s)", section));
            assertTrue(
                    viewProvider.grabActiveDataBlockCount(section, true) > 0,
                    String.format("Presence of active data blocks (%s)", section));
            if (!expectedInactive)
            {
                assertEquals(viewProvider.grabActiveDataBlockCount(section, false), 0,
                        String.format("Absence of inactive data blocks (%s)", section));
            }
            else
            {
                if (viewMode.equals(ViewMode.HISTORY) && section.equals(ProviderSection.CONFIDENTIALITY))
                {
                    // NOTE: Special case
                    assertEquals(viewProvider.grabActiveDataBlockCount(section, false), 0,
                            String.format("Absence of inactive data blocks (%s)", section));
                }
                else
                {
                    assertTrue(
                            viewProvider.grabActiveDataBlockCount(section, false) > 0,
                            String.format("Presence of inactive data blocks (%s)", section));
                }
            }
        }
    }

    /**
     * Verifies the links in the view header are visible as expected upon view facility page load
     *
     * @return  whether the print button, view mode button, and expand all button are displayed (true) or not (false)
     */
    public boolean verifyLinks()
    {
        ViewProviderPage viewProvider = waitForViewProviderPage();
        ViewHeaderFragment header = viewProvider.getViewHeader();

        return header.grabPrintButtonDisplayed() &&
                header.grabViewModeButtonDisplayed() &&
                header.grabExpandAllDisplayed();
    }

    /**
     * Compares the data block records between the webapp and FHIR endpoint response.
     *
     * @param providerType  the provider type of the provider to compare
     * @param provider      the provider builder (FHIR) to use as a comparison point. can be organization or individual
     * @param fhir          a FHIRController reference for further queries if necessary
     */
    public void compareRecords(ProviderType providerType, MaintainRequestBuilder provider, FHIRController fhir) {
        ViewProviderPage viewProvider = waitForViewProviderPage();

        MaintainOrgBuilder orgProvider = null;
        MaintainIndividualBuilder indivProvider = null;
        switch (providerType)
        {
            case ORGANIZATION -> orgProvider = (MaintainOrgBuilder) provider;
            case BC_PRACTITIONER, OOP_PRACTITIONER -> indivProvider = (MaintainIndividualBuilder) provider;
        }
        boolean isOrganization = !Objects.isNull(orgProvider);

        // Identifiers
        for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.IDENTIFIERS); i++) {
            IdentifierType idType;
            Map<String, String> idMap = viewProvider.grabDataBlockContent(ProviderSection.IDENTIFIERS, i);
            idType = switch (idMap.get("Type")) {
                case "Common Party Number (CPN)" -> IdentifierType.CPN;
                case "Internal Provider Code (IPC)" -> IdentifierType.IPC;
                case "Organization (ORGID)" -> IdentifierType.ORGID;
                case "Out of Province Provider (OOPID)" -> IdentifierType.OOPID;
                case "Dentist ID Number (DENID)" -> IdentifierType.DENID;
                default -> {
                    String msg = String.format("Unexpected Identifier type in Webpage (%s)", idMap.get("Type"));
                    throw new IllegalStateException(msg);
                }
            };

            final String expectedIdentifier = isOrganization ?
                    orgProvider.getIdentifier(idType) : indivProvider.getIdentifier(idType);
            final String expectedOwner = isOrganization ?
                    orgProvider.getIdentifierOwners().get(idType) : indivProvider.getIdentifierOwners().get(idType);

            assertEquals(idMap.get("Identifier"), expectedIdentifier,
                    "Identifier in webapp does not match FHIR response");
            assertEquals(idMap.get("Data Owner Code"), expectedOwner,
                    "Identifier owner in webapp does not match FHIR response");
        }

        // Role Type
        String roleType = viewProvider.grabDataBlockContent(ProviderSection.ROLE_TYPE, 0)
                .get("Role Type").split(" ")[0];
        final String expectedRoleType = isOrganization ?
                orgProvider.getRoleType().getRoleType() : indivProvider.getRoleType().getRoleType();
        assertEquals(roleType, expectedRoleType, "Role Type in webapp does not match FHIR response");

        // Statuses
        for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.STATUSES); i++) {
            Map<String, String> webStatusMap = viewProvider.grabDataBlockContent(ProviderSection.STATUSES, i);
            Map<String, String> fhirStatusMap = isOrganization ?
                    orgProvider.getStatusList().get(i) : indivProvider.getStatusList().get(i);

            List<String> statusFields = new ArrayList<>();

            for (String statusField : List.of("Type", "Class", "Reason")) {
                Matcher statusMatcher = DATA_KEY_PARENS_PATTERN.matcher(webStatusMap.get(statusField));
                if (statusMatcher.find()) statusFields.add(statusMatcher.group(1));
                else statusFields.add(null);
            }

            assertEquals(fhirStatusMap.get("status"), statusFields.get(0),
                    "Status Type in webapp does not match FHIR response");
            assertEquals(fhirStatusMap.get("statusClass"), statusFields.get(1),
                    "Status Class in webapp does not match FHIR response");
            assertEquals(fhirStatusMap.get("statusReason"), statusFields.get(2),
                    "Status Reason in webapp does not match FHIR response");
        }

        // Expertise
        if (!isOrganization)
        {
            for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.EXPERTISE); i++) {
                Map<String,String> webExpertiseMap = viewProvider.grabDataBlockContent(ProviderSection.EXPERTISE, i);
                Map<String,String> fhirExpertiseMap = indivProvider.getExpertiseList().get(i);

                String expertiseType;
                Matcher expertiseMatcher = DATA_KEY_PARENS_PATTERN.matcher(webExpertiseMap.get("Type"));
                if (expertiseMatcher.find()) expertiseType = expertiseMatcher.group(1);
                else expertiseType = null;

                assertEquals(fhirExpertiseMap.get("code"), expertiseType,
                        "Expertise Type in webapp does not match FHIR response");
                assertEquals(fhirExpertiseMap.get("sourceCode"), webExpertiseMap.get("Source's Code"),
                        "Expertise Source Code in webapp does not match FHIR response");
            }
        }

        // Credentials
        if (!isOrganization)
        {
            for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.CREDENTIALS); i++) {
                Map<String,String> webCredentialMap = viewProvider.grabDataBlockContent(ProviderSection.CREDENTIALS, i);
                Map<String,String> fhirCredentialMap = indivProvider.getCredentialList().get(i);

                String credentialType;
                Matcher credentialMatcher = DATA_KEY_PARENS_PATTERN.matcher(webCredentialMap.get("Credential Type"));
                if (credentialMatcher.find()) credentialType = credentialMatcher.group(1);
                else credentialType = null;

                final String webEquivalency = switch (webCredentialMap.get("Equivalency Flag")) {
                    case "Yes" -> "true";
                    case "No" -> "false";
                    default -> null;
                };

                assertEquals(fhirCredentialMap.get("type"), credentialType,
                        "Credential Type in webapp does not match FHIR response");
                assertEquals(fhirCredentialMap.get("designation"), webCredentialMap.get("Designation"),
                        "Credential Designation in webapp does not match FHIR response");
                assertEquals(fhirCredentialMap.get("registrationNumber"), webCredentialMap.get("Registration Number"),
                        "Credential Registration Number in webapp does not match FHIR response");
                assertEquals(fhirCredentialMap.get("institution"), webCredentialMap.get("Granting Institution"),
                        "Credential Granting Institution in webapp does not match FHIR response");
                assertEquals(fhirCredentialMap.get("city"), webCredentialMap.get("Institution City"),
                        "Credential Institution City in webapp does not match FHIR response");
                assertEquals(fhirCredentialMap.get("year"), webCredentialMap.get("Year Issued"),
                        "Credential Year Issued in webapp does not match FHIR response");
                assertEquals(fhirCredentialMap.get("equivalency"), webEquivalency,
                        "Credential Equivalency Flag in webapp does not match FHIR response");
            }
        }

        // Organization/Practitioner Name
        if (isOrganization)
        {
            String name = viewProvider.grabDataBlockContent(ProviderSection.ORGANIZATION_NAMES, 0).get("Name");
            assertEquals(name, orgProvider.getName(), "Name in webapp does not match FHIR response");
        }
        else
        {
            String surname = viewProvider.grabDataBlockContent(ProviderSection.PRACTITIONER_NAMES, 0).get("Surname");
            List<String> webNames = List.of(
                    viewProvider.grabDataBlockContent(ProviderSection.PRACTITIONER_NAMES, 0).get("First Name"),
                    viewProvider.grabDataBlockContent(ProviderSection.PRACTITIONER_NAMES, 0).get("Second Name"),
                    viewProvider.grabDataBlockContent(ProviderSection.PRACTITIONER_NAMES, 0).get("Third Name")
            );
            assertEquals(webNames, Arrays.asList(indivProvider.getNames()),
                    "First/Middle/Third Names in webapp do not match FHIR response");
            assertEquals(surname, indivProvider.getFamilyName(),
                    "Surname in webapp does not match FHIR response");
        }

        // Addresses
        for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.ADDRESSES); i++) {
            Map<String, String> webAddressMap = viewProvider.grabDataBlockContent(ProviderSection.ADDRESSES, i);
            Map<String, String> fhirAddressMap = isOrganization ?
                    orgProvider.getAddressList().get(i) : indivProvider.getAddressList().get(i);

            List<String> addressFields = new ArrayList<>();

            for (String addressField : List.of("Address Type", "Address Purpose")) {
                Matcher addressMatcher = DATA_KEY_PARENS_PATTERN.matcher(webAddressMap.get(addressField));
                if (addressMatcher.find()) addressFields.add(addressMatcher.group(1));
                else addressFields.add(null);
            }

            assertEquals("" + fhirAddressMap.get("type").toUpperCase().charAt(0), addressFields.get(0),
                    "Address Type in webapp does not match FHIR response");
            assertEquals(fhirAddressMap.get("purpose"), addressFields.get(1),
                    "Address Purpose in webapp does not match FHIR response");
            assertEquals(fhirAddressMap.get("line1"), webAddressMap.get("Address Line 1"),
                    "Address Line 1 in webapp does not match FHIR response");
            assertEquals(fhirAddressMap.get("city"), webAddressMap.get("City"),
                    "City in webapp does not match FHIR response");
            assertEquals(fhirAddressMap.get("postalCode"), webAddressMap.get("Postal/Zip Code"),
                    "Postal Code in webapp does not match FHIR response");
        }

        // Telecommunications
        final Map<String, String> telecomType = Map.of(
                "fax", "FAX",
                "other", "M",
                "sms", "MB",
                "pager", "PG",
                "phone", "T");
        for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.TELECOMMUNICATIONS); i++) {
            Map<String, String> webTelecomMap = viewProvider.grabDataBlockContent(ProviderSection.TELECOMMUNICATIONS, i);
            // TODO: e-addresses are missing from organization telecomList currently, remember to fix this line once that changes
            Map<String, String> fhirTelecomMap = isOrganization ?
                    orgProvider.getTelecomList().get(i) : indivProvider.getTelecomList().get(i+3);

            List<String> telecomFields = new ArrayList<>();

            for (String telecomField : List.of("Type", "Purpose")) {
                Matcher telecomMatcher = DATA_KEY_PARENS_PATTERN.matcher(webTelecomMap.get(telecomField));
                if (telecomMatcher.find()) telecomFields.add(telecomMatcher.group(1));
                else telecomFields.add(null);
            }

            assertEquals(telecomType.get(fhirTelecomMap.get("type")), telecomFields.get(0),
                    "Telecom Type in webapp does not match FHIR response");
            assertEquals(fhirTelecomMap.get("purpose"), telecomFields.get(1),
                    "Telecom Purpose in webapp does not match FHIR response");
            assertEquals(fhirTelecomMap.get("value"),
                    webTelecomMap.get("Area Code") + webTelecomMap.get("Number") + webTelecomMap.get("Extension"),
                    "Number in webapp does not match FHIR response");
        }

        // Electronic Addresses
        // TODO: electronic addresses are missing from the organization builder telecomList, so individual-only tests currently.
        if (!isOrganization)
        {
            for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.ELECTRONIC_ADDRESSES); i++)
            {
                Map<String,String> webTelecomMap = viewProvider.grabDataBlockContent(ProviderSection.ELECTRONIC_ADDRESSES, i);
                Map<String,String> fhirTelecomMap = isOrganization ?
                        orgProvider.getTelecomList().get(i) : indivProvider.getTelecomList().get(i);

                List<String> eAddressFields = new ArrayList<>();

                for (String eAddressField : List.of("Type", "Purpose"))
                {
                    Matcher eAddressMatcher = DATA_KEY_PARENS_PATTERN.matcher(webTelecomMap.get(eAddressField));
                    if (eAddressMatcher.find()) eAddressFields.add(eAddressMatcher.group(1));
                    else eAddressFields.add(null);
                }

                String fhirAddressType;
                if (fhirTelecomMap.get("type").equals("email")) fhirAddressType = "E";
                else if (fhirTelecomMap.get("type").equals("url")) {
                    if (fhirTelecomMap.get("value").contains("ftp://")) fhirAddressType = "F";
                    else fhirAddressType = "H";
                } else fhirAddressType = null;

                assertEquals(fhirAddressType, eAddressFields.get(0), "E-Address Type in webapp does not match FHIR response");
                assertEquals(fhirTelecomMap.get("purpose"), eAddressFields.get(1), "E-Address Purpose in webapp does not match FHIR response");
                assertEquals(fhirTelecomMap.get("value"), webTelecomMap.get("Address"), "E-Address value in webapp does not match FHIR response");
            }
        }

        // Demographics
        if (!isOrganization)
        {
            Map<String,String> webDemographicMap = viewProvider.grabDataBlockContent(ProviderSection.DEMOGRAPHICS, 0);
            Map<String,String> fhirDemographicMap = indivProvider.getDemographics();

            List<String> demographicFields = new ArrayList<>();

            for (String demographicField : List.of("Birth Prov/State", "Gender"))
            {
                Matcher demographicMatcher = DATA_KEY_PARENS_PATTERN.matcher(webDemographicMap.get(demographicField));
                if (demographicMatcher.find()) demographicFields.add(demographicMatcher.group(1));
                else demographicFields.add(null);
            }

            final String webCountry = webDemographicMap.get("Birth Country").split(" ")[0];

            assertEquals(fhirDemographicMap.get("birthDate"), webDemographicMap.get("Birth Date"),
                    "Birth Date in webapp does not match FHIR response");
            assertEquals(fhirDemographicMap.get("deathDate"), webDemographicMap.get("Death Date"),
                    "Death Date in webapp does not match FHIR response");
            assertEquals(fhirDemographicMap.get("birthCountry"), webCountry,
                    "Birth Country in webapp does not match FHIR response");
            assertEquals(fhirDemographicMap.get("birthProvince"), demographicFields.get(0),
                    "Birth Province / State in webapp does not match FHIR response");
            assertEquals(String.valueOf(fhirDemographicMap.get("gender").toUpperCase().charAt(0)),
                    demographicFields.get(1), "Gender in webapp does not match FHIR response");
        }

        // TODO: Work Locations

        // Conditions
        if (!isOrganization)
        {
            for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.CONDITIONS); i++)
            {
                Map<String,String> webConditionMap = viewProvider.grabDataBlockContent(ProviderSection.CONDITIONS, i);
                Map<String,String> fhirConditionMap = indivProvider.getConditionList().get(i);

                String webType;
                Matcher conditionMatcher = DATA_KEY_PARENS_PATTERN.matcher(webConditionMap.get("Type"));
                if (conditionMatcher.find()) webType = conditionMatcher.group(1);
                else webType = null;

                final String webRestriction = switch (webConditionMap.get("Restriction Flag")) {
                    case "Yes" -> "true";
                    case "No" -> "false";
                    default -> null;
                };

                assertEquals(fhirConditionMap.get("type"), webType,
                        "Condition Type in webapp does not match FHIR response");
                assertEquals(fhirConditionMap.get("restriction"), webRestriction,
                        "Restriction Flag in webapp does not match FHIR response");
                assertEquals(fhirConditionMap.get("explanation"), webConditionMap.get("Restriction Explanation Text"),
                        "Restriction Explanation Text in webapp does not match FHIR response");
            }
        }

        // Disciplinary Actions
        if (!isOrganization)
        {
            for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.DISCIPLINARY_ACTIONS); i++)
            {
                Map<String,String> webDispActionMap = viewProvider.grabDataBlockContent(
                        ProviderSection.DISCIPLINARY_ACTIONS, i);
                Map<String,String> fhirDispActionMap = indivProvider.getDisciplinaryActionList().get(i);

                final String webDisplay = switch (webDispActionMap.get("Display Flag")) {
                    case "Yes" -> "true";
                    case "No" -> "false";
                    default -> null;
                };

                assertEquals(fhirDispActionMap.get("display"), webDisplay,
                        "Display Flag in webapp does not match FHIR response");
                assertEquals(fhirDispActionMap.get("archiveDate"), webDispActionMap.get("Archive Date"),
                        "Archive Date in webapp does not match FHIR response");
                assertEquals(fhirDispActionMap.get("description"), webDispActionMap.get("Description"),
                        "Disciplinary Action Description in webapp does not match FHIR response");
            }
        }

        // TODO: Communication Preferences / Information Routes

        // Confidentiality
        if (!isOrganization) {
            String webConfidentialityFlag = viewProvider.grabDataBlockContent(ProviderSection.CONFIDENTIALITY, 0)
                    .get("Confidentiality Flag");
            Boolean webConfidentiality = switch (webConfidentialityFlag) {
                case "Yes" -> true;
                case "No" -> false;
                default -> null;
            };
            Boolean fhirConfidentiality = isOrganization ?
                    orgProvider.getConfidentiality() : indivProvider.getConfidentiality();


            assertEquals(fhirConfidentiality, webConfidentiality,
                    "Confidentiality flag does not match FHIR response");
        }

        // Provider Relationships
        List<Map<String,String>> fhirOrgRels = isOrganization ?
                orgProvider.getOrganizationRelationshipList() : indivProvider.getOrganizationRelationshipList();
        List<Map<String,String>> fhirIndRels = isOrganization ?
                orgProvider.getIndividualRelationshipList() : indivProvider.getIndividualRelationshipList();
        for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.PROVIDER_RELATIONSHIPS); i++)
        {
            Map<String,String> webProvRelMap = viewProvider.grabDataBlockContent(ProviderSection.PROVIDER_RELATIONSHIPS, i);

            String webRelType;
            Matcher provRelMatcher = DATA_KEY_PARENS_PATTERN.matcher(webProvRelMap.get("Relationship Type"));
            if (provRelMatcher.find()) webRelType = provRelMatcher.group(1);
            else webRelType = null;

            Map<String,String> fhirProvRelMap = null;
            if (OrgRoleType.resolveRoleType(webProvRelMap.get("Role of Related Provider")) != null)
            {
                for (Map<String,String> orgRel : fhirOrgRels)
                {
                    if (orgRel.get("code").equals(webRelType)) {
                        fhirProvRelMap = orgRel;
                        break;
                    }
                }
            } else
            {
                for (Map<String,String> indRel : fhirIndRels)
                {
                    if (indRel.get("code").equals(webRelType)) {
                        fhirProvRelMap = indRel;
                        break;
                    }
                }
            }

            if (fhirProvRelMap == null) Assert.fail("Provider Relationship in webapp not found in FHIR response");

            assertEquals(fhirProvRelMap.get("code"), webRelType,
                    "Provider Relationship Type in webapp does not match FHIR response");
            assertEquals(fhirProvRelMap.get("identifier"), webProvRelMap.get("Related Provider Identifier"),
                    "Related Provider Identifier in webapp does not match FHIR response");
        }

        // Facility Relationships
        if (isOrganization)
        {
            for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.FACILITY_RELATIONSHIPS); i++)
            {
                Map<String,String> webFacRelMap = viewProvider.grabDataBlockContent(ProviderSection.FACILITY_RELATIONSHIPS, i);
                Map<String,String> fhirFacRelMap = orgProvider.getFacilityRelationshipList().get(i);

                IdentifierType facilityType = IdentifierType.resolveIdentifierType(fhirFacRelMap.get("type"));
                MaintainFacilityBuilder facility = fhir.queryFacilityByIdentifier(facilityType, fhirFacRelMap.get("identifier"));

                assertEquals(fhirFacRelMap.get("identifier"), webFacRelMap.get("Related Facility Identifier"),
                        "Related Facility Identifier in webapp does not match FHIR response");
                assertEquals(facility.getName(), webFacRelMap.get("Related Facility Name"),
                        "Related Facility Name in webapp does not match FHIR response");
            }
        }

        // TODO: Registry User Relationships

        // Notes
        for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.NOTES); i++)
        {
            String webNote = viewProvider.grabDataBlockContent(ProviderSection.NOTES, i).get("Note Text");
            String fhirNote = isOrganization ?
                    orgProvider.getNoteList().get(i).get("text") : indivProvider.getNoteList().get(i).get("text");

            assertEquals(webNote, fhirNote, "Note Text in webapp does not match FHIR response");
        }

        // Organization Properties
        if (isOrganization) {
            OrganizationProperties fhirOrgProp = orgProvider.getOrganizationProperties();
            List<List<String>> orgPropLists = List.of(
                    new ArrayList<>(fhirOrgProp.getClinicHoursOfOperation()),
                    new ArrayList<>(fhirOrgProp.getAddressUnit()),
                    new ArrayList<>(fhirOrgProp.getClinicOwnerNames()),
                    new ArrayList<>(fhirOrgProp.getPayeeNumber()));

            for (int i = 0; i < viewProvider.grabDataBlockCount(ProviderSection.ORGANIZATION_PROPERTIES); i++) {
                Map<String, String> webPropMap = viewProvider.grabDataBlockContent(ProviderSection.ORGANIZATION_PROPERTIES, i);

                String propType = webPropMap.get("Property Type");
                Matcher propMatcher = DATA_KEY_PARENS_PATTERN.matcher(propType);
                if (propMatcher.find()) propType = propMatcher.group(1);

                String fhirValue = switch (propType) {
                    case "PCI_FLAG" -> fhirOrgProp.getPciFlag().toString();
                    case "CLINIC_TYPE" -> fhirOrgProp.getClinicType().getText();
                    case "CLINIC_SERVICES" -> fhirOrgProp.getClinicServices().getText();
                    case "CLINIC_OWNER_BUSINESS_TYPE" -> fhirOrgProp.getClinicOwnerBusinessType().getText();
                    case "CLINIC_LEGAL_BUSINESS_NAME" -> fhirOrgProp.getClinicLegalBusinessName();
                    case "CLINIC_HOURS_OF_OPERATION" -> orgPropLists.get(0).removeFirst();
                    case "ADDRESS_UNIT" -> orgPropLists.get(1).removeFirst();
                    case "CLINIC_OWNER_NAMES" -> orgPropLists.get(2).removeFirst();
                    case "PAYEE_NUMBER" -> orgPropLists.get(3).removeFirst();
                    default -> {
                        String msg = String.format("Unexpected property type (%s)", propType);
                        throw new IllegalStateException(msg);
                    }
                };

                assertEquals(fhirValue, webPropMap.get("Property Value"),
                        "Organization Property Value does not match FHIR response");
            }
        }
    }

    private static List<String> extractDataValueList(
            List<String> keyList, Map<String,String> dataMap)
    {
        List<String> valueList = new ArrayList<>();
        for (String key : keyList)
        {
            requireNonNull(key, "Null key.");
            String value = dataMap.get(key);
            if (value != null)
            {
                Matcher matcher = VALUE_CODE_PATTERN.matcher(value);
                if (matcher.matches())
                {
                    value = matcher.group("code");
                }
                valueList.add(value);
            }
            else
            {
                String msg = String.format("Null value for key (%s).", key);
                throw new NullPointerException(msg);
            }
        }
        return valueList;
    }

    private static Set<ProviderSection> getProviderSectionSet(
            ProviderType providerType, UserType userType)
    {
        Set<ProviderSection> set =
                EnumSet.copyOf(ProviderSection.getProviderSectionSet(providerType));
        if (!userType.equals(UserType.ADMIN))
        {
            set.remove(ProviderSection.REGISTRY_IDENTIFIERS);
        }
        return Collections.unmodifiableSet(set);
    }

    private ViewProviderPage waitForViewProviderPage()
    {
        ViewProviderPage page = new ViewProviderPage(selenium_);
        page.waitForReady();
        return page;
    }
}
