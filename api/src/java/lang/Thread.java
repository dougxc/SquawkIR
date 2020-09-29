/*
 * Copyright 1994-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

package java.lang;

/**
 * A <i>thread</i> is a thread of execution in a program. The Java
 * Virtual Machine allows an application to have multiple threads of
 * execution running concurrently.
 * <p>
 * Every thread has a priority. Threads with higher priority are
 * executed in preference to threads with lower priority.
 * <p>
 * There are two ways to create a new thread of execution. One is to
 * declare a class to be a subclass of <code>Thread</code>. This
 * subclass should override the <code>run</code> method of class
 * <code>Thread</code>. An instance of the subclass can then be
 * allocated and started. For example, a thread that computes primes
 * larger than a stated value could be written as follows:
 * <p><hr><blockquote><pre>
 *     class PrimeThread extends Thread {
 *         long minPrime;
 *         PrimeThread(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * The following code would then create a thread and start it running:
 * <p><blockquote><pre>
 *     PrimeThread p = new PrimeThread(143);
 *     p.start();
 * </pre></blockquote>
 * <p>
 * The other way to create a thread is to declare a class that
 * implements the <code>Runnable</code> interface. That class then
 * implements the <code>run</code> method. An instance of the class can
 * then be allocated, passed as an argument when creating
 * <code>Thread</code>, and started. The same example in this other
 * style looks like the following:
 * <p><hr><blockquote><pre>
 *     class PrimeRun implements Runnable {
 *         long minPrime;
 *         PrimeRun(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * The following code would then create a thread and start it running:
 * <p><blockquote><pre>
 *     PrimeRun p = new PrimeRun(143);
 *     new Thread(p).start();
 * </pre></blockquote>
 * <p>
 *
 *
 * @author  unascribed
 * @see     java.lang.Runnable
 * @see     java.lang.Runtime#exit(int)
 * @see     java.lang.Thread#run()
 * @since   JDK1.0
 */

/*
 * Implementation note.
 *
 * Race conditions are prevented by calling a native function that will
 * disable thread preemntion. The function that does this is called
 * disablePreemption(). A function called enablePreemption() will restore
 * the thread preemption state. These functions are not nestable.
 * An internal function called rechedule() will also enable preemption
 * so every disablePreemption() must be paired either with an
 * enablePreemption() or a rechedule(). When thread preemption is
 * disabled after a statement the comment "// ***" is placed in column 73.
 *
 * Thread schedulting is disabled by calling rechedule(). If the current
 * thread should not be disabled then "runnableThreads.add(currentThread);"
 * should be executed before the call to rechedule() (see yield()).
 */

public class Thread implements Runnable {

    /**
     * The minimum priority that a thread can have.
     */
    public final static int MIN_PRIORITY = 1;

   /**
     * The default priority that is assigned to a thread.
     */
    public final static int NORM_PRIORITY = 5;

    /**
     * The maximum priority that a thread can have.
     */
    public final static int MAX_PRIORITY = 10;

    /**
     * Returns a reference to the currently executing thread object.
     *
     * @return  the currently executing thread.
     */
    public static Thread currentThread() {
        return currentThread;
    }



    public void setDaemon(boolean value) {
        System.out.println("Thread::setDaemon");
    }


    /**
     * Causes the currently executing thread to sleep (temporarily cease
     * execution) for the specified number of milliseconds. The thread
     * does not lose ownership of any monitors.
     *
     * @param      millis   the length of time to sleep in milliseconds.
     * @exception  InterruptedException if another thread has interrupted
     *             the current thread.  The <i>interrupted status</i> of the
     *             current thread is cleared when this exception is thrown.
     * @see        java.lang.Object#notify()
     */
    public static void sleep(long delta) throws InterruptedException {
        if (trace) {
            System.out.println("Thread sleep("+delta+") "+currentThread.threadNumber);
        }
        if (delta < 0) {
            throw new IllegalArgumentException("negitive sleep time");
        }
        if (delta > 0) {
            disablePreemption();                                        // ***
            timerQueue.add(currentThread, delta);                       // ***
            reschedule();
        }
    }

