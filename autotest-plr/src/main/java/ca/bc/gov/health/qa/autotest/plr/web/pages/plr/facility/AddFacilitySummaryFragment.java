package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import ca.bc.gov.health.qa.autotest.runner.util.selenium.SeleniumSession;
import ca.bc.gov.health.qa.autotest.runner.util.selenium.pages.BasicWebPageFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AddFacilitySummaryFragment extends BasicWebPageFragment {

    private static final String SUBMIT_BUTTON_CSS = "button#form\\:addProviderSubmit";

    private static final String FACTYPE_FIELD_CSS = "input#form\\:facilityTypeCon";

    private static final String FACNAME_FIELD_CSS = "input#form\\:facilityNameCon";

    private static final String DESCRIPTION_FIELD_CSS = "input#form\\:facilityDescriptionCon";

    public AddFacilitySummaryFragment(SeleniumSession selenium)
    {
        super(selenium,
                By.xpath("//table//tbody//tr//td//div//div//span[contains(text(),'Facility Summary')]"));
    }

    public void clickSubmitButton()
    {
        WebElement button = selenium_.findElement(By.cssSelector(SUBMIT_BUTTON_CSS));
        button.click();
        selenium_.waitUntil(ExpectedConditions.stalenessOf(button));
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

}
