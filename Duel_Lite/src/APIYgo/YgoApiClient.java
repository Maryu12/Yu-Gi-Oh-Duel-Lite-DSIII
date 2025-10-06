package APIYgo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


public class YgoApiClient {

    //Url de seleccion
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
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(RANDOM_URL))
                        .header("User-Agent", "Java HttpClient")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    System.err.println("HTTP " + response.statusCode() + " — reintentando...");
                    continue;
                }

                JSONObject root = new JSONObject(response.body());
                JSONObject cardJson = root.has("data")
                        ? root.getJSONArray("data").getJSONObject(0)
                        : root;

                String type = cardJson.optString("type", "").toLowerCase();
                if (!type.contains("monster")) {
                    System.out.println("Carta no válida (no Monster), intento " + attempt);
                    continue;
                }

                String name = cardJson.optString("name", "Desconocida");
                int atk = Math.max(0, cardJson.optInt("atk", 0));
                int def = Math.max(0, cardJson.optInt("def", 0));

                String imageURL = "";
                if (cardJson.has("card_images")) {
                    JSONArray imgs = cardJson.getJSONArray("card_images");
                    if (imgs.length() > 0) {
                        imageURL = imgs.getJSONObject(0).optString("image_url", "");
                    }
                }

                System.out.println("Carta obtenida: " + name);
                return new Card(name, atk, def, imageURL);

            } catch (java.net.ConnectException ce) {
                throw new Exception("Error de conexión: no se pudo conectar con la API.");
            } catch (java.net.UnknownHostException ue) {
                throw new Exception("Error de red: verifica tu conexión a Internet.");
            } catch (Exception e) {
                System.err.println("Error al obtener carta: " + e.getMessage());
                if (attempt == MAX_ATTEMPTS)
                    throw new Exception("No se pudo obtener una carta después de varios intentos.");
            }
        }

        throw new Exception("Error desconocido al intentar obtener cartas.");
    }



    // numero de cartas aleatorias
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


