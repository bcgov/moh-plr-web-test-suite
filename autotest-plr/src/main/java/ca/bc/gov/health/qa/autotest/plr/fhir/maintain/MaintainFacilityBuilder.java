package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

import ca.bc.gov.health.qa.autotest.plr.fhir.model.EndReasonCode;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.io.ResourceUtils;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityAttribute;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.PlrFhirResourceType;

/**
 * Builder for Facility  maintain requests.
 * Provides fluent methods for setting single-value fields and accumulating multi-value
 * collections (telecoms, notes, organization relationships). The {@link #build()} method
 * materializes a JSON payload using a template resource and the configured state.
 */
public class MaintainFacilityBuilder implements MaintainRequestBuilder
{
    private String                    identifier_           = null;
    private Map<String,String>        address_              = new HashMap<>();
    private Map<String,String>        position_             = new HashMap<>();
    private Map<String,String>        hsda_                 = new HashMap<>();
    private String                    name_                 = null;
    private String                    description_          = null;
    private List<Map<String,String>>  telecomList_          = new ArrayList<>();
    private List<Map<String,String>>  noteList_             = new ArrayList<>();
    private List<Map<String,String>>  orgRelationshipList_  = new ArrayList<>();
    private final String              PURPOSE               = "FC";
    private final String              ADDRESS_TYPE_PHYS     = "physical";
    private String                    date                  = null;

    // Modifier that will be used on build to determine the end reason code CEASE instead of template default CHG.
    private boolean                   ceaseRelationships_   = false;

    /**
     * Creates an empty Facility builder. Required field validation occurs during {@link #build()}.
     */
    public MaintainFacilityBuilder()
    {}

    /**
     * Adds (or replaces) the single facility address used in the maintain payload. The builder keeps only
     * one address instance; invoking this again overwrites the previous address.
     * <p>
     * Address type is fixed to {@code physical} and purpose to {@code FC}
     *
     * @param line1      first address line (street / civic)
     * @param city       city name
     * @param postalCode postal code (format as accepted by upstream service)
     * @return this builder for fluent chaining
     */
    public MaintainFacilityBuilder addAddress(
            String line1,
            String city,
            String postalCode)
    {
        Map<String,String> addressInfo = new HashMap<>();
        addressInfo.put("type",       ADDRESS_TYPE_PHYS);
        addressInfo.put("purpose",    PURPOSE);
        addressInfo.put("line1",      line1);
        addressInfo.put("city",       city);
        addressInfo.put("postalCode", postalCode);
        this.address_ = addressInfo;
        return this;
    }

    /**
     * Adds (or replaces) the position info from the FHIR response.
     * (Not used in maintain payload - auxiliary information related to the address)
     * This builder only keeps one position instance; invoking this again overwrites the previous position info.
     *
     * @param latitude      the civic address latitude
     * @param longitude     the civic address longitude
     * @return              this builder for fluent chaining
     */
    public MaintainFacilityBuilder addPosition(String latitude, String longitude)
    {
        Map<String,String> positionInfo = Map.of("latitude", latitude, "longitude", longitude);
        this.position_ = positionInfo;
        return this;
    }

    /**
     * Adds (or replaces) the HSDA info from the FHIR response.
     * (Not used in maintain payload - auxiliary information related to the address)
     * This builder only keeps one HSDA instance; invoking this again overwrites the previous HSDA info.
     *
     * @param chsa      community health service area
     * @param pcn       primary care network
     * @param hsda      health service delivery area
     * @param lha       local health area
     * @param ha        health authority
     * @return          this builder for fluent chaining
     */
    public MaintainFacilityBuilder addHSDA(
        String chsa, String pcn, String hsda, String lha, String ha
    )
    {
        Map<String,String> hsdaInfo = new HashMap<>();
        hsdaInfo.put("CHSA", chsa);
        hsdaInfo.put("PCN", pcn);
        hsdaInfo.put("HSDA", hsda);
        hsdaInfo.put("LHA", lha);
        hsdaInfo.put("HA", ha);
        this.hsda_ = hsdaInfo;
        return this;
    }

    /**
     * Adds a free-form note that will be rendered as an extension element in the outgoing FHIR payload.
     *
     * @param text human readable note text (ignored if null)
     * @return this builder for fluent chaining
     */
    public MaintainFacilityBuilder addNote(String text)
    {
        Map<String,String> info = new HashMap<>();
        info.put("text", text);
        noteList_.add(info);
        return this;
    }

