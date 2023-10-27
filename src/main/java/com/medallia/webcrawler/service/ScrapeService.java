package com.medallia.webcrawler.service;

import com.medallia.webcrawler.exception.ApiException;
import com.medallia.webcrawler.model.DomainAndImagesMappingRequest;
import com.medallia.webcrawler.model.ScrapeResponse;
import com.medallia.webcrawler.util.JsoupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class ScrapeService {
    private static final Logger LOG = LoggerFactory.getLogger(ScrapeService.class);
    private final DataSourceService dataSourceService;
    private final JsoupUtil jsoupUtil;

    @Autowired
    public ScrapeService(DataSourceService dataSourceService, JsoupUtil jsoupUtil) {
        this.dataSourceService = dataSourceService;
        this.jsoupUtil = jsoupUtil;
    }
    public ScrapeResponse extractAndSaveImages(String url) {
        Set<String> imageUrlList;
        try {
            imageUrlList = jsoupUtil.extractImageUrls(url);
        } catch (IOException e) {
            LOG.error("IO exception while connecting to the url {}, the exception:{}", url, e);
            throw new ApiException(e.getMessage());
        }
        String domain = extractDomain(url);
        dataSourceService.saveDataLocally(domain, imageUrlList);
        dataSourceService.saveImagesIntoForeignDB(domain, imageUrlList, true);
        return new ScrapeResponse(domain, imageUrlList);
    }

    public String saveAllDomainAndImages(List<DomainAndImagesMappingRequest> domainAndImagesMappingRequests) {
        try {
            domainAndImagesMappingRequests.forEach(domainAndImagesMappingRequest -> {
                dataSourceService.saveImagesIntoForeignDB(domainAndImagesMappingRequest.getDomain(),
                        new HashSet<>(domainAndImagesMappingRequest.getImageUrls()), false);
            });
            return "All images saved";
        } catch (Exception e) {
            LOG.error("Exception occurred while saving data {}", e);
            throw new ApiException(e.getMessage());
        }
    }

    private String extractDomain(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
