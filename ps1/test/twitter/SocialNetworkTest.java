package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Testing strategy:
     *
     * guessFollowsGraph(tweets):
     *   Partition on number of tweets:      0, 1, >1
     *   Partition on mentions per tweet:    0, 1, >1
     *   Partition on tweets per author:     1, >1 (same author tweets repeatedly)
     *   Partition on authors:               1, >1
     *   Partition on special cases:         duplicate mention, self-mention,
     *                                       mixed-case usernames, mention-only user
     *   NOTE: the postcondition is underdetermined (an implementation may infer
     *   extra follows, and may or may not list a mention-free author as a key),
     *   so the tests only assert what MUST be true: that the evidence-based
     *   follows are present, that nobody follows themselves, and that no
     *   follows appear without any evidence. They never compare the whole graph
     *   with assertEquals except in the empty-input case.
     *
     * influencers(followsGraph):
     *   Partition on graph size:            empty, 1 user, >1 users
     *   Partition on follower counts:       all zero, one influencer,
     *                                       several distinct counts, ties
     *   Partition on user position:         user appears only as a key, only as a
     *                                       followed value, or both
     *   Ties are only checked as "the tied users are adjacent and both present",
     *   since the spec does not define tie order.
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "hello @bbitdiddle and @rivest", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "hello @alyssa", d2);

    /** @return the set of users that 'user' follows, or an empty set if user is not a key. */
    private static Set<String> followsOf(Map<String, Set<String>> graph, String user) {
        Set<String> s = graph.get(user);
        return s == null ? new HashSet<>() : s;
    }

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // ---------------- guessFollowsGraph() ----------------

    // 1. Empty list of tweets
    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());
        assertTrue("expected empty graph", followsGraph.isEmpty());
    }

    // 2. Tweets without mentions
    @Test
    public void testGuessFollowsGraphNoMentions() {
        List<Tweet> tweets = Arrays.asList(
                new Tweet(1, "alyssa", "just thinking out loud", d1),
                new Tweet(2, "bbitdiddle", "no one to mention here, email me at a@b.com", d2));
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);

        // an author with no mentions may or may not be a key, but must follow nobody
        for (Map.Entry<String, Set<String>> e : followsGraph.entrySet()) {
            assertTrue(e.getKey() + " should follow nobody", e.getValue().isEmpty());
        }
    }

    // 3. Single mention
    @Test
    public void testGuessFollowsGraphSingleMention() {
        Tweet t = new Tweet(1, "ernie", "hi @bert", d1);
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        assertTrue("expected ernie to be in the graph", followsGraph.containsKey("ernie"));
        assertTrue("expected ernie to follow bert", followsGraph.get("ernie").contains("bert"));
    }

    // 4. Multiple mentions in one tweet
    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1));

        assertTrue("expected alyssa to follow bbitdiddle", followsGraph.get("alyssa").contains("bbitdiddle"));
        assertTrue("expected alyssa to follow rivest", followsGraph.get("alyssa").contains("rivest"));
    }

    // multiple authors (also the original sample test)
    @Test
    public void testGuessFollowsGraphMultipleAuthors() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        assertFalse("expected non-empty graph", followsGraph.isEmpty());
        assertTrue("expected alyssa to follow bbitdiddle", followsGraph.get("alyssa").contains("bbitdiddle"));
        assertTrue("expected alyssa to follow rivest", followsGraph.get("alyssa").contains("rivest"));
        assertTrue("expected bbitdiddle to follow alyssa", followsGraph.get("bbitdiddle").contains("alyssa"));
    }

    // 5. Multiple tweets from one user
    @Test
    public void testGuessFollowsGraphMultipleTweetsSameUser() {
        List<Tweet> tweets = Arrays.asList(
                new Tweet(1, "ernie", "hi @bert", d1),
                new Tweet(2, "ernie", "and hello @elmo", d2),
                new Tweet(3, "ernie", "@bert again, and @grover", d3));
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);

        Set<String> follows = followsGraph.get("ernie");
        assertTrue("expected ernie to follow bert", follows.contains("bert"));
        assertTrue("expected ernie to follow elmo", follows.contains("elmo"));
        assertTrue("expected ernie to follow grover", follows.contains("grover"));
    }

    // duplicate mentions of the same user collapse into one edge (it is a Set)
    @Test
    public void testGuessFollowsGraphDuplicateMention() {
        Tweet t = new Tweet(1, "ernie", "@bert @bert @bert", d1);
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        assertTrue("expected ernie to follow bert", followsGraph.get("ernie").contains("bert"));
        int bertCount = 0;
        for (String u : followsGraph.get("ernie")) {
            if (u.equals("bert")) bertCount++;
        }
        assertEquals("bert should appear once", 1, bertCount);
    }

    // a user does not follow themselves
    @Test
    public void testGuessFollowsGraphSelfMention() {
        Tweet t = new Tweet(1, "ernie", "note to self @ernie, also hi @bert", d1);
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(t));

        assertFalse("ernie must not follow ernie", followsOf(followsGraph, "ernie").contains("ernie"));
        assertTrue("expected ernie to follow bert", followsOf(followsGraph, "ernie").contains("bert"));
    }

    // usernames are case-insensitive
    @Test
    public void testGuessFollowsGraphCaseInsensitive() {
        List<Tweet> tweets = Arrays.asList(
                new Tweet(1, "Ernie", "hi @BERT", d1),
                new Tweet(2, "ERNIE", "and @Elmo", d2));
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);

        // compare ignoring case so we do not over-specify which casing is returned
        Set<String> follows = new HashSet<>();
        for (Map.Entry<String, Set<String>> e : followsGraph.entrySet()) {
            if (e.getKey().equalsIgnoreCase("ernie")) {
                for (String f : e.getValue()) follows.add(f.toLowerCase());
            }
        }
        assertTrue("expected ernie to follow bert", follows.contains("bert"));
        assertTrue("expected ernie to follow elmo", follows.contains("elmo"));

        // and "Ernie"/"ERNIE" must not produce two separate nodes
        int ernieKeys = 0;
        for (String k : followsGraph.keySet()) {
            if (k.equalsIgnoreCase("ernie")) ernieKeys++;
        }
        assertEquals("Ernie and ERNIE are the same user", 1, ernieKeys);
    }

    // no follow without evidence: every followed user must have been mentioned by that author
    @Test
    public void testGuessFollowsGraphNoUnmentionedFollows() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        assertFalse("bbitdiddle never mentioned rivest", followsOf(followsGraph, "bbitdiddle").contains("rivest"));
        assertFalse("alyssa must not follow herself", followsOf(followsGraph, "alyssa").contains("alyssa"));
    }

    // ---------------- influencers() ----------------

    // 6. Empty graph
    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        assertTrue("expected empty list", influencers.isEmpty());
    }

    // 7. Single user without followers
    @Test
    public void testInfluencersSingleUserNoFollowers() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alyssa", new HashSet<>());
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        // the only user has 0 followers; the list contains just that user
        assertEquals("expected exactly one user", 1, influencers.size());
        assertEquals("alyssa", influencers.get(0));
    }

    // 8. Single influencer
    @Test
    public void testInfluencersSingleInfluencer() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alyssa", new HashSet<>(Arrays.asList("rivest")));
        followsGraph.put("bbitdiddle", new HashSet<>(Arrays.asList("rivest")));
        followsGraph.put("carol", new HashSet<>());

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected all four users", 4, influencers.size());
        assertEquals("rivest should be top influencer (2 followers)", "rivest", influencers.get(0));
    }

    // 9. Multiple influencers with distinct counts -> exact order is determined
    @Test
    public void testInfluencersMultipleInfluencers() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alyssa", new HashSet<>(Arrays.asList("bbitdiddle", "rivest")));
        followsGraph.put("bbitdiddle", new HashSet<>(Arrays.asList("rivest")));
        followsGraph.put("carol", new HashSet<>(Arrays.asList("rivest", "bbitdiddle", "dave")));

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        // followers: rivest=3, bbitdiddle=2, dave=1, alyssa=0, carol=0
        assertEquals(5, influencers.size());
        assertEquals("rivest", influencers.get(0));
        assertEquals("bbitdiddle", influencers.get(1));
        assertEquals("dave", influencers.get(2));
        assertTrue(influencers.indexOf("dave") < influencers.indexOf("alyssa"));
        assertTrue(influencers.indexOf("dave") < influencers.indexOf("carol"));
    }

    // 10. Tied influence: both tied users present, ahead of lower-ranked users, order between them unspecified
    @Test
    public void testInfluencersTied() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alyssa", new HashSet<>(Arrays.asList("bbitdiddle", "rivest", "dave")));
        followsGraph.put("carol", new HashSet<>(Arrays.asList("bbitdiddle", "rivest")));

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        // followers: bbitdiddle=2, rivest=2 (tied), dave=1, alyssa=0, carol=0
        assertEquals(5, influencers.size());
        Set<String> topTwo = new HashSet<>(influencers.subList(0, 2));
        assertEquals("tied users should occupy the top two places",
                new HashSet<>(Arrays.asList("bbitdiddle", "rivest")), topTwo);
        assertEquals("dave should be next", "dave", influencers.get(2));
    }

    // user who appears only as a followed value (never as a key) must still be listed
    @Test
    public void testInfluencersUserOnlyAsFollowedValue() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alyssa", new HashSet<>(Arrays.asList("rivest")));

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals(2, influencers.size());
        assertEquals("rivest", influencers.get(0));
        assertTrue(influencers.contains("alyssa"));
    }

    // end-to-end: guessFollowsGraph feeding influencers
    @Test
    public void testGuessThenInfluencers() {
        List<Tweet> tweets = Arrays.asList(
                new Tweet(1, "alyssa", "hi @rivest", d1),
                new Tweet(2, "bbitdiddle", "hi @rivest and @alyssa", d2));
        List<String> influencers = SocialNetwork.influencers(SocialNetwork.guessFollowsGraph(tweets));

        assertEquals("rivest", influencers.get(0));
        assertEquals(3, influencers.size());
    }
}