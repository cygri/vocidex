package org.deri.vocidex;

import java.util.Collection;

import org.codehaus.jackson.JsonNode;

import com.hp.hpl.jena.rdf.model.Resource;

public interface ResourceCollection {

	public Collection<Resource> getAllResources();
	
	public JsonNode describeResource(Resource resource);
	
	public String toString(JsonNode json);
}
