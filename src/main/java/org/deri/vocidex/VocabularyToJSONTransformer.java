package org.deri.vocidex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Reads an RDFS vocabulary or OWL ontology from a Jena Model, and allows
 * extraction of JSON representations of individual classes, properties
 * or datatypes, or of the entire vocabulary. Methods for listing all
 * classes, properties and datatypes are also provided. Works by running
 * SPARQL queries, stored in external files, against the model.
 * 
 * @author Richard Cyganiak
 */
public class VocabularyToJSONTransformer extends SPARQLToJSONHelper implements ResourceCollection {	
	private Collection<Resource> allTerms = null;
	private ObjectNode vocabulary = null;
	
	public VocabularyToJSONTransformer(Model model) {
		super(model);
	}

	/**
	 * Any term not in this list will not be considered part of
	 * the vocabulary. Use this if there are additional rules for
	 * what should be considered part of the vocabulary.
	 */
	public void setAllTerms(Collection<Resource> terms) {
		allTerms = terms;
	}
	
	public void setVocabularyDescription(ObjectNode vocabulary) {
		if (vocabulary == null) return;
		this.vocabulary = mapper.createObjectNode();
		this.vocabulary.putAll(vocabulary);
		this.vocabulary.remove("shortLabel");
		this.vocabulary.remove("comment");
	}
	
	public String getLabel(Resource term) {
		return getLangString("term-label.sparql", term, "label");
	}
	
	public String makeUpLabelFromURI(String uri) {
		return uri.replaceFirst("^.*[#:/](.+)$", "$1");
	}

	public String getLabelOrMakeOneUp(Resource term) {
		return getLabel(term) != null ? getLabel(term) : makeUpLabelFromURI(term.getURI());
	}
	
	public String getComment(Resource term) {
		return getLangString("term-comment.sparql", term, "comment");
	}

	public Collection<Resource> getClasses() {
		return intersection(getURIs("list-classes.sparql", null, null, "class"), allTerms);
	}

	public Collection<Resource> getSuperclasses(Resource term) {
		return getURIs("class-superclasses.sparql", "term", term, "superclass");
	}
	
	public Collection<Resource> getDisjointClasses(Resource term) {
		return getURIs("class-disjoint-classes.sparql", "term", term, "disjointClass");
	}
	
	public Collection<Resource> getEquivalentClasses(Resource term) {
		return getURIs("class-equivalent-classes.sparql", "term", term, "equivalentClass");
	}
	
	public Collection<Resource> getProperties() {
		return intersection(getURIs("list-properties.sparql", null, null, "property"), allTerms);
	}

	public Collection<Resource> getTypes(Resource term) {
		return getURIs("term-types.sparql", "term", term, "type");
	}
	
	public Collection<Resource> getDomains(Resource term) {
		return getURIs("property-domains.sparql", "term", term, "domain");
	}
	
	public Collection<Resource> getRanges(Resource term) {
		return getURIs("property-ranges.sparql", "term", term, "range");
	}
	
	public Collection<Resource> getSuperproperties(Resource term) {
		return getURIs("property-superproperties.sparql", "term", term, "superproperty");
	}
	
	public Collection<Resource> getInverseProperties(Resource term) {
		return getURIs("property-inverse-properties.sparql", "term", term, "inverseProperty");
	}
	
	public Collection<Resource> getEquivalentProperties(Resource term) {
		return getURIs("property-equivalent-properties.sparql", "term", term, "equivalentProperty");
	}

	public Collection<Resource> getDatatypes() {
		if (datatypeCache == null) {
			datatypeCache = intersection(getURIs("list-datatypes.sparql", null, null, "datatype"), allTerms);
		}
		return datatypeCache;
	}
	private Collection<Resource> datatypeCache = null;

	private <K> Collection<K> intersection(Collection<K> set1, Collection<K> set2) {
		if (set1 == null) return set2;
		if (set2 == null) return set1;
		Set<K> result = new HashSet<K>(set1);
		result.retainAll(set2);
		return result;
	}

	private void putURIArrayWithLabels(ObjectNode json, String key, Collection<Resource> uris) {
		putURIArrayWithLabels(json, key, uris, false);
	}

	private void putURIArrayWithLabels(ObjectNode json, String key, Collection<Resource> uris, boolean withTypes) {
		ArrayNode array = mapper.createArrayNode();
		for (Resource uri: uris) {
			ObjectNode o = mapper.createObjectNode();
			o.put("uri", uri.getURI());
			o.put("label", getLabelOrMakeOneUp(uri));
			if (withTypes) {
				if (uri.hasProperty(RDF.type, RDFS.Class) || uri.hasProperty(RDF.type, OWL.Class)) {
					o.put("isClass", true);
				}
				if (getDatatypes().contains(uri)) {
					o.put("isDatatype", true);
				}
			}
			array.add(o);
		}
		if (array.size() > 0) {
			json.put(key, array);
		}
	}	

