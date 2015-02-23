package speed.ontologymatcher.tests;

import speed.ontologymatcher.lematching.LEMatcherManager;
import speed.ontologymatcher.lematching.enums.EMappingCardinality;
import speed.ontologymatcher.lematching.enums.EMatchingType;

public class Main {

	public static void main(String[] args) {
		
		String ontologiesDirectory = "D:\\CIn\\workspace\\eclipseCIn\\OntologyMatcher\\owls\\Carlos\\";
		
		String cloi = ontologiesDirectory + "LO01-Education.owl"; //AnimalCLO1.owl
		String cloj = ontologiesDirectory + "LO03-Education.owl"; //AnimalCLO1.owl
		//String cmo = ontologiesDirectory + "LO03-Education.owl"; //Organism_CMO.owl
		
		LEMatcherManager manager = new LEMatcherManager();
		
		manager.executeLEMAtching(cloi, cloj, ontologiesDirectory + "saida.owl", EMappingCardinality.ONE_TO_ONE, EMatchingType.LINGUISTIC_STRUCTURAL);
		
	}
}
