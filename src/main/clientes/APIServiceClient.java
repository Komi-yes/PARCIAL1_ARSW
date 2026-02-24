package edu.eci.arsw.tickets;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class APIServiceClient {
    private static HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public String getInfo(){
        String url = "http://localhost:8080/api/info";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        for (int i = 0; i<100; i++){
            Thread thread = new Thread(() -> {
                APIServiceClient client = new APIServiceClient();
                System.out.println(client.getInfo());
            });
            thread.start();
        }
    }
}
