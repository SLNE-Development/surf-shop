package dev.slne.base.core.command;

import java.util.ArrayList;
import java.util.List;

public class Suggestions {

    /**
     * Returns the actual list of suggestions given the current arguments
     * 
     * @param suggestions the list of suggestions
     * @param args        the current arguments
     * @return the actual list of suggestions
     */
    public static List<String> suggestions(List<String> suggestions, String[] args) {
        ArrayList<String> completerList = new ArrayList<>();

        if (args.length == 0) {
            return suggestions;
        }

        String currentArg = args[args.length - 1].toLowerCase();

        for (String suggestion : suggestions) {
            String suggestionLowercase = suggestion.toLowerCase();

            if (suggestionLowercase.startsWith(currentArg)) {
                completerList.add(suggestion);
            }
        }

        return completerList;
    }

}
