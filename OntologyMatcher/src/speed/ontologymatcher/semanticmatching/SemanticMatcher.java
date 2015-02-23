package speed.ontologymatcher.semanticmatching;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import speed.ontologymatcher.basics.OntologyClass;
import speed.ontologymatcher.basics.OntologyResource;
import speed.ontologymatcher.basics.WeightParam;
import speed.ontologymatcher.lematching.LEMatcherManager;
import speed.ontologymatcher.lematching.basics.LEAlignment;
import speed.ontologymatcher.lematching.enums.EMappingCardinality;
import speed.ontologymatcher.lematching.enums.EMatcher;
import speed.ontologymatcher.lematching.enums.EMatchingType;
import speed.ontologymatcher.semanticmatching.basics.Alignment;
import speed.ontologymatcher.semanticmatching.basics.ESSMFunction;
import speed.ontologymatcher.semanticmatching.inference.Inference;
import speed.ontologymatcher.util.OntologyMatcherProperties;
import speed.ontologymatcher.util.ontologyhelpers.JenaOntology;
import speed.ontologymatcher.util.ontologyhelpers.OWLApiOntology;
import speed.ontologymatcher.util.sparql.StorageManager;

/**
 * Classe com m�todos para c�lculo de alinhamentos sem�nticos.
 * 
 * @author Thiago Pach�co Andrade Pereira
 * 
 */
public class SemanticMatcher {

	private static final String SEPARATOR = File.separator;
	private String fileCLOi;
	private String fileCLOj;
	private String fileCMO;
	
	private double wLE, wSemantic, wEquivalent, wSubConcept, wSuperConcept, wPart, wWhole, wDisjoint, wClose;
	
	OWLApiOntology owlApiCLOi;
	OWLApiOntology owlApiCLOj;
	
	
	public SemanticMatcher(String fileCLOi, String fileCLOj, String fileCMO, 
			Map<WeightParam, Double> weightParams)
	{
		this.fileCLOi = fileCLOi;
		this.fileCLOj = fileCLOj;
		this.fileCMO = fileCMO;
		
		this.wLE = weightParams.containsKey(WeightParam.LE_MATCH) ? weightParams.get(WeightParam.LE_MATCH) : 0;
		this.wSemantic = weightParams.containsKey(WeightParam.SEMANTIC_MATCH) ? weightParams.get(WeightParam.SEMANTIC_MATCH) : 0;
		
		this.wEquivalent = weightParams.containsKey(WeightParam.EQUIVALENT) ? weightParams.get(WeightParam.EQUIVALENT) : 0;
		this.wSubConcept = weightParams.containsKey(WeightParam.SUB_CONCEPT) ? weightParams.get(WeightParam.SUB_CONCEPT) : 0;
		this.wSuperConcept = weightParams.containsKey(WeightParam.SUPER_CONCEPT) ? weightParams.get(WeightParam.SUPER_CONCEPT) : 0;
		this.wPart = weightParams.containsKey(WeightParam.PART_OF) ? weightParams.get(WeightParam.PART_OF) : 0;
		this.wWhole = weightParams.containsKey(WeightParam.WHOLE_OF) ? weightParams.get(WeightParam.WHOLE_OF) : 0;
		this.wDisjoint = weightParams.containsKey(WeightParam.DISJOINT) ? weightParams.get(WeightParam.DISJOINT) : 0;
		this.wClose = weightParams.containsKey(WeightParam.CLOSE_TO) ? weightParams.get(WeightParam.CLOSE_TO) : 0;
		
		this.owlApiCLOi = new OWLApiOntology(fileCLOi);
		this.owlApiCLOj = new OWLApiOntology(fileCLOj);		
	}
	
