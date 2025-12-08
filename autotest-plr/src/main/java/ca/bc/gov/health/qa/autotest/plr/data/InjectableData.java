package ca.bc.gov.health.qa.autotest.plr.data;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;

import ca.bc.gov.health.qa.autotest.plr.util.ProviderType;
import ca.bc.gov.health.qa.autotest.plr.util.UserType;

/**
 * TODO (AZ) - doc
 */
public class InjectableData
{
    private InjectableData()
    {}

    /**
     * TODO (AZ) - doc
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @return ???
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
     * TODO (AZ) - doc
     *
     * @return ???
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
