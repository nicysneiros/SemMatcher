package speed.ontologymatcher.lematching;

import islab.hmatch.CommandLineHMatch;
import islab.hmatch.DeepMatching;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import speed.ontologymatcher.basics.OntologyResource;
import speed.ontologymatcher.lematching.basics.LEAlignment;
import speed.ontologymatcher.lematching.enums.EMappingCardinality;
import speed.ontologymatcher.lematching.enums.EMatcher;
import speed.ontologymatcher.lematching.enums.EMatchingType;
import speed.ontologymatcher.lematching.enums.ERelation;
import speed.ontologymatcher.util.FileHandler;
import speed.ontologymatcher.util.OntologyMatcherProperties;
import speed.ontologymatcher.util.XMLParser;
import speed.ontologymatcher.util.ontologyhelpers.JenaOntology;
import speed.ontologymatcher.util.ontologyhelpers.OWLApiOntology;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import common.Ontology;
import common.PropertyFinder;

import coordination.HMatchController;
import coordination.History;

/**
 * Classe com métodos para gerenciar o uso de matcher linguístico-estruturais.
 * Pode fazer uso dos seguintes matcher:
 * 	- Hmatch 2.0 cmd
 *  - Alignment API
 *  - HMatchCL 2.0
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public class LEMatcherManager {

	private HMatchController controller;

	private History operationsHistory;

	private ArrayList<Integer> mappingIDs;

	/**
	 * Construtor default.
	 */
	public LEMatcherManager() {
		PropertyFinder pFinder = PropertyFinder.getInstance();
		PropertyConfigurator.configure(pFinder.getLogConfigurationFile());
		this.controller = HMatchController.getInstance();
		this.operationsHistory = new History();
		this.mappingIDs = new ArrayList<Integer>();
	}

	/**
	 * Método que executa o matching linguistico-estrutural com uso de um matcher externo.
	 * @param fileOntology1 Caminho da ontologia de entrada 1.
	 * @param fileOntology2 Caminho da ontologia de entrada 2.
	 * @param fileOut Caminho do arquivo de saída.
	 * @param mappingType Tipo do alinhamento (1:1, 1:N, N:M).
	 * @param matcher Tipo de matching a ser realizado. 
	 * @return Retorna uma lista de alinhamentos.
	 */
	public ArrayList<LEAlignment> executeLEMAtching(String fileOntology1,
			String fileOntology2, String fileOut, EMappingCardinality mappingType,
			EMatchingType matcher) {
		ArrayList<LEAlignment> matchings = new ArrayList<LEAlignment>();
		
		OWLApiOntology clo1Ontology = new OWLApiOntology(fileOntology1);
		OWLApiOntology clo2Ontology = new OWLApiOntology(fileOntology2);
		
		if (OntologyMatcherProperties.MATCHER == EMatcher.HMATCH) {

			Ontology ontology1 = Ontology
					.createOntology(new File(fileOntology1));
			Ontology ontology2 = Ontology
					.createOntology(new File(fileOntology2));

			if (ontology1 != null && ontology2 != null) {

				mappingIDs.add(Integer.valueOf(controller
						.executeLinguisticMatching(ontology1, ontology2)));

				operationsHistory.recordMatchingOperation(matcher.toString(),
						fileOntology1, fileOntology2, new Vector<Integer>(
								mappingIDs));

				Integer id = mappingIDs.get(mappingIDs.size() - 1);

				FileHandler.createMatchingFile(fileOut, mappingType, id);
				
				matchings = this.readFileAlignments(fileOut, clo1Ontology, clo2Ontology);

				operationsHistory.recordMappingSave(id.intValue(), fileOut,
				mappingType.toString());

				// Analise estrutural

				int idNew = controller.executeStructuralMatching(ontology1,
						ontology2, id);

				mappingIDs.add(Integer.valueOf(idNew));

				id = mappingIDs.get(mappingIDs.size() - 1);

				FileHandler.createMatchingFile(fileOut, mappingType, id);

				matchings = this.readFileAlignments(fileOut, clo1Ontology, clo2Ontology);
								
				operationsHistory.recordMappingSave(id.intValue(), fileOut,
						mappingType.toString());

			} else {

				System.out.println("ERROR: File not found.");

			}
		} else if (OntologyMatcherProperties.MATCHER == EMatcher.ALIGNMENT_API) {
			try {
				fileOntology1 = fileOntology1.replace('\\', '/');
				fileOntology2 = fileOntology2.replace('\\', '/');
				fileOut = fileOut.replace('\\', '/');
				
				String ontology1 = "file:///" + fileOntology1;
				String ontology2 = "file:///" + fileOntology2;
				String output =  fileOut;
				
				Process proc = Runtime
						.getRuntime()
						.exec(
								"java -jar "+ OntologyMatcherProperties.ALIGNMENTAPI_PATH + "procalign.jar " + ontology1 + " " + ontology2 + " -o " + output + " -t 0.0" );
				
				/*InputStream inputStream = proc.getInputStream();
				ByteArrayOutputStream bais = new ByteArrayOutputStream();
				int lido = 0;
				while ((lido = inputStream.read()) > 0) {
					bais.write(lido);
				}
				System.out.println(new String(bais.toByteArray()));*/
				
				boolean finished = false;
				
				while(!finished)
				{
					try {
						proc.exitValue();
						finished = true;
					} catch (IllegalThreadStateException e) {}
				}
				
				matchings = this.readFileAlignments(fileOut, clo1Ontology, clo2Ontology);
								
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
		}
		else if(OntologyMatcherProperties.MATCHER == EMatcher.HMATCHCL)
		{	
			fileOntology1 = fileOntology1.replace('\\', '/');
			fileOntology2 = fileOntology2.replace('\\', '/');
			fileOut = fileOut.replace('\\', '/');
			
			String mappingTypeFile = "policy11.xml";
			
			switch (mappingType) {
			case ONE_TO_ONE:
				mappingTypeFile = "policy11.xml";
				break;
			case ONE_TO_MANY:
				mappingTypeFile = "policy1N.xml";
				break;
			default:
				break;
			}
			
			String[] arguments = new String[]{ 
					mappingTypeFile, 
					fileOntology1 , 
					fileOntology2, 
					"true", 
					"true", 
					fileOut };
			
			System.out.println("COME�ANDO A EXECUTAR HMATCH");
			
			CommandLineHMatch.main(arguments);
			DeepMatching.revalidateThesaurus();
			
			matchings = this.readFileAlignments(fileOut, clo1Ontology, clo2Ontology);
						
			this.cleanFiles();
			
		}
		else if (OntologyMatcherProperties.MATCHER == EMatcher.NONE)
		{
			matchings = this.readFileAlignments(fileOut, clo1Ontology, clo2Ontology);
						
		}

		return matchings;
	}
	

	public ArrayList<LEAlignment> executeLEMAtchingNormalized(String fileOntology1,
			String fileOntology2)
	{	
		ArrayList<LEAlignment> alignments = new ArrayList<LEAlignment>();
		
		JenaOntology jenaOntologyCLO1 = new JenaOntology(fileOntology1);
		jenaOntologyCLO1.buildHeritance();
		JenaOntology jenaOntologyCLO2 = new JenaOntology(fileOntology2);
		jenaOntologyCLO2.buildHeritance();
		
		OWLApiOntology owlOntologyCLO1 = new OWLApiOntology(fileOntology1);
		owlOntologyCLO1.buildHeritance();
		OWLApiOntology owlOntologyCLO2 = new OWLApiOntology(fileOntology2);
		owlOntologyCLO2.buildHeritance();
		
		//Classes
		ExtendedIterator it = jenaOntologyCLO1.getOntModel().listClasses();
		
		while(it.hasNext())
		{	
			String clazz = String.valueOf(it.next());
			String[] uri_class = clazz.split("#");
			
			if(uri_class.length == 2 && uri_class[1] != null && !uri_class[1].isEmpty())
			{
				OntClass clazzObj = jenaOntologyCLO2.getOntModel().getOntClass(jenaOntologyCLO2.getURI() + uri_class[1]);
				
				if(clazzObj != null)
				{
					String clazz1 = clazz;
					String clazz2 = clazzObj.getURI();
					
					OntologyResource resource1 = owlOntologyCLO1.getOntologyResource(clazz1);
					OntologyResource resource2 = owlOntologyCLO2.getOntologyResource(clazz2);					
					
					if(resource1 != null && resource2 != null)
					{
						LEAlignment align = new LEAlignment(resource1, resource2, 1.0, ERelation.EQUIVALENT);
						alignments.add(align);
					}
				}
			}
		}
		
		//Datatype properties
		ExtendedIterator it2 = jenaOntologyCLO1.getOntModel().listDatatypeProperties();
		
		while(it2.hasNext())
		{	
			String property = String.valueOf(it2.next());
			String[] uri_property = property.split("#");
			
			if(uri_property.length == 2 && uri_property[1] != null && !uri_property[1].isEmpty())
			{
				DatatypeProperty propertyCMO = jenaOntologyCLO2.getOntModel().getDatatypeProperty(jenaOntologyCLO2.getURI() + uri_property[1]);				
				DatatypeProperty propertyCLO = jenaOntologyCLO1.getOntModel().getDatatypeProperty(property);
				
				if(propertyCMO != null && propertyCLO != null)
				{
					String clazz1 = property;
					String clazz2 = propertyCMO.getURI();
					
					OntologyResource resource1 = owlOntologyCLO1.getOntologyResource(clazz1);
					OntologyResource resource2 = owlOntologyCLO2.getOntologyResource(clazz2);						
					
					if(resource1 != null && resource2 != null)
					{
						LEAlignment align = new LEAlignment(resource1, resource2, 1.0, ERelation.EQUIVALENT);
						alignments.add(align);
					}
				}
			}
		}
		
		//Object Properties
		ExtendedIterator it3 = jenaOntologyCLO1.getOntModel().listObjectProperties();
		
		while(it3.hasNext())
		{	
			String property = String.valueOf(it3.next());
			String[] uri_property = property.split("#");
			
			if(uri_property.length == 2 && uri_property[1] != null && !uri_property[1].isEmpty())
			{
				ObjectProperty propertyCMO = jenaOntologyCLO2.getOntModel().getObjectProperty(jenaOntologyCLO2.getURI() + uri_property[1]);
				ObjectProperty propertyCLO = jenaOntologyCLO1.getOntModel().getObjectProperty(property);
				
				if(propertyCMO != null && propertyCLO != null)
				{
					String clazz1 = property;
					String clazz2 = propertyCMO.getURI();
					
					OntologyResource resource1 = owlOntologyCLO1.getOntologyResource(clazz1);
					OntologyResource resource2 = owlOntologyCLO2.getOntologyResource(clazz2);
					
					if(resource1 != null && resource2 != null)
					{
						LEAlignment align = new LEAlignment(resource1, resource2, 1.0, ERelation.EQUIVALENT);
						alignments.add(align);
					}
				}
			}
		}
		
		return alignments;
	}
	
	
	/**
	 * Método que executa o matching linguistico-estrutural com uso de um matcher externo.
	 * Gera matching entre as ontologias locais (CLOs) e a de domínio (CMO).
	 * @param fileCLOi Caminho da ontologia local CLOi.
	 * @param fileCLOj Caminho da ontologia local CLOj.
	 * @param fileCMO Caminho da ontologia local CMO.
	 * @return Retorna 2 listas de matching:
	 * 				- Entre a CLOi e CMO.
	 * 				- Entre a CLOj e CMO.
	 */
	public ArrayList<ArrayList<LEAlignment>> executeLEMatchingWithCMO(String fileCLOi, String fileCLOj, String fileCMO)
	{
		ArrayList<ArrayList<LEAlignment>> alignments = new ArrayList<ArrayList<LEAlignment>>();
		
		String outPath2 = fileCMO.substring(0, fileCMO.lastIndexOf("\\"));
		
		ArrayList<LEAlignment> matchingsCLOi = this.executeLEMAtching(fileCLOi, fileCMO, outPath2 + "/MatchingFileCLOi.rdf", EMappingCardinality.ONE_TO_ONE, EMatchingType.LINGUISTIC_STRUCTURAL);
		ArrayList<LEAlignment> matchingsCLOj = this.executeLEMAtching(fileCLOj, fileCMO, outPath2 + "/MatchingFileCLOj.rdf", EMappingCardinality.ONE_TO_ONE, EMatchingType.LINGUISTIC_STRUCTURAL);
		
		alignments.add(matchingsCLOi);
		alignments.add(matchingsCLOj);
					
		return alignments;
	}

	public ArrayList<ArrayList<LEAlignment>> executeLEMatchingWithCMONormalized(String fileCLOi, String fileCLOj, String fileCMO)
	{
		ArrayList<ArrayList<LEAlignment>> alignments = new ArrayList<ArrayList<LEAlignment>>(2);
		
		ArrayList<LEAlignment> matchingsCLOi = this.executeLEMAtchingNormalized(fileCLOi, fileCMO);
		ArrayList<LEAlignment> matchingsCLOj = this.executeLEMAtchingNormalized(fileCLOj, fileCMO);
		
		alignments.add(matchingsCLOi);
		alignments.add(matchingsCLOj);
		
		return alignments;
	}
		
	private ArrayList<LEAlignment> readFileAlignments(String fileOut, OWLApiOntology owlOntology1, OWLApiOntology owlOntology2)
	{
		ArrayList<LEAlignment> matchings = new ArrayList<LEAlignment>();
		
		try {
			String fileContent = XMLParser.getFileContent(fileOut);
			
			ArrayList<String> entities1 = XMLParser.findNodeInAlignmentFile(fileContent,
					"entity1");
			ArrayList<String> entities2 = XMLParser.findNodeInAlignmentFile(fileContent,
					"entity2");
			ArrayList<String> measures = XMLParser.findNodeInAlignmentFile(fileContent,
					"measure");
			ArrayList<String> relations = XMLParser.findNodeInAlignmentFile(fileContent,
					"relation");

			for (int i = 0; i < entities1.size(); i++) {
				
				String clazz1 = entities1.get(i);
				String clazz2 = entities2.get(i);
				
				OntologyResource resource1 = owlOntology1.getOntologyResource(clazz1);
				OntologyResource resource2 = owlOntology2.getOntologyResource(clazz2);
				
				LEAlignment matchingItem = new LEAlignment(resource1, resource2, Double
						.parseDouble(measures.get(i)), ERelation
						.createERelation(relations.get(i)));
				if (entities1.get(i) != null
						&& entities2.get(i) != null
						&& !entities1.get(i).split("#")[1].equalsIgnoreCase("PartOf")
						&& !entities2.get(i).split("#")[1].equalsIgnoreCase("PartOf"))
					matchings.add(matchingItem);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return matchings;	
	}
	
	private void cleanFiles() {		
		String[] cmd = new String[3];
		cmd[0] = "cmd.exe";
		cmd[1] = "/C";
		cmd[2] = "del " + System.getProperty("user.dir") + "\\*.la";

		try
		{
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
		}
		catch (Exception ex)
		{			
			System.out.println(ex.getMessage());
		}
	}
	
}
