# Banksystem

A Java console-based banking system with customer and account management, deposits and withdrawals, transfers, fixed deposits, paused transactions, search, monthly reports, and file persistence.

## Requirements

- Java JDK 17 or newer
- Terminal or an IDE such as IntelliJ IDEA

## Run the application

Open a terminal in the project root and run:

**Windows (PowerShell):**

```powershell
New-Item -ItemType Directory -Force build | Out-Null
javac -encoding UTF-8 -d build (Get-ChildItem Folders/src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp build Main
```

**macOS / Linux:**

```bash
mkdir -p build
javac -encoding UTF-8 -d build $(find Folders/src -name '*.java')
java -cp build Main
```

The application creates a `data/` directory in the current working directory on first launch. Keep this directory to preserve the bank's saved state. Do not delete `bank_state.dat` while keeping the other saved-data files unless you intentionally want to recover or reset the data store.

## Run the test harnesses

Compile the source and test files:

**Windows (PowerShell):**

```powershell
New-Item -ItemType Directory -Force build | Out-Null
javac -encoding UTF-8 -d build (Get-ChildItem Folders/src,Folders/tests -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

**macOS / Linux:**

```bash
mkdir -p build
javac -encoding UTF-8 -d build $(find Folders/src Folders/tests -name '*.java')
```

Then run each test harness from the project root (the harnesses use a local `data/` folder, so run them in a disposable test directory if you have real bank data):

```text
CoreLogicTestHarness
FixedDepositScenarioHarness
IntegrationTestHarness
PersistenceTestHarness
SearchTestHarness
```

For example, run one harness with `java -cp build CoreLogicTestHarness` after compilation.

## Project layout

- `Folders/src/` — application source code
- `Folders/tests/` — standalone test harnesses
- `data/` — generated runtime persistence files; not included in this project archive

## Notes

- The project intentionally keeps its existing class structure, menu layout, and coding style.
- Compile output is generated into `build/` and should not be committed.
