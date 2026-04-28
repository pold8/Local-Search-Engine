package org.example.parser;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {

    @Test
    void testParseNullOrBlank() {
        ParsedQuery q1 = QueryParser.parse(null);
        assertFalse(q1.isQualified());
        assertEquals("", q1.getRawQuery());
        assertTrue(q1.getContentTerms().isEmpty());
        assertTrue(q1.getPathTerms().isEmpty());

        ParsedQuery q2 = QueryParser.parse("   ");
        assertFalse(q2.isQualified());
        assertEquals("", q2.getRawQuery());
        assertTrue(q2.getContentTerms().isEmpty());
        assertTrue(q2.getPathTerms().isEmpty());
    }

    @Test
    void testParseUnqualified() {
        ParsedQuery q = QueryParser.parse("hello world");
        assertFalse(q.isQualified());
        assertEquals("hello world", q.getRawQuery());
        assertTrue(q.getContentTerms().isEmpty());
        assertTrue(q.getPathTerms().isEmpty());
    }

    @Test
    void testParseQualifiedContent() {
        ParsedQuery q = QueryParser.parse("content:hello content:world");
        assertTrue(q.isQualified());
        assertEquals("content:hello content:world", q.getRawQuery());
        assertEquals(List.of("hello", "world"), q.getContentTerms());
        assertTrue(q.getPathTerms().isEmpty());
    }

    @Test
    void testParseQualifiedPath() {
        ParsedQuery q = QueryParser.parse("path:src path:main");
        assertTrue(q.isQualified());
        assertEquals("path:src path:main", q.getRawQuery());
        assertTrue(q.getContentTerms().isEmpty());
        assertEquals(List.of("src", "main"), q.getPathTerms());
    }

    @Test
    void testParseMixed() {
        ParsedQuery q = QueryParser.parse("content:hello path:src other token");
        assertTrue(q.isQualified());
        assertEquals("content:hello path:src other token", q.getRawQuery());
        assertEquals(List.of("hello"), q.getContentTerms());
        assertEquals(List.of("src"), q.getPathTerms());
    }
    
    @Test
    void testParseEmptyValues() {
        ParsedQuery q = QueryParser.parse("content: path:");
        assertFalse(q.isQualified());
        assertEquals("content: path:", q.getRawQuery());
        assertTrue(q.getContentTerms().isEmpty());
        assertTrue(q.getPathTerms().isEmpty());
    }
}
