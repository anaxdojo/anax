# Anax - Zephyr (ZAPI) Plugin
Include this plugin in order to programmatically update the status of your TCs on Jira via Jephyr api (ZAPI)

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

### Configure the following properties on your project application.properties file

Configure the following:
```anax.exec.suite= JIRA_CYCLE_NAME
```

Add the following:
```gradle
repositories { maven { url "https://dl.bintray.com/thanosa75/maven" } }
```
