# Buildscripts

- [x] add constants generator

# Core

## Notebook

- [ ] register Result Adapter
- [ ] get closest matching Result Adapter for given KType

## Page

- [ ] register other files to watch for triggering a reload or rerun

## Script Compiler

- [x] Handle Compilation Fail different than waiting on results to be available
  - `compiledScript` will be `null`

## PathWatcher

- [x] watch folder -> register new notebooks
- [x] watch notebook files -> relink pages
- [x] watch pages -> rerun tasks and visualizations

## Dependencies

possibly annotations on method arguments

## Gradle Plugin

- [x] Extension
- [x] ShadowJar tasks
- [ ] register notebooks in extension
- [ ] set dependencies for notebook
- [ ] different configuration and shadowJar tasks for each notebook

# GUI TornadoFx

- [ ] Page dependency digraph drawer with clickable features
- [ ] JVM dependency manager

- [ ] RichTextFX  
      https://github.com/FXMisc/RichTextFX/wiki

# Visualizations

- raw
- String
- Table (differing column count)
- Plot
  - Bars
  - Lines
  - Pie Chart