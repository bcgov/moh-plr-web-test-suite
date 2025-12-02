package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import java.security.SecureRandom;
import java.util.List;

/**
 * Abstract base for data generators that provide randomized test values used in FHIR builders.
 * Shared generation logic for identifiers, telecom values, notes and descriptions lives here.
 * Concrete subclasses override name/address pools to distinguish facility vs organization data.
 */
public abstract class AbstractDataGenerator {

    /**
     * Shared random number generator used for all test data value selection.
     */
    protected static final SecureRandom RNG = new SecureRandom();

    /**
     * Area codes used when generating phone numbers for test data.
     */
    protected static final List<String> PHONE_AREA_CODES = List.of("604", "778", "236", "250");

    /**
     * Email domains used to build randomized email addresses for test entities.
     */
    protected static final List<String> EMAIL_DOMAINS = List.of("health.ca", "moh.ca");

    /**
     * URL prefixes (sub-domains) applied when constructing website URLs.
     */
    protected static final List<String> WEBSITE_PREFIXES = List.of("www.", "portal.", "services.");

    /**
     * FTP host names used to generate sample FTP endpoints for integration related test cases.
     */
    protected static final List<String> FTP_HOSTS = List.of("ftp.health.ca", "ftp.services.ca", "files.hospital.ca");

    /**
     * Human-readable operational note templates appended with a random suffix for uniqueness.
     */
    protected static final List<String> NOTE_TEMPLATES = List.of(
        "24/7 emergency services available",
        "Wheelchair accessible facility",
        "Parking available on-site",
        "Public transit accessible",
        "Multilingual staff available"
    );

    /**
     * Descriptive adjectives used when composing facility or organization description text.
     */
    protected static final List<String> DESCRIPTION_DESCRIPTORS = List.of("Advanced", "Comprehensive", "Specialized", "Community-focused", "Modern");

    /**
     * Constructs the generator. Subclasses provide specialized pools (e.g. facility vs organization)
     */
    public AbstractDataGenerator() { /* base constructor */ }

    /** Generate a pseudo-random numeric identifier (12 digits). 
     * @return 12-digit numeric identifier
    */
    public String generateNumericId() {
        long value = Math.abs(RNG.nextLong()) % 1_000_000_000_000L; // 0 .. 999,999,999,999
        return String.format("%012d", value);
    }

    /** Generate a random phone number in BC format. 
     * @return phone number string
    */
    public String generatePhoneNumber() {
        String areaCode = pick(PHONE_AREA_CODES);
        int exchange = 555; // Fixed for test data
        int number = 1000 + RNG.nextInt(9000); // 1000-9999
        return String.format("%s-%d-%d", areaCode, exchange, number);
    }

    /** Generate a pseudo FTP URL. 
     * @return ftp url string
    */
    public String generateFtpUrl() {
        String host = pick(FTP_HOSTS);
        String dir = pick(List.of("incoming", "secure", "pub", "outbound"));
        return "ftp://" + host + "/" + dir;
    }

    /** Generate a random email address. 
     * @return email address string
    */
    public String generateEmailAddress() {
        String domain = pick(EMAIL_DOMAINS);
        String[] prefixes = {"info", "contact", "admin", "reception", "services"};
        String prefix = prefixes[RNG.nextInt(prefixes.length)];
        return prefix + "@" + domain;
    }

    /** Generate a random website URL. 
     * @return website url string
    */
    public String generateWebsiteUrl() {
        String prefix = pick(WEBSITE_PREFIXES);
        String domain = pick(EMAIL_DOMAINS);
        return "https://" + prefix + domain;
    }

    /** Generate a random operational note (shared pool for facility and organization). 
     * @return note string
    */
    public String generateNote() {
        int number = 1000 + RNG.nextInt(9000);
        return pick(NOTE_TEMPLATES) + " " + number;
    }

    /** Generate a random description. 
     * @return description string
    */
    public String generateDescription() {
        String descriptor = pick(DESCRIPTION_DESCRIPTORS);
        return descriptor + " Healthcare Facility"; // Facility wording retained; org may override if needed.
    }

    /** Subclasses must implement distinct name generation. 
     * @return generated name string
    */
    public abstract String generateName();

    /** Subclasses must implement address generation (array: line1, city, postal). 
     * @return generated address array
    */
    public abstract String[] generateAddress();

    /**
     * Returns a uniformly random element from the provided non-empty list.
     *
     * @param list source list (must contain at least one element)
     * @param <T> element type
     * @return randomly selected element from {@code list}
     */
    protected <T> T pick(List<T> list) {
        if (list.isEmpty()) throw new IllegalArgumentException("Cannot pick from empty list");
        return list.get(RNG.nextInt(list.size()));
    }
}
