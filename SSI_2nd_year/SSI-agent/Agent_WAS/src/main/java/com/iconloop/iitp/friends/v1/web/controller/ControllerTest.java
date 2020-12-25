package com.iconloop.iitp.friends.v1.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ControllerTest {

    @GetMapping("/checkhealth")
    /**
     *
     */
    public String test1() {
        return "{\"alive\":true}";
    }

}
