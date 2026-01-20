package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainRequestBuilder;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.MaintainAccessor;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.MaintainUtils;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.PlrFhirResourceType;
import ca.bc.gov.health.qa.autotest.plr.fhir.data.individual.IndividualAttribute;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;

/**
 * Builder for Practitioner maintain requests. Supports configuration of multi-valued
 * addresses, telecoms, expertise, statuses, credentials, notes and
 * scalar attributes (name, role type, confidentiality). The {@link #build()} method
 * materializes a maintain Bundle JSON using a template resource.
 */
public class MaintainIndividualBuilder implements MaintainRequestBuilder
{
    public static final java.util.List<String> STATUS_CLASSES_ORDER = java.util.List.of("LIC", "AE");
    public static final int MAX_STATUS_COUNT = STATUS_CLASSES_ORDER.size();
    private List<Map<String,String>>   addressList_            = new ArrayList<>();
    private List<Map<String,String>>   conditionList_          = new ArrayList<>();
    private Boolean                    confidentiality_        = null;
    private List<Map<String,String>>   credentialList_         = new ArrayList<>();
    private List<Map<String,String>>   disciplinaryActionList_ = new ArrayList<>();
    private String                     familyName_             = null;
    private String[]                   names_                  = null; //Array with format [firstName, middleName, thirdName]
    private Map<IdentifierType,String> identifiers_            = new HashMap<>();
    private Map<String,String>         demographics_           = null; //date of birth, date of death, birth country, birth province, gender 
    private List<Map<String,String>>   expertiseList_          = new ArrayList<>();
    private List<Map<String,String>>   noteList_               = new ArrayList<>();
    private IndividualRoleType         roleType_               = null; // must be explicitly set
    private List<Map<String,String>>   statusList_             = new ArrayList<>();
    private List<Map<String,String>>   telecomList_            = new ArrayList<>();
    //TODO: P2P relationships
    //private String workLocations = null //N/A TO FHIR
    //TODO CHECK IF APPLIES TO FHIR private MAP<String,String> communicationPreferences_         = null;


    /**
     * Constructs an empty Practitioner builder. Required field validation occurs during {@link #build()}.
     */
    public MaintainIndividualBuilder()
    {}

    /**
     * Adds an address to the practitioner.
     * @param type address type (e.g. physical, postal)
     * @param purpose usage purpose code (BC, CC, DC, EC, FC, HC, MC, OC)
     * @param line1 first address line
     * @param city city name
     * @param postalCode postal code
     * @return this builder for fluent chaining
     */
    public MaintainIndividualBuilder addAddress(
            String type,
            String purpose,
            String line1,
            String city,
            String postalCode)
    {
        Map<String,String> info = new HashMap<>();
        info.put("type",       type);
        info.put("purpose",    purpose);
        info.put("line1",      line1);
        info.put("city",       city);
        info.put("postalCode", postalCode);
        addressList_.add(info);
        return this;
    }

    /**
     * Adds a condition/restriction to the practitioner.
     * @param type condition type code
     * @param restriction true if restrictive; false otherwise
     * @param explanation free-text explanation
     * @return this builder
     */
    public MaintainIndividualBuilder addCondition(String type, boolean restriction, String explanation)
    {
        Map<String,String> info = new HashMap<>();
        info.put("type",        type);
        info.put("restriction", "" + restriction);
        info.put("explanation", explanation);
        conditionList_.add(info);
        return this;
    }

    /**
     * Adds a credential/qualification to the practitioner.
     * @param type credential type code
     * @param designation designation/title text
     * @param registrationNumber registration number string
     * @param institution granting institution name
     * @param city institution city
     * @param equivalency true if considered equivalent
     * @param year year obtained
     * @return this builder
     */
    public MaintainIndividualBuilder addCredential(
            String type,
            String designation,
            String registrationNumber,
            String institution,
            String city,
            boolean equivalency,
            String year)
    {
        Map<String,String> info = new HashMap<>();
        info.put("type",               type);
        info.put("designation",        designation);
        info.put("registrationNumber", registrationNumber);
        info.put("institution",        institution);
        info.put("city",               city);
        info.put("equivalency",        "" + equivalency);
        info.put("year",               year);
        credentialList_.add(info);
        return this;
    }

