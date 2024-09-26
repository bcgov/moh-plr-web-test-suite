package ca.bc.gov.health.qa.autotest.plr.util;

import java.util.Collections;
import java.util.EnumSet;
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

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public static Set<UserType> getDsrUserTypeSet()
    {
        return DSR_USER_TYPE_SET;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public static Set<UserType> getPlrUserTypeSet()
    {
        return PLR_USER_TYPE_SET;
    }
}
