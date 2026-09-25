# Guidelines for Jules

* When compiling Java files directly without Maven (e.g., using `javac`), do not compile class files into the source directories (`src/main/java` or similar). Always use the `-d` flag to output compiled classes to a separate directory such as `target/classes` or another temporary directory.
