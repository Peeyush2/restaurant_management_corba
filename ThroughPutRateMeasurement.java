import Client.HelloClient;

//used for calculation of throughput rate

class MyRunnable2 implements Runnable {
	private static int counter = 0;
	private static long startTime = 0;
	private static long endTime = 0;
    private static final Object lock = new Object();
    private static final Object lock2 = new Object();

	String[] args = {};
	HelloClient myClient = null;
   public MyRunnable2(String[] givenARGS, HelloClient clients) {
       // store parameter for later user
	   args = givenARGS;
	   myClient = clients;
   }

   public void run(){
	   try {
            // Displaying the thread that is running
		   setStart();
            System.out.println(
                "Thread " + Thread.currentThread().getId()
                + " is running");
           myClient.main(args);
            setEnd();
            System.out.println("Time Elapsed : "+(startTime - endTime));
        }
        catch (Exception e) {
            // Throwing an exception
        	
        	
        	increaseCounter();
            System.out.println("Exception is caught");
        }
	   printAll();
	   
   }
   
    private void increaseCounter() {
        synchronized (lock) {
            System.out.println("Current thread here : "+Thread.currentThread().getName() + " : " + counter);
            counter++;
            System.out.println("Time vals: "+(startTime - endTime));
        }
    }
    private void setStart() {
        synchronized (lock) {
        	System.out.println("Current thread time here : "+Thread.currentThread().getName() + " : " + startTime);
            startTime = Math.min(System.currentTimeMillis(), startTime);
            if(startTime == 0) {
            	startTime = System.currentTimeMillis();
            }
        }
    }
    private void setEnd() {
        synchronized (lock) {
            System.out.println("Current thread here : "+Thread.currentThread().getName() + " : " + endTime);
            endTime = Math.max(System.currentTimeMillis(), endTime);
            if(endTime == 0) {
            	endTime = System.currentTimeMillis();
            }
        }
    }
    
    private void printAll() {
    	 synchronized (lock) {
    		 System.out.println("Time Elapsed: "+( endTime - startTime ) );
    		 System.out.println("Errors occurred: "+ (counter));
    	 }
    }
}

public class ThroughPutRateMeasurement {
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int call_count = 1000;
		if(args.length == 5) {
			call_count = Integer.parseInt( args[4]);
		}
		
		
		System.out.println("Using calls: "+(call_count));
		int error = 0;
		 HelloClient clients = new HelloClient();
		
		String[] stringArgs = { "custom" };
		long startTime = System.currentTimeMillis();
		try {
			for(int i=0; i< call_count;i++) {
				try {
					
					System.out.println("Hello");
					Runnable r = new MyRunnable2(args,clients);

	            new Thread(r).start();
					
				}catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error here");
					error++;
				}	
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error += 1;
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Time elapsed :" + (endTime - startTime));
		

	}

}