   /**
     * Allocates a new <code>Thread</code> object.
     * <p>
     * Threads created this way must have overridden their
     * <code>run()</code> method to actually do anything.
     *
     * @see     java.lang.Runnable
     */
    public Thread() {
        this(null);
    }

    /**
     * Allocates a new <code>Thread</code> object with a
     * specific target object whose <code>run</code> method
     * is called.
     *
     * @param   target   the object whose <code>run</code> method is called.
     */
    public Thread(Runnable target) {
        this.threadNumber = nextThreadNumber++;
        this.target = target;
        this.state  = NEW;
        if (currentThread != null) {
            priority = currentThread.getPriority();
        } else {
            priority = NORM_PRIORITY;
        }
    }

   /**
    * Causes the currently executing thread object to temporarily pause
    * and allow other threads to execute.
    */
    public static void yield() {
        if (trace) {
            System.out.println("Thread yield() "+currentThread.threadNumber);
        }
        disablePreemption();                                            // ***
        runnableThreads.add(currentThread);                             // ***
        reschedule();
    }

    /**
     * Causes this thread to begin execution; the Java Virtual Machine
     * calls the <code>run</code> method of this thread.
     * <p>
     * The result is that two threads are running concurrently: the
     * current thread (which returns from the call to the
     * <code>start</code> method) and the other thread (which executes its
     * <code>run</code> method).
     *
     * @exception  IllegalThreadStateException  if the thread was already
     *               started.
     * @see        java.lang.Thread#run()
     */
    public void start() {
        disablePreemption();
        if (state != NEW) {                                             // ***
            enablePreemption();                                         // ***
            throw new IllegalThreadStateException();
        }                                                               // ***
        state = ALIVE;                                                  // ***
        aliveThreads++;                                                 // ***
        runnableThreads.add(this);                                      // ***
        runnableThreads.add(currentThread);                             // ***
        reschedule();
    }

    /**
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see     java.lang.Thread#start()
     * @see     java.lang.Runnable#run()
     */
    public void run() {
        if (target != null) {
            target.run();
        }
    }

    /**
     * Tests if this thread is alive. A thread is alive if it has
     * been started and has not yet died.
     *
     * @return  <code>true</code> if this thread is alive;
     *          <code>false</code> otherwise.
     */
    public final boolean isAlive() {
        return state == ALIVE;
    }

