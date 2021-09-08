package com.Bitgo.AncestralTreeCount.controller;

import com.Bitgo.AncestralTreeCount.exceptions.InvalidBlockException;
import com.Bitgo.AncestralTreeCount.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class MainController {
    @Autowired
    MainService mainService;

    @RequestMapping("/")
    public List<MainService.Pair> getNLargestAncestrySet() throws InvalidBlockException {
        return mainService.getNLargestAncestrySet(10);
    }
}
