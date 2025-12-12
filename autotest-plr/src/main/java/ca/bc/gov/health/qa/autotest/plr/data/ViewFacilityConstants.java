package ca.bc.gov.health.qa.autotest.plr.data;

public class ViewFacilityConstants {
    /** The fields available in the civic address data blocks. */
    public enum CivicAddressField
    {
        LATITUDE("Latitude"),
        LONGITUDE("Longitude"),
        ADDRESS_LINE_1("Address Line 1"),
        ADDRESS_LINE_2("Address Line 2"),
        ADDRESS_LINE_3("Address Line 3"),
        CITY("City"),
        PROVINCE_STATE("Province / State"),
        COUNTRY("Country"),
        HEALTH_AUTHORITY("Health Authority"),
        HEALTH_SERVICE_DELIVERY_AREA("Health Service Delivery Area"),
        LOCAL_HEALTH_AREA("Local Health Area"),
        PRIMARY_CARE_NETWORK("Primary Care Network"),
        COMMUNITY_HEALTH_SERVICE_AREA("Community Health Service Area");

        private final String fieldString;

        CivicAddressField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the other address data blocks. */
    public enum OtherAddressField
    {
        VALIDATION_STATUS("Validation Status"),
        ADDRESS_TYPE("Address Type"),
        ADDRESS_PURPOSE("Address Purpose"),
        ADDRESS_LINE_1("Address Line 1"),
        ADDRESS_LINE_2("Address Line 2"),
        ADDRESS_LINE_3("Address Line 3"),
        CITY("City"),
        STATE_PROV("State/Prov"),
        POSTAL_ZIP_CODE("Postal/Zip Code"),
        COUNTRY("Country"),
        EFFECTIVE_FROM("Effective From"),
        EFFECTIVE_TO("Effective To"),
        END_REASON("End Reason"),
        DATA_SOURCE("Data Source"),
        DB_CREATED("DB Created"),
        DB_EXPIRED("DB Expired"),
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        OtherAddressField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** */
    public enum IdentifierField
    {
        FACILITY_TYPE("Facility Type"),
        IDENTIFIER("Identifier"),
        IDENTIFIER_TYPE("Identifier Type"),
        EFFECTIVE_FROM("Effective From"),
        EFFECTIVE_TO("Effective To"),
        END_REASON("End Reason"),
        DATA_SOURCE("Data Source"),
        DB_CREATED("DB Created"),
        DB_EXPIRED("DB Expired"),
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        IdentifierField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }
}