    /**
     * Changes the priority of this thread.
     *
     * @param newPriority priority to set this thread to
     * @exception  IllegalArgumentException  If the priority is not in the
     *             range <code>MIN_PRIORITY</code> to
     *             <code>MAX_PRIORITY</code>.
     * @see        #getPriority
     * @see        java.lang.Thread#getPriority()
     * @see        java.lang.Thread#MAX_PRIORITY
     * @see        java.lang.Thread#MIN_PRIORITY
     */
    public final void setPriority(int newPriority) {
        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
            throw new IllegalArgumentException();
        }
        priority = newPriority;
//System.out.println(""+this+" setPriority "+newPriority);
    }

    /**
     * Returns this thread's priority.
     *
     * @return  this thread's name.
     * @see     #setPriority
     * @see     java.lang.Thread#setPriority(int)
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * Returns the current number of active threads in the VM.
     *
     * @return the current number of active threads
     */
    public static int activeCount() {
        return runnableThreads.size() + 1;
    }

    /**
     * Waits for this thread to die.
     *
     * @exception  InterruptedException if another thread has interrupted
     *             the current thread.  The <i>interrupted status</i> of the
     *             current thread is cleared when this exception is thrown.
     */
    public final void join() throws InterruptedException {
        if (this != currentThread()) {

/*
            int save = currentThread().getPriority();
            int newp = getPriority() - 1;
            currentThread().setPriority(newp < MIN_PRIORITY ? MIN_PRIORITY : newp);
            while (isAlive()) {
                yield();
            }
            currentThread().setPriority(save);
*/
            disablePreemption();
            waitForJoin(currentThread);                                 // ***
            reschedule();                                               // ***
        }
    }

    /**
     * Returns a string representation of this thread, including a unique number
     * that identifies the thread and the thread's priority.
     *
     * @return  a string representation of this thread.
     */
    public String toString() {
        return "Thread[" + threadNumber + " (pri=" + getPriority() + ")]";
    }


   /* ------------------------------------------------------------------------ *\
    *                              Private stuff                               *
   \* ------------------------------------------------------------------------ */

   /**
    * assume
    */
    protected static void assume(boolean cond) {
        if (!cond) {
            fatalVMError();
        }
    }

   /**
    * callRun - This is called by the VM when a new thread is started.
    * The call sequence is that Thread.start() calls Thread.reschedule()
    * calls Thread.switchAndEnablePreemption() which calls this function.
    */
    private void callRun(boolean traceFlag) {
        trace = traceFlag;
        if (trace) {
            System.out.println("Thread start "+threadNumber);
        }
        try {
            run();
        } catch (Throwable ex) {
            System.out.print("Uncaught exception ");
            System.out.println(ex);
            ex.printStackTrace(System.out);
        }
        if (trace) {
            System.out.println("Thread exit "+threadNumber);
        }
        disablePreemption();                                        // ***
        state = DEAD;                                               // ***
        startJoinThreads();                                         // ***
        aliveThreads--;                                             // ***
        reschedule();
        fatalVMError();  // Catch VM errors here.
    }

    private void callRun() {
        callRun(false);
    }

    private void callRunTracing() {
        callRun(true);
    }


   /**
    * Add the thread to the list of threads to be started when a thread dies
    */
    private static void waitForJoin(Thread joinThread) {            // ***
        joinThread.nextThread = joinWaitQueue;                      // ***
        joinWaitQueue = joinThread;                                 // ***
    }                                                               // ***

   /**
    * Restart all threads waiting for a thread to die.
    */
    private static void startJoinThreads() {                        // ***
        while (joinWaitQueue != null) {                             // ***
            Thread next = joinWaitQueue;                            // ***
            joinWaitQueue = joinWaitQueue.nextThread;               // ***
            runnableThreads.add(next);                              // ***
        }                                                           // ***
    }                                                               // ***


   /**
    * callMain - This is called by the VM to start the application running
    * It is the first Java code to be run after a few class initializers
    * have executed.
    */
    private static void callMain() {
        new Thread() {
            public void run() {
                startPreemptiveScheduling();
                doMain();
            }
        }.start();
        fatalVMError();  // Catch VM errors here.
    }

   /**
    * Context switch to another thread
    *
    * The executrion of this function *must* be preceeded by a call to
    * disablePreemption().
    */
    private static void reschedule() {                                  // ***
                                                                        // ***
        Thread thread;                                                  // ***
        Thread oldThread = currentThread;                               // ***
        currentThread = null; // safety                                 // ***
                                                                        // ***
       /*                                                               // ***
        * Loop until there is something to do                           // ***
        */                                                              // ***
        for (;;) {                                                      // ***
                                                                        // ***
           /*                                                           // ***
            * Add any threads that are ready to be restarted.           // ***
            */                                                          // ***
            while ((thread = threadToRestart()) != null) {              // ***
                runnableThreads.add(thread);                            // ***
            }                                                           // ***
                                                                        // ***
           /*                                                           // ***
            * Add any threads waiting for a certain time that           // ***
            * are now due.                                              // ***
            */                                                          // ***
            while ((thread = timerQueue.next()) != null) {              // ***
                Monitor monitor = thread.monitor;                       // ***
                if (monitor != null) {                                  // ***
                    monitor.removeCondvarWait(thread);                  // ***
                }                                                       // ***
                runnableThreads.add(thread);                            // ***
            }                                                           // ***
                                                                        // ***
           /*                                                           // ***
            * Break fo there is something to do                         // ***
            */                                                          // ***
            if ((thread = runnableThreads.next()) != null) {            // ***
                break;                                                  // ***
            }                                                           // ***
                                                                        // ***
           /*                                                           // ***
            * Wait for something to happen or exit VM if there are      // ***
            * no threads left.                                          // ***
            */                                                          // ***
            waitForEvent(aliveThreads, timerQueue.nextTime());          // ***
        }                                                               // ***
                                                                        // ***
       /*                                                               // ***
        * Set the current thread                                        // ***
        */                                                              // ***
        currentThread = thread;                                         // ***
                                                                        // ***
       /*                                                               // ***
        * The following will either return to a previous context        // ***
        * or cause callRun() to be entered if currentThread             // ***
        * is a new thread.                                              // ***
        */                                                              // ***
        switchAndEnablePreemption(oldThread, currentThread);
    }

   /**
    * Block the current thread (Premprion is disabled by the VM)
    */
    private static void block() {                                       // ***
        reschedule();                                                   // ***
    }

   /**
    * Restart a blocked thread
    */
