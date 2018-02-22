import static com.github.forax.pro.Pro.*;
import static com.github.forax.pro.builder.Builders.*;

resolver.
    dependencies(list(
        "org.junit.jupiter.api=org.junit.jupiter:junit-jupiter-api:5.0.3",
        "org.junit.platform.commons=org.junit.platform:junit-platform-commons:1.0.3",
        "org.apiguardian.api=org.apiguardian:apiguardian-api:1.0.0",
        "org.opentest4j=org.opentest4j:opentest4j:1.0.0"
    ))

docer.
    rawArguments(list(
        "-quiet",
        "-link", "https://docs.oracle.com/javase/9/docs/api/"))
   
packager.
    moduleMetadata(list(
        "com.github.forax.exotic@1.0"
    ))   
    
run(resolver, compiler, tester, docer, packager)

/exit
