package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.plr.fhir.data.organization.OrganizationAttribute;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.MaintainAccessor;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.MaintainUtils;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.PlrFhirResourceType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.HdsType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrganizationProperties;

/**
 * Builder for Organization maintain requests. Supports configuration of multi-valued
 * addresses, telecoms, statuses and notes along with scalar attributes (identifier,
 * name, alias, role type, confidentiality). The {@link #build()} method materializes
 * a maintain Bundle JSON using a template resource.
 */
public class MaintainOrgBuilder implements MaintainRequestBuilder
{
    private List<Map<String,String>>   addressList_     = new ArrayList<>();
    private String                     alias_           = null;
    private Boolean                    confidentiality_ = null;
    private Map<IdentifierType,String> identifiers_    = new HashMap<>();
    private String                     name_            = null;
    private List<Map<String,String>>   noteList_        = new ArrayList<>();
    private OrgRoleType                roleType_        = null; // must be explicitly set
    private List<Map<String,String>>   statusList_      = new ArrayList<>();
    private List<Map<String,String>>   telecomList_     = new ArrayList<>();
    private HdsType                    hdsType_         = null;
    private OrganizationProperties     orgProperties_   = null;
    //TODO: O2I relationships
    //TODO: 02F relationships


    /**
     * Constructs an empty Organization builder. Required field validation occurs during {@link #build()}.
     */
    public MaintainOrgBuilder()
    {}

    /**
     * Adds an address to the organization.
     * @param type address type (e.g. physical, postal)
     * @param purpose usage purpose code (BC, CC, DC, EC, FC, HC, MC, OC)
     * @param line1 first address line
     * @param city city name
     * @param postalCode postal code
     * @return this builder for fluent chaining
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
     * Adds a free-form note for the organization.
     * @param text note text
     * @return this builder
     */
    public MaintainOrgBuilder addNote(String text)
    {
        Map<String,String> info = new HashMap<>();
        info.put("text", text);
        noteList_.add(info);
        return this;
    }

    /**
     * Adds a status entry for the organization.
     * @param statusClass status classification (e.g. AE, LIC)
     * @param status status value (e.g. ACTIVE)
     * @param statusReason status reason code (e.g. GS)
     * @return this builder
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
     * Adds a telecom contact channel for the organization.
     * @param type channel type (email, fax, other, pager, phone, sms, url)
     * @param purpose usage purpose code (BC, CC, DC, FC, HC, MC, OC)
     * @param value channel value (number, address, URL, etc.)
     * @return this builder
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
     * Sets the organization alias.
     * @param alias alternative display name
     * @return this builder
     */
    public MaintainOrgBuilder alias(String alias)
    {
        alias_ = alias;
        return this;
    }