/*
    private static void unblock(Thread thread) {
        disablePreemption();                                            // ***
        runnableThreads.add(thread);                                    // ***
        runnableThreads.add(currentThread);                             // ***
        reschedule();                                                   // ***
    }
*/

   /**
    * addMonitorWait
    */
    private static void addMonitorWait(Monitor monitor, Thread thread) {// ***
       /*                                                               // ***
        * Check the nesting depth                                       // ***
        */                                                              // ***
        assume(thread.monitorDepth > 0);                                // ***
       /*                                                               // ***
        * Add to the wait queue                                         // ***
        */                                                              // ***
        monitor.addMonitorWait(thread);                                 // ***
       /*                                                               // ***
        * If the wait queue has no owner then try and start a           // ***
        * waiting thread.                                               // ***
        */                                                              // ***
        if (monitor.owner == null) {                                    // ***
            removeMonitorWait(monitor);                                 // ***
        }                                                               // ***
    }                                                                   // ***

   /**
    * removeMonitorWait
    */
    private static void removeMonitorWait(Monitor monitor) {            // ***
       /*                                                               // ***
        * Try and remove a thread from the wait queue                   // ***
        */                                                              // ***
        Thread waiter = monitor.removeMonitorWait();                    // ***
        if (waiter != null) {                                           // ***
           /*                                                           // ***
            * Set the monitor's ownership and nesting depth             // ***
            */                                                          // ***
            monitor.owner = waiter;                                     // ***
            monitor.depth = waiter.monitorDepth;                        // ***
            assume(waiter.monitorDepth > 0);                            // ***
           /*                                                           // ***
            * Restart execution of the thread                           // ***
            */                                                          // ***
            runnableThreads.add(waiter);                                // ***
        } else {                                                        // ***
           /*                                                           // ***
            * No thread is waiting for this monitor,                    // ***
            * so mark it as unused                                      // ***
            */                                                          // ***
            monitor.owner = null;                                       // ***
            monitor.depth = 0;                                          // ***
        }                                                               // ***
    }                                                                   // ***


   /**
    * monitiorEnter
    */
    private static void monitorEnter(Monitor monitor) {
        disablePreemption();                                            // ***
        if (monitor.owner == null) {                                    // ***
           /*                                                           // ***
            * Unowned monitor, make the current thread the owner        // ***
            */                                                          // ***
            monitor.owner = currentThread;                              // ***
            monitor.depth = 1;                                          // ***
            enablePreemption();                                         // ***
        } else if (monitor.owner == currentThread) {                    // ***
           /*                                                           // ***
            * Thread already owns the monitor, increment depth          // ***
            */                                                          // ***
            monitor.depth++;                                            // ***
            enablePreemption();                                         // ***
        } else {                                                        // ***
           /*                                                           // ***
            * Add to the wait queue and set the depth for when thread   // ***
            * is restarted                                              // ***
            */                                                          // ***
            currentThread.monitorDepth = 1;                             // ***
            addMonitorWait(monitor, currentThread);                     // ***
            reschedule();
           /*
            * Safety...
            */
            assume(monitor.owner == currentThread);
            currentThread.monitor = null;
            currentThread.monitorDepth = 0;
        }
    }

   /**
    * monitiorExit
    */
    private static void monitorExit(Monitor monitor) {
        disablePreemption();                                            // ***
       /*                                                               // ***
        * Throw an exception if things look bad                         // ***
        */                                                              // ***
        if (monitor.owner != currentThread) {                           // ***
            enablePreemption();
            throw new IllegalMonitorStateException();
        }
       /*                                                               // ***
        * Try and restart a thread if the nesting depth is zero         // ***
        */                                                              // ***
        if (--monitor.depth == 0) {                                     // ***
            removeMonitorWait(monitor);                                 // ***
        }                                                               // ***
        enablePreemption();
    }



   /**
    * monitiorWait
    */
    private static void monitorWait(Object object, Monitor monitor, long delta) {
        disablePreemption();
       /*                                                               // ***
        * Throw an exception if things look bad                         // ***
        */                                                              // ***
        if (monitor.owner != currentThread) {                           // ***
            enablePreemption();
            throw new IllegalMonitorStateException();
        }                                                               // ***
       /*                                                               // ***
        * Add to timer queue if time is > 0                             // ***
        */                                                              // ***
        if (delta > 0) {                                                // ***
            timerQueue.add(currentThread, delta);                       // ***
        }                                                               // ***
       /*                                                               // ***
        * Save the nesting depth so it can be restored                  // ***
        * when it regains the monitor                                   // ***
        */                                                              // ***
        currentThread.monitorDepth = monitor.depth;                     // ***
       /*                                                               // ***
        * Add to the wait queue                                         // ***
        */                                                              // ***
        monitor.addCondvarWait(currentThread);                          // ***
       /*                                                               // ***
        * Having relinquishing the monitor                              // ***
        * get the next thread off the wait queue                        // ***
        */                                                              // ***
        removeMonitorWait(monitor);                                     // ***
       /*                                                               // ***
        * Wait for a notify or timeout                                  // ***
        */                                                              // ***
        assume(currentThread.monitor == monitor);                       // ***
        reschedule();
       /*
        * At this point the thread has been restarted. This could have
        * been because of a call to notify() or a timeout.
        */
        disablePreemption();                                            // ***
       /*                                                               // ***
        * Must get the monitor again                                    // ***
        */                                                              // ***
        addMonitorWait(monitor, currentThread);                         // ***
        reschedule();
       /*
        * Safety...
        */
        currentThread.monitor = null;
        currentThread.monitorDepth = 0;
    }


   /**
    * monitorNotify
    *
    * A little fudge is applied here. In order to simplfy the interpreter
    * the signature of this function is the same as that of monitorWait()
    * The "long value" is set to 1 for true or 0 for false for notifyAll.
    */
    private static void monitorNotify(Object object, Monitor monitor, long value) {
        boolean notifyAll = value != 0;
        disablePreemption();
       /*
        * Throw an exception if things look bad                         // ***
        */                                                              // ***
        if (monitor.owner != currentThread) {                           // ***
            enablePreemption();
            throw new IllegalMonitorStateException();
        }                                                               // ***
       /*                                                               // ***
        * Try and restart a thread                                      // ***
        */                                                              // ***
        do {                                                            // ***
            Thread waiter = monitor.removeCondvarWait();                // ***
            if (waiter == null) {                                       // ***
                break;                                                  // ***
            }                                                           // ***
           /*                                                           // ***
            * Remove timeout is there was one and restart               // ***
            */                                                          // ***
            timerQueue.remove(waiter);                                  // ***
            runnableThreads.add(waiter);                                // ***
       /*                                                               // ***
        * Loop here if it is a notifyAll                                // ***
        */                                                              // ***
        } while (notifyAll);                                            // ***
        enablePreemption();
    }

   /**
    * setInQueue
    */
    protected void setInQueue() {                                       // ***
        assume(!inQueue);                                               // ***
        inQueue = true;                                                 // ***
    }                                                                   // ***

   /**
    * setNotInQueue
    */
    protected void setNotInQueue() {                                    // ***
        assume(inQueue);                                                // ***
        inQueue = false;                                                // ***
    }                                                                   // ***

   /**
    * setInTimerQueue
    */
    protected void setInTimerQueue() {                                  // ***
        assume(!inTimerQueue);                                          // ***
        inTimerQueue = true;                                            // ***
    }                                                                   // ***

   /**
    * setNotInTimerQueue
    */
    protected void setNotInTimerQueue() {                               // ***
        assume(inTimerQueue);                                           // ***
        inTimerQueue = false;                                           // ***
    }                                                                   // ***


   /* ------------------------------------------------------------------------ *\
    *                               Global state                               *
   \* ------------------------------------------------------------------------ */

    private static Thread currentThread;                            /* The current thread             */
    private static int aliveThreads = 0;                            /* Number of alive threads        */
    private static ThreadQueue runnableThreads = new ThreadQueue(); /* Queue of runnable threads      */
    private static TimerQueue timerQueue = new TimerQueue();        /* Queue of timed waiting threads */
    private static int nextThreadNumber = 0;                        /* The 'name' of the next thread  */
    private static boolean trace;                                   /* Trace flag                     */
    private static Thread joinWaitQueue;                            /* Queue of waiting threads       */


   /* ------------------------------------------------------------------------ *\
    *                              Instance state                              *
   \* ------------------------------------------------------------------------ */

    private final static int NEW   = 0;
    private final static int ALIVE = 1;
    private final static int DEAD  = 2;

    private   Runnable target;          /* Target to run (if run() is not overridden)                        */
    protected int priority;             /* Execution priority                                                */
    private   int state;                /* Aliveness                                                         */
    private   Object ar;                /* Used internally by the VM to point to the activation record       */
    private   int ip;                   /* Used internally by the VM to record the instruction pointer       */
    protected boolean inQueue;          /* Flag to show if thread is in a queue                              */
    protected Thread nextThread;        /* For enqueueing in the ready, monitor wait, or condvar wait queues */
    protected boolean inTimerQueue;     /* Flag to show if thread is in a queue                              */
    protected Thread nextTimerThread;   /* For enqueueing in the timer queue                                 */
    protected long time;                /* Time to emerge from the timer queue                               */
    protected int monitorDepth;         /* Saved monitor nesting depth                                       */
    protected Monitor monitor;          /* Monitor when thread is in the condvar queue                       */
    protected int threadNumber;         /* The 'name' of the thread                                          */


   /* ------------------------------------------------------------------------ *\
    *                            Native functions                              *
   \* ------------------------------------------------------------------------ */

    private native static void   startPreemptiveScheduling();
    private native static void   disablePreemption();
    private native static void   enablePreemption();
    private native static void   switchAndEnablePreemption(Thread fromThread, Thread toThread);
    private native static Thread threadToRestart();
    private native static void   waitForEvent(int aliveThreads, long nextTime);
    private native static void   doMain();
    protected native static void fatalVMError();
}



