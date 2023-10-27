package com.medallia.webcrawler.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JsoupUtil {
	public Set<String> extractImageUrls(String url) throws IOException{
		Document document = getDocument(url);
		Elements images = document.select("img");
		return images.stream()
				.filter(element -> !element.absUrl("src").isEmpty())
				.map(element -> element.absUrl("src"))
				.collect(Collectors.toSet());
	}

	private Document getDocument(String url) throws IOException {
		return Jsoup.connect(url).get();
	}
}
