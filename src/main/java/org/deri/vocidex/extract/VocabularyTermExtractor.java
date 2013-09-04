package org.deri.vocidex.extract;

import java.util.Iterator;

import org.deri.vocidex.SPARQLRunner;
import org.deri.vocidex.VocidexDocument;
import org.deri.vocidex.describers.ClassDescriber;
import org.deri.vocidex.describers.DatatypeDescriber;
import org.deri.vocidex.describers.SPARQLDescriber;
import org.deri.vocidex.describers.PropertyDescriber;

import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
 * Extracts all vocabulary terms (classes, properties, datatypes)
 * from a {@link SPARQLRunner}, describes them using the appropriate
 * describers, and packages them as indexable
 * {@link VocidexDocument}s.
 * 
 * @author Richard Cyganiak
 */
public class VocabularyTermExtractor implements Extractor {
	private final SPARQLRunner source;
	private final String prefix;
	
	/**
	 * @param source Model containing declarations of vocabulary terms
	 * @param prefix Prefix to be used for creating prefixed names; may be null
	 */
	public VocabularyTermExtractor(SPARQLRunner source, String prefix) {
		this.source = source;
		this.prefix = prefix;
	}
	
	/**
	 * Extract only classes
	 */
	public Iterator<VocidexDocument> classes() {
		return createDescriptionIterator("list-classes.sparql", "class", new ClassDescriber(source, prefix));
	}
	
	/**
	 * Extract only properties
	 */
	public Iterator<VocidexDocument> properties() {
		return createDescriptionIterator("list-properties.sparql", "property", new PropertyDescriber(source, prefix));
	}
	
	/**
	 * Extract only data types
	 */
	public Iterator<VocidexDocument> datatypes() {
		return createDescriptionIterator("list-datatypes.sparql", "datatype", new DatatypeDescriber(source, prefix));
	}

	/**
	 * Extract all terms (classes, properties, datatypes)
	 */
	@Override
	public Iterator<VocidexDocument> iterator() {
		return NiceIterator.andThen(classes(), properties()).andThen(datatypes());
	}
	
	private Iterator<VocidexDocument> createDescriptionIterator(
			String sparqlFileName, String sparqlResultVariable, SPARQLDescriber describer) {
		return new DescriberIterator(
				source.getURIs(sparqlFileName, null, null, sparqlResultVariable),
				describer);
	}
}
