package speed.ontologymatcher.util.ontologyhelpers;

import java.util.ArrayList;
import java.util.Iterator;

import speed.ontologymatcher.basics.OntologyClass;
import speed.ontologymatcher.basics.OntologyProperty;
import speed.ontologymatcher.basics.OntologyResource;
import speed.ontologymatcher.semanticmatching.basics.SPEEDProperty;
import speed.ontologymatcher.util.OntologyMatcherProperties;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.ProfileRegistry;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class JenaOntology extends SPEEDOntology {

	private Model model;
	private OntModel ontModel;
	private ArrayList<SPEEDProperty> properties;	
	private ArrayList<SPEEDProperty> partOfProperties;
	
	public JenaOntology()
	{
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_TRANS_INF);
		this.model = ModelFactory
		.createOntologyModel(ProfileRegistry.RDFS_LANG);
		this.ontModel = ModelFactory.createOntologyModel(spec, this.model);
	}

	public JenaOntology(String filePath)
	{
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_TRANS_INF);
		this.model = createOntologyFromDirectory(filePath);
		this.ontModel = ModelFactory.createOntologyModel(spec, this.model);
		
		StmtIterator subIt = this.model.listStatements(null, RDFS.subClassOf,
				(RDFNode) null);

		while (subIt.hasNext()) {
			Statement subStmt = (Statement) subIt.next();

			if (subStmt.getObject() != null && subStmt.getSubject() != null) {
				String subjectURI = subStmt.getObject().toString();
				String predicateURI = OntologyMatcherProperties.SPEED_URI + "superClassOf";
				String objectURI = subStmt.getSubject().getURI();
				
				Resource subject = this.model.createResource(subjectURI);
				Property predicate = this.model.createProperty(predicateURI);				
				Resource object = this.model.createResource(objectURI);
		
				if (!this.model.contains(subject, predicate, object))
					this.model.add(subject, predicate, object);
			}
		}
		
		StmtIterator subIt2 = this.model.listStatements(null, RDFS.subPropertyOf,
				(RDFNode) null);
		
		while (subIt2.hasNext()) {
			Statement subStmt = (Statement) subIt2.next();

			if (subStmt.getObject() != null && subStmt.getSubject() != null) {
				Resource subject = this.model.createResource(subStmt
						.getObject().toString());
				Property predicate = this.model
						.createProperty(OntologyMatcherProperties.SPEED_URI
								+ "superPropertyOf");
				Resource object = this.model.createResource(subStmt
						.getSubject().getURI());

				if (!this.model.contains(subject, predicate, object))
					this.model.add(subject, predicate, object);
			}
		}
		
		//TODO: Tentar tirar esse c�digo.
		OWLApiOntology owlApi = new OWLApiOntology(filePath);
		this.properties = owlApi.getProperties();
		this.partOfProperties = owlApi.getPartOfProperties();
	}
	
	public ArrayList<SPEEDProperty> getPartOfProperties() {
		return partOfProperties;
	}

	public void setPartOfProperties(ArrayList<SPEEDProperty> partOfProperties) {
		this.partOfProperties = partOfProperties;
	}

	public Model getModel() {
		return model;
	}

	public OntModel getOntModel() {
		return ontModel;
	}

	public String getURI()
	{
		return this.ontModel.getNsPrefixURI("");
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getRoots()
	{
		ArrayList<String> roots = new ArrayList<String>();
		
		for (Iterator i = this.ontModel.listClasses(); i.hasNext();) {
			OntClass c = (OntClass) i.next();
			if (c.getURI() != null && this.isRoot(c.getURI()) && this.ontModel.getOntClass(c.getURI()) != null)
				roots.add(c.getURI());
		}
		
		return roots;		
	}

	public boolean isRoot(String className)
	{
		boolean isRoot = true;
		
		OntClass clazz = this.ontModel.getOntClass(className);
		
		ExtendedIterator it = clazz.listSuperClasses(true);
		
		if(it.hasNext())
		{
			isRoot = false;		
		}
		
		return isRoot;
	}

	public ArrayList<SPEEDProperty> getPropertiesFromClass(String className)
	{
		ArrayList<SPEEDProperty> properties = new ArrayList<SPEEDProperty>();
		
		for(SPEEDProperty prop : this.properties)
		{	
			if(prop.getDomain().contains(className))
				properties.add(prop);
		}
		
		return properties;
	}
	
	public void addStatementToModel(OntologyResource subject, String predicate, OntologyResource object)
	{
		//Parte que adiciona no modelo
		Resource resourceSubject = null;
		Property propertyPredicate = this.model.createProperty(predicate);
		String objectStr = null;
		if(subject instanceof OntologyClass && object instanceof OntologyClass)
		{
			OntologyClass subOntClass = (OntologyClass)subject;
			resourceSubject = this.model.createResource(subOntClass.getClassName());
			objectStr = ((OntologyClass)object).getClassName();
		}
		else if(subject instanceof OntologyProperty && object instanceof OntologyProperty)
		{
			OntologyProperty subOntClass = (OntologyProperty)subject;
			resourceSubject = this.model.createResource(subOntClass.getProperty());
			objectStr = ((OntologyProperty)object).getProperty();
		}
		
		this.model.add(resourceSubject, propertyPredicate,objectStr);
		
	}
	
	/**
	 * Método que calcula o número de conceitos em uma ontologia.
	 * @param path Caminho do arquivo que contém a ontologia.
	 * @return Número de conceitos.
	 */
	public static int getNumberOfConcepts(String path)
	{
		int numConcepts = 0;
		
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_TRANS_INF);
		Model schema = FileManager.get().loadModel("file:/" + path);
		OntModel model = ModelFactory.createOntologyModel(spec, schema);
		
		ExtendedIterator it = model.listClasses();
		String URI = model.getNsPrefixURI("");
		
		while (it.hasNext())
		{
			String classURI = it.next().toString();
			if(classURI.contains(URI))
				numConcepts++;
		}		
		
		return numConcepts;
	}
	
	/**
	 * Método que retorna as classes em uma ontologia.
	 * @param path Caminho do arquivo que contém a ontologia.
	 * @return Lista de classes.
	 */
	public static ArrayList<String> getConcepts(String path)
	{
		ArrayList<String> classes = new ArrayList<String>();
		
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM_TRANS_INF);
		Model schema = JenaOntology.createOntologyFromDirectory(path);//FileManager.get().loadModel("file:/" + path);
		OntModel model = ModelFactory.createOntologyModel(spec, schema);
		
		ExtendedIterator it = model.listClasses();
		String URI = model.getNsPrefixURI("");
		
		while (it.hasNext())
		{
			String classURI = it.next().toString();
			if(classURI.contains(URI))
				classes.add(classURI);
		}
		
		return classes; 
	}
	
	public ArrayList<OntClass> getSubClassesFrom(String clazz)
	{
		ArrayList<OntClass> subClasses = new ArrayList<OntClass>();
		
		OntClass ontClass1 = this.ontModel.getOntClass(clazz);
		
		ExtendedIterator it = ontClass1.listSuperClasses(true);
		
		while(it.hasNext())
		{	
			if(subClasses == null)
				subClasses = new ArrayList<OntClass>();
			
			subClasses.add((OntClass) it.next());
		}
		
		return subClasses;		
	}

	@Override
	public void buildHeritance() {
		ArrayList<String> roots = this.getRoots();
		
		for(String root : roots)
		{
			this.expandAtributes(root, new ArrayList<SPEEDProperty>());			
		}
	}
	
	private void expandAtributes(String clazz, ArrayList<SPEEDProperty> properties)
	{
		ArrayList<SPEEDProperty> clazzProperties = this.getPropertiesFromClass(clazz);
				
		// Adiciona a classe que vem as propriedades da superclasse
		for(SPEEDProperty prop : properties)
		{	
			String range = prop.getRange() != null && prop.getRange().size() > 0 ? prop.getRange().get(0) : null;
			
			if(range != null)
			{
				Resource clazzOwl = this.model.getResource(clazz);
				
				this.model.add(clazzOwl, this.model.getProperty(prop.getPropertyName()), range);
			}
		}
		
		// Junta a lista as propriedades da classe corrente para serem passadas as subclasses		
		properties.addAll(clazzProperties);
		
		// Chama recursivamente as subclasses
		for(OntClass desc : this.getSubClassesFrom(clazz))
		{
			this.expandAtributes(desc.getURI(), properties);
		}		
	}

	private static Model createOntologyFromDirectory(String path)
	{
		return FileManager.get().loadModel("file:/" + path);
	}		
	
}
 