package ca.bc.gov.health.qa.autotest.plr.fhir.data.individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;

import ca.bc.gov.health.qa.autotest.plr.fhir.data.AbstractDataGenerator;

/**
 * Practitioner (individual)-specific data generator extending {@link AbstractDataGenerator}.
 * Provides simple name and address generation plus helper methods for domain-specific codes.
 * Singleton pattern retained for consistency with other generators.
 */
public final class IndividualDataGenerator extends AbstractDataGenerator {

    private static volatile IndividualDataGenerator instance;

    private static final List<String> FAMILY_NAMES = List.of(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Taylor", "Anderson",
        "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson", "Clark",
        "Rodriguez", "Lewis", "Lee", "Walker", "Hall", "Allen", "Young", "King", "Wright", "Scott",
        "Torres", "Nguyen", "Hill", "Flores", "Green", "Adams", "Nelson", "Baker", "Gonzalez", "Rivera",
        "Perez", "Campbell", "Mitchell", "Carter", "Roberts", "Phillips", "Evans", "Turner", "Parker", "Collins",
        "Edwards", "Stewart", "Sanchez", "Morris", "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy",
        "Bailey", "Cooper", "Richardson", "Cox", "Howard", "Ward", "Kelly", "Wood", "Watson", "Brooks",
        "Bennett", "Gray", "James", "Reyes", "Cruz", "Hughes", "Price", "Myers"
    );

    private static final List<String> GIVEN_NAMES = List.of(
        "Alex", "Jordan", "Taylor", "Casey", "Riley", "Sam", "Jamie", "Cameron", "Avery", "Quinn",
        "Chris", "Pat", "Morgan", "Skyler", "Rowan", "Drew", "Dylan", "Kai", "Reese", "Peyton",
        "Logan", "Harper", "Finley", "Sage", "Phoenix", "River", "Charlie", "Emerson", "Hayden", "Jesse",
        "Micah", "Blair", "Noel", "Sidney", "Leslie", "Robin", "Devon", "Bailey", "Frankie", "Marley",
        "Parker", "Dakota", "Eden", "Elliot", "Ellis", "Erin", "Jaden", "Jessie", "Jules", "Kendall",
        "Kennedy", "Kieran", "Lane", "Lennox", "Luca", "Max", "Paris", "Remy", "Rory", "Sawyer",
        "Shiloh", "Tatum", "Teagan", "Tyler", "Winter"
    );

    private static final List<String[]> ADDRESS_POOLS = List.of(
        new String[]{"Oak St", "Vancouver", "V6H 3N1"},
        new String[]{"Pandora Ave", "Victoria", "V8V 1R6"},
        new String[]{"Spall Rd", "Kelowna", "V1Y 4R1"},
        new String[]{"Fort St", "Victoria", "V8W 1H2"},
        new String[]{"Pandosy St", "Kelowna", "V1Y 1R7"},
        new String[]{"Granville St", "Vancouver", "V6Z 1C3"},
        new String[]{"Kingsway", "Burnaby", "V5H 2C2"},
        new String[]{"No. 3 Rd", "Richmond", "V6Y 2B8"},
        new String[]{"King George Blvd", "Surrey", "V3T 2W1"},
        new String[]{"Tranquille Rd", "Kamloops", "V2B 3G9"},
        new String[]{"Terminal Ave", "Nanaimo", "V9R 5E4"},
        new String[]{"Victoria St", "Prince George", "V2L 4V2"},
        new String[]{"South Fraser Way", "Abbotsford", "V2T 1P5"},
        new String[]{"Fraser Hwy", "Langley", "V3A 4T8"},
        new String[]{"Austin Ave", "Coquitlam", "V3K 3M4"},
        new String[]{"Columbia St", "New Westminster", "V3M 1B2"},
        new String[]{"Bernard Ave", "Kelowna", "V1Y 6P9"},
        new String[]{"W 4th Ave", "Vancouver", "V6K 1R2"},
        new String[]{"Main St", "Vancouver", "V5V 3R3"},
        new String[]{"E Hastings St", "Vancouver", "V5K 1Z9"},
        new String[]{"Cambie St", "Vancouver", "V6B 2P4"},
        new String[]{"McKenzie Ave", "Saanich", "V8P 2B9"},
        new String[]{"Cook St", "Victoria", "V8V 3X3"}
    );

    private static final List<String> ROLE_TYPES = List.of("MD", "RN", "DEN", "OPT", "PHARM", "LPN");
    private static final List<String> EXPERTISE_CODES = List.of("ENG", "C06", "L01", "SPAN", "F16", "A10", "S16", "L06", "L23", "M03", "N03", "P11", "S41", "S59", "T01", "T28", "T11", "V01", "W01", "X01", "Y03", "Z01");
    private static final List<String> CREDENTIAL_TYPES = List.of("BD", "BSC", "D", "M", "OTHER", "PHD");
    private static final List<String> INSTITUTION_NAMES = List.of(
        "Hospital School of ",
        "West Coast University of ",
        "Northern Health Institute of",
        "Pacific College of ",
        "Riverside Clinic of ",
        "City Medical School of ",
        "Research Center of ",
        "Institute of ",
        "University Health Sciences of ",
        "Health Academy of "
    );

