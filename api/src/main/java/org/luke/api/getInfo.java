package org.luke.api;

import org.json.JSONObject;
import org.luke.statusReporter.StatusReporter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class getInfo {
    final String webURL;
    public getInfo(String url) {
        this.webURL = url;
    }

    public String get() {
        try(HttpClient client = HttpClient.newHttpClient()) {
            // HttpClientの作成;
            // HTTPリクエストの作成
            String url = String.format("http://%s/status", webURL);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))  // JSONを取得するURLに変更
                    .build();

            // HTTPレスポンスの取得
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // レスポンスのステータスコードを確認
            if (response.statusCode() == 200) {
                // レスポンスボディをJSONとしてパース
                String responseBody = response.body();
                JSONObject json = new JSONObject(responseBody);

                return json.toString();
            } else {
                System.out.println("HTTP Error: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
