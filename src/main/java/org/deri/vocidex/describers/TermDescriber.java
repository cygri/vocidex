package org.deri.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;

public abstract class TermDescriber extends SPARQLDescriber {
	protected final LabelDescriber labelDescriber;
	private final String prefix;
	
	public TermDescriber(SPARQLRunner source, String prefix) {
		super(source);
		this.prefix = prefix;
		this.labelDescriber = new LabelDescriber(source);
	}
	
	public String getURI(Resource term) {
		return term.getURI();
	}
	
	public String getLocalName(Resource term) {
		return term.getLocalName();
	}

	public String getComment(Resource term) {
		return getSource().getLangString("term-comment.sparql", term, "comment");
	}

	public void describe(String type, Resource term, ObjectNode descriptionRoot) {
		descriptionRoot.put("type", type);
		putString(descriptionRoot, "uri", getURI(term));
		if (prefix != null) {
			descriptionRoot.put("prefix", prefix);
			descriptionRoot.put("prefixed", prefix + ":" + getLocalName(term));
		};
		putString(descriptionRoot, "localName", getLocalName(term));
		// Adds "label" key
		labelDescriber.describe(term, descriptionRoot);
		putString(descriptionRoot, "comment", getComment(term));
	}
}
