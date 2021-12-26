package com.fajarrizky.tokpedscraper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScraperRESTAPI {

    @Autowired
    TokopediaScraperService scraperService;

    @GetMapping(path = "/scrapeTokopedia")
    public void fetchTopProductAndPrintCSV() throws Exception {
        scraperService.fetchTop100Products();
    }
}
