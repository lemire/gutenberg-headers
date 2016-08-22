public class CGroup {
    CGroup prev;  // dbl linked list of groups
    CGroup next;
    Counter list; // counters with this value
    int delta;    // from the original

    public CGroup() {
        next = null;
        list = null;
        delta=0;
    }


    void checkList() {
        for (Counter t = list; t != null; t = t.next) {
            if (t.next != null && t.next.prev != t) 
                if (t.next.prev == null)
                    throw new RuntimeException("prev was null, step " + Gut.p1);
                else
                    throw new RuntimeException("prevs messed up, step number "+Gut.p1);
        }
    }
    
    int count() {
        int cnt=0;
        for (Counter t = list; t != null; t = t.next)  ++cnt;
        return cnt;
    }
    
    
    public String toString() {
        return "Group delta " + delta + ":"+count();
    }

}
