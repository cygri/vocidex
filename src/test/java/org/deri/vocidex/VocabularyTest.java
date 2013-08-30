package org.deri.vocidex;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.deri.vocidex.describers.VocabularyDetailDescriber;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

@RunWith(Parameterized.class)
public class VocabularyTest {
	private final static String TEST_SUITE_DIR = "src/test/resources/vocabs";
	
	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> getTestList() {
		Collection<Object[]> results = new ArrayList<Object[]>();
		for (File f: new File(TEST_SUITE_DIR).listFiles()) {
			if (!f.getName().endsWith(".ttl")) continue;
			String jsonFile = changeExtension(f.getName(), "json");
			results.add(new Object[]{
					f.getName() + "/" + jsonFile, 
					f.toURI().toString(), 
					changeExtension(f.toURI().toString(), "json")});
		}
		return results;
	}

	private static String changeExtension(String fileName, String newExtension) {
		return fileName.replaceFirst("\\.[^/.]+$", "." + newExtension);
	}
	
	private final String turtleURL;
	private final String jsonURL;
	private Model model;
	private JsonNode expectedJson;
	
	public VocabularyTest(String name, String turtleURL, String jsonURL) {
		this.turtleURL = turtleURL;
		this.jsonURL = jsonURL;
	}

	@Before
	public void before() throws IOException {
		this.model = FileManager.get().loadModel(turtleURL);
		this.expectedJson = new ObjectMapper().readTree(new URL(jsonURL));
	}
	
	@Test
	public void test() {
		ObjectNode actualJson = JSONHelper.createObject();
		new VocabularyDetailDescriber(new SPARQLRunner(model)).describe(null, actualJson);
		assertEquals(expectedJson, actualJson);
	}
}
