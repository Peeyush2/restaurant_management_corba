import Client.HelloClient;

//used for calculation of erros and scalability rate


class MyRunnable implements Runnable {
    	private static int counter = 0;
    	private static long startTime = 0;
    	private static long endTime = 0;
        private static final Object lock = new Object();
        private static final Object lock2 = new Object();

		String[] args = {};
	   public MyRunnable(String[] givenARGS) {
	       // store parameter for later user
		   args = givenARGS;
	   }

	   public void run(){
		   try {
	            // Displaying the thread that is running
			   setStart();
	            System.out.println(
	                "Thread " + Thread.currentThread().getId()
	                + " is running");
	            HelloClient clients = new HelloClient();
	            clients.main(args);	
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

public class PerformanceMeasurement {
	
	
	
	public static void main(String[] args) {
		int clients_count = 1000;
		if(args.length == 5) {
			clients_count = Integer.parseInt( args[4]);
		}
		
		
		int error = 0;
		
		String[] stringArgs = { "custom" };
		long startTime = System.currentTimeMillis();
		try {
			for(int i=0; i< clients_count;i++) {
				try {
					
					System.out.println("Hello");
					Runnable r = new MyRunnable(args);

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
