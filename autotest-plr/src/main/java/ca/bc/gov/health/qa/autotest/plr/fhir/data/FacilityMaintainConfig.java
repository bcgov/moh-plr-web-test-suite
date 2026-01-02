package ca.bc.gov.health.qa.autotest.plr.fhir.data;

// Uses FacilityAttribute enum for scalar attribute selection


import java.util.List;

/**
 * Configuration object representing which optional MaintainFacilityFields are enabled
 * for a Facility maintain operation. Required fields are implicitly present; this class
 * focuses on toggling optional ones while still allowing explicit inclusion of required
 * fields for clarity.
 */
public class FacilityMaintainConfig {

    // boolean toggles for single-occurrence optional attributes
    private boolean phone;
    private boolean mobile;
    private boolean pager;
    private boolean modem;
    private boolean fax;
    private boolean email;
    private boolean website;
    private boolean ftp;
    private boolean description;
    private boolean name;
    private boolean address;   
    private boolean identifier;
    private boolean nameManual;


    // multi-valued counts
    private int noteCount;                  // number of notes to generate (0 => omit notes unless required)
    private int relationshipCount;          // number of org relationships to generate (0 => omit relationships unless required)
    private String facility_name;           // manually selected name (if applicable)
    private List<String> relationshipNames; // manually selected organization relationship names (if applicable)

    /**
     * Default constructor: enables all required attributes to build a valid facility based on FacilityAttribute enum.
     */
    public FacilityMaintainConfig() {
        // Dynamically with required attributes based on if FacilityAttribute is marked as required.
        for (FacilityAttribute attr : FacilityAttribute.values()) {
            if (!attr.isRequired()) continue;
            switch (attr) {
                case NAME:       this.name = true; break;
                case ADDRESS:    this.address = true; break;
                case IDENTIFIER: this.identifier = true; break;
                case PHONE:      this.phone = true; break;
                case MOBILE:     this.mobile = true; break;
                case PAGER:      this.pager = true; break;
                case MODEM:      this.modem = true; break;
                case FAX:        this.fax = true; break;
                case EMAIL:      this.email = true; break;
                case WEBSITE:    this.website = true; break;
                case FTP:        this.ftp = true; break;
                case DESCRIPTION: this.description = true; break;
                case ORG_RELATIONSHIP: this.relationshipCount = 1; break; // required => at least one
                case NOTE:            this.noteCount = 1; break; // required => at least one
                default: break;
            }
        }
        this.nameManual = false;
    }

    // Accessors for factory usage


    /** Verifies if PHONE will be included. 
     * @return true if PHONE telecom will be generated */
    public boolean isPhoneEnabled() { return phone; }
    
    /** Verifies if MOBILE will be included. 
     * @return true if MOBILE telecom will be generated */
    public boolean isMobileEnabled() { return mobile; }
    
    /** Verifies if PAGER will be included. 
     * @return true if PAGER telecom will be generated */
    public boolean isPagerEnabled() { return pager; }
    
    /** Verifies if MODEM will be included. 
     * @return true if MODEM telecom will be generated */
    public boolean isModemEnabled() { return modem; }
    
    /** Verifies if FAX will be included. 
     * @return true if FAX telecom will be generated */
    public boolean isFaxEnabled() { return fax; }
    
    /** Verifies if EMAIL will be included. 
     * @return true if EMAIL telecom will be generated */
    public boolean isEmailEnabled() { return email; }
    
    /** Verifies if WEBSITE will be included. 
     * @return true if WEBSITE telecom will be generated */
    public boolean isWebsiteEnabled() { return website; }
    
    /** Verifies if FTP will be included. 
     * @return true if FTP telecom will be generated */
    public boolean isFtpEnabled() { return ftp; }
    
    /** Verifies if DESCRIPTION will be included. 
     * @return true if DESCRIPTION will be generated */
    public boolean isDescriptionEnabled() { return description; }
    
    /** Verifies if NAME will be included. 
     * @return true if NAME will be generated */
    public boolean isNameEnabled() { return name; }
    
    /** Verifies if ADDRESS will be included. 
     * @return true if ADDRESS will be generated */
    public boolean isAddressEnabled() { return address; }

    /** Verifies if IDENTIFIER will be included. 
     * @return true if IDENTIFIER will be generated */
    public boolean isIdentifierEnabled() { return identifier; }

    /**
     * Verifies if NAME will be manually set instead of randomly generated.
     * @return true if name will be manually generated, false if not */
    public boolean isNameManualEnabled() { return nameManual; }

    /**
     * Number of note entries that will be generated.
     * @return note count (0 if notes disabled)
     */
    public int getNoteCount() { return noteCount; }

