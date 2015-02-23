package speed.ontologymatcher.util.sparql;

import speed.ontologymatcher.basics.OntologyResource;

/**
 * Classe básica que representa um statement owl.
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public class SpeedStatement {
	
	/**
	 * Sujeito
	 */
	private OntologyResource subject;
	/**
	 * Predicado
	 */
	private String predicate;
	/**
	 * Objeto
	 */
	private OntologyResource object;
	
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

}
