package org.deri.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Provides a partial or complete JSON description of some resource.
 *  
 * @author Richard Cyganiak
 */
public interface Describer {
	
	/**
	 * Describes a resource and stores the result in fields of the provided JSON object.
	 * 
	 * @param resource The resource to be described
	 * @param descriptionRoot The root object of the description to be constructed; fields will be added to hold the describer's information.
	 */
	void describe(Resource resource, ObjectNode descriptionRoot);
}
