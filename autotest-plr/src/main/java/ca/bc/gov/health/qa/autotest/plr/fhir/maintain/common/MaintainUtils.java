package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.io.ResourceUtils;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.EndReasonCode;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.HdsType;

/**
 * TODO (AZ) - doc
 */
public class MaintainUtils
{
    private static final String PERIOD_EXTENSION_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-period-extension";
    private static final String SPECIALTY_SOURCE_EXTENSION_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-specialty-source-extension";

    // Organization property extension URLs (centralized for reuse)
    private static final String ORG_PRIMARY_CARE_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-primary-care-clinic-extension";
    private static final String ORG_CLINIC_TYPE_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-type-extension";
    private static final String ORG_CLINIC_OWNERSHIP_TYPE_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-ownership-type-extension";
    private static final String ORG_CLINIC_SERVICES_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-service-delivery-type-extension";
    private static final String ORG_CLINIC_OWNER_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-owner-extension";
    private static final String ORG_PCI_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-pci-extension";
    private static final String ORG_CLINIC_LEGAL_NAME_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-organization-clinic-legal-name-extension";

    private MaintainUtils()
    {}

    /**
     * TODO (AZ) - doc
     *
     * @param info
     *        ???
     *
     * @return ???
     */
    public static JSONObject createAddress(Map<String,String> info)
    {
        JSONObject json = readJsonTemplate("address.json");
        json.put("type", info.get("type"));
        json.getJSONArray("extension")
                .getJSONObject(0)
                .getJSONObject("valueCodeableConcept")
                .getJSONArray("coding")
                .getJSONObject(0)
                .put("code", info.get("purpose"));
        json.getJSONArray("line").put(0, info.get("line1"));
        json.put("city", info.get("city"));
        json.put("postalCode", info.get("postalCode"));
        json.getJSONObject("period").put("start", currentDate());
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param info
     *        ???
     *
     * @return ???
     */
    public static JSONObject createCondition(Map<String,String> info)
    {
        JSONObject json = readJsonTemplate("condition.json");
        JSONArray extensionJson = json.getJSONArray("extension");
        findEntry(extensionJson, "url", "code")
                .getJSONObject("valueCodeableConcept")
                .getJSONArray("coding")
                .getJSONObject(0)
                .put("code", info.get("type"));
        findEntry(extensionJson, "url", "restrictionText")
                .put("valueString", info.get("explanation"));
        findEntry(extensionJson, "url", "restriction")
                .put("valueBoolean", Boolean.parseBoolean(info.get("restriction")));
        findEntry(extensionJson, "url", PERIOD_EXTENSION_URL)
                .getJSONObject("valuePeriod").put("start", currentDate());
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param confidentiality
     *        ???
     *
     * @return ???
     */
    public static JSONObject createConfidentiality(boolean confidentiality)
    {
        JSONObject json = readJsonTemplate("confidentiality.json");
        JSONArray extensionJson = json.getJSONArray("extension");
        findEntry(extensionJson, "url", "code")
                .getJSONObject("valueCodeableConcept")
                .getJSONArray("coding")
                .getJSONObject(0)
                .put("code", confidentiality ? "R" : "N");
        findEntry(extensionJson, "url", PERIOD_EXTENSION_URL)
                .getJSONObject("valuePeriod").put("start", currentDate());
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param info
     *        ???
     *
     * @param reference
     *        ???
     *
     * @return ???
     */
    public static JSONObject createContained(Map<String,String> info, String reference)
    {
        JSONObject json = readJsonTemplate("contained.json");
        json.put("id", reference);
        json.put("name", info.get("institution"));
        json.getJSONArray("address").getJSONObject(0).put("city", info.get("city"));
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param info
     *        ???
     *
     * @return ???
     */
    public static JSONObject createDisciplinaryAction(Map<String,String> info)
    {
        JSONObject json = readJsonTemplate("disciplinary-action.json");
        JSONArray extensionJson = json.getJSONArray("extension");
        findEntry(extensionJson, "url", "description").put("valueString", info.get("description"));
        findEntry(extensionJson, "url", "displayFlag")
                .put("valueBoolean", Boolean.parseBoolean(info.get("display")));
        String archiveDate = info.get("archiveDate");
        if (archiveDate == null || archiveDate.isBlank())
        {
            archiveDate = "9999-12-31";
        }
        findEntry(extensionJson, "url", "archiveDate").put("valueDateTime", archiveDate);
        findEntry(extensionJson, "url", PERIOD_EXTENSION_URL)
                .getJSONObject("valuePeriod").put("start", currentDate());
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param language
     *        ???
     *
     * @param info
     *        ???
     *
     * @return ???
     */
    public static JSONObject createExpertise(boolean language, Map<String,String> info)
    {
        String template = language ? "communication.json" : "specialty.json";
        JSONObject json = readJsonTemplate(template);
        json.getJSONArray("coding").getJSONObject(0).put("code", info.get("code"));
        JSONArray extensionJson = json.getJSONArray("extension");
        findEntry(extensionJson, "url", SPECIALTY_SOURCE_EXTENSION_URL)
                .put("valueString", info.get("sourceCode"));
        findEntry(extensionJson, "url", PERIOD_EXTENSION_URL)
                .getJSONObject("valuePeriod").put("start", currentDate());
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param info
     *        ???
     *
     * @return ???
     */
    public static JSONObject createNote(Map<String,String> info)
    {
        JSONObject json = readJsonTemplate("note.json");
        JSONArray extensionJson = json.getJSONArray("extension");
        findEntry(extensionJson, "url", "text").put("valueString", info.get("text"));
        findEntry(extensionJson, "url", PERIOD_EXTENSION_URL)
                .getJSONObject("valuePeriod").put("start", currentDate());
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param info
     *        ???
     *
     * @param reference
     *        ???
     *
     * @return ???
     */
    public static JSONObject createQualification(Map<String,String> info, String reference)
    {
        JSONObject json = readJsonTemplate("qualification.json");
        JSONArray extensionJson =
                json.getJSONArray("extension").getJSONObject(0).getJSONArray("extension");
        findEntry(extensionJson, "url", "designation")
                .put("valueString", info.get("designation"));
        findEntry(extensionJson, "url", "registrationNumber")
                .put("valueString", info.get("registrationNumber"));
        findEntry(extensionJson, "url", "equivalencyFlag")
                .put("valueBoolean", Boolean.parseBoolean(info.get("equivalency")));
        findEntry(extensionJson, "url", "issuedDate")
                .put("valueDate", info.get("year"));
        json.getJSONObject("code")
                .getJSONArray("coding")
                .getJSONObject(0)
                .put("code", info.get("type"));
        json.getJSONObject("issuer").put("reference", "#" + reference);
        json.getJSONObject("period").put("start", currentDate());
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param info
     *        ???
     *
     * @return ???
     */
    public static JSONObject createStatus(Map<String,String> info)
    {
        JSONObject json = readJsonTemplate("status.json");
        JSONArray extensionJson = json.getJSONArray("extension");
        findEntry(extensionJson, "url", "statusClassCode")
                .getJSONObject("valueCodeableConcept")
                .getJSONArray("coding")
                .getJSONObject(0)
                .put("code", info.get("statusClass"));
        findEntry(extensionJson, "url", "statusCode")
                .getJSONObject("valueCodeableConcept")
                .getJSONArray("coding")
                .getJSONObject(0)
                .put("code", info.get("status"));
        findEntry(extensionJson, "url", "statusReasonCode")
                .getJSONObject("valueCodeableConcept")
                .getJSONArray("coding")
                .getJSONObject(0)
                .put("code", info.get("statusReason"));
        findEntry(extensionJson, "url", "period")
                .getJSONObject("valuePeriod")
                .put("start", currentDate());
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param info
     *        ???
     *
     * @return ???
     */
    public static JSONObject createTelecom(Map<String,String> info)
    {
        JSONObject json = readJsonTemplate("telecom.json");
        json.getJSONArray("extension")
            .getJSONObject(0)
            .getJSONObject("valueCodeableConcept")
            .getJSONArray("coding")
            .getJSONObject(0)
            .put("code", info.get("purpose"));
        json.put("system", info.get("type"));
        json.put("value", info.get("value"));
        json.getJSONObject("period").put("start", currentDate());
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public static String currentDate()
    {
        return formatDate(Instant.now());
    }

    /**
     * TODO (AZ) - doc
     *
     * @param time
     *        ???
     *
     * @return ???
     */
    public static String formatDate(Instant time)
    {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(time);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param jsonArray
     *        ???
     *
     * @param name
     *        ???
     *
     * @param value
     *        ???
     *
     * @return ???
     *
     * @throws IllegalStateException
     *         ???
     */
    public static JSONObject findEntry(JSONArray jsonArray, String name, String value)
    {
        JSONObject json = null;
        boolean found = false;
        for (int i = 0; i < jsonArray.length(); i++)
        {
            json = jsonArray.getJSONObject(i);
            if (value.equals(json.getString(name)))
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            String msg = String.format("Entry not found (%s:%s).", name, value);
            throw new IllegalStateException(msg);
        }
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public static String generateId()
    {
        long value = System.currentTimeMillis() / 100;
        String id = Long.toString(value, Character.MAX_RADIX).toUpperCase(Locale.ROOT).substring(1);
        return "AT." + id;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param templateName
     *        ???
     *
     * @return ???
     */
    public static JSONObject readJsonTemplate(String templateName)
    {
        // Templates are located under resources at ca/bc/gov/health/qa/autotest/plr/fhir/maintain/
        String path = "/ca/bc/gov/health/qa/autotest/plr/fhir/maintain/" + templateName;
        return new JSONObject(ResourceUtils.readResource(
                MethodHandles.lookup().lookupClass(), path));
    }

        /**
         * Creates the specialized _type extension block used only for organizations whose role type is HDS.
         * The provided hdsType value is written into the nested coding[0].code element of the hdsType extension.
         * Template file: hds-type.json
         *
         * @param hdsType the HDS type classification code
         * @return populated _type extension JSON object ready to attach to Organization JSON
         */
        public static JSONObject createHdsType(HdsType hdsType) {
                JSONObject json = readJsonTemplate("hds-type.json");
                JSONArray outerExtension = json.getJSONArray("extension");
                JSONObject healthDeliverySiteExt = outerExtension.getJSONObject(0); // bc-health-delivery-site-type-extension
                JSONArray innerExtArray = healthDeliverySiteExt.getJSONArray("extension");
                JSONObject hdsTypeExt = findEntry(innerExtArray, "url", "hdsType");
                hdsTypeExt.getJSONObject("valueCodeableConcept")
                                  .getJSONArray("coding")
                                  .getJSONObject(0)
                                  .put("code", hdsType.name());
                return json;
        }

        // ----------------------- Organization Properties helpers -------------------------------

        /**
         * Finds a nested extension node by walking a series of URLs. Starts at the root object's
         * top-level "extension" array and for each url provided, finds the matching entry and
         * drills into its own "extension" array for the next step.
         * @param root root JSON object containing an "extension" array
         * @param urls ordered URL path segments to navigate nested extensions
         * @return the final matched extension node JSON object
         */
        public static JSONObject findNestedExtension(JSONObject root, String... urls) {
                JSONArray current = root.getJSONArray("extension");
                JSONObject node = null;
                for (int i = 0; i < urls.length; i++) {
                        node = findEntry(current, "url", urls[i]);
                        if (i < urls.length - 1) {
                                current = node.getJSONArray("extension");
                        }
                }
                return node;
        }

        private static JSONObject createExtensionWithCode(String template, String[] urlPath, String code) {
                JSONObject json = readJsonTemplate(template);
                JSONObject codeNode = findNestedExtension(json, urlPath);
                codeNode
                        .getJSONObject("valueCodeableConcept")
                        .getJSONArray("coding")
                        .getJSONObject(0)
                        .put("code", code);
                return json;
        }

        private static JSONObject createExtensionWithString(String template, String[] urlPath, String value) {
                JSONObject json = readJsonTemplate(template);
                JSONObject node = findNestedExtension(json, urlPath);
                node.put("valueString", value);
                return json;
        }

        private static JSONObject createExtensionWithBoolean(String template, String[] urlPath, boolean value) {
                JSONObject json = readJsonTemplate(template);
                JSONObject node = findNestedExtension(json, urlPath);
                node.put("valueBoolean", value);
                return json;
        }

        /**
         * Creates clinic type extension block from template and sets code.
         * @param code clinic type code to set
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicType(String code) {
                return createExtensionWithCode(
                        "org-clinic-type.json",
                        new String[]{
                                ORG_PRIMARY_CARE_URL,
                                ORG_CLINIC_TYPE_URL,
                                "code"
                        },
                        code);
        }

        /**
         * Creates clinic ownership business type extension block and sets code.
         * @param code owner business type code to set
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicOwnerBusinessType(String code) {
                return createExtensionWithCode(
                        "org-clinic-owner-business-type.json",
                        new String[]{
                                ORG_PRIMARY_CARE_URL,
                                ORG_CLINIC_OWNERSHIP_TYPE_URL,
                                "code"
                        },
                        code);
        }

        /**
         * Creates clinic services delivery type extension block and sets code.
         * @param code clinic services delivery type code to set
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicServices(String code) {
                return createExtensionWithCode(
                        "org-clinic-services.json",
                        new String[]{
                                ORG_PRIMARY_CARE_URL,
                                ORG_CLINIC_SERVICES_URL,
                                "code"
                        },
                        code);
        }

        /**
         * Creates clinic owner name extension block and sets owner name.
         * @param ownerName clinic owner name value
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicOwnerName(String ownerName) {
                return createExtensionWithString(
                        "org-clinic-owner.json",
                        new String[]{
                                ORG_PRIMARY_CARE_URL,
                                ORG_CLINIC_OWNER_URL,
                                "ownerName"
                        },
                        ownerName);
        }

        /**
         * Creates PCI flag extension block and sets boolean value.
         * @param pciEnabled whether PCI is enabled
         * @return constructed extension JSON object
         */
        public static JSONObject createPciFlag(boolean pciEnabled) {
                return createExtensionWithBoolean(
                        "org-pci.json",
                        new String[]{
                                ORG_PRIMARY_CARE_URL,
                                ORG_PCI_URL,
                                "pciEnabled"
                        },
                        pciEnabled);
        }

        /**
         * Creates clinic legal business name extension and sets string value.
         * @param legalName legal business name value
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicLegalBusinessName(String legalName) {
                return createExtensionWithString(
                        "org-clinic-legal-name.json",
                        new String[]{
                                ORG_PRIMARY_CARE_URL,
                                ORG_CLINIC_LEGAL_NAME_URL,
                                "clinicLegalBusinessName"
                        },
                        legalName);
        }

        /**
         * Creates clinic payee number extension and sets value plus current period start.
         * @param payeeNumber clinic payee number value
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicPayeeNumber(String payeeNumber) {
                JSONObject json = createExtensionWithString(
                        "org-clinic-payee-number.json",
                        new String[]{ "payeeNumber" },
                        payeeNumber);
                JSONArray ext = json.getJSONArray("extension");
                JSONObject periodExt = findEntry(ext, "url", PERIOD_EXTENSION_URL);
                periodExt.getJSONObject("valuePeriod").put("start", currentDate());
                return json;
        }

        /**
         * Creates a Bundle.entry JSON object containing an OrganizationAffiliation resource linking a facility to
         * an organization. The end reason extension code is left as the template default (CHG) indicating a change.
         * @param info organization relationship mapping data (identifier + type/system)
         * @param facilityIdentifier IFC identifier value of the facility
         * @return populated affiliation entry
         */
        public static JSONObject createFacilityOrgAffiliation(Map<String,String> info, String facilityIdentifier) {
                return createFacilityOrgAffiliation(info, facilityIdentifier, (EndReasonCode) null);
        }

        /**
         * Overload supporting an explicit end-reason code override (e.g. CEASE) that replaces the template default.
         * @param info organization relationship mapping data (identifier + type/system)
         * @param facilityIdentifier IFC identifier value of the facility
         * @param endReasonCode optional end reason code (if null template value retained). Use {@link EndReasonCode#CHANGE}
         *                      only if you want to be explicit; the template default is already CHG.
         * @return populated affiliation entry JSON
         */
        public static JSONObject createFacilityOrgAffiliation(Map<String,String> info, String facilityIdentifier, EndReasonCode endReasonCode) {
                JSONObject entry = readJsonTemplate("facility-to-organization-relationship.json");
                // Generate unique fullUrl (urn:uuid)
                String uuid = java.util.UUID.randomUUID().toString();
                entry.put("fullUrl", "urn:uuid:" + uuid);
                JSONObject resource = entry.getJSONObject("resource");

                // Organization identifier
                JSONObject orgIdentifier = resource.getJSONObject("organization").getJSONObject("identifier");
                orgIdentifier.put("system", info.get("type"));
                orgIdentifier.put("value", info.get("identifier"));

                // Location identifier (facility IFC)
                JSONObject locationIdentifier = resource.getJSONArray("location").getJSONObject(0).getJSONObject("identifier");
                locationIdentifier.put("value", facilityIdentifier);

                // Optional end reason code override
                if (endReasonCode != null) {
                        JSONArray extensions = resource.getJSONArray("extension");
                        JSONObject endReasonExt = findEntry(extensions, "url", "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-end-reason-extension");
                        endReasonExt
                                .getJSONObject("valueCodeableConcept")
                                .getJSONArray("coding")
                                .getJSONObject(0)
                                .put("code", endReasonCode.wire());
                }
                return entry;
        }
}
