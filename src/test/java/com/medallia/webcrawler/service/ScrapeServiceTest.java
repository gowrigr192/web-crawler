package com.medallia.webcrawler.service;

import com.medallia.webcrawler.exception.ApiException;
import com.medallia.webcrawler.model.DomainAndImagesMappingRequest;
import com.medallia.webcrawler.model.ScrapeResponse;
import com.medallia.webcrawler.util.JsoupUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ScrapeServiceTest {

	@Mock
	private DataSourceService dataSourceService;
	@Mock
	private JsoupUtil jsoupUtil;
	private ScrapeService scrapeService;

	@BeforeEach
	public void setup() {
		scrapeService = new ScrapeService(dataSourceService, jsoupUtil);
	}

	@Test
	public void testExtractAndSaveImages() throws IOException {
		String url = "https://unitTest.com";
		Set<String> imageUrlList = new HashSet<>();
		imageUrlList.add("https://unitTest.com/image1.jpg");
		imageUrlList.add("https://unitTest.com/image2.jpg");

		doNothing().when(dataSourceService).saveImagesIntoForeignDB(Mockito.anyString(), Mockito.anySet(), Mockito.anyBoolean());
		doNothing().when(dataSourceService).saveDataLocally(Mockito.anyString(), Mockito.anySet());
		when(jsoupUtil.extractImageUrls(url)).thenReturn(imageUrlList);

		ScrapeResponse result = scrapeService.extractAndSaveImages(url);

		assertEquals(url.substring(8), result.getDomain());
		assertEquals(imageUrlList, result.getImageUrls());
	}

	@Test
	public void testExtractAndSaveImagesWithInvalidUrlThrowingIOException() throws IOException {
		doNothing().when(dataSourceService).saveImagesIntoForeignDB(Mockito.anyString(), Mockito.anySet(), Mockito.anyBoolean());
		doNothing().when(dataSourceService).saveDataLocally(Mockito.anyString(), Mockito.anySet());

		when(jsoupUtil.extractImageUrls(Mockito.anyString())).thenThrow(new IOException("Mocked IO Exception"));

		assertThrows(ApiException.class, () -> {
			scrapeService.extractAndSaveImages("https://unitTest.com");
		});
	}

	@Test
	public void testSaveAllDomainAndImages() {
		doNothing().when(dataSourceService).saveImagesIntoForeignDB(Mockito.anyString(), Mockito.anySet(), Mockito.anyBoolean());
		List<DomainAndImagesMappingRequest> list = new ArrayList<>();
		list.add(new DomainAndImagesMappingRequest("test_domain", new ArrayList<>()));
		String response = scrapeService.saveAllDomainAndImages(list);
		assertNotNull(response);
		assertEquals("All images saved", response);
	}

	@Test
	public void testSaveAllDomainAndImagesWhereRuntimeExceptionOccurs() {
		doThrow(new RuntimeException("Test exception")).when(dataSourceService).saveImagesIntoForeignDB(Mockito.anyString(), Mockito.anySet(), Mockito.anyBoolean());
		List<DomainAndImagesMappingRequest> list = new ArrayList<>();
		list.add(new DomainAndImagesMappingRequest("test_domain", new ArrayList<>()));
		assertThrows(ApiException.class, () -> {
			scrapeService.saveAllDomainAndImages(list);
		});
	}
}
