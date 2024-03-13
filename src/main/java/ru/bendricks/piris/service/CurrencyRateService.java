package ru.bendricks.piris.service;

import lombok.Getter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Getter
public class CurrencyRateService {

    private double usdSellInByn;
    private double usdBuyInByn;
    private double eurSellInByn;
    private double eurBuyInByn;

    public void updateRates() {
        ParameterizedTypeReference<Map<String, String>> responseType =
                new ParameterizedTypeReference<>() {};
        RestTemplate restTemplate = new RestTemplate();
        RequestEntity<Void> request = RequestEntity.get("https://api.nbrb.by/exrates/rates/USD?parammode=2")
                .accept(MediaType.APPLICATION_JSON).build();
        Map<String, String> jsonDictionary = restTemplate.exchange(request, responseType).getBody();
        var usd = Double.parseDouble(jsonDictionary.getOrDefault("Cur_OfficialRate", "0.0"));
        request = RequestEntity.get("https://api.nbrb.by/exrates/rates/EUR?parammode=2")
                .accept(MediaType.APPLICATION_JSON).build();
        jsonDictionary = restTemplate.exchange(request, responseType).getBody();
        var eur = Double.parseDouble(jsonDictionary.getOrDefault("Cur_OfficialRate", "0.0"));
        usdSellInByn = usd * 1.05;
        usdBuyInByn = usd * 0.95;
        eurSellInByn = eur * 1.05;
        eurBuyInByn = eur * 0.95;
    }

}
