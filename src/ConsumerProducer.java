
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsumerProducer {
    
 static ArrayList <Consumer> consumers = new ArrayList <Consumer> ();
 static Producer producer;
 
 
 //Two shared queues that will be accessed by the Producer's and Consumers' threads:
    
 //timeQueue: stores time messages sent by the Producer to the relevant Consumers
 //The producer's time_thread places these messages in the queue
 //Each consumer's time_thread checks the messages on this queue, and retrieves them
 //if they are destined for that particular consumer 
 static ConcurrentLinkedQueue<Message> timeQueue;

 //consumerStatusQueue: stores consumers' register and keepAlive messages 
 //Each consumer's consumerStatus_thread places these messages in the queue
 //The producer's consumerStatus_thread retrieves these messages from the queue
 static ConcurrentLinkedQueue<Message> consumerStatusQueue;
 
 //This application takes the number of consumers to start as a command-line argument.
 public static void main(String[] args) throws InterruptedException {
        
       timeQueue = new ConcurrentLinkedQueue<Message>();
      
       consumerStatusQueue = new ConcurrentLinkedQueue<Message>();
        
        //Grabbing the command-line argument
        int numConsumers = Integer.parseInt(args[0]);
        
        //Creating a producer, and starting its threads
        producer = new Producer(timeQueue,consumerStatusQueue);
        producer.startThreads();
        
        //Creating consumers, and starting their threads 
        for (int i = 0; i < numConsumers; i ++){        
            Consumer c = new Consumer(i,timeQueue,consumerStatusQueue); 
            consumers.add(c);
            c.startThreads();
        }  
        
    }
    
}
