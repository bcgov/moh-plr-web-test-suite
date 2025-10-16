package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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
	private static final String FIXED_STREET = "Richter St"; // Spelling per specification
	private static final String FIXED_CITY = "Kelowna";
    private static final String FIXED_POSTAL = "V1Y 2J6";
    private static final int FIXED_LOWER_STREET_NUMBER = 700;
    private static final int FIXED_UPPER_STREET_NUMBER = 3000;

	// Optional field data pools
	private static final List<String> PHONE_AREA_CODES = List.of("604", "778", "236", "250");
	private static final List<String> EMAIL_DOMAINS = List.of("healthbc.ca", "vch.ca", "fraserhealth.ca", "islandhealth.ca");
	private static final List<String> WEBSITE_PREFIXES = List.of("www.", "portal.", "services.");
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
     *  - identifier (random 12-digit numeric string)
	 * The caller may further enrich (telecom, notes, etc.) before submit.
	 * @return configured MaintainFacilityBuilder
	 */
	public MaintainFacilityBuilder generateFacilityBuilder() {
		return generateFacilityBuilder(EnumSet.noneOf(MaintainFacilityFields.class));
	}

	/**
	 * Builds and returns a {@code MaintainFacilityBuilder} with basic required fields
	 * plus the specified optional fields populated with generated data.
	 * 
	 * @param optionalFields set of optional fields to include
	 * @return configured MaintainFacilityBuilder
	 */
	public MaintainFacilityBuilder generateFacilityBuilder(Set<MaintainFacilityFields> optionalFields) {

		MaintainFacilityBuilder builder = new MaintainFacilityBuilder();

		// Add fields based on configuration
		if (optionalFields.contains(MaintainFacilityFields.IDENTIFIER) || MaintainFacilityFields.IDENTIFIER.isRequired()) {
			builder.identifier(generateNumericId());
		}
		if (optionalFields.contains(MaintainFacilityFields.NAME) || MaintainFacilityFields.NAME.isRequired()) {
			builder.name(generateFacilityName());
		}
		if (optionalFields.contains(MaintainFacilityFields.ADDRESS) || MaintainFacilityFields.ADDRESS.isRequired()) {
			String[] addressParts = generateFacilityAddress();
			builder.addAddress(addressParts[0], addressParts[1], addressParts[2]);
		}
		if (optionalFields.contains(MaintainFacilityFields.PHONE) || MaintainFacilityFields.PHONE.isRequired()) {
			builder.addTelecom("phone", generatePhoneNumber());
		}
		if (optionalFields.contains(MaintainFacilityFields.EMAIL) || MaintainFacilityFields.EMAIL.isRequired()) {
			builder.addTelecom("email", generateEmailAddress());
		}
		if (optionalFields.contains(MaintainFacilityFields.FAX) || MaintainFacilityFields.FAX.isRequired()) {
			builder.addTelecom("fax", generatePhoneNumber());
		}
		if (optionalFields.contains(MaintainFacilityFields.WEBSITE) || MaintainFacilityFields.WEBSITE.isRequired()) {
			builder.addTelecom("url", generateWebsiteUrl());
		}
		if (optionalFields.contains(MaintainFacilityFields.NOTES) || MaintainFacilityFields.NOTES.isRequired()) {
			builder.addNote(generateNote());
		}
		if (optionalFields.contains(MaintainFacilityFields.DESCRIPTION) || MaintainFacilityFields.DESCRIPTION.isRequired()) {
			builder.description(generateDescription());
		}

		return builder;
	}

	/**
	 * Convenience method to generate facility with specific optional fields.
	 * @param fields variable arguments of optional fields to include
	 * @return configured MaintainFacilityBuilder
	 */
	public MaintainFacilityBuilder generateFacilityBuilder(MaintainFacilityFields... fields) {
		return generateFacilityBuilder(EnumSet.copyOf(Arrays.asList(fields)));
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

	/**
	 * Generate a random phone number in BC format.
	 * @return phone number string (e.g. "604-555-1234")
	 */
	public String generatePhoneNumber() {
		String areaCode = pick(PHONE_AREA_CODES);
		int exchange = 555; // Using 555 for test data
		int number = 1000 + RNG.nextInt(9000); // 1000-9999
		return String.format("%s-%d-%d", areaCode, exchange, number);
	}

	/**
	 * Generate a random email address for the facility.
	 * @return email address string
	 */
	public String generateEmailAddress() {
		String domain = pick(EMAIL_DOMAINS);
		String[] prefixes = {"info", "contact", "admin", "reception", "services"};
		String prefix = pick(List.of(prefixes));
		return prefix + "@" + domain;
	}

	/**
	 * Generate a random website URL for the facility.
	 * @return website URL string (e.g. "https://www.healthbc.ca")
	 */
	public String generateWebsiteUrl() {
		String prefix = pick(WEBSITE_PREFIXES);
		String domain = pick(EMAIL_DOMAINS);
		return "https://" + prefix + domain;
	}

	/**
	 * Generate a random operational note for the facility.
	 * @return note text string
	 */
	public String generateNote() {
		return pick(NOTE_TEMPLATES);
	}

	/**
	 * Generate an additional description beyond the standard "Selenium FHIR".
	 * @return additional description string
	 */
	public String generateDescription() {
		String[] descriptors = {"Advanced", "Comprehensive", "Specialized", "Community-focused", "Modern"};
		String descriptor = pick(List.of(descriptors));
		return descriptor + " Healthcare Facility";
	}

	private <T> T pick(List<T> list) { return list.get(RNG.nextInt(list.size())); }

    //TODO add missing facility parameters (telecom, etc) as needed.
}
