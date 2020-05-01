public class URLGrabber implements Runnable{
    @Override
    public void run() {



    }

    /*********************************/
    /*      Methods           */
    /*********************************/

    // @Param 0: The repository to process
    // @Desc:    For each entry in the repository, if it contains a url in its text field, add the header of that URL to its entry in the repositroy
    // @Returns: A boolean representing whether or not the processing was successful
    public boolean processRepository(TweetRepository tweetRepository) {

        for (String[] tweetFields : tweetRepository.tweets) {
            tweetFields[TweetRepository.NUM_TWEET_FIELDS - 1] = parseURL(tweetFields[TweetRepository.TWEET_TEXT_INDEX]);
        }

        return true;
    }

    /*********************************/
    /*      Helper Methods           */
    /*********************************/

    // @Param 0: A string from which to parse a url
    // @Desc:    Parse a URL from a string
    // @Returns: A string containing the first-located URL. Return an empty string if nothing is found.
    private String parseURL(String s) {

        return s;
    }
}
