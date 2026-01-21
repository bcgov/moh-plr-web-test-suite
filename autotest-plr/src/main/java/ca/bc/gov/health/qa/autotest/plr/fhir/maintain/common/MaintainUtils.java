package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final String AVAILABILITY_EXTENSION_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-availability-extension";
    private static final String OWNER_EXTENSION_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-owner-extension";
    private static final String END_REASON_EXTENSION_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-end-reason-extension";
    private static final String RELATIONSHIP_TYPE_EXTENSION_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-relationship-type-extension";
    private static final String END_REASON_CODE_SYSTEM =
            "https://terminology.hlth.gov.bc.ca/ProviderLocationRegistry/CodeSystem/bc-end-reason-code-system";
        


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

    // Practitioner demographics extension URLs
    private static final String PRACTITIONER_BIRTHPLACE_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-birthplace-extension";
    private static final String PRACTITIONER_DEATHDATE_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-practitioner-deathdate-extension";

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
         * Returns the current date-time in ISO offset format (e.g., 2026-01-08T00:00:00-08:00).
         * @return formatted date-time string
         */
        public static String currentDateTime()
        {
                return DateTimeFormatter
                                .ISO_OFFSET_DATE_TIME
                                .withZone(ZoneId.systemDefault())
                                .format(Instant.now());
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
                int startIndex = 0;
                // If the first segment matches the root's url, skip it since we've already "navigated" to root
                if (urls.length > 0 && root.has("url") && urls[0].equals(root.optString("url"))) {
                        startIndex = 1;
                }
                for (int i = startIndex; i < urls.length; i++) {
                        node = findEntry(current, "url", urls[i]);
                        if (i < urls.length - 1) {
                                current = node.getJSONArray("extension");
                        }
                }
                return node;
        }

        /**
         * Creates clinic type extension block from template and sets text value.
         * The new template format uses a leaf node "clinicType" with valueCodeableConcept.text.
         * @param text clinic type text to set
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicType(String text) {
                JSONObject json = readJsonTemplate("org-clinic-type.json");
                JSONObject typeExt = findNestedExtension(json, new String[]{ ORG_PRIMARY_CARE_URL, ORG_CLINIC_TYPE_URL });
                JSONArray inner = typeExt.getJSONArray("extension");
                JSONObject leaf = findEntry(inner, "url", "clinicType");
                leaf.getJSONObject("valueCodeableConcept").put("text", text);
                return json;
        }

        /**
         * Creates clinic ownership business type extension block and sets text value.
         * Leaf node "ownershipType" holds valueCodeableConcept.text.
         * @param text owner business type text to set
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicOwnerBusinessType(String text) {
                JSONObject json = readJsonTemplate("org-clinic-owner-business-type.json");
                JSONObject ownerExt = findNestedExtension(json, new String[]{ ORG_PRIMARY_CARE_URL, ORG_CLINIC_OWNERSHIP_TYPE_URL });
                JSONArray inner = ownerExt.getJSONArray("extension");
                JSONObject leaf = findEntry(inner, "url", "ownershipType");
                leaf.getJSONObject("valueCodeableConcept").put("text", text);
                return json;
        }

        /**
         * Creates clinic services delivery type extension block and sets text value.
         * Leaf node "serviceDeliveryType" holds valueCodeableConcept.text.
         * @param text clinic services delivery type text to set
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicServices(String text) {
                JSONObject json = readJsonTemplate("org-clinic-services.json");
                JSONObject svcExt = findNestedExtension(json, new String[]{ ORG_PRIMARY_CARE_URL, ORG_CLINIC_SERVICES_URL });
                JSONArray inner = svcExt.getJSONArray("extension");
                JSONObject leaf = findEntry(inner, "url", "serviceDeliveryType");
                leaf.getJSONObject("valueCodeableConcept").put("text", text);
                return json;
        }

        /**
         * Creates clinic owner name extension block and sets owner name.
         * @param ownerName clinic owner name value
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicOwnerName(String ownerName) {
                JSONObject json = readJsonTemplate("org-clinic-owner.json");
                JSONObject ext = findNestedExtension(json, new String[]{ ORG_PRIMARY_CARE_URL, ORG_CLINIC_OWNER_URL });
                JSONArray inner = ext.getJSONArray("extension");
                JSONObject leaf = findEntry(inner, "url", "clinicOwner");
                leaf.put("valueString", ownerName);
                return json;
        }

        /**
         * Creates PCI flag extension block and sets boolean value.
         * @param pciFlag whether PCI is enabled
         * @return constructed extension JSON object
         */
        public static JSONObject createPciFlag(boolean pciFlag) {
                JSONObject json = readJsonTemplate("org-pci.json");
                JSONObject ext = findNestedExtension(json, new String[]{ ORG_PRIMARY_CARE_URL, ORG_PCI_URL });
                JSONArray inner = ext.getJSONArray("extension");
                JSONObject leaf = findEntry(inner, "url", "pciFlag");
                leaf.put("valueBoolean", pciFlag);
                return json;
        }

        /**
         * Creates clinic legal business name extension and sets string value.
         * @param legalName legal business name value
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicLegalBusinessName(String legalName) {
                JSONObject json = readJsonTemplate("org-clinic-legal-name.json");
                JSONObject ext = findNestedExtension(json, new String[]{ ORG_PRIMARY_CARE_URL, ORG_CLINIC_LEGAL_NAME_URL });
                JSONArray inner = ext.getJSONArray("extension");
                JSONObject leaf = findEntry(inner, "url", "clinicLegalName");
                leaf.put("valueString", legalName);
                return json;
        }

        /**
         * Creates clinic payee number extension and sets value plus current period start.
         * @param payeeNumber clinic payee number value
         * @return constructed extension JSON object
         */
        public static JSONObject createClinicPayeeNumber(String payeeNumber) {
                JSONObject json = readJsonTemplate("org-clinic-payee-number.json");
                JSONArray ext = json.getJSONArray("extension");
                JSONObject leaf = findEntry(ext, "url", "payeeNumber");
                leaf.put("valueString", payeeNumber);
                JSONObject periodExt = findEntry(ext, "url", PERIOD_EXTENSION_URL);
                periodExt.getJSONObject("valuePeriod").put("start", currentDateTime());
                return json;
        }

        private static String normalizeTime(String hhmm) {
                // Ensure seconds are present e.g., 13:00 -> 13:00:00
                if (hhmm.matches("\\d{2}:\\d{2}:\\d{2}")) return hhmm;
                if (hhmm.matches("\\d{2}:\\d{2}")) return hhmm + ":00";
                return hhmm; // fallback (already formatted)
        }

        /**
         * Creates a bc-availability-extension container where each day/time pair in the input string
         * becomes its own bc-availableTime-extension child.
         * Example: "MON 12:00-13:00 TUE 13:00-14:00" yields one availability container with two
         * availableTime children.
         * The container includes owner and a period start; identifier can be added later if needed.
         * @param hoursEntry one string possibly containing multiple day/time pairs
         * @return constructed availability extension JSON object
         */
        public static JSONObject createClinicAvailability(String hoursEntry) {
                JSONObject container = new JSONObject();
                container.put("url", AVAILABILITY_EXTENSION_URL);
                JSONArray ext = new JSONArray();

                // Owner extension (assignee display defaults to MOH)
                JSONObject ownerExt = new JSONObject();
                ownerExt.put("url", OWNER_EXTENSION_URL);
                JSONObject ownerVal = new JSONObject();
                ownerVal.put("assigner", new JSONObject().put("display", "MOH"));
                ownerExt.put("valueIdentifier", ownerVal);
                ext.put(ownerExt);

                // End reason extension (default code CHG)
                JSONObject endReasonExt = new JSONObject();
                endReasonExt.put("url", END_REASON_EXTENSION_URL);
                JSONObject endReasonVal = new JSONObject();
                JSONArray coding = new JSONArray();
                coding.put(new JSONObject()
                                .put("system", END_REASON_CODE_SYSTEM)
                                .put("code", "CHG"));
                endReasonVal.put("coding", coding);
                endReasonExt.put("valueCodeableConcept", endReasonVal);
                ext.put(endReasonExt);

                // Parse pairs: require dash separator (HH:MM-HH:MM), days are case-insensitive
                Pattern p = Pattern.compile("(?i)([A-Z]{3})\\s+(\\d{2}:\\d{2})(?::\\d{2})?\\s*-\\s*(\\d{2}:\\d{2})(?::\\d{2})?");
                Matcher m = p.matcher(hoursEntry);
                boolean any = false;
                while (m.find()) {
                        any = true;
                        String day = m.group(1).toUpperCase(Locale.ROOT);
                        String start = m.group(2);
                        String end = m.group(3);

                        // Use template for availableTime block shape (includes owner/end-reason/period per template)
                        JSONObject avail = readJsonTemplate("org-clinic-hours.json");
                        JSONArray inner = avail.getJSONArray("extension");

                        // Set leaf values
                        findEntry(inner, "url", "daysOfWeek").put("valueCode", day);
                        findEntry(inner, "url", "availableStartTime").put("valueTime", normalizeTime(start));
                        findEntry(inner, "url", "availableEndTime").put("valueTime", normalizeTime(end));

                        ext.put(avail);
                }

                if (!any) {
                        throw new IllegalArgumentException("Invalid clinic hours entry: " + hoursEntry);
                }

                // Period extension (start only)
                JSONObject periodExt = new JSONObject();
                periodExt.put("url", PERIOD_EXTENSION_URL);
                periodExt.put("valuePeriod", new JSONObject().put("start", currentDateTime()));
                ext.put(periodExt);

                container.put("extension", ext);
                return container;
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
                        JSONObject endReasonExt = findEntry(extensions, "url", END_REASON_EXTENSION_URL);
                        endReasonExt
                                .getJSONObject("valueCodeableConcept")
                                .getJSONArray("coding")
                                .getJSONObject(0)
                                .put("code", endReasonCode.wire());
                }
                return entry;
        }

        /**
         * Helper to generate UUID and set fullUrl on a bundle entry.
         * @param entry bundle entry JSON object
         * @return the generated UUID string
         */
        private static String setFullUrl(JSONObject entry) {
                String uuid = java.util.UUID.randomUUID().toString();
                entry.put("fullUrl", "urn:uuid:" + uuid);
                return uuid;
        }

        /**
         * Helper to set identifier (system and value) on a JSON object.
         * @param target target JSON object containing identifier
         * @param info map with "type" (system) and "identifier" (value) keys
         */
        private static void setIdentifier(JSONObject target, Map<String,String> info) {
                JSONObject identifier = target.getJSONObject("identifier");
                identifier.put("system", info.get("type"));
                identifier.put("value", info.get("identifier"));
        }

        /**
         * Helper to apply end reason code override to a resource's extension array.
         * @param resource resource JSON object
         * @param endReasonCode end reason code to apply (if not null)
         */
        private static void applyEndReasonCode(JSONObject resource, EndReasonCode endReasonCode) {
                if (endReasonCode == null) return;
                
                JSONArray extensions = resource.optJSONArray("extension");
                if (extensions == null) {
                        extensions = new JSONArray();
                        resource.put("extension", extensions);
                }
                
                // Find or create end reason extension
                JSONObject endReasonExt = null;
                for (int i = 0; i < extensions.length(); i++) {
                        JSONObject ext = extensions.optJSONObject(i);
                        if (ext != null && END_REASON_EXTENSION_URL.equals(ext.optString("url"))) {
                                endReasonExt = ext;
                                break;
                        }
                }
                
                if (endReasonExt == null) {
                        // Create new end reason extension
                        endReasonExt = new JSONObject();
                        endReasonExt.put("url", END_REASON_EXTENSION_URL);
                        JSONObject valueCC = new JSONObject();
                        JSONArray coding = new JSONArray();
                        JSONObject codingObj = new JSONObject();
                        codingObj.put("system", END_REASON_CODE_SYSTEM);
                        codingObj.put("code", endReasonCode.wire());
                        coding.put(codingObj);
                        valueCC.put("coding", coding);
                        endReasonExt.put("valueCodeableConcept", valueCC);
                        extensions.put(endReasonExt);
                } else {
                        // Update existing extension
                        endReasonExt
                                .getJSONObject("valueCodeableConcept")
                                .getJSONArray("coding")
                                .getJSONObject(0)
                                .put("code", endReasonCode.wire());
                }
        }

        /**
         * Creates an OrganizationAffiliation entry for organization-to-organization relationships.
         * Uses the organization-relationship.json template.
         * @param orgInfo source organization mapping data (identifier + type/system)
         * @param relatedOrgInfo related organization mapping data (identifier + type/system)
         * @param relationshipCode relationship type code (e.g., P2P, O2F)
         * @return populated affiliation entry
         */
        public static JSONObject createOrganizationOrgAffiliation(Map<String,String> orgInfo, Map<String,String> relatedOrgInfo, String relationshipCode) {
                return createOrganizationOrgAffiliation(orgInfo, relatedOrgInfo, relationshipCode, (EndReasonCode) null);
        }

        /**
         * Overload supporting an explicit end-reason code override (e.g. CEASE) that replaces the template default.
         * @param orgInfo source organization mapping data (identifier + type/system)
         * @param relatedOrgInfo related organization mapping data (identifier + type/system)
         * @param relationshipCode relationship type code (e.g., P2P, O2F)
         * @param endReasonCode optional end reason code (if null template value retained). Use {@link EndReasonCode#CHANGE}
         *                      only if you want to be explicit; the template default is already CHG.
         * @return populated affiliation entry JSON
         */
        public static JSONObject createOrganizationOrgAffiliation(Map<String,String> orgInfo, Map<String,String> relatedOrgInfo, String relationshipCode, EndReasonCode endReasonCode) {
                JSONObject entry = readJsonTemplate("organization-to-organization-relationship.json");
                setFullUrl(entry);
                JSONObject resource = entry.getJSONObject("resource");

                // Organization identifier (source organization)
                setIdentifier(resource.getJSONObject("organization"), orgInfo);

                // Participating organization identifier (related organization)
                setIdentifier(resource.getJSONObject("participatingOrganization"), relatedOrgInfo);

                // Relationship code
                if (relationshipCode != null && !relationshipCode.isEmpty()) {
                        JSONObject codeObj = resource.getJSONArray("code").getJSONObject(0);
                        codeObj.getJSONArray("coding").getJSONObject(0).put("code", relationshipCode);
                }

                // Optional end reason code override
                applyEndReasonCode(resource, endReasonCode);
                
                return entry;
        }

        /**
         * Creates a PractitionerRole entry representing an organization-to-individual relationship.
         * Overload without end reason code - uses template default (CHG).
         * @param orgInfo source organization mapping data (identifier + type/system)
         * @param individualInfo related individual/practitioner mapping data (identifier + type/system)
         * @param relationshipCode relationship type code
         * @return populated practitioner role entry
         */
        public static JSONObject createOrganizationIndividualAffiliation(Map<String,String> orgInfo, Map<String,String> individualInfo, String relationshipCode) {
                return createOrganizationIndividualAffiliation(orgInfo, individualInfo, relationshipCode, (EndReasonCode) null);
        }

        /**
         * Creates a PractitionerRole entry representing an organization-to-individual relationship.
         * Overload supporting an explicit end-reason code override (e.g. CEASE) that replaces the template default.
         * @param orgInfo source organization mapping data (identifier + type/system)
         * @param individualInfo related individual/practitioner mapping data (identifier + type/system)
         * @param relationshipCode relationship type code
         * @param endReasonCode optional end reason code (if null template value retained)
         * @return populated practitioner role entry JSON
         */
        public static JSONObject createOrganizationIndividualAffiliation(Map<String,String> orgInfo, Map<String,String> individualInfo, String relationshipCode, EndReasonCode endReasonCode) {
                JSONObject entry = readJsonTemplate("organiztion-to-individual.json");
                setFullUrl(entry);
                JSONObject resource = entry.getJSONObject("resource");

                // Organization identifier
                setIdentifier(resource.getJSONObject("organization"), orgInfo);

                // Practitioner identifier (individual)
                setIdentifier(resource.getJSONObject("practitioner"), individualInfo);

                // Relationship code in extension
                if (relationshipCode != null && !relationshipCode.isEmpty()) {
                        JSONArray extensions = resource.getJSONArray("extension");
                        JSONObject relationshipExt = findEntry(extensions, "url", RELATIONSHIP_TYPE_EXTENSION_URL);
                        relationshipExt
                                .getJSONObject("valueCodeableConcept")
                                .getJSONArray("coding")
                                .getJSONObject(0)
                                .put("code", relationshipCode);
                }

                // Optional end reason code override
                applyEndReasonCode(resource, endReasonCode);
                
                return entry;
        }

        /**
         * Updates practitioner demographics extensions (birthplace, death date) in the extension array.
         * Finds and updates existing extension entries for birthplace and death date rather than creating new ones.
         * 
         * @param extensionJson the practitioner extension array to update
         * @param birthCountry ISO country code for birth country (nullable)
         * @param birthProvince province/state code for birth province (nullable)
         * @param deathDate death date string in ISO format (nullable)
         */
        public static void updateDemographicsExtensions(
                JSONArray extensionJson, 
                String birthCountry, 
                String birthProvince, 
                String deathDate)
        {
                for (int i = 0; i < extensionJson.length(); i++)
                {
                        JSONObject ext = extensionJson.getJSONObject(i);
                        String url = ext.optString("url", "");
                        
                        // Update birthplace extension if birthCountry or birthProvince provided
                        if (url.equals(PRACTITIONER_BIRTHPLACE_URL))
                        {
                                if (birthCountry != null || birthProvince != null)
                                {
                                        JSONObject valueAddress = ext.getJSONObject("valueAddress");
                                        if (birthProvince != null)
                                        {
                                                valueAddress.put("state", birthProvince);
                                        }
                                        if (birthCountry != null)
                                        {
                                                valueAddress.put("country", birthCountry);
                                        }
                                }
                        }
                        // Update death date extension if deathDate provided
                        else if (url.equals(PRACTITIONER_DEATHDATE_URL))
                        {
                                if (deathDate != null)
                                {
                                        ext.put("valueDateTime", deathDate);
                                }
                        }
                }
        }
}