    /**
     * Number of organization affiliation relationships that will be generated.
     * @return relationship count (0 if none requested)
     */
    public int getRelationshipCount() { return relationshipCount; }

    /**
     * Number of manually specified organization affiliation relationships that will be generated.
     * @return  relationship names to be used (null if nothing specific will be generated)
     */
    public List<String> getRelationshipNames() { return relationshipNames; }

    /**
     * Name that is used for the facility if name will be manually set
     * @return  facility name (null if will be randomly generated)
     */
    public String getName() { return facility_name; }

    /**
     * Indicates whether at least one note will be generated.
     * @return true if noteCount > 0
     */
    public boolean hasNotes() { return noteCount > 0; }

    /**
     * Indicates whether at least one organization relationship will be generated.
     * @return true if relationshipCount > 0
     */
    public boolean hasOrgRelationships() { return relationshipCount > 0; }

    /** Enable PHONE attribute.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withPhone() { this.phone = true; return this; }
    /** Enable MOBILE attribute.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withMobile() { this.mobile = true; return this; }
    /** Enable PAGER attribute.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withPager() { this.pager = true; return this; }
    /** Enable MODEM attribute.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withModem() { this.modem = true; return this; }
    /** Enable FAX attribute.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withFax() { this.fax = true; return this; }
    /** Enable EMAIL attribute.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withEmail() { this.email = true; return this; }
    /** Enable WEBSITE attribute.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withWebsite() { this.website = true; return this; }
    /** Enable FTP attribute.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withFtp() { this.ftp = true; return this; }
    /** Enable DESCRIPTION attribute.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withDescription() { this.description = true; return this; }
    /** Explicitly (re)enable NAME (required by default).
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withName() { this.name = true; return this; }
    /** Explicitly (re)enable ADDRESS (required by default).
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withAddress() { this.address = true; return this; }
    /** Explicitly (re)enable IDENTIFIER (required by default).
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withIdentifier() { this.identifier = true; return this; }
    /** Explicitly enable NAME with a desired specific facility name.
     * @param name  the name to set as the facility name.
     * @return this config for fluent chaining */
    public FacilityMaintainConfig withName(String name)
    {
        this.name = true; this.nameManual = true;
        this.facility_name = name;
        return this;
    }

    /**
     * Set number of notes to generate.
     * @param count number of notes (>= required minimum)
     * @return this config for fluent chaining
     * @throws IllegalArgumentException if count below required minimum
     */
    public FacilityMaintainConfig withNotes(int count) {
        int minRequired = FacilityAttribute.NOTE.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("note count must be >= " + minRequired);
        this.noteCount = count;
        return this;
    }

    /**
     * Set number of organization relationships to generate.
     * @param count number of relationships (>= required minimum)
     * @return this config for fluent chaining
     * @throws IllegalArgumentException if count below required minimum
     */
    public FacilityMaintainConfig withOrgRelationships(int count) {
        int minRequired = FacilityAttribute.ORG_RELATIONSHIP.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("relationship count must be >= " + minRequired);
        this.relationshipCount = count;
        return this;
    }

    /**
     * Set organization relationships to generate based on specific names instead of a count.
     *
     * @param orgNames list of organization names to create relationships for
     * @return this config for fluent chaining
     * @throws IllegalArgumentException if provided list size is below required minimum
     */
    public FacilityMaintainConfig withOrgRelationships(List<String> orgNames)
    {
        int count = orgNames.size();
        int minRequired = FacilityAttribute.ORG_RELATIONSHIP.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("relationship count must be >= " + minRequired);
        // TODO something involving validation of names (under maximum, etc.)
        this.relationshipNames = orgNames;
        return this;
    }

    /**
     * Enable all telecom related attributes (phone, mobile, pager, modem, fax, email, website, ftp).
     * @return this config
     */
    public FacilityMaintainConfig withAllTelecom() {
        this.phone = true;
        this.mobile = true;
        this.pager = true;
        this.modem = true;
        this.fax = true;
        this.email = true;
        this.website = true;
        this.ftp = true;
  
        return this;
    }

    /**
     * Enable all optional scalar attributes plus set counts for notes and organization relationships.
     * Required attributes are already enabled by the constructor; this method reaffirms them for clarity.
     * @param noteCount number of notes
     * @param relationshipCount number of organization relationships
     * @return this config
     */
    public FacilityMaintainConfig withAllAttributes(int noteCount, int relationshipCount) {
        this.description = true;
        this.name = true;
        this.address = true;   
        this.identifier = true; 
        withAllTelecom();
        withOrgRelationships(relationshipCount);
        withNotes(noteCount);

        return this;
    }

}
