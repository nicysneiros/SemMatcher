package speed.ontologymatcher.lematching.enums;

/**
 * Enum com os tipos de relacionamentos possíveis em um alinhamento.
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public enum ERelation {

	NONE(""),
	EQUIVALENT("=");
	
	private String str = null;
	
	private ERelation(String toStr)
	{
		this.str = toStr;		
	}
	@Override	
	public String toString() 
	{
		return this.str;
	}
	
	public static ERelation createERelation(String relationName)
	{
		if(relationName.equalsIgnoreCase(EQUIVALENT.str))
		{
			return ERelation.EQUIVALENT;
		}
		else
		{
			return ERelation.NONE;			
		}
	}
	
}
