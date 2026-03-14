package com.xcr;

public class LessonInfo {
    String student, teacher, breakTime;
    LessonInfo(String s, String t, String b) { student = s; teacher = t; breakTime = b; }
    LessonInfo copy() { return new LessonInfo(student, teacher, breakTime); }
}