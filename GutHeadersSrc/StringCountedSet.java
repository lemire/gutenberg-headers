import java.util.*;

class StringCountedSet extends BookKeeper {

    private int truePositive, falsePositive, trueNegative, falseNegative;
    int mFrequentThreshold;
  
    Hashtable<String,Integer> mTable;
   public StringCountedSet() {
     this(CountedSet.frequentEnough);
    }
 
   public StringCountedSet(int frequentThreshold) {
        mFrequentThreshold = frequentThreshold;
        mTable = new Hashtable<String,Integer>();
    }

    public void processItem( String thisItem) {
        if(mTable.containsKey(thisItem)) {
            mTable.put(thisItem,new Integer(mTable.get(thisItem).intValue()+1));
        } else {
            mTable.put(thisItem,new Integer(1));
        }
    }
    
    public int exactCount(String thisItem) {
      if(!mTable.containsKey(thisItem)) return 0;
      return mTable.get(thisItem).intValue();
    }

    public boolean frequent( String thisItem) {
        return mTable.containsKey(thisItem) && (mTable.get(thisItem).intValue() >mFrequentThreshold);
    }
    public boolean frequent( String thisItem,int threshold) {
        return mTable.containsKey(thisItem) && (mTable.get(thisItem).intValue() >threshold);
    }
    
    public void reportNonZero() {
      for(String i: mTable.keySet() ) {
        System.out.println(" count " + mTable.get(i) + ": "+i);
      }
    }

    public int reportThreshold() { return mFrequentThreshold;}

    // this is for false-positive/false negative testing (for false for frequency)
    void checkOn( BookKeeper other, String s) {
	if (frequent(s, other.reportThreshold())) {
	    truePositive++;
	    if (!other.frequent(s)) falseNegative++;
	}
	else {
	    trueNegative++;
	    if (other.frequent(s)) falsePositive++; 
	}
    }
    
    /* package-visible accessors */
    int falsePos() { return falsePositive;}
    int truePos() { return truePositive;}
    int trueNeg() { return trueNegative;}
    
    public int size() { return mTable.size();}

}
