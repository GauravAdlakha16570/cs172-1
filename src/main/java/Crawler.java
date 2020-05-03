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

    final static String DEFAULT_USER_DESCRIPTION = "<no bio>";

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

        // User's Twitter Name
        fields[0] = sanitizeString(tweet.getUser().getName());

        // User's Bio
        if (tweet.getUser().getDescription() != "" && tweet.getUser().getDescription() != null) {
            fields[1] = sanitizeString(tweet.getUser().getDescription().replaceAll("\n", ""));
        } else {
            fields[1] = DEFAULT_USER_DESCRIPTION;
        }

        // User's Location
        try {
            fields[2] = sanitizeString(tweet.getGeoLocation().toString().replaceAll("\n", ""));
        } catch (NullPointerException npe) {
            fields[2] = "0,0";
        }
        // Test of User's Tweet
        fields[TweetRepository.TWEET_TEXT_INDEX] = sanitizeString(tweet.getText().replaceAll("\n", ""));

        return fields;
    }

    // A private anonymous class that does the intermediary processing for the crawler. It adds elements from the stream to the
    // tweetRepository in batches as its buffer fills up.
    private Runnable TweetAnalyzerClient(final TweetRepository tweetRepository) {
        return new Runnable() {

            @Override
            public void run() {
                int id = abs((int) (System.nanoTime()%100000000)); // ID for housekeeping purposes
                System.out.println("[INFO]: Analyzer " + id + " has started running");

                List<Status> tempTweets = new ArrayList<Status>(); // process buffer

                while (true) {
                    if (tweets.size() > 0) {        // Dump the current batch of tweets into the processing buffer
                        tempTweets.clear();         // Clear the buffer
                        tweets.drainTo(tempTweets); // Dump

                        System.out.print("[DEBUG]: Inserting " + tempTweets.size() + " tweets...");

                        // Use a thread-safe approach to insert the tweets in the buffer into the tweetRepository
                        for (Status s : tempTweets) {
                            synchronized (tweetRepository) {
                                tweetRepository.insert(getTweetFields(s));
                            }
                        }
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

    public static String sanitizeString(final String s) {
        return s.replaceAll("\n", " ").replaceAll("\t", " ").replaceAll("," , ".").replaceAll("\"", "").replaceAll("\\P{Print}", "");
    }

    /*********************************/
    /*      StatusListener Methods   */
    /*********************************/

    @Override
    public void onStatus(Status status) {
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
