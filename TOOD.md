# Buildscripts

- [ ] add constants generator

# Core

## Script COmpiler

- [ ] use withObject on scripts to pass previous compiled scripts ?  
  NOTE: would not allow access to functions defined in other scripts.. (i think)  
  NOTE: `@Import`/`@Include` seems like a cleaner solution
- [ ] Handle Compilation Fail different than waiting on results to be available

## PathWatcher

- [x] watch folder -> register new notebooks
- [x] watch notebook files -> relink pages
- [x] watch pages -> rerun tasks and visualizations

## Dependencies

possibly annotations on method arguments

## Gradle Plugin

- [ ] Extension
- [ ] ShadowJar tasks


# GUI TornadoFx

- [ ] Finish conversion layer to send data back to KNote module to compile and send results
- [ ] Page dependency digraph drawer with clickable features
- [ ] JVM dependency manager

- [ ] RichTextFX  
      https://github.com/FXMisc/RichTextFX/wiki
