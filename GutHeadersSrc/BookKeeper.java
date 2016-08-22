public abstract class BookKeeper {

    abstract void processItem( String thisItem);
    public abstract boolean frequent( String mi);
    public abstract int reportThreshold();
    abstract void reportNonZero();
    void dump() {};
    void sanityCheck() {};
}
