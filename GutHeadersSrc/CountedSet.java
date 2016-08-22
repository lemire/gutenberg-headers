
class CountedSet extends BookKeeper {
    protected static final int frequentEnough = 10;  // how frequent for a LINE to be assumed part of preamble
    static final int logTblSize = 23;
    static final int tblSize = (0x1 << logTblSize);
    
    int mFrequentThreshold;
    int [] mTable; // note, could use bytes and probabilistic counting
    int mTableSize;
    int mNumBits;
   public CountedSet() {
     this(frequentEnough,logTblSize);
   }
   public CountedSet(int frequentThreshold, int tsize_bits) {
        mFrequentThreshold = frequentThreshold;
        mNumBits = tsize_bits;
        int tsize = (0x1 << mNumBits);
        mTableSize = tsize;
        mTable = new int[mTableSize];
	System.out.println("CountedSet with threshold " + mFrequentThreshold + " table size " + tsize);
    }


    // fold the 64 bits into 20 (or however many we use to index)
    private int collapseHash( long hash64) {
        int result=0;
        int bitsLeft = 64;
        while (bitsLeft > 0) {
            int bitsToTake = mNumBits ;
            if (bitsToTake > bitsLeft) bitsToTake = bitsLeft;

            result ^= (hash64 & ((0x1L << bitsToTake)-1));
            hash64 >>>= bitsToTake;
            bitsLeft -= bitsToTake;
        }
        return result;
    }

    public void processItem( String thisItem) {
        int whichCtr =  collapseHash(Crc64MonitoredItem.crc64(thisItem));
        ++mTable[whichCtr];
    }

    public boolean frequent( String thisItem) {
        int whichCtr =  collapseHash(Crc64MonitoredItem.crc64(thisItem));
        //        System.out.println("whichCtr is " + whichCtr + " ctr is " + mTable[whichCtr]);
        return mTable[whichCtr] > mFrequentThreshold;
    }

    public int reportThreshold() { return mFrequentThreshold;}

    public void reportNonZero() {
      for(int k = 0; k < mTable.length; ++k)  {
        if(mTable[k]>0)
          System.out.println(" count " + mTable[k] + ": "+k);
      }
    }
    
}