    /**
     * Adds a disciplinary action.
     * @param display whether to display publicly
     * @param description description text
     * @param archiveDate archive date string (YYYY-MM-DD)
     * @return this builder
     */
    public MaintainIndividualBuilder addDisciplinaryAction(
            boolean display, String description, String archiveDate)
    {
        Map<String,String> info = new HashMap<>();
        info.put("display", "" + display);
        info.put("description", description);
        info.put("archiveDate", archiveDate);
        disciplinaryActionList_.add(info);
        return this;
    }

    /**
     * Adds an expertise entry for the practitioner.
     * @param code expertise code
     * @param sourceCode source coding system code
     * @return this builder
     */
    public MaintainIndividualBuilder addExpertise(String code, String sourceCode)
    {
        Map<String,String> info = new HashMap<>();
        info.put("code",       code);
        info.put("sourceCode", sourceCode);
        expertiseList_.add(info);
        return this;
    }

    /**
     * Adds a note.
     * @param text note text
     * @return this builder
     */
    public MaintainIndividualBuilder addNote(String text)
    {
        Map<String,String> info = new HashMap<>();
        info.put("text", text);
        noteList_.add(info);
        return this;
    }


    /**
     * Adds a status entry for the practitioner.
     * @param statusClass status classification (e.g. AE, LIC)
     * @param status status value (e.g. ACTIVE)
     * @param statusReason status reason code (e.g. GS)
     * @return this builder
     */
    public MaintainIndividualBuilder addStatus(String statusClass, String status, String statusReason)
    {
        Map<String,String> info = new HashMap<>();
        info.put("status",       status);
        info.put("statusClass",  statusClass);
        info.put("statusReason", statusReason);
        statusList_.add(info);

        // Mirror MaintainOrgBuilder behavior: cap list size to the supported number of classes.
        if (statusList_.size() > MAX_STATUS_COUNT)
        {
            statusList_ = new ArrayList<>(statusList_.subList(0, MAX_STATUS_COUNT));
        }
        return this;
    }

    /**
     * Adds a telecom contact channel for the practitioner.
     * @param type channel type (email, fax, other, pager, phone, sms, url)
     * @param purpose usage purpose code (BC, CC, DC, FC, HC, MC, OC)
     * @param value channel value (number, address, URL, etc.)
     * @return this builder
     */
    public MaintainIndividualBuilder addTelecom(String type, String purpose, String value)
    {
        telecomList_.add(Map.of(
                "purpose", purpose,
                "type",    type,
                "value",   value));
        return this;
    }

    /**
     * Sets an identifier value for a specific identifier type.
     * @param identifierType type of identifier (e.g., IPC, ORGID)
     * @param identifierValue identifier string
     * @return this builder
     */
    public MaintainIndividualBuilder addIdentifier(IdentifierType identifierType, String identifierValue)
    {
        if (identifierType != null && identifierValue != null) {
            identifiers_.put(identifierType, identifierValue);
        }
        return this;
    }

