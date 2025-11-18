package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.runner.util.log.ExecutionLogManager;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

public class AddFacilitySummaryFragment extends BasicWebPageFragment {

    private static final Logger LOG = ExecutionLogManager.getLogger();

    private static final String SUBMIT_BUTTON_CSS = "button#form\\:addProviderSubmit";

    private static final String FACTYPE_FIELD_CSS = "input#form\\:facilityTypeCon";

    private static final String FACNAME_FIELD_CSS = "input#form\\:facilityNameCon";

    private static final String DESCRIPTION_FIELD_CSS = "input#form\\:facilityDescriptionCon";

    private static final String ADDRESS_TABLES_CSS = "table#form\\:panelGr > tbody > tr > td";

    public AddFacilitySummaryFragment(SeleniumSession selenium)
    {
        super(selenium,
                By.xpath("//table//tbody//tr//td//div//div//span[contains(text(),'Facility Summary')]"));
    }

    public ViewFacilityPage clickSubmitButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(SUBMIT_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));

        return new ViewFacilityPage(selenium_);
    }

    public String getFacilityType()
    {
        return selenium_.findElement(By.cssSelector(FACTYPE_FIELD_CSS)).getAttribute("value");
    }

    public String getFacilityName()
    {
        return selenium_.findElement(By.cssSelector(FACNAME_FIELD_CSS)).getAttribute("value");
    }

    public String getDescription()
    {
        return selenium_.findElement(By.cssSelector(DESCRIPTION_FIELD_CSS)).getAttribute("value");
    }

    public List<String> getCivicAddress()
    {
        final String civicAddressSelector = ADDRESS_TABLES_CSS + ":first-child > table > tbody > tr > td";
        return selenium_.findElementsByCss(civicAddressSelector).stream().map(WebElement::getText).toList();
    }

    public List<String> getMailingAddress()
    {
        final String mailingAddressSelector = ADDRESS_TABLES_CSS + ":last-child > table > tbody > tr > td";
        return selenium_.findElementsByCss(mailingAddressSelector).stream().map(WebElement::getText).toList();
    }
}
