package org.deri.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.JSONHelper;
import org.deri.vocidex.SPARQLRunner;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Produces a JSON description of metrics for a vocabulary term, using the metrics metadata present in LOV.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class LOVTermMetricsDescriber extends SPARQLDescriber {

	public LOVTermMetricsDescriber(SPARQLRunner source) {
		super(source);
	}
	
	public void describe(Resource term, ObjectNode descriptionRoot) {
		QuerySolution qs = getSource().getOneSolution("lov-term-metrics.sparql", "term", term);
		
		ObjectNode v = JSONHelper.createObject();
				
		if(qs!=null && qs.get("occurrencesInVocabularies")!=null) 
			putString(v, "occurrencesInVocabularies", qs.get("occurrencesInVocabularies").asLiteral().getLexicalForm());
		else putString(v, "occurrencesInVocabularies", "0");
		
		if(qs!=null && qs.get("occurrencesInDatasets")!=null) 
			putString(v, "occurrencesInDatasets", qs.get("occurrencesInDatasets").asLiteral().getLexicalForm());
		else putString(v, "occurrencesInDatasets", "0");
		
		if(qs!=null && qs.get("reusedByVocabularies")!=null) 
			putString(v, "reusedByVocabularies", qs.get("reusedByVocabularies").asLiteral().getLexicalForm());
		else putString(v, "reusedByVocabularies", "0");
		
		if(qs!=null && qs.get("reusedByDatasets")!=null) 
			putString(v, "reusedByDatasets", qs.get("reusedByDatasets").asLiteral().getLexicalForm());
		else putString(v, "reusedByDatasets", "0");
		
		descriptionRoot.put("metrics", v);
	}	
}
