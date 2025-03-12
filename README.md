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
- **Indexing:** Apache Lucene
- **Data Storage:** MongoDB
- **Deployment:** Apache Tomcat

## Deployment
- Deployed initially on local servers using Apache Tomcat for controlled testing environments.
- Open-source deployment setup for future scalability.

## Contributors
- Pian Wan
- Oscar Zhang
- Yen-Shun Lu
- Tracy Guo
- Yuanzhe Liu

---
For questions or contributions, please contact:

`{pianwan, ozhang31, ylu776, tguo72, yliu3794}@gatech.edu`

