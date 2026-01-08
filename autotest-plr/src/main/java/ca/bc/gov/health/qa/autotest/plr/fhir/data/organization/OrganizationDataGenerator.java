package ca.bc.gov.health.qa.autotest.plr.fhir.data.organization;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import ca.bc.gov.health.qa.autotest.plr.fhir.data.AbstractDataGenerator;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.HdsType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.OrgRoleType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.ClinicOwnerBusinessType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.ClinicServices;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.organization.model.ClinicType;

/**
 * Organization-specific data generator providing distinct name and address pools
 * while reusing shared telecom/identifier/note logic from {@link AbstractDataGenerator}.
 */
public final class OrganizationDataGenerator extends AbstractDataGenerator {

    private static volatile OrganizationDataGenerator instance;

    // Modest distinct name components vs facility
    private static final List<String> NAME_ROOTS = List.of(
        "Provincial", "Health Authority", "Services", "Network", "Alliance", "Community Health", "Care Group", "Wellness", "Outreach", "Support"
    );
    private static final List<String> NAME_SUFFIXES = List.of(
        "Organization", "Group", "Services", "Association", "Collective", "Consortium"
    );

    private static final List<String> HOURS_DAYS = Arrays.asList("MON", "TUE", "WED", "THU", "FRI");

    // Address variation: choose among several cities/postals; street number randomized
    private static final List<String[]> ADDRESS_POOLS = List.of(
        new String[]{"Blanshard St", "Victoria", "V8W 3C8"},
        new String[]{"Georgia St", "Vancouver", "V6B 0N7"},
        new String[]{"Kingsway", "Burnaby", "V5H 2A1"},
        new String[]{"Broadway Ave", "Williams Lake", "V2G 2X8"},
        new String[]{"Granville St", "Prince George", "V2L 2Z1"}
    );

    //HDS possible types
    private static final List<HdsType> HDS_TYPES = List.of(
            HdsType.CLINIC,
            HdsType.PHARMACY,
            HdsType.HOSPITAL,
            HdsType.EMERGENCY,
            HdsType.LAB,
            HdsType.GENERAL_CARE,
            HdsType.INPATIENT,
            HdsType.HOUSING,
            HdsType.OUTPATIENT
        );

    private static final int FIXED_LOWER_STREET_NUMBER = 2000;
    private static final int FIXED_UPPER_STREET_NUMBER = 10000;

    private OrganizationDataGenerator() { /* singleton */ }

    /**
     * Returns the singleton instance (lazy initialized, thread-safe).
     * @return OrganizationDataGenerator instance
     */
    public static OrganizationDataGenerator getInstance() {
        OrganizationDataGenerator result = instance;
        if (result == null) {
            synchronized (OrganizationDataGenerator.class) {
                result = instance;
                if (result == null) {
                    result = new OrganizationDataGenerator();
                    instance = result;
                }
            }
        }
        return result;
    }

    /**
     * Returns a randomly selected HDS classification value from the predefined list.
     * @return random HDS type string
     */
    public HdsType randomHdsType() {
        return pick(HDS_TYPES);
    }

    /**
     * Returns a randomly selected {@link OrgRoleType} value.
     * @return randomly chosen OrgRoleType
     */
    public OrgRoleType randomOrgRoleType() {
        return pick(List.of(OrgRoleType.values()));
    }


    /** Generate a random organization name using distinct root + suffix. 
     * @return generated name string
    */
    @Override
    public String generateName() {
        return pick(NAME_ROOTS) + " " + pick(NAME_SUFFIXES);
    }


    /** Generate organization address components. 
     * @return generated address array
    */
    @Override
    public String[] generateAddress() {
        String[] base = pick(ADDRESS_POOLS);
        int streetNum = FIXED_LOWER_STREET_NUMBER + RNG.nextInt(FIXED_UPPER_STREET_NUMBER - FIXED_LOWER_STREET_NUMBER + 1); // inclusive upper bound
        return new String[]{ streetNum + " " + base[0], base[1], base[2] };
    }

    // ----------------------- Organization Properties Generators -------------------------------

    /**
     * Returns a random clinic services value.
     * @return a random {@link ClinicServices}
     */
    public ClinicServices randomClinicServices() { return pick(List.of(ClinicServices.values())); }

    /**
     * Returns a random clinic owner business type value.
     * @return a random {@link ClinicOwnerBusinessType}
     */
    public ClinicOwnerBusinessType randomClinicOwnerBusinessType() { return pick(List.of(ClinicOwnerBusinessType.values())); }

    /**
     * Returns a random clinic type value.
     * @return a random {@link ClinicType}
     */
    public ClinicType randomClinicType() { return pick(List.of(ClinicType.values())); }

    /**
     * Generates a plausible legal business name for the clinic.
     * @return generated legal business name
     */
    public String generateClinicLegalBusinessName() {
        return "Legal Business " + generateNumericId().substring(0, 6);
    }

    /**
     * Generates a list of address unit strings.
     * @param count number of units to generate
     * @return list of address units
     */
    public List<String> generateAddressUnitList(int count) {
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int unitNum = 1 + RNG.nextInt(999);
            list.add("Unit " + unitNum);
        }
        return list;
    }

    /**
     * Generates a single address unit string (e.g., "Unit 123").
     * @return generated address unit string
     */
    public String generateAddressUnit() {
        int unitNum = 1 + RNG.nextInt(999);
        return "Unit " + unitNum;
    }

    /**
     * Generates a clinic hours entry in required format: DAY HH:MM-HH:MM.
     * Ensures 00:00-24:00 compliant time slot and start {@literal <} end.
     * @return formatted clinic hours entry
     */
    public String generateClinicHourEntry() {
        String day = pick(HOURS_DAYS);
        int startHour = 8 + RNG.nextInt(4);   // 08-11
        int endHour = 16 + RNG.nextInt(3);    // 16-18
        String start = String.format("%02d:%02d", startHour, RNG.nextBoolean() ? 0 : 30);
        String end   = String.format("%02d:%02d", endHour, RNG.nextBoolean() ? 0 : 30);
        return day + " " + start + "-" + end;
    }

    /**
     * Generates a list of clinic hours entries in required format.
     * @param count number of entries to generate
     * @return list of clinic hours entries
     */
    public List<String> generateClinicHoursOfOperationList(int count) {
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(generateClinicHourEntry());
        }
        return list;
    }

    /**
     * Generates a list of clinic owner names.
     * @param count number of names to generate
     * @return list of owner names
     */
    public List<String> generateClinicOwnerNamesList(int count) {
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add("Owner " + generateNumericId().substring(0, 4));
        }
        return list;
    }

    /**
     * Generates a single clinic owner name (e.g., "Owner 1234").
     * @return generated owner name
     */
    public String generateClinicOwnerName() {
        return "Owner " + generateNumericId().substring(0, 4);
    }

    /**
     * Generates a single payee number (e.g., "PAY123456").
     * @return generated payee number
     */
    public String generatePayeeNumber() {
        return "PAY" + generateNumericId().substring(0, 6);
    }

    /**
     * Randomizes PCI flag.
     * @return randomized PCI flag
     */
    public boolean generatePciFlag() { return RNG.nextBoolean(); }

    
}
