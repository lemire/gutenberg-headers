/**
* The idea here is to show that applying Demaine's algo with thresholding
* is a fast and robust approach for the case where the distribution of the
* histogram of the number of occurrences if very skewed.
*
* The problem we seek to solve is to find all elements occuring more than
* k times.
* 
* The general selling point would be: 
*  - faster than hashtable
*  - can be "exact" (as precise as hashtable), but the downside is that you
*   fix the memory, you assume a skewed distribution, and you get the threshold
*   only after you've ran the code (as a function of the memory you did put in).

*
* TODO:
* - we can probably derive some hard theoretical results to support this?
* - does this buy us anything for the Gutenberg template problem?
*
* Naturally, Owen's implementation of Demaine's algo  might not be so fast... but it 
* has the potential of being quite fast I think.
*/

import java.util.*;

public class TestZipfDemaine {
  /**
  * Demaine sometimes allow exact results
  */
  public static void computeExactResult(int n, int distinct,  double exponent) {
          double Zipfconstant = Zipfmodel(n, distinct, exponent);
          /**
          * Firstly, we simulate a Zipf-like distribution
          */
          Vector<String> theText =  getZipfDistributedText( n,distinct, exponent);
          /**
          * next, for different budgets, we determine what exact result
          * is possible
          */
          for (int m = 50; m < distinct; m+=(distinct)/5) {
            GenMajority gm = new GenMajority(m);
            for( String i : theText)  gm.processItem(i);
            int newthreshold = gm.suggestThreshold();
            int topele = (int) (Zipfconstant/(double)newthreshold);
            //System.out.println(gm.meaningOfThreshold(newthreshold));
            System.out.println(" using "+m+" elements, exact result set for thres. "+newthreshold+" or the top "+topele+" elements, efficiency = "+ topele/(double)m);
          }
  }

  public static void unitTesting(int n, int distinct, double exponent) {
         System.out.println("Running unit test");
         /**
          * Firstly, we simulate a Zipf-like distribution
          */
          Vector<String> theText =  getZipfDistributedText( n,distinct, exponent);
          // we try various memory budget
          for(int m = 30;m < 1010; m+=10) {
            System.out.println("m = "+m);
            int exactthreshold = 0;
            GenMajority gm;
            int gmnumber = 0;
            // first we apply the genmajority algo
            {
              long before = System.currentTimeMillis();
              gm = new GenMajority(m);
              for( String i : theText)  gm.processItem(i);
              long after = System.currentTimeMillis();
              exactthreshold = gm.suggestThreshold();
              System.out.println("Threshold = "+exactthreshold);
              //System.out.println("Fakes = "+gm.howManyFake(newthreshold));
              System.out.println("time: "+(after-before)+" ms");
              gmnumber = 0;
              System.out.println("Here are all elements appearing more than "+exactthreshold+" times");
              System.out.println("(Fast Demaine approach ) ");
              for (int i=1; i <= distinct; ++i) {
                // the assumption here is that you can apply the exactthreshold and it is the
                // same as painfully adding the fake items
                if(gm.frequent("a"+(i-1),exactthreshold)) {
                    System.out.println("a"+(i-1));
                    ++gmnumber;
                }
              }
              System.out.println("number of items = "+gmnumber);
            }
            // next we test that it is exact
            {
              long before = System.currentTimeMillis();
              StringCountedSet sc = new StringCountedSet();
              for( String i : theText)  sc.processItem(i);
              long after = System.currentTimeMillis();
              System.out.println("time: "+(after-before)+" ms");
              int number = 0;
              System.out.println("Here are all elements appearing more than "+exactthreshold+" times");
              System.out.println("(Exact approach ) ");
              System.out.println("( element / true count / Demaine's count ) ");
              for (int i=1; i <= distinct; ++i) {
                if(sc.frequent("a"+(i-1),exactthreshold)) {
                  System.out.println("a"+(i-1)+ " "+sc.exactCount("a"+(i-1))+ " "+ gm.count("a"+(i-1)));
                  ++number;
                }
              }
              System.out.println("number of items = "+number);
              if(gmnumber != number) { 
                 System.out.println("======BUG============");
                 System.out.println("===Dumping content == ( element / true count / Demaine's count )");
                 for (int i=1; i <= distinct; ++i) {
                   if(gm.monitored("a"+(i-1))) {
                        System.out.println("a"+(i-1)+ " "+sc.exactCount("a"+(i-1))+ " "+ gm.count("a"+(i-1)));
                   }
                }
                throw new RuntimeException("Something is wrong");
              }
            }
          }
  }

    public static void main(String [] params) {
      //System.out.println("Zipf2");
      int m = 30;
      for(double exponent = 0.6; exponent <= 3.0; exponent+= 0.2) {
        System.out.println(exponent+" "+testAccuracy(100000, 1000, exponent,m)/m);
      }
      /*
      //test1(100000, 1000, 100, false);
      int distinct = 1000;
      int n = 100000;
      unitTesting(n,distinct,1.0);
      /*
      * Ok, now should be sane
      */
      /*distinct = 10000;
      n = 1000000;
      System.out.println("standard Zipf ");
      computeExactResult(n, distinct,1.0);
      System.out.println("power-of-2 Zipf ");    
      computeExactResult(n, distinct,2.0);
      System.out.println("benchmarking... ");    
      */
    }

    public static double testAccuracy(int n, int distinct, double exponent, int m) {
     Vector<String> theText = getZipfDistributedText(n,distinct,exponent);
     int times = 20;
     int values = 0;
     for (int k = 0 ; k < times; ++k ) {
      Collections.shuffle(theText);
      GenMajority gm = new GenMajority(m);
      for( String i : theText)  gm.processItem(i);
      Hashtable<String,Integer> ht = new Hashtable<String,Integer>(); 
      for (int i=1; i <= distinct; ++i) {
        if(gm.monitored("a"+i))
          ht.put("a"+i,new Integer(gm.count("a"+i)));
      }
      int i = 0;
      for(; i < distinct; ++i) {
        Integer myvalue = ht.get("a"+(i+1));
        if(myvalue == null) break;
        for (Integer x : ht.values()) {
            if(x.intValue() > myvalue.intValue())
                  break;
        }
        ht.remove("a"+(i+1));
      }
      values += i;
      //return i;
     }
     return values / (double) times;
     //System.out.println("The first "+i+" counts are sorted out of "+m);
    }


   public static Vector<String> getZipfDistributedText(int n, int distinct, double exponent) {
          double Zipfconstant = Zipfmodel(n, distinct, exponent);
          assert distinct < 10000;// 16 bits char model
          Vector<String> theText = new Vector<String>();
          Hashtable<String,Integer> ht = new Hashtable<String,Integer>(); 
          int counter = 0;
          for (int i=1; i <= distinct; ++i) {
            //System.out.println(i);
            int p_i = (int) Math.round(Zipfconstant/Math.pow(i,exponent));
            counter += p_i;
            //System.out.println(p_i);
            ht.put("a"+(i-1) , new Integer(p_i));
            for (int c=0; c < p_i; ++c) {
                theText.add( "a"+(i-1)  );
            }
          }
          //System.out.println("number of elements = "+ counter + " ("+n+")");
          return theText;
    }

    /**
    * this returns a constant used
    * by the Zipf model.
    */
    public static double Zipfmodel(int textlength, int distinctwords, double exponent) {
            double sum = 0.0;
            for (int k = 1; k <= distinctwords; ++k) sum+= 1.0/Math.pow(k,exponent);
            return textlength/sum;
    }
}
