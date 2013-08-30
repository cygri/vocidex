package org.deri.vocidex.extract;

import java.util.Iterator;

import org.deri.vocidex.VocidexDocument;

/**
 * Extracts indexable documents ({@link VocidexDocument} instances)
 * from some data source and provides an iterator over the documents.
 *  
 * @author Richard Cyganiak
 */
public interface Extractor extends Iterable<VocidexDocument> {

	/**
	 * Returns an iterator over indexable {@link VocidexDocument}s
	 * extracted from an underlying data source.
	 */
	public abstract Iterator<VocidexDocument> iterator();
}