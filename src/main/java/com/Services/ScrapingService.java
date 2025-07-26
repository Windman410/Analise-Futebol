package com.Services;

import com.ProjetoFutebol.Stats;
import com.PageObjects.SearchPage;
import com.PageObjects.TeamStatsPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
                    // Ignora e continua se este item não tiver o link esperado
                }
            }
        }
        return null;
    }

    public Stats extractStatsFromTeamPage() {
        System.out.println("[DEBUG] A aguardar pela tabela de jogos na página do time...");
        wait.until(ExpectedConditions.presenceOfElementLocated(TeamStatsPage.MATCHLOG_TABLE));
        System.out.println("[DEBUG] Tabela de jogos visível.");

        Document soup = Jsoup.parse(driver.getPageSource());
        Stats stats = new Stats();

        Element teamNameTag = soup.selectFirst(TeamStatsPage.TEAM_NAME_SELECTOR);
        stats.teamName = (teamNameTag != null) ? teamNameTag.text() : "Nome não encontrado";

        Element summaryRow = soup.selectFirst(TeamStatsPage.SUMMARY_ROW_SELECTOR);
        if (summaryRow != null) {
            Elements cols = summaryRow.select("td");
            try {
                stats.gamesPlayed = Integer.parseInt(cols.get(0).text());
                stats.wins = Integer.parseInt(cols.get(1).text());
                stats.draws = Integer.parseInt(cols.get(2).text());
                stats.losses = Integer.parseInt(cols.get(3).text());
                stats.goalsFor = Integer.parseInt(cols.get(5).text());
                stats.goalsAgainst = Integer.parseInt(cols.get(6).text());
            } catch (Exception e) {
                System.out.println("[DEBUG] ERRO: Falha ao ler as colunas da linha de resumo: " + e.getMessage());
            }
        }

        Elements playerElements = soup.select(TeamStatsPage.PLAYERS_SELECTOR);
        List<String> players = new ArrayList<>();
        if (!playerElements.isEmpty()) {
            for (int i = 0; i < Math.min(5, playerElements.size()); i++) {
                players.add(playerElements.get(i).text());
            }
        }
        stats.players = players;

        return stats;
    }
}

