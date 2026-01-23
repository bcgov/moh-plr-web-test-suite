package ca.bc.gov.health.qa.autotest.plr.fhir.data.individual;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainIndividualBuilder;

/**
 * Factory responsible for creating and configuring {@link MaintainIndividualBuilder}
 * instances using generated test data from {@link IndividualDataGenerator}
 * based on configurations from {@link IndividualMaintainConfig}.
 */
public class IndividualBuilderFactory {

    private final IndividualDataGenerator dataGen;

    /** Create a new factory with the provided data generator. */
    public IndividualBuilderFactory(IndividualDataGenerator dataGen) {
        this.dataGen = dataGen;
    }

    /**
     * Builds a practitioner with a freshly created {@link IndividualMaintainConfig}.
     * @return configured builder
     */
    public MaintainIndividualBuilder build() { return build(new IndividualMaintainConfig()); }

    /**
     * Builds a {@link MaintainIndividualBuilder} using the provided configuration.
     * Optional attributes are added based on enabled flags; list attributes
     * are generated according to their counts.
     * @param config configuration describing which attributes and counts to include
     * @return populated MaintainPracBuilder ready for submission
     */
    public MaintainIndividualBuilder build(IndividualMaintainConfig config) {
        MaintainIndividualBuilder b = new MaintainIndividualBuilder();

        if (config.isIdentifierEnabled()) {
            Map<IdentifierType,String> ids = new HashMap<>();
            ids.put(config.getRoleType().getIdentifierType(), dataGen.generateNumericId());
            b.setIdentifiers(ids);
        }
        if (config.isFamilyNameEnabled()) {
            b.familyName(dataGen.generateFamilyName());
        }
        if (config.isGivenNamesEnabled()) {
            String[] names = dataGen.generateGivenNames();
            b.setNames(names[0], names[1], names[2]);
        }
        // role type always present in config; no conditional needed
        b.roleType(config.getRoleType());

        if (config.isAddressEnabled()) {
            String[] addr = dataGen.generateAddress();
            b.addAddress("physical", "BC", addr[0], addr[1], addr[2]);
        }

        // Telecom purpose codes mirrored from org
        if (config.isPhoneEnabled())  b.addTelecom("phone", "MC", dataGen.generatePhoneNumber());
        if (config.isMobileEnabled()) b.addTelecom("sms",   "OC", dataGen.generatePhoneNumber());
        if (config.isPagerEnabled())  b.addTelecom("pager", "HC", dataGen.generatePhoneNumber());
        if (config.isModemEnabled())  b.addTelecom("other", "CC", dataGen.generatePhoneNumber());
        if (config.isFaxEnabled())    b.addTelecom("fax",   "DC", dataGen.generatePhoneNumber());
        if (config.isEmailEnabled())  b.addTelecom("email", "FC", dataGen.generateEmailAddress());
        if (config.isWebsiteEnabled())b.addTelecom("url",   "BC", dataGen.generateWebsiteUrl());
        if (config.isFtpEnabled())    b.addTelecom("url",   "MC", dataGen.generateFtpUrl());

        if (config.isConfidentialityEnabled()) {
            b.confidentiality(true);
        }

        if (config.isDemographicsEnabled()) {
            String birthDate = dataGen.generateBirthDate();
            String birthCountry = dataGen.generateBirthCountry();
            String birthProvince = dataGen.generateBirthProvince();
            String gender = dataGen.generateGender();
            String deathDate = dataGen.generateDeathDate(); // optional
            b.setDemographics(birthDate, birthCountry, birthProvince, gender, deathDate);
        }

        List<String> expertiseCodes = dataGen.generateUniqueExpertiseCodes(config.getExpertiseCount());
        for (String code : expertiseCodes) {
            b.addExpertise(code, dataGen.shortText());
        }

        List<String> credentialTypes = dataGen.generateUniqueCredentialTypes(config.getCredentialCount());
        for (String credentialType : credentialTypes) {
            b.addCredential(
                credentialType,
                dataGen.shortText(),
                dataGen.generateNumericId().substring(0, 7),
                dataGen.generateInstitutionName(),
                dataGen.generateAddress()[1],
                true,
                "" + (1995 + (int)(Math.random() * 30))
            );
        }
        for (int i = 0; i < config.getConditionCount(); i++) {
            b.addCondition(dataGen.randomConditionType(), i % 2 == 0, dataGen.shortText());
        }
        for (int i = 0; i < config.getDisciplinaryActionCount(); i++) {
            b.addDisciplinaryAction(true, dataGen.shortText(), "2024-01-0" + ((i % 9) + 1));
        }
        for (int i = 0; i < config.getNoteCount(); i++) {
            b.addNote(dataGen.generateNote());
        }
        // Generate up to MAX_STATUS_COUNT unique statuses with distinct statusClass values
        int desiredStatuses = Math.min(config.getStatusCount(), MaintainIndividualBuilder.MAX_STATUS_COUNT);
        List<String> statusCodes = dataGen.generateUniqueStatusCodes(desiredStatuses);
        for (int i = 0; i < statusCodes.size(); i++) {
            String code = statusCodes.get(i);
            String reason = dataGen.reasonForStatus(code);
            String statusClass = MaintainIndividualBuilder.STATUS_CLASSES_ORDER.get(i);
            b.addStatus(statusClass, code, reason);
        }

        return b;
    }
}
