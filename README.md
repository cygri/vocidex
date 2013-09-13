# Vocidex: Vocabulary Index

The goal of this project is to provide a reliable and high-quality search functionality over RDF Schemas and OWL Ontologies:

* Search for classes, properties and vocabularies
* Index based on ElasticSearch and Lucene
* Bootstrap the index from a [Linked Open Vocabularies](http://lov.okfn.net/) (LOV) dump
* Load individual RDFS and OWL files into the index
* Search using the RESTful ElasticSearch API


## Setting it all up

This is how you get an index up and running, and filled with data.


### 1. Installing ElasticSearch

The recommended way on OS X is using [Homebrew](http://brew.sh). After Homebrew is set up and configured, simply run:

`brew install elasticsearch`

To do: Add instructions for other operating systems...


### 2. Starting the server

The easiest way for development use is this, using the provided configuration file:

````
elasticsearch -f -D es.config=elasticsearch.yml
````

The `-f` flag starts ElasticSearch in the foreground so you can stop it with Ctrl+C.

The `-D` option instructs ElasticSearch to use the `elasticsearch.yml` configration file. This configuration places data and logs into a subdirectory `elasticsearch` within this repository. For production use, you may want to use a different setup. 


### 3. Building the CLI

You need [Maven](http://maven.apache.org). Install it if necessary (`brew install maven` on OS X).

`mvn package`

This compiles and assembles the command-line app. The result is two things:

1. A gzipped version of the command-line app is generated in `target/vocidex-cli.tar.gz` and can be deployed wherever you like
2. An uncompressed version of the app is in `target/vocidex-cli/vocidex` and can be used directly

From inside the generated app's directory, the command-line tools can be run by invoking `bin/appname`.


### 4. Populating the index

````
# go to CLI build dir
cd target/vocidex-cli/vocidex
# Download LOV N-Quads dump as lov_aggregator.nq, takes a while
curl -o lov_aggregator.nq http://lov.okfn.org/dataset/lov/agg/lov_aggregator.rdf 
# Load it, takes a while
bin/index-lov elasticsearch localhost lov lov_aggregator.nq
````


### 5. Test if it worked

````
curl 'http://localhost:9200/lov/class,property,vocabulary/_search?q=test&pretty=1'
````

If this returns a longish JSON response, all is good.


## Command-line tool documentation

### `create-index`: Index initializer

This tool connects to an ElasticSearch cluster and initializes a new index for use with Vocidex. To see its syntax:

````
bin/create-index
````

Example invocation:

````
# Adds an index called 'lov' on the 'elasticsearch' cluster
bin/create-index elasticsearch localhost lov
````

### `add-vocabulary`: The ElasticSearch Vocabulary Indexer

This tool reads an RDFS or OWL file, and indexes any terms defined therein in an ElasticSearch index. To see its syntax:

````
bin/add-vocabulary
````

Example invocation:

````
# Indexes SKOS into the 'skos' index on the 'elasticsearch' cluster
bin/add-vocabulary elasticsearch localhost skos http://www.w3.org/2004/02/skos/core
````

### `index-lov`: The Linked Open Vocabularies Indexer

This tool populates an ElasticSearch index with the contents of the Linked Open Vocabularies dump. The dump can be obtained [here](http://lov.okfn.org/dataset/lov/agg/lov_aggregator.rdf). The file needs to be downloaded, and its extension changed to `.nq` because otherwise Jena gets confused. It really is an N-Quads file, not an RDF/XML file. To see the tool's syntax:

````
bin/index-lov
````

Example invocation:

````
# Download LOV dump with right name
curl -o lov_aggregator.nq http://lov.okfn.org/dataset/lov/agg/lov_aggregator.rdf
# Indexes the dump into an index called 'lov' on the 'elasticsearch' cluster
bin/index-lov elasticsearch localhost lov lov_aggregator.nq
````


## Executing searches

Once the ElasticSearch index is populated, the standard REST-based ElasticSearch APIs can be used to run searches.

### Simple keyword ("match") queries
The following example searches for classes, properties and vocabularies in the `lov` index, using the keyword `test`:

````
curl 'http://localhost:9200/lov/class,property,vocabulary/_search?q=test&pretty=1'
````

Equivalent to:

````
curl -XPOST 'http://localhost:9200/lov/class,property,vocabulary/_search?pretty=1' -d '{"query":{"match":{"_all":"test"}}}'
````


### Autocompletion

This provides an autocomplete feature on pre-tokenized (using edge_ngram [1;100]) and indexed fields `*.autocomplete`.

````
curl -XPOST 'http://localhost:9200/lov/class,property/_search?pretty=1' -d '{
  "fields" : ["uri", "prefixed", "localName"],
  "query" : {
     "multi_match" : {
         "query": "foaf:",
         "fields": ["prefixed.autocomplete","uri.autocomplete"],
          "type" : "match_phrase"
     }
  }
}'
````


## Developing

Initializing Eclipse files:

`mvn eclipse:eclipse -DdownloadSources -DdownloadJavadocs`

Running the tests:

`mvn test`

Use the issue tracker to discuss stuff, and feel free to submit pull requests.


## Structure of indexed JSON documents

Vocidex works by creating a JSON document for each entity to be indexed (classes, properties, datatypes, vocabularies), and putting them into an ElasticSearch index. Here we document the structure of these JSON documents.

Note: “term array” is a JSON array of objects, each with `uri` and `label` keys.

### All Terms (classes, properties, datatypes)

* `type`: `class`, `property`, `datatype`
* `uri`: absolute URI
* `uri.autocomplete`: edge_ngram tokenized for autocomplete over `uri`
* `prefix`: Namespace prefix, either provided by LOV or manually at index time; may be absent
* `localName`: Part after the last hash/slash
* `localName.autocomplete`: edge_ngram tokenized for autocomplete over `localName`
* `prefixed`: Prefixed name (e.g., `foaf:Person`), or absent if no `prefix`
* `prefixed.autocomplete`: edge_ngram tokenized for autocomplete over `prefixed`
* `label`: `rdfs:label` or similar property, or a string synthesized from the local name
* `comment`: `rdfs:comment` or similar property; may be absent
* `vocabulary`: LOV metadata about the vocabulary; may be absent
** `uri`
** `prefix`
** `label`
** `homepage`

### Class

Term keys as listed above, plus:

* `superclasses`: term array
* `disjointClasses`: term array
* `equivalentClasses`: term array

### Property

Term keys as listed above, plus:

* `domains`: term array
* `ranges`: term array; each member also has either an `isDatatype` or `isClass` field with value `true`
* `superproperties`: term array
* `inverseProperties`: term array
* `equivalentProperties`: term array
* `isAnnotationProperty`: boolean
* `isObjectProperty`: boolean
* `isDatatypeProperty`: boolean
* `isFunctionalProperty`: boolean
* `isInverseFunctionalProperty`: boolean
* `isTransitiveProperty`: boolean
* `isSymmetricProperty`: boolean

### Datatype

Term keys as listed above

### Vocabulary

* `type`: `vocabulary`
* `uri`: absolute URI as per LOV
* `uri.autocomplete`: edge_ngram tokenized for autocomplete over `uri`
* `prefix`: conventional prefix as per LOV
* `prefix.autocomplete`: edge_ngram tokenized for autocomplete over `prefix`
* `label`: as for terms
* `shortLabel`: curated short-form label as per LOV; may be absent
* `comment`: as for terms
* `homepage`: URL from LOV metadata; may be absent

