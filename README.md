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
bin/lov-index lov_aggregator.nq elasticsearch localhost
````


### 5. Test if it worked

````
curl 'http://localhost:9200/lov/class,property,vocabulary/_search?q=test&pretty=1'
````

If this returns a longish JSON response, all is good.


## Command-line tool documentation

### `build-index`: The ElasticSearch Vocabulary Indexer

This tool reads an RDFS or OWL file, and indexes any terms defined therein in an ElasticSearch index. To see its syntax:

````
bin/build-index
````

Example invocation:

````
# Indexes SKOS into the 'skos' index on the 'elasticsearch' cluster
bin/build-index http://www.w3.org/2004/02/skos/core elasticsearch localhost skos
````

### `lov-index`: The Linked Open Vocabularies Indexer

This tool populates an ElasticSearch index with the contents of the Linked Open Vocabularies dump. The dump can be obtained [here](http://lov.okfn.org/dataset/lov/agg/lov_aggregator.rdf). The file needs to be downloaded, and its extension changed to `.nq` because otherwise Jena gets confused. It really is an N-Quads file, not an RDF/XML file. To see the tool's syntax:

````
bin/lov-index
````

Example invocation:

````
# Download LOV dump with right name
curl -o lov_aggregator.nq http://lov.okfn.org/dataset/lov/agg/lov_aggregator.rdf
# Indexes the dump into an index called 'lov' on the 'elasticsearch' cluster
bin/lov-index lov_aggregator.nq elasticsearch localhost
````


## Executing searches

Once the ElasticSearch index is populated, the standard REST-based ElasticSearch APIs can be used to run searches.

The following example searches for classes, properties and vocabularies in the `lov` index, using the keyword `test`:

````
http://localhost:9200/lov/class,property,vocabulary/_search?q=test&pretty=1
````


## Developing

Initializing Eclipse files:

`mvn eclipse:eclipse -DdownloadSources -DdownloadJavadocs`

Running the tests:

`mvn test`

Use the issue tracker to discuss stuff, and feel free to submit pull requests.
