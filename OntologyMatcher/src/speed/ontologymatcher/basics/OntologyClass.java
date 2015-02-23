package speed.ontologymatcher.basics;

public class OntologyClass extends OntologyResource {

	private String className;

	public OntologyClass(String clazz)
	{
		this.className = clazz;
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public String getDescription() {
		return this.className.split("#")[1];
	}
	
	@Override
	public String toString() {
		return this.className;
	}
	
}
