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
    //private List<Map<String,String>>  facilityRelationships  = new ArrayList<>();
    private final String              PURPOSE           = "FC";
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
            //String purpose = "FC",
            String line1,
            String city,
            String postalCode)
    {
        Map<String,String> addressInfo = new HashMap<>();
        addressInfo.put("type",       type);
        addressInfo.put("purpose",    PURPOSE);
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
     *
     * @param value
     *        ???
     *
     * @return ???
     */
    public MaintainFacilityBuilder addTelecom(String type, String value)
    {
        telecomList_.add(Map.of(
                "purpose", PURPOSE,
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

        MaintainAccessor accessor = new MaintainAccessor(json);
        JSONObject facilityJson = accessor.getFacilityJson();

        accessor.getFacilityIdentifierJson(0).put("value", identifier_);
        facilityJson.put("name", name_);
        if (description_ != null)
        {
            facilityJson.getJSONArray("alias").put(0, description_);
        }

        // Populate the Location.extension[0].valueAddress with the built address.
        // The template has extension[0].valueAddress = null, so we replace it.
        JSONObject extension0 = facilityJson.getJSONArray("extension").getJSONObject(0);
        extension0.put("valueAddress", MaintainUtils.createAddress(address_));
        

        JSONArray telecomJson = facilityJson.getJSONArray("telecom");
        for (Map<String,String> info : telecomList_)
        {
            telecomJson.put(MaintainUtils.createTelecom(info));
        }

        JSONArray extensionJson = accessor.getFacilityExtensionJson();
        
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
        //requireNonNull(identifier_, "Missing facility identifier.");
        
        if (address_.isEmpty())
        {
            requireNonNull(null, "Missing facility address.");
            
        }

    }
}
