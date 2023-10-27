package com.medallia.webcrawler.model;

import lombok.Data;

@Data
public class DatabaseDetails {

    private int id;
    private String host;
    private String schema;
    private String user;
    private String password;

}
