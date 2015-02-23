package speed.ontologymatcher.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import speed.ontologymatcher.basics.WeightParam;
import speed.ontologymatcher.semanticmatching.SemanticMatcher;
import speed.ontologymatcher.semanticmatching.basics.Alignment;
import speed.ontologymatcher.semanticmatching.basics.ESSMFunction;

public class Facade {
	
	private static Facade facade;
	
	private Map<WeightParam, Double> weights;
	
	private Facade(){
		this.weights = getDefaultWeight();
	}
	
	public static Facade getInstance()
	{
		if(facade == null)
			facade = new Facade();
		
		return facade;
	}	
	
	/**
	 * Método que calcula a medida de similaridade semântica entre duas ontologias.
	 * 
	 * @param fileCLOi Caminho da ontologia local CLOi.
	 * @param fileCLOj Caminho da ontologia local CLOj.
	 * @param fileCMO Caminho da ontologia de domínio CMO.
	 * @param bestAlignmentsFile Caminho do arquivo com os alinhamentos finais.
	 * @return Retorna o grau de similaridade entre as duas ontologias.
	 */
	public double calculateSSM(String fileCLOi, String fileCLOj,
			String fileCMO, ESSMFunction function, String bestAlignmentsFile) {		
		SemanticMatcher semMatcher = new SemanticMatcher(fileCLOi, fileCLOj, fileCMO, 
				weights);
		return semMatcher.calculateSSM(function, bestAlignmentsFile);
		
	}
			
	/**
	 * Método que executa o matching semântico entre a ontologia em CLOi e CLOj
	 * 
	 * @param fileCLOi Caminho da ontologia local CLOi.
	 * @param fileCLOj Caminho da ontologia local CLOj.
	 * @param fileCMO Caminho da ontologia de domínio CMO.
	 * @return Lista de alinhamentos gerados pelo processo de matching
	 */
	public ArrayList<Alignment> executeSemanticMatching(String fileCLOi, String fileCLOj,
			String fileCMO) {
		SemanticMatcher semMatcher = new SemanticMatcher(fileCLOi, fileCLOj, fileCMO, 
				weights);
		return semMatcher.executeSemanticMatching();
	
	}
	
	/**
	 * Método que executa o matching semântico considerando ontologias normalizadas entre a ontologia CLOi e CLOj
	 * 
	 * @param fileCLOi Caminho da ontologia local CLOi.
	 * @param fileCLOj Caminho da ontologia local CLOj.
	 * @param fileCMO Caminho da ontologia de domínio CMO.
	 * @return
	 */
	public ArrayList<Alignment> executeSemanticMatchingNormalized(String fileCLOi, String fileCLOj,
			String fileCMO) {
		SemanticMatcher semMatcher = new SemanticMatcher(fileCLOi, fileCLOj, fileCMO, 
				weights);
		return semMatcher.executeSemanticMatchingNormalized();
	
	}
	
	public void generateAcoAlignments(String fileCLOi, String fileCLOj,
			String fileCMO)
	{
		SemanticMatcher semMatcher = new SemanticMatcher(fileCLOi, fileCLOj, fileCMO, 
				weights);
		semMatcher.generateAcoAlignments();
	}
	
	/**
	 * Método para configurar o conjunto de pesos que deve ser usado pela ferramenta
	 * 
	 * @param weights
	 */
	public void setWeights (Map<WeightParam, Double> weights){
		this.weights = weights;
	}
	
	/**
	 * Métode que retorna os valores default para os pesos
	 * Equivalent: 1.0
	 * SubConcept: 0.8
	 * SuperConcept: 0.8
	 * Close: 0.7
	 * Part: 0.3
	 * Whole: 0.3
	 * Disjoint: 0.0
	 * 
	 * @return
	 */
	public Map<WeightParam, Double> getDefaultWeight (){
		
		Map<WeightParam, Double> weights = new HashMap<WeightParam, Double>();
		
		weights.put(WeightParam.LE_MATCH, 0.4);
		weights.put(WeightParam.SEMANTIC_MATCH, 0.6);
		weights.put(WeightParam.EQUIVALENT, 1.0);
		weights.put(WeightParam.SUB_CONCEPT, 0.8);
		weights.put(WeightParam.SUPER_CONCEPT, 0.8);
		weights.put(WeightParam.CLOSE_TO, 0.7);
		weights.put(WeightParam.PART_OF, 0.3);
		weights.put(WeightParam.WHOLE_OF, 0.3);
		weights.put(WeightParam.DISJOINT, 0.0);
	
		return weights;
	}
}
