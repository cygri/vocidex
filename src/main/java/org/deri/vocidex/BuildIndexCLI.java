package org.deri.vocidex;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * A command line interface for invoking the {@link ResourceIndexer}.
 * It is built using Jena's command line library.
 * 
 * @author Richard Cyganiak
 */
public class BuildIndexCLI extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(BuildIndexCLI.class);
	
	public static void main(String... args) {
		new BuildIndexCLI(args).mainRun();
	}

	private String inFile;
	private String clusterName;
	private String hostName;
	private String indexName;
	
	public BuildIndexCLI(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("input.rdf", "RDFS/OWL file or URL to be indexed; many RDF formats supported");
		getUsage().addUsage("clusterName", "ElasticSearch cluster name");
		getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
		getUsage().addUsage("indexName", "ElasticSearch target index name (e.g., vocabs)");
	}
	
	@Override
    protected String getCommandName() {
		return "build-index";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " input.rdf clusterName hostname indexName";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 4) {
			doHelp();
		}
		inFile = getPositionalArg(0);
		clusterName = getPositionalArg(1);
		hostName = getPositionalArg(2);
		indexName = getPositionalArg(3);
	}

	@Override
	protected void exec() {
		try {
			log.info("Loading RDF file: " + inFile);
			final Model model = ModelFactory.createDefaultModel();
			// Ignore the fourth element in quad-based formats. There has to be a simpler way of doing this!?
			RDFDataMgr.parse(new StreamRDFBase() {
				@Override
				public void triple(Triple triple) {
					model.getGraph().add(triple);
				}
				@Override
				public void quad(Quad quad) {
					model.getGraph().add(quad.asTriple());
				}
			}, inFile);
			
			log.info("Read " + model.size() + " triples");
			VocabularyToJSONTransformer transformer = new VocabularyToJSONTransformer(model);
			ResourceIndexer indexer = new ResourceIndexer(transformer, hostName, clusterName, indexName);
			try {
				indexer.doDelete();
				indexer.doIndex();
			} finally {
				indexer.close();
			}
			log.info("Done!");
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		}
	}
}
