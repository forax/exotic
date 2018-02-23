# exotic
A bestiary of classes implementing exotic semantics in Java

### MostlyConstant

A constant for the VM that can be changed by deoptimizing all the code that contains the previous value of the constant.

### StableField

A field that becomes a constant if the object itsef is constant and the field is initialized

### ConstantMemoizer

A function that returns constants values if its parameter is a constant.

### StructuralCall

A method call that can call different method if they have the same name and same parameter types.

