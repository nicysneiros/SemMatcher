package speed.ontologymatcher.lematching.enums;

/**
 * Enum com os tipos de alinhamentos possíveis.
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public enum EMappingCardinality {
	
	ONE_TO_ONE("1:1"),
	ONE_TO_MANY("1:N")
	;
	
	private String str = null;
	
	private EMappingCardinality(String toStr)
	{
		this.str = toStr;		
	}
	@Override	
	public String toString() {
		return this.str;
	}
}
