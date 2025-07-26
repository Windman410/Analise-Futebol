package com.ProjetoFutebol;

import com.google.gson.Gson;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Configura o servidor para servir TODOS os ficheiros da pasta 'public'
        // Isto agora inclui a subpasta 'json'.
        Spark.staticFiles.location("/public");
        Spark.port(3000);
        Gson gson = new Gson();

        // A rota especial para /json/:fileName foi REMOVIDA.
        // O Spark irá agora servir os ficheiros JSON automaticamente.

        // Rota da API principal
        Spark.get("/api/stats", (req, res) -> {
            String team1Query = req.queryParams("team1");
            String team2Query = req.queryParams("team2");
            res.type("application/json");

            if (team1Query == null || team2Query == null || team1Query.isEmpty() || team2Query.isEmpty()) {
                res.status(400);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Dois nomes de times são obrigatórios.");
                return gson.toJson(error);
            }

            try {
                TeamData team1Data = Scraper.fetchTeamData(team1Query);
                TeamData team2Data = Scraper.fetchTeamData(team2Query);

                Map<String, TeamData> responseData = new HashMap<>();
                responseData.put("team1Data", team1Data);
                responseData.put("team2Data", team2Data);

                return gson.toJson(responseData);

            } catch (Exception e) {
                res.status(500);
                Map<String, String> error = new HashMap<>();
                error.put("error", e.getMessage());
                return gson.toJson(error);
            }
        });

        System.out.println("Servidor iniciado em http://localhost:3000");
    }
}
