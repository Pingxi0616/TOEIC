package com.google.gson;
import java.io.*;
public class Gson {
    public String toJson(Object o) { return "{}"; }
    public <T> T fromJson(Reader r, java.lang.reflect.Type t) { return null; }
    public void toJson(Object o, Writer w) {}
}
