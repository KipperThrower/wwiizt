#!/bin/bash

#Na podstawie:
#http://www.elasticsearch.org/guide/reference/index-modules/similarity/
#http://www.elasticsearch.org/guide/reference/api/admin-indices-create-index/
#http://www.elasticsearch.org/guide/reference/mapping/core-types/
#http://www.elasticsearch.org/guide/reference/java-api/index_/


#DEFAULT
curl -XPUT 'http://localhost:9200/wiki/' -d '';

#BM25
curl -XPUT 'http://localhost:9200/wikibm25/' -d '{                           
    "settings" : {
        "index" : {
            "similarity" : "BM25"
        }
    }
}';

#DRF - divergence from randomness
 curl -XPUT 'http://localhost:9200/wikidrf/' -d '{                           
    "settings" : {
        "index" : {
            "similarity" : "DRF"
        }
    }
}';


#Information Based Model
curl -XPUT 'http://localhost:9200/wikiib/' -d '{                           
    "settings" : {
        "index" : {
            "similarity" : "IB"
        }
    }
}';
