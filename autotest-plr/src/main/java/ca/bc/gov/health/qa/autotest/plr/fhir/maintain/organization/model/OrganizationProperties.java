package ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Organization-specific properties used by maintain flows and UI automation.
 * Single-valued properties are represented as enums or scalars, while
 * multi-valued attributes are exposed as lists with defensive copying.
 */
public class OrganizationProperties {

    private ClinicServices clinicServices = null;
    private ClinicOwnerBusinessType clinicOwnerBusinessType = null;
    private ClinicType clinicType = null;
    private String clinicLegalBusinessName = null;
    private List<String> addressUnit = new ArrayList<>(); //TODO - Issues with address unit. Implement logic to query parse and use in maintain later
    private List<String> clinicHoursOfOperation = new ArrayList<>();
    private List<String> clinicOwnerNames = new ArrayList<>();
    private List<String> payeeNumber = new ArrayList<>();
    private Boolean pciFlag = null;

    /**
     * Gets the clinic services model.
     * @return clinic services or null
     */
    public ClinicServices getClinicServices() { return clinicServices; }
    /**
     * Sets the clinic services model.
     * @param clinicServices services value
     */
    public void setClinicServices(ClinicServices clinicServices) { this.clinicServices = clinicServices; }

    /**
     * Gets the legal business name of the clinic.
     * @return legal business name or null
     */
    public String getClinicLegalBusinessName() { return clinicLegalBusinessName; }
    /**
     * Sets the legal business name of the clinic.
     * @param clinicLegalBusinessName name value
     */
    public void setClinicLegalBusinessName(String clinicLegalBusinessName) { this.clinicLegalBusinessName = clinicLegalBusinessName; }

    /**
     * Gets the business ownership type.
     * @return ownership type or null
     */
    public ClinicOwnerBusinessType getClinicOwnerBusinessType() { return clinicOwnerBusinessType; }
    /**
     * Sets the business ownership type.
     * @param clinicOwnerBusinessType ownership type
     */
    public void setClinicOwnerBusinessType(ClinicOwnerBusinessType clinicOwnerBusinessType) { this.clinicOwnerBusinessType = clinicOwnerBusinessType; }

    /**
     * Gets the clinic type classification.
     * @return clinic type or null
     */
    public ClinicType getClinicType() { return clinicType; }
    /**
     * Sets the clinic type classification.
     * @param clinicType type value
     */
    public void setClinicType(ClinicType clinicType) { this.clinicType = clinicType; }

    /**
     * Returns a defensive copy of address unit values.
     * @return unmodifiable copy of address unit entries
     */
    public List<String> getAddressUnit() { return List.copyOf(addressUnit); }
    /**
     * Replaces address unit values; makes a defensive copy.
     * @param addressUnit list of unit strings (nullable)
     */
    public void setAddressUnit(List<String> addressUnit) {
        this.addressUnit = addressUnit != null ? new ArrayList<>(addressUnit) : new ArrayList<>();
    }

    /**
     * Returns a defensive copy of clinic hours of operation.
     * @return unmodifiable copy of hours entries
     */
    public List<String> getClinicHoursOfOperation() { return List.copyOf(clinicHoursOfOperation); }
    /**
     * Replaces clinic hours of operation; makes a defensive copy.
     * @param clinicHoursOfOperation list of hours strings (nullable)
     */
    public void setClinicHoursOfOperation(List<String> clinicHoursOfOperation) {
        this.clinicHoursOfOperation = clinicHoursOfOperation != null ? new ArrayList<>(clinicHoursOfOperation) : new ArrayList<>();
    }

    /**
     * Returns a defensive copy of clinic owner names.
     * @return unmodifiable copy of owner names
     */
    public List<String> getClinicOwnerNames() { return List.copyOf(clinicOwnerNames); }
    /**
     * Replaces clinic owner names; makes a defensive copy.
     * @param clinicOwnerNames list of names (nullable)
     */
    public void setClinicOwnerNames(List<String> clinicOwnerNames) {
        this.clinicOwnerNames = clinicOwnerNames != null ? new ArrayList<>(clinicOwnerNames) : new ArrayList<>();
    }

    /**
     * Returns a defensive copy of payee numbers.
     * @return unmodifiable copy of payee numbers
     */
    public List<String> getPayeeNumber() { return List.copyOf(payeeNumber); }
    /**
     * Replaces payee numbers; makes a defensive copy.
     * @param payeeNumber list of payee numbers (nullable)
     */
    public void setPayeeNumber(List<String> payeeNumber) {
        this.payeeNumber = payeeNumber != null ? new ArrayList<>(payeeNumber) : new ArrayList<>();
    }

    /**
     * Indicates whether the PCI flag is set.
     * Returns false when unset (null-safe).
     * @return true if PCI flag is explicitly set to true
     */
    public Boolean getPciFlag() { return pciFlag; }
    /**
     * Sets the PCI flag.
     * @param pciFlag flag value
     */
    public void setPciFlag(boolean pciFlag) { this.pciFlag = pciFlag; }
}