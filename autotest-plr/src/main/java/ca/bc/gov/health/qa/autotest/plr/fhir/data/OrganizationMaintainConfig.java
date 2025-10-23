package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import ca.bc.gov.health.qa.autotest.plr.fhir.model.OrgRoleType;

/**
 * Configuration object representing which Organization attributes and list counts
 * should be included when building a {@code MaintainOrgBuilder}. Required attributes
 * (per {@link OrganizationAttribute} flags) are enabled automatically.
 *
 * Mirrors the pattern used by {@link FacilityMaintainConfig} to keep test code uniform.
 */
public class OrganizationMaintainConfig {

    // Required scalar toggles (auto-enabled in ctor)
    private boolean identifier;
    private boolean name;
    private OrgRoleType roleType; // required actual value (defaults in ctor)
    private boolean address; // at least one address

    // Optional scalars
    private boolean alias;
    private boolean confidentiality;

    // Individual telecom channel toggles (all optional)
    private boolean phone;    // phone
    private boolean mobile;   // sms
    private boolean pager;    // pager
    private boolean modem;    // other
    private boolean fax;      // fax
    private boolean email;    // email
    private boolean website;  // url (http)
    private boolean ftp;      // url (ftp)

    // Multi-valued attribute counts
    private int noteCount;     // optional notes (0 => none)
    private int statusCount;   // optional statuses (0 => rely on builder default ACTIVE)

    /**
     * Initializes required attributes by inspecting {@link OrganizationAttribute} enum.
     */
    public OrganizationMaintainConfig() {
        for (OrganizationAttribute attr : OrganizationAttribute.values()) {
            if (!attr.isRequired()) continue;
            switch (attr) {
                case IDENTIFIER: this.identifier = true; break;
                case NAME:       this.name = true; break;
                case ROLE_TYPE:  this.roleType = OrgRoleType.HDS; break;
                case ADDRESS:    this.address = true; break;
                case ALIAS:      this.alias = true; break;
                case CONFIDENTIALITY: this.confidentiality = true; break;
                case TELECOM:
                    this.phone = true;
                    this.mobile = true;
                    this.pager = true;
                    this.modem = true;
                    this.fax = true;
                    this.email = true;
                    this.website = true;
                    this.ftp = true;
                    break;
                case STATUS:
                    this.statusCount = 1;
                case NOTE:
                    this.noteCount = 1;
                    break;
                default: break; // others currently not required
            }
        }
    }

    // Accessors ---------------------------------------------------------------------------------
    /** Verifies if IDENTIFIER will be included.
     * @return true if IDENTIFIER will be included */
    public boolean isIdentifierEnabled() { return identifier; }

    /** Verifies if NAME will be included.
     * @return true if NAME will be included */
    public boolean isNameEnabled() { return name; }

    /** Verifies if ROLE_TYPE will be included.
     * @return organization role type value (never null after construction) */
    public OrgRoleType getRoleType() { return roleType; }

    /** Verifies if ADDRESS will be included.
     * @return true if ADDRESS will be included */
    public boolean isAddressEnabled() { return address; }

    /** Verifies if ALIAS will be included.
     * @return true if ALIAS will be included */
    public boolean isAliasEnabled() { return alias; }

    /** Verifies if CONFIDENTIALITY will be included.
     * @return true if CONFIDENTIALITY will be included */
    public boolean isConfidentialityEnabled() { return confidentiality; }

    /** Verifies if PHONE will be included.
     * @return true if PHONE will be included */
    public boolean isPhoneEnabled() { return phone; }

    /** Verifies if MOBILE will be included.
     * @return true if MOBILE will be included */
    public boolean isMobileEnabled() { return mobile; }

    /** Verifies if PAGER will be included.
     * @return true if PAGER will be included */
    public boolean isPagerEnabled() { return pager; }

    /** Verifies if MODEM will be included.
     * @return true if MODEM will be included */
    public boolean isModemEnabled() { return modem; }

    /** Verifies if FAX will be included.
     * @return true if FAX will be included */
    public boolean isFaxEnabled() { return fax; }

