

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Consumer {

    //consumer's unique identifier
    int consumer_key;
    
    //consumer's randomly determined number of keepAlive messages to be sent 
    int quantity_keepAlives_toSend;
    
    //Queues shared between all consumers as well as producer
    ConcurrentLinkedQueue<Message> timeQueue;
    ConcurrentLinkedQueue<Message> consumerStatusQueue;
    
    //thread used for accessing timeQueue
    Thread time_thread;
    
    //thread used for accessing consumerStatusQueue
    Thread consumerStatus_thread;
    
    
    //Variables for JUnitTest:
    
    //first timestamp received from producer
    String first_timestamp_received;
    //last timestamp received from producer
    String last_timestamp_received; 
    //total quantity of time messages received from producer 
    int quantity_timeMessages_received = 0;
    
    Consumer( int key,ConcurrentLinkedQueue<Message> t_q,ConcurrentLinkedQueue<Message> c_q){
       consumer_key = key;
       timeQueue = t_q;
       consumerStatusQueue = c_q;
       System.out.println("Creating consumer " +  consumer_key );
   }
  
    
  public void startThreads () {
      System.out.println("Starting consumer threads" );
      consumerStatusThread cs = new consumerStatusThread();
      timeThread t = new timeThread();
      if (consumerStatus_thread == null) {
         consumerStatus_thread = new Thread (cs, "consumer"+consumer_key);
         consumerStatus_thread.start ();
      } 
      if (time_thread == null) {
         time_thread = new Thread (t, "consumer"+consumer_key);
         time_thread.start ();
      }
  }
    
  class timeThread implements Runnable {      
     
     public void run() {
               //Keeps checking until a message intended for this particular consumer
               //is available in the timeQueue
               while(true){                 
                     Object o = timeQueue.peek();
                       if (o instanceof Message) {
                           if (((Message)o).consumer== consumer_key) {                       
                                timeQueue.remove((Message)o);
                                receiveTimeMessage((Message)o); }
                       }
              }
     }
     
      void receiveTimeMessage(Message m){
          //Prints to console: consumer key, and timestamp received from producer
          System.out.println("consumer thread " + consumer_key + " received " + m.content);
          
          //Updates variables to be used in JUnitTest 
          if (first_timestamp_received == null) {
              first_timestamp_received = m.content;
          }
          last_timestamp_received = m.content;
          quantity_timeMessages_received+=1;
      }
  }
  
  class consumerStatusThread implements Runnable {
                   
     public void run() {
       try {
           //Registers this consumer with the producer
            sendMessageToProducer("register",consumer_key);
            
            Random r = new Random();
            //Determines a random quantity of keepAlive messages to send out (with max at 12)
            quantity_keepAlives_toSend = r.nextInt((12 - 0) + 1);
            System.out.println(consumer_key + " will send " + quantity_keepAlives_toSend + " keepAlive messages");
            
            int counter = 0;      
            //Sends a keepAlive message every 5 seconds, until the granted limit is reached
            while (counter < quantity_keepAlives_toSend) {
                Thread.sleep(5000);
                sendMessageToProducer("keepAlive",consumer_key);
                counter+=1;
            }  
        } catch (Exception ex) {
            ex.printStackTrace();
        }
     }
        void sendMessageToProducer(String type,int key) throws Exception {
            //Creates the message, and adds it to the consumerStatusQueue
            Message m = new Message(type,key);
            consumerStatusQueue.add(m);      
        }
  }
        
    
    
}