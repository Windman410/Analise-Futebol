package com.ProjetoFutebol;

import java.util.List;

// Esta classe serve como um contentor de dados (Data Transfer Object - DTO)
// para armazenar as estatísticas de forma organizada.
public class Stats {
    public String teamName;
    public int gamesPlayed;
    public int wins;
    public  int draws;
    public int losses;
    public  int goalsFor;
    public  int goalsAgainst;
    public List<String> players;

    // Getters e Setters podem ser adicionados se necessário,
    // mas para este projeto, o acesso direto aos campos é suficiente.
}
