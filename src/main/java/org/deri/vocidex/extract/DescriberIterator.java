package org.deri.vocidex.extract;

import java.util.Collection;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.JSONHelper;
import org.deri.vocidex.VocidexDocument;
import org.deri.vocidex.VocidexException;
import org.deri.vocidex.describers.Describer;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Utility class that implements an iterator over indexable
 * {@link VocidexDocument}s by calling a {@link Describer}
 * on every {@link Resource} in a collection.
 * 
 * @author Richard Cyganiak
 */
public class DescriberIterator implements Iterator<VocidexDocument> {
	private final Iterator<Resource> it;
	private final Describer describer;
	
	public DescriberIterator(Collection<Resource> resources, Describer describer) {
		this.it = resources.iterator();
		this.describer = describer;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public VocidexDocument next() {
		Resource resource = it.next();
		ObjectNode description = JSONHelper.createObject();
		describer.describe(resource, description);
		JsonNode typeNode = description.get("type");
		if (typeNode == null || !typeNode.isTextual()) {
			throw new VocidexException("Description for " + resource + " must include \"type\" key: " + description);
		}
		return new VocidexDocument(typeNode.getTextValue(), resource, description);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