	/**
	 * M�todo que executa o matching sem�ntico.
	 * 
	 * @param fileCLOi Caminho da ontologia local CLOi.
	 * @param fileCLOj Caminho da ontologia local CLOj.
	 * @param fileCMO Caminho da ontologia local CMO.
	 * @return Retorna uma lista de alinhamentos sem�nticos.
	 */
	public ArrayList<Alignment> executeSemanticMatching() {
		ArrayList<Alignment> semanticAlignments = null;
		
		// Matching lingu�stico-estrutural
		LEMatcherManager matcherManager = new LEMatcherManager();
		ArrayList<ArrayList<LEAlignment>> leAlignments = matcherManager
				.executeLEMatchingWithCMO(fileCLOi, fileCLOj, fileCMO);

		// Matching-sem�ntico
		if(leAlignments.size() == 2)
		{
			Inference inference = new Inference(leAlignments.get(0), leAlignments
					.get(1), fileCMO, 1.0);
			/*Model matchings = inference.generateMatchingModel();
			semanticAlignments = inference
					.returnSematicAlignments(matchings);*/
			semanticAlignments = inference.executeRules();
						
			for(Alignment ali : semanticAlignments)
			{
				ali.setWeight(this.getSemanticRelationWeight(ali.getPredicate()));
			}
		}

		return semanticAlignments;
	}
	
	/**
	 * M�todo que executa o matching sem�ntico considerando ontologias normalizadas.
	 * 
	 * @param fileCLOi Caminho da ontologia local CLOi.
	 * @param fileCLOj Caminho da ontologia local CLOj.
	 * @param fileCMO Caminho da ontologia local CMO.
	 * @return Retorna uma lista de alinhamentos sem�nticos.
	 */
	public ArrayList<Alignment> executeSemanticMatchingNormalized()
	{
		ArrayList<Alignment> semanticAlignments = null;
		
		// Matching lingu�stico-estrutural
		LEMatcherManager matcherManager = new LEMatcherManager();
		ArrayList<ArrayList<LEAlignment>> leAlignments = matcherManager
		.executeLEMatchingWithCMONormalized(fileCLOi, fileCLOj, fileCMO);

		// Matching-sem�ntico
		if(leAlignments.size() == 2)
		{
			Inference inference = new Inference(leAlignments.get(0), leAlignments
					.get(1), fileCMO, 1.0);
			
			semanticAlignments = inference.executeRules();
						
			for(Alignment ali : semanticAlignments)
			{				
				ali.setWeight(this.getSemanticRelationWeight(ali.getPredicate()));
			}
		}
		
		return semanticAlignments;
	}
	

