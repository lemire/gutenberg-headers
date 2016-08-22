import java.util.*;

class Crc64CountedSet extends BookKeeper {
     
    int mFrequentThreshold;
  
    Hashtable<Crc64MonitoredItem,Integer> mTable;
   public Crc64CountedSet() {
     this(CountedSet.frequentEnough);
    }
 
   public Crc64CountedSet(int frequentThreshold) {
        mFrequentThreshold = frequentThreshold;
        mTable = new Hashtable<Crc64MonitoredItem,Integer>();
    }

    public void processItem( String thisItem) {
        Crc64MonitoredItem mi = new Crc64MonitoredItem(thisItem);
        if(mTable.containsKey(mi)) {
            mTable.put(mi,new Integer(mTable.get(mi).intValue()+1));
        } else {
            mTable.put(mi,new Integer(1));
        }
    }

    public int count(String thisItem) {
      Crc64MonitoredItem mi = new Crc64MonitoredItem(thisItem);
      if(! mTable.containsKey(mi)) return 0;
      return mTable.get(mi).intValue() ;
    }

    public boolean frequent( String thisItem) {
      Crc64MonitoredItem mi = new Crc64MonitoredItem(thisItem);
      return mTable.containsKey(mi) && (mTable.get(mi).intValue() >mFrequentThreshold);
    }
    

    public int reportThreshold() {
	return mFrequentThreshold;
    }

    public void reportNonZero() {
      for(Crc64MonitoredItem i: mTable.keySet() ) {
        System.out.println(" count " + mTable.get(i) + ": "+i);
      }
    }
    
    public int size() { return mTable.size();}

}
