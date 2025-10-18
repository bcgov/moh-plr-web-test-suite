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
import ca.bc.gov.health.qa.autotest.plr.fhir.data.FacilityAttribute;
import ca.bc.gov.health.qa.autotest.plr.fhir.model.PlrFhirResourceType;

/**
 * TODO (AZ) - doc
 */
public class MaintainFacilityBuilder implements MaintainRequestBuilder
{
    private String                    identifier_      = null;
    private Map<String,String>        address_         = new HashMap<>();
    private String                    name_            = null;
    private String                    description_     = null;
    private List<Map<String,String>>  telecomList_     = new ArrayList<>();
    private  List<Map<String,String>> noteList_        = new ArrayList<>();
    //private List<Map<String,String>>  facilityRelationships  = new ArrayList<>();
    private final String              PURPOSE           = "FC";
    private final String              ADDRESS_TYPE_PHYS = "physical";
    /**
     * TODO (AZ) - doc
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
     * Programmatically validates all required fields based on the MaintainFacilityFields enum.
     * This method automatically adapts when fields are marked as required/optional in the enum,
     * ensuring validation stays in sync with business rules.
     * 
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
     * Validates that a specific required field has been properly set in the builder.
     * 
     * @param field the required field to validate
     * @throws NullPointerException if the required field is missing or invalid
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
     * Validates that at least one telecom entry of the specified type exists.
     * 
     * @param type the telecom type to validate (e.g., "phone", "email", "fax", "url")
     * @throws NullPointerException if no telecom of the specified type is found
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
     * Returns the identifier currently set on this builder (may be null if not yet assigned).
     */
    public String getIdentifier()
    {
        return identifier_;
    }

    /**
     * Returns the facility name currently configured (may be null until provided).
     */
    public String getName()
    {
        return name_;
    }

    /**
     * Returns the optional description / alias (null if none provided).
     */
    public String getDescription()
    {
        return description_;
    }

    /**
     * Returns a defensive copy of the single address map (empty if not set).
     */
    public Map<String,String> getAddress()
    {
        return new HashMap<>(address_);
    }

    /**
     * Returns an immutable snapshot of telecom entries added so far.
     */
    public List<Map<String,String>> getTelecomList()
    {
        return List.copyOf(telecomList_);
    }

    /**
     * Returns an immutable snapshot of note entries added so far.
     */
    public List<Map<String,String>> getNoteList()
    {
        return List.copyOf(noteList_);
    }
}
