package com.vrlc92.bplmonitor.models;

/**
 * Created by victorlins on 21/03/17.
 */

import java.util.Arrays;
import java.util.List;

public enum Server {
    mainnet1(0, "mainnet 1", "http://13.56.163.57:4001/api/"),
    mainnet2(1, "mainnet 2", "http://54.183.132.15:4001/api/"),
    custom(2, "Custom", "");

    private final int id;
    private final String name;
    private final String apiAddress;

    Server(int id, String name, String apiAddress) {
        this.id = id;
        this.name = name;
        this.apiAddress = apiAddress;
    }

    public static Server fromId(int id){
        switch (id){
            case 0:
                return mainnet1;
            case 1:
                return mainnet2;
            default:
                return null;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApiAddress() {
        return apiAddress;
    }

    public boolean isCustomServer() {
        return id == Server.custom.getId();
    }

    public static List<String> getServers() {
        return Arrays.asList(
                Server.mainnet1.name,
                Server.mainnet2.name,
                Server.custom.name);
    }
}