    /**
     * Builds the maintain JSON Bundle reflecting configured organization attributes.
     * Applies defaults for certain required elements when not explicitly set (e.g. ACTIVE status).
     * When the role type resolves to HDS, an additional _type block is injected
     * @return immutable JSON representation ready for submission
     */
    public JSONObject build()
    {
        verifyParameters();
        JSONObject json = MaintainUtils.readJsonTemplate("maintain-organization.json");

        MaintainAccessor accessor = new MaintainAccessor(json);
        JSONObject orgJson = accessor.getOrgJson();
     
       if(roleType_ != null ){ orgJson.getJSONObject("type")
            .getJSONArray("coding")
            .getJSONObject(0)
            .put("code", roleType_.toString());
        }
        // Include specialized _type block for HDS role type.
        if (roleType_ == OrgRoleType.HDS) {
            requireNonNull(hdsType_, "HDS type required when roleType is HDS");
            orgJson.put("_type", MaintainUtils.createHdsType(hdsType_));
        }

        String orgid = identifiers_.get(IdentifierType.ORGID);
        accessor.getOrgIdentifierJson(0).put("value", orgid);

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

    @Override
    public PlrFhirResourceType resourceType() {
        return PlrFhirResourceType.ORGANIZATION;
    }

    /**
     * Sets the confidentiality flag extension value.
     * @param confidentiality confidentiality boolean
     * @return this builder
     */
    public MaintainOrgBuilder confidentiality(boolean confidentiality)
    {
        confidentiality_ = confidentiality;
        return this;
    }

    /**
     * Sets an identifier value for a specific identifier type.
     * @param identifierType type of identifier (e.g., IPC, ORGID)
     * @param identifierValue identifier string
     * @return this builder
     */
    public MaintainOrgBuilder addIdentifier(IdentifierType identifierType, String identifierValue)
    {
        if (identifierType != null && identifierValue != null) {
            identifiers_.put(identifierType, identifierValue);
        }
        return this;
    }

    /**
     * Sets the organization name.
     * @param name display name
     * @return this builder
     */
    public MaintainOrgBuilder name(String name)
    {
        name_ = name;
        return this;
    }

    /**
     * Sets variable organization properties for this builder.
     * This allows configuring advanced attributes outside of the standard
     * scalar and list fields (e.g., clinic services, owner type, clinic type).
     * Note: these properties are currently not serialized by {@link #build()} and
     * will be wired into the template mapping in a later step.
     * @param properties container of organization properties (nullable)
     * @return this builder
     */
    public MaintainOrgBuilder organizationProperties(OrganizationProperties properties)
    {
        orgProperties_ = properties;
        return this;
    }

    /**
     * Sets the organization role type. If changed away from HDS any previously assigned HDS subtype
     * is cleared. For HDS role types an explicit {@link #hdsType(HdsType)} must be provided before build.
     * @param roleType role type enum (never null)
     * @return this builder
     */
    public MaintainOrgBuilder roleType(OrgRoleType roleType)
    {
        roleType_ = requireNonNull(roleType);
        if (roleType != OrgRoleType.HDS) {
            hdsType_ = null;
        }
        return this;
    }

    /**
     * Sets the specific HDS type classification for the organization (only meaningful when role type is HDS).
     * @param hdsType classification string
     * @return this builder
     */
    /**
     * Sets the specific HDS type classification for the organization (only meaningful when role type is HDS).
     * @param hdsType HDS subtype enum (never null)
     * @return this builder
     * @throws IllegalStateException if role type is not HDS
     */
    public MaintainOrgBuilder hdsType(HdsType hdsType) {
        if (roleType_ != OrgRoleType.HDS) {
            throw new IllegalStateException("Cannot set HDS subtype when roleType != HDS");
        }
        hdsType_ = requireNonNull(hdsType);
        return this;
    }

    /**
     * Validates all required organization attributes based on {@link OrganizationAttribute} enum flags.
     * This automatically adapts if attribute requiredness changes in the enum.
     * @throws NullPointerException if any required attribute is absent
     */
    private void verifyParameters() {
        for (OrganizationAttribute attr : OrganizationAttribute.values()) {
            if (attr.isRequired()) {
                validateRequiredField(attr);
            }
        }

        if(roleType_ == OrgRoleType.HDS) {
            requireNonNull(hdsType_, "Missing HDS type classification for organization with HDS role type.");
        }
    }

    /**
     * Performs attribute-specific required validation.
     * @param attr required attribute to check
     */
    private void validateRequiredField(OrganizationAttribute attr) {
        switch (attr) {
            case IDENTIFIER:
                if (identifiers_.isEmpty()) {
                    requireNonNull(null, "Missing organization identifier.");
                }
                break;
            case NAME:
                requireNonNull(name_, "Missing organization name.");
                break;
            case ADDRESS:
                if (addressList_.isEmpty()) {
                    requireNonNull(null, "At least one organization address is required.");
                }
                break;
            case TELECOM:
                if (telecomList_.isEmpty()) {
                    requireNonNull(null, "At least one organization telecom is required.");
                }
                break;
            case STATUS:
                if (statusList_.isEmpty()) {
                    requireNonNull(null, "At least one organization status is required.");
                }
                break;
            case NOTE:
                if (noteList_.isEmpty()) {
                    requireNonNull(null, "At least one organization note is required.");
                }
                break;
            case ALIAS:
                requireNonNull(alias_, "Missing organization alias.");
                break;
            case CONFIDENTIALITY:
                requireNonNull(confidentiality_, "Missing organization confidentiality flag.");
                break;
            case ROLE_TYPE:
                requireNonNull(roleType_, "Missing organization role type.");
                break;
            default:
                // no-op for unsupported entries
                break;
        }
    }

    /**
     * Returns the preferred identifier value (IPC if present, else null).
     * Backwards compatibility method use getIdentifier(IdentifierType.IPC) instead
     * @return IPC identifier value.
     */
    @Deprecated
    public String getIdentifier() {
        return getIdentifier(IdentifierType.IPC);
    }

    /**
     * Returns the identifier value for the specified identifier type (or null if not present).
     * @param type identifier type enum
     * @return identifier string or null
     */
    public String getIdentifier(IdentifierType type){
        return identifiers_.get(type);
    }

    /**
     * Returns the ORGID value if present, else null.
     * Backwards compatibility method use getIdentifier(IdentifierType.ORGID) instead
     */
    @Deprecated
    public String getOrgIdentifier() {
        return identifiers_.get(IdentifierType.ORGID);
    }

    /**
     * Returns an immutable snapshot of all identifiers.
     */
    public Map<IdentifierType,String> getIdentifiers() { return Map.copyOf(identifiers_); }

	/**
     * Organization name configured.
     * @return name value or null if not provided
     */
    public String getName() { return name_; }

    /**
     * Role type code (defaults to HDS if not explicitly set).
     * @return role type code string
     */
    public OrgRoleType getRoleType() { return roleType_; }

    /**
     * Alias string.
     * @return alias value or null if not set
     */
    public String getAlias() { return alias_; }

    /**
     * Confidentiality flag extension value.
     * @return confidentiality Boolean or null if not set
     */
    public Boolean getConfidentiality() { return confidentiality_; }

    /**
     * Returns an immutable snapshot of addresses added.
     * @return unmodifiable list of address maps
     */
    public List<Map<String,String>> getAddressList() { return List.copyOf(addressList_); }

    /**
     * Telecom entries accumulated.
     * @return immutable list of telecom maps
     */
    public List<Map<String,String>> getTelecomList() { return List.copyOf(telecomList_); }

    /**
     * Status entries accumulated.
     * @return immutable list of status maps
     */
    public List<Map<String,String>> getStatusList() { return List.copyOf(statusList_); }

    /**
     * Note entries accumulated.
     * @return immutable list of note maps
     */
    public List<Map<String,String>> getNoteList() { return List.copyOf(noteList_); }

    /**
     * Returns the configured HDS type classification (may be null if not set or role type not HDS).
     * @return hds type string or null
     */
    public HdsType getHdsType() { return hdsType_; }

    /**
     * Returns the variable organization properties configured for this builder.
     * @return `OrganizationProperties` instance or null if not set
     */
    public OrganizationProperties getOrganizationProperties() { return orgProperties_; }
    
}
