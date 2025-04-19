from config import (
    INDEX_NAME_DEFAULT,
    INDEX_NAME_EMBEDDING,
    INDEX_NAME_N_GRAM,
    INDEX_NAME_RAW,
)
from elastic_transport import ObjectApiResponse
from elasticsearch import Elasticsearch
from sentence_transformers import SentenceTransformer


def create_index(es: Elasticsearch, use_n_gram_tokenizer: bool) -> ObjectApiResponse:
    tokenizer = "n_gram_tokenizer" if use_n_gram_tokenizer else "standard"
    index_name = INDEX_NAME_N_GRAM if use_n_gram_tokenizer else INDEX_NAME_DEFAULT

    _ = es.indices.delete(index=index_name, ignore_unavailable=True)
    return es.indices.create(
        index=index_name,
        body={
            "settings": {
                "analysis": {
                    "analyzer": {
                        "default": {
                            "type": "custom",
                            "tokenizer": tokenizer,
                        },
                    },
                    "tokenizer": {
                        "n_gram_tokenizer": {
                            "type": "edge_ngram",
                            "min_gram": 1,
                            "max_gram": 30,
                            "token_chars": ["letter", "digit"],
                        },
                    },
                },
            },
        },
    )


def insert_document(
    es, document: dict, use_n_gram_tokenizer: bool
) -> ObjectApiResponse:
    index_name = INDEX_NAME_N_GRAM if use_n_gram_tokenizer else INDEX_NAME_DEFAULT
    return es.index(index=index_name, document=document)


def create_index_embedding(es: Elasticsearch) -> ObjectApiResponse:
    _ = es.indices.delete(index=INDEX_NAME_EMBEDDING, ignore_unavailable=True)
    return es.indices.create(
        index=INDEX_NAME_EMBEDDING,
        mappings={
            "properties": {
                "embedding": {
                    "type": "dense_vector",
                }
            }
        },
    )


def insert_document_embedding(
    es: Elasticsearch, model: SentenceTransformer, document: dict
) -> ObjectApiResponse:
    return es.index(
        index=INDEX_NAME_EMBEDDING,
        document={**document, "embedding": model.encode(document["explanation"])},
    )


def create_pipeline(es: Elasticsearch, pipeline_id: str) -> ObjectApiResponse:
    pipeline_body = {
        "description": "Pipeline that strips HTML tags from the explanation and title fields",
        "processors": [
            {"html_strip": {"field": "explanation"}},
            {"html_strip": {"field": "title"}},
        ],
    }
    return es.ingest.put_pipeline(id=pipeline_id, body=pipeline_body)


def create_index_raw(es: Elasticsearch) -> ObjectApiResponse:
    _ = es.indices.delete(index=INDEX_NAME_RAW, ignore_unavailable=True)
    return es.indices.create(index=INDEX_NAME_RAW)


def insert_document_raw(
    es: Elasticsearch, document: dict, pipeline_id: str
) -> ObjectApiResponse:
    return es.index(index=INDEX_NAME_RAW, document=document, pipeline=pipeline_id)
