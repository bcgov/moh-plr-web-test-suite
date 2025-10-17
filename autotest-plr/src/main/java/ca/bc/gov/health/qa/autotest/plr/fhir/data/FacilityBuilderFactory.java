package ca.bc.gov.health.qa.autotest.plr.fhir.data;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainFacilityBuilder;

/**
 * Factory responsible for creating and configuring {@link MaintainFacilityBuilder}
 * instances using generated test data from {@link FacilityDataGenerator}.
 * <p>
 * This class encapsulates the logic that was previously embedded in
 * {@code FacilityDataGenerator.generateFacilityBuilder(..)}, improving
 * adherence to the Single Responsibility Principle by separating data generation
 * from builder assembly.
 * <p>
 * NOTE: The existing methods in {@link FacilityDataGenerator} can remain for
 * backward compatibility and delegate to this factory in a later step. For now
 * the factory is introduced without altering existing call sites.
 */
public class FacilityBuilderFactory {

    private final FacilityDataGenerator dataGen;

    /**
     * Creates a new factory with the provided data generator.
     * @param dataGen singleton (or custom) data generator to supply random values
     */
    public FacilityBuilderFactory(FacilityDataGenerator dataGen) {
        this.dataGen = dataGen;
    }

    /**
     * Builds a facility builder with only required fields populated.
     * @return configured builder
     */
    public MaintainFacilityBuilder build() {
        return build(EnumSet.noneOf(MaintainFacilityFields.class));
    }

    /**
     * Builds a facility builder with required + specified optional fields populated.
     * @param optionals set of optional fields to include
     * @return configured builder
     */
    public MaintainFacilityBuilder build(Set<MaintainFacilityFields> optionals) {
        MaintainFacilityBuilder builder = new MaintainFacilityBuilder();

        // Identifier
        if (include(MaintainFacilityFields.IDENTIFIER, optionals)) {
            builder.identifier(dataGen.generateNumericId());
        }
        // Name
        if (include(MaintainFacilityFields.NAME, optionals)) {
            builder.name(dataGen.generateFacilityName());
        }
        // Address
        if (include(MaintainFacilityFields.ADDRESS, optionals)) {
            String[] addr = dataGen.generateFacilityAddress();
            builder.addAddress(addr[0], addr[1], addr[2]);
        }
        // Telecoms / Notes / Description (optional)
        if (include(MaintainFacilityFields.PHONE, optionals)) {
            builder.addTelecom("phone", dataGen.generatePhoneNumber());
        }
        if (include(MaintainFacilityFields.MOBILE, optionals)) {
            builder.addTelecom("sms", dataGen.generatePhoneNumber());
        }
        if (include(MaintainFacilityFields.PAGER, optionals)) {
            builder.addTelecom("pager", dataGen.generatePhoneNumber());
        }
        if (include(MaintainFacilityFields.MODEM, optionals)) {
            builder.addTelecom("other", dataGen.generatePhoneNumber());
        }
        if (include(MaintainFacilityFields.EMAIL, optionals)) {
            builder.addTelecom("email", dataGen.generateEmailAddress());
        }
        if (include(MaintainFacilityFields.FAX, optionals)) {
            builder.addTelecom("fax", dataGen.generatePhoneNumber());
        }
        if (include(MaintainFacilityFields.WEBSITE, optionals)) {
            builder.addTelecom("url", dataGen.generateWebsiteUrl());
        }
        if (include(MaintainFacilityFields.FTP, optionals)) {
            builder.addTelecom("url", dataGen.generateFtpUrl());
        }
        if (include(MaintainFacilityFields.NOTES, optionals)) {
            builder.addNote(dataGen.generateNote());
        }
        if (include(MaintainFacilityFields.DESCRIPTION, optionals)) {
            builder.description(dataGen.generateDescription());
        }

    return builder;
    }

    /**
     * Varargs convenience overload.
     * @param optionals optional fields
     * @return configured builder
     */
    public MaintainFacilityBuilder build(MaintainFacilityFields... optionals) {
    MaintainFacilityBuilder builder = build(optionals == null || optionals.length == 0
        ? EnumSet.noneOf(MaintainFacilityFields.class)
        : EnumSet.copyOf(Arrays.asList(optionals)));
    // Already validated in underlying build(Set..) path
    return builder;
    }

    /**
     * Determines whether a field should be included (true if required or explicitly requested).
     */
    private boolean include(MaintainFacilityFields field, Set<MaintainFacilityFields> requested) {
        return field.isRequired() || requested.contains(field);
    }
}
