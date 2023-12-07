package client;

public class Configuration {
    private int timeLimit;

    public Configuration() {
        this(1500);
    }

    public Configuration(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTimeLimit() {
        return timeLimit;
    }
}
