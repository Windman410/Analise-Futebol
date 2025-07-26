package com.ProjetoFutebol;

import com.Services.ScrapingService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class Scraper {

    public static Stats fetchTeamStats(String teamQuery) throws Exception {
        System.out.printf("%n--- Iniciando busca para: %s ---%n", teamQuery);

        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        WebDriver driver = new FirefoxDriver(options);

        // Cria uma instância do nosso serviço de scraping
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

            // Etapa 4: Aceder à página do time e extrair os dados
            driver.get(teamUrl);
            Stats stats = scrapingService.extractStatsFromTeamPage();

            if (stats.gamesPlayed == 0) {
                throw new Exception("SCRAPING_FAILED: O número de jogos extraído foi zero ou não foi encontrado.");
            }

            System.out.printf("--- Busca para %s concluída com sucesso. ---%n", teamQuery);
            return stats;

        } finally {
            driver.quit();
            System.out.println("[DEBUG] Driver do Selenium encerrado.");
        }
    }
}
