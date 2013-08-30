package org.deri.vocidex.describers;

import java.util.Collection;

import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

public class PropertyDescriber extends TermDescriber {
	public final static String TYPE = "property";

	private final DatatypeIdentifier datatypeIdentifier;
	
	public PropertyDescriber(SPARQLRunner source) {
		super(source);
		this.datatypeIdentifier = new DatatypeIdentifier();
	}
	
	public Collection<Resource> getDomains(Resource property) {
		return getSource().getURIs("property-domains.sparql", "term", property, "domain");
	}
	
	public Collection<Resource> getRanges(Resource property) {
		return getSource().getURIs("property-ranges.sparql", "term", property, "range");
	}
	
	public Collection<Resource> getSuperproperties(Resource property) {
		return getSource().getURIs("property-superproperties.sparql", "term", property, "superproperty");
	}
	
	public Collection<Resource> getInverseProperties(Resource property) {
		return getSource().getURIs("property-inverse-properties.sparql", "term", property, "inverseProperty");
	}
	
	public Collection<Resource> getEquivalentProperties(Resource property) {
		return getSource().getURIs("property-equivalent-properties.sparql", "term", property, "equivalentProperty");
	}

	public Collection<Resource> getTypes(Resource property) {
		return getSource().getURIs("term-types.sparql", "term", property, "type");
	}
	
	public void describe(Resource property, ObjectNode descriptionRoot) {
		super.describe(TYPE, property, descriptionRoot);
		putURIArrayWithLabels(descriptionRoot, "domains", getDomains(property), labelProvider);
		putURIArrayWithLabels(descriptionRoot, "ranges", getRanges(property), labelProvider, datatypeIdentifier);
		putURIArrayWithLabels(descriptionRoot, "superproperties", getSuperproperties(property), labelProvider);
		putURIArrayWithLabels(descriptionRoot, "inverseProperties", getInverseProperties(property), labelProvider);
		putURIArrayWithLabels(descriptionRoot, "equivalentProperties", getEquivalentProperties(property), labelProvider);
		Collection<Resource> types = getTypes(property);
		putBoolean(descriptionRoot, "isAnnotationProperty", types.contains(OWL.AnnotationProperty));
		putBoolean(descriptionRoot, "isObjectProperty", types.contains(OWL.ObjectProperty));
		putBoolean(descriptionRoot, "isDatatypeProperty", types.contains(OWL.DatatypeProperty));
		putBoolean(descriptionRoot, "isFunctionalProperty", types.contains(OWL.FunctionalProperty));
		putBoolean(descriptionRoot, "isInverseFunctionalProperty", types.contains(OWL.InverseFunctionalProperty));
		putBoolean(descriptionRoot, "isTransitiveProperty", types.contains(OWL.TransitiveProperty));
		putBoolean(descriptionRoot, "isSymmetricProperty", types.contains(OWL.SymmetricProperty));
	}
}
