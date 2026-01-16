package ca.bc.gov.health.qa.autotest.plr.data;

public class ViewProviderConstants {
    /** The fields available in the facility relationship data blocks. */
    public enum FacRelationshipField
    {
        /** Relationship Identifier */
        RELATIONSHIP_IDENTIFIER("Relationship Identifier"),
        /** Relationship Type */
        RELATIONSHIP_TYPE("Relationship Type"),
        /** Related Facility Name */
        RELATED_FACILITY_NAME("Related Facility Name"),
        /** Facility Type */
        FACILITY_TYPE("Facility Type"),
        /** Related Facility Identifier */
        RELATED_FACILITY_IDENTIFIER("Related Facility Identifier"),
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

        FacRelationshipField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the identifier data blocks */
    public enum IdentifierField
    {
        /** Identifier */
        IDENTIFIER("Identifier"),
        /** Type */
        TYPE("Type"),
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

    /** The fields available in the name data blocks */
    public enum NameField
    {
        /** Name Type */
        NAME_TYPE("Name Type"),
        /** Preferred */
        PREFERRED("Preferred"),
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
}
