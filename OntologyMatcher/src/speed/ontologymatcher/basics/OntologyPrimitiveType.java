package speed.ontologymatcher.basics;

public class OntologyPrimitiveType extends OntologyResource {

	private String typeName;

	public OntologyPrimitiveType(String clazz)
	{
		this.typeName = clazz;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public String getDescription() {
		return this.typeName.split("#")[1];
	}
	
	@Override
	public String toString() {
		return this.typeName;
	}

}
