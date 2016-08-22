public class TestGut {


    // this tests the algorithm/data structure for generalized majority,
    // but not all the ad-hackery in Gut.java

    public static void feed( BookKeeper gm, int [] data) {
        for (int d : data)
            ((GenMajority)gm).processItem( new Crc64MonitoredItem(d));

    }


    public static void main( String [] j) {

        int [] input;


        Gut g = new Gut(4, false, new GenMajority(4), null, false,false);


        System.out.println("In checking results, you need to mentally accumulate deltas\n");

        input = new int [] {1,2,2,3,2,4};
        System.out.println("expect none at 0, 3 at 1 and 1 at 3");
        feed(g.gen, input);
        g.gen.dump();

        // bump 1,2,4 up to 2
        feed(g.gen, new int []{1,3,4});
        System.out.println("expect none at 0, 3 at 2, 1 at 3");
        g.gen.dump();

        // demote everyone, no counter to allocate
        feed(g.gen, new int [] {5});
        System.out.println("expect none at 0, 3 at 1, 1 at 2");
        g.gen.dump();

        // demote everyone, still no counter to allocate
        feed(g.gen, new int [] {5});
        System.out.println("expect 3 at 0, 1 at 1");
        g.gen.dump();

        // bump up so that we have a bunch of singleton guys
        //feed(g.gen, new int [] {1,2,3,4,1,2,1,1});
        feed(g.gen, new int [] {1,3,4,1,2,1,1});
        System.out.println("expect 0 at 0, 2 at 1, 1 at 2, 1 at 4 ");
        g.gen.dump();

        // steal someone's counter (first, demote all.  Then then allocate 6 from either 3 or 4}
        feed(g.gen, new int [] {5,6});
        System.out.println("expect 1 at 0, 2 at 1, 1 at 3 ");
        g.gen.dump();

        // experimentally, see whether rescue from garbage can works..
        feed(g.gen, new int [] {7,8});  // 7: get used, 0 at 0; 3 at 1; 1 at 3; 8: demote all
        feed(g.gen, new int [] {7,9});  // 7 recycled; 9 new
        System.out.println("expect 1 at 0, 2 at 1, 1 at 2 ");
        g.gen.dump();

        int numKeys = ( (GenMajority) g.gen).monitoredItems.keySet().size();
        System.out.println("expect number of monitored items should be 4, and it is "+numKeys);
   
        System.out.println("ensure that no counter below is 0, or there is definitely unreached code\n");
        g.dumpStats();  // ensure that we have 100% block coverage
    }






}
