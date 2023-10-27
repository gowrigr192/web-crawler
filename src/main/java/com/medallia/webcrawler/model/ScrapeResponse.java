package com.medallia.webcrawler.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
public class ScrapeResponse {
	private String domain;
	private Set<String> imageUrls;
}
