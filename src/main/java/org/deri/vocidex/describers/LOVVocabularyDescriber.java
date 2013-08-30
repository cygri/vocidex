package org.deri.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.SPARQLRunner;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Produces a JSON description of a vocabulary, using the metadata present in LOV.
 * 
 * @author Richard Cyganiak
 */
public class LOVVocabularyDescriber extends SPARQLDescriber {
	public final static String TYPE = "vocabulary";

	public LOVVocabularyDescriber(SPARQLRunner source) {
		super(source);
	}
	
	public void describe(Resource vocabulary, ObjectNode descriptionRoot) {
		QuerySolution qs = getSource().getOneSolution("describe-lov-vocab.sparql", "vocab", vocabulary);
		descriptionRoot.put("type", TYPE);
		putString(descriptionRoot, "uri", vocabulary.getURI());
		putString(descriptionRoot, "prefix", qs.get("prefix").asLiteral().getLexicalForm());
		putString(descriptionRoot, "label", qs.get("title").asLiteral().getLexicalForm());
		putString(descriptionRoot, "shortLabel", qs.contains("shortTitle") ? qs.get("shortTitle").asLiteral().getLexicalForm() : null);
		putString(descriptionRoot, "comment", qs.contains("description") ? qs.get("description").asLiteral().getLexicalForm() : null);
		putString(descriptionRoot, "homepage", qs.contains("homepage") ? qs.get("homepage").asResource().getURI() : null);
	}	
}
