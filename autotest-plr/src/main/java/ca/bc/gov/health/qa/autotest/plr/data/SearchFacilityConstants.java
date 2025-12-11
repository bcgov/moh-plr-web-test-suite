package ca.bc.gov.health.qa.autotest.plr.data;

public class SearchFacilityConstants {
    /** Expected columns of table of search results  */
    public enum TableColumn
    {
        FACILITY_NAME("Facility Name", 0),
        IDENTIFIER("Identifier", 1),
        CIVIC_ADDRESS("Civic Address", 2);

        private final String columnName;
        private final int columnIndex;

        TableColumn(String columnName, int columnIndex)
        {
            this.columnName = columnName; this.columnIndex = columnIndex;
        }

        public String getName() { return columnName; }
        public int getIndex() { return columnIndex; }
    }

    /** Details and fields to appear in the Search by Identifier tab, listed in order. */
    public enum IdentifierTabAttribute
    {
        MANDATORY_FIELD("(*) Indicates a mandatory field.", 0),
        FACILITY_IDENTIFIER_TYPE("Facility Identifier Type*", 1),
        FACILITY_IDENTIFIER("Facility Identifier*", 2),
        SEARCH_BUTTON("Search", 3);

        private final String attributeString;
        private final int attributeIndex;

        IdentifierTabAttribute(String attributeString, int attributeIndex)
        {
            this.attributeString = attributeString; this.attributeIndex = attributeIndex;
        }

        public String getString() { return attributeString; }
        public int getIndex() { return attributeIndex; }
    }
}
