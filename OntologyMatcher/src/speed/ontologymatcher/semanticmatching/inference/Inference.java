package speed.ontologymatcher.semanticmatching.inference;

import java.util.ArrayList;

import speed.ontologymatcher.basics.OntologyClass;
import speed.ontologymatcher.basics.OntologyProperty;
import speed.ontologymatcher.basics.OntologyResource;
import speed.ontologymatcher.lematching.basics.LEAlignment;
import speed.ontologymatcher.semanticmatching.basics.Alignment;
import speed.ontologymatcher.semanticmatching.basics.SPEEDProperty;
import speed.ontologymatcher.util.OntologyMatcherProperties;
import speed.ontologymatcher.util.ontologyhelpers.JenaOntology;
import speed.ontologymatcher.util.ontologyhelpers.OWLApiOntology;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Classe que encapsula operações relativas a inferências geradas pelo Jena.
 * 
 * @author Thiago Pachêco Andrade Pereira.
 * 
 */
public class Inference {

	private ArrayList<LEAlignment> matchingsCLOi;
	private ArrayList<LEAlignment> matchingsCLOj;
	
	private JenaOntology jenaCMOModel;
	private OWLApiOntology owlApiModel;
	
	//private ArrayList<Alignment> matchingStatements;
	
	private ArrayList<SPEEDProperty> partOfProperties;

	/**
	 * Contrutor da classe Inference.
	 * 
	 * @param matchingsCLOi
	 * @param matchingsCLOj
	 * @param ontologyPathCMO
	 * @param minMeasure
	 */
	public Inference(ArrayList<LEAlignment> matchingsCLOi,
			ArrayList<LEAlignment> matchingsCLOj, String ontologyPathCMO,
			double minMeasure) {
		
		//this.matchingStatements = new ArrayList<Alignment>();
				
		this.jenaCMOModel = new JenaOntology(ontologyPathCMO);
		
		this.owlApiModel = new OWLApiOntology(ontologyPathCMO);
						
		this.matchingsCLOi = matchingsCLOi;
		this.matchingsCLOj = matchingsCLOj;

		//this.roots = this.jenaCMOModel.getRoots();
		
		this.partOfProperties = this.owlApiModel.getPartOfProperties();
	}

	/**
	 * Método auxliar que gera o modelo inferido com as regras do Jena.
	 * 
	 * @param ontModel Modelo onde vai ser aplicado as regras.
	 * @return Modelo com as inferências obtidas a partir das regras.
	 */
	public ArrayList<Alignment> executeRules() {
		PrintUtil.registerPrefix("speed", OntologyMatcherProperties.SPEED_URI);
		PrintUtil.registerPrefix("CMO", this.jenaCMOModel.getURI());
	
		ArrayList<Alignment> matchingStatements = new ArrayList<Alignment>();
		
		for (LEAlignment itemCLOi : this.matchingsCLOi) {
			for (LEAlignment itemCLOj : this.matchingsCLOj) {
				
				if (itemCLOj != null) {
					
					if (itemCLOi.getMeasure() == 1.0
							&& itemCLOj.getMeasure() == 1.0) {
						//Equivalent
						if(this.isEquivalentTo(itemCLOi.getClass2(),
								itemCLOj.getClass2()))
						{							
							Alignment align = new Alignment(itemCLOi.getClass1(),
									OntologyMatcherProperties.SPEED_URI + "isEquivalentTo",
									itemCLOj.getClass1());
							matchingStatements.add(align);
						}
						
						//SubConcept
						if(this.isSubConceptOf(itemCLOi.getClass2(),
								itemCLOj.getClass2()))
						{
							Alignment align = new Alignment(itemCLOi.getClass1(),
									OntologyMatcherProperties.SPEED_URI + "isSubConceptOf",
									itemCLOj.getClass1());
							
							matchingStatements.add(align);
						}
						
						//SuperConcept
						if(this.isSuperConceptOf(itemCLOi.getClass2(),
								itemCLOj.getClass2()))
						{		
							Alignment align = new Alignment(itemCLOi.getClass1(),
									OntologyMatcherProperties.SPEED_URI + "isSuperConceptOf",
									itemCLOj.getClass1());
							
							matchingStatements.add(align);
						}
						
						//IsPartOf
						if(this.isPartOf(itemCLOi.getClass2(),
								itemCLOj.getClass2()))
						{
							Alignment align = new Alignment(itemCLOi.getClass1(),
									OntologyMatcherProperties.SPEED_URI + "isPartOf",
									itemCLOj.getClass1());
							
							matchingStatements.add(align);
						}
						
						//IsWholeOf
						if(this.isPartOf(itemCLOj.getClass2(),
								itemCLOi.getClass2()))
						{
							Alignment align = new Alignment(itemCLOi.getClass1(),
									OntologyMatcherProperties.SPEED_URI + "isWholeOf",
									itemCLOj.getClass1());
							
							matchingStatements.add(align);
						}
	
						if (this.isDisjointWith(itemCLOi.getClass2(),
								itemCLOj.getClass2())) {
							
							Alignment align = new Alignment(itemCLOi.getClass1(),
									OntologyMatcherProperties.SPEED_URI + "isDisjointWith",
									itemCLOj.getClass1());
							
							matchingStatements.add(align);
						}
	
						if (this.isCloseTo(itemCLOi.getClass2(), itemCLOj
								.getClass2())) {
							
							Alignment align = new Alignment(itemCLOi.getClass1(),
									OntologyMatcherProperties.SPEED_URI + "isCloseTo",
									itemCLOj.getClass1());
							
							matchingStatements.add(align);
						}
					}
				}
			}
		}
		return matchingStatements;
	}

