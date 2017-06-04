//This class implements the job computation for the grid simulation
class Work implements GenericJob {
    int id;
    Phase pc;
    BarrierForPool barrier;
    int iters = 0;

    // PRE-CONDITION: Someone is trying to create a Work object
    // POST-CONDITION: A work object has been created and the phase, id, and
    // barrier of the Work object are set.
    public Work(int id, BarrierForPool barrier) {
        this.pc = Phase.initial;
        this.id = id;
        this.barrier = barrier;
    }

    // PRE-CONDITION: Someone wants to get the current phase of the Work object
    // POST-CONDITION: The current phase of the Work object is returned
    @Override
    public Phase getPhase() {
        return pc;
    }

    // PRE-CONDITION: A thread tries to execute the Work object
    // PRE-CONDITION: the Work object performed an operation as far as it can go
    // without getting ahead of the other GenericJob objects
    @Override
    public boolean execute() {
        System.out.println(pc);
        switch (pc) {
        case initial:
            pc = Phase.compute;
            iters = 1;
            if (!barrier.pass(id, this, iters))
                return false;
            else
                return true;
        case compute:
            if (iters > TPoolBarrier.MAXITERS) { /* compute */
                pc = Phase.terminating;
                return true;
            }
            pc = Phase.copy;

            if (!barrier.pass(id, this, iters))
                return false;
            else
                return true;
        case copy: /* copy */
            pc = Phase.compute;
            iters++;
            if (!barrier.pass(id, this, iters))
                return false;
            else
                return true;
        case terminating:
            return false;
        }
        return true;
    }
}