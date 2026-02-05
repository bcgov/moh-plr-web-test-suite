package ca.bc.gov.health.qa.autotest.plr.web.tests.model;

public enum OrganizationProperties {
    CLINIC_HOURS_OF_OPERATION("CLINIC_HOURS_OF_OPERATION - Clinic Hours of Operation", CssFieldType.TEXT_AREA),
    CLINIC_OWNER_BUSINESS_TYPE("CLINIC_OWNER_BUSINESS_TYPE - Clinic Owner Business Type", CssFieldType.DROPDOWN_LIST),
    CLINIC_SERVICE_DELIVERY_TYPE("CLINIC_SERVICES - Clinic Service Delivery Type", CssFieldType.DROPDOWN_LIST),
    CLINIC_TYPE("CLINIC_TYPE - Clinic Type", CssFieldType.DROPDOWN_LIST),
    PCI_FLAG("PCI_FLAG - PCI Flag", CssFieldType.CHECKBOX),
    CLINIC_OWNER_NAMES("CLINIC_OWNER_NAMES - Clinic Owner Name", CssFieldType.TEXT_FIELD),
    CLINIC_LEGAL_BUSINESS_NAME("CLINIC_LEGAL_BUSINESS_NAME - Clinic Legal Business Name", CssFieldType.TEXT_FIELD),
    PAYEE_NUMBER("PAYEE_NUMBER - Payee Number", CssFieldType.TEXT_FIELD),
    ADDRESS_UNIT("ADDRESS_UNIT - Unit Associated with Civic Address", CssFieldType.TEXT_FIELD),
    HDS_SUB_TYPE("HDS_SUB_TYPE - HDS Sub Type", CssFieldType.DROPDOWN_LIST);

    private final String displayName;
    private final CssFieldType fieldType;

    OrganizationProperties(String displayName, CssFieldType fieldType) {
        this.displayName = displayName;
        this.fieldType = fieldType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssField() {
        return fieldType.getCssField();
    }

    public CssFieldType getFieldType() {
        return fieldType;
    }
}
