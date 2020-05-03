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

    // Stream stuff
    final static String[] FILTER_ARGS = {"at"};

    // Input Parameter defaults
    private static final String DEFAULT_SEED_FILE_DIR = "seed.txt";
    public static final String DEFAULT_OUTPUT_DIR = "output";
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
        Crawler crawler = new Crawler(); // Create crawler
        crawler.init();                  // Initialize it

        // Set up the streaming API using the credentials created earlier
        TwitterStream twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
        twitterStream.addListener(crawler);

        // Set up stream filters
        FilterQuery query = new FilterQuery().language("en").track(FILTER_ARGS);
        twitterStream.filter(query); // Start the stream

        // Set up URLGrabber
        URLGrabber urlGrabber = new URLGrabber();

        Thread t1 = new Thread();

        // Perform repository checking until the system exits
        while(true) {
            if (tweetRepository.getSize() >= tweetRepository.MAX_ENTRIES) {

                // Stall until repository is done being URL-Grabbed
                while(!tweetRepository.getGrabbed()) {
                    if (urlGrabber.busy == false) {
                        synchronized(tweetRepository) {


                                if (!t1.isAlive()) {
                                    System.out.println("[INFO]: Spawning new thread for URLGrabber...");

                                    urlGrabber.addRepository(tweetRepository);
                                    t1 = new Thread(urlGrabber, "URLGrabber");
                                    t1.start();

                                    System.out.println("done");
                                } else {
                                    if (!urlGrabber.repositoryQueue.contains(tweetRepository)) {
                                        System.out.println("[INFO]: Adding repository to URLGrabber...");
                                        urlGrabber.addRepository(tweetRepository);
                                    }
                                }
                        }
                    }
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("[INFO]: Main thread woken up externally!");
                }
                //System.out.println("[DEBUG]: tweetRepository size: " + tweetRepository.getSize());
            }
        }
    }

    /*********************************/
    /*      Helper Methods           */
    /*********************************/

    // Adapted from: http://twitter4j.org/en/code-examples.html
    // @Param 0: The Twitter object to modify
    // @Param 1: The file that contains the required API keys
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
            twitter.setOAuthConsumer(consumerKey, consumerSecret);
        } catch (Exception e) {
            System.out.println("Something went wrong when reading " + DEFAULT_AUTH_FILE_DIR + "!");
            e.printStackTrace();
            //System.exit(-1);
        }


       
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
