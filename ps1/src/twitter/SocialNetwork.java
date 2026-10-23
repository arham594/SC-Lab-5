package twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SocialNetwork consists of methods that operate on a social network.
 * 
 * DO NOT change the method signatures or specifications of these methods, but
 * you may implement their body as you see fit.
 */
public class SocialNetwork {

    /**
     * Guess a social network graph from a list of tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return a social network (a graph where each node is a username and edges
     *         represent follow relationships) guessed from the tweets. The graph's
     *         keys are the usernames of users who wrote tweets in the list, and
     *         the value for a key is a set of the usernames of users whom the author
     *         mentions. A user does not follow themselves.
     */
    public static Map<String, Set<String>> guessFollowsGraph(List<Tweet> tweets) {
        Map<String, Set<String>> graph = new HashMap<>();

        for (Tweet tweet : tweets) {
            // usernames are case-insensitive, so normalise to lower case
            String author = tweet.getAuthor().toLowerCase();
            Set<String> mentionedUsers = Extract.getMentionedUsers(Collections.singletonList(tweet));

            // a user does not follow themselves
            mentionedUsers.remove(author);

            // keys are only users who wrote tweets (per the spec)
            graph.computeIfAbsent(author, k -> new HashSet<>()).addAll(mentionedUsers);
        }

        return graph;
    }

    /**
     * Find the influencers in a social network.
     * 
     * @param followsGraph
     *            social network (a graph where each node is a username and edges
     *            represent follow relationships), not modified by this method.
     * @return a list of all usernames in the graph, sorted by their follower count,
     *         most followers first.
     */
    public static List<String> influencers(Map<String, Set<String>> followsGraph) {
        Map<String, Integer> followerCounts = new HashMap<>();

        // every user that appears as a key or as a followed user is a node
        for (Map.Entry<String, Set<String>> entry : followsGraph.entrySet()) {
            followerCounts.putIfAbsent(entry.getKey(), 0);
            for (String followed : entry.getValue()) {
                followerCounts.merge(followed, 1, Integer::sum);
            }
        }

        // most followers first; ties broken alphabetically so output is deterministic
        List<String> influencersList = new ArrayList<>(followerCounts.keySet());
        influencersList.sort((u1, u2) -> {
            int byCount = followerCounts.get(u2).compareTo(followerCounts.get(u1));
            return byCount != 0 ? byCount : u1.compareTo(u2);
        });

        return influencersList;
    }
}