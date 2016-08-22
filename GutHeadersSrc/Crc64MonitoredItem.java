public class Crc64MonitoredItem implements Comparable<Crc64MonitoredItem> {
    long hash;
    
    static Crc64 cs64 = new Crc64();

    public int compareTo(Crc64MonitoredItem other) {
      return (int) ( hash - other.hash);
    }
    public boolean equals(Object other) {
      if(! ( other instanceof Crc64MonitoredItem) ) return false;
      return hash == ((Crc64MonitoredItem) other).hash;
    }
    
    public static long crc64(String item) {
      byte [] itemAsBytes = item.getBytes();      
      cs64.reset();
      cs64.update(itemAsBytes);
      return  cs64.getValue();
    }
    public Crc64MonitoredItem(String item ) {
      hash = crc64(item);
    }
    public Crc64MonitoredItem(long h ) {
      hash = h;
    }

    public String toString() { 
    return "("+hash+")";
    }

    public long hashCode64() {
        return hash;
    }
    
    public int hashCode() {
      return (int)(hash^(hash>>>32));
       
    }
    
}
