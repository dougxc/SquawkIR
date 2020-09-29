package com.sun.squawk.analysis;

import java.io.*;
import java.util.*;
import  com.sun.squawk.runtime.util.*;

class Count2 {

    final static int PERCENT = 90;

    static DataInputStream dis;
    static ArrayHashtable table = new ArrayHashtable();
    static ArrayHashtable stats = new ArrayHashtable();

    static boolean plus;
    static int total;

    public static void main(String[] args) throws Exception {
        plus = (args.length == 0 || args[0].equals("+"));
        dis = new DataInputStream(System.in);
        String[] line;

        int count = 0;
        while ((line = getLine()) != null) {
//            addLine(line);
            addLine(line, 0);
            for (int i = 1 ; i < line.length ; i++) {
                addStats(line, i);
            }

//            for (int i = 1 ; i < line.length ; i++) {
//                addLine(line, 0, i);
//            }
        }

        for (Enumeration e = table.keys() ; e.hasMoreElements() ;) {
            line = (String[])e.nextElement();
            count = ((Integer)table.get(line)).intValue();

            String scount = Integer.toString(count);
            int slength = 8 - scount.length();
            while (slength-- > 0) {
                System.out.print(" ");
            }
            System.out.print(scount);
            for (int i = 0 ; i < line.length ; i++) {
                System.out.print(" "+(line[i] == null ? "*" : line[i]));
            }
            System.out.println();

            for (int i = 1 ; i < line.length ; i++) {
                System.out.print(line[0]+"->"+i+" \t");
                ArrayHashtable stat = (ArrayHashtable)stats.get(line[0]+"->"+i);
                int[] value = new int[stat.size()];
                int[] freq  = new int[stat.size()];
                int index = 0;
                for (Enumeration e2 = stat.keys() ; e2.hasMoreElements() ;) {
                    Integer key = (Integer)e2.nextElement();
                    Integer val = (Integer)stat.get(key);
                    System.out.print(" "+key+"-"+val);
                    value[index]= key.intValue();
                    freq [index]= val.intValue();
                    index++;
                }
                System.out.println();
                System.out.print(line[0]+"->"+i+".\t");
                if (i == line.length - 1 && (line[0].startsWith("if") || line[0].equals("goto"))) {
                    analyse(value, freq, true);
                } else {
                    analyse(value, freq, false);
                }
            }

        }
        System.err.println(total);
    }

    public static void addLine(String[] line) throws Exception {
        Integer i = (Integer)table.get(line);
        if (i == null) {
            table.put(line, new Integer(1));
        } else {
            table.put(line, new Integer(i.intValue()+1));
        }
    }

    public static void addStats(String[] line, int entry) throws Exception {
        int val = getValueFrom(line[entry]);
        ArrayHashtable stat = (ArrayHashtable)stats.get(line[0]+"->"+entry);
        if (stat == null) {
           stat = new ArrayHashtable();
           stats.put(line[0]+"->"+entry, stat);
        }
        Integer i = (Integer)stat.get(new Integer(val));
        if (i == null) {
           i = new Integer(0);
        }
        stat.put(new Integer(val), new Integer(i.intValue()+1));
    }


/*
    public static void addLine(String[] line, int exclude) throws Exception {
        String[] line2 = (String[])line.clone();
        line2[exclude] = null;
        addLine(line2);
    }
*/

    public static void addLine(String[] line, int include) throws Exception {
        String[] line2 = new String[line.length];
        line2[include] = line[include];
        addLine(line2);
    }

    public static void addLine(String[] line, int include, int include2) throws Exception {
        String[] line2 = new String[line.length];
        line2[include]  = line[include];
        line2[include2] = line[include2];
        addLine(line2);
    }


    private static int getValueFrom(String s) {
        return getValueFrom(s, 0);
    }

    private static int getValueFrom(String s, int from) {
        int ch;
        int val = 0;
        int mul = 1;
        int i;

        for (i = from ; i < s.length() ; i++) {
            ch = s.charAt(i);
            if (ch == '-') {
                mul = -1;
                i++;
                break;
            }
            if (ch >= '0' && ch <='9') {
                break;
            }
        }

        for (; i < s.length() ; i++) {
            ch = s.charAt(i);
            if (ch >= '0' && ch <='9') {
                val *= 10;
                val += ch - '0';
            } else {
                break;
            }
        }
        return val * mul;
    }



//-5-6 -6-5 -7-6 -9-1 -11-1 13-1 -15-1 -17-1 -19-1 -21-1

    public static void analyse(int[] value, int[] freq, boolean plusminus) {
        System.out.print(" (n="+value.length);
        sort(value, freq); // sort by value
        System.out.print(" lo="+value[0]);
        System.out.print(" hi="+value[value.length-1]);
        int x = 0;
        int tot = 0;
        for (int i = 0 ; i < value.length ; i++) {
            if(value[i] >= 0) {
                x++;
            }
            tot += freq[i];
        }
        System.out.print(" +ve="+x/*+" tot="+tot*/);

        int lim = (tot * PERCENT) / 100;

        sort(freq, value); // sort by freq
        x = 0;
        int hi = Integer.MIN_VALUE;
        int lo = Integer.MAX_VALUE;
        for (int i = value.length-1 ; i >= 0 ; i--) {
            int n = value[i];
            if (n >= 0 || plusminus) {
                if (n < lo) lo = n;
                if (n > hi) hi = n;
                x += freq[i];
                if (x >= lim) {
                    break;
                }
            }
        }

        x = 0;
        for (int i = value.length-1 ; i >= 0 ; i--) {
            int n = value[i];
            if (n >= lo && n <= hi) {
                x += freq[i];
            }
        }

        System.out.print(" "+(tot==0?100:x*100/tot)+"%=("+lo+","+hi+")");

        System.out.println(")");
    }





    public static String[] getLine() throws Exception {
        while(true) {
            String line = dis.readLine();
            if (line == null) {
                return null;
            }
            if (line.endsWith("Terminate batch job (Y/N)?")) {
                return null;
            }
            if (line.length() > 1 && line.charAt(0) == (plus ? '+' : '-')) {
                StringTokenizer st = new StringTokenizer(line.substring(1));
                String[] array = new String[st.countTokens()];
                for (int i = 0 ; i < array.length ; i++) {
                    array[i] = st.nextToken().intern();
                }
                total++;
                return array;
            }
        }
    }
























    public static void sort(int[] value, int[] freq) {
        if (value.length > 1) {
            quicksort(value, freq, 0, value.length - 1);
        }
    }

   /**
    * Quicksort
    */
    private static void quicksort(int[] value, int[] freq, int p, int r) {
        if(p < r) {
            int q = partition(value, freq, p, r);
            if(q == r) {
                q--;
            }
            quicksort(value, freq, p, q);
            quicksort(value, freq, q+1, r);
        }
    }

   /**
    * partition
    */
    private static int partition(int[] value, int[] freq, int lo, int hi) {
        int pivot = value[lo];
        while (true) {
            while(compare(value[hi], pivot) >= 0 && lo < hi) {
                hi--;
            }
            while(compare(value[lo], pivot) <  0 && lo < hi) {
                lo++;
            }
            if(lo < hi) {
                int T        = value[lo];
                value[lo] = value[hi];
                value[hi] = T;

                T        = freq[lo];
                freq[lo] = freq[hi];
                freq[hi] = T;
            } else {
                return hi;
            }
        }
    }

    private static int compare(int a, int b) {
        return a - b;
    }


}