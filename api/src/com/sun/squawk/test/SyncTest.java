
package com.sun.squawk.test;

class SyncTest {

    Object o1 = new Object();
    Object o2 = new Object();

    public void foo() {
        synchronized(o1) {
            synchronized(o2) {
                System.out.println("foo");
            }
        }
    }


/*
Optimizing Lcom/sun/squawk/test/SyncTest;::foo

++IR1 trace for Lcom/sun/squawk/test/SyncTest;::foo
0:          Instructions = 24 Locals = l2# l1# l4# t0# l3# *l0#

1:          l1#         = [o1 Ljava/lang/Object; (fslot 0)] l0#
2:          monitorenter l1#
3:          handlerenter 19


4:          l2#         = [o2 Ljava/lang/Object; (fslot 1)] l0#
5:          monitorenter l2#
6:          handlerenter 12


7:          t0#         = [out Ljava/io/PrintStream; (fslot 30004)]
8:          invoke [println(Ljava/lang/Object;Ljava/lang/String;)V (mslot 36)] t0# const("foo")
9:          monitorexit l2#
10:         goto 15


11:         handlerexit 12
12:     12: l3# = exception(Ljava/lang/Throwable;)
13:         monitorexit l2#
14:         throw l3#


15:     15:
16:         monitorexit l1#
17:         goto 22


18:         handlerexit 19
19:     19: l4# = exception(Ljava/lang/Throwable;)
20:         monitorexit l1#
21:         throw l4#

22:     22:
23:         return

*/





    public void foo2() {
        try {
            synchronized(o1) {
                System.out.println("foo");
            }
        } catch(Exception ex) {
            System.out.println("Exception");
        } catch(Throwable ex) {
            System.out.println("Throwable");
        }
    }


/*
++IR1 trace for Lcom/sun/squawk/test/SyncTest;::foo2
0:          Instructions = 27 Locals = t0# l1# l2# *l0#

1:          handlerenter 22
2:          handlerenter 18
3:          l1#         = [o1 Ljava/lang/Object; (fslot 0)] l0#
4:          monitorenter l1#
5:          handlerenter 11
6:          t0#         = [out Ljava/io/PrintStream; (fslot 30004)]
7:          invoke [println(Ljava/lang/Object;Ljava/lang/String;)V (mslot 36)] t 0# const("foo")
8:          monitorexit l1#
9:          goto 16



10:         handlerexit 11
11:     11: l2# = exception(Ljava/lang/Throwable;)
12:         monitorexit l1#
13:         throw l2#
14:         handlerexit 22
15:         handlerexit 18
16:     16:
17:         goto 25


18:     18: l1# = exception(Ljava/lang/Exception;)
19:         t0#         = [out Ljava/io/PrintStream; (fslot 30004)]
20:         invoke [println(Ljava/lang/Object;Ljava/lang/String;)V (mslot 36)] t 0# const("Exception")
21:         goto 25


22:     22: l2# = exception(Ljava/lang/Throwable;)
23:         t0#         = [out Ljava/io/PrintStream; (fslot 30004)]
24:         invoke [println(Ljava/lang/Object;Ljava/lang/String;)V (mslot 36)] t 0# const("Throwable")


25:     25:
26:         return

--IR1 trace for Lcom/sun/squawk/test/SyncTest;::foo2
*/







}