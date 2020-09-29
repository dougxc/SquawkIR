package com.sun.squawk.analysis;

import java.io.*;

class Count {
    static DataInputStream dis;
    public static void main(String[] args) throws Exception {
        boolean plus = (args.length == 0 || args[0].equals("+"));
        dis = new DataInputStream(System.in);
        String line;
        String lastline = null;
        int count = 0;
        while ((line = getLine()) != null) {
            if (line != null && line.length() > 1 && line.charAt(0) == (plus ? '+' : '-')) {
                if (line.equals(lastline)) {
                    count++;
                } else {
                    String scount = Integer.toString(count);
                    int slength = 8 - scount.length();
                    while (slength-- > 0) {
                        System.out.print(" ");
                    }
                    System.out.println(scount+" "+lastline);
                    count = 1;
                    lastline = line;
                }
            }
        }
    }
    public static String getLine() throws Exception {
        return dis.readLine();
    }

}