package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import java.util.List;

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

    // Address variation: choose among several cities/postals; street number randomized
    private static final List<String[]> ADDRESS_POOLS = List.of(
        new String[]{"Blanshard St", "Victoria", "V8W 3C8"},
        new String[]{"Georgia St", "Vancouver", "V6B 0N7"},
        new String[]{"Kingsway", "Burnaby", "V5H 2A1"},
        new String[]{"Broadway Ave", "Williams Lake", "V2G 2X8"},
        new String[]{"Granville St", "Prince George", "V2L 2Z1"}
    );
    private static final int FIXED_LOWER_STREET_NUMBER = 2000;
    private static final int FIXED_UPPER_STREET_NUMBER = 10000;

    private OrganizationDataGenerator() { /* singleton */ }

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

    @Override
    public String generateName() {
        return pick(NAME_ROOTS) + " " + pick(NAME_SUFFIXES);
    }


    @Override
    public String[] generateAddress() {
        String[] base = pick(ADDRESS_POOLS);
        int streetNum = FIXED_LOWER_STREET_NUMBER + RNG.nextInt(FIXED_UPPER_STREET_NUMBER - FIXED_LOWER_STREET_NUMBER + 1); // inclusive upper bound
        return new String[]{ streetNum + " " + base[0], base[1], base[2] };
    }
}
