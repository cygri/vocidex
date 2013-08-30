package org.deri.vocidex.describers;

import org.deri.vocidex.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Provides labels for resources, either by querying a SPARQL source for
 * rdfs:label and other properties, or if that fails, then by synthesizing
 * a label from the URI.
 * 
 * TODO: Cache the labels
 * TODO: Turn this into a {@link SPARQLDescriber}
 * 
 * @author Richard Cyganiak
 */
public class LabelProvider {
	private SPARQLRunner source;
	
	public LabelProvider(SPARQLRunner source) {
		this.source = source;
	}

	private String getLabelFromSource(Resource term) {
		return source.getLangString("term-label.sparql", term, "label");
	}
	
	private String makeUpLabelFromURI(String uri) {
		return uri.replaceFirst("^.*[#:/](.+)$", "$1");
	}

	public String getLabel(Resource term) {
		return getLabelFromSource(term) != null ? getLabelFromSource(term) : makeUpLabelFromURI(term.getURI());
	}
}
