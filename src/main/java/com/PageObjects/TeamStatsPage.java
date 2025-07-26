package com.PageObjects;
import org.openqa.selenium.By;

// Esta classe armazena os localizadores dos elementos da página de estatísticas do time.
public class TeamStatsPage {

    public static final By MATCHLOG_TABLE = By.cssSelector("table[id^=matchlogs_for]");

    // Como estes são usados pelo Jsoup depois de o HTML ser carregado, mantemo-los como Strings.
    public static final String TEAM_NAME_SELECTOR = "h1 span[itemprop=name]";
    public static final String SUMMARY_ROW_SELECTOR = "table[id^=matchlogs_for] tfoot tr:nth-of-type(2)";
    public static final String PLAYERS_SELECTOR = "table[id^=stats_standard_] tbody th[data-stat=player] a";

}
