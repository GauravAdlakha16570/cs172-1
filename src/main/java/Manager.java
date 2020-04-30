import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.Scanner;

public class Manager {

    /*********************************/
    /*      Constants                */
    /*********************************/

    // Authentication stuff
    private static final String DEFAULT_AUTH_FILE_DIR = "auth.txt";
    private static final String DEFAULT_TOKEN_DIR     = ".token.txt";

    // Input Parameter defaults
    private static final String DEFAULT_SEED_FILE_DIR = "seed.txt";
    private static final String DEFAULT_OUTPUT_DIR = "output";
    private static final int DEFAULT_NUM_PAGES = 10000;
    private static final int DEFAULT_NUM_HOPS = 6;

    /*********************************/
    /*      Class Variables          */
    /*********************************/

    public static TweetRepository tweetRepository = new TweetRepository();

    /*********************************/
    /*      Main method              */
    /*********************************/

    public static void main(String[] args) {

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
        // The factory instance is re-useable and thread safe.
        Twitter twitter = TwitterFactory.getSingleton();
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        try {
            configurationBuilder = Authorize(twitter, new File(DEFAULT_AUTH_FILE_DIR));
        } catch (Exception e) {
            System.out.println("Something went wrong while authorizing!");
            e.printStackTrace();
            System.exit(-1);
        }





        /**** Start the Crawling ****/
        tweetRepository.init(); // Initialize the tweet repository
        Crawler crawler = new Crawler();
        crawler.init();

        TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        twitterStream.addListener(crawler);
        twitterStream.sample();
        //while(true) {System.out.println(tweetRepository.getSize());}
    }



    /*********************************/
    /*      Helper Methods           */
    /*********************************/

    // Adapted from: http://twitter4j.org/en/code-examples.html
    // @Param 0: The file that contains the required API keys
    // @Desc   : A method that takes care of authorizing this application to run on a twitter account
    private static ConfigurationBuilder Authorize(Twitter twitter, File authFile) throws TwitterException, IOException {

        // Vars to store the API keys
        String consumerKey = "";
        String consumerSecret = "";
        long userID = -1L;

        // Get the consumerKey and Secret
        try {
            Scanner scan = new Scanner(authFile);
            consumerKey = scan.nextLine(); // Try to read the keys from the keyfile. This assumes formatting is correct.
            consumerSecret = scan.nextLine();
        } catch (Exception e) {
            System.out.println("Something went wrong when reading " + DEFAULT_AUTH_FILE_DIR + "!");
            e.printStackTrace();
            System.exit(-1);
        }


        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // If an authentication token has been obtained on this machine before, just use that instead of re-authenticating
        if (new File(DEFAULT_TOKEN_DIR).exists()) {
            Scanner scan = new Scanner(new File(DEFAULT_TOKEN_DIR));
            accessToken = new AccessToken(scan.nextLine(), scan.nextLine(), scan.nextLong()); // This assumes that the .token.txt file is not corrupted.
            twitter.setOAuthAccessToken(accessToken);
        }

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
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken.getToken())
                .setOAuthAccessTokenSecret(accessToken.getTokenSecret());

        if (twitter.getAuthorization().isEnabled()) System.out.println("It worked! You're now authorized!");
        //persist to the accessToken for future reference.
        storeAccessToken(twitter.verifyCredentials().getId(), accessToken);

        return configurationBuilder;
    }

    // @Param 0: Twitter User ID
    // @Param 1: Twitter access Token
    // @Desc   : A function that saves local file containing the access token
    private static void storeAccessToken(long useId, AccessToken accessToken) {
        File tokenFile = new File(DEFAULT_TOKEN_DIR);
        try {
            if (tokenFile.createNewFile()) {
                FileWriter fw = new FileWriter(tokenFile);
                fw.write(accessToken.getToken() + "\n");
                fw.write(accessToken.getTokenSecret() + "\n");
                fw.write(accessToken.getUserId() + "\n");
                fw.close();
            }

        } catch (Exception e) {
            System.out.println("Something went wrong while saving the AccessToken!");
            e.printStackTrace();
        }

    }
}
