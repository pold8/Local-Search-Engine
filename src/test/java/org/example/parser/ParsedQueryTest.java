package org.example.parser;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ParsedQueryTest {

    @Test
    void testIsQualified() {
        ParsedQuery q1 = new ParsedQuery("test", List.of(), List.of(), null);
        assertFalse(q1.isQualified());

        ParsedQuery q2 = new ParsedQuery("content:test", List.of("test"), List.of(), null);
        assertTrue(q2.isQualified());

        ParsedQuery q3 = new ParsedQuery("path:test", List.of(), List.of("test"), null);
        assertTrue(q3.isQualified());

        ParsedQuery q4 = new ParsedQuery("content:test path:test", List.of("test"), List.of("test"), null);
        assertTrue(q4.isQualified());

        ParsedQuery q5 = new ParsedQuery("color:red", List.of(), List.of(), "red");
        assertTrue(q5.isQualified());
    }

    @Test
    void testGetters() {
        ParsedQuery q = new ParsedQuery("content:hello path:world", List.of("hello"), List.of("world"), null);
        assertEquals("content:hello path:world", q.getRawQuery());
        assertEquals(List.of("hello"), q.getContentTerms());
        assertEquals(List.of("world"), q.getPathTerms());
        assertNull(q.getColorTerm());
    }

    @Test
    void testColorTermGetter() {
        ParsedQuery q = new ParsedQuery("color:blue", List.of(), List.of(), "blue");
        assertEquals("blue", q.getColorTerm());
    }

    @Test
    void testKeywordsAreUnmodifiable() {
        ParsedQuery q = new ParsedQuery("test", List.of("c"), List.of("p"), null);
        assertThrows(UnsupportedOperationException.class, () -> q.getContentTerms().add("new"));
        assertThrows(UnsupportedOperationException.class, () -> q.getPathTerms().add("new"));
    }
}
