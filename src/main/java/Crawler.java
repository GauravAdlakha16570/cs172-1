import java.io.File;

public class Crawler {

    /*********************************/
    /*      Class Variables          */
    /*********************************/

    File output;

    /*********************************/
    /*      Constructors             */
    /*********************************/

    public Crawler() {

    }

    public Crawler(String outputDir) {

    }

    /*********************************/
    /*      Life Cycle Methods       */
    /*********************************/

    // @Desc: Initializes the crawler object
    public boolean init() {


        return true; // By default, return true since nothing went wrong.
    }

    // @Desc: Temporarily stops the crawler
    public boolean pause() {

        return true; // By default, return true since nothing went wrong.
    }

    // @Desc: Crawler stars crawling
    public boolean start() {

        return true; // By default, return true since nothing went wrong.
    }

    // @Desc: Stops the crawler object. It dumps its data and leaves memory.
    public boolean stop() {

        return true; // By default, return true since nothing went wrong.
    }


    /*********************************/
    /*      Helper Methods           */
    /*********************************/
    // @Param 0: The object containing a single tweet's data
    // @Desc   : Generates a line of text containing all relevant details about a given tweet.
    private String generateLine(Object tweet) {

        return "Placeholder Text";
    }


} // End of Crawler class