	private ObjectNode describeTerm(Resource term) {
		ObjectNode json = mapper.createObjectNode();
		putString(json, "uri", term.getURI());
		putString(json, "localName", term.getLocalName());
		String label = getLabelOrMakeOneUp(term);
		putString(json, "label", label);
		putString(json, "comment", getComment(term));
		if (vocabulary != null) {
			json.put("vocabulary", vocabulary);
			if (vocabulary.get("prefix") != null && vocabulary.get("prefix").getTextValue() != null) {
				json.put("prefixed", vocabulary.get("prefix").getTextValue() + ":" + term.getLocalName());
			}
		}
		return json;
	}
	
	public ObjectNode describeClass(Resource class_) {
		ObjectNode json = describeTerm(class_);
		json.put("type", "class");
		putURIArrayWithLabels(json, "superclasses", getSuperclasses(class_));
		putURIArrayWithLabels(json, "disjointClasses", getDisjointClasses(class_));
		putURIArrayWithLabels(json, "equivalentClasses", getEquivalentClasses(class_));
		return json;
	}

	public ObjectNode describeProperty(Resource property) {
		ObjectNode json = describeTerm(property);
		json.put("type", "property");
		Collection<Resource> types = getTypes(property);
		putURIArrayWithLabels(json, "domains", getDomains(property));
		putURIArrayWithLabels(json, "ranges", getRanges(property), true);
		putURIArrayWithLabels(json, "superproperties", getSuperproperties(property));
		putURIArrayWithLabels(json, "inverseProperties", getInverseProperties(property));
		putURIArrayWithLabels(json, "equivalentProperties", getEquivalentProperties(property));
		putBoolean(json, "isAnnotationProperty", types.contains(OWL.AnnotationProperty));
		putBoolean(json, "isObjectProperty", types.contains(OWL.ObjectProperty));
		putBoolean(json, "isDatatypeProperty", types.contains(OWL.DatatypeProperty));
		putBoolean(json, "isFunctionalProperty", types.contains(OWL.FunctionalProperty));
		putBoolean(json, "isInverseFunctionalProperty", types.contains(OWL.InverseFunctionalProperty));
		putBoolean(json, "isTransitiveProperty", types.contains(OWL.TransitiveProperty));
		putBoolean(json, "isSymmetricProperty", types.contains(OWL.SymmetricProperty));
		return json;
	}

	public ObjectNode describeDatatype(Resource datatype) {
		ObjectNode json = describeTerm(datatype);
		json.put("type", "datatype");
		return json;
	}
	
	public ObjectNode describeVocabulary() {
		ArrayNode classes = mapper.createArrayNode();
		for (Resource class_: getClasses()) {
			classes.add(describeClass(class_));
		}
		ArrayNode properties = mapper.createArrayNode();
		for (Resource property: getProperties()) {
			properties.add(describeProperty(property));
		}
		ArrayNode datatypes = mapper.createArrayNode();
		for (Resource datatype: getDatatypes()) {
			datatypes.add(describeDatatype(datatype));
		}
		ObjectNode result = mapper.createObjectNode();
		result.put("classes", classes);
		result.put("properties", properties);
		result.put("datatypes", datatypes);
		return result;
	}

	private Map<Resource,String> resourceTypes = null; 

	@Override
	public Collection<Resource> getAllResources() {
		if (resourceTypes == null) {
			resourceTypes = new HashMap<Resource,String>();
			addAll(getClasses(), "class");
			addAll(getProperties(), "property");
			addAll(getDatatypes(), "datatype");
		}
		return resourceTypes.keySet();
	}

	@Override
	public JsonNode describeResource(Resource resource) {
		String type = resourceTypes.get(resource);
		if (type == null) return null;
		if (type == "class") {
			return describeClass(resource);
		}
		if (type == "property") {
			return describeProperty(resource);
		}
		if (type == "datatype") {
			return describeDatatype(resource);
		}
		throw new IllegalArgumentException("Unknown type '" + type + "' for resource " + resource);
	}

	@Override
	public String toString(JsonNode json) {
		return asJsonString(json);
	}

	private void addAll(Collection<Resource> resources, String type) {
		for (Resource resource: resources) {
			resourceTypes.put(resource, type);
		}
	}
}
