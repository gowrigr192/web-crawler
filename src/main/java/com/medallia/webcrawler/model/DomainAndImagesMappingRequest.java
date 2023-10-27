package com.medallia.webcrawler.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DomainAndImagesMappingRequest {
	private String domain;
	private List<String> imageUrls;
}
