package com.bd.ssi.common;

import com.bd.ssi.auth.User;
import com.bd.ssi.common.mapper.MarketDBMappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestPages {
    @Autowired
    private MarketDBMappers marketDBMappers;

    @RequestMapping("/test_pages/page1")
    @ResponseBody
    public String age1(){
        User user = marketDBMappers.getUser(1);
        return "page1";
    }
}
