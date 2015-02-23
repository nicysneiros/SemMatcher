package speed.ontologymatcher.util.sparql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import speed.ontologymatcher.semanticmatching.basics.Alignment;
import speed.ontologymatcher.util.OntologyMatcherProperties;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * Classe que gerencia armazenamento de ontologias.
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public class StorageManager {
	
	/**
	 * Método que gera um arquivo RDF a partir dos matching semânticos.
	 * @param alignments Alinhamentos semânticos.
	 * @param outputPath Caminho do arquivo de saída.
	 * @throws FileNotFoundException Caso o arquivo não consiga ser criado.
	 */
	public static void saveSemanticAlignmentsToRDF(ArrayList<Alignment> alignments, String outputPath)
			throws FileNotFoundException {
		
		Model model = Alignment.createJenaModelFromAlignment(alignments);
		FileOutputStream stream = new FileOutputStream(new File(outputPath));
		model.write(stream);
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Método que gera um arquivo com informações dos alinhamentos.
	 * @param alignments Alinhamentos.
	 * @param outputPath Caminho do arquivo de saída.
	 */
	public static void saveToFile(ArrayList<Alignment> alignments,
			String outputPath) {
		String content = "";
		
		for (Alignment statement : alignments) {
		
			content += "<Match>\n" +
						"\t<Subject>" + statement.getSubject() + "</Subject>\n" +
						"\t<Predicate>" + statement.getPredicate() + "</Predicate>\n" +
						"\t<Object>" + statement.getObject() + "</Object>\n" +
						"\t<Similarity>" + (statement.getWeight() == -1.0 ? "Indefinido" : statement.getWeight()) + "</Similarity>\n" +
						"</Match>\n";
		}
		
		try {

			FileWriter fileWriter = new FileWriter(outputPath, false);			
			fileWriter.write(content); 
			fileWriter.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Método que salva os alinhamentos semânticos no banco de dados.
	 * @param alignments Alinhamentos semânticos.
	 * @param schemaName Nome do esquema no banco.
	 */
	public static void saveToDB(ArrayList<Alignment> alignments, String schemaName) {

		Model model = Alignment.createJenaModelFromAlignment(alignments);
		
		try {
			Class.forName(OntologyMatcherProperties.DB_DRIVER);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ModelMaker maker = null;

		try {
			IDBConnection conn = new DBConnection(OntologyMatcherProperties.DB_URL,
					OntologyMatcherProperties.DB_USER, OntologyMatcherProperties.DB_PASSWD, OntologyMatcherProperties.DB);
			maker = ModelFactory.createModelRDBMaker(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}

		maker.createModel(schemaName);

		maker.getModel(schemaName).add(model);		

	}

}
