package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * TODO (AZ) - doc
 */
public class ProviderDataFields
{
    private static final Map<ProviderSection,List<String>> SORT_KEY_MAP;
    static
    {
        Map<ProviderSection,List<String>> map = new EnumMap<>(ProviderSection.class);
        map.put(ProviderSection.REGISTRY_IDENTIFIERS,        List.of("Type"));
        map.put(ProviderSection.IDENTIFIERS,                 List.of("Type"));
        map.put(ProviderSection.STATUSES,                    List.of());
        map.put(ProviderSection.EXPERTISE,                   List.of("Type"));
        map.put(ProviderSection.CREDENTIALS,                 List.of("Credential Type"));
        map.put(ProviderSection.ORGANIZATION_NAMES,          List.of("Name Type"));
        map.put(ProviderSection.PRACTITIONER_NAMES,          List.of("Name Type"));
        map.put(ProviderSection.ADDRESSES,                   List.of("Address Type",
                                                                     "Address Purpose"));
        map.put(ProviderSection.TELECOMMUNICATIONS,          List.of("Type", "Purpose"));
        map.put(ProviderSection.ELECTRONIC_ADDRESSES,        List.of("Type", "Purpose"));
        map.put(ProviderSection.DEMOGRAPHICS,                List.of());
        map.put(ProviderSection.WORK_LOCATIONS,              List.of("Identifier"));
        map.put(ProviderSection.CONDITIONS,                  List.of("Type"));
        map.put(ProviderSection.DISCIPLINARY_ACTIONS,        List.of("Identifier"));
        map.put(ProviderSection.COMMUNICATION_PREFERENCE,    List.of("Content Type"));
        map.put(ProviderSection.CONFIDENTIALITY,             List.of());
        map.put(ProviderSection.PROVIDER_RELATIONSHIPS,      List.of("Relationship Type"));
        map.put(ProviderSection.FACILITY_RELATIONSHIPS,      List.of("Relationship Type"));
        map.put(ProviderSection.REGISTRY_USER_RELATIONSHIPS, List.of("Type"));
        map.put(ProviderSection.NOTES,                       List.of("Note Identifier"));
        map.put(ProviderSection.ORGANIZATION_PROPERTIES,     List.of());
        SORT_KEY_MAP = Collections.unmodifiableMap(map);
    }

    private static final Map<ProviderSection,List<String>> SECTION_FIELD_NAME_MAP;
    static
    {
        Map<ProviderSection,List<String>> map = new EnumMap<>(ProviderSection.class);
        map.put(ProviderSection.REGISTRY_IDENTIFIERS, List.of(
                "Identifier",
                "Type",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired"));
        map.put(ProviderSection.IDENTIFIERS, List.of(
                "Identifier",
                "Type",
                "Role Type",
                "Hds Type",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.STATUSES, List.of(
                "Type",
                "Class",
                "Reason",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.EXPERTISE, List.of(
                "Type",
                "Source's Code",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.CREDENTIALS, List.of(
                "Credential Type",
                "Designation",
                "Registration Number",
                "Granting Institution",
                "Institution City",
                "Institution Prov/State",
                "Institution Country",
                "Year Issued",
                "Equivalency Flag",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.ORGANIZATION_NAMES, List.of(
                "Name Type",
                "Preferred",
                "Name",
                "Description",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.PRACTITIONER_NAMES, List.of(
                "Name Type",
                "Prefix",
                "First Name",
                "Second Name",
                "Third Name",
                "Surname",
                "Suffix",
                "Preferred",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.ADDRESSES, List.of(
                "Validation Status",
                "Address Type",
                "Address Purpose",
                "Address Line 1",
                "Address Line 2",
                "Address Line 3",
                "City",
                "State/Prov",
                "Postal/Zip Code",
                "Country",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.TELECOMMUNICATIONS, List.of(
                "Type",
                "Purpose",
                "Area Code",
                "Number",
                "Extension",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.ELECTRONIC_ADDRESSES, List.of(
                "Type",
                "Purpose",
                "Address",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.DEMOGRAPHICS, List.of(
                "Birth Date",
                "Death Date",
                "Birth Country",
                "Birth Prov/State",
                "Gender",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.WORK_LOCATIONS, List.of(
                "Identifier",
                "DB Created",
                "Data Source",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.CONDITIONS, List.of(
                "Type",
                "Identifier",
                "Restriction Flag",
                "Restriction Explanation Text",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.DISCIPLINARY_ACTIONS, List.of(
                "Identifier",
                "Description",
                "Display Flag",
                "Archive Date",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.COMMUNICATION_PREFERENCE, List.of(
                "Work Location Identifier",
                "Content Type",
                "Address Type",
                "Telecommunication Number Type",
                "Electronic Address Type",
                "Communication Purpose",
                "Communication Data Owner Code",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.CONFIDENTIALITY, List.of(
                "Confidentiality Flag",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.PROVIDER_RELATIONSHIPS, List.of(
                "Relationship Identifier",
                "Relationship Type",
                "Related Provider Name",
                "Role of Related Provider",
                "Related Provider Identifier",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.FACILITY_RELATIONSHIPS, List.of(
                "Relationship Identifier",
                "Relationship Type",
                "Related Facility Name",
                "Facility Type",
                "Related Facility Identifier",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.REGISTRY_USER_RELATIONSHIPS, List.of(
                "Type",
                "Registry User ID",
                "Registry User Type",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.NOTES, List.of(
                "Note Identifier",
                "Note Text",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        map.put(ProviderSection.ORGANIZATION_PROPERTIES, List.of(
                "Property Type",
                "Property Value",
                "Effective From",
                "Effective To",
                "End Reason",
                "Data Source",
                "DB Created",
                "DB Expired",
                "Data Owner Code"));
        SECTION_FIELD_NAME_MAP = Collections.unmodifiableMap(map);
    }

    private ProviderDataFields()
    {}

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @return ???
     */
    public static List<String> getFieldNameList(ProviderSection section)
    {
        return SECTION_FIELD_NAME_MAP.get(section);
    }

    /**
     * TODO (AZ) - doc
     *
     * @param section
     *        ???
     *
     * @return ???
     */
    public static List<String> getSortKey(ProviderSection section)
    {
        return SORT_KEY_MAP.get(section);
    }
}
