package ru.bendricks.piris.model;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public enum Currency {

    BYN("BYN"),USD("USD"),EUR("EUR");

    private final String curCode;

    Currency(String cur) {
        curCode = cur;
    }

    public Double getRate() {
        if (curCode.equals("BYN"))
            return 1.0;
        ParameterizedTypeReference<Map<String, String>> responseType =
                new ParameterizedTypeReference<>() {};
        RestTemplate restTemplate = new RestTemplate();
        RequestEntity<Void> request = RequestEntity.get("https://api.nbrb.by/exrates/rates/" + curCode + "?parammode=2")
                .accept(MediaType.APPLICATION_JSON).build();
        Map<String, String> jsonDictionary = restTemplate.exchange(request, responseType).getBody();
        return Double.parseDouble(jsonDictionary.get("Cur_OfficialRate"));
    }

}
