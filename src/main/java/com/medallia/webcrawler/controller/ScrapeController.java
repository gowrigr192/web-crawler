package com.medallia.webcrawler.controller;

import com.medallia.webcrawler.exception.ApiException;
import com.medallia.webcrawler.model.DomainAndImagesMappingRequest;
import com.medallia.webcrawler.model.ScrapeRequest;
import com.medallia.webcrawler.model.ScrapeResponse;
import com.medallia.webcrawler.service.ScrapeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ScrapeController {

    private final ScrapeService scrapeService;

    @Autowired
    public ScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    @PostMapping(value = "/scrape", produces = "application/json")
    public ResponseEntity<?> scrapePage(@RequestBody ScrapeRequest request) {
        try {
            ScrapeResponse scrapedData = scrapeService.extractAndSaveImages(request.getUrl());
            if (scrapedData == null) {
                return new ResponseEntity<>("Could not find data to scrape", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(scrapedData, HttpStatus.OK);
            }
        } catch (ApiException e) {
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/saveAll", produces = "application/json")
    public ResponseEntity<?> saveAll(@RequestBody List<DomainAndImagesMappingRequest> request) {
        try {
            String response = scrapeService.saveAllDomainAndImages(request);
            if (response == null) {
                return new ResponseEntity<>("Could not save the data", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (ApiException e) {
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
