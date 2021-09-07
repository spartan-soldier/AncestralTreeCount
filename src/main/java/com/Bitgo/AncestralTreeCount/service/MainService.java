package com.Bitgo.AncestralTreeCount.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MainService {
    public String getData(){
        final String uri = "https://blockstream.info/api/blocks/680000";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);
        System.out.println(result);
        return  result;
    }
}
