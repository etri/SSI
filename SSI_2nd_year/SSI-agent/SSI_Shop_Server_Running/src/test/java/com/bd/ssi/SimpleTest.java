package com.bd.ssi;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class SimpleTest {

    @Test
    public void test(){
        String root = "/Users/BD";
        System.out.println(Paths.get(root, "idea").resolve("idea2"));
    }
}
