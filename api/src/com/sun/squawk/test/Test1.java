package com.sun.squawk.test;

public class Test1 implements Runnable {

    boolean go = true; // This tests if newInstance() calls <init>
    Thread thread;

    public static void main(String[] args) throws Exception {
        System.out.print("Args = ");
        for (int i = 0 ; i < args.length ; i++) {
            System.out.print(args[i]+" ");
        }
        System.out.println("\n");

        Class clazz = Class.forName("com.sun.squawk.test.Test1");
        Test1 object = (com.sun.squawk.test.Test1)clazz.newInstance();
        object.start();
    }

    public void start() throws Exception {
        if (go) {
            long time = System.currentTimeMillis();
            thread = new Thread(this);

            System.out.println("***start***");
            thread.start();
            synchronized(thread) {
                thread.wait(10000);
            }
            System.out.println("***end*** " + ((System.currentTimeMillis() - time)));
        }
    }

    public void run() {
        int[] list = {1, 2, 3};
        try {
            try {
                list[3]++; // Cause an ArrayIndexOutOfBoundsException
            } catch(NullPointerException ex) {
                System.out.println("Wrong exception!");
                return;
            } catch(ArrayIndexOutOfBoundsException ex) {
                System.out.println("Got exception");
                return;
            } catch(Exception ex) {
                System.out.println("Wrong exception!");
                return;
            }
            System.out.println("No exception!");
        } finally {
            System.out.println("2 sec sleep start");
            try {
                Thread.sleep(2000);
            } catch(Exception ex) {}
            System.out.println("2 sec sleep end");

            synchronized(thread) {
                thread.notify();
            }
            System.out.println("thread notifyed");
        }
    }
}