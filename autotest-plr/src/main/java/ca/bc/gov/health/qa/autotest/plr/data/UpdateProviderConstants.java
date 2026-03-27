package ca.bc.gov.health.qa.autotest.plr.data;

import java.util.List;

/** Class containing constant values for Update Provider test cases */
public class UpdateProviderConstants {

    public static final List<String> RELATIONSHIP_TYPE_OPTIONS = List.of(
            "LOC - Locum",
            "ER - Employer",
            "EE - Employee",
            "PHCST - Pharmacist",
            "PHMGR - Pharmacy Manager",
            "PHSTF - Staff Pharmacist",
            "OTHER - Other",
            "SPONSOR - Owner Provider",
            "PHARMACY - Pharmacy",
            "OPERATES - Operates the target.",
            "OPERATED - Operated by the target.",
            "DIRECTS - Directs the target, e.g. Medical Director.",
            "DIRECTED - Directed by the target.",
            "WORKSAT - Works at",
            "WORKLOCATION - Work location",
            "MANAGES - Manages the target",
            "MANAGEDBY - Managed by the target"
    );
}
