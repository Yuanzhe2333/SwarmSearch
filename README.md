# SwarmSearch

## Overview

SwarmSearch is a specialized search engine tailored exclusively for the Georgia Tech community, aiming to provide fast, relevant, and efficient search results. Unlike general search engines, SwarmSearch directly crawls and indexes content from the websites of Georgia Tech's seven colleges, optimizing the experience for students, faculty, and researchers.

## Motivation

General-purpose search engines like Google or Yahoo often return excessive unrelated results, making the process of finding specific academic resources inefficient. SwarmSearch addresses this issue by indexing only content from relevant Georgia Tech departments, significantly reducing irrelevant search results and improving search precision and speed.

## Objectives

- Develop a specialized, efficient search engine focused on Georgia Tech’s academic ecosystem.
- Enhance search relevance by directly indexing content from Georgia Tech’s college websites.
- Reduce query latency compared to standard web search tools.

## Features

### Web Crawling

- Utilizes JSoup for efficient web scraping and parsing of HTML content.
- Employs a combination of Breadth-First Search (BFS) and Depth-First Search (DFS) crawling strategies to optimize data collection.
- Implements MongoDB to store crawled URLs for scalability and rapid retrieval.

### LLM Preprocessing

- Integrates large language models (LLMs) to preprocess and summarize crawled HTML pages before indexing.
- Strips boilerplate HTML content and extracts the core semantic meaning, improving downstream search accuracy.
- Preprocessed results are transformed into structured JSON format to aid semantic indexing and relevance scoring.
- Helps normalize inconsistent structures across Georgia Tech’s various department pages.


### Indexing & Search

- Uses Apache Lucene to create and manage a powerful reverse index for fast query retrieval.
- Tests multiple scoring algorithms, including BM25, TF-IDF, LMJelinekMercer, and DFR similarity models, to identify the best-performing solution.

### User Interface

- Designed with React for a clean, intuitive user experience.
- Features advanced search capabilities, auto-complete suggestions, filters, and pagination.

### Deployment

- Backend services implemented using Spring Boot for efficient API handling.
- Initial deployment on Apache Tomcat to test in a controlled environment.

## Evaluation and Testing

### Relevance and Precision

- Precision testing against Google’s search results for Georgia Tech-specific queries.
- Calculation formula: `(Relevant results retrieved) / (Total results retrieved)`.

### Latency Evaluation

- Round-trip request latency testing to benchmark performance against Google using standardized queries.

## Technology Stack

- **Frontend:** React
- **Backend:** Java, Spring
- **Crawling & Parsing:** JSoup
- **Indexing:** Elastic
- **Data Storage:** MongoDB
- **Deployment:** Apache Tomcat

## Deployment

- Deployed initially on local servers using Apache Tomcat for controlled testing environments.
- Open-source deployment setup for future scalability.

## Contributors

- Oscar Zhang
- Tracy Guo
- Yuanzhe Liu
- Yen-Shun Lu
- Pian Wan

## Installation Instructions

### Crawler (under src/crawler)

Within the src/crawler directory run

```bash
mvn install
```

to install necessary dependencies.

Run the crawler using

```bash
mvn clean compile exec:java
```

For the application to work, config/config.properties must be setup under src/crawler with the following properties:

1. mongodb.uri: The complete MongoDB URI to connect to the database. This should include the protocol, username, password, host, and any necessary connection parameters.
2. elastic.host: The hostname or IP address where your Elasticsearch instance is hosted.
3. elastic.port: The port number the Elastic instance is running on.
4. elastic.scheme: The URL scheme used to connect to Elasticsearch (http or https).
5. elastic.apikey: The Base64-encoded API key used to authenticate requests to your Elasticsearch instance. Make sure there are no leading or trailing spaces.

### Search Engine (under src/search_engine/backend)

#### Prerequisite

You have to have `npm` installed.


1. Install the required dependencies:

```bash
pip install -r requirements.txt
````

2. Setup indicies for Elastic
- DO NOT run if there is already data, as it will delete the data.
- This is already done, so do not run this.

```bash
python3 index_setup.py
```

3. Run service

```bash
fastapi dev main.py
```

### Frontend (under src/search_engine/frontend)

- Install required packages

```bash
npm install
npm install -g @vue/cli@latest # This may not be needed
```

- Run service

```
npm run serve
```

---

For questions or contributions, please contact:

`{ozhang31, tguo72, yliu3794, ylu776, pianwan}@gatech.edu`
