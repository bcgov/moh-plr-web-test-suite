package ca.bc.gov.health.qa.autotest.plr.fhir.data.organization;

import java.util.List;

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
    private boolean nameManual;     // if name is being specified manually
    private String org_name;        // organization name, used if name is input manually

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
    private int facilityRelationshipCount;   // number of facility relationships to generate (0 => omit)
    private List<String> facilityRelationshipNames;  // explicit facility names for relationships (null => random generation)
    private int organizationRelationshipCount; // number of organization relationships to generate (0 => omit)
    private int individualRelationshipCount; // number of individual relationships to generate (0 => omit)


    /**
     * Initializes required attributes by inspecting {@link OrganizationAttribute} enum.
     * Use HDS as default organization role type.
     */
    public OrganizationMaintainConfig() {
        // Call constructor using HDS as default role type
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
                    break;
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
                case FACILITY_RELATIONSHIPS:
                    this.facilityRelationshipCount = 1; // required => at least one
                    break;
                case ORGANIZATION_RELATIONSHIPS:
                    this.organizationRelationshipCount = 1; // required => at least one
                    break;
                case INDIVIDUAL_RELATIONSHIPS:
                    this.individualRelationshipCount = 1; // required => at least one
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

    /**
     * Verifies if NAME will be manually set instead of randomly generated.
     * @return true if name will be manually generated, false if not */
    public boolean isNameManualEnabled() { return nameManual; }

    /**
     * Name that is used for the facility if name will be manually set
     * @return  facility name (null if will be randomly generated)
     */
    public String getName() { return org_name; }

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

    /** Gets the number of facility relationships configured.\n     * @return facility relationship count */
    public int getFacilityRelationshipCount() { return facilityRelationshipCount; }

    /** Gets the explicit facility relationship names (if any).\n     * @return list of facility names or null if not set */
    public List<String> getFacilityRelationshipNames() { return facilityRelationshipNames; }

    /** Checks if facility relationships are configured.\n     * @return true if facilityRelationshipCount > 0 */
    public boolean hasFacilityRelationships() { return facilityRelationshipCount > 0; }

    /** Gets the number of organization relationships configured.\n     * @return organization relationship count */
    public int getOrganizationRelationshipCount() { return organizationRelationshipCount; }

    /** Checks if organization relationships are configured.\n     * @return true if organizationRelationshipCount > 0 */
    public boolean hasOrganizationRelationships() { return organizationRelationshipCount > 0; }

    /** Gets the number of individual relationships configured.\n     * @return individual relationship count */
    public int getIndividualRelationshipCount() { return individualRelationshipCount; }

    /** Checks if individual relationships are configured.\n     * @return true if individualRelationshipCount > 0 */
    public boolean hasIndividualRelationships() { return individualRelationshipCount > 0; }

    // Fluent enabling ----------------------------------------------------------------------------
    /** Enable identifier attribute. 
     * @return this config */
    public OrganizationMaintainConfig withIdentifier() { this.identifier = true; return this; }
    /** Enable name attribute. 
     * @return this config */
    public OrganizationMaintainConfig withName() { this.name = true; return this; }
    /** Explicitly enable name attribute
     * @param name  the name to use
     * @return      this config */
    public OrganizationMaintainConfig withName(String name)
    {
        this.name = true; this.nameManual = true;
        this.org_name = name;
        return this;
    }
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

    /** Enable facility relationships with specified count (random facilities).
     * @param count number of facility relationships to create
     * @return this config
     * @throws IllegalArgumentException if count below required minimum */
    public OrganizationMaintainConfig withFacilityRelationships(int count) {
        int minRequired = OrganizationAttribute.FACILITY_RELATIONSHIPS.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("facility relationship count must be >= " + minRequired);
        this.facilityRelationshipCount = count;
        return this;
    }

    /** Enable facility relationships with explicit facility names.
     * @param names list of facility names to create relationships with
     * @return this config
     * @throws IllegalArgumentException if provided list size is below required minimum */
    public OrganizationMaintainConfig withFacilityRelationships(List<String> names) {
        int count = names != null ? names.size() : 0;
        int minRequired = OrganizationAttribute.FACILITY_RELATIONSHIPS.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("facility relationship count must be >= " + minRequired);
        // TODO something involving validation of names (under maximum, etc.)
        this.facilityRelationshipNames = names;
        this.facilityRelationshipCount = count;
        return this;
    }

    /** Enable organization relationships with specified count (random relationship codes).
     * @param count number of organization relationships to create
     * @return this config
     * @throws IllegalArgumentException if count below required minimum */
    public OrganizationMaintainConfig withOrganizationRelationships(int count) {
        int minRequired = OrganizationAttribute.ORGANIZATION_RELATIONSHIPS.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("organization relationship count must be >= " + minRequired);
        this.organizationRelationshipCount = count;
        return this;
    }

    /** Enable individual relationships with specified count (random relationship codes).
     * @param count number of organization relationships to create
     * @return this config
     * @throws IllegalArgumentException if count below required minimum */
    public OrganizationMaintainConfig withIndividualRelationships(int count) {
        int minRequired = OrganizationAttribute.INDIVIDUAL_RELATIONSHIPS.isRequired() ? 1 : 0;
        if (count < minRequired) throw new IllegalArgumentException("organization relationship count must be >= " + minRequired);
        this.individualRelationshipCount = count;
        return this;
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

    /** Set one status to be present as an entry to generate.
     * @return this config
     * @throws IllegalArgumentException if below required minimum */
    public OrganizationMaintainConfig withStatus() {
            this.statusCount = 1;
        return this;
    }


    /** Enable all attributes and set counts for note and status lists.
     * @param noteCount number of notes
     * @param statusCount number of statuses
     * @param addressUnitCount number of address unit values
     * @param hoursCount number of hours of operation entries
     * @param ownerNamesCount number of owner names
     * @param payeeCount number of payee numbers
     * @param facilityRelationshipCount number of facility relationships
     * @param organizationRelationshipCount number of organization relationships
     * @param individualRelationshipCount number of individual relationships
     * @return this config */
    public OrganizationMaintainConfig withAllAttributes(int noteCount, int statusCount, int addressUnitCount, int hoursCount, int ownerNamesCount, int payeeCount, int facilityRelationshipCount, int organizationRelationshipCount, int individualRelationshipCount) {
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
        withFacilityRelationships(facilityRelationshipCount);
        withOrganizationRelationships(organizationRelationshipCount);
        withIndividualRelationships(individualRelationshipCount);
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
