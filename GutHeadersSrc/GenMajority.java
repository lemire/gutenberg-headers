/**
  implementation of the algorithm in Section 3.2 of Demaine, Lopez-Ortiz, Munro 
  "Frequency Estimation of Internet Packet Streams with Limited Space", ESA 2002
  idea also apparently appears in early 80s work by Misra and Gries [1982] (though Misra and Gries didn't have the O(1) per item implementation) 
  FOr a related survey and new algorithms, see "What's Hot and What's Not: Tracking Most Frequent Items Dynamically, by  GRAHAM CORMODE and 
  S. MUTHUKRISHNAN, ACM Transactions on Database Systems, Vol. 30, No. 1, March 2005, Pages 249--278.
  implemented by Owen Kaser, January 2007
*/

import java.util.*;

public class GenMajority extends BookKeeper {
    public static final boolean monitoring = true;
    public static final boolean danielverbose = false;
    public static final int default_threshold = 5;
    public int myM, myN;
    CGroup groups;  // list starts with group0
    HashMap<Crc64MonitoredItem,Counter> monitoredItems;

    protected void cutCounts() {
        CGroup group1 = groups.next; 
        if(monitoring) Gut.c1++;  // these are for performance monitoring and could be removed if necessary
        if (group1 != null) {
            // there is someone with a count!
            if(monitoring) Gut.c2++;
            group1.delta--; // decreases all nonzero
            if (group1.delta == 0) {
                // we must merge the two groups
                // we probably should track ends of lists instead;
                if(monitoring) Gut.c3++;
                // the algorithm is such that this careful list merging is
                // unnecessary, as this routine is only ever called when
                // group 0's list is empty
                //
                Counter listEnd = group1.list;  // never null, though group 0's list might be
                while (listEnd.next != null) {
                    // they move to group 0
                    listEnd.cg = groups;
                    listEnd = listEnd.next;    
                }
                listEnd.cg = groups;
                listEnd.next = groups.list;  // attach newly 0 to start of old0
                if (groups.list != null) groups.list.prev = listEnd;
                groups.list = group1.list;

                groups.next = group1.next;  // remove group1
                if (groups.next != null) groups.next.prev = groups;
            }
        }
    }

   void processItem( String thisItem) {
       processItem( new Crc64MonitoredItem(thisItem) );       
    }
    
    void processItem( Crc64MonitoredItem mi) {
        ++myN;       
        Counter itemCtr = monitoredItems.get(mi);        
        if(monitoring) Gut.p1++;
        if (itemCtr == null && groups.list != null ) {  // nobody monitors this and there is room left to add it
            if(monitoring) Gut.p2++;
            // un-monitor whoever was formerly monitored by this, if any.
            Counter victim = groups.list;
            if (victim.myItem != null) {
                if(monitoring) Gut.p3++;
                monitoredItems.remove(victim.myItem);
            }
            // and use this counter to monitor thisItem
            victim.myItem = mi;
            monitoredItems.put(mi,victim);
            itemCtr = victim;
        }
        if (itemCtr != null) { 
            // either the item was present or it was 
            // recently added
            itemCtr.bumpUp();
        }
        else // the item is not there, decrement everyone 
          cutCounts();

        // sanityCheck();
    }



    public GenMajority(int m) {
        myM = m;
        myN = 0;
        monitoredItems = new HashMap<Crc64MonitoredItem,Counter>();
        groups = new CGroup();
        // make the counters for group 0
        for (int i=0; i < m; ++i) {
            Counter temp = new Counter(groups);
            temp.next = groups.list;
            if (groups.list != null) groups.list.prev = temp;
            groups.list = temp;
        }
    }


    void reportNonZero() {
      int cnt = 0;
      if (groups == groups.next) 
        throw new RuntimeException("cycle in groups list");
      for (CGroup cg = groups.next; cg != null; cg = cg.next) {
        cnt += cg.delta;
        System.out.print(" count " + cnt + ": ");
        for (Counter c = cg.list; c != null; c = c.next) {
          System.out.print( c.myItem);
        }
        System.out.println();
      }
    }

    // Want that item is not in a group whose delta-sum is less than 3 or 5 or...
    public boolean frequent( String thisItem) {
      return frequent(thisItem,default_threshold);
    }

    public boolean monitored(String thisItem) {
       Crc64MonitoredItem mi = new Crc64MonitoredItem(thisItem);
       return monitoredItems.containsKey(mi);
    }
    
    /** 
    * this will be a lower bound on how often the item appears in the stream
    *
    * if you are going to call this repeatedly, better precompute it
    */
    public int count(String thisItem) {
       Crc64MonitoredItem mi = new Crc64MonitoredItem(thisItem);
       if (!monitoredItems.containsKey(mi)) return 0;
       CGroup mygroup = monitoredItems.get(mi).cg;
       if (mygroup == groups) return 0;
       int totalsofar = 0;
       CGroup currentgroup = groups; 
       while(true) {
         currentgroup = currentgroup.next;
         totalsofar += currentgroup.delta;
         if(mygroup == currentgroup) return totalsofar;
       }
    }