/* ======================================================================== *\
 *                                 Monitor                                  *
\* ======================================================================== */

/*
 * Note - All the code in the following class is run with preemption disabled
 */

class Monitor {
    protected Object realType;          /* Used internally by the VM to point to the Type */
    protected Thread owner;             /* Owening thread of the monitor                  */
    protected int    depth;             /* Nesting depth                                  */
    protected Thread monitorQueue;      /* Queue of threads waiting to claim the monitor  */
    protected Thread condvarQueue;      /* Queue of threads waiting to claim the object   */

   /**
    * addMonitorWait
    */
    void addMonitorWait(Thread thread) {
        thread.setInQueue();
        Thread.assume(thread.nextThread == null);
        Thread next = monitorQueue;
        if (next == null) {
            monitorQueue = thread;
        } else {
            while (next.nextThread != null) {
                next = next.nextThread;
            }
            next.nextThread = thread;
        }
    }

   /**
    * removeMonitorWait
    */
    Thread removeMonitorWait() {
        Thread thread = monitorQueue;
        if (thread != null) {
            monitorQueue = thread.nextThread;
            thread.setNotInQueue();
            thread.nextThread = null;
        }
        return thread;
    }

   /**
    * addCondvarWait
    */
    void addCondvarWait(Thread thread) {
        thread.setInQueue();
        thread.monitor = this;
        Thread.assume(thread.nextThread == null);
        Thread next = condvarQueue;
        if (next == null) {
            condvarQueue = thread;
        } else {
            while (next.nextThread != null) {
                next = next.nextThread;
            }
            next.nextThread = thread;
        }
    }

