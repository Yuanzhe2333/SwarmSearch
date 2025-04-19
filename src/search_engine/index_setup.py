import indexing_utils as iu
from utils import get_es_client

if __name__ == "__main__":
    es = get_es_client(max_retries=5, sleep_time=5)

    iu.create_index(es=es, use_n_gram_tokenizer=False)
    iu.create_index(es=es, use_n_gram_tokenizer=True)
    iu.create_index_embedding(es=es)

    # create pipeline to remove html
    iu.create_pipeline(es, pipeline_id="html_strip")
    iu.create_index_raw(es)