	/**
	 * M�todo que calcula a medida de similaridade entre duas ontologias.
	 * 
	 * @param fileCLOi Caminho da ontologia local CLOi.
	 * @param fileCLOj Caminho da ontologia local CLOj.
	 * @param fileCMO Caminho da ontologia de dom�nio CMO.
	 * @param bestAlignmentsFile Caminho do arquivo com os alinhamentos finais.
	 * @return Retorna o grau de similaridade entre as duas ontologias.
	 */
	public double calculateSSM(ESSMFunction function, String bestAlignmentsFile) {
		double measure = -1.0;

		if(OntologyMatcherProperties.NORMALIZED)
		{
			//Matching linguistico estrutural.
			LEMatcherManager matcherManager = new LEMatcherManager();
			String outPath = fileCMO.substring(0, fileCMO.lastIndexOf(SEPARATOR));
			
			ArrayList<LEAlignment> leAlignmentsCLOij = new ArrayList<LEAlignment>();
			
			if(OntologyMatcherProperties.LE_WEIGHT > 0)
			if(OntologyMatcherProperties.MATCHER == EMatcher.NONE)
			{
				leAlignmentsCLOij = matcherManager
				.executeLEMAtching(fileCLOi, fileCLOj, OntologyMatcherProperties.MATCHINGS_LE, EMappingCardinality.ONE_TO_MANY,
					EMatchingType.LINGUISTIC_STRUCTURAL);
			}
			else
			{
			
				leAlignmentsCLOij = matcherManager
					.executeLEMAtching(fileCLOi, fileCLOj, outPath
						+ File.separator +"StruturalLinguisticAlignments.rdf", EMappingCardinality.ONE_TO_MANY,
						EMatchingType.LINGUISTIC_STRUCTURAL);
			
			}
			
			//Matching sem�ntico
			ArrayList<Alignment> semanticAlignmentsCLOij = new ArrayList<Alignment>();
			
			if(this.wSemantic > 0)
				semanticAlignmentsCLOij = this.executeSemanticMatchingNormalized();
			
			if(OntologyMatcherProperties.DEBUG)
			{
				StorageManager.saveToFile(semanticAlignmentsCLOij, outPath + SEPARATOR + "SemanticAlignments.owl");
			}
						
			ArrayList<Alignment> finalAlignmentsCLOij = this.calculateAlignmentAverage(semanticAlignmentsCLOij, leAlignmentsCLOij, JenaOntology.getConcepts(fileCLOi), JenaOntology.getConcepts(fileCLOj));
			ArrayList<Alignment> finalAlignmentsCLOji = null;
					
			
			ArrayList<ArrayList<Alignment>> finalAlignments = this.selectMatchCandidates(finalAlignmentsCLOij);  
			
			finalAlignmentsCLOij = finalAlignments.get(0);
			finalAlignmentsCLOji = finalAlignments.get(1);				
			
			this.saveBestAlignmentsToFile(finalAlignmentsCLOij, finalAlignmentsCLOji, bestAlignmentsFile);
						
			//Pega o n�mero de conceitos.
			// TODO: Tornar n�o est�tico.
			int numOfConceptsCLOi = JenaOntology.getNumberOfConcepts(fileCLOi);
			int numOfConceptsCLOj = JenaOntology.getNumberOfConcepts(fileCLOj);
			
			if(function == ESSMFunction.Average)
			{
				double sumCLOij = 0.0;
				
				for(Alignment align : finalAlignmentsCLOij)
					sumCLOij += align.getWeight();
				
				double sumCLOji = 0.0;
				
				for(Alignment align : finalAlignmentsCLOji)
					sumCLOji += align.getWeight();
				
				measure = (sumCLOij + sumCLOji)/(numOfConceptsCLOi + numOfConceptsCLOj);
				
			}
			else if(function == ESSMFunction.DICE)
			{
				double numCLOij = 0.0;
				
				for(Alignment align : finalAlignmentsCLOij)
					if(align.getWeight() > 0)
						numCLOij += 1;				
				
				double numCLOji = 0.0;
				
				for(Alignment align : finalAlignmentsCLOji)
					if(align.getWeight() > 0)
						numCLOji += 1;
				
				measure = (numCLOij + numCLOji)/(numOfConceptsCLOi + numOfConceptsCLOj);
				
			}
		}else{
		
			// Matching lingu�stico estrutural.		
			LEMatcherManager matcherManager = new LEMatcherManager();
			String outPath = fileCMO.substring(0, fileCMO.lastIndexOf(SEPARATOR));
			ArrayList<LEAlignment> alignmentsLECLOij = matcherManager
					.executeLEMAtching(fileCLOi, fileCLOj, outPath
							+ "/StruturalLinguisticAlignments.rdf", EMappingCardinality.ONE_TO_MANY,
							EMatchingType.LINGUISTIC_STRUCTURAL);
			ArrayList<LEAlignment> alignmentsLECLOji = matcherManager
					.executeLEMAtching(fileCLOj, fileCLOi, outPath
							+ "/StruturalLinguisticAlignments.rdf", EMappingCardinality.ONE_TO_MANY,
							EMatchingType.LINGUISTIC_STRUCTURAL);
			
			// Matching Sem�ntico.
			ArrayList<Alignment> alignmentsSemanticCLOij = this
			.executeSemanticMatching();
			ArrayList<Alignment> alignmentsSemanticCLOji = this
			.executeSemanticMatching();
			
			ArrayList<ArrayList<Alignment>> alignments11 = this.selectMatchCandidates(alignmentsSemanticCLOij); 
			
			alignmentsSemanticCLOij = alignments11.get(0);
			alignmentsSemanticCLOji = alignments11.get(1);
			
			ArrayList<Alignment> finalAlignmentsCLOij = this.calculateAlignmentAverage(alignmentsSemanticCLOij, alignmentsLECLOij , JenaOntology.getConcepts(fileCLOi), JenaOntology.getConcepts(fileCLOj));
			ArrayList<Alignment> finalAlignmentsCLOji = this.calculateAlignmentAverage(alignmentsSemanticCLOji, alignmentsLECLOji, JenaOntology.getConcepts(fileCLOj), JenaOntology.getConcepts(fileCLOi));
			
			int numO1Concepts = JenaOntology.getNumberOfConcepts(fileCLOi);
			int numO2Concepts = JenaOntology.getNumberOfConcepts(fileCLOj);
	
			if (function == ESSMFunction.Average) {
				double sumSimLESemanticCLOij = 0.0;
				double sumSimLESemanticCLOji = 0.0;
	
				for (Alignment align : finalAlignmentsCLOij)
					sumSimLESemanticCLOij += align.getWeight();
	
				for (Alignment align : finalAlignmentsCLOji)
					sumSimLESemanticCLOji += align.getWeight();
	
				measure = (sumSimLESemanticCLOij + sumSimLESemanticCLOji)
						/ (numO1Concepts + numO2Concepts);
			} else if (function == ESSMFunction.DICE) {
				double numAlignmentsCLOij = finalAlignmentsCLOij.size();
				double numAlignmentsCLOji = finalAlignmentsCLOji.size();
	
				measure = (numAlignmentsCLOij + numAlignmentsCLOji)
						/ (numO1Concepts + numO2Concepts);
			}
		}
		return measure;
	}

	
	public void generateAcoAlignments()
	{
//		Matching linguistico estrutural.
		LEMatcherManager matcherManager = new LEMatcherManager();
		String outPath = fileCMO.substring(0, fileCMO.lastIndexOf(SEPARATOR));
		
		ArrayList<LEAlignment> leAlignmentsCLOij = null;
		
		if(OntologyMatcherProperties.MATCHER == EMatcher.NONE)
		{
			leAlignmentsCLOij = matcherManager
			.executeLEMAtching(fileCLOi, fileCLOj, OntologyMatcherProperties.MATCHINGS_LE, EMappingCardinality.ONE_TO_MANY,
				EMatchingType.LINGUISTIC_STRUCTURAL);
		}
		else
		{
		
			leAlignmentsCLOij = matcherManager
				.executeLEMAtching(fileCLOi, fileCLOj, outPath
					+ "/StruturalLinguisticAlignments.rdf", EMappingCardinality.ONE_TO_MANY,
					EMatchingType.LINGUISTIC_STRUCTURAL);
		
		}
		
		//Matching sem�ntico
		ArrayList<Alignment> semanticAlignmentsCLOij = 
			this.executeSemanticMatchingNormalized();
		
		if(OntologyMatcherProperties.DEBUG)
		{
			StorageManager.saveToFile(semanticAlignmentsCLOij, outPath + SEPARATOR + "SemanticAlignments.owl");
		}
		
		ArrayList<Alignment> finalAlignments = this.calculateAlignmentAverage(semanticAlignmentsCLOij, leAlignmentsCLOij, JenaOntology.getConcepts(fileCLOi), JenaOntology.getConcepts(fileCLOj));
		
		StorageManager.saveToFile(finalAlignments, outPath + SEPARATOR + "AcoAlignments.owl");
		
	}
	
