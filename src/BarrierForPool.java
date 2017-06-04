class BarrierForPool {
    boolean[] arrived;
    int size;
    ThreadPool pool;
    int[] times;
    int[] previous_times;
    Phase[] phases;
    Phase[] previous_phases;

    // PRE-CONDITION: Someone wants to create a BarrierForPool object
    // POST-CONDITION: A BarrierForPool object has been created and all
    // necessary pool tracking variables have been initialized
    public BarrierForPool(int size, ThreadPool pool) {
        this.size = size;
        this.pool = pool;
        arrived = new boolean[size];
        times = new int[size];
        previous_times = new int[size];
        phases = new Phase[size];
        previous_phases = new Phase[size];
        for (int i = 0; i < size; i++) {
            arrived[i] = false;
            times[i] = 0;
            phases[i] = Phase.initial;
        }
    } // This is where the jobs wait in the barrier

    // PRE-CONDITION: A GenericJob object requests to see if it can go to the
    // next
    // step of its execution
    // POST-CONDITION: The BarrierForPool object has determined whether the
    // GenericJob
    // object can proceed to the next step of its execution. Based on that, the
    // GenericJob is either put into the wait queue or the job queue.
    synchronized public boolean pass(int id, GenericJob job, int iters) {
        previous_phases[id] = phases[id];
        previous_times[id] = times[id];
        phases[id] = job.getPhase();
        times[id] = iters;
        arrived[id] = true;
        boolean go = true;
        for (int i = 0; i < size; i++) {
            if (!arrived[i]) {
                go = false;
                break;
            }
        }
        if (go) {
            // INVARIANT: Assert that all jobs are on the same iteration
            int currentIteration = times[0];
            for (int t : times) {
                assert currentIteration == t : "Not all jobs on same iteration";
            }
            // INVARIANT: Assert that all jobs are in the same phase
            Phase currentPhase = phases[0];
            for (Phase phase : phases) {
                assert currentPhase == phase : "Not all jobs on same phase of the iteration";
            }
            // INVARIANT: If copy operation just completed, assert that a
            // compute operation happened previously
            if (phases[id] == Phase.copy) {
                for (Phase previous_phase : previous_phases) {
                    assert previous_phase == Phase.compute : "Not all jobs computed before they copied";
                }
            } // INVARIANT: If compute operation just completed and the initial
              // phase did not happen previously, assert that a copy
              // operation happened previously and that the iteration is one
              // less than it is now.
            else if (phases[id] == Phase.compute
                    && previous_phases[id] != Phase.initial) {
                for (int i = 0; i < size; i++) {
                    assert (previous_phases[i] == Phase.copy && previous_times[i] == times[i] - 1) : "Not all jobs copied and then incremented by 1";
                }
            }

            for (int i = 0; i < size; i++) {
                arrived[i] = false;
            }

            while (!pool.waitQueueEmpty()) {
                pool.addJobToJobQueue(pool.getJobFromWaitQueue());
            }
            return true;
        }
        else {
            pool.addJobToWaitQueue(job);
            return false;
        }
    }
}