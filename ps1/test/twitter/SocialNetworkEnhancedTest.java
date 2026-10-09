package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkEnhancedTest {

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");

    @Test
    public void testSharedHashtagInfersFollow() {
        Tweet tweet1 = new Tweet(1, "alyssa", "learning about #mit software construction", d1);
        Tweet tweet2 = new Tweet(2, "bbitdiddle", "working on #mit problem set", d2);

        Map<String, Set<String>> graph = SocialNetworkEnhanced.guessEnhancedFollowsGraph(Arrays.asList(tweet1, tweet2));

        assertTrue("expected alyssa to follow bbitdiddle due to shared hashtag",
                graph.get("alyssa").contains("bbitdiddle"));
        assertTrue("expected bbitdiddle to follow alyssa due to shared hashtag",
                graph.get("bbitdiddle").contains("alyssa"));
    }
}