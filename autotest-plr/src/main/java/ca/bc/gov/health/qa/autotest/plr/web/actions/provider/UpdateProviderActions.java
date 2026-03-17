package ca.bc.gov.health.qa.autotest.plr.web.actions.provider;

import java.net.URI;

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

}
