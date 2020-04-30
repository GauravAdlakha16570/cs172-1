import twitter4j.TweetEntity;
import twitter4j.TwitterObjectFactory;

import java.util.ArrayList;

public class TweetRepository {

    public static final int NUM_TWEET_FIELDS = 5;

    ArrayList<String[]> tweets;

    /*********************************/
    /*      Life Cycle Methods       */
    /*********************************/

    // @Desc: Initializes the crawler object
    public boolean init() {

        tweets = new ArrayList<String[]>();

        return true;
    }

    /*********************************/
    /*      Access Methods           */
    /*********************************/

    public boolean insert(String[] tweetFields) {
        if (tweetFields.length != NUM_TWEET_FIELDS - 1) {    // Number of fields -1 because we won't
            System.out.println("Failed to insert!");         // have the final field, URL title, until after the URL grabber adds it
            return false;
        }



        String[] extended_tweetFields = new String[tweetFields.length + 1];
        for (int i = 0; i < tweetFields.length; i++) extended_tweetFields[i] = tweetFields[i]; // Copy the tweet fields
                                                                                               // into an array one larger so that we have an empty
                                                                                               // element for the URL grabber to add to afterwards
        tweets.add(extended_tweetFields);
        return true;
    }

    public int getSize() {
        return tweets.size();
    }

}
