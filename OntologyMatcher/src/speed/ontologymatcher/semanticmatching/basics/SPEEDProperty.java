package speed.ontologymatcher.semanticmatching.basics;

import java.util.ArrayList;

public class SPEEDProperty {
	
	private String propertyName;
	
	private ArrayList<String> domain;
	private ArrayList<String> range;
	
	public SPEEDProperty()
	{
		this.domain = new ArrayList<String>();
		this.range = new ArrayList<String>();
	}
	
	public SPEEDProperty(String propName, ArrayList<String> domain, ArrayList<String> range)
	{
		this.propertyName = propName;
		this.domain = domain;
		this.range = range;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public ArrayList<String> getDomain() {
		return domain;
	}
	public void setDomain(ArrayList<String> domain) {
		this.domain = domain;
	}
	public ArrayList<String> getRange() {
		return range;
	}
	public void setRange(ArrayList<String> range) {
		this.range = range;
	}
	
	public static SPEEDProperty findPropertyByName(String propName, ArrayList<SPEEDProperty> properties)
	{
		SPEEDProperty property = null;
		
		for(SPEEDProperty prop : properties)
			if(prop.getPropertyName().equals(propName))
				property = prop;
		
		return property;		
	}

}
