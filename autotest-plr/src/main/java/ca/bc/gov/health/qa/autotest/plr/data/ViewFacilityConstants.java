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
        /** Facility Type */
        FACILITY_TYPE("Facility Type"),
        /** Identifier */
        IDENTIFIER("Identifier"),
        /** Identifier Type */
        IDENTIFIER_TYPE("Identifier Type"),
        /** Effective From */
        EFFECTIVE_FROM("Effective From"),
        /** Effective To */
        EFFECTIVE_TO("Effective To"),
        /** End Reason */
        END_REASON("End Reason"),
        /** Data Source */
        DATA_SOURCE("Data Source"),
        /** DB Created */
        DB_CREATED("DB Created"),
        /** DB Expired */
        DB_EXPIRED("DB Expired"),
        /** Data Owner Code */
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        IdentifierField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the name data blocks. */
    public enum NameField
    {
        /** Name */
        NAME("Name"),
        /** Description */
        DESCRIPTION("Description"),
        /** Effective From */
        EFFECTIVE_FROM("Effective From"),
        /** Effective To */
        EFFECTIVE_TO("Effective To"),
        /** End Reason */
        END_REASON("End Reason"),
        /** Data Source */
        DATA_SOURCE("Data Source"),
        /** DB Created */
        DB_CREATED("DB Created"),
        /** DB Expired */
        DB_EXPIRED("DB Expired"),
        /** Data Owner Code */
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        NameField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the civic address data blocks. */
    public enum CivicAddressField
    {
        /** Latitude */
        LATITUDE("Latitude"),
        /** Longitude */
        LONGITUDE("Longitude"),
        /** Address Line 1 */
        ADDRESS_LINE_1("Address Line 1"),
        /** Address Line 2 */
        ADDRESS_LINE_2("Address Line 2"),
        /** Address Line 3 */
        ADDRESS_LINE_3("Address Line 3"),
        /** City */
        CITY("City"),
        /** Province / State */
        PROVINCE_STATE("Province / State"),
        /** Country */
        COUNTRY("Country"),
        /** Health Authority */
        HEALTH_AUTHORITY("Health Authority"),
        /** Health Service Delivery Area */
        HEALTH_SERVICE_DELIVERY_AREA("Health Service Delivery Area"),
        /** Local Health Area */
        LOCAL_HEALTH_AREA("Local Health Area"),
        /** Primary Care Network */
        PRIMARY_CARE_NETWORK("Primary Care Network"),
        /** Community Health Service Area */
        COMMUNITY_HEALTH_SERVICE_AREA("Community Health Service Area");

        private final String fieldString;

        CivicAddressField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the other address data blocks. */
    public enum OtherAddressField
    {
        /** Validation Status */
        VALIDATION_STATUS("Validation Status"),
        /** Address Type */
        ADDRESS_TYPE("Address Type"),
        /** Address Purpose */
        ADDRESS_PURPOSE("Address Purpose"),
        /** Address Line 1 */
        ADDRESS_LINE_1("Address Line 1"),
        /** Address Line 2 */
        ADDRESS_LINE_2("Address Line 2"),
        /** Address Line 3 */
        ADDRESS_LINE_3("Address Line 3"),
        /** City */
        CITY("City"),
        /** State / Province */
        STATE_PROV("State/Prov"),
        /** Postal / Zip Code */
        POSTAL_ZIP_CODE("Postal/Zip Code"),
        /** Country */
        COUNTRY("Country"),
        /** Effective From */
        EFFECTIVE_FROM("Effective From"),
        /** Effective To */
        EFFECTIVE_TO("Effective To"),
        /** End Reason */
        END_REASON("End Reason"),
        /** Data Source */
        DATA_SOURCE("Data Source"),
        /** DB Created */
        DB_CREATED("DB Created"),
        /** DB Expired */
        DB_EXPIRED("DB Expired"),
        /** Data Owner Code */
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        OtherAddressField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the note data blocks. */
    public enum NoteField
    {
        /** Note Identifier */
        NOTE_IDENTIFIER("Note Identifier"),
        /** Note Text */
        NOTE_TEXT("Note Text"),
        /** Effective From */
        EFFECTIVE_FROM("Effective From"),
        /** Effective To */
        EFFECTIVE_TO("Effective To"),
        /** End Reason */
        END_REASON("End Reason"),
        /** Data Source */
        DATA_SOURCE("Data Source"),
        /** DB Created */
        DB_CREATED("DB Created"),
        /** DB Expired */
        DB_EXPIRED("DB Expired"),
        /** Data Owner Code */
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        NoteField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the organization relationship data blocks. */
    public enum OrgRelationshipField
    {
        /** Relationship Identifier */
        RELATIONSHIP_IDENTIFIER("Relationship Identifier"),
        /** Relationship Type */
        RELATIONSHIP_TYPE("Relationship Type"),
        /** Related Organization Name */
        RELATED_ORGANIZATION_NAME("Related Organization Name"),
        /** Related Organization Identifier */
        RELATED_ORGANIZATION_IDENTIFIER("Related Organization Identifier"),
        /** Effective From */
        EFFECTIVE_FROM("Effective From"),
        /** Effective To */
        EFFECTIVE_TO("Effective To"),
        /** End Reason */
        END_REASON("End Reason"),
        /** Data Source */
        DATA_SOURCE("Data Source"),
        /** DB Created */
        DB_CREATED("DB Created"),
        /** DB Expired */
        DB_EXPIRED("DB Expired"),
        /** Data Owner Code */
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        OrgRelationshipField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the telecommunication data blocks. */
    public enum TelecomField
    {
        /** Type */
        TYPE("Type"),
        /** Purpose */
        PURPOSE("Purpose"),
        /** Area Code */
        AREA_CODE("Area Code"),
        /** Number */
        NUMBER("Number"),
        /** Extension */
        EXTENSION("Extension"),
        /** Effective From */
        EFFECTIVE_FROM("Effective From"),
        /** Effective To */
        EFFECTIVE_TO("Effective To"),
        /** End Reason */
        END_REASON("End Reason"),
        /** Data Source */
        DATA_SOURCE("Data Source"),
        /** DB Created */
        DB_CREATED("DB Created"),
        /** DB Expired */
        DB_EXPIRED("DB Expired"),
        /** Data Owner Code */
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        TelecomField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the electronic address data blocks. */
    public enum EAddressField
    {
        /** Type */
        TYPE("Type"),
        /** Purpose */
        PURPOSE("Purpose"),
        /** Address */
        ADDRESS("Address"),
        /** Effective From */
        EFFECTIVE_FROM("Effective From"),
        /** Effective To */
        EFFECTIVE_TO("Effective To"),
        /** End Reason */
        END_REASON("End Reason"),
        /** Data Source */
        DATA_SOURCE("Data Source"),
        /** DB Created */
        DB_CREATED("DB Created"),
        /** DB Expired */
        DB_EXPIRED("DB Expired"),
        /** Data Owner Code */
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        EAddressField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }
}
