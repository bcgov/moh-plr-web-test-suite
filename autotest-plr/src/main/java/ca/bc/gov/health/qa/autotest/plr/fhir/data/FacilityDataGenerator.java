package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import java.security.SecureRandom;
import java.util.List;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;

/**
 * Singleton utility to generate random-ish facility data (name and address) for tests.
 * 
 * This class purposefully avoids any external libraries so it can be reused in lightweight
 * test contexts. Generated values are not guaranteed unique, but incorporate a timestamp+counter
 * suffix to greatly reduce collisions in parallel test execution.
 */
public final class FacilityDataGenerator {

	private static volatile FacilityDataGenerator instance; // Double-checked locking

	private static final SecureRandom RNG = new SecureRandom();

	private static final List<String> NAME_PREFIXES = List.of(
			"North", "South", "East", "West", "Central", "Fraser", "Coastal", "Interior", "Island", "Northern", "Community", "General", "Regional", "Valley", "Mountain", "Harbour", "Riverside", "Lakeside", "Prairie", "Metro"
	);

	private static final List<String> NAME_SUFFIXES = List.of(
			"Health Centre", "Medical Clinic", "Hospital", "Outpatient Centre", "Urgent Care", "Primary Care Clinic"
	);

	// Address fixed except for street number.
	private static final String FIXED_STREET = "Griffiths Wy"; // Spelling per specification
	private static final String FIXED_CITY = "Vancouver";
    private static final String FIXED_POSTAL = "V6B 6G1";
    private static final int FIXED_LOWER_STREET_NUMBER = 800;
    private static final int FIXED_UPPER_STREET_NUMBER = 850;

	private FacilityDataGenerator() {
		// Private constructor to enforce singleton
	}

	/**
	 * Returns the singleton instance (lazy initialized, thread-safe).
     * @return singleton instance
	 */
	public static FacilityDataGenerator getInstance() {
		FacilityDataGenerator result = instance;
		if (result == null) { // First check (no locking)
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

	/**
	 * Generate a facility name using random prefix/root/suffix plus a disambiguating suffix.
	 * Example: "Fraser Regional Hospital".
	 * @return generated facility name
	 */
	public String generateFacilityName() {
		String name = pick(NAME_PREFIXES) + " " + pick(NAME_SUFFIXES);
		return name;
	}

	/**
	 * Generate an address with a randomized street number from FIXED_LOWER_STREET_NUMBER to FIXED_UPPER_STREET_NUMBER only.
	 * Return Format Example: "number Griffiths Wy, Vancouver, BC".
	 * @return string array with address components: [0]=line1, [1]=city, [2]=postalcode
	 */
	public String[] generateFacilityAddress() {
		int number = FIXED_LOWER_STREET_NUMBER + RNG.nextInt(FIXED_UPPER_STREET_NUMBER - FIXED_LOWER_STREET_NUMBER + 1);
		return new String[] {
			number + " " + FIXED_STREET,
            FIXED_CITY,
            FIXED_POSTAL
		};
	}

	/**
	 * Builds and returns a {@code MaintainFacilityBuilder} pre-populated with:
	 *  - name (randomized)
	 *  - address (single physical FC address)
	 *  - description (fixed: "Selenium FHIR")
	 * The caller may further enrich (identifier, telecom, notes, etc.) before submit.
	 * @return configured MaintainFacilityBuilder
	 */
	public MaintainFacilityBuilder generateFacilityBuilder() {
		String name = generateFacilityName();
		String[] addressParts = generateFacilityAddress();
        String identifier = generateNumericId();
        
		return new MaintainFacilityBuilder()
				.name(name)
				.description("Selenium FHIR")
				.identifier(identifier)
				.addAddress(addressParts[0], addressParts[1], addressParts[2]);
	}

	/**
	 * Generate a pseudo-random numeric identifier as a fixed-length string.
	 * Not guaranteed globally unique, but collision probability is negligible for test data.
	 * Length: 12 digits (leading zeros preserved).
	 * @return 12-digit numeric id string (e.g. "004928374655")
	 */
	public String generateNumericId() {
		long value = Math.abs(RNG.nextLong()) % 1_000_000_000_000L; // 0 .. 999,999,999,999
		return String.format("%012d", value);
	}

	private <T> T pick(List<T> list) { return list.get(RNG.nextInt(list.size())); }

    //TODO add missing facility parameters (telecom, etc) as needed.
}
