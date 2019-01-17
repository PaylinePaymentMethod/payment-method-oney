package com.payline.payment.oney.integration;

import com.payline.payment.oney.service.impl.PaymentServiceImpl;
import com.payline.payment.oney.service.impl.PaymentWithRedirectionServiceImpl;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.PaymentFormContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.integration.AbstractPaymentIntegration;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.payline.payment.oney.utils.OneyConstants.*;


public class TestIt extends AbstractPaymentIntegration {
    private PaymentServiceImpl paymentService = new PaymentServiceImpl();
    private PaymentWithRedirectionServiceImpl paymentWithRedirectionService = new PaymentWithRedirectionServiceImpl();

    @Override
    protected Map<String, ContractProperty> generateParameterContract() {
        HashMap<String, ContractProperty> contractConfiguration = new HashMap();
        contractConfiguration.put(OPC_KEY, new ContractProperty("3x002"));
        contractConfiguration.put(MERCHANT_GUID_KEY, new ContractProperty("9813e3ff-c365-43f2-8dca-94b850befbf9"));
        contractConfiguration.put(PSP_GUID_KEY, new ContractProperty("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a"));
        contractConfiguration.put(API_MARKETING_KEY, new ContractProperty("01c6ea9021574d608c631f1c3b880b3be"));
        contractConfiguration.put(NB_ECHEANCES_KEY, new ContractProperty("3"));
        contractConfiguration.put(COUNTRY_CODE_KEY, new ContractProperty("BE")); // caractères
        contractConfiguration.put(LANGUAGE_CODE_KEY, new ContractProperty("FR"));
        contractConfiguration.put(ID_INTERNATIONAL_KEY, new ContractProperty("BE"));
        return contractConfiguration;
    }

    @Override
    protected PaymentFormContext generatePaymentFormContext() {
        Map<String, String> paymentFormParameter = new HashMap<>();
        paymentFormParameter.put(PSP_GUID_KEY, "6ba2a5e2-df17-4ad7-8406-6a9fc488a60a");
        Map<String, String> sensitivePaymentFormParameter = new HashMap<>();
        sensitivePaymentFormParameter.put(PARTNER_CHIFFREMENT_KEY, "66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=");

        return PaymentFormContext.PaymentFormContextBuilder
                .aPaymentFormContext()
                .withPaymentFormParameter(paymentFormParameter)
                .withSensitivePaymentFormParameter(sensitivePaymentFormParameter)
                .build();
    }

    @Override
    protected String payOnPartnerWebsite(String partnerUrl) {
        // Start browser
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        try {
            // Go to partner's website
            driver.get(partnerUrl);

            String struct = "//*[@id='wt1_wt7_Oney_Theme_wt158_block_wtMainContent_WebPatterns_wtStructure";
            //Id du bouton nouveau client
            driver.findElement(By.xpath(struct + "_block_wtColumn1_wtWithoutAccount_rb2']")).click();
//            driver.findElement(By.name("wt1_wt7$1105887523")).click();
            //champs date de naissance
            WebDriverWait wait0 = new WebDriverWait(driver, 30);
            wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(struct + "_block_wtColumn1_WebPatterns_wt20_block_wtColumn1_DateSelector_wt78_block_wtShowDay']")));
            new Select(driver.findElement(By.xpath(struct + "_block_wtColumn1_WebPatterns_wt20_block_wtColumn1_DateSelector_wt78_block_wtShowDay']"))).selectByVisibleText("1");

            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(struct + "_block_wtColumn1_WebPatterns_wt20_block_wtColumn1_DateSelector_wt78_block_wtShowMonth']")));
            new Select(driver.findElement(By.xpath(struct + "_block_wtColumn1_WebPatterns_wt20_block_wtColumn1_DateSelector_wt78_block_wtShowMonth']"))).selectByVisibleText("Avr");

            WebDriverWait wait2 = new WebDriverWait(driver, 30);
            wait2.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(struct + "_block_wtColumn1_WebPatterns_wt20_block_wtColumn1_DateSelector_wt78_block_wtShowYear']")));
            new Select(driver.findElement(By.xpath(struct + "_block_wtColumn1_WebPatterns_wt20_block_wtColumn1_DateSelector_wt78_block_wtShowYear']"))).selectByVisibleText("1993");

            //nationalite
            WebDriverWait wait4 = new WebDriverWait(driver, 30);
            wait4.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(struct + "_block_wtColumn1_WebPatterns_wt20_block_wtColumn2_wtCustomerData_NacionalityInput']")));
            driver.findElement(By.xpath(struct + "_block_wtColumn1_WebPatterns_wt20_block_wtColumn2_wtCustomerData_NacionalityInput']")).sendKeys("Belge");

            //birthplace
            WebDriverWait wait9 = new WebDriverWait(driver, 10);
            driver.findElement(By.xpath(struct + "_block_wtColumn1_wtCustomerData_BirthPlaceInput']")).sendKeys("Bruxelles");

            // Les données de CB sont dasn une iFrame ...
            driver.switchTo().frame("cko-iframe-id");
            //numero de CB
            driver.findElement(By.xpath("//*[@data-checkout='card-number']")).sendKeys("4543474002249996");

            //CVV CB
            WebDriverWait wait5 = new WebDriverWait(driver, 30);
            wait5.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@data-checkout='cvv']")));
            WebElement elt = driver.findElement(By.xpath("//*[@data-checkout='cvv']"));
            int i = 0;
            do {
                elt.sendKeys("956");
                i++;
            }
            while (elt.getAttribute("value") == null || elt.getAttribute("value").length() != 3 || i < 5);


            //mois de expiration CB
            driver.findElement(By.xpath("//*[@data-checkout='expiry-month']")).sendKeys("06");
            //annne expiration de CB
            driver.findElement(By.xpath("//*[@data-checkout='expiry-year']")).sendKeys("25");

            driver.switchTo().defaultContent();
            //Checkboxes a cocher
            List<WebElement> els = driver.findElements(By.xpath("//input[@type='checkbox']"));
            els.get(1).click();
            els.get(2).click();
            // validate payment
            driver.findElement(By.xpath("//*[@value='Accepter']")).click();
            // Wait for redirection to success or cancel url
