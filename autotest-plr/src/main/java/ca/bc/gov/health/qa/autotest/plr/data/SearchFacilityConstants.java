package ca.bc.gov.health.qa.autotest.plr.data;

import java.util.List;

/** Class containing constants related to the Search Facility page */
public class SearchFacilityConstants {
    /** Service Delivery Area example alongside prefixes to be used for
     * displaying/filling the service delivery area field */
    public static final String sdaExpected = "South Vancouver Island (HSDA)";
    public static final String sdaPrefix1 = sdaExpected.substring(0, 6);
    public static final String sdaPrefix2 = sdaExpected.substring(0, 8);
    /** "Name" displayed in the search results a facility has no name */
    public static final String noNameFacility = "Link to View Facility";
    /** Message that appears when search results return zero results */
    public static final String zeroResultsMessage = "No records found.";
    /** Maximum search results expected on a page */
    public static final int maximumResults = 20;

    /** Expected columns of table of search results  */
    public enum TableColumn
    {
        /** Facility Name */
        FACILITY_NAME("Facility Name", 0),
        /** Identifier */
        IDENTIFIER("Identifier", 1),
        /** Civic Address */
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
        /** Mandatory Field detail */
        MANDATORY_FIELD("(*) Indicates a mandatory field.", 0),
        /** Facility Identifier Type field */
        FACILITY_IDENTIFIER_TYPE("Facility Identifier Type*", 1),
        /** Facility Identifier field */
        FACILITY_IDENTIFIER("Facility Identifier*", 2),
        /** Search button */
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

    /** Details and fields to appear in the Search by Criteria tab, listed in order. */
    public enum CriteriaTabAttribute
    {
        /** Criteria Instruction detail */
        CRITERIA_INSTRUCTION("At least 1 search criteria must be entered.", 0),
        /** Facility Name field */
        FACILITY_NAME("Facility Name", 1),
        /** Civic Address field */
        CIVIC_ADDRESS("Civic Address Line 1", 2),
        /** Other Address field */
        OTHER_ADDRESS("Other Address Line 1", 3),
        /** City field */
        CITY("City", 4),
        /** Facility Type field */
        FACILITY_TYPE("Facility Type", 5),
        /** Service Delivery Area field */
        SERVICE_DELIVERY_AREA("Service Delivery Area", 6),
        /** Clear button */
        CLEAR_BUTTON("Clear", 7),
        /** Search button */
        SEARCH_BUTTON("Search", 8);

        private final String attributeString;
        private final int attributeIndex;
        public static final List<CriteriaTabAttribute> fieldList = List.of(
                FACILITY_NAME, CIVIC_ADDRESS, OTHER_ADDRESS, CITY, FACILITY_TYPE, SERVICE_DELIVERY_AREA);
        public static final List<CriteriaTabAttribute> queryFieldList = List.of(
                FACILITY_NAME, CIVIC_ADDRESS, OTHER_ADDRESS, CITY, CITY, FACILITY_TYPE, SERVICE_DELIVERY_AREA);

        CriteriaTabAttribute(String attributeString, int attributeIndex)
        {
            this.attributeString = attributeString; this.attributeIndex = attributeIndex;
        }

        public String getString() { return attributeString; }
        public int getIndex() { return attributeIndex; }
    }
}
