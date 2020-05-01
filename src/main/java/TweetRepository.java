import twitter4j.TweetEntity;
import twitter4j.TwitterObjectFactory;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TweetRepository {

    public static final int NUM_TWEET_FIELDS = 5;
    //public static final int MAX_ENTRIES = (int)(500000.0 / (100.0 / 17.3)); // Total number of entries this structure can have before it stops recording data.
    public static final int MAX_ENTRIES = 100; // Debugging value

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

        // We need to stop inserting after a certain point so that we can empty the structure into a text file. The URLGrabber needs to finish with this object before it can be dumped.
        if(getSize() >= MAX_ENTRIES) {
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

    public void writeToFile(String path) {
        File file = new File(path);

        if (file.exists()) {
            System.out.println("Cannot save to disk, file " + path + " already exists!");
            return;
        }

        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file);

            for(String[] as : tweets) {
                for (String s : as) {
                    fw.write(s + "\t");
                }
                fw.write("\n");
            }

            fw.close();

        } catch (IOException e) {
            System.out.println("Cannot save to disk, file " + path + " cannot be created!");
            e.printStackTrace();
            return;
        }




    }

}
