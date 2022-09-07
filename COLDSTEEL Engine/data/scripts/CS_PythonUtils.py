"""
This file contains helper functions allowing scripters to check state of the 
scripting environment in their scripts and act accordingly. Additionally, it
contains functions to affect scripting state, such as clearing the return stack
and args array, or moving return values into the args array.

"""
import sys
def getPath(returnStack):
    returnStack.add(sys.path)
    return sys.path
    
def printReturn(returnStack):
    if(returnStack.size() != 0):           
        for x in returnStack:
            print(x)
    else:
        print("Stack empty")
        
def clearReturn(returnStack):
    if returnStack.size() != 0:    
        returnStack.pop()
        emptyReturns(returnStack)

def printArgs(globalArgs):
    numberArgs = 0
    for x in globalArgs:
        if x is None:
            continue
        numberArgs = numberArgs + 1
        print(x)
        
    print("number args:" , numberArgs)

def clearArgs(globalArgs):
    for i in range(24):
        if globalArgs[i] != None:
            globalArgs[i] = None
            
#removes numberReturns number of elements from the 
#Return stack and puts them in the args array
def putReturnInArgs(numberReturns , returnStack , globalArgs):
    
    startingIndex = 0    
    for y in range(24):
        if globalArgs[y] is None:
            startingIndex = y
            break
        else:
            continue
                
    print("args starting index: " , startingIndex)
    for i in range(numberReturns):        
        globalArgs[startingIndex] = returnStack.pop()
        startingIndex = startingIndex + 1
        