    private static final List<String> CONDITION_TYPES = List.of("LOC", "OTH", "NON-RX", "EXP", "PRAC", "HON", "LMCC", "MCCEE", "ACAD", "AF");

    private static final int FIXED_LOWER_STREET_NUMBER = 100; 
    private static final int FIXED_UPPER_STREET_NUMBER = 2000;

    private IndividualDataGenerator() { /* enforce singleton */ }

    /**
     * Returns the singleton instance (lazy initialized, thread-safe).
     * Uses double-checked locking to avoid unnecessary synchronization once initialized.
     * @return IndividualDataGenerator instance
     */
    public static IndividualDataGenerator getInstance() {
        IndividualDataGenerator result = instance;
        if (result == null) {
            synchronized (IndividualDataGenerator.class) {
                result = instance;
                if (result == null) {
                    result = new IndividualDataGenerator();
                    instance = result;
                }
            }
        }
        return result;
    }

    /**
     * Generate a family (last) name for practitioner.
     * @return generated family name
     */
    public String generateFamilyName() { return pick(FAMILY_NAMES); }

    /**
     * Generate a tuple [first, middle, third] given names.
     * @return array of names [first, middle, third]
     */
    public String[] generateGivenNames() {
        String first = pick(GIVEN_NAMES);
        String middle = pick(GIVEN_NAMES);
        String third = pick(GIVEN_NAMES);
        return new String[]{ first, middle, third };
    }

    /**
     * Return a random practitioner role type code (e.g., MD, RN).
     * @return role type code string
     */
    public String randomRoleType() { return pick(ROLE_TYPES); }


    /**
     * Generate a list of unique expertise codes (no duplicates), in randomized order.
     * If {@code count} exceeds the available code pool size, all codes are returned once.
     * @param count desired number of unique expertise codes
     * @return immutable list of unique expertise codes
     */
    public List<String> generateUniqueExpertiseCodes(int count) {
        if (count <= 0) {
            return List.of();
        }

        ArrayList<String> pool = new ArrayList<>(EXPERTISE_CODES);
        Collections.shuffle(pool, RNG);
        int take = Math.min(count, pool.size());
        return List.copyOf(pool.subList(0, take));
    }

    /**
     * Return a random credential type code.
     * @return one credential type string
     */
    public String randomCredentialType() { return pick(CREDENTIAL_TYPES); }

    /**
     * Generate a random institution name for credentials.
     * @return institution name string
     */
    public String generateInstitutionName() { return pick(INSTITUTION_NAMES) + pick(ADDRESS_POOLS)[1]; }

    /**
     * Return a random condition type code.
     * @return one of LOC, OTH, NON-RX, EXP, PRAC, HON, LMCC, MCCEE, ACAD, AF
     */
    public String randomConditionType() { return pick(CONDITION_TYPES); }

    /**
     * Returns a short description string usable in credential/disciplinary text.
     * @return short text token
     */
    public String shortText() { return "Text-" + generateNumericId().substring(0,6); }

    /**
     * Generate a combined display name (first + family).
     * Not directly used by practitioner builder (which splits given/family),
     * but provided for consistency with {@link AbstractDataGenerator} contract.
     * @return combined display name
     */
    @Override
    public String generateName() {
        String[] g = generateGivenNames();
        return (g[0] + " " + generateFamilyName()).trim();
    }

    /**
     * Generate practitioner address components.
     * @return generated address array [line1, city, postal]
     */
    @Override
    public String[] generateAddress() {
        String[] base = pick(ADDRESS_POOLS);
        int num = FIXED_LOWER_STREET_NUMBER + RNG.nextInt(FIXED_UPPER_STREET_NUMBER - FIXED_LOWER_STREET_NUMBER + 1);
        return new String[]{ num + " " + base[0], base[1], base[2] };
    }

    /**
     * Generate a random birth date in YYYY-MM-DD format.
     * Years range from 1950 to 2000; days limited to 28 to avoid invalid dates.
     * @return birth date string
     */
    public String generateBirthDate() {
        int year = 1950 + RNG.nextInt(51); // 1950-2000
        int month = 1 + RNG.nextInt(12);   // 1-12
        int day = 1 + RNG.nextInt(28);     // 1-28
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    /**
     * Generate a birth country code (ISO-like). Fixed to Canada for tests.
     * @return country code string
     */
    public String generateBirthCountry() {
        return "CA";
    }

    /**
     * Generate a birth province/state code.
     * Returns one of common Canadian province codes.
     * @return province code string
     */
    public String generateBirthProvince() {
        return pick(List.of("BC", "AB", "ON", "QC", "MB"));
    }

    /**
     * Generate a gender code/prefix used by UI components.
     * @return one of M, F, U
     */
    public String generateGender() {
        return pick(List.of("male", "female", "unknown"));
    }

    /**
     * Generate a death date string. Empty for most test cases.
     * @return (YYYY-MM-DD) String value
     */
    public String generateDeathDate() {
        return LocalDate.now().toString();
    }
}
