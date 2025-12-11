package ca.bc.gov.health.qa.autotest.plr.data;

public class SearchFacilityConstants {
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
}
