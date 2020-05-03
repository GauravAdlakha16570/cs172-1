import java.net.URL;
import org.jsoup.*;
import org.jsoup.nodes.Document;


public class URLGrabber implements Runnable{

    boolean busy = false;
    final static long DELAY_FOR_WORK = 10000; // The amount of time a work-less URLGrabber will wait before checking for work again

    @Override
    public void run() {

        while(true) {
            if(!busy) {
                try {
                    Thread.sleep(DELAY_FOR_WORK);
                } catch (InterruptedException e) {
                    System.out.println("[INFO]: URLGrabber woken up externally");
                }
            }
        }

    }

    /*********************************/
    /*      Methods           */
    /*********************************/

    // @Param 0: The repository to process
    // @Desc:    For each entry in the repository, if it contains a url in its text field, add the header of that URL to its entry in the repositroy
    // @Returns: A boolean representing whether or not the processing was successful
    public boolean processRepository(final TweetRepository tweetRepository) {
        busy = true;

        System.out.println("[INFO]: URLGrabber starting work");

        for (String[] tweetFields : tweetRepository.tweets) {
            tweetFields[TweetRepository.NUM_TWEET_FIELDS - 1] = parseURL(tweetFields[TweetRepository.TWEET_TEXT_INDEX]);
        }

        System.out.println("[INFO]: URLGrabber finished work");
        busy = false;
        return true;
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
	   b = s.substring(s.indexOf("http"), s.length());
	 }
	 else if (s.contains(".com") {
		  for(int i = s.indexOf(".com"); i > 0; i-- ) {			       
			 if(s.charAt(i) == ' ') { 
		         b = s.substring(i + 1, s.indexOf(".com") + 4);
	                 break;
                   } 		
		  }
	       }
	   return b; 
	  //indexOf(String str) Returns the index within this string of the first occurrence of the specified substring
	   // try {
	//	       	URL url = new URL(b);
	//	 b = url.getHost();
       // return b;
	//	}
	//	catch (Exception e) {
	//		return "";
	//	}

      
    }


//returns the title of the document(url) passed in as a string
//paramater 0: url in string form
//return: string containing the title of the page
private String urlTitle(String url) {
	Document urltitle;
	try {	
	urltitle = Jsoup.connect(url).get();
	return urltitle.title(); // adapted from https://jsoup.org/cookbook/input/load-document-from-url
	}
	catch (Exception e) {
		return "no title exists";
	}
	
}
}