    /**
     * Adds a telecom (contact) channel for the facility. Purpose is always set to {@code FC}.
     *
     * @param type  channel type (e.g. phone, fax, email, url, sms)
     * @param value channel value (e.g. number or address)
     * @return this builder for fluent chaining
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
     * Sets a description / alias for the facility.
     *
     * @param description alias / secondary display name (null to omit)
     * @return this builder for fluent chaining
     */
    public MaintainFacilityBuilder description(String description)
    {
        description_ = description;
        return this;
    }

    /**
     * Builds the FHIR maintain JSON object for the configured facility attributes.
     *
     * @return immutable JSON representation ready for submission
     * @throws NullPointerException if required parameters are missing (see {@link #verifyParameters()})
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


        // For each organization relationship create a distinct OrganizationAffiliation bundle entry
        JSONArray bundleEntryArray = accessor.getEntryArrayJson();
        for (Map<String,String> info : orgRelationshipList_) 
        {
            if (ceaseRelationships_) {
                bundleEntryArray.put(MaintainUtils.createFacilityOrgAffiliation(info, identifier_, EndReasonCode.CEASE));
            } else {
                bundleEntryArray.put(MaintainUtils.createFacilityOrgAffiliation(info, identifier_));
            }
        }

        date = LocalDate.now().toString();

        return json;
    }

    @Override
    public PlrFhirResourceType resourceType() {
        return PlrFhirResourceType.FACILITY;
    }

    /**
     * Sets the facility identifier.
     *
     * @param identifier identifier value
     * @return this builder for fluent chaining
     */
    public MaintainFacilityBuilder identifier(String identifier)
    {
        identifier_ = identifier;
        return this;
    }

    /**
     * Sets the facility primary name.
     *
     * @param name facility display name 
     * @return this builder for fluent chaining
     */
    public MaintainFacilityBuilder name(String name)
    {
        name_ = name;
        return this;
    }

    /**
     * Adds an organization relationship to the facility.
     * @param identifierType type of identifier used to reference the organization
     * @param identifier identifier value of the organization
     * @return this builder for fluent chaining
     */
    public MaintainFacilityBuilder addOrganizationRelationship(
            IdentifierType identifierType,
            String identifier, String name)
    {
        Map<String,String> orgRelationship = new HashMap<>();
        orgRelationship.put("type",       identifierType.getSourceSystem());
        orgRelationship.put("identifier", identifier);
        orgRelationship.put("name",       name);
        this.orgRelationshipList_.add(orgRelationship);
        return this;
    }

    /**
     * Marks all organization relationships to be ceased during maintain submission by overriding the
     * end reason extension code from CHG to CEASE.
     * @return this builder for fluent chaining
     */
    public MaintainFacilityBuilder ceaseOrganizationRelationships() {
        this.ceaseRelationships_ = true;
        return this;
    }

    /**
     * Validates all fields marked required in {@link FacilityAttribute}. Throws an NPE with
     * an explanatory message if a required value is absent.
     * @throws NullPointerException if any required field is missing or invalid
     */
    private void verifyParameters()
    {
        // Programmatically validate all required fields based on MaintainFacilityFields enum
        for (FacilityAttribute field : FacilityAttribute.values()) {
            if (field.isRequired()) {
                validateRequiredField(field);
            }
        }
    }
    
    /**
     * Field-specific required validation logic invoked for each required enum constant.
     * @param field required field to validate
     * @throws NullPointerException if validation fails
     */
    private void validateRequiredField(FacilityAttribute field) {
        switch (field) {
            case NAME:
                requireNonNull(name_, "Missing facility name.");
                break;
            case IDENTIFIER:
                requireNonNull(identifier_, "Missing facility identifier.");
                break;
            case ADDRESS:
                if (address_.isEmpty()) {
                    requireNonNull(null, "Missing facility address.");
                }
                break;
            case NOTE:
                if (noteList_.isEmpty()) {
                    requireNonNull(null, "At least one facility note is required.");
                }
                break;
            case ORG_RELATIONSHIP:
                if (orgRelationshipList_.isEmpty()) {
                    requireNonNull(null, "At least one organization relationship is required.");
                }
                break;
            case DESCRIPTION:
                requireNonNull(description_, "Missing facility description.");
                break;
            case PHONE:
                validateRequiredTelecomType("phone");
                break;
            case MOBILE:
                validateRequiredTelecomType("mobile");
                break;
            case PAGER:
                validateRequiredTelecomType("pager");
                break;
            case MODEM:
                validateRequiredTelecomType("modem");
                break;
            case EMAIL:
                validateRequiredTelecomType("email");
                break;
            case FAX:
                validateRequiredTelecomType("fax");
                break;
            case WEBSITE:
                validateRequiredTelecomType("url");
                break;
            case FTP:
                validateRequiredTelecomType("ftp");
                break;
            default:
                // Field is optional and has no required validation rules.
                break;
        }
    }
    
