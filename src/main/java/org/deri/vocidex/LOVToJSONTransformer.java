package org.deri.vocidex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;

public class LOVToJSONTransformer extends SPARQLToJSONHelper implements ResourceCollection {
	private final Dataset dataset;

	public LOVToJSONTransformer(Dataset dataset) {
		super(dataset);
		this.dataset = dataset;
	}
	
	public Collection<Resource> getVocabularies() {
		if (vocabulariesCache == null) {
			vocabulariesCache = getURIs("list-lov-vocabularies.sparql", null, null, "vocab");
		}
		return vocabulariesCache;
	}
	private Collection<Resource> vocabulariesCache = null;
	
	public ObjectNode describeVocabulary(Resource resource) {
		QuerySolution qs = getDescriptionWithCache(resource);
		ObjectNode json = mapper.createObjectNode();
		json.put("type", "vocabulary");
		putString(json, "uri", resource.getURI());
		putString(json, "prefix", qs.get("prefix").asLiteral().getLexicalForm());
		putString(json, "label", qs.get("title").asLiteral().getLexicalForm());
		putString(json, "shortLabel", qs.contains("shortTitle") ? qs.get("shortTitle").asLiteral().getLexicalForm() : null);
		putString(json, "comment", qs.contains("description") ? qs.get("description").asLiteral().getLexicalForm() : null);
		putString(json, "homepage", qs.contains("homepage") ? qs.get("homepage").asResource().getURI() : null);
		return json;
	}
	
	public Collection<Resource> listTerms(Resource vocabulary) {
		return getURIs("lov-vocabulary-terms.sparql", "vocab", vocabulary, "term");
	}
	
	private QuerySolution getDescriptionWithCache(Resource resource) {
		if (!descriptionCache.containsKey(resource)) {
			descriptionCache.put(resource, getOneSolution("describe-lov-vocab.sparql", "vocab", resource));
		}
		return descriptionCache.get(resource);
	}
	private final Map<Resource,QuerySolution> descriptionCache = new HashMap<Resource,QuerySolution>();
	
	public VocabularyToJSONTransformer getVocabularyTransformer(Resource resource) {
		VocabularyToJSONTransformer result = new VocabularyToJSONTransformer(dataset.getNamedModel(resource.getURI()));
		result.setVocabularyDescription(describeVocabulary(resource));
		result.setAllTerms(listTerms(resource));
		return result;
	}
	
	private VocabularyToJSONTransformer getTransformer(Resource vocab) {
		if (!transformerCache.containsKey(vocab)) {
			VocabularyToJSONTransformer t = getVocabularyTransformer(vocab);
			transformerCache.put(vocab, t);
		}
		return transformerCache.get(vocab);
	}
	private final Map<Resource,VocabularyToJSONTransformer> transformerCache = new HashMap<Resource,VocabularyToJSONTransformer>();
	
	@Override
	public Collection<Resource> getAllResources() {
		Set<Resource> result = new HashSet<Resource>();
		Collection<Resource> vocabs = getVocabularies();
		result.addAll(vocabs);
		for (Resource vocab: vocabs) {
			Collection<Resource> termsInVocab = getTransformer(vocab).getAllResources();
			result.addAll(termsInVocab);
			for (Resource term: termsInVocab) {
				transformerCache.put(term, getTransformer(vocab));
			}
		}
		return result;
	}

	@Override
	public JsonNode describeResource(Resource resource) {
		if (getVocabularies().contains(resource)) {
			return describeVocabulary(resource);
		}
		return getTransformer(resource).describeResource(resource);
	}

	@Override
	public String toString(JsonNode json) {
		return asJsonString(json);
	}
}
