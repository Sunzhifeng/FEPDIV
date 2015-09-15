package tool;

public class Stopwatch { 
	public static int SECOND=1000;
    private final long start;

    /**
     * Initialize a stopwatch object.
     */
    public Stopwatch() {
        start = System.currentTimeMillis();
    } 


    /**
     * Returns the elapsed time (in seconds) since this object was created.
     */
    public double elapsedTime() {
        long now = System.currentTimeMillis();
       return DataFilter.roundDouble((double)(now - start)/SECOND,2) ;
       
    }   

} 


