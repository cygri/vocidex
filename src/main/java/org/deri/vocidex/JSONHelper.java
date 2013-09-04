package org.deri.vocidex;

import java.io.IOException;
import java.util.Collection;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.describers.DatatypeIdentifier;
import org.deri.vocidex.describers.LabelDescriber;

import com.hp.hpl.jena.rdf.model.Resource;

public class JSONHelper {
	protected final static ObjectMapper mapper = new ObjectMapper();
	
	public static ObjectNode createObject() {
		return mapper.createObjectNode();
	}

	public static String asJsonString(JsonNode jsonNode) {
		try {
			return mapper.writeValueAsString(jsonNode);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void putString(ObjectNode json, String key, String value) {
		if (value != null) {
			json.put(key, value);
		}
	}
	
	public void putBoolean(ObjectNode json, String key, boolean value) {
		if (value) {
			json.put(key, value);
		}
	}
	
	public void putURIArray(ObjectNode json, String key, Collection<Resource> uris) {
		ArrayNode array = mapper.createArrayNode();
		for (Resource uri: uris) {
			array.add(uri.getURI());
		}
		if (array.size() > 0) {
			json.put(key, array);
		}
	}	

	public void putURIArrayWithLabels(ObjectNode json, String key, 
			Collection<Resource> uris, LabelDescriber labeller) {
		putURIArrayWithLabels(json, key, uris, labeller, null);
	}

	public void putURIArrayWithLabels(ObjectNode json, String key, 
			Collection<Resource> uris, LabelDescriber labeller, DatatypeIdentifier datatypeIdentifier) {
		ArrayNode array = mapper.createArrayNode();
		for (Resource uri: uris) {
			ObjectNode o = mapper.createObjectNode();
			o.put("uri", uri.getURI());
			labeller.describe(uri, o);
			if (datatypeIdentifier != null) {
				if (datatypeIdentifier.isDatatype(uri)) {
					o.put("isDatatype", true);
				} else {
					o.put("isClass", true);
				}
			}
			array.add(o);
		}
		if (array.size() > 0) {
			json.put(key, array);
		}
	}	
}
