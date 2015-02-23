package speed.ontologymatcher.util.ontologyhelpers;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.FileInputSource;
import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLPropertyAxiom;
import org.semanticweb.owl.model.OWLPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLPropertyRangeAxiom;

import speed.ontologymatcher.basics.OntologyClass;
import speed.ontologymatcher.basics.OntologyPrimitiveType;
import speed.ontologymatcher.basics.OntologyProperty;
import speed.ontologymatcher.basics.OntologyResource;
import speed.ontologymatcher.semanticmatching.basics.SPEEDProperty;

public class OWLApiOntology extends SPEEDOntology {

	private OWLOntology ontology;
	private OWLOntologyManager manager;
	//TODO: CONTINUAR DAQUI: Mudar pra lista pq precisa do nome da propriedade.
	private ArrayList<SPEEDProperty> properties;	
	private ArrayList<SPEEDProperty> partOfProperties;
	
	public OWLApiOntology(String filePath)
	{
		this.manager = OWLManager.createOWLOntologyManager();		
		try {
			
			System.out.println(filePath);
			File file = new File(filePath);
			OWLOntologyInputSource fileInputSource = new FileInputSource(file);
			this.ontology = this.manager.loadOntology(fileInputSource);

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		this.createProperties();
		System.out.println();
	}
		
	public OntologyResource getOntologyResource(String clazz)
	{		
		OntologyResource resource = null;
						
		if(this.ontology.containsClassReference(URI.create(clazz)))
			resource = new OntologyClass(clazz);
		else if(this.ontology.containsDataPropertyReference(URI.create(clazz))
				|| this.ontology.containsObjectPropertyReference(URI.create(clazz)))
		{
			
			ArrayList<String> domain = this.getDomainFromProperty(clazz);
			String propClazz = null;
			
			// Se a propriedade n�o est� vinculada a uma classe, ela � rejeitada.
			if(domain != null && domain.size() > 0)
			{
				propClazz = domain.get(0);			
				resource = new OntologyProperty(propClazz, clazz);
			}
		}
		else
		{
			resource = new OntologyPrimitiveType(clazz);
		}
		
		return resource;
	}

	public ArrayList<String> getRoots()
	{	
		ArrayList<String> roots = null;
	
		for(OWLClassAxiom axim : this.ontology.getClassAxioms())
		{
			for(OWLEntity entity : axim.getReferencedEntities())
			{
				if(entity.isOWLClass())
				{
					String className = entity.getURI().toASCIIString();
					
					OWLClass clazz = this.manager.getOWLDataFactory().getOWLClass(URI.create(className));
										
					if(clazz.getSuperClasses(this.ontology).size() == 0)
					{
						if(roots == null)
							roots = new ArrayList<String>();
						
						roots.add(className);
					}
				}
			}
		}
		
		return roots;
	}

	public OWLClass getOWLClass(String className)
	{
		return this.manager.getOWLDataFactory().getOWLClass(URI.create(className));
	}

	public ArrayList<SPEEDProperty> getProperties() {
		return properties;
	}

	public ArrayList<SPEEDProperty> getPartOfProperties() {
		return partOfProperties;
	}

	public ArrayList<String> getDomainFromProperty(String propURI)
	{
		ArrayList<String> domain = new ArrayList<String>();
		
		OWLDataProperty datProp = manager.getOWLDataFactory().getOWLDataProperty(URI.create(propURI));
		OWLObjectProperty objProp = manager.getOWLDataFactory().getOWLObjectProperty(URI.create(propURI));
		
		Set<OWLDescription> dom = null;
		
		if(datProp != null)
		{
			Set<OWLDescription> datDomTest = datProp.getDomains(ontology);
			
			if(datDomTest.size() > 0)
				dom = datDomTest;
		}
		
		if (objProp != null)
		{
			Set<OWLDescription> objDomTest = objProp.getDomains(ontology);
			
			if(objDomTest.size() > 0)
				dom = objDomTest;
		}
		
		if(dom != null)
		for(OWLDescription desc : dom)
			domain.add(this.ontology.getURI().toString() + "#" + desc.toString());
		
		return domain;
	}
	
	public SPEEDProperty getPropertyByName(String propName)
	{		
		return SPEEDProperty.findPropertyByName(propName, this.properties);		
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

	@Override
	public void buildHeritance() {
		
		this.manager.getOWLDataFactory().getOWLClass(URI.create("")).isOWLThing();
		
		ArrayList<String> roots = this.getRoots();
		
		for(String root : roots)
		{
			expandAtributes(root, new ArrayList<SPEEDProperty>());			
		}
	}
	
	private void expandAtributes(String clazz, ArrayList<SPEEDProperty> properties)
	{
		OWLClass classObj = this.getOWLClass(clazz);
		ArrayList<SPEEDProperty> clazzProperties = this.getPropertiesFromClass(clazz);
		OWLDataFactory dataFactory = this.manager.getOWLDataFactory();
		
		OWLDataPropertyAssertionAxiom datAssertion = null;
		OWLObjectPropertyAssertionAxiom objAssertion = null;
		AddAxiom addAxiomChange = null;
		
		// Adiciona a classe que vem as propriedades da superclasse
		for(SPEEDProperty prop : properties)
		{	
			String range = prop.getRange() != null && prop.getRange().size() > 0 ? prop.getRange().get(0) : null;
			
			if(range != null)
			{
				OWLIndividual clazzOwl = dataFactory.getOWLIndividual(URI.create(clazz));
				OWLDataProperty datPredOwl = dataFactory.getOWLDataProperty(URI.create(prop.getPropertyName()));
				OWLObjectProperty objPredOwl = dataFactory.getOWLObjectProperty(URI.create(prop.getPropertyName()));
				OWLIndividual rangeOwl = dataFactory.getOWLIndividual(URI.create(range));
				
				if(datPredOwl != null)
				{
					datAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(clazzOwl, datPredOwl, range);
					addAxiomChange = new AddAxiom(this.ontology, datAssertion);
					try {
						this.manager.applyChange(addAxiomChange);
					} catch (OWLOntologyChangeException e) {
						e.printStackTrace();
					}
				}
				else if (this.ontology.containsObjectPropertyReference(URI.create(prop.getPropertyName())))
				{
					objAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(clazzOwl, objPredOwl, rangeOwl);
					addAxiomChange = new AddAxiom(this.ontology, objAssertion);
					try {
						this.manager.applyChange(addAxiomChange);
					} catch (OWLOntologyChangeException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		// Junta a lista as propriedades da classe corrente para serem passadas as subclasses		
		properties.addAll(clazzProperties);
		
		// Chama recursivamente as subclasses
		for(OWLDescription desc : classObj.getSubClasses(this.ontology))
		{
			this.expandAtributes(desc.toString(), properties);
		}
	}

	//TODO: CONTINUAR DAQUI
	@SuppressWarnings({ "unchecked" })
	private void createProperties()
	{
		this.properties = new ArrayList<SPEEDProperty>();
		this.partOfProperties = new ArrayList<SPEEDProperty>();
		
		//OBJECT PROPERTIES
		Iterator<OWLPropertyAxiom> it =  this.ontology.getObjectPropertyAxioms().iterator();
				
		String partOfUri = this.ontology.getURI() + "#" + "PartOf";
						
		while(it.hasNext())
		{
			OWLPropertyAxiom ax = (OWLPropertyAxiom) it.next();
						
			if(ax instanceof OWLPropertyDomainAxiom)
			{
				ArrayList<String> domain = new ArrayList<String>();
				
				boolean isNotPartOf = false;
				OWLPropertyDomainAxiom a = (OWLPropertyDomainAxiom) ax;
				Iterator<OWLEntity> it2 = a.getReferencedEntities().iterator();
				String property = "";
				SPEEDProperty spProp = null;
				while (it2.hasNext())
				{
					OWLEntity entity = (OWLEntity) it2.next();
										
					if(!entity.isOWLObjectProperty())
						domain.add(entity.getURI().toString());
					else if(!entity.getURI().toString().equalsIgnoreCase(partOfUri))
					{
						isNotPartOf = true;
						property = entity.getURI().toString();
					}
					else if(entity.getURI().toString().equalsIgnoreCase(partOfUri))
					{
						isNotPartOf = false;
						property = entity.getURI().toString();
					}
				}
				
				if(isNotPartOf)
				{	
					spProp = this.getPropertyByName(property);
					
					if(spProp == null)
					{
						spProp = new SPEEDProperty();
						spProp.setPropertyName(property);
						spProp.getDomain().addAll(domain);
						this.properties.add(spProp);
					}
					else
					{
						spProp.getDomain().addAll(domain);
					}
				}
				else
				{
					spProp = SPEEDProperty.findPropertyByName(property, this.partOfProperties);
					
					if(spProp == null)
					{
						spProp = new SPEEDProperty();
						spProp.setPropertyName(property);
						spProp.getDomain().addAll(domain);
						this.partOfProperties.add(spProp);
					}
					else
					{
						spProp.getDomain().addAll(domain);
					}				
				}
			}
			else if(ax instanceof OWLPropertyRangeAxiom)
			{	
				ArrayList<String> range = new ArrayList<String>();
				
				boolean isNotPartOf = false;
				OWLPropertyRangeAxiom a = (OWLPropertyRangeAxiom) ax;
				Iterator<OWLEntity> it2 = a.getReferencedEntities().iterator();
				String property = "";
				SPEEDProperty spProp = null;
				
				while (it2.hasNext())
				{
					OWLEntity entity = (OWLEntity) it2.next();
					
					if(!entity.isOWLObjectProperty() || entity.isOWLDataType())
						range.add(entity.getURI().toString());
					else if(!entity.getURI().toString().equalsIgnoreCase(partOfUri))
					{
						isNotPartOf = true;
						property = entity.getURI().toString();
					}	
					else if(entity.getURI().toString().equalsIgnoreCase(partOfUri))
					{
						isNotPartOf = false;
						property = entity.getURI().toString();
					}
				}
				
				if(isNotPartOf)
				{
					spProp = this.getPropertyByName(property);
					
					if(spProp == null)
					{
						spProp = new SPEEDProperty();
						spProp.setPropertyName(property);
						spProp.getRange().addAll(range);
						this.properties.add(spProp);
					}
					else
					{
						spProp.getRange().addAll(range);
					}
				}
				else
				{
					spProp = SPEEDProperty.findPropertyByName(property, this.partOfProperties);
					
					if(spProp == null)
					{
						spProp = new SPEEDProperty();
						spProp.setPropertyName(property);
						spProp.getRange().addAll(range);
						this.partOfProperties.add(spProp);
					}
					else
					{
						spProp.getRange().addAll(range);
					}
					
				}
			}
		}
		
		//DATATYPE PROPERTIES
		it =  ontology.getDataPropertyAxioms().iterator();		
		while(it.hasNext())
		{
			OWLPropertyAxiom ax = (OWLPropertyAxiom) it.next();			
			
			
			if(ax instanceof OWLPropertyDomainAxiom)
			{
				ArrayList<String> domain = new ArrayList<String>();
				
				boolean isNotPartOf = false;
				OWLPropertyDomainAxiom a = (OWLPropertyDomainAxiom) ax;
				Iterator<OWLEntity> it2 = a.getReferencedEntities().iterator();
				String property = "";
				SPEEDProperty spProp = null;
				while (it2.hasNext())
				{	
					OWLEntity entity = (OWLEntity) it2.next();
					
					if(!entity.isOWLDataProperty())
						domain.add(entity.getURI().toString());
					else if(!entity.getURI().toString().equalsIgnoreCase(partOfUri))
					{
						isNotPartOf = true;
						property = entity.getURI().toString();
					}
				}				
				
				spProp = this.getPropertyByName(property);
				
				if(isNotPartOf)
				if(spProp == null)
				{
					spProp = new SPEEDProperty();
					spProp.setPropertyName(property);
					spProp.getDomain().addAll(domain);
					this.properties.add(spProp);
				}
				else
				{
					spProp.getDomain().addAll(domain);
				}
			}
			else if(ax instanceof OWLPropertyRangeAxiom)
			{	
				ArrayList<String> range = new ArrayList<String>();
				
				boolean isNotPartOf = false;
				OWLPropertyRangeAxiom a = (OWLPropertyRangeAxiom) ax;
				Iterator<OWLEntity> it2 = a.getReferencedEntities().iterator();
				String property = "";
				SPEEDProperty spProp = null;
				
				while (it2.hasNext())
				{
					OWLEntity entity = (OWLEntity) it2.next();
					
					if(!entity.isOWLDataProperty()  || entity.isOWLDataType())
						range.add(entity.getURI().toString());
					else if(!entity.getURI().toString().equalsIgnoreCase(partOfUri))
					{
						isNotPartOf = true;
						property = entity.getURI().toString();
					}	
				}
				
				spProp = this.getPropertyByName(property);
				
				if(isNotPartOf)
				if(spProp == null)
				{
					spProp = new SPEEDProperty();
					spProp.setPropertyName(property);
					spProp.getRange().addAll(range);
					this.properties.add(spProp);
				}
				else
				{
					spProp.getRange().addAll(range);
				}
			}
		}
	}
	
}
