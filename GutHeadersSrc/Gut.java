//import java.util.zip.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Gut {
    // 200x80 was noted to be almost too short for epilogue.
    static final int maxPrefixSentences = 300; // 1000;
    static final int maxEpilogueSentences = 300;
    static final int estimatedSentenceLength = 80; 
    static final int maxEpilogueChars = maxEpilogueSentences*estimatedSentenceLength;

    static final int minSentence = 30; // chars


    static final int MIN_LINE_LEN=30; // for linesMode only, counterpart to minSentence
    final int eolLen; // 1 or 2, depending on DOS or UNIX
    

    BookKeeper gen;
    StringCountedSet acc;
    File infile;

    boolean linesMode, heuristicMode, processEpilogue;
    int charCtr;  // don't see this in API: so I know my "seek position"
    int lineForSentenceStart;
    
    /* stat performance counters */
    public static int b1,b2,b3,b4,b5,b6,b7,c1,c2,c3,c4, p1,p2,p3,p4;

    
    public Gut(int n, boolean lines, BookKeeper bk, StringCountedSet bk1, boolean epilogue_too, boolean heur) {
        linesMode = lines;  processEpilogue = epilogue_too;
        /***
        ** it seems like n is never used???
        ***/
        heuristicMode = heur;
        eolLen = System.getProperty("line.separator").length();
        gen = bk;
	      acc = bk1; // frequently null
    }





    class sentenceAccumulator extends sentenceProcessor {
        List<String> result;
        public sentenceAccumulator() {
            result = new ArrayList<String>();
        }
        
        public void action( int linenumber, String sent) {
            result.add(sent);
        }

        List<String> yieldResult() { return result;}
    }
    

    //private LineNumberReader ff;
    
    List<String> itemStreamPreamble(LineNumberReader ff, File f) {
        sentenceAccumulator sp = new sentenceAccumulator();
        doSentencesPreamble(ff,sp);
        return sp.yieldResult();
    }



    List<String> itemStreamEpilogue( LineNumberReader ff,File f) {
        sentenceAccumulator sp = new sentenceAccumulator();
        
        // blindly assume that ff is not closed [or null]
        doSentencesEpilogue(ff,f,sp);
        return sp.yieldResult();
    }






    void doSentencesPreamble(LineNumberReader ff, sentenceProcessor s) {
        try {
            charCtr=0;
            doSentences(ff,s, true);
        } catch (Exception e) {
            System.err.println("ooh, error " + e); e.printStackTrace();
        }
    }
        

    // If we're not already there, jump to the general are of the epilogue
    // major assumptions that I don't know how to test:  the text is 7 or 8-bit ASCII
    // (not true in all cases) so that the "bytes=characters" assumption holds

    void doSentencesEpilogue( LineNumberReader ff, File f, sentenceProcessor s) {
        try {
            // Gutenberg books are not too long.
            int desiredSeekPos = ((int) f.length()) - maxEpilogueChars;
            int deltaPos = desiredSeekPos - charCtr;

            if (deltaPos >= 0) ff.skip(deltaPos); // case 1: we need to skip ahead
            else {
                // case 2
                // short file, so we've overshot already
                // backup and redo (note, we'll double-process lines, may be bad)
                ff.close();
                ff = new LineNumberReader(new FileReader(f));
                // REALLY short file? (case 2a)

                if (desiredSeekPos <= 0) {
                    System.err.println("File " + f.getAbsolutePath() +
                                       " is too short to meet assumptions of header/epilogue skipping.");
                    // continue from the start
                }
                else // case 2b
                    ff.skip(desiredSeekPos);

            }

            charCtr = (desiredSeekPos < 0) ? 0 : desiredSeekPos;
            // we're probably partway through a line; skip its mangled remains
            String junk = ff.readLine();
            if (junk == null) {
                System.err.println("Warning, cannot process " +f.getAbsolutePath()+
                                   " seek failed, probably has non-ASCII contents.");
                return;
            }
            charCtr += ( eolLen + junk.length());

            doSentences(ff,s,false);
        } catch (IOException e) {
            System.err.println("ooh, error " + e); e.printStackTrace();
        }
    }


    Pattern hasLetter = Pattern.compile(".*[a-zA-Z].*");

    boolean noLetters(String s) {
	return ! hasLetter.matcher(s).matches();
    }




    public void doSentences( LineNumberReader ff, sentenceProcessor s, boolean stopEarly) throws IOException {
        // a "Sentence" is a chunk of text of a minimum length and ending with .?!
        // followed by a whitespace

        // exception: any line delimited by *s is a itself a sentence, unless if contains no letters
	// (reason:  *  *  *  *  etc were apparently widely used in book text to set sections off)

        StringBuffer ans = new StringBuffer();       
            
        String line;
        int sentCtr=0;
        while ( (line = ff.readLine()) != null) {
            charCtr += ( eolLen  +  line.length());
            if (ans.length() == 0) lineForSentenceStart = ff.getLineNumber();  // also okay in "lines mode"
           
            line = line.trim();   // may not handle dos-style carriage returns
            if (line.endsWith("\r")) line = line.replaceAll("\\r","");
            if (line.length() == 0) continue;

            if (linesMode && (line.length() < MIN_LINE_LEN)) continue;

            // humans are poor at duplicating large runs of * or - 
            line = line.replaceAll("\\*{3,}","***");
            line = line.replaceAll("-{3,}","---");  //BUG omitted till 2007-03-02

	    if (noLetters(line)) continue;   // added after original measurements; not sure if a good idea

            // is line bracketed by *?  (or is it time for "lines mode?")
            if (linesMode || (line.startsWith("*") && line.endsWith("*"))) {
                // close out existing sentence, if any
                if (ans.length() > 0) {
                    s.action( ff.getLineNumber(), ans.toString());
                    ++sentCtr;
                }
                ans = new StringBuffer();
                // humans are bad at whitespace.
                line = line.replaceAll("\\s+"," ");
                s.action(ff.getLineNumber(), line);
                if (++sentCtr > maxPrefixSentences  && stopEarly) return;
                continue;
            }
                    
            for (String word : line.split("\\s+")) {
                if (word.length() == 0) continue;
                ans.append(word+" ");
                char finalchar = word.charAt(word.length()-1);
                if ( (finalchar == '.' || finalchar == '?' || finalchar == '!') &&
                     word.length() > 2 &&  // get rid of some spurious uses of .
                     ans.length() >= minSentence) {
                    // System.out.println("Sentence: " + ans.toString());
                        
                    s.action(ff.getLineNumber(), ans.toString());
                    if (++sentCtr > maxPrefixSentences && stopEarly) return;
                    ans = new StringBuffer();
                }
            }
        }
        //System.out.println("***** reached EOF *****");
        return;
    }
    
    void readAll(File f) {
        LineNumberReader ff = null;
        try {
            ff = new LineNumberReader(new FileReader(f));
        } catch (IOException e) { System.err.println("IO error "+e); }
        List<String> strm = itemStreamPreamble(ff,f);
        for (String it : strm) {
            gen.processItem(it);
	    if (acc != null) acc.processItem(it);
        }
        if (processEpilogue) {
            // similar thing
            strm = itemStreamEpilogue(ff,f);
            for (String it : strm) {
                gen.processItem(it);
		if (acc != null) acc.processItem(it);
            }
        }
    }





    class findLastHitPreamble extends sentenceProcessor {
        String last = "FAILURE OF ALGORITHM";
                
        public void action( int linenumber, String sent) {
            if (gen.frequent(sent)) 
                last = sent;
        }

        String yieldLast() { return last;}
    }



    String computeLastBoilerLinePreamble(File f) {
        findLastHitPreamble sp = new findLastHitPreamble();
        LineNumberReader ff = null;
        try {
            ff = new LineNumberReader(new FileReader(f));
        } catch (IOException e) { System.err.println("IO error "+e); }
        doSentencesPreamble(ff,sp);
        return sp.yieldLast();
    }



    class findLastHeader extends sentenceProcessor {
        String last = "line 0 : FAILURE OF ALGORITHM";
        boolean hack = true;  // we know that there IS a preamble (somewhere), so allow for some initial crud
        boolean inPreamble = true;
        static final int MAXGAP=10;
        int gapSize = 0;
                
        public void action(int linenumber, String sent) {
            // override, in case the heuristic is enabled
            if (heurHeaderEnd(sent)) last = "line " + /*ff.getLineNumber()*/ linenumber + ": "+sent;

            if (inPreamble) {
                if (gen.frequent(sent)) { 
                    hack = false;
                    // global variable ff helps out, although this is cruft
                    last = "line " + /*ff.getLineNumber()*/ linenumber + ": "+sent;
                    gapSize=0; // restart gap counter
                }
                else {
                    if (++gapSize > MAXGAP && !hack) inPreamble = false;
                }
            }
        }

        String yieldLast() { return last;}
    }

    String computeLastHeaderLine(File f) {
        findLastHeader sp = new findLastHeader();
        LineNumberReader ff = null;
        try {
            ff = new LineNumberReader(new FileReader(f));
        } catch (IOException e) { System.err.println("IO error "+e); }
        doSentencesPreamble(ff,sp);
        return sp.yieldLast();
    }



    class findFirstFooter extends sentenceProcessor {
        boolean inEpilogue = false;
        boolean stayInEpilogue = false;  // set in heuristic mode
        String topOfChunk = null;
        static final int MAXGAP=10;
        int gapSize = 0;
                
        public void action( int linenumber, String sent) {
	    // check for false positives if necessary
	    if (acc != null) acc.checkOn(gen, sent);
            if (heurFooterBegins(sent)) {
                stayInEpilogue = true;
                if (! inEpilogue) topOfChunk = "line "+lineForSentenceStart+": " + sent;
                inEpilogue = true;
            }
            else   
                if (gen.frequent(sent)) { 
                    if (!inEpilogue)
                        topOfChunk = "line "+lineForSentenceStart+": " + sent;
                    gapSize=0; // restart gap counter
                    inEpilogue = true;
                }
                else {
                    if (++gapSize > MAXGAP && !stayInEpilogue) inEpilogue = false;
                }
        }
        
        String yieldLast() { return inEpilogue ? topOfChunk : 
            ("line " + /*ff.getLineNumber() + */": FAILURE OF FOOTER ALGORITHM (or, no footer)");}
    }
    

    String computeFirstFooterLine(File f) {
        LineNumberReader ff = null;
        try {
            ff = new LineNumberReader(new FileReader(f));
        } catch (IOException e) { System.err.println("IO error "+e); }
        findFirstFooter sp = new findFirstFooter();
        doSentencesEpilogue(ff,f,sp);
        return sp.yieldLast();
    }





    
    void reportNonZero() {
        gen.reportNonZero();
    }
    
    public static void main(String [] argv) throws IOException {
        
        
        // process command line args  (-l for "lines mode" or -e for "exact counts")
        boolean l = false, ft = false, h=false, fp=false, cs=false, ex=false, cs64=false, gm=false;
	
        System.out.println("assuming the text files are in 'data'");
        File dataDir = new File("data");
        File [] theFiles = dataDir.listFiles(new FilenameFilter() {
                public boolean accept( File dir, String name) {
                    return name.matches("((.*[012][0-9][a-z]?)|(\\d+))\\.txt");
                    // note one of the test set is a "preliminary" version 09.
                }});        

        // c is a capacity, for countedset and genmajority
	int c = -1;  // impossible value, flag.

	int t = -1;  // t is a frequency threshold.


        for (String arg : argv) {
            if (arg.equals("-l")) l = true;
            else if (arg.equals("--countedset")) { cs=true; System.out.println(arg);} // counted set, will set later
            else if (arg.equals("-e"))   { ex=true; System.out.println(arg);}// exact, will set later
            else if (arg.equals("--crc64countedset")) { cs64=true;  System.out.println(arg);}// Dan's original idea, will set later            
            else if (arg.equals("-f")) ft = true; // footers too (using same data strux)
            else if (arg.equals("-h")) h = true;  // heuristic enabled
	    else if (arg.equals("--falsepositives")) fp = true;  // check false positives
            else if (arg.startsWith("-c")) {
		c = Integer.parseInt(arg.substring(2,arg.length()));
            }
	    else if (arg.startsWith("-t")) {
		t = Integer.parseInt(arg.substring(2,arg.length()));
	    }
            else System.err.println("unknown arg ignored: "+arg);
        }
        
	gm = !cs && !ex && !cs64;

	if ( (gm?1:0) + (cs?1:0) + (ex?1:0) + (cs64?1:0) != 1 || (fp && ex) 
	     || (c!=-1 && (cs64 || ex)))
	    throw new RuntimeException("command-line arguments do not make sense together");
	    
	if (gm && c == -1) {
	    /**
	     * this bit fixes default value for c for GenMajority        */
	    // attempt to compute suitable size
	    double headerProb = 0.03;   // min probability for a given header type
	    
	    int totalLines = maxPrefixSentences * theFiles.length;
	    int minHeaderOccurrences = (int) ( headerProb * (double) theFiles.length);
	    
	    double targetProbability = ( (double) minHeaderOccurrences)  / totalLines;
	    c = (int)  (1/targetProbability);  // chk 0?
	    if (c < 100000) c = 100000;    // now that we have a handle on min/max counts, we don't need to throttle by small memory
	    /**
	     * end of experiments to fix c
	     */
	    System.out.println("c is " + c);
	}        

        BookKeeper bk = null;
	StringCountedSet accurate = null;  // for false positive testing

	if (cs)
	    if (c == -1 && t == -1) bk=new CountedSet(); // counted set
	    else if (c == -1 && t != -1) bk = new CountedSet(t,CountedSet.logTblSize);
	    else if (c != -1 && t == -1) bk = new CountedSet( CountedSet.frequentEnough,
							      (int) Math.round(Math.log(c)/Math.log(2.0)));
	    else bk = new CountedSet(t,(int) Math.round(Math.log(c)/Math.log(2.0)));
	    
	if (cs64) 
	    if (t == -1) bk = new Crc64CountedSet();
	    else bk = new Crc64CountedSet(t);

        if (ex) 
	    if (t== -1) bk=new StringCountedSet();// exact
	    else bk = new StringCountedSet(t);

	if (gm) {
	    if (t == -1) bk = new GenMajority(c);
	    else {
		bk = new GenMajority(c/*,t*/);
		throw new RuntimeException("sorry, no constructor-adjustable threshold for GM (yet)");
	    }
	}

	if (fp) accurate = new StringCountedSet();

        System.out.println("BookKeeper created: "+bk.toString());
        Gut g = new Gut(c,l,bk,accurate, ft,h);
        System.out.println("Gut created");
        for (File f : theFiles) {
            System.out.println("Scanning file " + f);
            g.readAll(f);
            // g.gen.dump();
        }


	// optional report after pass 1
        if( g.gen instanceof GenMajority) 
        {
          System.err.println("suggested threshold for GenMajority: "+ ((GenMajority) g.gen).suggestThreshold() + " (c="+c+", l = "+l+")");
	  //          System.out.println("Press enter to continue");
          //  System.in.read();
        } else if  (g.gen instanceof Crc64CountedSet)   {
          System.err.println("number of distinct lines/sentences: "+ ((Crc64CountedSet) g.gen).size() );
        } else if (g.gen  instanceof StringCountedSet) {
          System.out.println("exact number of distinct lines/sentences: "+((StringCountedSet) g.gen).size());
       }



        //g.reportNonZero();
        for (File f : theFiles) {
            System.out.println("File: "+f);
            System.out.println("last header:"+g.computeLastHeaderLine(f));
            if (g.processEpilogue) 
		System.out.println("first footer:"+g.computeFirstFooterLine(f));
        }

	// optional reports after pass 2

	if (fp) {
	    // note, this does not quite meet paper's idea of false positive
	    // because there may be infrequent items in boilerplate that are now
	    // (mistakenly) classified as frequent.
	    System.out.println("FALSEPOS-fixed t="+ t+ " c=" + c + " "+ accurate.falsePos() + " false positives " + 
			       accurate.truePos() + " true positives " +
			       (accurate.truePos() + accurate.trueNeg()) + " total");
	}
        g.gen.dump();
        dumpStats();

    }


    public static void dumpStats() {
        System.out.println("stat counters:");
        
        System.out.println(" b1="+b1);
        System.out.println(" b2="+b2);
        System.out.println(" b3="+b3);
        System.out.println(" b4="+b4);
        System.out.println(" b5="+b5);
        System.out.println(" b6="+b6);
        System.out.println(" b7="+b7);
        System.out.println(" c1="+c1);
        System.out.println(" c2="+c2);
        System.out.println(" c3="+c3);
        System.out.println(" p1="+p1);
        System.out.println(" p2="+p2);
        System.out.println(" p3="+p3);
    }

    // lifted from litOLAP
    static String endPatString = 
        "([ *]|This|THIS|this|Is|IS|is|The|THE|the|Of|OF|of)*(End|END|end)(\\s|Of|OF|of|The|THE|the|This|THIS|this)*(Project\\s+Gutenberg|PROJECT\\s+GUTENBERG).*";
    static Pattern eMatch = Pattern.compile(endPatString);

    boolean heurFooterBegins( String s) {
        if (!heuristicMode) return false;
	if (s.startsWith("ETEXT")) return true;   // new
        else return eMatch.matcher(s).matches();
    }

    static String hEndPatString = 
        "[ *]*\\*[ ]?(START OF (THE|THIS) PROJECT GUTENBERG|END[ *]THE SMALL PRINT!).*";
    static Pattern bMatch = Pattern.compile(hEndPatString);
    

    boolean heurHeaderEnd( String s) {
        if (!heuristicMode) return false;
        else return bMatch.matcher(s).matches();
    }
    public BookKeeper getBookKeeper() { return gen; }
}

