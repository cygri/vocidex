package org.deri.vocidex.describers;

import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * TODO: This is really ugly. Should probably be a Describer and should look up the resource's definition.
 * 
 * @author Richard Cyganiak
 */
public class DatatypeIdentifier {

	public boolean isDatatype(Resource uri) {
		// xsd:anything
		if (uri.getNameSpace().equals(XSD.getURI())) return true;
		// rdf:XMLLiteral
		if (uri.getURI().equals(XMLLiteralType.theXMLLiteralType.getURI())) return true;
		// rdf:Literal (not strictly speaking a datatype, but the class of all literals)
		if (uri.equals(RDFS.Literal)) return true;
		return false;
	}
}
