package com.ProjetoFutebol;

import com.google.gson.Gson;
import spark.Spark;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Define a porta em que o servidor irá rodar
        Spark.port(3000);
        Gson gson = new Gson();

        // --- ROTAS PARA SERVIR FICHEIROS ESTÁTICOS ---
        // Esta é a forma correta de servir ficheiros de dentro de um JAR.

        // Rota para a página principal
        Spark.get("/", (req, res) -> {
            res.type("text/html");
            return readResource("/public/index.html");
        });

        // Rota para os ficheiros CSS e JS
        Spark.get("/:fileName", (req, res) -> {
            String fileName = req.params(":fileName");
            if (fileName.endsWith(".css")) {
                res.type("text/css");
            } else if (fileName.endsWith(".js")) {
                res.type("application/javascript");
            }
            return readResource("/public/" + fileName);
        });

        // Rota para os ficheiros JSON com depuração melhorada
        Spark.get("/json/:fileName", (req, res) -> {
            String fileName = req.params(":fileName");
            System.out.println("\n[DEBUG] Recebido pedido para o ficheiro JSON: " + fileName);
            res.type("application/json; charset=utf-8");

            try {
                String content = readResource("/public/json/" + fileName);
                // Imprime uma amostra do conteúdo para verificação
                String contentSample = content.length() > 100 ? content.substring(0, 100) + "..." : content;
                System.out.println("[DEBUG] Amostra do conteúdo de '" + fileName + "': " + contentSample.replaceAll("\n", ""));
                return content;
            } catch (Exception e) {
                System.err.println("[ERRO] Falha ao ler o recurso: " + e.getMessage());
                res.status(404);
                return "{\"error\": \"Ficheiro JSON não encontrado no servidor: " + fileName + "\"}";
            }
        });


        // --- ROTA DA API PRINCIPAL ---
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

    /**
     * Função auxiliar para ler um ficheiro a partir da pasta 'resources'.
     * @param path O caminho para o ficheiro dentro de 'resources'.
     * @return O conteúdo do ficheiro como uma String.
     * @throws Exception Se o ficheiro não for encontrado.
     */
    private static String readResource(String path) throws Exception {
        // Procura pelo ficheiro dentro do JAR/classpath
        try (InputStream is = Main.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new Exception("Recurso não encontrado no classpath: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