    /**
    * This computes how many fake items you need to insert into the
    * stream so that all elements under the given threshold will be 
    * replaced by fake items.
    */
    public int howManyFake(int threshold) {
      int totalsofar = 0;
      int numbersofar = 0;
      System.out.println("======");
      for (CGroup t = groups; t != null; t = t.next) {
        totalsofar += t.delta;
        System.out.println("totalsofar = "+totalsofar+" delta="+t.delta+" numbersofar="+numbersofar+" t.cout= "+t.count());
        if(numbersofar < totalsofar)
           numbersofar = totalsofar;
        numbersofar += t.count();
        if(totalsofar >= threshold)
          return numbersofar;
      }
      throw new RuntimeException("can't find it, your threshold is probably too agressive");
    }
    


    /**
    * This is the threshold of exactness
    */
    // suggests "731" for dvd, with myM = whatever(10k) //Daniel: this was with old code
    public int suggestThreshold() {
      int currentcount = 0;
      int numberoffakes = 0;
      for (CGroup t = groups; t != null; t = t.next) {
        currentcount += t.delta;
        if(currentcount >= ( myN + numberoffakes ) / (double)(myM + 1)) {
                 System.out.println("at value "+currentcount+" I have added "+numberoffakes+" fakes and theory bound is "+( myN + numberoffakes ) / (double)(myM + 1));
                 return currentcount-1;
        }
        // we need to make sure that the number of fakes is sufficient so that the the level
        // is at zero before we take them out the number of items at this level
        if(numberoffakes < currentcount) numberoffakes = currentcount;
        numberoffakes += t.count();

      }
      throw new RuntimeException("can't find it, increase available memory");
    }
    


    /**
    * if you are going to call this repeatedly, better precompute it
    */
    public boolean frequent( String thisItem, int threshold) {
        Crc64MonitoredItem mi = new Crc64MonitoredItem(thisItem);
        if(danielverbose) System.out.println("Checking "+thisItem+" mi="+mi);
        if (!monitoredItems.containsKey(mi)) return false; // not even in recycle bin
        if(danielverbose) System.out.println("Item "+mi+" found!");
        if(danielverbose)  {for (Crc64MonitoredItem i : monitoredItems.keySet())
          System.out.println("   "+i+ " in group "+monitoredItems.get(i).cg+" "+mi+"=="+i+" = "+(i==mi)+" "+ monitoredItems.containsKey(mi)+ " "+ monitoredItems.containsKey(i)+" "+monitoredItems.containsKey(new Crc64MonitoredItem("ostie")));
        }
        CGroup mygroup = monitoredItems.get(mi).cg;
        if (mygroup == groups) return false; // group0 is the recycle bin
        int totalsofar = 0;
        CGroup currentgroup = groups;        
        for(int c = 0; c< threshold; ++c) {
          currentgroup = currentgroup.next;
          if(currentgroup == null) return false;
          totalsofar += currentgroup.delta;
          if(danielverbose) System.out.println("group "+(c+1)+ " total = "+totalsofar);
          if(danielverbose) if(mygroup == currentgroup) System.out.println(" found "+mi+" in "+currentgroup+ " with total "+totalsofar);
          if(mygroup == currentgroup) return totalsofar > threshold; // it must be an inequality for the case threshold = 0 for which you recover Demaine's original algo.         
        }
        if(danielverbose) System.out.println("*** should never make it here***");
        return true; // should never make it here
        /*
        if(danielverbose) System.out.println("[frequent] we are in group 1"+groups.next.delta);        
        // at this point, we know a group 1 exists
        if (monitoredItems.get(mi).cg == groups.next) return (groups.next.delta >= threshold);
        if(danielverbose) System.out.println("[frequent] we are in group 2");
        // at this point, we know a group 2 exists
        if (monitoredItems.get(mi).cg == groups.next.next) 
            return (groups.next.delta+groups.next.next.delta >= threshold);
        if(danielverbose) System.out.println("[frequent] we are in group 3");
        // at this point, we know a group 3 exists, and the item is in it or beyond
        // hence its delta sum is 3 or more
        return true;*/
    }

    public int reportThreshold() {
	return default_threshold;
    }

    void dump()
    {
        for (CGroup t = groups; t != null; t = t.next) {
            if (t.next != null && t.next.prev != t) 
                throw new RuntimeException("Group (delta="+t.delta+") prevs messed up");
            System.out.println(t);
        }

    }
    
    void completeDump()    {
      for (Crc64MonitoredItem i : monitoredItems.keySet())
        System.out.println(i+ " in group "+monitoredItems.get(i).cg);
    }
        
    void sanityCheck()
    {
        for (CGroup t = groups; t != null; t = t.next) {
            t.checkList();
        }
    }
}
