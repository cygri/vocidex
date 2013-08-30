package org.deri.vocidex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileUtils;

/**
 * Convenience class for running SPARQL queries stored in files against
 * a Jena {@link Model} or {@link Dataset}.
 * 
 * @author Richard Cyganiak
 */
public class SPARQLRunner {
	private final Dataset dataset;
	
	public SPARQLRunner(Model model) {
		this(DatasetFactory.create(model));
	}
	
	public SPARQLRunner(Dataset dataset) {
		this.dataset = dataset;
	}
	
	public List<Resource> getURIs(String queryFile, String paramVariable, Resource paramValue, String resultVariable) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		ArrayList<Resource> result = new ArrayList<Resource>();
		ResultSet rs = QueryExecutionFactory.create(query, dataset, args).execSelect();
		while (rs.hasNext()) {
			RDFNode n = rs.next().get(resultVariable);
			if (n == null || !n.isURIResource()) continue;
			result.add(n.asResource());
		}
		Collections.sort(result, new Comparator<Resource>() {
			public int compare(Resource r1, Resource r2) {
				return r1.getURI().compareTo(r2.getURI());
			}
		});
		return result;
	}
	
	public String getLangString(String queryFile, Resource term, String resultVariable) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		args.add("term", term);
		args.add("prefLang", ResourceFactory.createPlainLiteral("en"));
		ResultSet rs = QueryExecutionFactory.create(query, dataset, args).execSelect();
		if (!rs.hasNext()) return null;
		RDFNode n = rs.next().get(resultVariable);
		if (n == null || !n.isLiteral()) return null;
		return n.asLiteral().getLexicalForm();
	}

	public QuerySolution getOneSolution(String queryFile, String paramVariable, Resource paramValue) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		QueryExecution qe = QueryExecutionFactory.create(query, dataset, args);
		ResultSet rs = qe.execSelect();
		if (!rs.hasNext()) return null;
		QuerySolution result = rs.next();
		qe.close();
		return result;
	}
	
	private static Query getQuery(String filename) {
		if (!queryCache.containsKey(filename)) {
			try {
				return QueryFactory.create(FileUtils.readWholeFileAsUTF8(
						SPARQLRunner.class.getResourceAsStream("/queries/" + filename)));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
		return queryCache.get(filename);
	}
	private static final Map<String,Query> queryCache = new HashMap<String,Query>();
}
