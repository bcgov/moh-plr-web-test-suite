package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * TODO (AZ) - doc
 */
public class MaintainProviderAccessor
{
    /**
     * TODO (AZ) - doc
     */
    protected JSONObject json_;

    /**
     * TODO (AZ) - doc
     *
     * @param json
     *        ???
     */
    public MaintainProviderAccessor(JSONObject json)
    {
        json_ = json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public JSONArray getOrgExtensionJson()
    {
        return getOrgJson().getJSONArray("extension");
    }

    /**
     * TODO (AZ) - doc
     *
     * @param index
     *        ???
     *
     * @return ???
     */
    public JSONObject getOrgIdentifierJson(int index)
    {
        return getOrgJson().getJSONArray("identifier").getJSONObject(0);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public JSONObject getOrgJson()
    {
        return getResourceJson(0, "Organization");
    }


    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public JSONArray getPracExtensionJson()
    {
        return getPracJson().getJSONArray("extension");
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public JSONObject getPracJson()
    {
        return getResourceJson(1, "Practitioner");
    }

    /**
     * TODO (AZ) - doc
     *
     * @param index
     *        ???
     *
     * @return ???
     */
    public JSONObject getPracNameJson(int index)
    {
        return getPracJson().getJSONArray("name").getJSONObject(index);
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public JSONObject getPracRoleJson()
    {
        return getResourceJson(0, "PractitionerRole");
    }

    /**
     * TODO (AZ) - doc
     *
     * @param resourceIndex
     *        ???
     *
     * @param resourceType
     *        ??? (can be {@code null})
     *
     * @return ???
     */
    public JSONObject getResourceJson(int resourceIndex, String resourceType)
    {
        JSONObject maintainJson =
                MaintainUtils.findEntry(json_.getJSONArray("parameter"), "name", "maintain");
        JSONObject json = maintainJson
                .getJSONObject("resource")
                .getJSONArray("entry")
                .getJSONObject(resourceIndex)
                .getJSONObject("resource");
        if (resourceType != null)
        {
            verifyEquals(json.getString("resourceType"), resourceType);
        }
        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param message
     *        ???
     */
    protected static void fail(String message)
    {
        String failureMessage = "Failed to process FHIR message: " + message;
        throw new IllegalStateException(failureMessage);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param actual
     *        ???
     *
     * @param expected
     *        ???
     */
    protected static void verifyEquals(String actual, String expected)
    {
        if (!expected.equals(actual))
        {
            fail(String.format("Invalid value (actual: %s; expected: %s).", actual, expected));
        }
    }
}
