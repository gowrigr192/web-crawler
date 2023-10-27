package com.medallia.webcrawler.service;

import com.medallia.webcrawler.exception.ApiException;
import com.medallia.webcrawler.model.DatabaseDetails;
import com.medallia.webcrawler.repository.ScrapeDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

@Service
public class DataSourceService {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceService.class);
    private final RestTemplate restTemplate;
    private final ScrapeDataRepository scrapeDataRepository;
    private final JdbcTemplate adminJdbcTemplate;

    @Autowired
    public DataSourceService(RestTemplate restTemplate, DataSource dataSource, ScrapeDataRepository scrapeDataRepository) {
        this.restTemplate = restTemplate;
        this.scrapeDataRepository = scrapeDataRepository;
        this.adminJdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Async
    public void saveImagesIntoForeignDB(String domain, Set<String> imageUrlList, boolean isDataSavedLocally) {
        LOG.info("Going to save data into foreign DB");
        DataSource dataSource = createDataSourceForDomain(domain, imageUrlList, isDataSavedLocally);
        createImageTableIfNotExists(dataSource);
        saveImageIntoExtractedDB(imageUrlList, dataSource);
    }

    private void saveImageIntoExtractedDB(Set<String> imageUrlList, DataSource dataSource) {
        StringBuilder imageUrlsStringBuilder = new StringBuilder();
        imageUrlList.forEach(imageUrl -> {
            imageUrlsStringBuilder.append("('" + imageUrl + "'),");
        });
        String imageUrlString = imageUrlsStringBuilder.toString().substring(0, imageUrlsStringBuilder.toString().length() - 1);
        try {
            String insertScript = "INSERT INTO IMAGE (imageUrl) VALUES " + imageUrlString + " ON CONFLICT (imageUrl)\n" +
                    "DO NOTHING";
            new JdbcTemplate(dataSource).execute(insertScript);
            LOG.info("Data saved in the DB {}", dataSource.getConnection().getMetaData());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private DataSource createDataSourceForDomain(String domain, Set<String> imageUrlList, boolean isDataSavedLocally) {
        int dbId = getDbIdForDomain(domain, imageUrlList, isDataSavedLocally);
        DatabaseDetails dbDetails = getDatabaseDetails(dbId);
        String url = "jdbc:postgresql://" + dbDetails.getHost() + ":5432/" + dbDetails.getSchema();
        String username = dbDetails.getUser();
        String password = dbDetails.getPassword();

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        createRoleIfNotExists(username, password);
        createDbIfNotExists(dbDetails.getSchema(), username);

        return dataSource;
    }

    private int getDbIdForDomain(String domain, Set<String> imageUrlList, boolean isDataSavedLocally) {
        int retryCount = 0;
        int maxRetryCount = 10;
        while (retryCount < maxRetryCount) {
            try {
                HashMap<String, Integer> response = restTemplate.getForObject("http://localhost:8888/getDBForDomain/" + domain, HashMap.class);
                return response.get("dbId");
            } catch (HttpServerErrorException | HttpClientErrorException ex) {
                try {
                    if (!isDataSavedLocally) {
                        saveDataLocally(domain, imageUrlList);
                        isDataSavedLocally = true;
                    }
                    retryCount++;
                    LOG.info("power nap time");
                    Thread.sleep(5 * 60 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new ApiException("Foreign DB details not found");
    }

    @Async
    public void saveDataLocally(String domain, Set<String> imageUrlList) {
        imageUrlList.forEach(imageUrl -> {
            scrapeDataRepository.insertOrSkipDuplicate(domain, imageUrl);
        });
        LOG.info("Data saved locally");
    }

    private DatabaseDetails getDatabaseDetails(int dbId) {
        int retryCount = 0;
        int maxRetryCount = 10;
        while (retryCount < maxRetryCount) {
            try {
                DatabaseDetails[] dbDetails = restTemplate.getForObject("http://localhost:8888/getDatabases", DatabaseDetails[].class);
                if (dbDetails != null) {
                    for (DatabaseDetails dbDetail : dbDetails) {
                        if (dbDetail.getId() == dbId) {
                            return dbDetail;
                        }
                    }
                }
            } catch (HttpServerErrorException | HttpClientErrorException ex) {
                try {
                    LOG.info("Power nap time");
                    retryCount++;
                    Thread.sleep(5 * 60 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new ApiException("Foriegn DB details not found");
    }

    private void createRoleIfNotExists(String roleName, String password) {
        String checkRoleSql = "SELECT 1 FROM pg_roles WHERE rolname = ?";
        try {
            adminJdbcTemplate.queryForObject(checkRoleSql, Integer.class, roleName);
        } catch (EmptyResultDataAccessException e){
            LOG.info(String.format("Role %s does not exist; hence creating", roleName));
            String createRoleSql = "CREATE ROLE " + roleName + " WITH LOGIN PASSWORD '" + password + "'";
            adminJdbcTemplate.execute(createRoleSql);
        }
    }

    private void createDbIfNotExists(String dbName, String roleName) {
        String checkDbSql = "SELECT 1 FROM pg_database WHERE datname = ?";
        try {
            adminJdbcTemplate.queryForObject(checkDbSql, Integer.class, dbName);
        } catch (EmptyResultDataAccessException e) {
            LOG.info(String.format("Database %s does not exist; hence creating", dbName));
            adminJdbcTemplate.execute("CREATE DATABASE " + dbName);
        }

        String grantSchemaPermissionSql = "GRANT ALL PRIVILEGES ON DATABASE " + dbName + " TO " + roleName;
        adminJdbcTemplate.execute(grantSchemaPermissionSql);
    }

    private void createImageTableIfNotExists(DataSource dataSource) {
        String createTableSql = "CREATE TABLE IF NOT EXISTS IMAGE ("
                + "id serial PRIMARY KEY, "
                + "imageUrl TEXT UNIQUE, "
                + "created_date timestamp default current_timestamp, "
                + "modified_date timestamp default current_timestamp"
                + ")";

        new JdbcTemplate(dataSource).execute(createTableSql);
        LOG.info("Table created");
    }
}
