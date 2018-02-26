# exotic
A bestiary of classes implementing exotic semantics in Java

### MostlyConstant

A constant for the VM that can be changed by de-optimizing all the codes that contain the previous value of the constant.

### StableField

A field that becomes a constant if the object itsef is constant and the field is initialized

### ConstantMemoizer

A function that returns a constant value if its parameter is a constant.

### StructuralCall

A method call that can call different method if they have the same name and same parameter types.



## Build Tool Integration [![](https://jitpack.io/v/forax/exotic.svg)](https://jitpack.io/#forax/exotic)

Get latest binary distribution via [JitPack](https://jitpack.io/#forax/exotic) 


### Maven

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    <dependency>
        <groupId>com.github.forax</groupId>
        <artifactId>exotic</artifactId>
        <version>master-SNAPSHOT</version>
    </dependency>


### Gradle

    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        compile 'com.github.forax:exotic:master-SNAPSHOT'
    }
