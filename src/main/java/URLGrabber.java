import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.LinkedList;
import java.net.URL;
import org.jsoup.*;
import org.jsoup.nodes.Document;

public class URLGrabber implements Runnable{

    boolean busy = false;
    final static long DELAY_FOR_WORK = 5; // The amount of time a work-less URLGrabber will wait before checking for work again

    LinkedList<TweetRepository> repositoryQueue = new LinkedList<TweetRepository>(); // Queue of repositories for the grabber to work on

    @Override
    public void run() {

        while(true) {
            if(this.repositoryQueue.size() == 0) { // If there are no repos to work on
            busy = false;

                synchronized (Manager.repositoryQueue) {
                    if (Manager.repositoryQueue.size() > 0) {
                        System.out.print("[INFO]: " + Thread.currentThread().getName() + " started processing a repository!");
                        addRepository(Manager.repositoryQueue.pop());
                        busy = true;
                    }
                }

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

                repositoryQueue.removeFirst(); // Remove the repo that we just saved from the queue
                busy = false;

                System.out.print("[INFO]: " + Thread.currentThread().getName() + " finished processing a repository!");

            }
        }
    }

    /*********************************/
    /*      Methods           */
    /*********************************/

    // @Param 0: A repo to add to the queue
    // @Desc   : Adds a repo to the queue
    public void addRepository(TweetRepository tweetRepository) {

        if (!repositoryQueue.contains(tweetRepository)) {
            repositoryQueue.add(tweetRepository);
        }
    }



    /*********************************/
    /*      Helper Methods           */
    /*********************************/

    // @Param 0: The repository to process
    // @Desc:    For each entry in the repository, if it contains a url in its text field, add the header of that URL to its entry in the repositroy
    // @Returns: A boolean representing whether or not the processing was successful
    private boolean processRepository(final TweetRepository tweetRepository) {

        for (String[] tweetFields : tweetRepository.tweets) { // For each tweet in the repo
            tweetFields[TweetRepository.NUM_TWEET_FIELDS - 1] = Crawler.sanitizeString(urlTitle(parseURL(tweetFields[TweetRepository.TWEET_TEXT_INDEX]))); // Write the parsed URL into the last spot in the tweet's array
        }

        tweetRepository.setGrabbed(true); // Mark that all tweets have been URLGrabber
        return true;  // Nothing went wrong, return true
    }

    /*********************************/
    /*      Helper Methods           */
    /*********************************/

    // @Param 0: A string from which to parse a url
    // @Desc:    Parse a URL from a string
    // @Returns: A string containing the first-located URL. Return an empty string if nothing is found.
    private String parseURL(String s) {
        String b = " ";
        if (s.contains("http")) {
            for (int i = s.indexOf("http"); i < s.length(); i++) {
                if (s.charAt(i) == ' ') {
                    b = s.substring(s.indexOf("http"), i);
                    break;
                }
		else if (i == s.length() - 1) {
			b = s.substring(s.indexOf("http"), i + 1);
			break;
		}
            }
        }
        else if (s.contains(".com")) {
            for(int i = s.indexOf(".com"); i > 0; i-- ) {
                if(s.charAt(i) == ' ') {
                    b = s.substring(i + 1, s.indexOf(".com") + 4);
                    break;
                }
		else if (i == 1) {
			b = s.substring( i - 1, s.indexOf(".com") + 4 );
			break;
		}
            }
        }
        else if (s.contains(".org")) {
            for (int i = s.indexOf(".org"); i > 0; i--) {
                if(s.charAt(i) == ' ') {
                    b = s.substring(i + 1, s.indexOf(".org") + 4);
                    break;
                }
		else if (i == 1) {
		    b = s.substring(i - 1, s.indexOf(".org") + 4);
		    break;
		}
            }
        }
        else if (s.contains(".co")) {
            for (int i = s.indexOf(".co"); i > 0; i--) {
                if(s.charAt(i) == ' ') {
                    b = s.substring(i + 1, s.indexOf(".co") + 3);
                    break;
                }
		else if (i == 1) {
		    b = s.substring(i - 1, s.indexOf(".co") + 3);
		    break;
		}
            }
        }
        else if (s.contains(".edu")) {
            for (int i = s.indexOf(".edu"); i > 0; i--) {
                if(s.charAt(i) == ' ') {
                    b = s.substring(i + 1, s.indexOf(".edu") + 4);
                    break;
                }
		else if (i == 1) {
		    b = s.substring(i - 1, s.indexOf(".edu") + 4);
		    break;
		}
            }
        }

        else if (s.contains(".gov")) {
            for (int i = s.indexOf(".gov"); i > 0; i--) {
                if(s.charAt(i) == ' ') {
                    b = s.substring(i + 1, s.indexOf(".gov") + 4);
                    break;
                }
		else if (i == 1) {
		    b = s.substring(i - 1, s.indexOf(".gov") + 4);
			break;
		}
            }
        }
        else if (s.contains(".net")) {
            for (int i = s.indexOf(".net"); i > 0; i--) {
                if (s.charAt(i) == ' ') {
                    b = s.substring(i + 1, s.indexOf(".net") + 4);
                    break;
                }
		else if (i == 1) {
		    b = s.substring(i - 1, s.indexOf(".net") + 4);
		    break;
		}
            }
        }

        return b;
    }


    //returns the title of the document(url) passed in as a string
//paramater 0: url in string form
//return: string containing the title of the page
    private String urlTitle(String url)  {
       // Document urltitle;
        try {
         Document urltitle = Jsoup.connect(url).get();
            return urltitle.title(); // adapted from https://jsoup.org/cookbook/input/load-document-from-url
        }
        catch (Exception e) {
            return "no title exists";
        }

	    /*InputStream input = null;
	    try {
		    input = new URL (url).openStream();
		    Scanner scan = new Scanner(input);
		    String body = scan.useDelimiter("\\A").next();
		    body = body.substring(body.indexOf("<title>") + 7, body.indexOf("</title>"));
		    return body;
	    }
	    catch (IOException ex) {
		    return "unable to get title";
	    }
	    finally {
		    try {
			    input.close();
		    }
		    catch (IOException ex) {
			  //  ex.printStackTree();
			  System.out.println("error");
		    }
	    }*/

		
    
    }
}
