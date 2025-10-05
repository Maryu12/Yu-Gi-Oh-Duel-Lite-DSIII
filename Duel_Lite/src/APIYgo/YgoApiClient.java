package APIYgo;

import org.json.JSONObject;
import org.json.JSONException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;


public class YgoApiClient {

    //Url de seleccion aleatoria
    private static final String RANDOM_URL = "https://db.ygoprodeck.com/api/v7/randomcard.php/";
    private final HttpClient client;


    public YgoApiClient() {
        //soporte para redirecciones HTTP
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    //Carta aleatoria, tener en cuenta esta seccion ya que aca tiene ya argumentos definidos para la Clase Card
    public Card getRandomCard() throws Exception {
        final int MAX_ATTEMPTS = 8;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RANDOM_URL))
                    .header("User-Agent", "Java HttpClient") // ayuda a evitar bloqueos
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Código HTTP: " + response.statusCode());
            System.out.println("que vaina esta saliendo?: " + response.body());

            if (response.statusCode() != 200) {
                System.err.println("HTTP " + response.statusCode() + " — reintentando...");
                continue;
            }

            JSONObject root = new JSONObject(response.body());
            JSONObject cardJson = null;

            // Si la API devuelve { "data": [ {...} ], ... } -> usar data[0]
            if (root.has("data")) {
                JSONArray arr = root.getJSONArray("data");
                if (arr.length() == 0) {
                    System.err.println("data vacío, reintentando...");
                    continue;
                }
                cardJson = arr.getJSONObject(0);
            } else {
                // Caso en que la API retorne un objeto carta directo
                cardJson = root;
            }

            // Obtener el tipo de la carta (con fallback)
            String type = cardJson.optString("type", cardJson.optString("humanReadableCardType", ""));
            if (type == null) type = "";

            // Filtrar solo cartas Monster
            if (!type.toLowerCase().contains("monster")) {
                System.out.println("No es Monster (tipo = \"" + type + "\"), intento " + attempt + " de " + MAX_ATTEMPTS);
                continue;
            }

            // Obtener campos seguros
            String name = cardJson.optString("name", "Unknown");
            int atk = cardJson.has("atk") ? cardJson.optInt("atk", 0) : 0;
            int def = cardJson.has("def") ? cardJson.optInt("def", 0) : 0;
            // evitar valores negativos
            if (atk < 0) atk = 0;
            if (def < 0) def = 0;

            String imageURL = "";
            if (cardJson.has("card_images")) {
                JSONArray imgs = cardJson.getJSONArray("card_images");
                if (imgs.length() > 0) {
                    imageURL = imgs.getJSONObject(0).optString("image_url", "");
                }
            }

            System.out.println(" Carta obtenida: " + name + " (ATK: " + atk + ", DEF: " + def + ")");
            return new Card(name, atk, def, imageURL);
        }

        throw new RuntimeException("No se obtuvo carta Monster tras " + MAX_ATTEMPTS + " intentos");
    }


    //Obtener X numero de cartas aleatorias (Cambiarlo para generar las 3)
    public List<Card> getRandomCards(int count) throws Exception {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
            hand.add(getRandomCard());}
            catch (Exception e) {System.err.println ("No se pudo obtener una carta, " + e.getMessage());
            }
        }
        return hand;
    }
}


