package com.sun.squawk.analysis;

import java.io.*;

public class Test {

    public static void main(String[] args) throws Exception {
         Object o = "foo";
         Class c1 = o.getClass();
         Class c2 = c1.getClass();
         Class c3 = c2.getClass();
         System.out.println("o="+o+" "+o.hashCode());
         System.out.println("c1="+c1+" "+c1.hashCode()+" name="+c1.getName());
         System.out.println("c2="+c2+" "+c2.hashCode()+" name="+c2.getName());
         System.out.println("c3="+c3+" "+c3.hashCode()+" name="+c3.getName());

    }


}