	private boolean isEquivalentTo(OntologyResource classCMO1, OntologyResource classCMO2) {
		
		boolean isEquivalent = false;
		
		if(this.isOntClasses(classCMO1, classCMO2))
		{
			OntologyClass ontClass1 = (OntologyClass) classCMO1;
			OntologyClass ontClass2 = (OntologyClass) classCMO2;
			
			isEquivalent = ontClass1.getClassName().equals(ontClass2.getClassName());
			
		}
		else if(this.isOntProperty(classCMO1, classCMO2))
		{
			OntologyProperty ontClass1 = (OntologyProperty) classCMO1;
			OntologyProperty ontClass2 = (OntologyProperty) classCMO2;
			
			isEquivalent = ontClass1.getClassName().equals(ontClass2.getClassName())
							&& ontClass1.getProperty().equals(ontClass2.getProperty());
		}
		
		return isEquivalent;
	}

	private boolean isSubConceptOf(OntologyResource classCMO1, OntologyResource classCMO2) {
		boolean isSubConceptOf = false;
		
		if(this.isOntClasses(classCMO1, classCMO2))
		{
			OntologyClass class1Ont = (OntologyClass) classCMO1;
			
			OntClass ontClass1 = this.jenaCMOModel.getOntModel().getOntClass(class1Ont.getClassName());
			
			ExtendedIterator it = ontClass1.listSuperClasses(true);
			
			while(it.hasNext())
			{	
				if(classCMO2.toString().equals(it.next().toString()))
					isSubConceptOf = true;
			}
		}
		else if (this.isOntProperty(classCMO1, classCMO2))
		{
			OntologyProperty prop1Ont = (OntologyProperty) classCMO1;
			OntologyProperty prop2Ont = (OntologyProperty) classCMO2;
			
			OntProperty ontProp = this.jenaCMOModel.getOntModel().getOntProperty(prop1Ont.getProperty());
			
			ExtendedIterator it = ontProp.listSuperProperties(true);
			
			while(it.hasNext())
			{	
				if(classCMO2.toString().equals(it.next().toString())
						&& prop2Ont.getClassName().equals(prop1Ont.getClassName()))
					isSubConceptOf = true;
			}			
		}
				
		return isSubConceptOf;
	}

