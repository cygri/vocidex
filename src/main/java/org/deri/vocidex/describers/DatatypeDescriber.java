package org.deri.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;

public class DatatypeDescriber extends TermDescriber {
	public final static String TYPE = "datatype";

	public DatatypeDescriber(SPARQLRunner source) {
		super(source);
	}
	
	public void describe(Resource datatype, ObjectNode descriptionRoot) {
		super.describe(TYPE, datatype, descriptionRoot);
	}
}
