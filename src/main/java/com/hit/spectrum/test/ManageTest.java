package com.hit.spectrum.test;

import com.hit.spectrum.Api;

import java.util.List;

public class ManageTest {
    public static void main(String[] args) {
        Api api = new Api();
//        System.out.println(api.getAll());
//        System.out.println(api.getStandardById(1L));
        System.out.println(api.deleteById(1L));
    }
}