    /**
     * Ensures at least one telecom entry of the provided type exists when that type is required.
     * @param type telecom type string (e.g. phone, email, fax, url)
     * @throws NullPointerException if not found
     */
    private void validateRequiredTelecomType(String type) {
        boolean found = telecomList_.stream()
            .anyMatch(telecom -> type.equals(telecom.get("type")));
        
        if (!found) {
            requireNonNull(null, "Missing required telecom type: " + type);
        }
    }

    // --- Getters (added for external inspection / assertions) ---

    /**
     * Identifier currently set on this builder.
     * @return identifier value or null if not assigned yet
     */
    public String getIdentifier()
    {
        return identifier_;
    }

    /**
     * Facility name currently configured.
     * @return name value or null if not provided
     */
    public String getName()
    {
        return name_;
    }

    /**
     * Returns the description / alias (null if none provided).
     * @return description value or null if not provided
     */
    public String getDescription()
    {
        return description_;
    }

    /**
     * Returns the current date upon being built
     * (hopefully close to the date of facility creation in all scenarios)
     * @return a string of the date value or null if not assigned yet
     */
    public String getDate() { return date; }

    /**
     * Returns a defensive copy of the single address map (empty if not set).
     * @return defensive copy of address (empty map if unset)
     */
    public Map<String,String> getAddress()
    {
        return new HashMap<>(address_);
    }

    /**
     * Returns a defensive copy of the position map (empty if not set).
     * @return defensive copy of position (empty map if unset)
     */
    public Map<String,String> getPosition() { return new HashMap<>(position_); }

    /**
     * Returns a defensive copy of the HSDA map (empty if not set).
     * @return defensive copy of HSDA (empty map if unset)
     */
    public Map<String,String> getHsda()
    {
        return new HashMap<>(hsda_);
    }

    /**
     * Returns an immutable snapshot of telecom entries added so far.
     * @return immutable list of telecom maps
     */
    public List<Map<String,String>> getTelecomList()
    {
        return List.copyOf(telecomList_);
    }

    /**
     * Returns an immutable snapshot of note entries added so far.
     * @return immutable list of note maps
     */
    public List<Map<String,String>> getNoteList()
    {
        return List.copyOf(noteList_);
    }

    /**
     * Returns an immutable snapshot of organization relationship entries added so far.
     * @return immutable list of organization relationship maps
     */
    public List<Map<String,String>> getOrgRelationshipList()
    {
        return List.copyOf(orgRelationshipList_);
    }

    /**
     * Creates a copy of this builder containing all attributes
     * but EXCLUDING any organization relationships. The returned builder
     * has the cease flag cleared regardless of the current builder state so future builds will not implicitly cease
     * relationships unless explicitly requested again.
     *
     * @return new builder instance without organization relationships
     */
    public MaintainFacilityBuilder copyWithoutOrgRelationships() {
        MaintainFacilityBuilder copy = new MaintainFacilityBuilder();
        copy.identifier_ = this.identifier_;
        copy.name_ = this.name_;
        copy.description_ = this.description_;
        copy.address_ = new HashMap<>(this.address_);
        copy.hsda_ = new HashMap<>(this.hsda_);
        // Deep copy telecom list
        for (Map<String,String> telecom : this.telecomList_) {
            copy.telecomList_.add(new HashMap<>(telecom));
        }
        // Deep copy notes
        for (Map<String,String> note : this.noteList_) {
            copy.noteList_.add(new HashMap<>(note));
        }
        // orgRelationshipList_ intentionally left empty
        copy.ceaseRelationships_ = false; // explicit
        return copy;
    }
}
