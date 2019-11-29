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

### Configure the following properties on your project application.properties file

Configure the suite name , keep the same name as the cycle on Jira:
```gradle
anax.exec.suite = JIRA_CYCLE_NAME
```

Configure the zapi enabled variable, By default is enabled , so no need to include except if you want to disable it:
```gradle
zapi.enabled = true
```

Configure the zapi url:
```gradle
zapi.url = ZAPI_URL
```

Configure the jira url:
```gradle
jira.url = ZIRA_URL
```

Configure the jira username in order to authenticate the api connection:
```gradle
api.user=USER
```

Configure the jira password in order to authenticate the api connection:
```gradle
zapi.password=PWD
```

Configure the jira project prefix:
```gradle
jira.project.prefix=PREFIX
```


Configure the jira project name:
```gradle
jira.project=JIRA_PROJECT
```
