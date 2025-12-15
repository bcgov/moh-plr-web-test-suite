package ca.bc.gov.health.qa.autotest.plr.data;

public class ViewProviderConstants {
    /** The fields available in the facility relationship data blocks. */
    public enum FacRelationshipField
    {
        RELATIONSHIP_IDENTIFIER("Relationship Identifier"),
        RELATIONSHIP_TYPE("Relationship Type"),
        RELATED_FACILITY_NAME("Related Facility Name"),
        FACILITY_TYPE("Facility Type"),
        RELATED_FACILITY_IDENTIFIER("Related Facility Identifier"),
        EFFECTIVE_FROM("Effective From"),
        EFFECTIVE_TO("Effective To"),
        END_REASON("End Reason"),
        DATA_SOURCE("Data Source"),
        DB_CREATED("DB Created"),
        DB_EXPIRED("DB Expired"),
        DATA_OWNER_CODE("Data Owner Code");

        private final String fieldString;

        FacRelationshipField(String fieldString) { this.fieldString = fieldString; }

        public String getString() { return fieldString; }
    }

    /** The fields available in the identifier data blocks */
    public enum IdentifierField
    {
        IDENTIFIER("Identifier"),
        TYPE("Type"),
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

    /** The fields available in the name data blocks */
    public enum NameField
    {
        NAME_TYPE("Name Type"),
        PREFERRED("Preferred"),
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
}