   /**
    * removeCondvarWait
    */
    Thread removeCondvarWait() {
        Thread thread = condvarQueue;
        if (thread != null) {
            condvarQueue = thread.nextThread;
            thread.setNotInQueue();
            thread.monitor = null;
            thread.nextThread = null;
        }
        return thread;
    }

   /**
    * removeCondvarWait
    */
    void removeCondvarWait(Thread thread) {
        if (thread.inQueue) {
            thread.setNotInQueue();
            thread.monitor = null;
            Thread next = condvarQueue;
            if (next != null) {
                if (next == thread) {
                    condvarQueue = thread.nextThread;
                    thread.nextThread = null;
                    return;
                }
                while (next.nextThread != thread) {
                    next = next.nextThread;
                }
                if (next.nextThread == thread) {
                    next.nextThread = thread.nextThread;
                }
                thread.nextThread = null;
                return;
            }
        }
    }

}


/* ======================================================================== *\
 *                               ThreadQueue                                *
\* ======================================================================== */

/*
 * Note - All the code in the following class is run with preemption disabled
 */

class ThreadQueue {

    Thread first;
    int count;

   /**
    * add
    */
    void add(Thread thread) {
        if (thread != null) {
            thread.setInQueue();
            if (first == null) {
                first = thread;
            } else {
                if (first.priority < thread.priority) {
                    thread.nextThread = first;
                    first = thread;
                } else {
                    Thread last = first;
                    while (last.nextThread != null && last.nextThread.priority >= thread.priority) {
                        last = last.nextThread;
                    }
                    thread.nextThread = last.nextThread;
                    last.nextThread = thread;
                }
            }
            count++;
        }
    }

