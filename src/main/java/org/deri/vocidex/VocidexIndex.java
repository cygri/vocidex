package org.deri.vocidex;

import java.io.Closeable;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * A connection to a specific named index on an ElasticSearch cluster
 * 
 * @author Richard Cyganiak
 */
public class VocidexIndex implements Closeable {
	private final String clusterName;
	private final String hostName;
	private final String indexName;
	private Client client = null;
	
	public VocidexIndex(String clusterName, String hostName, String indexName) {
		this.clusterName = clusterName;
		this.hostName = hostName;
		this.indexName = indexName;
	}

	/**
	 * Connects to the cluster if not yet connected. Is called implicitly by
	 * all operations that require a connection. 
	 */
	public void connect() {
		if (client != null) return;
		Settings settings = ImmutableSettings.settingsBuilder()
		        .put("cluster.name", clusterName).build();
		client = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(hostName, 9300));
	}
	
	public void close() {
		if (client == null) return;
		client.close();
		client = null;
	}
	
	public boolean exists() {
		connect();
		return client.admin().indices().exists(Requests.indicesExistsRequest(indexName)).actionGet().isExists();
	}
	
	public void delete() {
		connect();
		client.admin().indices().prepareDelete(indexName).execute();		
	}
	
	public boolean create() {
		connect();
		return client.admin().indices().create(Requests.createIndexRequest(indexName)).actionGet().isAcknowledged();
		// TODO: Read mappings from a JSON file and set them on the index
	}
	
	/**
	 * Adds a document (that is, a JSON structure) to the index.
	 * @return The document's id
	 */
	public String addDocument(VocidexDocument document) {
		return client
				.prepareIndex(indexName, document.getType(), document.getId())
				.setSource(document.getJSONContents())
				.execute().actionGet().getId();
	}
}
