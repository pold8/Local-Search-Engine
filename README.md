# Local Search Engine

A robust, modular Java application that acts as a local file search engine. It crawls the local filesystem, extracts metadata and text content, and indexes it into an SQLite database (using FTS5 for full-text search). It includes an interactive CLI REPL for querying the index.

## Features

- **Interactive REPL CLI**: Run the app once and interactively execute commands.
- **Incremental Indexing**: Skips unchanged files, updates modified files, and adds new ones.
- **Full-Text Search (FTS5)**: Fast content matching using SQLite FTS5 extension.
- **Smart Filtering**: Configurable ignored extensions and directories via `config.json`.
- **Symlink Loop Detection**: Safely crawls complex filesystem structures.

## Installation & Setup

Requirements:
- Java 17+
- Maven
- SQLite

1. Clone the repository.
2. Edit `config.json` to suit your needs (e.g., set the `rootDirectory`).
3. Compile the project:
   ```bash
   mvn compile
   ```
4. Run the interactive CLI:
   ```bash
   mvn -q exec:java
   ```
   *(The `-q` flag silences Maven's built-in compile logs so the console only displays the application.)*

## Usage

Once the application starts, it enters an interactive prompt where you can run the following commands:

- `index` - Crawls the configured root directory and indexes the files.
- `search <query>` - Searches the indexed database for the given phrase/word. Returns ranked results with context previews and match reasons.
- `help` - Displays the help menu.
- `exit` or `quit` - Closes the application.

## Architecture

The project follows the C4 architecture model dividing the system into 4 structured lenses. See [ARHITECTURE.md](ARHITECTURE.md) for detailed diagrams mapping System Context, Containers, Components, and Code (Classes).

## Built With

- Java 17
- Maven
- SQLite JDBC (`org.xerial:sqlite-jdbc`)
- Jackson Databind (`com.fasterxml.jackson.core:jackson-databind`)
- SLF4J (`org.slf4j:slf4j-nop`)
