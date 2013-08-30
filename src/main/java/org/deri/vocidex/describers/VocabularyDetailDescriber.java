package org.deri.vocidex.describers;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.SPARQLRunner;
import org.deri.vocidex.VocidexDocument;
import org.deri.vocidex.extract.VocabularyTermExtractor;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Describes a vocabulary provided in an RDF graph by describing
 * the details of all classes, properties and datatypes.
 * 
 * @author Richard Cyganiak
 */
public class VocabularyDetailDescriber extends SPARQLDescriber {

	public VocabularyDetailDescriber(SPARQLRunner source) {
		super(source);
	}

	/**
	 * TODO: This actually ignores the first argument. Smells of a problem!
	 */
	@Override
	public void describe(Resource vocabulary, ObjectNode descriptionRoot) {
		ArrayNode classes = mapper.createArrayNode();
		ArrayNode properties = mapper.createArrayNode();
		ArrayNode datatypes = mapper.createArrayNode();
		for (VocidexDocument document: new VocabularyTermExtractor(getSource())) {
			if (document.getType() == ClassDescriber.TYPE) {
				classes.add(document.getRoot());
			} else if (document.getType() == PropertyDescriber.TYPE) {
				properties.add(document.getRoot());
			} else if (document.getType() == DatatypeDescriber.TYPE) {
				datatypes.add(document.getRoot());
			}
		}
		descriptionRoot.put("classes", classes);
		descriptionRoot.put("properties", properties);
		descriptionRoot.put("datatypes", datatypes);
	}
}
