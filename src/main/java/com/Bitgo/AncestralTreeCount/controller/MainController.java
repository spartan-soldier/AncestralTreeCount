package com.Bitgo.AncestralTreeCount.controller;

import com.Bitgo.AncestralTreeCount.service.MainService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MainController {
    @Autowired
    MainService mainService;

    @RequestMapping("/")
    public String getData(){
        return mainService.getData();
    }
}