    /**
     * Builds the maintain JSON Bundle reflecting configured practitioner attributes.
     * @return immutable JSON representation ready for submission
     */
    public JSONObject build()
    {
        verifyParameters();
        JSONObject json = MaintainUtils.readJsonTemplate("maintain-practitioner.json");

        MaintainAccessor accessor = new MaintainAccessor(json);
        JSONObject nameJson     = accessor.getPracNameJson(0);
        JSONObject pracJson     = accessor.getPracJson();
        JSONObject pracRoleJson = accessor.getPracRoleJson();

        if (familyName_ != null)
        {
            nameJson.put("family", familyName_);
        }
        if (names_ != null && names_.length > 0)
        {
            nameJson.getJSONArray("given").putAll(names_);
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
            // Set Practitioner identifier
            pracJson.getJSONArray("identifier")
                    .getJSONObject(0)
                    .put("system", firstIdentifierType.getSourceSystem())
                    .put("value", firstIdentifierValue);
            
            // Set PractitionerRole practitioner identifier
            pracRoleJson
                    .getJSONObject("practitioner")
                    .getJSONObject("identifier")
                    .put("system", firstIdentifierType.getSourceSystem())
                    .put("value", firstIdentifierValue);
        }
        
        if (roleType_ != null)
        {
            pracRoleJson
                    .getJSONArray("code")
                    .getJSONObject(0)
                    .getJSONArray("coding")
                    .getJSONObject(0)
                    .put("code", roleType_.getRoleType());
        }

        // Demographics mapping
        if (demographics_ != null)
        {
            // Gender
            String gender = demographics_.get("gender");
            if (gender != null)
            {
                pracJson.put("gender", gender);
            }

            // Birth date (goes in _birthDate.extension[0].valueDateTime)
            String birthDate = demographics_.get("birthDate");
            if (birthDate != null)
            {
                pracJson.getJSONObject("_birthDate")
                        .getJSONArray("extension")
                        .getJSONObject(0)
                        .put("valueDateTime", birthDate);
            }

            // Birthplace and death date - update existing extensions in template
            String birthCountry = demographics_.get("birthCountry");
            String birthProvince = demographics_.get("birthProvince");
            String deathDate = demographics_.get("deathDate");
            
            JSONArray extensionJson = accessor.getPracExtensionJson();
            MaintainUtils.updateDemographicsExtensions(extensionJson, birthCountry, birthProvince, deathDate);
        }

        JSONArray addressesJson = pracJson.getJSONArray("address");
        for (Map<String,String> info : addressList_)
        {
            addressesJson.put(MaintainUtils.createAddress(info));
        }

        JSONArray telecomJson = pracJson.getJSONArray("telecom");
        for (Map<String,String> info : telecomList_)
        {
            telecomJson.put(MaintainUtils.createTelecom(info));
        }

        JSONArray communicationJson = pracJson.getJSONArray("communication");
        for (Map<String,String> info : expertiseList_)
        {
            communicationJson.put(MaintainUtils.createExpertise(true, info));
        }

        JSONArray extensionJson = accessor.getPracExtensionJson();
        for (Map<String,String> info : statusList_)
        {
                extensionJson.put(MaintainUtils.createStatus(info));
        }

        if (confidentiality_ != null)
        {
            extensionJson.put(MaintainUtils.createConfidentiality(confidentiality_));
        }
        for (Map<String,String> info : conditionList_)
        {
            extensionJson.put(MaintainUtils.createCondition(info));
        }
        for (Map<String,String> info : disciplinaryActionList_)
        {
            extensionJson.put(MaintainUtils.createDisciplinaryAction(info));
        }
        for (Map<String,String> info : noteList_)
        {
           extensionJson.put(MaintainUtils.createNote(info));
        }

        JSONArray containedJson = pracJson.getJSONArray("contained");
        JSONArray qualificationJson = pracJson.getJSONArray("qualification");
        int refNum = 0;
        for (Map<String,String> info : credentialList_)
        {
            refNum++;
            String reference = "grantingInstitution-" + refNum;
            containedJson.put(MaintainUtils.createContained(info, reference));
            qualificationJson.put(MaintainUtils.createQualification(info, reference));
        }

        return json;
    }

    /**
     * Validates required practitioner attributes based on {@link IndividualAttribute} flags.
     * This automatically adapts if attribute requiredness changes in the enum.
     * @throws NullPointerException if any required attribute is absent
     */
    private void verifyParameters() {
        for (IndividualAttribute attr : IndividualAttribute.values()) {
            if (attr.isRequired()) {
                validateRequiredField(attr);
            }
        }
    }

    /**
     * Performs attribute-specific required validation.
     * @param attr required attribute to check
     */
    private void validateRequiredField(IndividualAttribute attr) {
        switch (attr) {
            case IDENTIFIER:
                if (identifiers_.isEmpty()) {
                    requireNonNull(null, "Missing practitioner identifier.");
                }
                break;
            case FAMILY_NAME:
                requireNonNull(familyName_, "Missing practitioner family name.");
                break;
            case NAMES:
                if (names_ == null || names_.length == 0 || names_[0] == null || names_[0].isBlank()) {
                    requireNonNull(null, "Missing practitioner given names.");
                }
                break;
            case ADDRESS:
                if (addressList_.isEmpty()) {
                    requireNonNull(null, "At least one practitioner address is required.");
                }
                break;
            case TELECOM:
                if (telecomList_.isEmpty()) {
                    requireNonNull(null, "At least one practitioner telecom is required.");
                }
                break;
            case STATUS:
                if (statusList_.isEmpty()) {
                    requireNonNull(null, "At least one practitioner status is required.");
                }
                break;
            case NOTE:
                if (noteList_.isEmpty()) {
                    requireNonNull(null, "At least one practitioner note is required.");
                }
                break;
            case EXPERTISE:
                if (expertiseList_.isEmpty()) {
                    requireNonNull(null, "At least one practitioner expertise is required.");
                }
                break;
            case CREDENTIAL:
                if (credentialList_.isEmpty()) {
                    requireNonNull(null, "At least one practitioner credential is required.");
                }
                break;
            case DISCIPLINARY_ACTION:
                if (disciplinaryActionList_.isEmpty()) {
                    requireNonNull(null, "At least one practitioner disciplinary action is required.");
                }
                break;
            case CONDITION:
                if (conditionList_.isEmpty()) {
                    requireNonNull(null, "At least one practitioner condition is required.");
                }
                break;
            case DEMOGRAPHICS:
                if (demographics_ == null || demographics_.isEmpty()) {
                    requireNonNull(null, "Missing practitioner demographics.");
                }
                break;
            case ROLE_TYPE:
                requireNonNull(roleType_, "Missing practitioner role type.");
                break;
            case CONFIDENTIALITY:
                requireNonNull(confidentiality_, "Missing practitioner confidentiality flag.");
                break;
            default:
                // no-op
                break;
        }
    }

