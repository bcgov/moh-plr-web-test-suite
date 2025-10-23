package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import java.util.List;

/**
 * Facility-specific data generator extending {@link AbstractDataGenerator}.
 * Provides distinct name/address pools for facilities. Singleton pattern retained.
 */
public final class FacilityDataGenerator extends AbstractDataGenerator {

    private static volatile FacilityDataGenerator instance; // Double-checked locking

    private static final List<String> NAME_PREFIXES = List.of(
        "North", "South", "East", "West", "Central", "Fraser", "Coastal", "Interior", "Island", "Northern", "Community", "General", "Regional", "Valley", "Mountain", "Harbour", "Riverside", "Lakeside", "Prairie", "Metro"
    );
    private static final List<String> NAME_SUFFIXES = List.of(
        "Health Centre", "Medical Clinic", "Hospital", "Outpatient Centre", "Urgent Care", "Primary Care Clinic"
    );

    // Address fixed except for street number.
    private static final String FIXED_STREET = "Richter St"; // Spelling per specification
    private static final String FIXED_CITY = "Kelowna";
    private static final String FIXED_POSTAL = "V1Y 2J6";
    private static final int FIXED_LOWER_STREET_NUMBER = 700;
    private static final int FIXED_UPPER_STREET_NUMBER = 3000;

    private FacilityDataGenerator() { /* enforce singleton */ }

    /** Returns the singleton instance (lazy initialized, thread-safe). 
     * @return FacilityDataGenerator instance
    */
    public static FacilityDataGenerator getInstance() {
        FacilityDataGenerator result = instance;
        if (result == null) {
            synchronized (FacilityDataGenerator.class) {
                result = instance;
                if (result == null) {
                    result = new FacilityDataGenerator();
                    instance = result;
                }
            }
        }
        return result;
    }

    /** Generate a facility name using random prefix + suffix. 
     * @return generated name string
    */
    @Override
    public String generateName() {
        return pick(NAME_PREFIXES) + " " + pick(NAME_SUFFIXES);
    }

    /** Generate facility address components. 
     * @return generated address array
    */
    @Override
    public String[] generateAddress() {
        int number = FIXED_LOWER_STREET_NUMBER + RNG.nextInt(FIXED_UPPER_STREET_NUMBER - FIXED_LOWER_STREET_NUMBER + 1);
        return new String[]{ number + " " + FIXED_STREET, FIXED_CITY, FIXED_POSTAL };
    }

}

