import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.Scanner;
import java.util.*;

public class Manager {

    /*********************************/
    /*      Constants                */
    /*********************************/

    // Authentication stuff
    private static final String DEFAULT_AUTH_FILE_DIR = "auth.txt";
    private static final String DEFAULT_TOKEN_DIR     = ".token.txt";



    // Input Parameter defaults
    private static final String DEFAULT_SEED_FILE_DIR = "seed.txt";
    public static final String DEFAULT_OUTPUT_DIR = "output";
    private static final int DEFAULT_NUM_OUTPUT_FILES = 200;
    private static final String DEFAULT_KEYWORD_FILTER = "at";
    private static final int DEFAULT_NUM_GRABBERS = 5;


    /*********************************/
    /*      Class Variables          */
    /*********************************/

    public static TweetRepository tweetRepository = new TweetRepository();
    public static LinkedList<TweetRepository> repositoryQueue = new LinkedList<TweetRepository>();

    /*********************************/
    /*      Main method              */
    /*********************************/

    public static void main(String[] args) {

        String seedDir = DEFAULT_SEED_FILE_DIR;
        String outputDir = DEFAULT_OUTPUT_DIR;
        int numOutputFiles = DEFAULT_NUM_OUTPUT_FILES;
        String keywordFilter = DEFAULT_KEYWORD_FILTER;

        // Get the input parameters. There's no parsing so parameter location is fixed.
        switch (args.length) {
            case 4:
                outputDir = (args[3]);
            case 3:
                keywordFilter = (args[2]);
            case 2:
                numOutputFiles = Integer.parseInt(args[1]);
            case 1:
                seedDir = args[0];
            case 0:
            default:
                System.out.println("Please enter between 0 and 4 input parameters.");
        }

        // TODO: Remove debugging
        System.out.println("PARAM0: " + seedDir);
        System.out.println("PARAM1: " + numOutputFiles);
        System.out.println("PARAM2: " + keywordFilter);
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
        FilterQuery query = new FilterQuery().language("en").track(keywordFilter);
        twitterStream.filter(query); // Start the stream

        // Set up URLGrabbers
        Thread[] urlGrabberThreads = new Thread[DEFAULT_NUM_GRABBERS];
        URLGrabber[] urlGrabbers = new URLGrabber[DEFAULT_NUM_GRABBERS];
        for (int i = 0; i < DEFAULT_NUM_GRABBERS; i++) {
            urlGrabbers[i] = new URLGrabber();
            urlGrabberThreads[i] = new Thread(urlGrabbers[i], ("URLGrabber " + Integer.toString(i) ) );
            urlGrabberThreads[i].start();
        }

        int numRepositoriesProduced = 1;

        // Perform repository checking until the system exits
        while(true) {
            if (Manager.tweetRepository.getSize() >= tweetRepository.MAX_ENTRIES && numRepositoriesProduced <= DEFAULT_NUM_OUTPUT_FILES) {
                synchronized (Manager.repositoryQueue) {
                    System.out.println("[INFO]: Adding Repo to queue...");
                    repositoryQueue.add(tweetRepository);
                    numRepositoriesProduced++;

                }

                // Stall until repository is done being URL-Grabbed
                /*while(!tweetRepository.getGrabbed()) {
                    if (urlGrabber1.busy == false) { // If the URLGrabber is ready to accept new work
                        synchronized(tweetRepository) { // Get synchronous access to the repo we want to process


                                if (!t1.isAlive()) { // Check if the URLGrabber thread is alive. If it isn't:
                                    System.out.println("[INFO]: Spawning new thread for URLGrabber...");

                                    urlGrabber1.addRepository(tweetRepository);    // Add the repository to the URLGrabber
                                    t1 = new Thread(urlGrabber1, "URLGrabber"); // Create a new thread for the URLGrabber
                                    t1.start();                                   // Start the grabber's thread

                                    System.out.println("done");
                                } else { // If the thread already exists
                                    if (!urlGrabber1.repositoryQueue.contains(tweetRepository)) { // If didn't we already added the repo to the URLGrabber's queue
                                        System.out.println("[INFO]: Adding repository to URLGrabber...");
                                        urlGrabber1.addRepository(tweetRepository); // Add to URLGrabber's queue
                                    }
                                }
                        }
                    }
                }*/

                System.out.println("[DEBUG]: Checking for repo-regeneration..."); //TODO: REmove
                if (numRepositoriesProduced < numOutputFiles ) {
                    Manager.tweetRepository = new TweetRepository();
                    tweetRepository.init();

                    twitterStream.removeListener(crawler);
                    twitterStream.shutdown();

                    crawler = new Crawler();
                    crawler.init();
                    // Set up the streaming API using the credentials created earlier
                    //twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
                    twitterStream.addListener(crawler);

                    // Set up stream filters
                    twitterStream.filter(query); // Start the stream

                    System.out.println("[Debug]: Generating a new repository..."); //TODO: Remove
                }


            } else {

                boolean grabbersBusy = false;
                for (URLGrabber ug: urlGrabbers) {
                    synchronized (ug) {
                        grabbersBusy = grabbersBusy || ug.busy;
                    }
                }

                // If it looks like nothing is busy
                if (!grabbersBusy && numRepositoriesProduced >= numOutputFiles + 1) {

                    try {
                        Thread.sleep(5500);
                    } catch (Exception e) {}

                    //Check again and see if anything is busy
                    for (URLGrabber ug: urlGrabbers) {
                        synchronized (ug) {
                            grabbersBusy = grabbersBusy || ug.busy;
                        }
                    }

                    // If they still aren't busy, exit
                    if (!grabbersBusy) {
                        System.out.println("[INFO]: Crawling complete!");
                        System.exit(0);
                    }

                }


                try {
                    Thread.sleep(1000); // Wait
                } catch (InterruptedException e) {
                    System.out.println("[INFO]: Main thread woken up externally!");
                }
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
