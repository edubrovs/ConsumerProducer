
public class Message { 
    
    String content;
    
    //Indicates consumer_key for consumer of origin or consumer of destination
    int consumer;
    
    public Message(String c,int s){
        content = c;
        consumer = s;
    }
}
