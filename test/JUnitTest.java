
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JUnitTest {
    static ConsumerProducer cp;
    static String inputNum;
   
   public JUnitTest(String input) {
      this.inputNum = input;
   }

   //Enables mutiple testing (on different parameters)
   //In this case, first sending {"1"} to ConsumerProducer's main method
   //Subsequently, sending {"2"} to ConsumerProducer's main method
   @Parameterized.Parameters
   public static Collection numberOfConsumers() {
      return Arrays.asList(new String[][] {{"1"},{"2"}});
   }
   
    @Test
    public void test() throws InterruptedException {
        System.out.println("Testing for number of consumers : " + inputNum);
        cp = new ConsumerProducer();
        cp.main(new String[] {inputNum});
        
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZ yyyy", Locale.US); 
        Date d_last,d_first;
        Consumer c;
        
        //80 seconds: Providing time for message-creation to stop 
        //Calculation: 
        //5 seconds x 12 = 60 (12: maximum possible number of keepAlive messages to send, at 5 second intervals)
        //60 + 10 = 70 (producer waits for 10 extra minutes before cutting off updates to a consumer)
        //10 seconds extra 
        TimeUnit.SECONDS.sleep(80);
        
        //Looping through the ConsumerProducer's consumers
        for (int i = 0; i < cp.consumers.size(); i ++) {
            try {
                c = cp.consumers.get(i);
                
                //Parsing, from String format back to Date,
                //earliest and latest timestamp messages received from producer 
                d_last = format.parse(c.last_timestamp_received);
                d_first = format.parse(c.first_timestamp_received);
                        
                //The difference in time between the latest and earliest timestamp messages
                //should be c.quantity_keepAlives_toSend * 5 + 9
                //A range of 8 through 11 is made available because slow processing on my personal
                //computer made timing imprecise 
                assertTrue(
                 (d_last.getTime()-d_first.getTime())/1000 >= c.quantity_keepAlives_toSend * 5 + 8 &&
                  (d_last.getTime()-d_first.getTime())/1000 <= c.quantity_keepAlives_toSend * 5 + 11);
                
                //The total quantity of time messages received from the producer
                //should be c.quantity_keepAlives_toSend * 5 + 10
                //A range of 8 through 11 is made available because slow processing on my personal
                //computer made timing imprecise 
                assertTrue(
                  c.quantity_timeMessages_received >= c.quantity_keepAlives_toSend * 5 + 8 &&
                  c.quantity_timeMessages_received <= c.quantity_keepAlives_toSend * 5 + 11);
                                   
            
            } catch (Exception ex) {
                ex.printStackTrace();
            }
          }
    }
        
}
