//package com.iconloop.iitpvault.controller;
//
//import com.iconloop.iitpvault.utils.AES;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//
//@RestController
//@Slf4j
//@RequiredArgsConstructor
//@RequestMapping("v1/test")
//public class TestController {
//
//    @GetMapping("/decrypt/{value}")
//    public ResponseEntity testDecrypt(@PathVariable("value") String value) {
//        return ResponseEntity.ok().body(AES.decrypt(value));
//    }
//
//}
