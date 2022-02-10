package tw.wally.dixit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author - wally55077@gmail.com
 */
@Data
public class Registration {
    private String name;
    private String serviceHost;
    private int minPlayers;
    private int maxPlayers;
    private Collection<Option> options;

    public Registration(String name, String serviceHost, int minPlayers, int maxPlayers) {
        this.name = name;
        this.serviceHost = serviceHost;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.options = new LinkedList<>();
    }

    public void addOption(Option option) {
        options.add(option);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        public String name;
        public String type;
        public int min;
        public int max;
        public int step;
    }
}
