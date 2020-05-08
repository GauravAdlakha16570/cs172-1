### CS172 Project, Spring 2020

Contributors: Aaron Sigal, Surya Kumaraguru, Gaurav Adlakha, Anthony Yip

##

### Setup

Simply clone the repository and open it up with a Maven-compatible IDE. IntelliJ IDEA is recommended. It is recommended 
that JDK 1.8 is used and compiled at language level 1.5.

##

###Running The Project

Install Maven, then run:

mvn package
mvn exec:java

##

###USAGE

You will need a file containing a Twitter API key and a Twitter secret API key seperated by a newline character. This 
file should be named `auth.txt`. After successfully authenticating the application, a file named `.token.txt` will
be generated containing your final authentication tokens. Do not share the contents of this file with anyone. If this file
is deleted, you will simply need to re-authenticate.

###Contributing

Please only work in your own branches. Branches should follow the naming convention `dev_<your_first_name>_<feature_name>`.

Merging with the master branch should be done only by the project leader.
