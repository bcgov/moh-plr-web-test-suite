package ca.bc.gov.health.qa.autotest.plr.web.actions.provider;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import ca.bc.gov.health.qa.autotest.plr.util.UserType;
import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.UpdateProviderPage;
import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;

public class UpdateProviderActions {
	private static final Logger LOG = ExecutionLogManager.getLogger();
	private final SeleniumSession selenium_;
	private final URI uri_;
	private final UserType userType_;

	public UpdateProviderActions(SeleniumSession selenium, URI uri, UserType userType) {
		selenium_ = selenium;
		uri_ = uri;
		userType_ = userType;

	}
	
	/**
	 * Gets the selenium_ value.
	 *
	 * @return the selenium_
	 */
	public SeleniumSession getSelenium_() {
		return selenium_;
	}

	/**
	 * Gets the uri_ value.
	 *
	 * @return the uri_
	 */
	public URI getUri_() {
		return uri_;
	}
	
	/**
	 * Gets the userType_ value.
	 *
	 * @return the userType_
	 */
	public UserType getUserType_() {
		return userType_;
	}
	
	 /**
     * Opens the provider page for a provider given their internal provider ID.
     *
     * @param authId                internal provider ID
     * @return                      a ViewProviderPage reference to the provider page specified by authId
     *
     * @throws NullPointerException if {@code pauthId} is {@code null}
     */
    public UpdateProviderPage openProvider(String authId)
    {
        LOG.info("Open provider view ({}).", authId);
        UpdateProviderPage updateProvider =
                new UpdateProviderPage(selenium_, uri_.resolve("plr/ProviderDetails.xhtml"));
        updateProvider.openProvider(authId);
        return updateProvider;
    }
    
 /**
  * get Status Code To Reason Code Map
 * @return a Map of < key=String of Status Code, value=List<String of Reason Code> > 
 */
public Map<String, List<String>> getStatusCodeToReasonCodeMap() {
    	
    	String[] cancelArray= {"AU - Address Unknown", "INNONPRAC - Initial Non Practicing", 
    			"LAP - License Lapsed on Request", "DEN - Licensed Denied", "MEDSTUD - Medical Student", 
    			"ORG - Organization Provider", "OOP - Out of Province", "RESDISC - Resigned - disciplinary action", "RET - Retired", "UNK - Unknown", "VW - Voluntary Withdrawal"};
    	String[] activeArray= {"ASSOC - Associate", "GS - Good Standing", "LAP - License Lapsed on Request", 
    			"MEDSTUD - Medical Student", "NONPRAC - Non Practicing", "ORG - Organization Provider", "OOP - Out of Province", 
    			"PRAC - Practising", "RET - Retired", "SPE - Special Registry", "TEMPPER - Temporary Permit", "UNK - Unknown"};
    	String[] terminatedArray= {"AU - Address Unknown", "DEC - Deceased", "ERSRES - Erased by Resolution", 
    			"HON - Honorary", "LTP - Left the Province", "LAP - License Lapsed on Request", "MEDSTUD - Medical Student", 
    			"NONPRAC - Non Practicing", "NR - Non-resident", "ORG - Organization Provider", "OOP - Out of Province", "RESDISC - Resigned - disciplinary action", 
    			"RET - Retired", "TI - Temporary Inactive", "TSF - Transfer", "UNK - Unknown"};
    	String[] inactiveArray= {"MEDSTUD - Medical Student", "ORG - Organization Provider", "OOP - Out of Province", "UNK - Unknown"};
    	String[] suspendedArray= {"AU - Address Unknown", "HON - Honorary", "LTP - Left the Province", 
    			"LAP - License Lapsed on Request", "MEDSTUD - Medical Student", "MIS - Missionary", "NONPAY - Non Payment of Fee", 
    			"NONPRAC - Non Practicing", "NR - Non-resident", "ORG - Organization Provider", "OOP - Out of Province", 
    			"RESDISC - Resigned - disciplinary action", "RET - Retired", "SUS - Suspended", "TI - Temporary Inactive", "UNK - Unknown", "VW - Voluntary Withdrawal"};
    	String[] nullifiedArray= {"MEDSTUD - Medical Student", "ORG - Organization Provider", "OOP - Out of Province", "UNK - Unknown"};
    	String[] pendingArray= {"INNONPRAC - Initial Non Practicing", "MEDSTUD - Medical Student", "NONPRAC - Non Practicing", "ORG - Organization Provider", "OOP - Out of Province", "UNK - Unknown"};
    	String[] unknownArray= {"MEDSTUD - Medical Student", "ORG - Organization Provider",	"OOP - Out of Province", "UNK - Unknown"};
    	
    	
    	
    	Map<String, List<String>> map = new HashMap<>();
    	
    	map.put("CANCELLED - Cancelled",Arrays.asList(cancelArray));
    	map.put("ACTIVE - Active",Arrays.asList(activeArray));
    	map.put("TERMINATED - Terminated",Arrays.asList(terminatedArray));
    	map.put("INACTIVE - Inactive",Arrays.asList(inactiveArray));
    	map.put("SUSPENDED - Suspended",Arrays.asList(suspendedArray));
    	map.put("NULLIFIED - Nullified",Arrays.asList(nullifiedArray));
    	map.put("PENDING - Pending",Arrays.asList(pendingArray));
    	map.put("UNKNOWN - Unknown",Arrays.asList(unknownArray));
    	return map;
    }

}
