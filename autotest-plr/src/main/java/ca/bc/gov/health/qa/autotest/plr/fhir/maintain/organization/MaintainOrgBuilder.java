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
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.EndReasonCode;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.PlrFhirResourceType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.PractitionerRelationshipCode;
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
    private List<Map<String,String>>   facilityRelationshipList_ = new ArrayList<>();
    private List<Map<String,String>>   organizationRelationshipList_ = new ArrayList<>();
    private List<Map<String,String>>   individualRelationshipList_ = new ArrayList<>();
    // Modifier that will be used on build to determine the end reason code CEASE instead of template default CHG.
    private boolean                    ceaseRelationships_ = false;

    // Organization status rules (single source of truth)
    public static final List<String> STATUS_CLASSES_ORDER = List.of("LIC", "AE");
    public static final int MAX_STATUS_COUNT = STATUS_CLASSES_ORDER.size();

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

        // Cap to a maximum number of unique classes
        if (statusList_.size() > MAX_STATUS_COUNT) {
            statusList_ = new ArrayList<>(statusList_.subList(0, MAX_STATUS_COUNT));
        }
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
     
       if(roleType_ != null ){
            orgJson.getJSONObject("type")
            .getJSONArray("coding")
            .getJSONObject(0)
            .put("code", roleType_.toString());
        }

        // Include specialized _type block for HDS role type.
        if (roleType_ != null && roleType_ == OrgRoleType.HDS) {
            requireNonNull(hdsType_, "HDS type required when roleType is HDS");
            orgJson.put("_type", MaintainUtils.createHdsType(hdsType_));
        }

        // Pick the first available identifier deterministically
        IdentifierType firstIdentifierType = null;
        String firstIdentifierValue = null;
        for (IdentifierType t : IdentifierType.values()) {
            String v = identifiers_.get(t);
            if (v != null && !v.isBlank()) {
                firstIdentifierType = t;
                firstIdentifierValue = v;
                break;
            }
        }
        
        if(firstIdentifierType != null && firstIdentifierValue != null) {
            JSONObject orgIdentifierJson = accessor.getOrgIdentifierJson(0);
            orgIdentifierJson.put("system", firstIdentifierType.getSourceSystem());
            orgIdentifierJson.put("value", firstIdentifierValue);
        }


        if (name_ != null){
            orgJson.put("name", name_);
        }

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
        for (Map<String,String> info : statusList_)
        {
            extensionJson.put(MaintainUtils.createStatus(info));
        }
        
        if (confidentiality_ != null)
        {
            extensionJson.put(MaintainUtils.createConfidentiality(confidentiality_));
        }
        for (Map<String,String> info : noteList_)
        {
           extensionJson.put(MaintainUtils.createNote(info));
        }

        // OrganizationProperties mapping (if provided) and if not ceasing relationships)
        if (orgProperties_ != null && !ceaseRelationships_)
        {
            // Clinic hours of operation (availableTime blocks) — same level as note blocks
            for (String hours : orgProperties_.getClinicHoursOfOperation())
            {
                extensionJson.put(MaintainUtils.createClinicAvailability(hours));
            }
            if (orgProperties_.getClinicType() != null)
            {
                mergePrimaryCareWrapper(extensionJson, MaintainUtils.createClinicType(orgProperties_.getClinicType().getText()));
            }
            if (orgProperties_.getClinicOwnerBusinessType() != null)
            {
                mergePrimaryCareWrapper(extensionJson, MaintainUtils.createClinicOwnerBusinessType(orgProperties_.getClinicOwnerBusinessType().getText()));
            }
            if (orgProperties_.getClinicServices() != null)
            {
                mergePrimaryCareWrapper(extensionJson, MaintainUtils.createClinicServices(orgProperties_.getClinicServices().getText()));
            }
            if (orgProperties_.getClinicLegalBusinessName() != null && !orgProperties_.getClinicLegalBusinessName().isBlank())
            {
                mergePrimaryCareWrapper(extensionJson, MaintainUtils.createClinicLegalBusinessName(orgProperties_.getClinicLegalBusinessName()));
            }
            if (orgProperties_.getPciFlag() != null)
            {
                mergePrimaryCareWrapper(extensionJson, MaintainUtils.createPciFlag(orgProperties_.getPciFlag()));
            }
            for (String ownerName : orgProperties_.getClinicOwnerNames())
            {
                mergePrimaryCareWrapper(extensionJson, MaintainUtils.createClinicOwnerName(ownerName));
            }
            for (String payee : orgProperties_.getPayeeNumber())
            {
                extensionJson.put(MaintainUtils.createClinicPayeeNumber(payee));
            }
        }

        // For each facility relationship create a distinct OrganizationAffiliation bundle entry
        JSONArray bundleEntryArray = accessor.getEntryArrayJson();
        
        // Create organization info map for the OrganizationAffiliation
        if (firstIdentifierValue != null && firstIdentifierType != null) {
            Map<String,String> orgInfo = new HashMap<>();
            orgInfo.put("type", firstIdentifierType.getSourceSystem());
            orgInfo.put("identifier", firstIdentifierValue);
            
            // For each facility relationship create a distinct OrganizationAffiliation bundle entry
            
            for (Map<String,String> facilityInfo : facilityRelationshipList_) 
            {
                String facilityIdentifier = facilityInfo.get("identifier");
                if (ceaseRelationships_) {
                    bundleEntryArray.put(MaintainUtils.createFacilityOrgAffiliation(orgInfo, facilityIdentifier, EndReasonCode.CEASE));
                } else {
                    bundleEntryArray.put(MaintainUtils.createFacilityOrgAffiliation(orgInfo, facilityIdentifier));
                }
            }
            
            // For each organization relationship create a distinct OrganizationAffiliation bundle entry
            for (Map<String,String> relatedOrgInfo : organizationRelationshipList_)
            {
                String relationshipCode = relatedOrgInfo.get("code");
                if (ceaseRelationships_) {
                    bundleEntryArray.put(MaintainUtils.createOrganizationOrgAffiliation(orgInfo, relatedOrgInfo, relationshipCode, EndReasonCode.CEASE));
                } else {
                    bundleEntryArray.put(MaintainUtils.createOrganizationOrgAffiliation(orgInfo, relatedOrgInfo, relationshipCode));
                }
            }

            // For each individual relationship create a distinct PractitionerAffiliation bundle entry
            for (Map<String,String> relatedIndividualInfo : individualRelationshipList_)
            {
                String relationshipCode = relatedIndividualInfo.get("code");
                if (ceaseRelationships_) {
                    bundleEntryArray.put(MaintainUtils.createOrganizationIndividualAffiliation(orgInfo, relatedIndividualInfo, relationshipCode, EndReasonCode.CEASE));
                } else {
                    bundleEntryArray.put(MaintainUtils.createOrganizationIndividualAffiliation(orgInfo, relatedIndividualInfo, relationshipCode));
                }
            }
        }

        return json;
    }

    /**
     * Ensures Organization property extensions that are nested under the primary care wrapper are consolidated
     * into a single wrapper entry (matching the expected wire format). If a primary-care wrapper already exists
     * in the organization extension array, the child extensions from the provided wrapper are appended to it;
     * otherwise the provided wrapper is added.
     *
     * @param orgExtensions organization-level extension array to update
     * @param newWrapper a newly created wrapper JSON from MaintainUtils (url should be the primary-care URL)
     */
    private static void mergePrimaryCareWrapper(JSONArray orgExtensions, JSONObject newWrapper) {
        if (newWrapper == null) return;
        String url = newWrapper.optString("url", "");
        // Only merge wrappers (skip non-wrapper extensions like payee number)
        if (!url.contains("bc-organization-primary-care-clinic-extension")) {
            orgExtensions.put(newWrapper);
            return;
        }

        // Find existing wrapper
        JSONObject existing = null;
        for (int i = 0; i < orgExtensions.length(); i++) {
            JSONObject candidate = orgExtensions.getJSONObject(i);
            if (url.equals(candidate.optString("url", ""))) {
                existing = candidate;
                break;
            }
        }

        if (existing == null) {
            orgExtensions.put(newWrapper);
            return;
        }

        // Append child extensions from newWrapper into existing wrapper's extension array
        JSONArray existingChildren = existing.optJSONArray("extension");
        if (existingChildren == null) {
            existingChildren = new JSONArray();
            existing.put("extension", existingChildren);
        }
        JSONArray newChildren = newWrapper.optJSONArray("extension");
        if (newChildren != null) {
            for (int j = 0; j < newChildren.length(); j++) {
                existingChildren.put(newChildren.getJSONObject(j));
            }
        }
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
     * Adds a facility relationship to the organization.
     * @param identifierType type of identifier used to reference the facility
     * @param identifier identifier value of the facility
     * @param name facility name
     * @return this builder for fluent chaining
     */
    public MaintainOrgBuilder addFacilityRelationship(
            IdentifierType identifierType,
            String identifier, String name)
    {
        Map<String,String> facilityRelationship = new HashMap<>();
        facilityRelationship.put("type",       identifierType.getSourceSystem());
        facilityRelationship.put("identifier", identifier);
        facilityRelationship.put("name",       name);
        this.facilityRelationshipList_.add(facilityRelationship);
        return this;
    }

    /**
     * Adds an organization relationship to this organization.
     * @param identifierType type of identifier used to reference the related organization
     * @param identifier identifier value of the related organization
     * @param relationshipCode relationship type code (e.g., P2P, O2F)
     * @return this builder for fluent chaining
     */
    public MaintainOrgBuilder addOrganizationRelationship(
            IdentifierType identifierType,
            String identifier,
            PractitionerRelationshipCode relationshipCode)
    {
        Map<String,String> orgRelationship = new HashMap<>();
        orgRelationship.put("type",       identifierType.getSourceSystem());
        orgRelationship.put("identifier", identifier);
        orgRelationship.put("code",       relationshipCode.getCode());
        this.organizationRelationshipList_.add(orgRelationship);
        return this;
    }

    /**
     * Adds an individual (practitioner) relationship to this organization.
     * @param identifierType type of identifier used to reference the related individual
     * @param identifier identifier value of the related individual
     * @param relationshipCode relationship type code
     * @return this builder for fluent chaining
     */
    public MaintainOrgBuilder addIndividualRelationship(
            IdentifierType identifierType,
            String identifier,
            PractitionerRelationshipCode relationshipCode)
    {
        Map<String,String> individualRelationship = new HashMap<>();
        individualRelationship.put("type",       identifierType.getSourceSystem());
        individualRelationship.put("identifier", identifier);
        individualRelationship.put("code",       relationshipCode.getCode());
        this.individualRelationshipList_.add(individualRelationship);
        return this;
    }

    /**
     * Marks all relationships to be ceased during maintain submission by overriding the
     * end reason extension code from CHG to CEASE.
     * @return this builder for fluent chaining
     */
    public MaintainOrgBuilder ceaseRelationships() {
        this.ceaseRelationships_ = true;
        return this;
    }

    /**
     * Sets the specific HDS type classification for the organization (only meaningful when role type is HDS).
     * @param hdsType classification string
     * @return this builder
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
            case FACILITY_RELATIONSHIPS:
                if (facilityRelationshipList_.isEmpty()) {
                    requireNonNull(null, "At least one facility relationship is required.");
                }
                break;
            case ORGANIZATION_RELATIONSHIPS:
                if (organizationRelationshipList_.isEmpty()) {
                    requireNonNull(null, "At least one organization relationship is required.");
                }
                break;
            case INDIVIDUAL_RELATIONSHIPS:
                if (individualRelationshipList_.isEmpty()) {
                    requireNonNull(null, "At least one individual relationship is required.");
                }
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
     * @return ORGID identifier string or null if not present
     */
    @Deprecated
    public String getOrgIdentifier() {
        return identifiers_.get(IdentifierType.ORGID);
    }

    /**
     * Returns an immutable snapshot of all identifiers.
     * @return map copy of identifier values keyed by type
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
     * Facility relationship entries accumulated.
     * @return immutable list of facility relationship maps
     */
    public List<Map<String,String>> getFacilityRelationshipList() { return List.copyOf(facilityRelationshipList_); }

    /**
     * Organization relationship entries accumulated.
     * @return immutable list of organization relationship maps
     */
    public List<Map<String,String>> getOrganizationRelationshipList() { return List.copyOf(organizationRelationshipList_); }

    /**
     * Individual relationship entries accumulated.
     * @return immutable list of individual relationship maps
     */
    public List<Map<String,String>> getIndividualRelationshipList() { return List.copyOf(individualRelationshipList_); }

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

    /**
     * Creates a copy of this builder without any relationship lists (facility, P2P, O2F).
     * Useful for creating fresh maintain requests that inherit attributes but drop
     * relationships unless explicitly requested again.
     *
     * @return new builder instance without any relationships
     */
    public MaintainOrgBuilder copyWithoutRelationships() {
        MaintainOrgBuilder copy = new MaintainOrgBuilder();
        copy.name_ = this.name_;
        copy.alias_ = this.alias_;
        copy.confidentiality_ = this.confidentiality_;
        copy.roleType_ = this.roleType_;
        copy.hdsType_ = this.hdsType_;
        copy.orgProperties_ = this.orgProperties_;
        
        // Deep copy identifiers
        copy.identifiers_ = new HashMap<>(this.identifiers_);
        
        // Deep copy address list
        for (Map<String,String> address : this.addressList_) {
            copy.addressList_.add(new HashMap<>(address));
        }
        
        // Deep copy telecom list
        for (Map<String,String> telecom : this.telecomList_) {
            copy.telecomList_.add(new HashMap<>(telecom));
        }
        
        // Deep copy status list
        for (Map<String,String> status : this.statusList_) {
            copy.statusList_.add(new HashMap<>(status));
        }
        
        // Deep copy notes
        for (Map<String,String> note : this.noteList_) {
            copy.noteList_.add(new HashMap<>(note));
        }
        
        // facilityRelationshipList_ intentionally left empty
        // Future P2P and O2F relationship lists also left empty
        copy.ceaseRelationships_ = false; // explicit
        
        return copy;
    }
    
}
