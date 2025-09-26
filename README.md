# Price Hunt API

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.x-orange.svg)](https://maven.apache.org/)

A Spring Boot-based REST API for scraping and tracking product prices from e-commerce websites.

## 🚀 Features

- **Web Scraping**: Utilizes Jsoup to parse HTML and extract product information like price, name, and availability.
- **RESTful Endpoints**: Provides a clean API to request product data from a given URL.
- **JSON Responses**: Returns structured product data in JSON format using Gson.
- **Extensible**: Built with modern tools to easily add support for more websites or data points.

## 🛠️ Technologies Used

- **Backend**: [Spring Boot](https://spring.io/projects/spring-boot) (Web)
- **Language**: [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Build Tool**: [Maven](https://maven.apache.org/)
- **HTTP Client**: [Apache HttpClient5](https://hc.apache.org/httpcomponents-client-5.2.x/)
- **HTML Parsing**: [Jsoup](https://jsoup.org/)
- **JSON Processing**: [Gson](https://github.com/google/gson)

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **JDK 17** or later
- **Apache Maven**

## ⚙️ Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing.

### 1. Clone the Repository

```sh
git clone https://github.com/Uttham-412/pricehunt.git
cd pricehunt
```

### 2. Build the Project

Use Maven to build the project and install dependencies.

```sh
mvn clean install
```

### 3. Run the Application

You can run the application using the Spring Boot Maven plugin:

```sh
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## 📖 API Usage

*(This is an example. You should update this section with your actual API endpoints.)*

Once the server is running, you can make requests to the API.

**Example Request:**

`GET /api/v1/product?url=<PRODUCT_URL>`

Where `<PRODUCT_URL>` is the URL-encoded link to the product page you want to scrape.

