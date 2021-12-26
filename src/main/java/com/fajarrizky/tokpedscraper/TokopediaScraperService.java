package com.fajarrizky.tokpedscraper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokopediaScraperService {
    @Value("${website.baseUrl}")
    String baseUrl;

        
}
