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
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class getInfo {
    public static void isOnline(String serverName) {
        JSONObject status = new JSONObject(getStatusJSON());
        status.getBoolean(serverName);
    }
    public String getStatusJSON() {
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

                return String.valueOf(new JSONObject(responseBody));
            } else {
                System.out.println("HTTP Error: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public List<DynamicServerData> getStatusList() {
        String string_status = getStatusJSON();

        List<DynamicServerData> list = new ArrayList<>();

        if(!string_status.isEmpty()) {
            JSONObject status = new JSONObject(string_status);

            for(String key : status.keySet()) {
                JSONObject serverStatus = status.getJSONObject(key);
                JSONObject serverData = serverStatus.getJSONObject("serverData");

                Gson gson = new Gson();
                DynamicServerData data = gson.fromJson(serverData.toString(), DynamicServerData.class);
                list.add( data );
            }
        }
        return list;
    }
    public DynamicServerData getStatusByServerData(String serverName) {
         String string_status = getStatusJSON();
         if(string_status != null) {
             JSONObject status = new JSONObject(string_status);

             for(String key :  status.keySet()) {
                 JSONObject json_serverData = status.getJSONObject(key);
                 JSONObject serverData = json_serverData.getJSONObject("serverData");
                 if(serverData.getString("serverName").equals(serverName)) {
                     Gson gson = new Gson();
                     return gson.fromJson(serverData.toString(), DynamicServerData.class);
                 }
             }
         }
         return null;
    }
}