	/**
	 * M�todo auxiliar que diminui os relacionamentos 1:N em 1:1 selecionando os melhores candidatos.
	 * 
	 * @param alignments1N Todos alinhamentos sem�nticos.
	 * @return Retorna alinhamentos 1:1.
	 */
	private ArrayList<ArrayList<Alignment>> selectMatchCandidates(ArrayList<Alignment> alignments1N)
	{
		ArrayList<ArrayList<Alignment>> alignments = new ArrayList<ArrayList<Alignment>>();
		
		ArrayList<Alignment> bestAlignments11_1 = new ArrayList<Alignment>();
		ArrayList<Alignment> bestAlignments11_2 = new ArrayList<Alignment>();
		ArrayList<Alignment> selectedAlignments11_1 = new ArrayList<Alignment>();
		ArrayList<Alignment> selectedAlignments11_2 = new ArrayList<Alignment>();
		
		
		//Seleciona melhores alinhamentos na "ida"
		ArrayList<String> subjects = new ArrayList<String>();

		for (Alignment align : alignments1N) {
			//if(align.getSubject() instanceof OntologyClass && align.getObject() instanceof OntologyClass)
			if (!subjects.contains(align.getSubject().toString())) //VERIFICAR
				subjects.add(align.getSubject().toString()); //VERIFICAR
		}
		
		for(int i = 0; i < subjects.size(); i++)
		{
			String sub = subjects.get(i);
			int indexSub = -1;
			
			double biggersemanticRelation = -1.0;
			
			for (int j = 0; j < alignments1N.size(); j++) {
				Alignment align = alignments1N.get(j);
				
				//if(align.getSubject() instanceof OntologyClass && align.getObject() instanceof OntologyClass)
				if (align.getSubject().toString().equals(sub)
						&& align.getWeight() > biggersemanticRelation) {
					biggersemanticRelation = align.getWeight();
					indexSub = j;
				}
			}

			if (indexSub != -1 && biggersemanticRelation > 0.0) {
				bestAlignments11_1.add(alignments1N.get(indexSub));
			}			
		}
		
		// Seleciona melhores alinhamentos na "volta"
		subjects = new ArrayList<String>();
		
		for (Alignment align : alignments1N) {
			if(align.getSubject() instanceof OntologyClass && align.getObject() instanceof OntologyClass)
				if (!subjects.contains(align.getObject().toString()))
					subjects.add(align.getObject().toString());
		}
		
		for(int i = 0; i < subjects.size(); i++)
		{
			String sub = subjects.get(i);
			int indexSub = -1;
			
			double biggersemanticRelation = -1.0;
			
			for (int j = 0; j < alignments1N.size(); j++) {
				Alignment align = alignments1N.get(j);
				//if(align.getSubject() instanceof OntologyClass && align.getObject() instanceof OntologyClass)
				if (align.getObject().toString().equals(sub)
						&& align.getWeight() > biggersemanticRelation) {
					biggersemanticRelation = align.getWeight();
					indexSub = j;
				}
			}
						
			if (indexSub != -1 && biggersemanticRelation > 0.0) {
				Alignment invertedAlign = new Alignment();
				invertedAlign.setSubject(alignments1N.get(indexSub).getObject());
				invertedAlign.setObject(alignments1N.get(indexSub).getSubject());
				invertedAlign.setPredicate(alignments1N.get(indexSub).getPredicate());
				invertedAlign.setWeight(alignments1N.get(indexSub).getWeight());				
				bestAlignments11_2.add(invertedAlign);
			}			
		}
		
		//Faz verifica��o do COMA de se existe nos dois lados o relacionamento.
		
		for(Alignment ali1 : bestAlignments11_1)
		{
			boolean contains = false;
			
			for (Alignment ali2 : bestAlignments11_2)
			{	
				if(ali1.getSubject().toString().equals(ali2.getObject().toString())
						&& ali1.getObject().toString().equals(ali2.getSubject().toString())
						//&& ali1.getPredicate().equals(ali2.getPredicate())
						)
					contains = true;
			}
			
			if(contains)
			{
				selectedAlignments11_1.add(ali1);
			}
		}
		
		for(Alignment ali2 : bestAlignments11_2)
		{
			boolean contains = false;
			
			for (Alignment ali1 : bestAlignments11_1)
			{	
				if(ali2.getSubject().toString().equals(ali1.getObject().toString())
						&& ali2.getObject().toString().equals(ali1.getSubject().toString())
						//&& ali2.getPredicate().equals(ali1.getPredicate())
						)
				{
					contains = true;
				}
			}
			
			if(contains)
			{
				selectedAlignments11_2.add(ali2);
			}
		}
		
		alignments.add(selectedAlignments11_1);
		alignments.add(selectedAlignments11_2);
		
		return alignments;
	}
		
