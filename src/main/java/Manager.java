import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.*;
import java.util.Scanner;

public class Manager {

    /*********************************/
    /*      Constants                */
    /*********************************/

    private static final String DEFAULT_AUTH_FILE_DIR = "auth.txt";
    private static final String DEFAULT_TOKEN_DIR     = ".token.txt";

    private static final String DEFAULT_SEED_FILE_DIR = "seed.txt";
    private static final String DEFAULT_OUTPUT_DIR = "output";
    private static final int DEFAULT_NUM_PAGES = 10000;
    private static final int DEFAULT_NUM_HOPS = 6;



    /*********************************/
    /*      Main method              */
    /*********************************/

    public static void main(String args[]) {

        String seedDir = DEFAULT_SEED_FILE_DIR;
        String outputDir = DEFAULT_OUTPUT_DIR;
        int numPages = DEFAULT_NUM_PAGES;
        int numHops = DEFAULT_NUM_HOPS;

        // Get the input parameters. There's no parsing so parameter location is fixed.
        switch (args.length) {
            case 4:
                outputDir = (args[3]);
            case 3:
                numHops = Integer.parseInt(args[2]);
            case 2:
                numPages = Integer.parseInt(args[1]);
            case 1:
                seedDir = args[0];
            case 0:
            default:
                System.out.println("Please enter between 0 and 4 input parameters.");
        }

        // TODO: Remove debugging
        System.out.println("PARAM0: " + seedDir);
        System.out.println("PARAM1: " + numPages);
        System.out.println("PARAM2: " + numHops);
        System.out.println("PARAM3: " + outputDir);

        // Authorize the user to gain access to the API
        try {
            Authorize(new File(DEFAULT_AUTH_FILE_DIR));
        } catch (Exception e) {
            System.out.println("Something went wrong while authorizing!");
            e.printStackTrace();
            System.exit(-1);
        }

        // Initiailize seed file
        File seedFile = new File(seedDir);
    }

    // Adapted from: http://twitter4j.org/en/code-examples.html
    // @Param 0: The file that contains the required API keys
    // @Desc   : A function that takes care of authorizing this application to run on a twitter account
    private static void Authorize(File authFile) throws TwitterException, IOException {

        String consumerKey = "";
        String consumerSecret = "";

        // Get the consumerKey and Secret
        try {
            Scanner scan = new Scanner(authFile);

            consumerKey = scan.nextLine();
            consumerSecret = scan.nextLine();
        } catch (Exception e) {
            System.out.println("Something went wrong when reading " + DEFAULT_AUTH_FILE_DIR + "!");
            e.printStackTrace();
            System.exit(-1);
        }

        // The factory instance is re-useable and thread safe.
        Twitter twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (null == accessToken) {
            System.out.println("Open the following URL and grant access to your account:");
            System.out.println(requestToken.getAuthorizationURL());
            System.out.print("Enter the PIN(if available) or just hit enter.[PIN]:");
            String pin = br.readLine();
            try {
                if (pin.length() > 0) {
                    accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                } else {
                    accessToken = twitter.getOAuthAccessToken();
                }
            } catch (TwitterException te) {
                if (401 == te.getStatusCode()) {
                    System.out.println("Unable to get the access token.");
                } else {
                    te.printStackTrace();
                }
            }
        }

        System.out.println("It worked! You're now authorized!");
        //persist to the accessToken for future reference.
        storeAccessToken(twitter.verifyCredentials().getId(), accessToken);

    }

    private static void storeAccessToken(long useId, AccessToken accessToken) {
        //store accessToken.getToken()
        //store accessToken.getTokenSecret()
    }
}
