package twitter;

import java.util.ArrayList;
import java.util.Arrays;
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
            String author = tweet.getAuthor().toLowerCase();
            Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));
            
            // Remove self-mentions if any
            mentionedUsers.remove(author);
            
            // Add to graph
            if (!graph.containsKey(author)) {
                graph.put(author, new HashSet<>());
            }
            graph.get(author).addAll(mentionedUsers);
            
            // Ensure mentioned users also appear as keys in the graph even if they didn't tweet
            for (String user : mentionedUsers) {
                graph.putIfAbsent(user, new HashSet<>());
            }
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
        
        // Initialize all users with 0 followers
        for (String user : followsGraph.keySet()) {
            followerCounts.putIfAbsent(user, 0);
            for (String followed : followsGraph.get(user)) {
                followerCounts.putIfAbsent(followed, 0);
                followerCounts.put(followed, followerCounts.get(followed) + 1);
            }
        }
        
        // Collect all users and sort by follower count descending
        List<String> influencersList = new ArrayList<>(followerCounts.keySet());
        influencersList.sort((u1, u2) -> followerCounts.get(u2).compareTo(followerCounts.get(u1)));
        
        return influencersList;
    }
}