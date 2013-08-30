package org.deri.vocidex.extract;

import java.util.Collection;
import java.util.Iterator;

import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.JSONHelper;
import org.deri.vocidex.VocidexDocument;
import org.deri.vocidex.describers.Describer;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * A wrapper around another {@link Extractor} that enriches the results of the
 * other extractor with information about a vocabulary. The class also provides
 * a facility for filtering the results from the wrapped iterator, and skipping
 * certain documents.
 *  
 * @author Richard Cyganiak
 */
public class LOVWrapper extends Filter<VocidexDocument> implements Extractor {
	private final Extractor wrapped;
	private final Collection<Resource> eligibleResources;
	private final Describer vocabularyDescriber;
	private final Describer prefixedNameDescriber;
	
	/**
	 * @param wrapped The underlying extractor whose results are to be modified
	 * @param eligibleResources Any resources listed by the wrapped extractor but not in this collection will be ignored
	 * @param vocabularyDescription A vocabulary description based on this node is added to each returned document
	 */
	public LOVWrapper(VocabularyTermExtractor wrapped, Collection<Resource> eligibleResources, final ObjectNode vocabularyDescription) {
		this.wrapped = wrapped;
		this.eligibleResources = eligibleResources;
		this.vocabularyDescriber = new Describer() {
			@Override
			public void describe(Resource resource, ObjectNode descriptionRoot) {
				ObjectNode v = JSONHelper.createObject();
				v.putAll(vocabularyDescription);
				v.remove("shortLabel");
				v.remove("comment");
				descriptionRoot.put("vocabulary", v);
			}
		};
		final String prefix = vocabularyDescription.get("prefix").getTextValue();
		this.prefixedNameDescriber = new Describer() {
			@Override
			public void describe(Resource term, ObjectNode descriptionRoot) {
				descriptionRoot.put("prefixed", prefix + ":" + term.getLocalName());
			}
		};
	}
	
	/**
	 * Decides whether a document from the underlying extractor should be skipped.
	 * 
	 * @param doc A document returned from the underlying extractor
	 * @return Keep or skip?
	 */
	public boolean accept(VocidexDocument doc) {
		return eligibleResources.contains(doc.getURI());		
	}
	
	/**
	 * Modifies documents returned by the underlying extractor.
	 *  
	 * @param doc The document to be modified
	 * @return The modified document
	 */
	private VocidexDocument modifyDocument(VocidexDocument doc) {
		// Add "prefixed" field with prefixed name
		prefixedNameDescriber.describe(doc.getURI(), doc.getRoot());
		// Add "vocabulary" field with selected vocabulary details
		vocabularyDescriber.describe(doc.getURI(), doc.getRoot());
		return doc;
	}
	
	@Override
	public Iterator<VocidexDocument> iterator() {
		final Iterator<VocidexDocument> it = filterKeep(wrapped.iterator());
		return new Iterator<VocidexDocument>() {
			@Override public boolean hasNext() { return it.hasNext(); }
			@Override public VocidexDocument next() { return modifyDocument(it.next()); }
			@Override public void remove() { throw new UnsupportedOperationException(); }
		};
	}
}
