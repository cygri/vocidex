package org.deri.vocidex;

import org.codehaus.jackson.JsonNode;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Indexes an RDFS vocabulary or OWL ontolgoy, provided as a Jena Model,
 * into an ElasticSearch index.
 * 
 * TODO: Make indexing faster by using batch updates
 * 
 * @author Richard Cyganiak
 */
public class ResourceIndexer {
	private final static Logger log = LoggerFactory.getLogger(ResourceIndexer.class);
	
	private ResourceCollection resources;
	private String host;
	private String clusterName;
	private String indexName;
	private Client client = null;

	public ResourceIndexer(ResourceCollection resources, String host, String clusterName, String indexName) {
		this.resources = resources;
		this.host = host;
		this.clusterName = clusterName;
		this.indexName = indexName;
	}

	private void initClient() {
		if (client != null) return;
		Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", clusterName).build();
		client = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(host, 9300));
	}

	public void close() {
		client.close();
	}
	
	private void indexItem(JsonNode item) {
		initClient();
		String type = item.get("type").getTextValue();
		String json = resources.toString(item);
		IndexResponse response = client
				.prepareIndex(indexName, type, item.get("uri").getTextValue())
				.setSource(json).execute().actionGet();
		log.debug("Added new " + type + ", id " + response.getId());
	}

	public void doDelete() {
		initClient();
		log.info("Deleting index: " + indexName);
		client.admin().indices().prepareDelete(indexName).execute();
	}
	
	public void doIndex() {
		log.info("Creating index " + indexName);
		for (Resource resource: resources.getAllResources()) {
			log.info("Indexing resource " + resource.getURI());
			JsonNode item = resources.describeResource(resource);
			indexItem(item);
		}
	}
}
