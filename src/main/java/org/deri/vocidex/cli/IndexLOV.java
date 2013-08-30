package org.deri.vocidex.cli;

import java.util.Iterator;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.deri.vocidex.VocidexDocument;
import org.deri.vocidex.VocidexException;
import org.deri.vocidex.VocidexIndex;
import org.deri.vocidex.extract.LOVExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.shared.NotFoundException;

/**
 * A command line tool that indexes an LOV dump, adding all vocabularies
 * and their terms to the index. Uses {@link LOVExtractor}.
 * 
 * @author Richard Cyganiak
 */
public class IndexLOV extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(IndexLOV.class);
	
	public static void main(String... args) {
		new IndexLOV(args).mainRun();
	}

	private String clusterName;
	private String hostName;
	private String indexName;
	private String lovDumpFile;
	
	public IndexLOV(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("clusterName", "ElasticSearch cluster name (e.g., elasticsearch)");
		getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
		getUsage().addUsage("indexName", "Target ElasticSearch index (e.g., lov)");
		getUsage().addUsage("lov.nq", "Filename or URL of the LOV N-Quads dump");
	}
	
	@Override
    protected String getCommandName() {
		return "index-lov";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " clusterName hostname indexName lov.nq";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 4) {
			doHelp();
		}
		clusterName = getPositionalArg(0);
		hostName = getPositionalArg(1);
		indexName = getPositionalArg(2);
		lovDumpFile = getPositionalArg(3);
	}

	@Override
	protected void exec() {
		try {
			log.info("Loading LOV dump: " + lovDumpFile);
			Dataset dataset = RDFDataMgr.loadDataset(lovDumpFile, Lang.NQUADS);
			long graphCount = 1;
			long tripleCount = dataset.getDefaultModel().size();
			Iterator<String> it = dataset.listNames();
			while (it.hasNext()) {
				graphCount++;
				tripleCount += dataset.getNamedModel(it.next()).size();
			}
			log.info("Read " + tripleCount + " triples in " + graphCount + " graphs");

			VocidexIndex index = new VocidexIndex(clusterName, hostName, indexName);
			try {
				if (!index.exists()) {
					throw new VocidexException("Index '" + indexName + "' does not exist on the cluster. Create the index first!");
				}
				LOVExtractor lovTransformer = new LOVExtractor(dataset);
				for (VocidexDocument document: lovTransformer) {
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
