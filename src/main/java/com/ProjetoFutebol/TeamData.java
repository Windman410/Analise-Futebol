package com.ProjetoFutebol;

import java.util.List;
import java.util.Map;

// Esta classe armazena todos os dados extraídos para um único time.
public class TeamData {
    String teamName;
    // Cada tabela é uma lista de mapas (linhas)
    Map<String, List<Map<String, String>>> tables;

    public TeamData(String teamName, Map<String, List<Map<String, String>>> tables) {
        this.teamName = teamName;
        this.tables = tables;
    }
}
