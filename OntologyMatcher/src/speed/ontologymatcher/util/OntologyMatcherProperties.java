package speed.ontologymatcher.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import speed.ontologymatcher.lematching.enums.EMatcher;

public class OntologyMatcherProperties {

	public static String SPEED_URI;

	public static String TYPE_OF_URI;

	public static String CLASS_URI;

	public static String EQUIVALENT_CLASS;

	public static String DB_URL;

	public static String DB_USER;

	public static String DB_PASSWD;

	public static String DB;

	public static String DB_DRIVER;

	public static int THRESHOLD;
	
	public static int THRESHOLD_ROOT;

	public static EMatcher MATCHER;

	public static boolean DEBUG;

	public static double LE_WEIGHT;

	public static double SEMANTIC_WEIGHT;
	
	//public static boolean USE_JENA;
	
	public static boolean NORMALIZED;
	
	public static String ALIGNMENTAPI_PATH;
	
	public static String MATCHINGS_LE;

	static {
		File file = new File("ontologymatcher.properties");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		SPEED_URI = props.getProperty("speed_uri");
		TYPE_OF_URI = props.getProperty("type_of_uri");
		CLASS_URI = props.getProperty("class_uri");
		EQUIVALENT_CLASS = props.getProperty("equivalent_class_uri");
		DB_URL = props.getProperty("db_url");
		DB_USER = props.getProperty("db_user");
		DB_PASSWD = props.getProperty("db_passwd");
		DB = props.getProperty("db");
		DB_DRIVER = props.getProperty("db_driver");
		THRESHOLD = Integer.parseInt(props.getProperty("threshold"));
		THRESHOLD_ROOT = Integer.parseInt(props.getProperty("thresholdRoot"));
		MATCHER = EMatcher.create(Integer.parseInt(props.getProperty("matcher")));
		DEBUG = Boolean.parseBoolean(props.getProperty("debug"));
		LE_WEIGHT = Double.parseDouble(props.getProperty("le_weight"));
		SEMANTIC_WEIGHT = Double.parseDouble(props.getProperty("semantic_weight"));
		//USE_JENA = Boolean.parseBoolean(props.getProperty("use_jena"));
		NORMALIZED = Boolean.parseBoolean(props.getProperty("normalized"));
		ALIGNMENTAPI_PATH = props.getProperty("alignmentApiPath");
		MATCHINGS_LE = props.getProperty("matchingsLE");
		
	}
}
