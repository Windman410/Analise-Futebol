package com.ProjetoFutebol;

import com.google.gson.Gson;
import spark.Spark;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Configura o servidor para servir ficheiros da pasta 'public'
        Spark.staticFiles.location("/public");
        Spark.port(3000);
        Gson gson = new Gson();

        // NOVA ROTA: Serve os ficheiros JSON a partir da pasta 'resources/json'
        Spark.get("/json/:fileName", (req, res) -> {
            String fileName = req.params(":fileName");
            res.type("application/json");

            // Usa o ClassLoader para ler um ficheiro a partir da pasta 'resources'
            try (InputStream is = Main.class.getResourceAsStream("/json/" + fileName)) {
                if (is == null) {
                    res.status(404);
                    return "{\"error\": \"Ficheiro não encontrado: " + fileName + "\"}";
                }
                // Converte o conteúdo do ficheiro para uma String
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\": \"Erro ao ler o ficheiro: " + fileName + "\"}";
            }
        });

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
