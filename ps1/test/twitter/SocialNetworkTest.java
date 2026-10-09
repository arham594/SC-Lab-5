package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Testing strategy:
     * 
     * guessFollowsGraph():
     * - Partition by number of tweets: zero tweets, tweets with no mentions, tweets with mentions.
     * - Partition by mentions: single mention, multiple mentions, self-mention (author mentioning themselves), duplicate mentions.
     * - Partition by authors: single author, multiple authors.
     * 
     * influencers():
     * - Partition by graph size/structure: empty graph, graph with no edges, graph with a single influencer, graph with multiple influencers with distinct and tied follower counts.
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "hello @bbitdiddle and @rivest", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "hello @alyssa", d2);

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());
        assertTrue("expected empty graph", followsGraph.isEmpty());
    }
    
    @Test
    public void testGuessFollowsGraphNonEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));
        
        assertFalse("expected non-empty graph", followsGraph.isEmpty());
        assertTrue("expected alyssa to follow bbitdiddle", followsGraph.get("alyssa").contains("bbitdiddle"));
        assertTrue("expected alyssa to follow rivest", followsGraph.get("alyssa").contains("rivest"));
        assertTrue("expected bbitdiddle to follow alyssa", followsGraph.get("bbitdiddle").contains("alyssa"));
    }

    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        assertTrue("expected empty list", influencers.isEmpty());
    }

    @Test
    public void testInfluencersNonEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alyssa", Set.of("bbitdiddle", "rivest"));
        followsGraph.put("bbitdiddle", Set.of("rivest"));
        
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        
        assertEquals("expected three users in influencers list", 3, influencers.size());
        assertEquals("rivest should be top influencer (most followers)", "rivest", influencers.get(0));
    }
}