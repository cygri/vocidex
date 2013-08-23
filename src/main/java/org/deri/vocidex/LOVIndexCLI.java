package org.deri.vocidex;

import java.util.Iterator;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NotFoundException;

/**
 * A command line interface for invoking the {@link LOVIndexer}.
 * It is built using Jena's command line library.
 * 
 * @author Richard Cyganiak
 */
public class LOVIndexCLI extends CmdGeneral {
	
	public final static String lovIndexName = "lov";
	
	private final static Logger log = LoggerFactory.getLogger(LOVIndexCLI.class);
	
	public static void main(String... args) {
		new LOVIndexCLI(args).mainRun();
	}

	private String lovDumpFile;
	private String clusterName;
	private String hostName;
	
	public LOVIndexCLI(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("lov.nq", "Filename or URL of the LOV N-Quads dump");
		getUsage().addUsage("clusterName", "ElasticSearch cluster name");
		getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
	}
	
	@Override
    protected String getCommandName() {
		return "lov-index";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " lov.nq clusterName hostname";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 3) {
			doHelp();
		}
		lovDumpFile = getPositionalArg(0);
		clusterName = getPositionalArg(1);
		hostName = getPositionalArg(2);
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
			LOVToJSONTransformer lovTransformer = new LOVToJSONTransformer(dataset);
			ResourceIndexer indexer = new ResourceIndexer(lovTransformer, hostName, clusterName, lovIndexName);
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