	private boolean isSuperConceptOf(OntologyResource class1, OntologyResource class2) {
		boolean isSuperConceptOf = false;
		
		if(this.isOntClasses(class1, class2))
		{
			OntologyClass class1Ont = (OntologyClass) class1;
			
			OntClass ontClass1 = this.jenaCMOModel.getOntModel().getOntClass(class1Ont.getClassName());
		
			Property superClassOf = this.jenaCMOModel.getModel().createProperty(OntologyMatcherProperties.SPEED_URI + "superClassOf");
			
			StmtIterator it = this.jenaCMOModel.getModel().listStatements(ontClass1,
					superClassOf, (RDFNode) null);
			
			
			while (it.hasNext()) {
				Statement stmt = it.nextStatement();
		
				if(class2.toString().equals(stmt.getObject().toString()))						
					isSuperConceptOf = true;
			}
		}
		else if(this.isOntProperty(class1, class2))
		{
			OntologyProperty prop1Ont = (OntologyProperty) class1;
			OntologyProperty prop2Ont = (OntologyProperty) class2;
			
			OntProperty ontProp1 = this.jenaCMOModel.getOntModel().getOntProperty(prop1Ont.getProperty());
			Property superPropOf = this.jenaCMOModel.getModel().createProperty(OntologyMatcherProperties.SPEED_URI + "superPropertyOf");
			
			StmtIterator it = this.jenaCMOModel.getModel().listStatements(ontProp1,
					superPropOf, (RDFNode) null);
			
			while (it.hasNext()) {
				Statement stmt = it.nextStatement();
		
				if(prop2Ont.getProperty().equals(stmt.getObject().toString())
						&& prop2Ont.getClassName().equals(prop1Ont.getClassName()))
					isSuperConceptOf = true;
			}	
		}
		
		return isSuperConceptOf;
	}

	private boolean isPartOf(OntologyResource class1, OntologyResource class2)
	{
		boolean isPartOf = false;
		
		if(this.isOntClasses(class1, class2))
		{
			for(SPEEDProperty prop : this.partOfProperties)
			{
				if(prop.getDomain().contains(class1.toString()) && prop.getRange().contains(class2.toString()))
					isPartOf = true;
			}
		}
		else if (this.isOntProperty(class1, class2))
		{
						
		}
		
		return isPartOf;
	}

