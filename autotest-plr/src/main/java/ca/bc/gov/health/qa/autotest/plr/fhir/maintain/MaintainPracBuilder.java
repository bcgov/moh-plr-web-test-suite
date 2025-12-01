package ca.bc.gov.health.qa.autotest.plr.fhir.maintain;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.bc.gov.health.qa.autotest.core.util.io.ResourceUtils;

/**
 * TODO (AZ) - doc
 */
public class MaintainPracBuilder
{
    private List<Map<String,String>> addressList_            = new ArrayList<>();
    private String                   birthDate_              = null;
    private List<Map<String,String>> conditionList_          = new ArrayList<>();
    private Boolean                  confidentiality_        = null;
    private List<Map<String,String>> credentialList_         = new ArrayList<>();
    private List<Map<String,String>> disciplinaryActionList_ = new ArrayList<>();
    private String                   familyName_             = null;
    private String                   firstName_              = null;
    private String                   gender_                 = null;
    private IdentifierType           identifierType_         = null;
    private String                   identifierValue_        = null;
    private List<Map<String,String>> languageList_           = new ArrayList<>();
    private List<Map<String,String>> noteList_               = new ArrayList<>();
    private String                   roleType_               = null;
    private List<Map<String,String>> specialtyList_          = new ArrayList<>();
    private List<Map<String,String>> statusList_             = new ArrayList<>();
    private List<Map<String,String>> telecomList_            = new ArrayList<>();

    /**
     * TODO (AZ) - doc
     */
    public MaintainPracBuilder()
    {}

