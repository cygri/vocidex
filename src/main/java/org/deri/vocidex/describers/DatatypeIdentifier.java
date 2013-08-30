package org.deri.vocidex.describers;

import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * TODO: This is really ugly. Should probably be a Describer and should look up the resource's definition.
 * 
 * @author Richard Cyganiak
 */
public class DatatypeIdentifier {

	public boolean isDatatype(Resource uri) {
		if (uri.getNameSpace().equals(XSD.getURI())) return true;
		if (uri.getURI().equals(XMLLiteralType.theXMLLiteralType.getURI())) return true;
		return false;
	}
}