   /**
    * size
    */
    int size() {
        return count;
    }

   /**
    * next
    */
    Thread next() {
        Thread thread = first;
        if (thread != null) {
            thread.setNotInQueue();
            first = thread.nextThread;
            thread.nextThread = null;
            count--;
        }
        return thread;
    }

}


/* ======================================================================== *\
 *                                TimerQueue                                *
\* ======================================================================== */

/*
 * Note - All the code in the following class is run with preemption disabled
 */

class TimerQueue {

    Thread first;

   /**
    * add
    */
    void add(Thread thread, long delta) {
        Thread.assume(thread.nextTimerThread == null);
        thread.setInTimerQueue();
        thread.time = System.currentTimeMillis() + delta;
        if (thread.time < 0) {
           /*
            * If delta is so huge that the time went -ve then just make
            * it a very large value. The universe will end before the error
            * is detected!
            */
            thread.time = Long.MAX_VALUE;
        }
        if (first == null) {
            first = thread;
        } else {
            if (first.time > thread.time) {
                thread.nextTimerThread = first;
                first = thread;
            } else {
                Thread last = first;
                while (last.nextTimerThread != null && last.nextTimerThread.time < thread.time) {
                    last = last.nextTimerThread;
                }
                thread.nextTimerThread = last.nextTimerThread;
                last.nextTimerThread = thread;
            }
        }
    }

   /**
    * next
    */
    Thread next() {
        Thread thread = first;
        if (thread == null || thread.time > System.currentTimeMillis()) {
            return null;
        }
        first = first.nextTimerThread;
        thread.setNotInTimerQueue();
        thread.nextTimerThread = null;
        Thread.assume(thread.time != 0);
        thread.time = 0;
        return thread;
    }

   /**
    * remove
    */
    void remove(Thread thread) {
        if (first == null || thread.time == 0) {
            Thread.assume(!thread.inQueue);
            return;
        }
        thread.setNotInTimerQueue();
        if (thread == first) {
            first = thread.nextTimerThread;
            thread.nextTimerThread = null;
            return;
        }
        Thread p = first;
        while (p.nextTimerThread != null) {
            if (p.nextTimerThread == thread) {
                p.nextTimerThread = thread.nextTimerThread;
                thread.nextTimerThread = null;
                return;
            }
            p = p.nextTimerThread;
        }
        thread.fatalVMError();
    }

   /**
    * nextTime
    */
    long nextTime() {
        if (first != null) {
            return first.time;
        } else {
            return Long.MAX_VALUE;
        }
    }

}

