package ca.bc.gov.health.qa.autotest.plr.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * PLR and DSR user types.
 */
public enum UserType
{
    /**
     * PLR/DSR registry administrator user.
     */
    ADMIN,

    /**
     * PLR primary data source user.
     */
    PRIMARY,

    /**
     * PLR secondary data source user.
     */
    SECONDARY,

    /**
     * PLR consumer user.
     */
    CONSUMER,

    /**
     * DSR MOH approver user.
     */
    MOH,

    /**
     * DSR regular user.
     */
    USER;

    private static final Set<UserType> DSR_USER_TYPE_SET =
            Collections.unmodifiableSet(EnumSet.of(ADMIN, MOH, USER));

    private static final Set<UserType> PLR_USER_TYPE_SET =
            Collections.unmodifiableSet(EnumSet.of(ADMIN, PRIMARY, SECONDARY, CONSUMER));

    private static final Map<UserType,String> REG_USER_TYPE_MAP = Map.of(
            PRIMARY, "PSRC",
            SECONDARY, "SSRC",
            CONSUMER, "CONS",
            ADMIN, "RA"
    );

    /**
     * Gets the DSR user types in this enum
     *
     * @return A set of the DSR user types
     */
    public static Set<UserType> getDsrUserTypeSet()
    {
        return DSR_USER_TYPE_SET;
    }

    /**
     * Gets the PLR user types in this enum
     *
     * @return A set of PLR user types
     */
    public static Set<UserType> getPlrUserTypeSet()
    {
        return PLR_USER_TYPE_SET;
    }

    /**
     * Gets the registry user type string for this user type
     * @return The registry user type string for this user type,
     * or null if this user type does not have a registry user type string
     */
    public String getRegUserType() { return REG_USER_TYPE_MAP.get(this); }
}
