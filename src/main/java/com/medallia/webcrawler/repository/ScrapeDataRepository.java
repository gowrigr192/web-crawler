package com.medallia.webcrawler.repository;

import com.medallia.webcrawler.entity.ScrapeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional("transactionManager")
public interface ScrapeDataRepository extends JpaRepository<ScrapeData, Integer> {
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO scrape_data (domain, image_url) VALUES (:domain, :imageUrl) ON CONFLICT (domain, image_url) DO NOTHING", nativeQuery = true)
	void insertOrSkipDuplicate(@Param("domain") String domain, @Param("imageUrl") String imageUrl);
}
