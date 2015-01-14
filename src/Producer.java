
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Producer {
   
    //Queues shared between all consumers as well as producer
    ConcurrentLinkedQueue<Message> timeQueue;
    ConcurrentLinkedQueue<Message> consumerStatusQueue;
    
    //HashMap to keep track of registered consumers
    //key: consumer_key; value: timestamp of consumer's latest keepAlive or register message
    ConcurrentHashMap <Integer,Date> consumers;
    
    //thread used for accessing timeQueue
    Thread time_thread;
    
    //thread used for accessing consumerStatusQueue
    Thread consumerStatus_thread;
     
  Producer( ConcurrentLinkedQueue<Message> t_q, ConcurrentLinkedQueue<Message> c_q){
       timeQueue = t_q;
       consumerStatusQueue = c_q;
       consumers = new ConcurrentHashMap <Integer,Date>();
       System.out.println("Creating producer" );       
   }
     
    public void startThreads () {
      System.out.println("Starting producer threads" );
      consumerStatusThread cs = new consumerStatusThread();
      timeThread t = new timeThread();
      if (consumerStatus_thread == null) {
         consumerStatus_thread = new Thread (cs, "producer");
         consumerStatus_thread.start ();
      } 
      if (time_thread == null) {
         time_thread = new Thread (t, "producer");
         time_thread.start ();
      }
  }
    
 class timeThread implements Runnable {      
     
       public void run() {              
         try { 
            while (true) {
                send_date(); 
                //Waiting 1 second before sending another time message
                Thread.sleep(1000);}
         } catch (Exception ex) {
            ex.printStackTrace();
         }         
       }
       
       //For each eligible consumer_key in stored, registered consumers HashMap:
       //Adds a message with current timestamp and given consumer_key to timeQueue
       //Messages are only added for consumers that have reported within the last 10 seconds
       void send_date() {    
            Date current_date = new Date();
            Iterator it = consumers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                //If the consumer hasn't reported in the last 10 seconds, removing the
                //consumer from stored, registered consumers, and continuing to the next consumer
                if ((current_date.getTime() - 
                ((Date)pairs.getValue()).getTime())/1000 > 10) {
                    consumers.remove((Integer)pairs.getKey());
                    continue;
                }
               Message m = new Message(current_date.toString(),(Integer)pairs.getKey());
               timeQueue.add(m);
            }         
         } 
    }
    
     class consumerStatusThread implements Runnable {
         
            public void run() {
              //Keeps checking until a message becomes available in consumerStatusQueue                  
               while (true){ 
                   try {
                       Object o = consumerStatusQueue.poll();
                       if (o instanceof Message) {
                           checkConsumerStatus((Message)o);
                       }
                   } catch (Exception ex) {
                        ex.printStackTrace();
                   }
                }
        }
     
        void checkConsumerStatus(Message m) {         
                
                //Stores new registered consumer key and current timestamp,
                //if the consumer is registering 
                if (m.content.startsWith("register")){
                    consumers.put(m.consumer,new Date());
                }
                //Updates the consumer's latest timestamp, if the consumer 
                //sent a keepAlive message that is within 10 seconds of this consumer's
                //prior register or keepAlive message
                else if (m.content.startsWith("keepAlive")) {
                    Date current_date = new Date();
                    if ( (current_date.getTime() - consumers.get(m.consumer).getTime())/1000 <= 10) {
                            consumers.put(m.consumer, current_date);} 
                }
            }
    }
      

}
