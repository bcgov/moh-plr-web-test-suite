package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.io.ResourceUtils;

/**
 * TODO (AZ) - doc
 */
public class MaintainFacilityBuilder
{
    private String                    identifier_      = null;
    private Map<String,String>        address_         = new HashMap<>();
    private String                    name_            = null;
    private String                    description_     = null;
    private List<Map<String,String>>  telecomList_     = new ArrayList<>();
    private  List<Map<String,String>> noteList_        = new ArrayList<>();

    /**
     * TODO (AZ) - doc
     */
    public MaintainFacilityBuilder()
    {}

    /**
     * TODO (AZ) - doc
     *
     * @param type
     *        ???
     *        physical, postal
     *
     * @param purpose
     *        ???
     *        BC, CC, DC, EC, FC, HC, MC, OC
     *
     * @param line1
     *        ???
     *
     * @param city
     *        ???
     *
     * @param postalCode
     *        ???
     *
     * @return ???
     */
    public MaintainFacilityBuilder addAddress(
            String type,
            String purpose,
            String line1,
            String city,
            String postalCode)
    {
        Map<String,String> addressInfo = new HashMap<>();
        addressInfo.put("type",       type);
        addressInfo.put("purpose",    purpose);
        addressInfo.put("line1",      line1);
        addressInfo.put("city",       city);
        addressInfo.put("postalCode", postalCode);
        this.address_ = addressInfo;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param text
     *        ???
     *
     * @return ???
     */
    public MaintainFacilityBuilder addNote(String text)
    {
        Map<String,String> info = new HashMap<>();
        info.put("text", text);
        noteList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param type
     *        ???
     *        email (Email)
     *        fax   (Fax)
     *        other (Modem)
     *        pager (Pager)
     *        phone (Telephone)
     *        sms   (Mobile)
     *        url   (HTTP)
     *
     * @param purpose
     *        ???
     *        BC, CC, DC, FC, HC, MC, OC
     *
     * @param value
     *        ???
     *
     * @return ???
     */
    public MaintainFacilityBuilder addTelecom(String type, String purpose, String value)
    {
        telecomList_.add(Map.of(
                "purpose", purpose,
                "type",    type,
                "value",   value));
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param description
     *        ???
     *
     * @return ???
     */
    public MaintainFacilityBuilder description(String description)
    {
        description_ = description;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public JSONObject build()
    {
        verifyParameters();
        String template = ResourceUtils.readResource(
                MethodHandles.lookup().lookupClass(), "maintain-facility.json");
        JSONObject json = new JSONObject(template);

        MaintainProviderAccessor accessor = new MaintainProviderAccessor(json);
        JSONObject orgJson = accessor.getOrgJson();

        accessor.getOrgIdentifierJson(0).put("value", identifier_);
        orgJson.put("name", name_);
        if (description_ != null)
        {
            orgJson.getJSONArray("description").put(0, description_);
        }

        JSONArray addressesJson = orgJson.getJSONArray("address");

        addressesJson.put(MaintainUtils.createAddress(address_));
        

        JSONArray telecomJson = orgJson.getJSONArray("telecom");
        for (Map<String,String> info : telecomList_)
        {
            telecomJson.put(MaintainUtils.createTelecom(info));
        }

        JSONArray extensionJson = accessor.getOrgExtensionJson();
        
        for (Map<String,String> info : noteList_)
        {
           extensionJson.put(MaintainUtils.createNote(info));
        }

        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param identifier
     *        ???
     *
     * @return ???
     */
    public MaintainFacilityBuilder identifier(String identifier)
    {
        identifier_ = identifier;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param name
     *        ???
     *
     * @return ???
     */
    public MaintainFacilityBuilder name(String name)
    {
        name_ = name;
        return this;
    }

    private void verifyParameters()
    {
        requireNonNull(identifier_, "Missing organization identifier.");
        requireNonNull(name_,       "Missing organization name.");
    }
}
