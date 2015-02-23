package speed.ontologymatcher.tests;

import java.net.URI;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class Main2 {

	public static void main(String[] args) {
		
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		try {
		
			manager.loadOntologyFromPhysicalURI(URI.create("file:/D:/CIn/workspace/eclipseCIn/OntologyMatcher/owls/Carlos/Novapasta/NewFolder/univcsCMO.owl"));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
	}
	
}
