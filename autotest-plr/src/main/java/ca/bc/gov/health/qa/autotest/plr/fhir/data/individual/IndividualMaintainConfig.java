package ca.bc.gov.health.qa.autotest.plr.fhir.data.individual;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.model.IndividualRoleType;

/**
 * Configuration object representing which Practitioner attributes and list counts
 * should be included when building a MaintainPracBuilder. Required attributes
 * (per {@link IndividualAttribute} flags) are enabled automatically.
 */
public class IndividualMaintainConfig {

    private boolean identifier;
    private boolean familyName;
    private IndividualRoleType roleType;
    private boolean address;
    private boolean confidentiality;
    private boolean demographics;
    private boolean givenNames; // names array

    // Individual telecom channel toggles (optional)
    private boolean phone;    // phone
    private boolean mobile;   // sms
    private boolean pager;    // pager
    private boolean modem;    // other
    private boolean fax;      // fax
    private boolean email;    // email
    private boolean website;  // url (http)
    private boolean ftp;      // url (ftp)

    // Multi-valued attribute counts
    private int noteCount;
    private int statusCount;
    private int expertiseCount;
    private int credentialCount;
    private int conditionCount;
    private int disciplinaryActionCount;

    /**
     * Initializes required attributes by inspecting {@link IndividualAttribute} enum.
     * Required flags are auto-enabled for scalars, telecom channels, and list counts.
     * Uses MD as the default role type.
     */
    public IndividualMaintainConfig() {
        // Call constructor using MD as default role type
        this(IndividualRoleType.MD);
    }

    /**
     * Initializes required attributes by inspecting {@link IndividualAttribute} enum.
     * @param roleType individual role type to set as required value
     */
    public IndividualMaintainConfig(IndividualRoleType roleType) {
        // Initialize based on required IndividualAttribute flags
        for (IndividualAttribute attr : IndividualAttribute.values()) {
            if (!attr.isRequired()) continue;
            switch (attr) {
                case IDENTIFIER: 
                    this.identifier = true; 
                    break;
                case FAMILY_NAME: 
                    this.familyName = true; 
                    break;
                case ROLE_TYPE: 
                    this.roleType = roleType; 
                    break;
                case ADDRESS: 
                    this.address = true; 
                    break;
                case CONFIDENTIALITY: 
                    this.confidentiality = true; 
                    break;
                case DEMOGRAPHICS:
                    this.demographics = true;
                    break;
                case NAMES:
                    this.givenNames = true;
                    break;
                case TELECOM:
                    this.phone = true; this.mobile = true; this.pager = true; this.modem = true;
                    this.fax = true; this.email = true; this.website = true; this.ftp = true;
                    break;
                case NOTE:
                    this.noteCount = 1;
                    break;
                case STATUS:
                    this.statusCount = 1;
                    break;
                case EXPERTISE:
                    this.expertiseCount = 1;
                    break;
                case CREDENTIAL:
                    this.credentialCount = 1;
                    break;
                case CONDITION:
                    this.conditionCount = 1;
                    break;
                case DISCIPLINARY_ACTION:
                    this.disciplinaryActionCount = 1;
                    break;
                default: 
                    break;
            }
        }
    }

    // Accessors

    /** Verifies if IDENTIFIER will be included.
     * @return true if IDENTIFIER will be included */
    public boolean isIdentifierEnabled() { return identifier; }

    /** Verifies if FAMILY_NAME will be included.
     * @return true if FAMILY_NAME will be included */
    public boolean isFamilyNameEnabled() { return familyName; }

    /** Gets the individual role type.
     * @return the role type */
    public IndividualRoleType getRoleType() { return roleType; }

    /** Verifies if ADDRESS will be included.
     * @return true if ADDRESS will be included */
    public boolean isAddressEnabled() { return address; }

    /** Verifies if CONFIDENTIALITY will be included.
     * @return true if CONFIDENTIALITY will be included */
    public boolean isConfidentialityEnabled() { return confidentiality; }

    /** Verifies if DEMOGRAPHICS will be included.
     * @return true if DEMOGRAPHICS will be included */
    public boolean isDemographicsEnabled() { return demographics; }

    /** Verifies if NAMES (given names array) will be included.
     * @return true if NAMES will be included */
    public boolean isGivenNamesEnabled() { return givenNames; }

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

    /** Gets the number of EXPERTISE entries to include.
     * @return count of EXPERTISE entries */
    public int getExpertiseCount() { return expertiseCount; }


    /** Gets the number of CREDENTIAL entries to include.
     * @return count of CREDENTIAL entries */
    public int getCredentialCount() { return credentialCount; }

    /** Gets the number of CONDITION entries to include.
     * @return count of CONDITION entries */
    public int getConditionCount() { return conditionCount; }