	/**
	 * Método auxiliar que verifica a regra isCloseTo.
	 * 
	 * @param class1 Classe 1.
	 * @param class2 Classe 2.
	 * @return True - Se é isCloseTo.
	 */
	private boolean isCloseTo(OntologyResource classCMO1, OntologyResource classCMO2) {
	
		boolean isCloseTo = false;
		
		if(this.isOntClasses(classCMO1, classCMO2))
		{
			String class1 = classCMO1.toString();
			String class2 = classCMO2.toString();
			
			OntClass ontClass1 = this.jenaCMOModel.getOntModel().getOntClass(class1);
			OntClass ontClass2 = this.jenaCMOModel.getOntModel().getOntClass(class2);
		
			Property subClassOf = this.jenaCMOModel.getModel().createProperty(RDFS.getURI()
					+ "subClassOf");
		
			String classA = null;
			
			for (int i = 1; i <= OntologyMatcherProperties.THRESHOLD; i++) {
		
				// SUBIR NA ÁRVORE
		
				ArrayList<String> superClasses = new ArrayList<String>();
				superClasses.add(class1);
		
				ArrayList<String> superClassesTmp = new ArrayList<String>();
		
				ArrayList<String> lastClassesVisited = new ArrayList<String>();
				lastClassesVisited.add(class1);
		
				for (int j = 0; j < i; j++) {
		
					for (String clazz : superClasses) {
						Resource rscrClassTmp = this.jenaCMOModel.getModel().createResource(clazz);
		
						StmtIterator it = this.jenaCMOModel.getModel().listStatements(rscrClassTmp,
								subClassOf, (RDFNode) null);
		
						lastClassesVisited.add(clazz);
		
						while (it.hasNext()) {
							Statement stmt = it.nextStatement();
		
							superClassesTmp.add(stmt.getObject().toString());
						}
					}
					superClasses.clear();
		
					superClasses.addAll(superClassesTmp);
		
					superClassesTmp.clear();
				}
				
				if (superClasses.size() == 0)
					break;
				else
					classA = superClasses.get(0);
		
				// Desce na árvore
		
				ArrayList<String> subClasses = new ArrayList<String>();
				for (String superrClass : superClasses) {
					Resource rscrClassTmp = this.jenaCMOModel.getModel().createResource(superrClass);
		
					StmtIterator it = this.jenaCMOModel.getModel().listStatements(null, subClassOf,
							rscrClassTmp);
		
					while (it.hasNext()) {
						Statement stmt = it.nextStatement();
						String clazz = stmt.getSubject().getURI();
						if (!lastClassesVisited.contains(clazz))
							subClasses.add(clazz);
					}
				}
		
				if (subClasses.contains(class2))
					isCloseTo = true;
		
				ArrayList<String> subClassesTmp = new ArrayList<String>();
		
				for (int j = 1; j < OntologyMatcherProperties.THRESHOLD; j++) { // "<" pq ja desceu um nível antes.
		
					for (String clazz : subClasses) {
						Resource rscrClassTmp = this.jenaCMOModel.getModel().createResource(clazz);
		
						StmtIterator it = this.jenaCMOModel.getModel().listStatements(null,
								subClassOf, rscrClassTmp);
		
						while (it.hasNext()) {
							Statement stmt = it.nextStatement();
		
							subClassesTmp.add(stmt.getSubject().getURI());
						}
					}
					subClasses.addAll(subClassesTmp);
		
					if (subClasses.contains(class2))
						isCloseTo = true;
		
					subClassesTmp.clear();
				}
			}
			
			if(isCloseTo && classA != null)
			{
				Resource rscrClassTmp = this.jenaCMOModel.getModel().createResource(classA);
				String currentClass = classA;
				int classARootHeight = 1;
				
				boolean getRoot = false;
				boolean isValid = true;
				while(!getRoot)
				{
					StmtIterator it = this.jenaCMOModel.getModel().listStatements(rscrClassTmp,
							subClassOf, (RDFNode) null);
					
					boolean hasSuperClass = false;
					
					while (it.hasNext()) {
						
						hasSuperClass = true;
						Statement stmt = it.nextStatement();
						currentClass = stmt.getObject().toString();
						OntClass clazzTmp = this.jenaCMOModel.getOntModel().getOntClass(currentClass);
						rscrClassTmp = clazzTmp;
						if(clazzTmp != null && clazzTmp.getURI() != null && this.jenaCMOModel.isRoot(clazzTmp.getURI()))//clazzTmp.isHierarchyRoot()
							getRoot = true;					
						
						if(clazzTmp == null)
							isValid = false;
					}
					
					if(!isValid || !hasSuperClass)
						break;
					
					
					classARootHeight++;
				}
				
				isCloseTo = classARootHeight >= OntologyMatcherProperties.THRESHOLD_ROOT
				&& !ontClass1.isDisjointWith(ontClass2) && isValid;
				
			}
			
			boolean isEqual = class1.equals(class2);
	
			isCloseTo = isCloseTo && !isEqual;		
		}
		else if(this.isOntProperty(classCMO1, classCMO2))
		{
			
		}
		
		return isCloseTo;
	}

	/**
	 * Método auxiliar que verifica a regra isDisjointWith.
	 * 
	 * @param class1 Classe 1.
	 * @param class2 Classe 2.
	 * @return True - Se é isDisjointWith.
	 */
	private boolean isDisjointWith(OntologyResource class1, OntologyResource class2) {

		boolean isDisjoint = false;
		
		if(this.isOntClasses(class1, class2))
		{
			OntClass ontClass1 = this.jenaCMOModel.getOntModel().getOntClass(class1.toString());
			OntClass ontClass2 = this.jenaCMOModel.getOntModel().getOntClass(class2.toString());
			isDisjoint = ontClass1.isDisjointWith(ontClass2);
		}
		else if(this.isOntProperty(class1, class2))
		{
			
		}
		
		
		return isDisjoint;		
	}		
	
	private boolean isOntClasses(OntologyResource class1, OntologyResource class2)
	{	
		return class1 instanceof OntologyClass && class2 instanceof OntologyClass; 		
	}
	
	private boolean isOntProperty(OntologyResource class1, OntologyResource class2)
	{	
		return class1 instanceof OntologyProperty && class2 instanceof OntologyProperty; 		
	}
	
	
	
}
