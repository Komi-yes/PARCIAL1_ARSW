package com.example.Lab_0_PPCA.Services;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class APIService {
    private static HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private Map<String,String> cache;

    public APIService(){
        cache = new HashMap<>();
    }

    public String getInfo(){
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=IBM&apikey=Q3TLEZT0YDTXMIIR";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        try {
            if (cache.containsKey(url)){
                return cache.get(url);
            }
            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            cache.put(url, response.body());
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