    /** Gets the number of DISCIPLINARY_ACTION entries to include.
     * @return count of DISCIPLINARY_ACTION entries */
    public int getDisciplinaryActionCount() { return disciplinaryActionCount; }

    // Fluent enabling
    /** Enable identifier attribute.
     * @return this config */
    public IndividualMaintainConfig withIdentifier() { this.identifier = true; return this; }

    /** Enable family name attribute.
     * @return this config */
    public IndividualMaintainConfig withFamilyName() { this.familyName = true; return this; }

    /** Set role type value.
     * @param roleType individual role type
     * @return this config */
    public IndividualMaintainConfig withRoleType(IndividualRoleType roleType) { this.roleType = roleType; return this; }

    /** Enable address attribute.
     * @return this config */
    public IndividualMaintainConfig withAddress() { this.address = true; return this; }

    /** Enable confidentiality attribute.
     * @return this config */
    public IndividualMaintainConfig withConfidentiality() { this.confidentiality = true; return this; }

    /** Enable demographics attribute.
     * @return this config */
    public IndividualMaintainConfig withDemographics() { this.demographics = true; return this; }

    /** Enable NAMES (given names array).
     * @return this config */
    public IndividualMaintainConfig withGivenNames() { this.givenNames = true; return this; }

    /** Enable PHONE telecom.
     * @return this config */
    public IndividualMaintainConfig withPhone() { this.phone = true; return this; }

    /** Enable MOBILE telecom.
     * @return this config */
    public IndividualMaintainConfig withMobile() { this.mobile = true; return this; }

    /** Enable PAGER telecom.
     * @return this config */
    public IndividualMaintainConfig withPager() { this.pager = true; return this; }

    /** Enable MODEM telecom.
     * @return this config */
    public IndividualMaintainConfig withModem() { this.modem = true; return this; }

    /** Enable FAX telecom.
     * @return this config */
    public IndividualMaintainConfig withFax() { this.fax = true; return this; }

    /** Enable EMAIL telecom.
     * @return this config */
    public IndividualMaintainConfig withEmail() { this.email = true; return this; }

    /** Enable WEBSITE telecom.
     * @return this config */
    public IndividualMaintainConfig withWebsite() { this.website = true; return this; }

    /** Enable FTP telecom.
     * @return this config */
    public IndividualMaintainConfig withFtp() { this.ftp = true; return this; }

    /** Set number of note entries to generate.
     * @param count number of notes
     * @return this config */
    public IndividualMaintainConfig withNotes(int count) { this.noteCount = count; return this; }

    /** Set number of status entries to generate.
     * @param count number of statuses
     * @return this config */
    public IndividualMaintainConfig withStatuses(int count) { this.statusCount = count; return this; }

    /** Set number of expertise entries to generate.
     * @param count number of expertise entries
     * @return this config */
    public IndividualMaintainConfig withExpertise(int count) { this.expertiseCount = count; return this; }

    /** Set number of credential entries to generate.
     * @param count number of credential entries
     * @return this config */
    public IndividualMaintainConfig withCredentials(int count) { this.credentialCount = count; return this; }

    /** Set number of condition entries to generate.
     * @param count number of condition entries
     * @return this config */
    public IndividualMaintainConfig withConditions(int count) { this.conditionCount = count; return this; }

    /** Set number of disciplinary action entries to generate.
     * @param count number of disciplinary action entries
     * @return this config */
    public IndividualMaintainConfig withDisciplinaryActions(int count) { this.disciplinaryActionCount = count; return this; }

    /** Enable all telecom channel types.
     * @return this config */
    public IndividualMaintainConfig withAllTelecom() {
        this.phone = true; this.mobile = true; this.pager = true; this.modem = true;
        this.fax = true; this.email = true; this.website = true; this.ftp = true;
        return this;
    }

    /** Enable all attributes and set counts for multi-valued lists.
     * @param noteCount number of notes
     * @param statusCount number of statuses
     * @param expertiseCount number of expertise entries
     * @param credentialCount number of credential entries
     * @param conditionCount number of condition entries
     * @param disciplinaryCount number of disciplinary action entries
     * @return this config */
    public IndividualMaintainConfig withAllAttributes(int noteCount, int statusCount, int expertiseCount, int credentialCount, int conditionCount, int disciplinaryCount) {
        withIdentifier(); withFamilyName();
        withAddress(); withConfidentiality(); withDemographics(); withGivenNames(); withAllTelecom();
        withNotes(noteCount); withStatuses(statusCount); withExpertise(expertiseCount);
        withCredentials(credentialCount); withConditions(conditionCount); withDisciplinaryActions(disciplinaryCount);
        return this;
    }
}
