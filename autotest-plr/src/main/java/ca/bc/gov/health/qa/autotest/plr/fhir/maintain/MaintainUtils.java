package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.io.ResourceUtils;

/**
 * TODO (AZ) - doc
 */
public class MaintainUtils
{
    private static final String PERIOD_EXTENSION_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-period-extension";
    private static final String SPECIALTY_SOURCE_EXTENSION_URL =
            "http://hlth.gov.bc.ca/fhir/provider/StructureDefinition/bc-specialty-source-extension";

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
        return new JSONObject(ResourceUtils.readResource(
                MethodHandles.lookup().lookupClass(), templateName));
    }
}
