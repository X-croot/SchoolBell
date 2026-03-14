package com.xcr;

public class BackupEntry {
    String name;
    long time, size;
    BackupEntry(String n, long t, long s) { name = n; time = t; size = s; }
    public String toString() { return name; }
}