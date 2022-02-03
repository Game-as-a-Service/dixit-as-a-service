package tw.wally.dixit;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author - wally55077@gmail.com
 */
@AllArgsConstructor
public class Registration {
    private final int minPlayers;
    private final int maxPlayers;
    private final Collection<Option> options;

    public Registration() {
        this(4, 6);
    }

    public Registration(int minPlayers, int maxPlayers) {
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
