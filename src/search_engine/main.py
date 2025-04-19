import indexing_utils as iu
import torch
from config import INDEX_NAME_DEFAULT, INDEX_NAME_EMBEDDING, INDEX_NAME_N_GRAM
from elastic_transport import ObjectApiResponse
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from utils import get_es_client

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = SentenceTransformer("all-MiniLM-L6-v2").to(device)


class Document(BaseModel):
    """
    @param title: title of the document
    @param explanation: explanation of the document or the full HTML
    @param url: url of the page that was scraped
    @param date: YYYY-MM-DD date
    """

    title: str
    url: str
    explanation: str
    date: str


@app.post("/api/v1/index_doc")
async def index_doc(body: Document):
    try:
        es = get_es_client(max_retries=1, sleep_time=0)
        document = {
            "title": body.title,
            "explanation": body.explanation,
            "url": body.url,
            "date": body.date,
        }

        iu.insert_document(es, document, use_n_gram_tokenizer=True)
        iu.insert_document(es, document, use_n_gram_tokenizer=False)
        print("Documents inserted using normal method")

        iu.insert_document_embedding(es, model, document)
        print("documents inserted using embedding method")

        iu.insert_document_raw(es, document, "html_strip")
        print("documents inserted using raw html method")

        return {"message": "Successfully indexed document", "statusCode": 200}
    except Exception as e:
        return {"message": e, "statusCode": 500}


@app.get("/api/v1/regular_search/")
async def regular_search(
    search_query: str,
    skip: int = 0,
    limit: int = 10,
    year: str | None = None,
    tokenizer: str = "Standard",
) -> dict:
    try:
        es = get_es_client(max_retries=1, sleep_time=0)
        query = {
            "bool": {
                "must": [
                    {
                        "multi_match": {
                            "query": search_query,
                            "fields": ["title", "explanation"],
                        }
                    }
                ]
            }
        }

        if year:
            query["bool"]["filter"] = [
                {
                    "range": {
                        "date": {
                            "gte": f"{year}-01-01",
                            "lte": f"{year}-12-31",
                            "format": "yyyy-MM-dd",
                        }
                    }
                }
            ]

        index_name = (
            INDEX_NAME_DEFAULT if tokenizer == "Standard" else INDEX_NAME_N_GRAM
        )
        response = es.search(
            index=index_name,
            body={
                "query": query,
                "from": skip,
                "size": limit,
            },
            filter_path=[
                "hits.hits._source",
                "hits.hits._score",
                "hits.total",
            ],
        )

        total_hits = get_total_hits(response)
        max_pages = calculate_max_pages(total_hits, limit)

        return {
            "hits": response["hits"].get("hits", []),
            "max_pages": max_pages,
        }
    except Exception as e:
        return handle_error(e)


@app.get("/api/v1/semantic_search/")
async def semantic_search(
    search_query: str, skip: int = 0, limit: int = 10, year: str | None = None
) -> dict:
    try:
        es = get_es_client(max_retries=1, sleep_time=0)
        embedded_query = model.encode(search_query)

        query = {
            "bool": {
                "must": [
                    {
                        "knn": {
                            "field": "embedding",
                            "query_vector": embedded_query,
                            # Because we have 3333 documents, we can return them all.
                            "k": 1e4,
                        }
                    }
                ]
            }
        }

        if year:
            query["bool"]["filter"] = [
                {
                    "range": {
                        "date": {
                            "gte": f"{year}-01-01",
                            "lte": f"{year}-12-31",
                            "format": "yyyy-MM-dd",
                        }
                    }
                }
            ]

        response = es.search(
            index=INDEX_NAME_EMBEDDING,
            body={
                "query": query,
                "from": skip,
                "size": limit,
            },
            filter_path=[
                "hits.hits._source",
                "hits.hits._score",
                "hits.total",
            ],
        )

        total_hits = get_total_hits(response)
        max_pages = calculate_max_pages(total_hits, limit)

        return {
            "hits": response["hits"].get("hits", []),
            "max_pages": max_pages,
        }
    except Exception as e:
        return handle_error(e)


def get_total_hits(response: ObjectApiResponse) -> int:
    return response["hits"]["total"]["value"]


def calculate_max_pages(total_hits: int, limit: int) -> int:
    return (total_hits + limit - 1) // limit


@app.get("/api/v1/get_docs_per_year_count/")
async def get_docs_per_year_count(
    search_query: str, tokenizer: str = "Standard"
) -> dict:
    try:
        es = get_es_client(max_retries=1, sleep_time=0)
        query = {
            "bool": {
                "must": [
                    {
                        "multi_match": {
                            "query": search_query,
                            "fields": ["title", "explanation"],
                        }
                    }
                ]
            }
        }

        index_name = (
            INDEX_NAME_DEFAULT if tokenizer == "Standard" else INDEX_NAME_N_GRAM
        )
        response = es.search(
            index=index_name,
            body={
                "query": query,
                "aggs": {
                    "docs_per_year": {
                        "date_histogram": {
                            "field": "date",
                            "calendar_interval": "year",  # Group by year
                            "format": "yyyy",  # Format the year in the response
                        }
                    }
                },
            },
            filter_path=["aggregations.docs_per_year"],
        )
        return {"docs_per_year": extract_docs_per_year(response)}
    except Exception as e:
        return handle_error(e)


def extract_docs_per_year(response: ObjectApiResponse) -> dict:
    aggregations = response.get("aggregations", {})
    docs_per_year = aggregations.get("docs_per_year", {})
    buckets = docs_per_year.get("buckets", [])
    return {bucket["key_as_string"]: bucket["doc_count"] for bucket in buckets}


def handle_error(e: Exception) -> dict[str, int | str]:
    error_message = f"An error occurred: {str(e)}"
    return {"message": error_message, "statusCode": 500}
