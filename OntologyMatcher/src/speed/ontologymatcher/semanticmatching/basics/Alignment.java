package speed.ontologymatcher.semanticmatching.basics;

import java.util.ArrayList;

import speed.ontologymatcher.basics.OntologyResource;
import speed.ontologymatcher.util.ontologyhelpers.JenaOntology;
import speed.ontologymatcher.util.sparql.SpeedStatement;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Classe que representa um alinhamento semântico.
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public class Alignment {
	
	/**
	 * Sujeito do statement.
	 */
	private OntologyResource subject;
	/**
	 * Predicado do statement.
	 */
	private String predicate;
	/**
	 * Objeto do statement.
	 */
	private OntologyResource object;
	/**
	 * Valor do alinhamento.
	 */
	private double weight;
	
	public Alignment() {
		this.weight = -1.0;
	}
	
	public Alignment(SpeedStatement stmt) {
		this.subject = stmt.getSubject();
		this.predicate = stmt.getPredicate();
		this.object = stmt.getObject();
		this.weight = -1.0;
	}
	
	public Alignment(OntologyResource subject, String predicate, OntologyResource obj)
	{
		this.subject = subject;
		this.predicate = predicate;
		this.object = obj;		
	}
	
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public void setSubject(OntologyResource subject) {
		this.subject = subject;
	}
	public OntologyResource getSubject() {
		return subject;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	public String getPredicate() {
		return predicate;
	}
	public void setObject(OntologyResource object) {
		this.object = object;
	}
	public OntologyResource getObject() {
		return object;
	}
	
	/**
	 * Método auxiliar que cria um modelo do Jena (Model) a partir de uma lista de Alignments.
	 * @param alignments Lista de Alignments.
	 * @return Modelo do Jena contendo todos alignments.
	 */
	public static Model createJenaModelFromAlignment(ArrayList<Alignment> alignments)
	{	
		JenaOntology ontHelp = new JenaOntology();

		for (Alignment ali : alignments) {	
			ontHelp.addStatementToModel(ali.getSubject(), 
					ali.getPredicate(), ali.getObject());
		}
		return ontHelp.getModel();
	}
	
	public static ArrayList<Alignment> invertAlignments(ArrayList<Alignment> alignments)
	{
		ArrayList<Alignment> invertedAlignments = new ArrayList<Alignment>();
		
		Alignment ali = null;
		for(Alignment align : alignments)
		{
			ali = new Alignment();
			ali.setSubject(align.getObject());
			ali.setObject(align.getSubject());
			ali.setPredicate(align.getPredicate());
			ali.setWeight(align.getWeight());
			
			invertedAlignments.add(ali);			
		}
		
		return invertedAlignments;
	}
	
}
