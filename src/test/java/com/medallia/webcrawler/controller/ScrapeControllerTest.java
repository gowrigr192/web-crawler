package com.medallia.webcrawler.controller;

import com.medallia.webcrawler.exception.ApiException;
import com.medallia.webcrawler.model.DomainAndImagesMappingRequest;
import com.medallia.webcrawler.model.ScrapeRequest;
import com.medallia.webcrawler.model.ScrapeResponse;
import com.medallia.webcrawler.service.ScrapeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ScrapeControllerTest {

	@Mock
	private ScrapeService scrapeService;
	private ScrapeController scrapeController;

	@BeforeEach
	public void setup() {
		scrapeController = new ScrapeController(scrapeService);
	}

	@Test
	public void testScrapePage() {
		String url = "https://example.com";
		ScrapeRequest request = new ScrapeRequest();
		request.setUrl(url);

		ScrapeResponse response = new ScrapeResponse(url, null);
		when(scrapeService.extractAndSaveImages(url)).thenReturn(response);

		ResponseEntity<?> result = scrapeController.scrapePage(request);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(response, result.getBody());

		verify(scrapeService, times(1)).extractAndSaveImages(url);
	}

	@Test
	public void testScrapePageWithNullResponse() {
		String url = "https://example.com";
		ScrapeRequest request = new ScrapeRequest();
		request.setUrl(url);

		when(scrapeService.extractAndSaveImages(url)).thenReturn(null);

		ResponseEntity<?> result = scrapeController.scrapePage(request);

		assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
		assertEquals("Could not find data to scrape", result.getBody());

		verify(scrapeService, times(1)).extractAndSaveImages(url);
	}

	@Test
	public void testScrapePageWithException() {
		String url = "https://example.com";
		ScrapeRequest request = new ScrapeRequest();
		request.setUrl(url);

		when(scrapeService.extractAndSaveImages(url)).thenThrow(new ApiException("Mocked Exception"));

		ResponseEntity<?> result = scrapeController.scrapePage(request);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

		verify(scrapeService, times(1)).extractAndSaveImages(url);
	}

	@Test
	public void testSaveAll() {
		List<DomainAndImagesMappingRequest> request = new ArrayList<>();
		String response = "not null";
		when(scrapeService.saveAllDomainAndImages(request)).thenReturn(response);

		ResponseEntity<?> result = scrapeController.saveAll(request);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(response, result.getBody());

		verify(scrapeService, times(1)).saveAllDomainAndImages(request);
	}

	@Test
	public void testSaveAllWithNullResponse() {
		List<DomainAndImagesMappingRequest> request = new ArrayList<>();
		when(scrapeService.saveAllDomainAndImages(request)).thenReturn(null);

		ResponseEntity<?> result = scrapeController.saveAll(request);

		assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
		assertEquals("Could not save the data", result.getBody());

		verify(scrapeService, times(1)).saveAllDomainAndImages(request);
	}

	@Test
	public void testSaveAllWithException() {
		List<DomainAndImagesMappingRequest> request = new ArrayList<>();
		when(scrapeService.saveAllDomainAndImages(request)).thenThrow(new ApiException("test exception"));

		ResponseEntity<?> result = scrapeController.saveAll(request);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

		verify(scrapeService, times(1)).saveAllDomainAndImages(request);
	}
}
