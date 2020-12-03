package br.com.dbserver.diego.sincronizacaoReceita.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CsvTools {
	
	static final String SEPARADOR_CSV = ";";

	// Valida o arquivo se é um arquivo mesmo e se é do tipo CSV
	static public Boolean validate(String fileName) {
		
		File csvFile = new File(fileName);
		
		if(!csvFile.exists() || csvFile.isDirectory()) 
			return false;
		
		if (!fileName.substring(fileName.length() - 3, fileName.length()).toLowerCase().equals( "csv") )
			return false;
		
		return true;
	}
	
	//Le o arquivo CSV e armazena os dados
	static public List<List<String>> getContent(String fileName){
		List<List<String>> csvContent = new ArrayList<List<String>>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

        	String line;

            while ((line = br.readLine()) != null) {
            		
            	String[] values = line.split(SEPARADOR_CSV);
            	List<String> linVals = new ArrayList<String>();
            	
            	for (String val : values) {
            		linVals.add(val);
            	}
            	
                csvContent.add(linVals);                    
            }
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return csvContent;
	}
	
	
	//Cria novo arquivo CSV, usando dados armazenados em uma lista
	static public void createCsvFile(String fileName, List<List<String>> content) {
		try (PrintWriter pw = new PrintWriter(new File(fileName)) ){
			StringBuilder contentBuilder = new StringBuilder();
		 
			for(List<String> linha : content) {
				for(String valCell : linha) {
					contentBuilder.append(valCell + SEPARADOR_CSV);
				}
				contentBuilder.append('\n');
			}
	        pw.write(contentBuilder.toString());
	        pw.close();
		        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
	}
}
