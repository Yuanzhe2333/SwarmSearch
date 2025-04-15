import json
import torch

from tqdm import tqdm
from typing import List
from pprint import pprint
from utils import get_es_client
from elasticsearch import Elasticsearch, helpers
from config import INDEX_NAME_EMBEDDING

from sentence_transformers import SentenceTransformer


def index_data(documents: List[dict], model: SentenceTransformer) -> None:
    es = get_es_client(max_retries=1, sleep_time=0)
    _ = _create_index(es=es)
    _ = _insert_documents(es=es, documents=documents, model=model)
    
    pprint(f'Indexed {len(documents)} documents into Elasticsearch index "{INDEX_NAME_EMBEDDING}"')


def _create_index(es: Elasticsearch) -> dict:
    _ = es.indices.delete(index=INDEX_NAME_EMBEDDING, ignore_unavailable=True)
    return es.indices.create(
        index=INDEX_NAME_EMBEDDING,
        mappings={
            "properties": {
                "embedding": {
                    "type": "dense_vector",
                }
            }
        }
    )


def _insert_documents(es: Elasticsearch, documents: List[dict], model: SentenceTransformer, batch_size: int = 500) -> dict:
    total_docs = len(documents)
    for i in tqdm(range(0, total_docs, batch_size), desc="Indexing documents (batched)"):
        batch = documents[i:i + batch_size]
        operations = []

        skipCnt = 0
        for document in batch:
            if not document.get('explanation'):
                # print(f"Skipping None document: {document}")
                skipCnt += 1
                continue
            try:
                operations.append({'index': {'_index': INDEX_NAME_EMBEDDING}})
                operations.append({
                    **document,
                    'embedding': model.encode(document['explanation'])
                })
            except Exception as e:
                print(f"Error processing document {document.get('explanation', '')}: {e}")
                continue

        if skipCnt > 0:
            print(f"Skipped {skipCnt} documents in this batch due to missing 'explanation' field.")
        if operations:
            try:
                es.bulk(operations=operations)
                print(f"Inserted {len(operations) // 2} documents into Elasticsearch.")
            except Exception as e:
                print(f"Error during bulk insert: {e}")


if __name__ == '__main__':
    with open('../../../data/apod.json') as f:
        documents = json.load(f)

    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    model = SentenceTransformer('all-MiniLM-L6-v2').to(device)
    index_data(documents=documents, model=model)
