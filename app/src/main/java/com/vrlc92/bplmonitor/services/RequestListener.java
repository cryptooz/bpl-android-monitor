package com.vrlc92.bplmonitor.services;

public interface RequestListener<T> {
    void onFailure(Exception e);
    void onResponse(T object);
}