    /** Verifies if EMAIL will be included.
     * @return true if EMAIL will be included */
    public boolean isEmailEnabled() { return email; }

    /** Verifies if WEBSITE will be included.
     * @return true if WEBSITE will be included */
    public boolean isWebsiteEnabled() { return website; }

    /** Verifies if FTP will be included.
     * @return true if FTP will be included */
    public boolean isFtpEnabled() { return ftp; }

    /** Gets the number of NOTEs that will be included. 
     * @return the number of NOTEs that will be included */
    public int getNoteCount() { return noteCount; }

    /** Gets the number of STATUSes that will be included. 
     * @return the number of STATUSes that will be included */
    public int getStatusCount() { return statusCount; }


    // Fluent enabling ----------------------------------------------------------------------------
    /** Enable identifier attribute. 
     * @return this config */
    public OrganizationMaintainConfig withIdentifier() { this.identifier = true; return this; }
    /** Enable name attribute. 
     * @return this config */
    public OrganizationMaintainConfig withName() { this.name = true; return this; }
    /** Set role type value. 
     * @param roleType organization role type
     * @return this config */
    public OrganizationMaintainConfig withRoleType(OrgRoleType roleType) { this.roleType = roleType; return this; }
    /** Enable address attribute. 
     * @return this config */
    public OrganizationMaintainConfig withAddress() { this.address = true; return this; }
    /** Enable alias attribute. 
     * @return this config */
    public OrganizationMaintainConfig withAlias() { this.alias = true; return this; }
    /** Enable confidentiality attribute. 
     * @return this config */
    public OrganizationMaintainConfig withConfidentiality() { this.confidentiality = true; return this; }
    /** Enable phone telecom. 
     * @return this config */
    public OrganizationMaintainConfig withPhone() { this.phone = true; return this; }
    /** Enable mobile telecom. 
     * @return this config */
    public OrganizationMaintainConfig withMobile() { this.mobile = true; return this; }
    /** Enable pager telecom. 
     * @return this config */
    public OrganizationMaintainConfig withPager() { this.pager = true; return this; }
    /** Enable modem telecom. 
     * @return this config */
    public OrganizationMaintainConfig withModem() { this.modem = true; return this; }
    /** Enable fax telecom. 
     * @return this config */
    public OrganizationMaintainConfig withFax() { this.fax = true; return this; }
    /** Enable email telecom. 
     * @return this config */
    public OrganizationMaintainConfig withEmail() { this.email = true; return this; }
    /** Enable website telecom. 
     * @return this config */
    public OrganizationMaintainConfig withWebsite() { this.website = true; return this; }
    /** Enable ftp telecom. 
     * @return this config */
    public OrganizationMaintainConfig withFtp() { this.ftp = true; return this; }
    /** Enable all telecom channel types.
     * @return this config */
    public OrganizationMaintainConfig withAllTelecom() {
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

    /** Set number of note entries to generate.
     * @param count number of notes (>= required minimum)
     * @return this config
     * @throws IllegalArgumentException if below required minimum */
    public OrganizationMaintainConfig withNotes(int count) {
        int minRequired = OrganizationAttribute.NOTE.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("note count must be >= " + minRequired);
        this.noteCount = count;
        return this;
    }
    /** Set number of status entries to generate.
     * @param count number of statuses (>= required minimum)
     * @return this config
     * @throws IllegalArgumentException if below required minimum */
    public OrganizationMaintainConfig withStatuses(int count) {
        int minRequired = OrganizationAttribute.STATUS.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("status count must be >= " + minRequired);
        this.statusCount = count;
        return this;
    }
    /** Enable all attributes and set counts for note and status lists.
     * @param noteCount number of notes
     * @param statusCount number of statuses
     * @return this config */
    public OrganizationMaintainConfig withAllAttributes(int noteCount, int statusCount) {
        withIdentifier(); 
        withName(); 
        withRoleType(roleType != null ? roleType : OrgRoleType.HDS); 
        withAddress(); 
        withAlias(); 
        withConfidentiality(); 
        withAllTelecom(); 
        withNotes(noteCount); 
        withStatuses(statusCount); 
        return this; 
    }
}
