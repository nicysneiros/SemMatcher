package speed.ontologymatcher.util.sparql;

import java.util.ArrayList;
import java.util.Iterator;

import speed.ontologymatcher.basics.OntologyClass;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Classe que gerencia consultas SPARQL sobre um modelo.
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public class QueryManager {
	
	/**
	 * Modelo a se submeter as queries.
	 */
	private Model modelToQuery;
	
	/**
	 * Construtor que recebe modelo a ser submetido as queries.
	 * @param model
	 */
	public QueryManager(Model model)
	{
		this.modelToQuery = model;		
	}
	
	/**
	 * Método que executa uma query no modelo.
	 * @param queryString Query em SPARQL
	 * @return Resultado da consulta (statements).
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<SpeedStatement> query(String queryString)
	{
		ArrayList<SpeedStatement> statementResults = new ArrayList<SpeedStatement>();
		
		QueryExecution queryExecution = QueryExecutionFactory.create(queryString, this.modelToQuery);
		
		try {
			
			ResultSet results = queryExecution.execSelect();
			
			while (results.hasNext()) {
				
				QuerySolution querySolution = results.nextSolution();
				Iterator iVariables = querySolution.varNames();
				
				SpeedStatement statement = new SpeedStatement();

				//L é sujeito
				if (iVariables.hasNext()) {

					String var = (String) iVariables.next();
					RDFNode node = querySolution.get(var);
					
					if(node != null)
						statement.setSubject(new OntologyClass(node.toString()));
				}
				
				//L é objeto
				if (iVariables.hasNext()) {

					String var = (String) iVariables.next();
					RDFNode node = querySolution.get(var);
					
					if(node != null)
						statement.setObject(new OntologyClass(node.toString()));
				}
				statementResults.add(statement);
			}
		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			queryExecution.close();
		}
		
		return statementResults;
	}
	

}
