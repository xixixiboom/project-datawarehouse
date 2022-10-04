package com.example.exceldemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestTest {
    @Autowired
    Test test;

    public void main(){
        System.out.println(test.getMap());
    }

}
