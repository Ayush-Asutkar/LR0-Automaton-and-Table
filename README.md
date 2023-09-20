# Compiler Design Lab

---

## LR(0) Parsing
## Following are my details for assignment submission:
<p>Name: &nbsp;&nbsp;&nbsp;&nbsp;Ayush Vinayak Asutkar</p>
<p>Roll No.: &nbsp;20CS01057</p>
<p>Semester: &nbsp;7th</p>
<p>Year of study: &nbsp;4th year</p>
<p>Subject: &nbsp;&nbsp;Compiler Design Laboratory</p>
<p>Assignment: &nbsp;Assignment - 6</p>

---

## How to run
1. Clone the repository: https://github.com/Ayush-Asutkar/LR0-Automaton-and-Table.git
2. Open in your favourite editor. (The editor used while making this project was Intellij IDEA and also the path are currently coded to handle only windows)
3. Run the complete project by running the Main.java in src folder. Follow the prompt to give input.

## Problem Statement
<p>Develop a program that takes any grammar "G" as input and has modules/functionalities to generate the 
LR(0) automaton and the LR(0) parse table for the given grammar</p>

## Input Grammar Format
1. First line should contain the start symbol.
2. Second line should contain the List of Non-terminal symbols in space separated manner.
3. Next Line should contain the list of Terminal symbols in space separated manner.
4. Next till the end of file, should contain production rule in format:
   <p>D -> var L : T ;</p>
   <p>T -> integer | real</p>
   <p>The left hand side and right side should be separated by ->. The right hand side rules should be separated by |. And for each rule, the symbols should be space separated</p>

   <details>
   <summary>Example input grammar</summary>

   <p>D</p>
   <p>D T L</p>
   <p>id : ; , var integer real</p>
   <p>D -> var L : T ;</p>
   <p>T -> integer | real</p>
   <p>L -> L , id</p>
   <p>L -> id</p>

   </details>
   

<p>NOTE: The input grammar should be written <a href="Input/InputGrammar.txt">here</a>.</p>

## Flow of working of the code
<p>Check the Main.java file to understand the flow. Following are the steps:</p>

1. Create empty output directory.
2. Take the input grammar from "InputGrammar.txt".
3. Print the input grammar to output file.
4. Compute transitions of the LR(0) automaton.
5. Compute indexing of states.
6. Compute the parsing table.
7. Print the transitions, indexing of states and parsing table to output file.

