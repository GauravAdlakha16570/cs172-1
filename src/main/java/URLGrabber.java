
import java.io.*;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
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
	       for (int i = s.indexOf("http"); i < s.length(); i++) {
			if (s.charAt(i) == ' ') {
	   b = s.substring(s.indexOf("http"), i - 1);
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
		  }
	       }
	       else if (s.contains(".org")) {
		       for (int i = s.indexOf(".org"); i > 0; i--) {
			       if(s.charAt(i) == ' ') {
				       b = s.substring(i + 1, s.indexOf(".org") + 4);
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
		       }
	       }
	        else if (s.contains(".edu")) {  
			for (int i = s.indexOf(".edu"); i > 0; i--) {
			       if(s.charAt(i) == ' ') {
				       b = s.substring(i + 1, s.indexOf(".edu") + 4);
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
		       }
	       }
		else if (s.contains(".net")) {
	 		for (int i = s.indexOf(".net"); i > 0; i--) {
				if (s.charAt(i) == ' ') {
					b = s.substring(i + 1, s.indexOf(".net") + 4);
					break;
				}
			}
		}

	   return b; 
	 
      
    }


//returns the title of the document(url) passed in as a string
//paramater 0: url in string form
//return: string containing the title of the page
private String urlTitle(String url) {
	
	try {	
	Document urltitle = Jsoup.connect(url).get();
	return urltitle.title(); // adapted from https://jsoup.org/cookbook/input/load-document-from-url
	}
	catch (Exception e) {
		return "no title exists";
	}

/*	InputStream input = null;                Second attempt

	try {
		input = new URL(url).openStream();
		Scanner scan = new Scanner(input);
		String inputBody = scan.useDelimiter("\\A").next();
		String title = inputBody.substring(inputBody.indexOf("<title>") + 7, inputBody.indexOf("</title>"));//adapted from https://stackoverflow.com/questions/40099397/how-can-i-get-the-page-title-information-from-a-url-in-java
		return title;

	}
	catch (IOExcpetion ex) {
		ex.printStackTrace();
		return "error";
	
	}
	finally {
		try {
			input.close();
			
		}
		catch (IOExcpetion ex) {
			ex.printStackTrace();
		}
	}*/
}
}
