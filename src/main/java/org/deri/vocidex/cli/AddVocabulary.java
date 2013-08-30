package org.deri.vocidex.cli;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;
import org.deri.vocidex.SPARQLRunner;
import org.deri.vocidex.VocidexDocument;
import org.deri.vocidex.VocidexException;
import org.deri.vocidex.VocidexIndex;
import org.deri.vocidex.extract.VocabularyTermExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * A command line utility that adds a single RDFS/OWL file to the index.
 * 
 * @author Richard Cyganiak
 */
public class AddVocabulary extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(AddVocabulary.class);
	
	public static void main(String... args) {
		new AddVocabulary(args).mainRun();
	}

	private String clusterName;
	private String hostName;
	private String indexName;
	private String inFile;
	
	public AddVocabulary(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("clusterName", "ElasticSearch cluster name (e.g., elasticsearch)");
		getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
		getUsage().addUsage("indexName", "ElasticSearch target index name (e.g., vocabs)");
		getUsage().addUsage("input.rdf", "RDFS/OWL file or URL to be indexed; many RDF formats supported");
	}
	
	@Override
    protected String getCommandName() {
		return "add-vocabulary";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " clusterName hostname indexName input.rdf";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 4) {
			doHelp();
		}
		clusterName = getPositionalArg(0);
		hostName = getPositionalArg(1);
		indexName = getPositionalArg(2);
		inFile = getPositionalArg(3);
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
			
			VocidexIndex index = new VocidexIndex(clusterName, hostName, indexName);
			try {
				if (!index.exists()) {
					throw new VocidexException("Index '" + indexName + "' does not exist on the cluster. Create the index first!");
				}
				for (VocidexDocument document: new VocabularyTermExtractor(new SPARQLRunner(model))) {
					log.info("Indexing " + document.getId());
					String resultId = index.addDocument(document);
					log.debug("Added new " + document.getType() + ", id " + resultId);
				}
				log.info("Done!");
			} finally {
				index.close();
			}
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (VocidexException ex) {
			cmdError(ex.getMessage());
		}
	}
}
