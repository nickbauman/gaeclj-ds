# gaeclj-ds Steps

## 1. Prerequisites

1. Install Java (JDK 11, 17, or 21; JDK 21 preferred for modernization work).
2. Install Leiningen.
3. Clone this repository and open it in your editor.

## 2. Install Dependencies

From the project root, run:

```bash
lein deps
```

## 3. Run the Test Suite

From the project root, run:

```bash
lein test
```

## 4. Optional: Generate Coverage Report

```bash
lein cloverage
```

## 5. Verify Project Metadata

To confirm the project/version info from `project.clj`:

```bash
lein pprint
```

## 6. Test Different Java Compilation Targets

Use these commands from the project root:

```bash
lein test-java11
lein test-java17
lein test-java21
```
