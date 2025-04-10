import time
from pprint import pprint

from elasticsearch import Elasticsearch


def get_es_client(max_retries: int = 1, sleep_time: int = 5) -> Elasticsearch:
    for i in range(max_retries):
        try:
            es = Elasticsearch("http://172.105.152.226:6675")
            pprint("Connected to Elasticsearch!")
            return es
        except Exception:
            pprint("Could not connect to Elasticsearch, retrying...")
            time.sleep(sleep_time)
            i += 1
    raise ConnectionError("Failed to connect to Elasticsearch after multiple attempts.")
