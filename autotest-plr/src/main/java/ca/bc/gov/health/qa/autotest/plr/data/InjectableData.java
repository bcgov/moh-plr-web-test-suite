package ca.bc.gov.health.qa.autotest.plr.data;

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.health.qa.autotest.plr.web.tests.model.ProviderRoleType;
import org.testng.annotations.DataProvider;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;

/**
 * Provides data for TestNG data-driven tests related to injectable providers and users.
 */
public class InjectableData
{
    private InjectableData()
    {}

    /**
     * Returns a two-dimensional array of all provider types for use in TestNG data-driven tests.
     *
     * @return a two-dimensional array of all provider types, where each inner array contains a single ProviderType value
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
     * Returns a two-dimensional array of practitioner provider types for use in TestNG data-driven tests.
     * @return a two-dimensional array of practitioner provider types,
     *         where each inner array contains a single ProviderType value that is not ORGANIZATION
     */
    @DataProvider(name = "practitioners")
    public static Object[][] getPractitioners()
    {
        List<Object[]> data = new ArrayList<>();
        for (ProviderType providerType : ProviderType.values())
        {
            if (!providerType.equals(ProviderType.ORGANIZATION)) data.add(new Object[]{providerType});
        }
        return toArray(data);
    }

    /**
     * Returns a two-dimensional array of OOP ProviderRoleTypes for use in TestNG data-driven tests.
     * @return a two-dimensional array of OOP ProviderRoleTypes,
     *         where each inner array contains a single ProviderRoleType value
     */
    @DataProvider(name = "oopRoleTypes")
    public static Object[][] getOopRoleTypes()
    {
        List<Object[]> data = new ArrayList<>();
        for (ProviderRoleType roleType : ProviderRoleType.getProviderRoleTypeSet(ProviderType.OOP_PRACTITIONER)) {
            data.add(new Object[]{roleType});
        }
        return toArray(data);
    }

    /**
     * Returns a two-dimensional array of practitioner role types for use in TestNG data-driven tests.
     * @return a two-dimensional array of practitioner role types,
     *         where each inner array contains a ProviderRoleType value and its corresponding ProviderType value
     */
    @DataProvider(name = "practitionerRoleTypes")
    public static Object[][] getPractitionerRoleTypes()
    {
        List<Object[]> data = new ArrayList<>();
        for (ProviderType providerType : ProviderType.values())
        {
            if (providerType.equals(ProviderType.ORGANIZATION)) continue;
            for (ProviderRoleType roleType : ProviderRoleType.getProviderRoleTypeSet(providerType)) {
                data.add(new Object[]{providerType, roleType});
            }

        }
        return toArray(data);
    }

    /**
     * Returns a two-dimensional array of all PLR user types for use in TestNG data-driven tests.
     *
     * @return a two-dimensional array of all PLR user types, where each inner array contains a single UserType value
     * Provides just the BC Practitioner and Organization provider types for tests that need to cover only these types.
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
     * Provides just the BC Practitioner and Organization provider types for tests that need to cover only these types.
     * (Includes null for second parameter to match builder method signature for a specific test)
     *
     * @return A two-dimensional array of BC Practitioner and Organization provider types.
     */
    @DataProvider(name = "indOrgBuilderTypes")
    public static Object[][] getIndOrgBuilderTypes()
    {
        List<Object[]> data = new ArrayList<>();
        for (ProviderType providerType : List.of(ProviderType.BC_PRACTITIONER, ProviderType.ORGANIZATION))
        {
            data.add(new Object[]{providerType,null});
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
     * Provides a matrix of user types for provider tests.
     *
     * @return a two-dimensional array of test parameters
     */
    @DataProvider(name = "providerTestUserTypes")
    public static Object[][] getProviderTestUserTypes()
    {
        List<Object[]> data = new ArrayList<>();
        
        data.add(new Object[]{UserType.ADMIN});
        data.add(new Object[]{UserType.PRIMARY});
        data.add(new Object[]{UserType.SECONDARY});
        data.add(new Object[]{UserType.CONSUMER});
       
        return toArray(data);
    }
    
    /**
     * Provides a matrix of user types for provider tests excluding ADMIN.
     *
     * @return a two-dimensional array of test parameters
     */
    @DataProvider(name = "providerTestUserTypesNonAdmin")
    public static Object[][] getProviderTestUserTypesNonAdmin()
    {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{UserType.PRIMARY});
        data.add(new Object[]{UserType.SECONDARY});
        data.add(new Object[]{UserType.CONSUMER});
       
        return toArray(data);
    }
    
    

    /**
     * Returns a two-dimensional array of all combinations of PLR user types and provider types for use in TestNG data-driven tests.
     *
     * @return a two-dimensional array of all combinations of PLR user types and provider types,
     *         where each inner array contains a UserType value and a ProviderType value
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
