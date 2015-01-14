# ConsumerProducer
(A multi-threaded application that uses concurrent queues)

<b>Initial attempt: JMS<br></b>
I initially wanted to use JMS (Java Messaging Service: a middleware that allows different clients to send messages via shared queues). However, I ran into a problem having to do with the fact that in the requirements, the number of consumers is variable (determined by an argument to the application).
In the JMS design of this system, each consumer would be receiving time messages from a different queue. However, JMS doesn't allow for the spontaneous creation of queues. Queues need to be created in advance, on the Glassfish server domain console. Thus, I wouldn't be able to make a program that could create any given number of consumers and corresponding queues. 
There is a way to change this functionality, but it is difficult (it has to do with installing and making changes to the Glassfish server). 

<b>Ended up: Creating application from scratch<br></b>
I wrote my own multi-threaded application with shared (concurrent) queues. 

Each producer and consumer object has access to 2 queues that are shared across the application:
- timeQueue: holds time messages (written by the producer to consumers)
- consumerStatusQueue: holds register or keepAlive messages (written by the consumers to the producer)

To enable simultaneous processes, I used threads. Each producer and consumer object has 2 threads running: 
- a time_thread (that writes messages to, or reads messages from, the timeQueue)
- a consumerStatus_thread (that writes messages to, or reads messages from, the consumerStatusQueue)

2 threads became necessary because time and consumerStatus message reading and writing need to occur simultaneously (for example: a consumer simultaneously checks for time messages and sends keepAlive messages)

<b>Room for improvement<br></b>
There are a few things that could be improved. Currently:

- The producer creates a different time message for each eligible consumer. I could alter that to allow just one message to be created per given time. 
Instead of including a destination consumer_key with each message, I would include a hashset with all destination consumer_keys. Every consumer who is peeking at the queue's messages would be able to, in O(1) complexity, understand whether it's eligible to receive the message. Meanwhile, the producer would be able to send out just 1 message per second.

- When the producer needs to send out its by-the-second time message, it has to loop through all the consumers it has stored in a hashmap. I think this solution isn't scalable - if there are a lot of consumers, some would receive the message with a delay. 

- All of the consumers continue to run and peek at time messages -- even if they had sent their last keepAlive message a while ago. This is unnecessary memory use, but I didn't want to take the liberty to shut those threads down.  

- In general, I have not optimized some of the threads. For example, some threads run while(true) and are constantly trying to access the next message off of a given queue. Rather, threads work more optimally and take up less memory if they are given the chance to wait(). Using wait() and notify() (to wake up the thread) might be more optimal. 
