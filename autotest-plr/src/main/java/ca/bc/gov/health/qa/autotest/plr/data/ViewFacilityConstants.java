package ca.bc.gov.health.qa.autotest.plr.data;

import java.util.Map;

public class ViewFacilityConstants {

    public static final String DATA_SOURCE_DEFAULT = "PLR-QA-REGADMIN@00002855";
    public static final String DATA_OWNER_CODE_DEFAULT = "MOH";

    public static final String IDENTIFIER_FACILITY_TYPE_DEFAULT = "BUILDING";
    public static final String IDENTIFIER_IDENTIFIER_TYPE_DEFAULT = "IFC";

    public static final String NAME_EFFECTIVE_FROM_DEFAULT = "2023-01-01"; //TODO verify this

    public static final Map<String,String> orgFacMap = Map.of(
            "Located at (LOCATED)", "Location of (LOCATION)",
            "Location of (LOCATION)", "Located at (LOCATED)");

    /** The fields available in the identifier data blocks. */
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

    /** The fields available in the name data blocks. */
    public enum NameField
    {
        NAME("Name"),
        DESCRIPTION("Description"),
        EFFECTIVE_FROM("Effective From"),
        EFFECTIVE_TO("Effective To"),
        END_REASON("End Reason"),
        DATA_SOURCE("Data Source"),
        DB_CREATED("DB Created"),
        DB_EXPIRED("DB Expired"),
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        NameField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

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

    /** The fields available in the note data blocks. */
    public enum NoteField
    {
        NOTE_IDENTIFIER("Note Identifier"),
        NOTE_TEXT("Note Text"),
        EFFECTIVE_FROM("Effective From"),
        EFFECTIVE_TO("Effective To"),
        END_REASON("End Reason"),
        DATA_SOURCE("Data Source"),
        DB_CREATED("DB Created"),
        DB_EXPIRED("DB Expired"),
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        NoteField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the organization relationship data blocks. */
    public enum OrgRelationshipField
    {
        RELATIONSHIP_IDENTIFIER("Relationship Identifier"),
        RELATIONSHIP_TYPE("Relationship Type"),
        RELATED_ORGANIZATION_NAME("Related Organization Name"),
        RELATED_ORGANIZATION_IDENTIFIER("Related Organization Identifier"),
        EFFECTIVE_FROM("Effective From"),
        EFFECTIVE_TO("Effective To"),
        END_REASON("End Reason"),
        DATA_SOURCE("Data Source"),
        DB_CREATED("DB Created"),
        DB_EXPIRED("DB Expired"),
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        OrgRelationshipField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the telecommunication data blocks. */
    public enum TelecomField
    {
        TYPE("Type"),
        PURPOSE("Purpose"),
        AREA_CODE("Area Code"),
        NUMBER("Number"),
        EXTENSION("Extension"),
        EFFECTIVE_FROM("Effective From"),
        EFFECTIVE_TO("Effective To"),
        END_REASON("End Reason"),
        DATA_SOURCE("Data Source"),
        DB_CREATED("DB Created"),
        DB_EXPIRED("DB Expired"),
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        TelecomField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the electronic address data blocks. */
    public enum EAddressField
    {
        TYPE("Type"),
        PURPOSE("Purpose"),
        ADDRESS("Address"),
        EFFECTIVE_FROM("Effective From"),
        EFFECTIVE_TO("Effective To"),
        END_REASON("End Reason"),
        DATA_SOURCE("Data Source"),
        DB_CREATED("DB Created"),
        DB_EXPIRED("DB Expired"),
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        EAddressField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }
}