    /**
     * TODO (AZ) - doc
     *
     * @param type
     *        ???
     *        physical, postal
     *
     * @param purpose
     *        ???
     *        BC, CC, DC, EC, FC, HC, MC, OC
     *
     * @param line1
     *        ???
     *
     * @param city
     *        ???
     *
     * @param postalCode
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder addAddress(
            String type,
            String purpose,
            String line1,
            String city,
            String postalCode)
    {
        Map<String,String> info = new HashMap<>();
        info.put("type",       type);
        info.put("purpose",    purpose);
        info.put("line1",      line1);
        info.put("city",       city);
        info.put("postalCode", postalCode);
        addressList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param type
     *        ???
     *
     * @param restriction
     *        ???
     *
     * @param explanation
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder addCondition(String type, boolean restriction, String explanation)
    {
        Map<String,String> info = new HashMap<>();
        info.put("type",        type);
        info.put("restriction", "" + restriction);
        info.put("explanation", explanation);
        conditionList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param type
     *        ???
     *
     * @param designation
     *        ???
     *
     * @param registrationNumber
     *        ???
     *
     * @param institution
     *        ???
     *
     * @param city
     *        ???
     *
     * @param equivalency
     *        ???
     *
     * @param year
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder addCredential(
            String type,
            String designation,
            String registrationNumber,
            String institution,
            String city,
            boolean equivalency,
            String year)
    {
        Map<String,String> info = new HashMap<>();
        info.put("type",               type);
        info.put("designation",        designation);
        info.put("registrationNumber", registrationNumber);
        info.put("institution",        institution);
        info.put("city",               city);
        info.put("equivalency",        "" + equivalency);
        info.put("year",               year);
        credentialList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param display
     *        ???
     *
     * @param description
     *        ???
     *
     * @param archiveDate
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder addDisciplinaryAction(
            boolean display, String description, String archiveDate)
    {
        Map<String,String> info = new HashMap<>();
        info.put("display", "" + display);
        info.put("description", description);
        info.put("archiveDate", archiveDate);
        disciplinaryActionList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param code
     *        ???
     *
     * @param sourceCode
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder addLanguage(String code, String sourceCode)
    {
        Map<String,String> info = new HashMap<>();
        info.put("code",       code);
        info.put("sourceCode", sourceCode);
        languageList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param text
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder addNote(String text)
    {
        Map<String,String> info = new HashMap<>();
        info.put("text", text);
        noteList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param code
     *        ???
     *
     * @param sourceCode
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder addSpecialty(String code, String sourceCode)
    {
        Map<String,String> info = new HashMap<>();
        info.put("code",       code);
        info.put("sourceCode", sourceCode);
        specialtyList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param statusClass
     *        ???
     *        AE, LIC
     *
     * @param status
     *        ???
     *
     * @param statusReason
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder addStatus(String statusClass, String status, String statusReason)
    {
        Map<String,String> info = new HashMap<>();
        info.put("status",       status);
        info.put("statusClass",  statusClass);
        info.put("statusReason", statusReason);
        statusList_.add(info);
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param type
     *        ???
     *        email (Email)
     *        fax   (Fax)
     *        other (Modem)
     *        pager (Pager)
     *        phone (Telephone)
     *        sms   (Mobile)
     *        url   (HTTP)
     *
     * @param purpose
     *        ???
     *        BC, CC, DC, FC, HC, MC, OC
     *
     * @param value
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder addTelecom(String type, String purpose, String value)
    {
        telecomList_.add(Map.of(
                "purpose", purpose,
                "type",    type,
                "value",   value));
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param birthDate
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder birthDate(String birthDate)
    {
        birthDate_ = birthDate;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public JSONObject build()
    {
        verifyParameters();
        String template = ResourceUtils.readResource(
                MethodHandles.lookup().lookupClass(), "maintain-practitioner.json");
        JSONObject json = new JSONObject(template);

        MaintainProviderAccessor accessor = new MaintainProviderAccessor(json);
        JSONObject nameJson     = accessor.getPracNameJson(0);
        JSONObject pracJson     = accessor.getPracJson();
        JSONObject pracRoleJson = accessor.getPracRoleJson();

        nameJson.put("family", familyName_);
        nameJson.getJSONArray("given").put(0, firstName_);
        pracJson.getJSONArray("identifier")
                .getJSONObject(0)
                .put("system", identifierType_.getSourceSystem())
                .put("value", identifierValue_);
        pracRoleJson
                .getJSONArray("code")
                .getJSONObject(0)
                .getJSONArray("coding")
                .getJSONObject(0)
                .put("code", roleType_);
        pracRoleJson
                .getJSONObject("practitioner")
                .getJSONObject("identifier")
                .put("system", identifierType_.getSourceSystem())
                .put("value", identifierValue_);
        pracJson.put("gender", gender_);
        pracJson.getJSONObject("_birthDate")
                .getJSONArray("extension")
                .getJSONObject(0)
                .put("valueDateTime", birthDate_);

        JSONArray specialtyJson = pracRoleJson.getJSONArray("specialty");
        for (Map<String,String> info : specialtyList_)
        {
            specialtyJson.put(MaintainUtils.createExpertise(false, info));
        }

        JSONArray addressesJson = pracJson.getJSONArray("address");
        for (Map<String,String> info : addressList_)
        {
            addressesJson.put(MaintainUtils.createAddress(info));
        }

        JSONArray telecomJson = pracJson.getJSONArray("telecom");
        for (Map<String,String> info : telecomList_)
        {
            telecomJson.put(MaintainUtils.createTelecom(info));
        }

        JSONArray communicationJson = pracJson.getJSONArray("communication");
        for (Map<String,String> info : languageList_)
        {
            communicationJson.put(MaintainUtils.createExpertise(true, info));
        }

        JSONArray extensionJson = accessor.getPracExtensionJson();
        if (statusList_.isEmpty())
        {
            extensionJson.put(MaintainUtils.createStatus(Map.of(
                    "statusClass",  "LIC",
                    "status",       "ACTIVE",
                    "statusReason", "GS")));
        }
        else
        {
            for (Map<String,String> info : statusList_)
            {
                extensionJson.put(MaintainUtils.createStatus(info));
            }
        }
        if (confidentiality_ != null)
        {
            extensionJson.put(MaintainUtils.createConfidentiality(confidentiality_));
        }
        for (Map<String,String> info : conditionList_)
        {
            extensionJson.put(MaintainUtils.createCondition(info));
        }
        for (Map<String,String> info : disciplinaryActionList_)
        {
            extensionJson.put(MaintainUtils.createDisciplinaryAction(info));
        }
        for (Map<String,String> info : noteList_)
        {
           extensionJson.put(MaintainUtils.createNote(info));
        }

        JSONArray containedJson = pracJson.getJSONArray("contained");
        JSONArray qualificationJson = pracJson.getJSONArray("qualification");
        int refNum = 0;
        for (Map<String,String> info : credentialList_)
        {
            refNum++;
            String reference = "grantingInstitution-" + refNum;
            containedJson.put(MaintainUtils.createContained(info, reference));
            qualificationJson.put(MaintainUtils.createQualification(info, reference));
        }

        return json;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param confidentiality
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder confidentiality(boolean confidentiality)
    {
        confidentiality_ = confidentiality;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param familyName
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder familyName(String familyName)
    {
        familyName_ = familyName;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param firstName
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder firstName(String firstName)
    {
        firstName_ = firstName;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param gender
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder gender(String gender)
    {
        gender_ = gender;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param identifierType
     *        ???
     *
     * @param identifierValue
     *        ???
     *
     * @return ???
     */
    public MaintainPracBuilder identifier(IdentifierType identifierType, String identifierValue)
    {
        identifierType_  = identifierType;
        identifierValue_ = identifierValue;
        return this;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param roleType
     *        ???
     *        DEN, MD, RN, OPT, ... etc.
     *
     * @return ???
     */
    public MaintainPracBuilder roleType(String roleType)
    {
        roleType_ = roleType;
        return this;
    }

    private void verifyParameters()
    {
        requireNonNull(birthDate_,       "Missing birth date.");
        requireNonNull(familyName_,      "Missing family name.");
        requireNonNull(firstName_,       "Missing first name.");
        requireNonNull(gender_,          "Missing gender.");
        requireNonNull(identifierType_,  "Missing identifier type.");
        requireNonNull(identifierValue_, "Missing identifier value.");
    }
}
