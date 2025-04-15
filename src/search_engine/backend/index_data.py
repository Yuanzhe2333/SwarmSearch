import json

from tqdm import tqdm
from typing import List
from pprint import pprint
from utils import get_es_client
from elasticsearch import Elasticsearch
from config import INDEX_NAME_DEFAULT, INDEX_NAME_N_GRAM
from typing import Optional, Dict


def index_data(
    documents: List[dict], 
    use_n_gram_tokenizer: bool,
    similarity: Optional[Dict] = None,
    mapping_similarity_name: Optional[str] = None
) -> None:
    es = get_es_client(max_retries=5, sleep_time=5)
    _ = _create_index_base(es=es, use_n_gram_tokenizer=use_n_gram_tokenizer, similarity=similarity, mapping_similarity_name=mapping_similarity_name)
    _ = _insert_documents(es=es, documents=documents, use_n_gram_tokenizer=use_n_gram_tokenizer)
    
    index_name = INDEX_NAME_N_GRAM if use_n_gram_tokenizer else INDEX_NAME_DEFAULT
    pprint(f'Indexed {len(documents)} documents into Elasticsearch index "{index_name}"')

def _create_index_base(
    es: Elasticsearch,
    use_n_gram_tokenizer: bool,
    similarity: Optional[Dict] = None,
    mapping_similarity_name: Optional[str] = None
) -> dict:
    tokenizer = 'n_gram_tokenizer' if use_n_gram_tokenizer else 'standard'
    index_name = INDEX_NAME_N_GRAM if use_n_gram_tokenizer else INDEX_NAME_DEFAULT

    es.indices.delete(index=index_name, ignore_unavailable=True)

    settings = {
        'analysis': {
            'analyzer': {
                'default': {
                    'type': 'custom',
                    'tokenizer': tokenizer,
                },
            },
            'tokenizer': {
                'n_gram_tokenizer': {
                    'type': 'edge_ngram',
                    'min_gram': 1,
                    'max_gram': 30,
                    'token_chars': ['letter', 'digit'],
                },
            },
        },
    }

    if similarity:
        settings['similarity'] = similarity

    mappings = {}
    if mapping_similarity_name:
        mappings = {
            'properties': {
                'title': {
                    'type': 'text',
                    'analyzer': 'default',
                    'similarity': mapping_similarity_name
                },
                'explanation': {
                    'type': 'text',
                    'analyzer': 'default',
                    'similarity': mapping_similarity_name
                }
            }
        }

    return es.indices.create(
        index=index_name,
        body={
            'settings': settings,
            'mappings': mappings
        }
    )


def _insert_documents(es: Elasticsearch, documents: List[dict], use_n_gram_tokenizer: bool) -> dict:
    operations = []
    index_name = INDEX_NAME_N_GRAM if use_n_gram_tokenizer else INDEX_NAME_DEFAULT
    for document in tqdm(documents, total=len(documents), desc='Indexing documents'):
        operations.append({'index': {'_index': index_name}})
        operations.append(document)
    return es.bulk(operations=operations)


if __name__ == '__main__':
    with open('../../../data/apod.json') as f:
        documents = json.load(f)

    mapping_similarity_names = [
        'custom_BM25', # pass
        'custom_LMDirichlet', # pass
        'custom_LMJelinekMercer', # pass
        'custom_DFR', # pass
    ]

    similarities = [
        {
            'custom_BM25': {
                'type': 'BM25',
                'b': 0.75,
                'k1': 1.2,
            }
        },
        {
            'custom_LMDirichlet': {
                'type': 'LMDirichlet',
                'mu': 2000,
            }
        },
        {
            'custom_LMJelinekMercer': {
                'type': 'LMJelinekMercer',
                'lambda': 0.5,
            }
        },
        {
            'custom_DFR': {
                'type': 'DFR',
                'basic_model': 'g',
                'after_effect': 'l',
                'normalization': 'h2',
                'normalization.h2.c': '3.0',
            }
        },
    ]

    similarity_index = 0
    mapping_similarity_name = mapping_similarity_names[similarity_index]
    similarity = similarities[similarity_index]

    print(f'Indexing with similarity: {mapping_similarity_name}')
    index_data(documents=documents, use_n_gram_tokenizer=True, similarity=similarity, mapping_similarity_name=mapping_similarity_name)
    index_data(documents=documents, use_n_gram_tokenizer=False, similarity=similarity, mapping_similarity_name=mapping_similarity_name)