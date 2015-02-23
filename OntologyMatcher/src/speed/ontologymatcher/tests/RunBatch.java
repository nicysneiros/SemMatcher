package speed.ontologymatcher.tests;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import speed.ontologymatcher.facade.Facade;
import speed.ontologymatcher.semanticmatching.basics.ESSMFunction;

public class RunBatch {
	public static void main(String[] args) {
		
		//Diretório das ontologias.
		String directory = "C:\\Z\\CIn\\workspace\\eclipse34\\OntologyMatcher\\owls\\CarlosBug\\";
		
		//Arquivo com as LOs listadas.
		String filePath = System.getProperty("user.dir") + "\\Ontologias.txt";
		
		// Arquivo CMO.
		String fileCMO = directory + "CMO01-Education.owl";
		
		
		// Não mexer depois daqui============================
		
		ArrayList<String> files = new ArrayList<String>();
		
		try {  
            BufferedReader br = new BufferedReader (new FileReader (filePath)); 
            String linha;  
            while ((linha = br.readLine()) != null) {  
                files.add(linha);
            }  
            br.close();
        } catch (IOException ex) {  
            System.err.println("Erro ao ler arquivo.");
        }  
		
        ArrayList<String> results = new ArrayList<String>();
        
        FileOutputStream fout;		
        PrintStream ps;
        
        
		try
		{	
		    // Open an output stream
		    fout = new FileOutputStream (System.getProperty("user.dir") + "\\Resultados.txt");
		    ps = new PrintStream(fout);
		    
		    // Print a line of text
		    for(int i = 0; i < files.size(); i++)
	        {
	        	for(int j = i + 1; j < files.size(); j++)
	            {	
	        		System.out.println("CALCULANDO MATCHING ENTRE:" + files.get(i) + " e " + files.get(j));
	        		double result = Facade.getInstance().calculateSSM(directory + files.get(i), directory + files.get(j), fileCMO, ESSMFunction.Average, directory + "BestAlignments.owl");
	        		String text = "Matching entre " + files.get(i) + " e " + files.get(j) + ": " + result;
	        		ps.println(text);
	        		results.add(text);
	            }
	        	
	        }		    
		    fout.close();		
		}
		
		catch (IOException e)
		{
			System.err.println ("Erro ao escrever no arquivo.");
			System.exit(-1);
		}

        for(String str : results)
        	System.out.println(str);
        
        
        
        
		
	}
}
