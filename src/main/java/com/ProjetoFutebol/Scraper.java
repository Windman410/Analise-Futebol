package com.ProjetoFutebol;

import com.PageObjects.TeamStatsPage;
import com.Services.ScrapingService;
import com.Utils.TableExtractor;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scraper {

    // O método foi atualizado para devolver um objeto TeamData, que contém múltiplas tabelas.
    public static TeamData fetchTeamData(String teamQuery) throws Exception {
        System.out.printf("%n--- Iniciando busca para: %s ---%n", teamQuery);

        // --- Configuração do Selenium para o Servidor (Render) ---
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        // CORREÇÃO: Lógica para adaptar o ambiente
        // A Render define a variável de ambiente "RENDER" automaticamente.
        if (System.getenv("RENDER") == null) {
            // Se NÃO estamos na Render (ambiente local), usa o WebDriverManager.
            System.out.println("[DEBUG] Ambiente local detetado. A usar o WebDriverManager...");
            WebDriverManager.chromedriver().setup();
        } else {
            // Se ESTAMOS na Render, assume que o ChromeDriver está no PATH.
            System.out.println("[DEBUG] Ambiente da Render detetado. A usar o ChromeDriver do sistema.");
        }

        WebDriver driver = new ChromeDriver(options);

        ScrapingService scrapingService = new ScrapingService(driver);

        try {
            // Etapa 1: Aceder à página de busca
            driver.get("https://fbref.com/en/search/search.fcgi?search=" + teamQuery);
            System.out.println("[DEBUG] Acedida página inicial de busca.");

            // Etapa 2: Lidar com cookies
            scrapingService.acceptCookies();

            // Etapa 3: Encontrar o URL do time
            String teamUrl = scrapingService.findMaleTeamUrlOnSearchPage();

            if (teamUrl == null) {
                throw new Exception("NOT_FOUND: Não foi possível encontrar um link para a equipa masculina de '" + teamQuery + "'.");
            }

            // Etapa 4: Aceder à página do time
            driver.get(teamUrl);

            // Etapa 5: Extrair o nome do time
            System.out.println("[DEBUG] Aguardando entrar na pagina do time");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            System.out.println("[DEBUG] site atual: " + driver.getCurrentUrl());
            System.out.println("[DEBUG] Verificando se esta na pagina do time e extraindo seu nome");
            WebElement teamNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(TeamStatsPage.TEAM_NAME_SELECTOR)));
            String teamName = teamNameElement.getText();

            // Etapa 6: Extrair todas as tabelas desejadas usando o TableExtractor
            Map<String, List<Map<String, String>>> extractedTables = new HashMap<>();
            List<Map<String, String>> statsData;
            System.out.println("[DEBUG] A extrair a tabela 'Standard Stats'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Standard Stats')]/../../div/table");
            extractedTables.put("Standard Stats", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Scores & Fixtures'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Scores & Fixtures')]/../../div/table");
            extractedTables.put("Scores & Fixtures", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Goalkeeping'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Goalkeeping')]/../../div/table");
            extractedTables.put("Goalkeeping", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Advanced Goalkeeping'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Advanced Goalkeeping')]/../../div/table");
            extractedTables.put("Advanced Goalkeeping", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Shooting'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Shooting')]/../../div/table");
            extractedTables.put("Shooting", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Passing'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Passing')]/../../div/table");
            extractedTables.put("Passing", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Pass Types'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Pass Types')]/../../div/table");
            extractedTables.put("Pass Types", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Goal and Shot Creation'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Goal and Shot Creation')]/../../div/table");
            extractedTables.put("Goal and Shot Creation", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Defensive Actions'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Defensive Actions')]/../../div/table");
            extractedTables.put("Defensive Actions", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Possession'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Possession')]/../../div/table");
            extractedTables.put("Possession", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Playing Time'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Playing Time')]/../../div/table");
            extractedTables.put("Playing Time", statsData);

            System.out.println("[DEBUG] A extrair a tabela 'Miscellaneous Stats'...");
            statsData = TableExtractor.extractTableData(driver, "//h2[contains(text(),'Miscellaneous Stats')]/../../div/table");
            extractedTables.put("Miscellaneous Stats", statsData);


            if (statsData.isEmpty()) {
                throw new Exception("SCRAPING_FAILED: Nenhuma tabela de dados foi extraída com sucesso.");
            }

            System.out.printf("--- Busca para %s concluída com sucesso. ---%n", teamQuery);
            // Retorna o objeto TeamData com o nome e o mapa de tabelas
            return new TeamData(teamName, extractedTables);

        } finally {
            driver.quit();
            System.out.println("[DEBUG] Driver do Selenium encerrado.");
        }
    }
}
