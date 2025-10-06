package ca.bc.gov.health.qa.autotest.plr.fhir;

import java.io.IOException;
import java.util.Locale;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.IdentifierType;
import ca.bc.gov.health.qa.autotest.plr.fhir.maintain.MaintainPracBuilder;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;

/**
 * Utility with a single entry point to build a minimal Practitioner maintain request and submit it.
 * <p>Intended for quick automation smoke scenarios. Adjust defaults as needed.</p>
 *
 */
public final class PracMaintainExecutor extends BaseMaintainExecutor
{
    private static final Logger LOG = ExecutionLogManager.getLogger();

    /**
     * Constructor performs all configuration and login side-effects so later method only builds and submits.
     */
    public PracMaintainExecutor(UserType userType) { super(userType); }

    /**
     * Single public operation: build the practitioner maintain payload and submit it.
     * Returns practitioner id.
     * @param identifierType
     * @param identifierValue
     * @param familyName
     * @param givenName
     * @param gender
     * @param birthDate
     * @param roleType
     * @param addressType
     * @param addressPurpose
     * @param addressLine1
     * @param addressCity
     * @param addressPostalCode
     * @return the created practitioner id
     * @throws IllegalStateException if the submission fails
     */
    public String submitPractitioner(
            IdentifierType identifierType,
            String identifierValue,
            String familyName,
            String givenName,
            String gender,
            String birthDate,
            String roleType,
            String addressType,
            String addressPurpose,
            String addressLine1,
            String addressCity,
            String addressPostalCode)
    {
        MaintainPracBuilder builder = new MaintainPracBuilder()
                .identifier(identifierType, identifierValue)
                .familyName(familyName)
                .firstName(givenName)
                .gender(gender.toLowerCase(Locale.ROOT))
                .birthDate(birthDate)
                .roleType(roleType)
                .addStatus("LIC", "ACTIVE", "GS")
                .addAddress(addressType, addressPurpose, addressLine1, addressCity, addressPostalCode);

        JSONObject payload = builder.build();
        LOG.info(payload.toString());

        try
        {
            String id = actions_.submitMaintainRequest(payload);
            LOG.info("Practitioner maintain submitted. Returned id {}.", id);
            return id;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while submitting practitioner maintain request", e);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("I/O failure during practitioner maintain request", e);
        }
    }

}
