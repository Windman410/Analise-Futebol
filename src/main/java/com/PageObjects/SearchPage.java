package com.PageObjects;

import org.openqa.selenium.By;

// Esta classe segue o padrão Page Object estrito.
// A sua única responsabilidade é armazenar os localizadores (seletores) dos elementos da página.
// Não contém nenhuma lógica de interação.
public class SearchPage {

    public static final By COOKIE_ACCEPT_BUTTON = By.cssSelector("button.osano-cm-accept");
    public static final By COOKIE_BANNER = By.cssSelector("div.osano-cm-window");
    public static final By SEARCH_RESULTS_CONTAINER = By.id("sh_squads");
    public static final By SEARCH_RESULT_ITEM = By.cssSelector(".search-item");
    public static final By TEAM_LINK_IN_RESULT = By.cssSelector(".search-item-name strong a");

}


