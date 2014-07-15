#!/usr/bin/env bash

# assumes Elasticsearch is available on port 9200

# create index
curl -XPUT 'http://localhost:9200/recipes/'

# populate
# TODO make paths relative to script location
curl -XPOST 'http://localhost:9200/recipes/recipe/' -d @cheese-on-toast.json
curl -XPOST 'http://localhost:9200/recipes/recipe/' -d @chocolate-cake.json
