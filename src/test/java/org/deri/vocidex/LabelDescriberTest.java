package org.deri.vocidex;

import static org.junit.Assert.assertEquals;

import org.deri.vocidex.describers.LabelDescriber;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class LabelDescriberTest {
	private Model model;
	private SPARQLRunner source;
	
	@Before
	public void setUp() {
		model = ModelFactory.createDefaultModel();
		source = new SPARQLRunner(model);
	}
	
	@Test
	public void testUseLocalNameOfHashURI() {
		assertEquals("name", getLabel("http://example.com/ns#name"));
	}
	
	@Test
	public void testUseLocalNameOfSlashURI() {
		assertEquals("name", getLabel("http://example.com/ns/name"));
	}
	
	@Test
	public void testIgnoreTrailingHash() {
		assertEquals("ns", getLabel("http://example.com/ns#"));
	}
	
	@Test
	public void testIgnoreTrailingSlash() {
		assertEquals("ns", getLabel("http://example.com/ns/"));
	}
	
	@Test
	public void testConvertUppercaseWordsToLowerCase() {
		assertEquals("name", getLabel("http://example.com/ns#Name"));
		assertEquals("UK", getLabel("http://example.com/ns#UK"));
		assertEquals("some thing", getLabel("http://example.com/ns#SomeThing"));
	}
	
	@Test
	public void testTreatUnderscoresAsSpaces() {
		assertEquals("based near", getLabel("http://example.com/ns#based_near"));
	}

	@Test
	public void testSplitCamelCase() {
		assertEquals("see also", getLabel("http://example.com/ns#seeAlso"));
		assertEquals("any URI", getLabel("http://example.com/ns#anyURI"));
	}

	@Test
	public void testDecodePercentEncodedSpaces() {
		assertEquals("foo bar", getLabel("http://example.com/foo%20bar"));
	}
	
	private String getLabel(String uri) {
		return new LabelDescriber(source).getLabel(
				ResourceFactory.createResource(uri));
	}
}
