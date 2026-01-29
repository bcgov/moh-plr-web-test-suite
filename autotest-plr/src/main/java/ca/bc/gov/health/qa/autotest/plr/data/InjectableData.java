package ca.bc.gov.health.qa.autotest.plr.data;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;

/**
 * Data Provider class to inject independent case types for testing (provider + user types typically)
 */
public class InjectableData
{
    private InjectableData()
    {}

    /**
     * Provides a matrix of all provider types
     *
     * @return A two-dimensional array of all provider types.
     */
    @DataProvider(name = "allProviderTypes")
    public static Object[][] getAllProviderTypes()
    {
        List<Object[]> data = new ArrayList<>();
        for (ProviderType providerType : ProviderType.values())
        {
            data.add(new Object[]{providerType});
        }
        return toArray(data);
    }

    /**
     * Provides just the BC Practitioner and Organization provider types for tests that need to cover only these types.
     *
     * @return A two-dimensional array of BC Practitioner and Organization provider types.
     */
    @DataProvider(name = "indOrgTypes")
    public static Object[][] getIndOrgTypes()
    {
        List<Object[]> data = new ArrayList<>();
        for (ProviderType providerType : List.of(ProviderType.BC_PRACTITIONER, ProviderType.ORGANIZATION))
        {
            data.add(new Object[]{providerType});
        }
        return toArray(data);
    }

    /**
     * Provides a matrix of user types in PLR.
     *
     * @return a two-dimensional array of test parameters
     */
    @DataProvider(name = "allPlrUserTypes")
    public static Object[][] getAllPlrUserTypes()
    {
        List<Object[]> data = new ArrayList<>();
        for (UserType userType : UserType.getPlrUserTypeSet())
        {
            data.add(new Object[]{userType});
        }
        return toArray(data);
    }

    /**
     * Provides a matrix of user types for facility tests.
     *
     * @return a two-dimensional array of test parameters
     */
    @DataProvider(name = "facilityTestUserTypes")
    public static Object[][] getFacilityTestUserTypes()
    {
        List<Object[]> data = new ArrayList<>();
        
        data.add(new Object[]{UserType.ADMIN});
        data.add(new Object[]{UserType.PRIMARY});
        data.add(new Object[]{UserType.SECONDARY});
        data.add(new Object[]{UserType.CONSUMER});
       
        return toArray(data);
    }

    /**
     * Provides a matrix of user type in PLR combined with each provider type.
     *
     * @return a two-dimensional array of each possible pair of PLR user type and provider type
     */
    @DataProvider(name = "allPlrUserTypesProviderTypes")
    public static Object[][] getAllPlrUserTypesProviderTypes()
    {
        List<Object[]> data = new ArrayList<>();
        for (UserType userType : UserType.getPlrUserTypeSet())
        {
            for (ProviderType providerType : ProviderType.values())
            {
                data.add(new Object[]{userType, providerType});
            }
        }
        return toArray(data);
    }

    private static Object[][] toArray(List<Object[]> data)
    {
        return data.toArray(new Object[data.size()][]);
    }
}
