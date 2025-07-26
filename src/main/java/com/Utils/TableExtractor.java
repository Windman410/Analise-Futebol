package com.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitária para extrair dados de tabelas HTML de forma genérica.
 * Esta classe foi projetada para ser modular e reutilizável em qualquer projeto
 * que utilize Selenium e Jsoup.
 */
public class TableExtractor {

    /**
     * Extrai os dados de uma tabela HTML de uma página web e converte-os para uma lista de mapas (JSON).
     * Este método assume que o WebDriver já navegou para a página desejada.
     *
     * @param driver A instância do WebDriver já inicializada e na página correta.
     * @param tableSelector O seletor CSS ou XPath para localizar a tabela na página.
     * (Recomenda-se usar seletores CSS com Jsoup, mas XPaths simples podem funcionar).
     * @return Uma lista de mapas, onde cada mapa representa uma linha da tabela (chave: cabeçalho, valor: dado da célula).
     * @throws Exception Se a tabela não for encontrada ou se ocorrer um erro durante a extração.
     */
    public static List<Map<String, String>> extractTableData(WebDriver driver, String tableSelector) throws Exception {
        System.out.println("--- Iniciando extração de tabela genérica ---");

        // --- Passo 1: Esperar que a tabela esteja visível na página ---
        // O método recebe o driver já na página correta, por isso apenas esperamos pelo elemento.
        try {
            System.out.println("[DEBUG] A aguardar até 10 segundos para a tabela '" + tableSelector + "' aparecer...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(tableSelector)));
            System.out.println("[DEBUG] Tabela encontrada na página pelo Selenium.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("A tabela com o seletor '" + tableSelector + "' não foi encontrada.", e);
        }

        // --- Passo 2: Extrair o HTML e processar com Jsoup ---
        Element tableElement;
        try {
            System.out.println("[DEBUG] Extraindo o HTML da pagina");
            Document soup = Jsoup.parse(driver.getPageSource());
            System.out.println("[DEBUG] processando com Jsoup");
            tableElement = soup.selectXpath(tableSelector).first();
            if (tableElement == null) {
                throw new Exception("Jsoup não conseguiu encontrar a tabela com o seletor '" + tableSelector + "'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erro ao extrair a pagina ou processar com o Jsoup");
        }

        // --- Passo 3: Extrair os cabeçalhos (headers) ---
        // Procura por 'th' dentro de 'thead' -> 'tr'.
        Elements headerElements = tableElement.select("thead tr th");
        List<String> headers = new ArrayList<>();
        for (Element header : headerElements) {
            headers.add(header.text().trim());
        }

        if (headers.isEmpty()) {
            throw new Exception("Nenhum cabeçalho (<th>) encontrado na tabela. Verifique a estrutura da tabela.");
        }
        System.out.println("[DEBUG] Cabeçalhos da tabela extraídos: " + headers);

        // --- Passo 4: Iterar pelas linhas (rows) e extrair os dados das células (cells) ---
        Elements rows = tableElement.select("tbody tr");
        System.out.printf("[DEBUG] Encontradas %d linhas no corpo da tabela.%n", rows.size());

        List<Map<String, String>> tableData = new ArrayList<>();
        for (Element row : rows) {
            Elements cells = row.select("td");
            // Usamos LinkedHashMap para garantir que a ordem das colunas no JSON é a mesma da tabela.
            Map<String, String> rowData = new LinkedHashMap<>();

            // Garante que só processamos linhas que têm um número de células compatível com os cabeçalhos.
            // Isto evita erros em tabelas com formatação complexa (rowspan/colspan).
            int limit = Math.min(headers.size(), cells.size());
            for (int i = 0; i < limit; i++) {
                String header = headers.get(i);
                String cellValue = cells.get(i).text().trim();
                rowData.put(header, cellValue);
            }

            if (!rowData.isEmpty()) {
                tableData.add(rowData);
            }
        }

        System.out.println("[DEBUG] Extração de dados da tabela concluída com sucesso.");
        return tableData;
    }
}

