package ru.cft;

import java.util.*;


public class Main {
    static boolean descendingSort = false;  // descendingSort - false или ascendingSort - true !!!!!
    static boolean isString = false;

    public static void main(String[] args) {
        ArgAnalysis argAnalysis = new ArgAnalysis();
        argAnalysis.parseParameters(args);

        FileAnalysis fileAnalysis = new FileAnalysis();
        fileAnalysis.merge(FileAnalysis.data);
    }
}
