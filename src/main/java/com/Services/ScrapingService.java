package com.Services;

import com.ProjetoFutebol.Stats;
import com.PageObjects.SearchPage;
import com.PageObjects.TeamStatsPage;
import com.Utils.TableExtractor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Esta classe contém toda a lógica de negócio e as ações de scraping.
// Ela utiliza os localizadores definidos nas classes PageObjects.
public class ScrapingService {

    private WebDriver driver;
    private WebDriverWait wait;

    public ScrapingService(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void acceptCookies() {
        try {
            System.out.println("[DEBUG] A procurar e a aceitar o banner de cookies...");
            wait.withTimeout(Duration.ofSeconds(5)); // Define um timeout mais curto para os cookies
            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(SearchPage.COOKIE_ACCEPT_BUTTON));
            acceptButton.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(SearchPage.COOKIE_BANNER));
            System.out.println("[DEBUG] Banner de cookies tratado.");
        } catch (TimeoutException e) {
            System.out.println("[DEBUG] Banner de cookies não encontrado ou já aceite.");
        } finally {
            wait.withTimeout(Duration.ofSeconds(10)); // Restaura o timeout padrão
        }
    }

    public String findMaleTeamUrlOnSearchPage() {
        System.out.println("[DEBUG] A aguardar pelo contentor de resultados da busca...");
        wait.until(ExpectedConditions.presenceOfElementLocated(SearchPage.SEARCH_RESULTS_CONTAINER));
        System.out.println("[DEBUG] Contentor de resultados da busca visível.");

        List<WebElement> results = driver.findElements(SearchPage.SEARCH_RESULT_ITEM);
        System.out.printf("[DEBUG] Encontrados %d resultados. A procurar pela equipa masculina...%n", results.size());

        for (WebElement result : results) {
            if (result.getText().contains("Gender: Male")) {
                System.out.println("[DEBUG] Encontrado resultado correspondente a 'Gender: Male'.");
                try {
                    WebElement linkElement = result.findElement(SearchPage.TEAM_LINK_IN_RESULT);
                    String url = linkElement.getAttribute("href");
                    System.out.println("[DEBUG] Link da equipa masculina encontrado: " + url);
                    return url;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public String extractStatsFromTeamPage() throws Exception {
        System.out.println("[DEBUG] A aguardar pela tabela de jogos na página do time...");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(),'Scores & Fixtures')]/../../div/table")));
        System.out.println("[DEBUG] Tabela de jogos visível.");

        // 1. Defina o seletor da tabela que quer extrair
        String seletorDaTabelaDeJogadores = "//h2[contains(text(),'Scores & Fixtures')]/../../div/table";

        // 2. Chame o método genérico de extração
        List<Map<String, String>> dadosDosJogadores = TableExtractor.extractTableData(this.driver, seletorDaTabelaDeJogadores);

        // 3. Converta o resultado para JSON para usar na sua API
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(dadosDosJogadores);

        // 4. Agora pode usar o 'jsonOutput'
        System.out.println("--- Tabela de Jogadores em JSON ---");
        System.out.println(jsonOutput);

        return jsonOutput;
    }
}

