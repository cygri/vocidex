package org.deri.vocidex;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A document that can be indexed. Documents consist of:
 * 
 * 1. A JSON object (the contents)
 * 2. A URI as a unique ID
 * 3. A document type (string). 
 * 
 * @author Richard Cyganiak
 */
public class VocidexDocument {
	private final static ObjectMapper mapper = new ObjectMapper();

	private static String toJsonString(ObjectNode description) {
		try {
			return mapper.writeValueAsString(description);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static ObjectNode toJsonObject(String json) {
		try {
			return (ObjectNode) mapper.readTree(json);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private final String type;
	private final Resource uri;
	private final ObjectNode root;

	/**
	 * Creates a new empty indexable document. Properties can be added via
	 * the JSON object returned by {@link #getRoot()}.
	 * 
	 * @param type The type of document
	 * @param uri The id that uniquely names the document
	 */
	public VocidexDocument(String type, Resource uri) {
		this(type, uri, mapper.createObjectNode());
	}
	
	/**
	 * Creates a new indexable document from a JSON string.
	 * 
	 * @param type The type of document
	 * @param uri The id that uniquely names the document
	 * @param json JSON contents of the document
	 */
	public VocidexDocument(String type, Resource uri, String json) {
		this(type, uri, toJsonObject(json));
	}
	
	/**
	 * Creates a new indexable document from a JSON object.
	 * 
	 * @param type The type of document
	 * @param uri The id that uniquely names the document
	 * @param description JSON object with the contents of the document
	 */
	public VocidexDocument(String type, Resource uri, ObjectNode description) {
		if (!uri.isURIResource()) {
			throw new IllegalArgumentException("Document ID is not a URI resource: " + uri);
		}
		this.type = type;
		this.uri = uri;
		this.root = description;
	}
	
	public String getType() {
		return type;
	}
	
	public Resource getURI() {
		return uri;
	}
	
	/**
	 * @return String form of {@link #getURI()}
	 */
	public String getId() {
		return uri.getURI();
	}

	public ObjectNode getRoot() {
		return root;
	}
	
	public String getJSONContents() {
		return toJsonString(root);
	}
}
