// Module dependencies.
var express = require('express')
  , http = require('http')
  , ElasticSearchClient = require('elasticsearchclient');
 
var app = express();

// Number of search results per page
var pageSize = 10;
// Name of the ElasticSearch index
var indexName = 'lov';

// Connection to ElasticSearch
var client = new ElasticSearchClient({host: 'localhost',port: 9200});



function execSearchTerms(queryString, page, type, callback) {
  if(!type)type='class,property';
  if (!page || page<1)page = 1;
  page = parseInt(page, 10) || 1;
  var q = {
		  "from": (page - 1) * pageSize,
		  "size": pageSize,
		  "fields" : ["uri", "prefixed", "localName"],
		  "query" : {
			 "multi_match" : {
				 "query": queryString,
				 "fields": ["prefixed.autocomplete","uri.autocomplete"],
				  "type" : "match_phrase"
			 }
		  }
		};
  return client.search(indexName, type, q).on('data', function(data) {
    var hit, parsed, result, x;
    parsed = JSON.parse(data).hits;
    result = {
      total_results: parsed.total,
      page: page,
	  page_size: pageSize,
      results: (function() {
        var results = [];
        for (var i = 0; i < parsed.hits.length; i++) {
          hit = parsed.hits[i];
          x = hit.fields;
          x.type = hit._type;
          x.score = hit._score;
          results.push(x);
        }
        return results;
      })()
    };
    return callback(null, result);
  }).on('error', function(error) {
    return callback(error, null);
  }).exec();
};

function execSearchVocabularies(queryString, page, callback) {
  if (!page || page<1)page = 1;
  page = parseInt(page, 10) || 1;
  var q = {
		  "from": (page - 1) * pageSize,
		  "size": pageSize,
		  "fields" : ["uri", "prefix"],
		  "query" : {
			 "multi_match" : {
				 "query": queryString,
				 "fields": ["prefix.autocomplete","uri.autocomplete"],
				  "type" : "match_phrase"
			 }
		  }
		};
  return client.search(indexName, 'vocabulary', q).on('data', function(data) {
    var hit, parsed, result, x;
    parsed = JSON.parse(data).hits;
    result = {
      total_results: parsed.total,
      page: page,
	  page_size: pageSize,
      results: (function() {
        var results = [];
        for (var i = 0; i < parsed.hits.length; i++) {
          hit = parsed.hits[i];
          x = hit.fields;
          x.score = hit._score;
          results.push(x);
        }
        return results;
      })()
    };
    return callback(null, result);
  }).on('error', function(error) {
    return callback(error, null);
  }).exec();
};



function standardBadRequestHandler(req, res, helpText) {
  res.set('Content-Type', 'text/plain');
  return res.send(400, helpText);
};

function standardCallback(req, res, err, results) {
  if (err != null) {
    console.log(err);
    return res.send(500, err);
  } else if (!(results != null)) {
    return res.send(404, 'API returned no results');
  } else {
    return res.send(200, results);
  }
};



app.get('/autocomplete/terms', function(req, res) {
	if (!req.query.q) { //control that q param is present
		return standardBadRequestHandler(req, res, 'Query parameter missing. Syntax: ?q=querytext');
	} else {
		console.log('Search for terms starting with "'+req.query.q+'"')
		return execSearchTerms(req.query.q, req.query.page || 1, req.query.type || 'class,property', function(err, results) {
		  return standardCallback(req, res, err, results);
		});
	}
});

app.get('/autocomplete/vocabularies', function(req, res) {
	if (!req.query.q) { //control that q param is present
		return standardBadRequestHandler(req, res, 'Query parameter missing. Syntax: ?q=querytext');
	} else {
		console.log('Search for vocabularies starting with "'+req.query.q+'"')
		return execSearchVocabularies(req.query.q, req.query.page || 1, function(err, results) {
		  return standardCallback(req, res, err, results);
		});
	}
});

app.listen(2000,"127.0.0.1");