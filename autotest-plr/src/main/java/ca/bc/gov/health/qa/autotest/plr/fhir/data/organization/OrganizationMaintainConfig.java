package ca.bc.gov.health.qa.autotest.plr.fhir.data.organization;

import ca.bc.gov.health.qa.autotest.plr.fhir.data.facility.FacilityMaintainConfig;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;

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

    // Optional OrganizationProperties toggles
    private boolean clinicServices;            // single-valued
    private boolean clinicOwnerBusinessType;   // single-valued
    private boolean clinicType;                // single-valued
    private boolean clinicLegalBusinessName;   // single-valued
    private boolean pciFlag;                   // single-valued

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
    // OrganizationProperties list counts
    private int addressUnitCount;            // number of address unit entries
    private int clinicHoursOfOperationCount; // number of hours entries
    private int clinicOwnerNamesCount;       // number of owner names
    private int payeeNumberCount;            // number of payee numbers

    /**
     * Initializes required attributes by inspecting {@link OrganizationAttribute} enum.
     * Use HDS as default organization role type.
     */
    public OrganizationMaintainConfig() {
        //Call construcotr using HDS as default roletype
        this(OrgRoleType.HDS);
    }

    /**
     * Initializes required attributes by inspecting {@link OrganizationAttribute} enum.
     * @param roleType organization role type to set as required value
     */
    public OrganizationMaintainConfig(OrgRoleType roleType) {
        for (OrganizationAttribute attr : OrganizationAttribute.values()) {
            if (!attr.isRequired()) continue;
            switch (attr) {
                case IDENTIFIER: this.identifier = true; break;
                case NAME:       this.name = true; break;
                case ROLE_TYPE:  
                    this.roleType = roleType;
                    break;
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
                // OrganizationProperties-derived attributes
                case CLINIC_SERVICES:
                    this.clinicServices = true;
                    break;
                case CLINIC_OWNER_BUSINESS_TYPE:
                    this.clinicOwnerBusinessType = true;
                    break;
                case CLINIC_TYPE:
                    this.clinicType = true;
                    break;
                case CLINIC_LEGAL_BUSINESS_NAME:
                    this.clinicLegalBusinessName = true;
                    break;
                case PCI_FLAG:
                    this.pciFlag = true;
                    break;
                case ADDRESS_UNIT:
                    this.addressUnitCount = 1;
                    break;
                case CLINIC_HOURS_OF_OPERATION:
                    this.clinicHoursOfOperationCount = 1;
                    break;
                case CLINIC_OWNER_NAMES:
                    this.clinicOwnerNamesCount = 1;
                    break;
                case PAYEE_NUMBER:
                    this.payeeNumberCount = 1;
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

    // OrganizationProperties toggles
    /** Verifies if CLINIC_SERVICES will be included.
     * @return true if CLINIC_SERVICES will be included */
    public boolean isClinicServicesEnabled() { return clinicServices; }

    /** Verifies if CLINIC_OWNER_BUSINESS_TYPE will be included.
     * @return true if CLINIC_OWNER_BUSINESS_TYPE will be included */
    public boolean isClinicOwnerBusinessTypeEnabled() { return clinicOwnerBusinessType; }

    /** Verifies if CLINIC_TYPE will be included.
     * @return true if CLINIC_TYPE will be included */
    public boolean isClinicTypeEnabled() { return clinicType; }

    /** Verifies if CLINIC_LEGAL_BUSINESS_NAME will be included.
     * @return true if CLINIC_LEGAL_BUSINESS_NAME will be included */
    public boolean isClinicLegalBusinessNameEnabled() { return clinicLegalBusinessName; }

    /** Verifies if PCI_FLAG will be included.
     * @return true if PCI_FLAG will be included */
    public boolean isPciFlagEnabled() { return pciFlag; }

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

    /** Gets the number of ADDRESS_UNIT entries to include.
     * @return count of ADDRESS_UNIT entries */
    public int getAddressUnitCount() { return addressUnitCount; }

    /** Gets the number of CLINIC_HOURS_OF_OPERATION entries to include.
     * @return count of CLINIC_HOURS_OF_OPERATION entries */
    public int getClinicHoursOfOperationCount() { return clinicHoursOfOperationCount; }

    /** Gets the number of CLINIC_OWNER_NAMES entries to include.
     * @return count of CLINIC_OWNER_NAMES entries */
    public int getClinicOwnerNamesCount() { return clinicOwnerNamesCount; }

    /** Gets the number of PAYEE_NUMBER entries to include.
     * @return count of PAYEE_NUMBER entries */
    public int getPayeeNumberCount() { return payeeNumberCount; }

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

    // OrganizationProperties fluent enabling
    /** Enable CLINIC_SERVICES.
     * @return this config */
    public OrganizationMaintainConfig withClinicServices() { this.clinicServices = true; return this; }
    /** Enable CLINIC_OWNER_BUSINESS_TYPE.
     * @return this config */
    public OrganizationMaintainConfig withClinicOwnerBusinessType() { this.clinicOwnerBusinessType = true; return this; }
    /** Enable CLINIC_TYPE.
     * @return this config */
    public OrganizationMaintainConfig withClinicType() { this.clinicType = true; return this; }
    /** Enable CLINIC_LEGAL_BUSINESS_NAME.
     * @return this config */
    public OrganizationMaintainConfig withClinicLegalBusinessName() { this.clinicLegalBusinessName = true; return this; }
    /** Enable ADDRESS_UNIT and set count.
     * @param count number of address unit entries (>= required minimum)
     * @return this config
     * @throws IllegalArgumentException if below required minimum */
    public OrganizationMaintainConfig withAddressUnit(int count) {
        int minRequired = OrganizationAttribute.ADDRESS_UNIT.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("address unit count must be >= " + minRequired);
        this.addressUnitCount = count; return this;
    }
    /** Enable CLINIC_HOURS_OF_OPERATION and set count.
     * @param count number of hours entries (>= required minimum)
     * @return this config
     * @throws IllegalArgumentException if below required minimum */
    public OrganizationMaintainConfig withClinicHoursOfOperation(int count) {
        int minRequired = OrganizationAttribute.CLINIC_HOURS_OF_OPERATION.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("clinic hours count must be >= " + minRequired);
        this.clinicHoursOfOperationCount = count; return this;
    }
    /** Enable CLINIC_OWNER_NAMES and set count.
     * @param count number of owner names (>= required minimum)
     * @return this config
     * @throws IllegalArgumentException if below required minimum */
    public OrganizationMaintainConfig withClinicOwnerNames(int count) {
        int minRequired = OrganizationAttribute.CLINIC_OWNER_NAMES.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("clinic owner names count must be >= " + minRequired);
        this.clinicOwnerNamesCount = count; return this;
    }
    /** Enable PAYEE_NUMBER and set count.
     * @param count number of payee numbers (>= required minimum)
     * @return this config
     * @throws IllegalArgumentException if below required minimum */
    public OrganizationMaintainConfig withPayeeNumber(int count) {
        int minRequired = OrganizationAttribute.PAYEE_NUMBER.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("payee number count must be >= " + minRequired);
        this.payeeNumberCount = count; return this;
    }
    /** Enable PCI_FLAG.
     * @return this config */
    public OrganizationMaintainConfig withPciFlag() { this.pciFlag = true; return this; }
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
     * @param addressUnitCount number of address unit values
     * @param hoursCount number of hours of operation entries
     * @param ownerNamesCount number of owner names
     * @param payeeCount number of payee numbers
     * @return this config */
    public OrganizationMaintainConfig withAllAttributes(int noteCount, int statusCount, int addressUnitCount, int hoursCount, int ownerNamesCount, int payeeCount) {
        withIdentifier(); 
        withName(); 
        withRoleType(roleType != null ? roleType : OrgRoleType.HDS); 
        withAddress(); 
        withAlias(); 
        withConfidentiality(); 
        withAllTelecom(); 
        withNotes(noteCount); 
        withStatuses(statusCount); 
        withAllOrgProperties(addressUnitCount, hoursCount, ownerNamesCount, payeeCount);
        return this; 
    }

    /** Enable all OrganizationProperties toggles and set list counts.
     * @param addressUnitCount number of address unit values
     * @param hoursCount number of hours entries
     * @param ownerNamesCount number of owner names
     * @param payeeCount number of payee numbers
     * @return this config */
    public OrganizationMaintainConfig withAllOrgProperties(int addressUnitCount, int hoursCount, int ownerNamesCount, int payeeCount) {
        withClinicServices();
        withClinicOwnerBusinessType();
        withClinicType();
        withClinicLegalBusinessName();
        withAddressUnit(addressUnitCount);
        withClinicHoursOfOperation(hoursCount);
        withClinicOwnerNames(ownerNamesCount);
        withPayeeNumber(payeeCount);
        withPciFlag();
        return this;
    }
}
