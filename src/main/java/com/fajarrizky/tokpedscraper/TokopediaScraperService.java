package com.fajarrizky.tokpedscraper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeading1;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlMeta;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class TokopediaScraperService {
    private static String PRODUCT_DATA_TEST_ID_KEY = "div.data-testid";
    private static String PRODUCT_DATA_TEST_ID_VALUE = "master-product-card";
    private static String FILE_PATH = "/log.csv";

    @Value("${website.baseUrl}")
    private String baseUrl;

    public void fetchTop100Products() throws Exception {

        List<ProductDTO> products = new ArrayList<>();
        List<String> productUrls = new ArrayList<>();

        try (final WebClient webClient = new WebClient()) {
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setUseInsecureSSL(true);

            String pagingUrlSuffix = "?page=";

            //Apologize, I understand that this is not
            //fetching top 100 products, but instead
            //fetching top 10 on each page, but this is
            //because tokopedia uses infinite scrolling
            //to load the items, which I can't find
            //the solution if I'm using htmlUnit library.
            //I considered switching to Selenium library but
            //I didn't much time to finish
            boolean shouldBreak = false;
            for (int i = 1; i <= 20; ++i) {
                HtmlPage page = webClient.getPage(baseUrl + pagingUrlSuffix + i);
                webClient.getCurrentWindow().setInnerHeight(Integer.MAX_VALUE);
                webClient.waitForBackgroundJavaScript(4000);
                HtmlElement listElement = page.getFirstByXPath("//div[@data-testid='lstCL2ProductList']");

                for (DomElement element : listElement.getChildElements()) {
                    if (element instanceof HtmlDivision) {
                        DomElement productElement = element.getFirstElementChild();
                        String productPageUrl = productElement.getAttribute("href");
                        if(!productPageUrl.isEmpty()) {
                            if(productPageUrl.contains("ta.tokopedia.com")) {
                                productPageUrl = productPageUrl.substring(productPageUrl.indexOf("https%3A%2F%2Fwww.tokopedia.com"));
                                productPageUrl = productPageUrl.replace("%3A", ":");
                                productPageUrl = productPageUrl.replace("%2F", "/");
                                productPageUrl = productPageUrl.replace("%3F", "?");
                                productPageUrl = productPageUrl.replace("%3D", "=");
                            }
                            productUrls.add(productPageUrl);
                        }
                        if(productUrls.size() == 100) {
                            shouldBreak = true;
                            break;
                        }
                    }
                }
                if(shouldBreak) break;
            }
        }

        for (String url : productUrls) {
            try(final WebClient webClient = new WebClient()) {
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setUseInsecureSSL(true);

                HtmlPage page = webClient.getPage(url);
                webClient.getCurrentWindow().setInnerHeight(Integer.MAX_VALUE);
                webClient.waitForBackgroundJavaScript(4000);

                ProductDTO productDTO = new ProductDTO();
                HtmlHeading1 productName = page.getFirstByXPath("//h1[@data-testid='lblPDPDetailProductName']");
                productDTO.setName(productName.getFirstChild().getNodeValue());

                HtmlMeta ratingMeta = page.getFirstByXPath("//meta[@itemprop='ratingValue']");
                if(ratingMeta != null) {
                    productDTO.setRating(ratingMeta.getContentAttribute());
                }
                HtmlDivision descriptionDiv = page.getFirstByXPath("//div[@data-testid='lblPDPDescriptionProduk']");
                if(descriptionDiv != null) {
                    productDTO.setDescription(descriptionDiv.getFirstChild().getNodeValue());
                }
                
                HtmlImage imageUrl = page.getFirstByXPath("//img[@class='success fade']");
                productDTO.setImgUrl(imageUrl.getSrc());

                HtmlDivision priceDiv = page.getFirstByXPath("//div[@class='price']");
                productDTO.setPrice(priceDiv.getFirstChild().getNodeValue());

                productDTO.setMerchant(url.split("/")[3]);
                products.add(productDTO);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (ProductDTO productDTO : products) {
            sb.append(productDTO.toCsvString());
            sb.append("\n");
        }
        printToFile(sb.toString());

    }

    private void printToFile(String content) throws Exception {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        BufferedWriter writer = new BufferedWriter(new FileWriter(s + FILE_PATH));
        writer.write(content);

        writer.close();
    }
}
