package ca.bc.gov.health.qa.autotest.plr.util;

/**
 * TODO (AZ) - doc
 */
public class PlrUtils
{
    private PlrUtils()
    {}

    /**
     * TODO (AZ) - doc
     *
     * @param parameters
     *        ???
     *
     * @return ???
     */
    public static UserType detectUserType(Object[] parameters)
    {
        UserType userType = null;
        for (Object parameter : parameters)
        {
            if (parameter instanceof UserType)
            {
                userType = (UserType) parameter;
                break;
            }
        }
        return userType;
    }
}
