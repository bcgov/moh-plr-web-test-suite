package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import java.security.SecureRandom;
import java.util.List;

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
	private static final String FIXED_STREET = "Richter St"; // Spelling per specification
	private static final String FIXED_CITY = "Kelowna";
    private static final String FIXED_POSTAL = "V1Y 2J6";
    private static final int FIXED_LOWER_STREET_NUMBER = 700;
    private static final int FIXED_UPPER_STREET_NUMBER = 3000;

	// Optional field data pools
	private static final List<String> PHONE_AREA_CODES = List.of("604", "778", "236", "250");
	private static final List<String> EMAIL_DOMAINS = List.of("health.ca", "moh.ca");
	private static final List<String> WEBSITE_PREFIXES = List.of("www.", "portal.", "services.");	// FTP host name components
	private static final List<String> FTP_HOSTS = List.of("ftp.health.ca", "ftp.services.ca", "files.hospital.ca");
	private static final List<String> NOTE_TEMPLATES = List.of(
		"24/7 emergency services available",
		"Wheelchair accessible facility", 
		"Parking available on-site",
		"Public transit accessible",
		"Multilingual staff available"
	);

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
	protected String generateFacilityName() {
		String name = pick(NAME_PREFIXES) + " " + pick(NAME_SUFFIXES);
		return name;
	}

	/**
	 * Generate an address with a randomized street number from FIXED_LOWER_STREET_NUMBER to FIXED_UPPER_STREET_NUMBER only.
	 * Return Format Example: "number Griffiths Wy, Vancouver, BC".
	 * @return string array with address components: [0]=line1, [1]=city, [2]=postalcode
	 */
	protected String[] generateFacilityAddress() {
		int number = FIXED_LOWER_STREET_NUMBER + RNG.nextInt(FIXED_UPPER_STREET_NUMBER - FIXED_LOWER_STREET_NUMBER + 1);
		return new String[] {
			number + " " + FIXED_STREET,
            FIXED_CITY,
            FIXED_POSTAL
		};
	}


	/**
	 * Generate a pseudo-random numeric identifier as a fixed-length string.
	 * Not guaranteed globally unique, but collision probability is negligible for test data.
	 * Length: 12 digits (leading zeros preserved).
	 * @return 12-digit numeric id string (e.g. "004928374655")
	 */
	protected String generateNumericId() {
		long value = Math.abs(RNG.nextLong()) % 1_000_000_000_000L; // 0 .. 999,999,999,999
		return String.format("%012d", value);
	}

	/**
	 * Generate a random phone number in BC format.
	 * @return phone number string (e.g. "604-555-1234")
	 */
	protected String generatePhoneNumber() {
		String areaCode = pick(PHONE_AREA_CODES);
		int exchange = 555; // Using 555 for test data
		int number = 1000 + RNG.nextInt(9000); // 1000-9999
		return String.format("%s-%d-%d", areaCode, exchange, number);
	}

	/**
	 * Generate a pseudo FTP URL.
	 * @return ftp url string (e.g. "ftp://ftp.healthbc.ca/incoming")
	 */
	protected String generateFtpUrl() {
		String host = pick(FTP_HOSTS);
		String dir = pick(List.of("incoming", "secure", "pub", "outbound"));
		return "ftp://" + host + "/" + dir;
	}

	/**
	 * Generate a random email address for the facility.
	 * @return email address string
	 */
	protected String generateEmailAddress() {
		String domain = pick(EMAIL_DOMAINS);
		String[] prefixes = {"info", "contact", "admin", "reception", "services"};
		String prefix = pick(List.of(prefixes));
		return prefix + "@" + domain;
	}

	/**
	 * Generate a random website URL for the facility.
	 * @return website URL string (e.g. "https://www.healthbc.ca")
	 */
	protected String generateWebsiteUrl() {
		String prefix = pick(WEBSITE_PREFIXES);
		String domain = pick(EMAIL_DOMAINS);
		return "https://" + prefix + domain;
	}

	/**
	 * Generate a random operational note for the facility.
	 * @return note text string
	 */
	protected String generateNote() {
		int number = 1000 + RNG.nextInt(9000);
		return pick(NOTE_TEMPLATES) + " " + number;
	}

	/**
	 * Generate an additional description beyond the standard "Selenium FHIR".
	 * @return additional description string
	 */
	protected String generateDescription() {
		String[] descriptors = {"Advanced", "Comprehensive", "Specialized", "Community-focused", "Modern"};
		String descriptor = pick(List.of(descriptors));
		return descriptor + " Healthcare Facility";
	}

	private <T> T pick(List<T> list) { return list.get(RNG.nextInt(list.size())); }

}
