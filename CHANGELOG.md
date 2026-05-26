# Changelog

Every version of Local Search Engine, documented from first commit to latest release.
This project follows [Semantic Versioning](https://semver.org/).

---

## [3.0.0] — 2026-05-26 · *Iteration 3: Information Design*

> The engine learns to see. Indexing goes parallel, queries get smarter before
> they even reach the database, and the UI reacts to what it finds.

**Multimodal indexing**
The indexer now understands images. When it encounters a `.jpg`, `.png`, `.gif`,
`.bmp`, or `.jpeg`, it samples the pixels via `BufferedImage`, computes the average
RGB value, and maps it to a human-readable color name stored in the database.
Text files go through the existing content extractor as before. The two behaviors
are separated behind a `FileIndexingStrategy` interface — `TextFileStrategy` and
`ImageFileStrategy` — with a `FileProcessor` picking the right one per extension.

**Color search**
A new `color:` qualifier lets users filter results by dominant image color.
Typing `color:red` returns only image files whose dominant color was classified
as red at index time.

**Context-aware widgets**
After every search, a `WidgetFactory` inspects the result set and activates
relevant widgets automatically. A result set heavy with `.log` files triggers
the `LogAnalyzerWidget`; one heavy with images triggers the `ImageGalleryWidget`.
Widgets are fully decoupled from the search engine — it has no knowledge of them.

**Query pre-processing pipeline**
Raw user input is now transformed before reaching SQLite's FTS engine. Three
`QueryBuilder` decorators wrap each other in a fixed order: `SanitizationDecorator`
removes characters that break FTS syntax, `SynonymDecorator` expands shorthand
(e.g. `img` becomes `img OR image OR photo`), and `LogicDecorator` appends
wildcards for prefix matching. Each decorator is independently swappable.

**Parallel indexing**
Indexing is no longer single-threaded. A pool of `FileParserWorker` threads
(one per available CPU core) reads and parses files concurrently, pushing
`FileRecord` objects into a `LinkedBlockingQueue`. A single `DatabaseWriterWorker`
drains the queue and commits records to SQLite — ensuring no concurrent writes.
Workers signal completion via poison pills; the writer shuts down only after
every producer has finished.

---

## [2.0.0] — 2026-04-09 · *Iteration 2: Ranking*

> The engine stops treating all results as equal. Queries gain structure,
> results gain order, and the system starts learning from how it is used.

**Structured query language**
Searches now support `path:` and `content:` qualifiers in any order and
combination (e.g. `path:docs content:budget`). Duplicate qualifiers are
merged with `AND`. The `QueryEngine` was updated to accept a structured
`ParsedQuery` instead of a raw string.

**Path scoring**
Every file receives a numeric score at index time, computed by `PathScorer`
from four signals: path depth, file size, extension type, and recency of
last access. This score persists in the database and influences result
ordering at query time alongside FTS5's built-in BM25 ranking.

**Swappable ranking strategies**
Three ranking algorithms are available and can be switched at runtime without
touching the engine: relevance (BM25 + path score), alphabetical, and
date-accessed. The active strategy is injected into `QueryEngine`.

**Search history**
Every executed query is recorded. The history feeds two features: query
suggestions as the user types, and a ranking boost for files that appeared
in results of similar past searches. History observation is wired in using
the Observer Pattern.

**Tests**
Unit tests cover the query parser and all three ranking strategies.

---

## [1.0.0] — 2026-03-11 · *Iteration 1: Data Transformation*

> The foundation. The engine can walk a filesystem, understand what it finds,
> store it intelligently, and answer questions about it.

**Filesystem crawling**
The `Crawler` walks the configured root directory recursively. It detects and
breaks symlink loops, recovers from permission errors without crashing, and
respects a configurable ignore list for directories and file extensions.

**Content extraction**
The `Extractor` interface (Strategy Pattern) separates format-specific parsing
from the indexing pipeline. `FileParser` implements it for text-based files,
extracting content and generating a short contextual preview of each match.

**SQLite + FTS5 index**
Extracted content and metadata are stored in SQLite using the FTS5 extension
with BM25 ranking. The schema captures filename, path, extension, size,
timestamps, content, and preview.

**Incremental indexing**
`ChangeDetector` compares each file's last-modified timestamp against the stored
record. Only new or changed files are processed — subsequent index runs are fast.

**Runtime configuration**
All engine parameters (root directory, database path, ignored paths, text
extensions, report format) live in `config.json` and are loaded at startup
with no recompilation required.

**CLI REPL**
The application runs as an interactive prompt. Available commands: `index`,
`search <query>`, `help`, `exit`, `quit`. Search output includes ranked results
with file previews and a plain-English match reason per result.

**Index reports**
Every `index` run concludes with a summary report: files added, updated,
skipped, and any errors encountered.