	/**
	 * M�todo auxiliar para pegar valor de cada relacionamento sem�ntico.
	 * 
	 * @param predicate Predicado do relacionamento sem�ntico.
	 * @return Valor do relacionamento sem�ntico.
	 */
	private double getSemanticRelationWeight(String predicate) {
		double weight = -1.0;

		if (predicate.contains("isEquivalentTo")) {
			weight = this.wEquivalent;
		} else if (predicate.contains("isSubConceptOf")) {
			weight = this.wSubConcept;
		} else if (predicate.contains("isSuperConceptOf")) {
			weight = this.wSuperConcept;
		} else if (predicate.contains("isPartOf")){
			weight = this.wPart;
		} else if (predicate.contains("isWholeOf")) {
			weight = this.wWhole;
		} else if (predicate.contains("isDisjointWith")) {
			weight = this.wDisjoint;
		} else if (predicate.contains("isCloseTo")) {
			weight = this.wClose;
		}

		return weight;
	}
	
	/**
	 * M�todo que calcula a m�dia dos pesos sem�nticos e lingu�stico-estruturais.
	 * @param semanticAlignments Alinhamentos sem�nticos.
	 * @param leAlignments Alinhamentos lingu�stico-estruturais.
	 * @return Lista de alinhamentos com a m�dia calculada.
	 */
	
