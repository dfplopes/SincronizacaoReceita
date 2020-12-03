//Cenário de Negócio:
// Todo o dia útil por volta das 6 horas da manhã um colaborador da retaguarda do Sicredi recebe e organiza
// as informações de contas para enviar ao Banco Central. Todas agencias e cooperativas enviam arquivos Excel à Retaguarda.
// Hoje o Sicredi já possiu mais de 4 milhões de contas ativas.
// Esse usuário da retaguarda exporta manualmente os dados em um arquivo CSV para ser enviada para a Receita Federal,
// antes as 10:00 da manhã na abertura das agências.
//
// Requisito:
// Usar o "serviço da receita" (fake) para processamento automático do arquivo.
//
// Funcionalidade:
// 0. Criar uma aplicação SprintBoot standalone. Exemplo: java -jar SincronizacaoReceita <input-file>
// 1. Processa um arquivo CSV de entrada com o formato abaixo.
// 2. Envia a atualização para a Receita através do serviço (SIMULADO pela classe ReceitaService).
// 3. Retorna um arquivo com o resultado do envio da atualização da Receita. Mesmo formato adicionando o resultado em uma nova coluna.
//
//
//Formato CSV:
//agencia;conta;saldo;status
//0101;12225-6;100,00;A
//0101;12226-8;3200,50;A
//3202;40011-1;-35,12;I
//3202;54001-2;0,00;P
//3202;00321-2;34500,00;B
//...

package br.com.dbserver.diego.sincronizacaoReceita;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger; 
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.dbserver.diego.sincronizacaoReceita.tools.CsvTools;


@SpringBootApplication
public class SincronizacaoReceita implements CommandLineRunner {

	static ReceitaService receitaService = new ReceitaService();
	Logger logger = Logger.getLogger(SincronizacaoReceita.class.getName());

    public static void main(String[] args) {

    	SpringApplication app = new SpringApplication(SincronizacaoReceita.class);
    	app.run(args);
    	
    }

    @Override
	public void run(String... args) throws Exception {
    	
    	logger.info("Começando processamento de arquvivo de contas com a Receita");
    	
        for (String arqIn : args) {
        	
        	List<List<String>> linhas = new ArrayList<>();
        	
        	logger.info("Validando o arquivo: " + arqIn);
        	// Valida se o parâmetro é um arquivo e se é de extensão csv
            if (CsvTools.validate(arqIn)) { 
    
            	// Define o arquivo de retorno com o mesmo nome do arquivo de entrada, adicionando _return antes da extensão            	
            	//Ex.: Arquivo de entrada = contas.csv / arquivo de retorno será conta_return.csv
            	
            	String arqOut = arqIn.replace(".csv", "_return.csv"); 
            	Boolean processStatus = true;
            	
            	//Armazena os dados do arquivo CSV numa lista de linhas(cada registro)
            	linhas = CsvTools.getContent(arqIn); 
            	
            	int countLin = 0;
            	
            	// Percorre a lista de linhas 
            	for(List<String> reg : linhas) {
            		countLin ++;
            		
            		// Primeira linha - header
            		// valida se o header é válido, se sim, adiciona uma nova coluna para o retorno do processamento e pula para a próxima linha
            		if (countLin == 1) {
            			if (!validateHeader(reg)) {
            				logger.warning("Erro no header do arquivo");
            				
            				processStatus = false;
            				break;
            			}
            			reg.add("Resultado Receita");
            			continue; 
            		}
            		
            		String numAg;
            		String numConta;
            		Double saldo;
            		String status;
            		Boolean resultado;
            		
            		//Valida a quantidade de colunas na linha
            		if (reg.size() != 4) {
        				reg.add("erro");
        				logger.warning("Problemas na linha " + countLin + ": Número de colunas inválido");
					}
            		else {
            			try {
            				//processa os valores das colunas e envia para o Serviço da Receita
            				numAg = reg.get(0);
            				numConta = reg.get(1).replace("-", "").replace(".", "");
            				saldo = Double.parseDouble(reg.get(2).replace(".", "").replace(",", "."));
            				status = reg.get(3);
            				resultado = receitaService.atualizarConta(numAg, numConta, saldo, status);
            				
            				reg.add(resultado.toString());
            				
            			} catch (Exception e) {
            				reg.add("erro");
            				logger.warning("Problemas na linha " + countLin + ": " + e.getMessage());
						}
            		}
            	}
            	
            	//Se finalizado com sucesso, gera arquivo de retorno
            	if (processStatus == true) {
                	logger.info("Leitura do arquivo de origem finalizada com sucesso");
                	logger.info("Gerando arquivo de retorno: " + arqOut);
            		CsvTools.createCsvFile(arqOut, linhas);
            	}
            	
            	
            } else {
				logger.warning("Arquivo inválido (" + arqIn + ")");
            }
        }
    	logger.info("Processamento finalizado!");
	}

    // Metodo para validação do Header
	static Boolean validateHeader(List<String> header) {
    	List<String> validHeaders = new ArrayList<String>();
    	
    	validHeaders.add("agencia");
    	validHeaders.add("conta");
    	validHeaders.add("saldo");
    	validHeaders.add("status");
    	
    	if (header.size() != 4) return false;
    	
    	for(String field: header) {
    		if (!validHeaders.contains(field.toLowerCase())) return false;
    	}
    	
    	return true;
    }
   
}
