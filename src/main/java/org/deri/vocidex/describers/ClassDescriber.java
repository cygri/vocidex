package org.deri.vocidex.describers;

import java.util.Collection;

import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;

public class ClassDescriber extends TermDescriber {
	public final static String TYPE = "class";
	
	public ClassDescriber(SPARQLRunner source, String prefix) {
		super(source, prefix);
	}
	
	public Collection<Resource> getSuperclasses(Resource class_) {
		return getSource().getURIs("class-superclasses.sparql", "term", class_, "superclass");
	}
	
	public Collection<Resource> getDisjointClasses(Resource class_) {
		return getSource().getURIs("class-disjoint-classes.sparql", "term", class_, "disjointClass");
	}
	
	public Collection<Resource> getEquivalentClasses(Resource class_) {
		return getSource().getURIs("class-equivalent-classes.sparql", "term", class_, "equivalentClass");
	}
	
	public void describe(Resource class_, ObjectNode descriptionRoot) {
		super.describe(TYPE, class_, descriptionRoot);
		putURIArrayWithLabels(descriptionRoot, "superclasses", getSuperclasses(class_), labelDescriber);
		putURIArrayWithLabels(descriptionRoot, "disjointClasses", getDisjointClasses(class_), labelDescriber);
		putURIArrayWithLabels(descriptionRoot, "equivalentClasses", getEquivalentClasses(class_), labelDescriber);
	}
}
