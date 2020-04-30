import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.*;

import static java.lang.Math.abs;

public class Crawler implements StatusListener {

    /*********************************/
    /*      Class Variables          */
    /*********************************/

    final int NUM_THREADS = 2;
    final long THREADS_DELAY = 1;
    final long TWEETS_SAVING_TIME = 1000;

    private LinkedBlockingQueue<Status> tweets;

    /*********************************/
    /*      Constructors             */
    /*********************************/

    public Crawler() {
        System.out.println("[INFO]: Crawler created.");
    }


    /*********************************/
    /*      Life Cycle Methods       */
    /*********************************/

    // @Desc: Initializes the crawler object
    public boolean init() {
        System.out.println("[INFO]: Crawler initialized.");
        tweets = new LinkedBlockingQueue<Status>();

        final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        Runnable tweetAnalyzer = TweetAnalyzerClient(Manager.tweetRepository);
        for (int i = 0; i < NUM_THREADS; i++) {
            System.out.println("[INFO]: Analyzer created.");
            executor.execute(tweetAnalyzer);
            try {
                Thread.sleep(THREADS_DELAY);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true; // By default, return true since nothing went wrong.
    }

    // @Desc: Temporarily stops the crawler
    public boolean pause() {

        return true; // By default, return true since nothing went wrong.
    }

    // @Desc: Crawler stars crawling
    public boolean start() {

        return true; // By default, return true since nothing went wrong.
    }

    // @Desc: Stops the crawler object. It dumps its data and leaves memory.
    public boolean stop() {

        return true; // By default, return true since nothing went wrong.
    }


    /*********************************/
    /*      Helper Methods           */
    /*********************************/
    // @Param 0: The object containing a single tweet's data
    // @Desc   : Generates a line of text containing all relevant details about a given tweet.
    private String[] getTweetFields(Status tweet) {
        String[] fields = new String[TweetRepository.NUM_TWEET_FIELDS - 1];

        fields[0] = tweet.getUser().getName();
        fields[1] = tweet.getUser().getEmail();
        try {
            fields[2] = tweet.getGeoLocation().toString();
        } catch (NullPointerException npe) {
            fields[2] = "0";
        }
        fields[3] = tweet.getText();

        return fields;
    }

    private Runnable TweetAnalyzerClient(final TweetRepository tweetRepository) {
        return new Runnable() {

            @Override
            public void run() {
                int id = abs((int) (System.nanoTime()%100000000));
                System.out.println("[INFO]: Analyzer " + id + " has started running");

                List<Status> tempTweets = new ArrayList<Status>();

                while (true) {
                    if (tweets.size() > 0) {
                        tempTweets.clear();
                        tweets.drainTo(tempTweets);

                        System.out.println("Inserting " + tempTweets.size() + " tweets...");
                        for (Status s : tempTweets) tweetRepository.insert(getTweetFields(s));
                        System.out.println(" done.");
                    }

                    try {
                        //System.out.println("[INFO]: Analyzer " + id + " has started sleeping...");
                        Thread.sleep(TWEETS_SAVING_TIME);
                        //System.out.println("[INFO]: Analyzer " + id + " has woken up");
                    }
                    catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        };
    }

    /*********************************/
    /*      StatusListener Methods   */
    /*********************************/

    @Override
    public void onStatus(Status status) {
        System.out.println("Adding status to tweets...");
        tweets.add(status);
    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    public void onTrackLimitationNotice(int i) {

    }

    public void onScrubGeo(long l, long l1) {

    }

    public void onStallWarning(StallWarning stallWarning) {

    }

    public void onException(Exception e) {

    }
} // End of Crawler class
