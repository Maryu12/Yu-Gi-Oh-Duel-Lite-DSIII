package APIYgo;

import org.json.JSONObject;
import org.json.JSONException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;



public class YgoApiClient {

    //Url de seleccion aleatoria
    private static final String RANDOM_URL = "https://db.ygoprodeck.com/api/v7/randomcard.php";
    private final HttpClient client;

    public YgoApiClient() {
        this.client = HttpClient.newHttpClient();
    }

    //Carta aleatoria
    public Card getRandomCard() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RANDOM_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode()!= 200){ throw new RuntimeException("Error HTTP: " + response.statusCode());}


        JSONObject json = new JSONObject(response.body());

        if (!json.has("name")) {throw new JSONException("El JSON no contiene name. "); }

        //Datos (ataque, defensa y nombre)
        String name = json.getString("name");
        int atk = json.has("atk") ? json.getInt("atk") : 0;
        int def = json.has("def") ? json.getInt("def") : 0;

        if (!json.has ("card_images")){
            throw new JSONException("El JSON no contiene imagenes");
        }

        // Imagen
        String imageURL = json.getJSONArray("card_images")
                .getJSONObject(0)
                .getString("image_url");

        return new Card(name, atk, def, imageURL);
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


