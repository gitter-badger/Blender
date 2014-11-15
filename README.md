Particle Compiler
===============

The goal of particle compiler is to create a codebase for easily building a toy compiler for any language. It may be used to experiment with language feature or as a way of teaching compiler construction. The approach taken is to define compiler 'particles' that represent language features and which can be combined in many ways. A similar approach is described in the paper '*A Nanopass Framework for Compiler Education*'. 

###Particles
A particle can include one or several phases such as parsing, type checking, optimization and code generation. Commonly compilers are modularised by defining several compiler phases and using a limited number of intermediating languages. Particle compiler focusses purely on intermediate languages to achieve modularisation. Using weak typing it becomes simple to define a new intermediate language by applying a delta to an existing one.

###GUI
When particle compiler is run, you are presented with a GUI. Here the defined particles and their dependencies can be viewed. Also, particles can be combined to create a compiler. Lastly, given a built compiler we perform several tasks. Ofcourse we can use the compiler to compile some code, but also we can ask the compiler for its in- and output grammar.

###BiGrammar
To enable parsing and printing with little development effort, the concept of a 'BiGrammar' is created. A BiGrammar defines a bidirectionmal mapping between text and an AST. The approach taken here is very similar to that described by the paper '*Invertible Syntax Descriptions: Unifying Parsing and Pretty Printing*'. Also, a BiGrammar may be defined in a left recursive fashion because our implementation uses packrat parsing as described in '*Packrat Parsing: Simple, Powerful, Lazy, Linear Time*' to deal with problems associated with such grammars.
