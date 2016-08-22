public class Counter {
    public static final boolean monitoring = true;
    CGroup cg;
    Crc64MonitoredItem myItem;
    Counter next;
    Counter prev;

    public Counter(CGroup c) {
        cg = c;
        myItem = null; next = null; prev = null;
    }

    void bumpUp() {
        if(monitoring) Gut.b1++;
        CGroup startingGroup = cg;

        // remove me from my list
        // if I have a predecessor, update him
        Counter myOldPredecessor = prev;
        Counter myOldSuccessor = next;
        if (prev != null) prev.next = myOldSuccessor;
        else {
            if(monitoring) Gut.b3++;
            // update beginning of group
            cg.list = myOldSuccessor;
        }
        // if I have a successor, update him
        if (myOldSuccessor != null) {
            if(monitoring) Gut.b4++;
            myOldSuccessor.prev = myOldPredecessor;
        }

        // now I'm not in the list any more
      
        // if we are lucky, the next group is for a delta larger by 1
        CGroup nextgroup = cg.next;

        if (nextgroup == null) {
            if(monitoring) Gut.b5++;
            // case 2,  there _was_ no next group
            cg.next = new CGroup();
            nextgroup = cg.next;
            nextgroup.next = null;
            nextgroup.prev = cg;
            nextgroup.delta = 1;
        }

        if (nextgroup.delta != 1) {
            if(monitoring) Gut.b6++;
            // rats, must make a new group and adjust the old guy
            CGroup tempg = new CGroup();
            tempg.delta = 1;
            nextgroup.delta--;
            tempg.next = nextgroup; 
            tempg.prev = cg;
            nextgroup.prev = tempg;
            cg.next = tempg;

            nextgroup = tempg;
        }

        // nextgroup has a list to which I should belong
        // just add me at the front of this group
        if (nextgroup.list != null) {   // could be null if I just created it.
            nextgroup.list.prev = this;
            if(monitoring) Gut.b7++;
        }
        next = nextgroup.list;
        nextgroup.list = this;
        prev = null;

        // remember my new group
        cg = nextgroup;
        // I am in.
    

        if (startingGroup.list == null && startingGroup.prev != null) {
            // now, perhaps my original group has no members. (and was not group 0)
            // delete it and adjust deltas.
            cg.delta += startingGroup.delta;
            // link around it
            cg.prev = startingGroup.prev;
            startingGroup.prev.next = cg;
            
            if(monitoring) Gut.b2++;
            
        }
    }
}
