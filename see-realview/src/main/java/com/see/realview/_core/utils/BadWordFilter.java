package com.see.realview._core.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BadWordFilter {

    private final WebDatabaseReader webDatabaseReader;


    public BadWordFilter(@Autowired WebDatabaseReader webDatabaseReader) {
        this.webDatabaseReader = webDatabaseReader;
    }


}
