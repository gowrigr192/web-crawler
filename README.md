# Web Crawler Application

Web Crawler Application is a Java-based tool that allows users to extract and save image URLs from web pages. 
It provides a convenient way to scrape images from web content, store them in a database, and save the data locally.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)

## Features

- Web scraping: Extract image URLs from web pages.
- Data storage: Store extracted image URLs in a database.
- Local data backup: Optionally save data locally.
- Asynchronous processing: Parallelize data saving tasks for improved performance.
- Retry mechanisms: Handle connection errors gracefully with automatic retries.
- RestTemplate: Utilize the RestTemplate for HTTP requests.
- Jsoup: Leverage Jsoup for web scraping.

## Prerequisites

Before you get started, ensure you have the following prerequisites:

- Java Development Kit (JDK)
- Gradle (for building the project)
- PostgreSQL (for database storage)
- Dependencies listed in the project's `build.gradle` file

## Installation

To set up the project locally, follow these steps:

1. create a database named scrape_db in your local postgres
2. change the username, password and url string in the application.properties file
3. go to the project directory in a command window and run ./gradlew build to build jar
4. in the same directory run java -Dserver.port=(DESIRED_PORT_NUMBER) -jar  build/libs/web-crawler-0.0.1-SNAPSHOT.jar
