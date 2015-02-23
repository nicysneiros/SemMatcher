package speed.ontologymatcher.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import speed.ontologymatcher.basics.OntologyClass;
import speed.ontologymatcher.lematching.enums.EMatcher;
import speed.ontologymatcher.semanticmatching.basics.Alignment;

/**
 * Classe para tratamento dos arquivo de matching gerados pelas ferramentas de matching linguistico-estruturais.
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public class XMLParser {
	/**
	 * Recebe um arquivo de alinhamento e um nó do xml, e retorna os valores de seus atributos.
	 * @param filePath Caminho do arquivo de alinhamentos.
	 * @param node Nome do nó do XML a ser procurado. 
	 * @return Lista de atributos do nó.
	 * @throws FileNotFoundException Lança uma excessão do tipo FileNotFoundException se o arquivo não for encontrado. 
	 */
	
	@SuppressWarnings("all")
	public static String getFileContent(String filePath)
	{
		String fileText = "";
		
		try {  
            BufferedReader br = new BufferedReader (new FileReader (filePath));  
            StringBuffer ret = new StringBuffer();  
            String line;  
            while ((line = br.readLine()) != null) {  
                ret.append (line);  
            }  
            br.close();  
             
            fileText = ret.toString();
        } catch (IOException ex) {  
            ex.printStackTrace();  
        }
		
		fileText = fileText.replaceAll(" ", "").replaceAll("\n", "");
		
		return fileText;
	}
	
	
	public static ArrayList<String> findNodeInAlignmentFile(String fileText, String node) throws FileNotFoundException
	{	
		ArrayList<String> results = new ArrayList<String>();
		
		fileText = fileText.replaceAll(" ", "").replaceAll("\n", "");
		
		String[] strings = fileText.split("<" + node);
		
		
		for(int i = 0; i < strings.length; i++)
		{
			String string = strings[i];
			if(string.contains("<?xmlversion=") || string.startsWith("<Match"))
				continue;
			
			int index = string.indexOf("</" + node + ">");
			
			if(index != -1)
			{
				int initialIndex = string.indexOf(">") + 1;
				
				String nodeResult = string.substring( initialIndex, index);
				results.add(nodeResult);
			}
			else
			{
				String stringTemp = string.split("/>")[0];
				
				int initialIndex = -1;
				int finalIndex = -1;
				if(OntologyMatcherProperties.MATCHER == EMatcher.HMATCH || OntologyMatcherProperties.MATCHER == EMatcher.HMATCHCL)
				{
					initialIndex = stringTemp.indexOf("\"") + 1;
					finalIndex = stringTemp.lastIndexOf("\"");
				}
				else if(OntologyMatcherProperties.MATCHER == EMatcher.ALIGNMENT_API || OntologyMatcherProperties.MATCHER == EMatcher.NONE)
				{
					initialIndex = stringTemp.indexOf("'") + 1;
					finalIndex = stringTemp.lastIndexOf("'");				
				}
				
				String nodeResult = stringTemp.substring(initialIndex, finalIndex);
				
				results.add(nodeResult);
			}
			
		}
		
		return 
			results != null && results.size() > 0 ? results : null;
	}

	public static ArrayList<Alignment> getFileAlignments(String filePath) throws FileNotFoundException
	{
		ArrayList<Alignment> alignments = new ArrayList<Alignment>();
		
		
		ArrayList<String> subjects = findNodeInAlignmentFile(filePath, "Subject");
		ArrayList<String> predicates = findNodeInAlignmentFile(filePath, "Predicate");
		ArrayList<String> objects = findNodeInAlignmentFile(filePath, "Object");
		ArrayList<String> similarity = findNodeInAlignmentFile(filePath, "Similarity");
		
		int size = subjects.size(); 
		
		if(predicates.size() == size && objects.size() == size && similarity.size() == size)
		{
			Alignment align = null;
			for(int i = 0; i < size; i++)
			{
				align = new Alignment();
				align.setSubject(new OntologyClass(subjects.get(i)));
				align.setPredicate(predicates.get(i));
				align.setObject(new OntologyClass(objects.get(i)));
				align.setWeight(Double.parseDouble(similarity.get(i)));
				
				alignments.add(align);
			}
		}
		return alignments;
	}
	
	/*
	public static ArrayList<String> findNodeInXML(String filePath, String node) throws FileNotFoundException
	{
		ArrayList<String> results = new ArrayList<String>();
		Scanner scan = new Scanner(new File(filePath));
		String fileText = "";
		
		while (scan.hasNextLine())
		{
			fileText += scan.nextLine();
		}
		
		fileText = fileText.replaceAll(" ", "").replaceAll("\n", "");
		
		String[] strings = fileText.split("<" + node + ">");
		
		for(String string : strings)
		{
			int index = string.indexOf("</" + node + ">");
		
		}
		
		return results;
		
	}*/
}
