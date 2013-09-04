package org.deri.vocidex.describers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Provides labels for resources in the "label" JSON key,
 * either by querying a SPARQL source for
 * rdfs:label and other properties, or if that fails, then by synthesizing
 * a label from the URI.
 * 
 * TODO: Cache the labels
 * 
 * @author Richard Cyganiak
 */
public class LabelDescriber extends SPARQLDescriber {
	
	public LabelDescriber(SPARQLRunner source) {
		super(source);
	}

	@Override
	public void describe(Resource term, ObjectNode descriptionRoot) {
		descriptionRoot.put("label", getLabel(term));
	}

	private String getLabelFromSource(Resource term) {
		return getSource().getLangString("term-label.sparql", term, "label");
	}
	
	/**
	 * Generates a label from the URI. Currently simply takes the local name.
	 */
	public String synthesizeLabelFromURI(String uri) {
		try {
			uri = URLDecoder.decode(uri, "utf-8");
		} catch (UnsupportedEncodingException ex) {
			// Can't happen, UTF-8 is always supported
		}
		uri = uri.
				replaceFirst("^.*[#:/](.+?)[#:/]*$", "$1").
				replaceAll("_+", " ").
				replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
		Matcher matcher = uppercaseWordPattern.matcher(uri);
		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(result, matcher.group().toLowerCase());
		}
		matcher.appendTail(result);
		return result.toString();
	}
	private final static Pattern uppercaseWordPattern =
			Pattern.compile("(?<!\\p{Lu})(\\p{Lu})(?!\\p{Lu})");

	public String getLabel(Resource term) {
		String label = getLabelFromSource(term);
		return label != null ? label : synthesizeLabelFromURI(term.getURI());
	}
}
