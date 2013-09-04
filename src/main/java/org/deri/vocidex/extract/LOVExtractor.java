package org.deri.vocidex.extract;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.deri.vocidex.SPARQLRunner;
import org.deri.vocidex.VocidexDocument;
import org.deri.vocidex.describers.LOVVocabularyDescriber;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Extracts indexable {@link VocidexDocument} instances from a
 * dataset containing the LOV dump. Will create one doucment for
 * each vocabulary, and one document for every term defined in
 * those vocabularies.
 * 
 * @author Richard Cyganiak
 */
public class LOVExtractor implements Extractor {
	private final Dataset dataset;
	private final SPARQLRunner source;
	private final LOVVocabularyDescriber vocabularyDescriber;
	
	public LOVExtractor(Dataset dataset) {
		this.dataset = dataset;
		this.source = new SPARQLRunner(dataset);
		this.vocabularyDescriber = new LOVVocabularyDescriber(source);
	}
	
	public Collection<Resource> listVocabularies() {
		return source.getURIs("list-lov-vocabularies.sparql", null, null, "vocab");
	}
	
	public Collection<Resource> listDefinedTerms(Resource vocabulary) {
		return source.getURIs("lov-vocabulary-terms.sparql", "vocab", vocabulary, "term");
	}

	private SPARQLRunner getSPARQLRunnerForVocabulary(Resource vocabulary) {
		return new SPARQLRunner(dataset.getNamedModel(vocabulary.getURI()));
	}
	
	/**
	 * An iterator over all vocabularies, classes and properties in the dataset. For each
	 * vocabulary, we first return a result representing the vocabulary itself. This
	 * is done by creating a {@link VocidexDocument} around the {@link LOVVocabularyDescriber}
	 * result. Then we return all terms defined in the vocabulary by using
	 * {@link VocabularyTermExtractor} with an {@link LOVWrapper} around it.
	 */
	@Override
	public Iterator<VocidexDocument> iterator() {
		return new Iterator<VocidexDocument>() {
			private final Iterator<VocidexDocument> vocabIterator = 
					new DescriberIterator(listVocabularies(), vocabularyDescriber);
			private VocidexDocument currentVocabularyDocument = null;
			private Iterator<VocidexDocument> currentDocIterator = null;
			@Override
			public boolean hasNext() {
				if (currentVocabularyDocument != null) return true;
				if (currentDocIterator != null && currentDocIterator.hasNext()) return true;
				if (!vocabIterator.hasNext()) return false;

				// Document for the vocabulary itself
				currentVocabularyDocument = vocabIterator.next();
				Resource vocab = currentVocabularyDocument.getURI();
				
				// Extractor for all terms mentioned in the vocabulary graph
				VocabularyTermExtractor ex = new VocabularyTermExtractor(
						getSPARQLRunnerForVocabulary(vocab),
						currentVocabularyDocument.getRoot().get("prefix").getTextValue());
				
				// Keep only the documents actually defined in that vocabulary,
				// and enrich them with some extra vocabulary information
				currentDocIterator = new LOVWrapper(
						ex, listDefinedTerms(vocab), currentVocabularyDocument.getRoot()).iterator();

				// At least the vocabulary document always exists, so return true
				return true;
			}
			@Override
			public VocidexDocument next() {
				// hasNext() prepares for the next vocabulary if necessary
				if (!hasNext()) throw new NoSuchElementException();
				// Return vocabulary document first
				if (currentVocabularyDocument != null) {
					VocidexDocument result = currentVocabularyDocument;
					currentVocabularyDocument = null;
					return result;
				}
				// Then return documents from the iterator over its terms
				return currentDocIterator.next();
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