//            WebDriverWait wait3 = new WebDriverWait(driver, 80);
//            wait3.until(ExpectedConditions.or(ExpectedConditions.urlToBe(SUCCESS_URL), ExpectedConditions.urlToBe(CANCEL_URL)));

            //page 2 confirmation de l'otp
            WebDriverWait wait6 = new WebDriverWait(driver, 30);
            wait6.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='txtPassword']")));
            driver.findElement(By.xpath("//*[@id='txtPassword']")).sendKeys("Checkout1!");

            //confirmer
            WebDriverWait wait7 = new WebDriverWait(driver, 30);
            wait7.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='txtButton']")));
            driver.findElement(By.xpath("//*[@id='txtButton']")).click();

            //page 3 creation d'un compte Oney
            WebDriverWait wait8 = new WebDriverWait(driver, 30);
            wait8.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='Oney_Theme_wt32_block_wtMainContent_WebPatterns_wt73_block_wtColumn2_WebPatterns_wt41_block_wtColumn1_wtPW3']")));
            driver.findElement(By.xpath("//*[@id='Oney_Theme_wt32_block_wtMainContent_WebPatterns_wt73_block_wtColumn2_WebPatterns_wt41_block_wtColumn1_wtPW3']")).sendKeys("Azerty1!");

//            WebDriverWait wait19 = new WebDriverWait(driver, 30);
//            wait19.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='Oney_Theme_wt32_block_wtMainContent_WebPatterns_wt73_block_wtColumn2_WebPatterns_wt41_block_wtColumn1_wtPW4']")));
            driver.findElement(By.xpath("//*[@id='Oney_Theme_wt32_block_wtMainContent_WebPatterns_wt73_block_wtColumn2_WebPatterns_wt41_block_wtColumn2_wtPW4']")).sendKeys("Azerty1!");


            //valider  creation de compte Oney
            WebDriverWait wait10 = new WebDriverWait(driver, 30);
            wait10.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='Oney_Theme_wt32_block_wtMainContent_wtbtnFinishAndActivate']")));
            driver.findElement(By.xpath("//*[@id='Oney_Theme_wt32_block_wtMainContent_wtbtnFinishAndActivate']")).click();

            wait.until(ExpectedConditions.or(ExpectedConditions.urlToBe(SUCCESS_URL), ExpectedConditions.urlToBe(CANCEL_URL)));

            return driver.getCurrentUrl();
            //debug

        } finally {
            driver.quit();
        }

    }

    @Override
    protected String cancelOnPartnerWebsite(String s) {
        return null;
    }

    @Test
    public void fullPaymentTest() {
        PaymentRequest request = createDefaultPaymentRequest();
        this.fullRedirectionPayment(request, paymentService, paymentWithRedirectionService);

    }


    @Override
    public PaymentRequest createDefaultPaymentRequest() {
        return TestUtils.createCompletePaymentBuilder().build();

    }
}
