# Script Stages

## Pages

1. Compilation  
   File is parsed and evaluated to a PageScript  
   Script is stored in `compiledScripts`
   
2. Execution  
   Reflection finds the `process` function  
   and tests if the return values of parameter scripts (have to be compiled) match
   `process` is executed, result stored in `results`