	private ArrayList<Alignment> calculateAlignmentAverage(
			ArrayList<Alignment> semanticAlignments, ArrayList<LEAlignment> leAlignments
			, ArrayList<String> classesInO1, ArrayList<String> classesInO2) {
		
		ArrayList<Alignment> finalAlignments = new ArrayList<Alignment>();		
				
		for(String class1 : classesInO1)
		{	
			for(String class2 : classesInO2)
			{
				LEAlignment alignLE = null;
				Alignment semAlign = null;
				double similarity = 0.0;
				
				for (LEAlignment leItem : leAlignments) 
				{	
					if(leItem != null && leItem.getClass1() != null && leItem.getClass2() != null)
					if(leItem.getClass1().toString().equals(class1.toString()) && leItem.getClass2().toString().equals(class2.toString()))
					{
						alignLE = leItem;
						break;
					}
				}
				
				for (Alignment semItem : semanticAlignments) 
				{
					if(semItem != null && semItem.getSubject() != null && semItem.getObject() != null)
					if(semItem.getSubject().toString().equals(class1.toString()) && semItem.getObject().toString().equals(class2.toString()))
					{
						semAlign = semItem;
						break;
					}
				}
				
				if(alignLE != null)
					similarity = alignLE.getMeasure()
					* this.wLE;
				
				if(semAlign != null)
				{
					String predicate = semAlign.getPredicate();
				
					similarity += this.getSemanticRelationWeight(predicate)
						* this.wSemantic;
				}
				
				OntologyResource clazz1 = null;
				
				if (semAlign != null)
				{
					clazz1 = semAlign.getSubject();
				}
				else if(alignLE != null)
				{
					clazz1 = alignLE.getClass1();					
				}
				else
				{
					clazz1 = this.owlApiCLOi.getOntologyResource(class1);					
				}
				
				OntologyResource clazz2 = null;
				
				
				if (semAlign != null)
				{
					clazz2 = semAlign.getObject();
				}

				else if(alignLE != null)
				{
					clazz2 = alignLE.getClass2();					
				}
				else
				{
					clazz2 = this.owlApiCLOj.getOntologyResource(class2);					
				}
				
				Alignment speedStatement = new Alignment();
				speedStatement.setSubject(clazz1);
				speedStatement.setPredicate(semAlign != null ? semAlign.getPredicate() : "");
				speedStatement.setObject(clazz2);
				speedStatement.setWeight(similarity);
				
				if(similarity > 0.0 && speedStatement.getSubject() != null && speedStatement.getObject() != null)
					finalAlignments.add(speedStatement);
			}
		}
		
		return finalAlignments;
	}

	/**
	 * M�todo que salva em um arquivo os melhores alinhamentos.
	 * @param alignmentsO1O2 Alinhamentos de ida.
	 * @param alignmentsO2O1 Alinhamentos de volta.
	 * @param path Caminho do arquivo
	 */
	private void saveBestAlignmentsToFile(ArrayList<Alignment> alignmentsO1O2, ArrayList<Alignment> alignmentsO2O1, String path)
	{
		ArrayList<Alignment> finalAlignments = new ArrayList<Alignment>();
		
		for(Alignment alignO1O2 : alignmentsO1O2)
		{
			for(Alignment alignO2O1 : alignmentsO2O1)
			{
				if(alignO1O2.getSubject().toString().equals(alignO2O1.getObject().toString())
					&& alignO1O2.getObject().toString().equals(alignO2O1.getSubject().toString())
					&& alignO1O2.getPredicate().equals(alignO2O1.getPredicate())
				)
				{
					Alignment align = new Alignment();
					align.setSubject(alignO1O2.getSubject());
					align.setPredicate(alignO1O2.getPredicate());
					align.setObject(alignO1O2.getObject());
					
					double weight = 0.0;
					
					if(alignO1O2.getWeight() > alignO1O2.getWeight())
					{	
						weight = alignO1O2.getWeight();
					}
					else
					{
						weight = alignO2O1.getWeight();						
					}
					
					align.setWeight(weight);
					
					finalAlignments.add(align);					
				}
			}
		}
		
		StorageManager.saveToFile(finalAlignments, path);
		
	}	
	
}
