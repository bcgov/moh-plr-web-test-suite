package ca.bc.gov.health.qa.autotest.plr.fhir.data.individual;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.common.model.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.individual.MaintainPracBuilder;

/**
 * Factory responsible for creating and configuring {@link MaintainPracBuilder}
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
    public MaintainPracBuilder build() { return build(new IndividualMaintainConfig()); }

    /**
     * Builds a {@link MaintainPracBuilder} using the provided configuration.
     * Optional attributes are added based on enabled flags; list attributes
     * are generated according to their counts.
     * @param config configuration describing which attributes and counts to include
     * @return populated MaintainPracBuilder ready for submission
     */
    public MaintainPracBuilder build(IndividualMaintainConfig config) {
        MaintainPracBuilder b = new MaintainPracBuilder();

        if (config.isIdentifierEnabled()) {
            Map<IdentifierType,String> ids = new HashMap<>();
            ids.put(IdentifierType.IPC, dataGen.generateNumericId());
            b.setIdentifiers(ids);
        }
        if (config.isFamilyNameEnabled()) {
            b.familyName(dataGen.generateFamilyName());
        }
        if (config.isGivenNamesEnabled()) {
            String[] names = dataGen.generateGivenNames();
            b.setNames(names[0], names[1], names[2]);
        }
        if (config.isRoleTypeEnabled()) {
            b.roleType(dataGen.randomRoleType());
        }

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

        for (int i = 0; i < config.getExpertiseCount(); i++) {
            b.addExpertise(dataGen.randomExpertiseCode(), "HL7");
        }
        for (int i = 0; i < config.getCredentialCount(); i++) {
            b.addCredential(
                dataGen.randomCredentialType(),
                dataGen.shortText(),
                dataGen.generateNumericId().substring(0, 7),
                dataGen.generateInstitutionName(),
                dataGen.generateAddress()[1],
                true,
                "1995" + (i)
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
        int desiredStatuses = Math.min(config.getStatusCount(), MaintainPracBuilder.MAX_STATUS_COUNT);
        List<String> statusCodes = dataGen.generateUniqueStatusCodes(desiredStatuses);
        for (int i = 0; i < statusCodes.size(); i++) {
            String code = statusCodes.get(i);
            String reason = dataGen.reasonForStatus(code);
            String statusClass = MaintainPracBuilder.STATUS_CLASSES_ORDER.get(i);
            b.addStatus(statusClass, code, reason);
        }

        return b;
    }
}
