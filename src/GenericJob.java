// GenericJob is an interface that defines method execute()
// All job classes implements GenericJob
public interface GenericJob {
    boolean execute();

    Phase getPhase();
}
