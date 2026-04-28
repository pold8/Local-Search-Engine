package org.example.parser;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ParsedQueryTest {

    @Test
    void testIsQualified() {
        ParsedQuery q1 = new ParsedQuery("test", List.of(), List.of());
        assertFalse(q1.isQualified());
        
        ParsedQuery q2 = new ParsedQuery("content:test", List.of("test"), List.of());
        assertTrue(q2.isQualified());
        
        ParsedQuery q3 = new ParsedQuery("path:test", List.of(), List.of("test"));
        assertTrue(q3.isQualified());
        
        ParsedQuery q4 = new ParsedQuery("content:test path:test", List.of("test"), List.of("test"));
        assertTrue(q4.isQualified());
    }
    
    @Test
    void testGetters() {
        ParsedQuery q = new ParsedQuery("content:hello path:world", List.of("hello"), List.of("world"));
        assertEquals("content:hello path:world", q.getRawQuery());
        assertEquals(List.of("hello"), q.getContentTerms());
        assertEquals(List.of("world"), q.getPathTerms());
    }
    
    @Test
    void testKeywordsAreUnmodifiable() {
        ParsedQuery q = new ParsedQuery("test", List.of("c"), List.of("p"));
        assertThrows(UnsupportedOperationException.class, () -> q.getContentTerms().add("new"));
        assertThrows(UnsupportedOperationException.class, () -> q.getPathTerms().add("new"));
    }
}
