package org.luke.statusReporter.API;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;
import org.json.JSONObject;
import org.luke.statusReporter.StatusReporter;
import org.luke.statusReporter.DynamicServerData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class getInfo {
    public String test() {
        return "test";
    }
    public JSONObject getStatusJSON() {
        try(HttpClient client = HttpClient.newHttpClient()) {
            // HttpClientの作成;
            // HTTPリクエストの作成
            String url = String.format("http://%s/status", StatusReporter.address_webServer);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))  // JSONを取得するURLに変更
                    .build();

            // HTTPレスポンスの取得
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // レスポンスのステータスコードを確認
            if (response.statusCode() == 200) {
                // レスポンスボディをJSONとしてパース
                String responseBody = response.body();

                return new JSONObject(responseBody);
            } else {
                System.out.println("HTTP Error: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public DynamicServerData getStatusByServerData(String serverName) {

         JSONObject status = getStatusJSON();
         if(status != null) {
            status.getJSONObject(serverName);

            Gson gson = new Gson();
            return gson.fromJson(status.toString() ,DynamicServerData.class);
         }
         return null;
    }
}
