package speed.ontologymatcher.basics;

public class OntologyProperty extends OntologyResource {

	private String className;
	private String property;

	public OntologyProperty(String clazz, String prop)
	{
		this.className = clazz;
		this.property = prop;
	}
	
	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public String getDescription() {
		return this.className.split("#")[1] + "." + this.property.split("#")[1];
	}
	
	@Override
	public String toString() {
		return this.className + "." + this.property.split("#")[1];
	}
	
}
