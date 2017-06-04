public class TPoolBarrier {
    // Define number of threads used in threadpool model
    public static int THREADSFORPOOL = 2;

    // number of jobs
    public static int HEIGHT;
    // Define maximum number of iterations
    public static int MAXITERS = 2;
    // Threadpool object
    ThreadPool pool;
    BarrierForPool barrierForPool;

    // PRE-CONDITION: Someone is trying to create a TPoolBarrier object
    // POST-CONDITION: A TPoolBarrier object has been created, the number of
    // jobs for the barrier has been determined and the ThreadPool has been
    // connected to a pool barrier
    public TPoolBarrier() {
        HEIGHT = 3;
        pool = new ThreadPool(THREADSFORPOOL);
        barrierForPool = new BarrierForPool(HEIGHT, pool);
    }

    // PRE-CONDITION: Program started
    // POST-CONDITION: constructed the barrier and the job objects
    public static void main(String[] args) {
        TPoolBarrier tPoolBarrier = new TPoolBarrier();
        Work[] work = new Work[HEIGHT];
        for (int i = 0; i < HEIGHT; i++) {
            work[i] = new Work(i, tPoolBarrier.barrierForPool);
            tPoolBarrier.pool.addJobToJobQueue(work[i]);
        }
        tPoolBarrier.pool.activate();
    }
}