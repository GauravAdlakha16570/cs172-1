public class URLGrabber implements Runnable{

    boolean busy = false;
    final static long DELAY_FOR_WORK = 100; // The amount of time a work-less URLGrabber will wait before checking for work again

    Queue<TweetRepository> repositoryQueue = new Queue<TweetRepository>();

    @Override
    public void run() {

        while(true) {
            if(repositoryQueue.empty()) {
                try {
                    Thread.sleep(DELAY_FOR_WORK);
                } catch (InterruptedException e) {
                    System.out.println("[INFO]: URLGrabber woken up externally");
                }
            } else {
                processRepository(repositoryQueue.peek());
            }
        }

    }

    /*********************************/
    /*      Methods           */
    /*********************************/
 
    public void addRepository(final TweetRepository tweetRepository) {
        repositoryQueue,add(tweetRepository);
    }



    /*********************************/
    /*      Helper Methods           */
    /*********************************/

    // @Param 0: The repository to process
    // @Desc:    For each entry in the repository, if it contains a url in its text field, add the header of that URL to its entry in the repositroy
    // @Returns: A boolean representing whether or not the processing was successful
    private boolean processRepository(final TweetRepository tweetRepository) {
        busy = true;

        System.out.println("[INFO]: URLGrabber starting work");

        for (String[] tweetFields : tweetRepository.tweets) {
            tweetFields[TweetRepository.NUM_TWEET_FIELDS - 1] = urlTitle(parseURL(tweetFields[TweetRepository.TWEET_TEXT_INDEX]));
        }

        tweetRepository.setGrabbed(true);

        System.out.println("[INFO]: URLGrabber finished work");
        busy = false;

        return true;
    }

    // @Param 0: A string from which to parse a url
    // @Desc:    Parse a URL from a string
    // @Returns: A string containing the first-located URL. Return an empty string if nothing is found.
    private String parseURL(String s) {

        return s;
    }
}
