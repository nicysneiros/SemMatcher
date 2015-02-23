package speed.ontologymatcher.lematching.basics;

import java.util.ArrayList;

import speed.ontologymatcher.basics.OntologyResource;
import speed.ontologymatcher.lematching.enums.ERelation;

/**
 * Classe que representa um alinhamento linguístico-estrutural.
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public class LEAlignment {
	
	/**
	 * Atributo com a URI da classe 1.
	 */
	private OntologyResource class1;
	/**
	 * Atributo com a URI da classe 2.
	 */
	private OntologyResource class2;
	/**
	 * Grau de semelhança entre as duas classes.
	 */
	private double measure;
	/**
	 * Tipo de relacionamento entre as classes.
	 */
	private ERelation relation;
	
	
	/**
	 * Construtor de LEAlignment.
	 * @param ontology1 URI da classe 1.
	 * @param ontology2 URI da classe 2.
	 * @param measure Medida de similaridade entre as duas classes
	 * @param relation Tipo do relacionamento.
	 */
	public LEAlignment(OntologyResource ontology1, OntologyResource ontology2, double measure,
			ERelation relation) {
		super();
		this.class1 = ontology1;
		this.class2 = ontology2;
		this.measure = measure;
		this.relation = relation;
	}

	public OntologyResource getClass1() {
		return class1;
	}

	public void setClass1(OntologyResource ontology1) {
		this.class1 = ontology1;
	}

	public OntologyResource getClass2() {
		return class2;
	}

	public void setClass2(OntologyResource ontology2) {
		this.class2 = ontology2;
	}

	public double getMeasure() {
		return measure;
	}

	public void setMeasure(double measure) {
		this.measure = measure;
	}

	public ERelation getRelation() {
		return relation;
	}

	public void setRelation(ERelation relation) {
		this.relation = relation;
	}
	
	public static ArrayList<LEAlignment> invertAlignment(ArrayList<LEAlignment> alignments)
	{	
		ArrayList<LEAlignment> invertedAlignments = null;
		
		if(alignments != null)
		{
			invertedAlignments = new ArrayList<LEAlignment>();
			
			for(LEAlignment align : alignments)
			{
				LEAlignment alignment = new LEAlignment(align.getClass2(), align.getClass1(), align.getMeasure(), align.getRelation());
				invertedAlignments.add(alignment);
			}
		}
		return invertedAlignments;
	}
	
	
	
}
