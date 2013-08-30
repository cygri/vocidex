package org.deri.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.JSONHelper;
import org.deri.vocidex.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A {@link Describer} that describes resources by running SPARQL queries
 * against an RDF source.
 * 
 * @author Richard Cyganiak
 */
public abstract class SPARQLDescriber extends JSONHelper implements Describer {
	private final SPARQLRunner source;
	
	public SPARQLDescriber(SPARQLRunner source) {
		this.source = source;
	}
	
	public SPARQLRunner getSource() {
		return source;
	}
	
	public abstract void describe(Resource resource, ObjectNode descriptionRoot);
}