    /**
     * Sets the confidentiality flag extension value.
     * @param confidentiality confidentiality boolean
     * @return this builder
     */
    public MaintainIndividualBuilder confidentiality(boolean confidentiality)
    {
        confidentiality_ = confidentiality;
        return this;
    }

    /**
     * Sets the practitioner family (last) name.
     * @param familyName family name string
     * @return this builder
     */
    public MaintainIndividualBuilder familyName(String familyName)
    {
        familyName_ = familyName;
        return this;
    }

    /**
    /**
     * Sets the practitioner role type using the IndividualRoleType enum.
     * @param roleType role type enum (never null)
     * @return this builder
     */
    public MaintainIndividualBuilder roleType(IndividualRoleType roleType)
    {
        roleType_ = requireNonNull(roleType);
        return this;
    }

    // Getters
    /**
     * Returns an immutable snapshot of addresses added.
     * @return unmodifiable list of address maps
     */
    public List<Map<String,String>> getAddressList() { return List.copyOf(addressList_); }
    /**
     * Returns accumulated condition entries.
     * @return immutable list of condition maps
     */
    public List<Map<String,String>> getConditionList() { return List.copyOf(conditionList_); }
    /**
     * Confidentiality flag extension value.
     * @return confidentiality Boolean or null if not set
     */
    public Boolean getConfidentiality() { return confidentiality_; }
    /**
     * Returns accumulated credential entries.
     * @return immutable list of credential maps
     */
    public List<Map<String,String>> getCredentialList() { return List.copyOf(credentialList_); }
    /**
     * Returns accumulated disciplinary action entries.
     * @return immutable list of disciplinary action maps
     */
    public List<Map<String,String>> getDisciplinaryActionList() { return List.copyOf(disciplinaryActionList_); }
    /**
     * Practitioner family name configured.
     * @return family name value or null if not provided
     */
    public String getFamilyName() { return familyName_; }
    /**
     * Returns configured names array [first, middle, third].
     * @return names array or null if not set
     */
    public String[] getNames() { return names_; }
    /**
     * Returns an immutable snapshot of all identifiers.
     * @return map copy of identifier values keyed by type
     */
    public Map<IdentifierType,String> getIdentifiers() { return Map.copyOf(identifiers_); }
    
    /**
     * Returns the identifier value for the specified identifier type (or null if not present).
     * @param type identifier type enum
     * @return identifier string or null
     */
    public String getIdentifier(IdentifierType type){
        return identifiers_.get(type);
    }
    
    /**
     * Returns practitioner demographics map.
     * @return immutable map copy or null if not set
     */
    public Map<String,String> getDemographics() { return demographics_ == null ? null : Map.copyOf(demographics_); }
    /**
     * Expertise entries accumulated.
     * @return immutable list of expertise maps
     */
    public List<Map<String,String>> getExpertiseList() { return List.copyOf(expertiseList_); }
    /**
     * Note entries accumulated.
     * @return immutable list of note maps
     */
    public List<Map<String,String>> getNoteList() { return List.copyOf(noteList_); }
    /**
     * Role type code.
     * @return role type code string or null
     */
    /**
     * Role type enum.
     * @return role type or null if not set
     */
    public IndividualRoleType getRoleType() { return roleType_; }
    /**
     * Status entries accumulated.
     * @return immutable list of status maps
     */
    public List<Map<String,String>> getStatusList() { return List.copyOf(statusList_); }
    /**
     * Telecom entries accumulated.
     * @return immutable list of telecom maps
     */
    public List<Map<String,String>> getTelecomList() { return List.copyOf(telecomList_); }

