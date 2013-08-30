package org.deri.vocidex.cli;

import org.deri.vocidex.VocidexIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

/**
 * A command line interface that initializes a Vocidex index.
 * If an index with the given name already exists, it will be
 * deleted.
 * 
 * @author Richard Cyganiak
 */
public class CreateIndex extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(CreateIndex.class);
	
	public static void main(String... args) {
		new CreateIndex(args).mainRun();
	}

	private String clusterName;
	private String hostName;
	private String indexName;
	
	public CreateIndex(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("clusterName", "ElasticSearch cluster name (e.g., elasticsearch)");
		getUsage().addUsage("hostname", "ElasticSearch hostname (e.g., localhost)");
		getUsage().addUsage("indexName", "Name for the new index (e.g., lov)");
	}
	
	@Override
    protected String getCommandName() {
		return "create-index";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " clusterName hostname indexName";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 3) {
			doHelp();
		}
		clusterName = getPositionalArg(0);
		hostName = getPositionalArg(1);
		indexName = getPositionalArg(2);
	}

	@Override
	protected void exec() {
		VocidexIndex index = new VocidexIndex(clusterName, hostName, indexName);
		try {
			if (index.exists()) {
				log.info("Deleting index: " + indexName);
				index.delete();
			}
			log.info("Creating index: " + indexName);
			if (index.create()) {
				log.info("Done!");
			} else {
				log.error("Error: Index creation not acknowledged!");
			}
		} finally {
			index.close();
		}
	}
}
