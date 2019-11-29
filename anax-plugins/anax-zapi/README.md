# Anax - Zephyr (ZAPI) Plugin

## How to include Jira Connector
### Maven 
Add the following dependency on your project pox.xml:
```xml
      <dependency>
            <groupId>org.anax.framework</groupId>
            <artifactId>anax-zapi</artifactId>
            <version>${anax.version}</version>
        </dependency>
```

Then, add the dependency for Chrome to your project:
```xml
<dependency> 
    <groupId>org.anax.framework</groupId> 
    <artifactId>anax-chrome</artifactId> 
    <version>LATEST</version>
</dependency>
```

### Gradle

Add the following:
```gradle
repositories { maven { url "https://dl.bintray.com/thanosa75/maven" } }
```

Then, add the dependency for Anax-Chrome to your project:
```gradle
compile 'org.anax.framework:anax-chrome:LATEST'
```

## The 10m quick intro
For the impatient and eager, we have created a 10m quick guide. Read on [Setting up an Anax project (10m guide)](https://github.com/thanosa75/anax/wiki/Anax-Setup-in-10m) to find out. For a more detailed and feature full explanation of what Anax is, you need to see the [Anax explained](https://github.com/thanosa75/anax/wiki/Anax-Explained) page.