    // Fluent setters
    /**
     * Replaces the address list.
     * @param addressList address list
     * @return this builder
     */
    public MaintainIndividualBuilder setAddressList(List<Map<String,String>> addressList) {
        this.addressList_ = addressList;
        return this;
    }
    /**
     * Replaces the condition list.
     * @param conditionList condition list
     * @return this builder
     */
    public MaintainIndividualBuilder setConditionList(List<Map<String,String>> conditionList) {
        this.conditionList_ = conditionList;
        return this;
    }
    /**
     * Sets the confidentiality flag (nullable).
     * @param confidentiality Boolean flag or null
     * @return this builder
     */
    public MaintainIndividualBuilder setConfidentiality(Boolean confidentiality) {
        this.confidentiality_ = confidentiality;
        return this;
    }
    /**
     * Replaces the credential list.
     * @param credentialList credential list
     * @return this builder
     */
    public MaintainIndividualBuilder setCredentialList(List<Map<String,String>> credentialList) {
        this.credentialList_ = credentialList;
        return this;
    }
    /**
     * Replaces the disciplinary action list.
     * @param disciplinaryActionList non-null disciplinary action list
     * @return this builder
     */
    public MaintainIndividualBuilder setDisciplinaryActionList(List<Map<String,String>> disciplinaryActionList) {
        this.disciplinaryActionList_ = disciplinaryActionList;
        return this;
    }
    /**
     * Sets the family name.
     * @param familyName family name string
     * @return this builder
     */
    public MaintainIndividualBuilder setFamilyName(String familyName) {
        this.familyName_ = familyName;
        return this;
    }
    /**
     * Replaces the given names for the practitioner.
     * @param firstName first (given) name
     * @param middleName middle name (optional; may be blank)
     * @param thirdName third name (optional; may be blank)
     * @return this builder
     */
    public MaintainIndividualBuilder setNames(String firstName, String middleName, String thirdName) {
        this.names_ = new String[] { firstName, middleName, thirdName };
        return this;
    }
    /**
     * Replaces the identifiers map.
     * @param identifiers identifier map keyed by type
     * @return this builder
     */
    public MaintainIndividualBuilder setIdentifiers(Map<IdentifierType,String> identifiers) {
        this.identifiers_ = identifiers;
        return this;
    }
    /**
     * Sets practitioner demographics values.
     * Populates the internal demographics map with provided fields.
     * @param birthDate birth date (YYYY-MM-DD)
     * @param birthCountry ISO country code for birth country
     * @param birthProvince province/state code for birth province
     * @param gender gender code/prefix
     * @param deathDate death date (YYYY-MM-DD), optional
     * @return this builder
     */
    public MaintainIndividualBuilder setDemographics(String birthDate, String birthCountry, String birthProvince, String gender, String deathDate) {
        Map<String,String> demo = new HashMap<>();
        if (birthDate != null && !birthDate.isBlank())       demo.put("birthDate", birthDate);
        if (birthCountry != null && !birthCountry.isBlank()) demo.put("birthCountry", birthCountry);
        if (birthProvince != null && !birthProvince.isBlank()) demo.put("birthProvince", birthProvince);
        if (gender != null && !gender.isBlank())             demo.put("gender", gender);
        if (deathDate != null && !deathDate.isBlank())       demo.put("deathDate", deathDate);
        this.demographics_ = demo;
        return this;
    }
    /**
     * Replaces the expertise list.
     * @param expertiseList expertise list
     * @return this builder
     */
    public MaintainIndividualBuilder setExpertiseList(List<Map<String,String>> expertiseList) {
        this.expertiseList_ = expertiseList;
        return this;
    }
    /**
     * Replaces the note list.
     * @param noteList note list
     * @return this builder
     */
    public MaintainIndividualBuilder setNoteList(List<Map<String,String>> noteList) {
        this.noteList_ = noteList;
        return this;
    }

    /**
     * Replaces the status list.
     * @param statusList status list
     * @return this builder
     */
    public MaintainIndividualBuilder setStatusList(List<Map<String,String>> statusList) {
        this.statusList_ = statusList;
        return this;
    }
    /**
     * Replaces the telecom list.
     * @param telecomList telecom list
     * @return this builder
     */
    public MaintainIndividualBuilder setTelecomList(List<Map<String,String>> telecomList) {
        this.telecomList_ = telecomList;
        return this;
    }

    @Override
    /**
     * Returns the FHIR resource type produced by this builder.
     * @return INDIVIDUAL resource type
     */
    public PlrFhirResourceType resourceType() {
        return PlrFhirResourceType.INDIVIDUAL;
    }
}
