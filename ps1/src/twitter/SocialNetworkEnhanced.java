package twitter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SocialNetworkEnhanced extends social network analysis by incorporating
 * additional evidence for follow relationships, such as shared hashtags.
 */
public class SocialNetworkEnhanced {

    /**
     * Guess an enhanced social network graph from a list of tweets,
     * combining @-mentions with shared hashtag usage as evidence of following.
     * 
     * @param tweets list of tweets with distinct ids, not modified by this method.
     * @return an enhanced social network graph.
     */
    public static Map<String, Set<String>> guessEnhancedFollowsGraph(List<Tweet> tweets) {
        // Start with the baseline graph based on @-mentions
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);

        // Map from hashtag (lowercase) to set of authors who used it
        Map<String, Set<String>> hashtagUsers = new HashMap<>();
        Pattern hashtagPattern = Pattern.compile("#([a-zA-Z0-9_]+)");

        for (Tweet tweet : tweets) {
            String author = tweet.getAuthor().toLowerCase();
            Matcher matcher = hashtagPattern.matcher(tweet.getText());
            while (matcher.find()) {
                String hashtag = matcher.group(1).toLowerCase();
                hashtagUsers.putIfAbsent(hashtag, new HashSet<>());
                hashtagUsers.get(hashtag).add(author);
            }
        }

        // Add follow edges between users who share hashtags
        for (Map.Entry<String, Set<String>> entry : hashtagUsers.entrySet()) {
            Set<String> authors = entry.getValue();
            if (authors.size() > 1) {
                for (String author1 : authors) {
                    graph.putIfAbsent(author1, new HashSet<>());
                    for (String author2 : authors) {
                        if (!author1.equals(author2)) {
                            graph.get(author1).add(author2);
                        }
                    }
                }
            }
        }

        return graph;
    }
}