package speed.ontologymatcher.lematching.enums;

/**
 * Enum com o tipo de matching a ser usado.
 * Obs: Apenas o tipo LINGUISTIC_STRUCTURAL está implementado. 
 * @author Thiago Pachêco Andrade Pereira.
 *
 */
public enum EMatchingType {
	
	LINGUISTIC("l"),
	CONTEXTUAL("c"),
	STRUCTURAL("s"),
	INSTANCE("i"),
	LINGUISTIC_STRUCTURAL("ls"),
	NONE("none");
	;
	
	private String str = null;
	
	private EMatchingType(String toStr)
	{
		this.str = toStr;		
	}
	@Override	
	public String toString() {
		return this.str;
	}
}
