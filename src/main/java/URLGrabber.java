import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.net.URL;
import org.jsoup.*;
import org.jsoup.nodes.Document;

public class URLGrabber implements Runnable{

    boolean busy = false;
    final static long DELAY_FOR_WORK = 100; // The amount of time a work-less URLGrabber will wait before checking for work again

    LinkedList<TweetRepository> repositoryQueue = new LinkedList<TweetRepository>(); // Queue of repositories for the grabber to work on

    @Override
    public void run() {

        while(true) {
            if(repositoryQueue.size() == 0) { // If there are no repos to work on
                try {
                    Thread.sleep(DELAY_FOR_WORK); // Sleep the thread to save power
                } catch (InterruptedException e) {
                    System.out.println("[INFO]: URLGrabber woken up externally");
                }
            } else { // There's at least one repo to work on
                processRepository(repositoryQueue.peek()); // Check the top repo
                System.out.println("[INFO]: Writing tweets to disk...");

                File f = new File(Manager.DEFAULT_OUTPUT_DIR); // Create the folder to save in
                f.mkdir();

                synchronized (repositoryQueue.peek()) { // Ensure the access to the top element is synchronized
                    repositoryQueue.peek()
                                   .writeToFile(Manager.DEFAULT_OUTPUT_DIR + "/" +
                                                      Long.toString(System.currentTimeMillis() % 1000) +
                                                      "_tweets.tsv"); // Write the repo to disk
                }

                //TODO: Remove debug code
                System.out.print("[DEBUG]: Exiting system...");
                System.exit(0);

                repositoryQueue.removeFirst(); // Remove the repo that we just saved from the queue
            }
        }

    }

    /*********************************/
    /*      Methods           */
    /*********************************/

    // @Param 0: A repo to add to the queue
    // @Desc   : Adds a repo to the queue
    public void addRepository(final TweetRepository tweetRepository) {
        repositoryQueue.add(tweetRepository);
    }



    /*********************************/
    /*      Helper Methods           */
    /*********************************/

    // @Param 0: The repository to process
    // @Desc:    For each entry in the repository, if it contains a url in its text field, add the header of that URL to its entry in the repositroy
    // @Returns: A boolean representing whether or not the processing was successful
    private boolean processRepository(final TweetRepository tweetRepository) {
        busy = true; // The URLGrabber is now doing work

        System.out.println("[INFO]: URLGrabber starting work");

        for (String[] tweetFields : tweetRepository.tweets) { // For each tweet in the repo
            tweetFields[TweetRepository.NUM_TWEET_FIELDS - 1] = Crawler.sanitizeString(urlTitle(parseURL(tweetFields[TweetRepository.TWEET_TEXT_INDEX]))); // Write the parsed URL into the last spot in the tweet's array
        }

        tweetRepository.setGrabbed(true); // Mark that all tweets have been URLGrabbed

        System.out.println("[INFO]: URLGrabber finished work");
        busy = false; // The URLGrabber is done doing work

        return true;  // Nothing went wrong, return true
    }

    //TODO: Fix
    // @Param 0: A string from which to parse a url
    // @Desc:    Parse a URL from a string
    // @Returns: A string containing the first-located URL. Return an empty string if nothing is found.
    private String parseURL(String s) {
        try {
            URL url = new URL(s);
            s = url.getHost(); //adapted from https://docs.oracle.com/javase/tutorial/networking/urls/urlInfo.html
            return s;
        }
        catch (Exception e) {
            return "could not find URL";
        }
    }

    //TODO: Fix
    //returns the title of the document(url) passed in as a string
    //paramater 0: url in string form
    //return: string containing the title of the page
    private String urlTitle(String url) {
        Document urltitle;
        try {
            urltitle = Jsoup.connect(url).get();
            return urltitle.title(); // adapted from https://jsoup.org/cookbook/input/load-document-from-url
        }
        catch (IOException e) {
            return "null";
        } catch (IllegalArgumentException e) {
            return "<Malformed URL: " + url + ">";
        }

    }
}
