package com.ProjetoFutebol;

import com.google.gson.Gson;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Configura o servidor para servir ficheiros da pasta 'public'
        Spark.staticFiles.location("/public");
        // Define a porta
        Spark.port(3000);

        // Cria uma instância do Gson para converter objetos para JSON
        Gson gson = new Gson();

        // Define a rota da API '/api/stats'
        Spark.get("/api/stats", (req, res) -> {
            String teamQuery = req.queryParams("team");
            res.type("application/json");

            if (teamQuery == null || teamQuery.isEmpty()) {
                res.status(400);
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("message", "Requisição Inválida");
                error.put("details", "O nome do time é obrigatório.");
                return gson.toJson(error);
            }

            try {
                Stats stats = Scraper.fetchTeamStats(teamQuery);
                return gson.toJson(stats);
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                if (e.getMessage().startsWith("NOT_FOUND")) {
                    res.status(404);
                    error.put("code", 404);
                    error.put("message", "Time Não Encontrado");
                } else if (e.getMessage().startsWith("SCRAPING_FAILED")) {
                    res.status(500);
                    error.put("code", 500);
                    error.put("message", "Falha ao Extrair Dados");
                } else {
                    res.status(503);
                    error.put("code", 503);
                    error.put("message", "Erro de Comunicação");
                }
                error.put("details", e.getMessage());
                return gson.toJson(error);
            }
        });

        System.out.println("Servidor iniciado em http://localhost:3000");
    }
}
