# pifuck
Just a toy language that is heavily inspired by PI

## What can this do

* Basic check if a file is valid including line endings and characters used
* Basic math operations (add, subtract, multiply, divide)
* Assign values to variables and print them.
* Create variables, use variables and do math / logic in one line
* Basic if statements


## Issues
If you found a bug, please create a new issue.

The issue must contain a [gist](https://gist.github.com/) with the minimal pifuck code to reproduce the issue.


## Contributing

Pull requests are welcome!

Before you open a pull request make sure that every example in the examples folder is working correctly!

## Cheatsheet

### PI

3.1415926535 8979323846 2643383279 5028841971 6939937510 5820974944 5923078164 0628620899 8628034825 3421170679


### Cheat sheet

| PI                     |  Thing                   |
|:----------------------:|:------------------------:|
| 3.                     |    line ending           |
| 3.1                    |    Variable              |
| 3.14                   |    Start if statement    |
| 3.141                  |    Start for loop        |
| 3.1415                 |    +                     |
| 3.14159                |    -                     |
| 3.141592               |    *                     |
| 3.1415926              |    /                     |
| 3.14159265             |    end LineManager       |
| 3.141592653            |    start foreach         |
| 3.1415926535           |    Call function         |
| 3.14159265358          |    ==                    |
| 3.141592653589         |    !=                    |
| 3.1415926535897        |    \>                    |
| 3.14159265358979       |    \<                    |
| +                      |    user input            |
| --                     |    Include file          |
| **                     |    Declare function      |

#### Basics
* Lines always ends with: `3.`
* If a line does not assign to a variable it will be printed.
* To have string in pifuck you must place it between `+` like so: `+example+`. Note: You can't use spaces in strings!
* See the examples folder for more pifuck :\)

#### Math
* Sum: `+3+ 3.1415 +3+ 3.`
* Subtract: `+3+ 3.14159 +3+ 3.`
* Multiply: `+3+ 3.141592 +3+ 3.`
* Divide: `+3+ 3.1415926 +3+ 3.`

#### Variables
Variables can contain a boolean, BigDecimal or string. It is also possible to initialize a variable so you can use it later.
* Assign to variable: `3.1 +variableName+ 3.1 +true+ 3.`
* Print a variable: `+variableName+ 3.`
* Advanced variable: `3.1 +name+ 3.1 +15+ 3.1415 +15 3.+`


#### Booleans
* equals: `+TestBoolean+ 3.14159265358 +TestBoolean+ 3.`
* !equals: `+TestBoolean+ 3.141592653589 +TestBoolean+ 3.`
* greater: `+TestBoolean+ 3.1415926535897 +TestBoolean+ 3.`
* smaller: `+TestBoolean+ 3.14159265358979 +TestBoolean+ 3.`

#### If statement 
* Start block: `3.14 +testValueOrVariable+ 3.`


#### For (each)
* Foreach iterates over target and stores temp value in Variable. (skips 0 and include == target)
  *  `3.141592653 +target+ +Variable+ 3.`
* For loop:
  *  `3.141  value    logicOperator    value   value mathOperator value 3.`
  *  `3.141 +value1+ 3.1415926535897 +value2+ +value2+ 3.1415 +2.0+ 3.`
