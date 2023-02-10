package de.dataport.weather.data.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.dataport.weather.data.entity.SampleAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SampleAddressService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleAddressService.class);

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    public JsonObject getWeather(SampleAddress sampleAddress) {
        JsonObject location = getLocation(sampleAddress);
        String apiKey = getAPIKey();

        if (apiKey == null) {
            return null;
        }

        if (location != null) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://api.openweathermap.org/data/3.0/onecall?lat=" +
                                location.get("lat").getAsDouble() + "&lon=" +
                                location.get("lon").getAsDouble() + "&units=metric&exclude=minutely,hourly,daily,alerts&appid=" + apiKey))
                        .GET()
                        .build();
                HttpResponse<String> response = HttpClient.newBuilder()
                        .build()
                        .send(request, HttpResponse.BodyHandlers.ofString());
                LOGGER.info(response.body());
                return GSON.fromJson(response.body(), JsonObject.class);
            } catch (URISyntaxException | IOException | InterruptedException ignored) {
            }
        }
        return null;
    }

    private JsonObject getLocation(SampleAddress sampleAddress) {
        String requestCode = null;
        String apiKey = getAPIKey();

        if (apiKey == null) {
            return null;
        }

        if ((sampleAddress.getPostalCode() != null && !sampleAddress.getPostalCode().isEmpty()) && (sampleAddress.getCountry() != null && !sampleAddress.getCountry().isEmpty())) {
            requestCode = "https://api.openweathermap.org/geo/1.0/zip?zip=" +
                    sampleAddress.getPostalCode() + "," + sampleAddress.getCountry() + "&appid=" + apiKey;
        } else if ((sampleAddress.getCity() != null && !sampleAddress.getCity().isEmpty()) &&
                ((sampleAddress.getCountry() != null && !sampleAddress.getCountry().isEmpty()) ||
                        (sampleAddress.getState() != null && !sampleAddress.getState().isEmpty()))) {
            requestCode = "https://api.openweathermap.org/geo/1.0/direct?q=" +
                    sampleAddress.getCity() + ",";
            if (sampleAddress.getState() != null) {
                requestCode += sampleAddress.getState();
            } else {
                requestCode += sampleAddress.getCountry();
            }
            requestCode += "&limit=1&appid=" + apiKey;
        }

        if (requestCode != null) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(requestCode))
                        .GET()
                        .build();
                HttpResponse<String> response = HttpClient.newBuilder()
                        .build()
                        .send(request, HttpResponse.BodyHandlers.ofString());
                LOGGER.info(response.body());
                return GSON.fromJson(response.body(), JsonObject.class);
            } catch (URISyntaxException | IOException | InterruptedException ignored) {
            }
        }
        return null;
    }

    private String getAPIKey() {
        return System.getenv("api");
    }
}
