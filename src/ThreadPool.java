// Class ThreadPool implements a thread pool.
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
    // Create the two queues
    private final LinkedList<GenericJob> jobQueue = new LinkedList<GenericJob>();
    private final LinkedList<GenericJob> waitQueue = new LinkedList<GenericJob>();
    Thread[] t;
    int numThreads;
    AtomicInteger runningThreadsCount = new AtomicInteger(0);

    // PRE-CONDITION: Someone wants to create a new ThreadPool
    // POST-CONDITION: ThreadPool object created and threads pre-spawned for
    // pool
    public ThreadPool(int numThreads) {
        this.numThreads = numThreads;
        t = new Thread[numThreads];
    }

    // PRE-CONDITION: ThreadPool object has been initialized
    // POST-CONDITION: All threads in the ThreadPool object have been
    // started
    public void activate() {
        // Set the number of running threads
        runningThreadsCount.set(numThreads);
        // Run the threads
        for (int i = 0; i < numThreads; i++) {
            t[i] = new PoolThread();
            t[i].start();
        }
    }

    // PRE-CONDITION: Someone wants to know whether or not the job queue is
    // empty
    // POST-CONDITION: Whether or not the job queue is empty is returned
    public synchronized boolean jobQueueEmpty() {
        return (jobQueue.isEmpty());
    }

    // PRE-CONDITION: Someone wants to know whether or not the wait queue is
    // empty
    // POST-CONDITION: Whether or not the wait queue is empty is returned
    public synchronized boolean waitQueueEmpty() {
        return (waitQueue.isEmpty());
    }

    // PRE-CONDITION: Someone wants to add a GenericJob to the job queue
    // POST-CONDITION: A GenericJob is added to the job queue
    public synchronized void addJobToJobQueue(GenericJob job) {
        jobQueue.add(job);
        notify();
    }

    // PRE-CONDITION: Someone wants to add a GenericJob to the wait queue
    // POST-CONDITION: A GenericJob is added to the wait queue
    public synchronized void addJobToWaitQueue(GenericJob job) {
        waitQueue.add(job);
    }

    // PRE-CONDITION: Someone requests a GenericJob from the wait queue
    // POST-CONDITION: Someone gets a GenericJob from the wait queue
    public synchronized GenericJob getJobFromWaitQueue() {
        return waitQueue.removeFirst();
    }

    // PRE-CONDITION: A PoolThread is requesting a GenericJob
    // POST-CONDITION: A PoolThread either got a GenericJob or is told none were
    // left
    private synchronized GenericJob getJob() {
        while (true) {
            if (jobQueue.size() == 1) {
                return jobQueue.removeFirst();
            }
            else if (jobQueue.size() > 1) {
                notifyAll();
                return jobQueue.removeFirst();
            }
            else if (jobQueue.isEmpty() && waitQueue.isEmpty()) {
                return null;
            }
            else { // there are jobs in the waitQueue
                try {
                    wait();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Implementation of a job thread (refer to c1 in Section 2.4)
    private class PoolThread extends Thread {
        // PRE-CONDITION: The PoolThread object has been activated by the
        // ThreadPool
        // POST-CONDITION: A GenericJob has run successfully as far as possible
        @Override
        public void run() {
            GenericJob job;
            while (true) {
                job = getJob();
                if (job == null && jobQueue.isEmpty() && waitQueue.isEmpty()) {
                    runningThreadsCount.decrementAndGet();
                    // Verify that the both queues are empty before shutdown of
                    // the final thread
                    if (runningThreadsCount.get() == 0) {
                        assert (jobQueue.isEmpty() && waitQueue.isEmpty());
                    }
                    break;
                }
                else if (job == null) {
                    continue;
                }
                while (true) {
                    boolean doNextStep = job.execute();
                    if (doNextStep == false) {
                        break;
                    }
                }
            }
        }
    }
}