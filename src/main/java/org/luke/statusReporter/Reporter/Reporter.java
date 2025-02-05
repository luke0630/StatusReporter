package org.luke.statusReporter.Reporter;

import com.google.gson.Gson;
import org.luke.statusReporter.StatusReporter;
import org.manager.Library.Data.ReportData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Reporter {
    static final Integer timeout = 5;
    static HttpClient client;

    public static void Report(ReportData.ReportType reportType, String content) {
        if(reportType == ReportData.ReportType.PLAYER) {
            throw new IllegalArgumentException("PLAYER タイプの報告にはPlayerReportDataが必要です");
        } else {
            SendReport(reportType, content);
        }
    }

    public static void Report(ReportData.PlayerReportData playerReportData) {
        String data = new Gson().toJson(playerReportData);
        SendReport(ReportData.ReportType.PLAYER, data);
    }

    static void SendReport(ReportData.ReportType type, String content) {
        String url = String.format("http://%s/report", StatusReporter.address_webServer);

        ReportData reportData = new ReportData();
        reportData.setReportType(type);
        reportData.setContent(content);

        String jsonPayload = new Gson().toJson(reportData);
        if(client == null) {
            client = HttpClient.newHttpClient();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(timeout))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAcceptAsync(response -> {
                    if(response.statusCode() == 200) {
                        System.out.println("問題なく送信できました。");
                    }
                })
                .exceptionally(ex -> {
                    System.out.println("エラーが発生しました: " + ex.getMessage());
                    return null;
                });
    }
}
