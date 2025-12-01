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
public class MaintainOrgBuilder
{
    private List<Map<String,String>>  addressList_     = new ArrayList<>();
    private String                    alias_           = null;
    private Boolean                   confidentiality_ = null;
    private String                    identifier_      = null;
    private String                    name_            = null;
    private  List<Map<String,String>> noteList_        = new ArrayList<>();
    private String                    roleType_        = "ORG";
    private List<Map<String,String>>  statusList_      = new ArrayList<>();
    private List<Map<String,String>>  telecomList_     = new ArrayList<>();

    /**
     * TODO (AZ) - doc
     */
    public MaintainOrgBuilder()
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
    public MaintainOrgBuilder addAddress(
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
        addressList_.add(addressInfo);
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
    public MaintainOrgBuilder addNote(String text)
    {
        Map<String,String> info = new HashMap<>();
        info.put("text", text);
        noteList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param statusClass
     *        ???
     *        AE, LIC
     *
     * @param status
     *        ???
     *
     * @param statusReason
     *        ???
     *
     * @return ???
     */
    public MaintainOrgBuilder addStatus(String statusClass, String status, String statusReason)
    {
        Map<String,String> statusInfo = new HashMap<>();
        statusInfo.put("status",       status);
        statusInfo.put("statusClass",  statusClass);
        statusInfo.put("statusReason", statusReason);
        statusList_.add(statusInfo);
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
    public MaintainOrgBuilder addTelecom(String type, String purpose, String value)
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
     * @param alias
     *        ???
     *
     * @return ???
     */
    public MaintainOrgBuilder alias(String alias)
    {
        alias_ = alias;
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
                MethodHandles.lookup().lookupClass(), "maintain-organization.json");
        JSONObject json = new JSONObject(template);

        MaintainProviderAccessor accessor = new MaintainProviderAccessor(json);
        JSONObject orgJson = accessor.getOrgJson();
        orgJson.getJSONObject("type")
               .getJSONArray("coding")
               .getJSONObject(0)
               .put("code", roleType_);
        accessor.getOrgIdentifierJson(0).put("value", identifier_);
        orgJson.put("name", name_);
        if (alias_ != null)
        {
            orgJson.getJSONArray("alias").put(0, alias_);
        }

        JSONArray addressesJson = orgJson.getJSONArray("address");
        for (Map<String,String> info : addressList_)
        {
            addressesJson.put(MaintainUtils.createAddress(info));
        }

        JSONArray telecomJson = orgJson.getJSONArray("telecom");
        for (Map<String,String> info : telecomList_)
        {
            telecomJson.put(MaintainUtils.createTelecom(info));
        }

        JSONArray extensionJson = accessor.getOrgExtensionJson();
        if (statusList_.isEmpty())
        {
            extensionJson.put(MaintainUtils.createStatus(Map.of(
                    "statusClass",  "LIC",
                    "status",       "ACTIVE",
                    "statusReason", "GS")));
        }
        else
        {
            for (Map<String,String> info : statusList_)
            {
                extensionJson.put(MaintainUtils.createStatus(info));
            }
        }
        if (confidentiality_ != null)
        {
            extensionJson.put(MaintainUtils.createConfidentiality(confidentiality_));
        }
        for (Map<String,String> info : noteList_)
        {
           extensionJson.put(MaintainUtils.createNote(info));
        }

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
    public MaintainOrgBuilder confidentiality(boolean confidentiality)
    {
        confidentiality_ = confidentiality;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param identifier
     *        ???
     *
     * @return ???
     */
    public MaintainOrgBuilder identifier(String identifier)
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
    public MaintainOrgBuilder name(String name)
    {
        name_ = name;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param roleType
     *        ???
     *        BUSINESS, CLINIC, ORG
     *
     * @return ???
     */
    public MaintainOrgBuilder roleType(String roleType)
    {
        roleType_ = roleType;
        return this;
    }

    private void verifyParameters()
    {
        requireNonNull(identifier_, "Missing organization identifier.");
        requireNonNull(name_,       "Missing organization name.");
    }
}
