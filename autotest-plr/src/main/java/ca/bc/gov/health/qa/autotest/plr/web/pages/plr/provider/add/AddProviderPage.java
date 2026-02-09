package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.add;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPage;
import org.openqa.selenium.By;

/**
 * Page object for the Add Provider page
 */
public class AddProviderPage extends BasicWebPage {

    /**
     * Initializes page object and changes selenium's main locator to the Add Provider Heading
     * @param selenium the selenium session
     */
    public AddProviderPage(SeleniumSession selenium) {
        super(selenium,
                By.xpath("//div[@id='content']//h2[contains(text(),'Add Provider')]"),
                "Add Provider");
    }
}
