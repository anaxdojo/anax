# Anax - Zephyr (ZAPI) Plugin
Include this plugin in order to programmatically update the status of your TCs on Jira via Zephyr api (ZAPI).

The plugin copy the jira cycle (which actually is the suite name) under the specified project from 'Unscheduled' folder to the specified version and then on execution finish update its TC status (PASS-FAIL-SKIP).
By default automated TC are matched on Jira with the label of each TC.

On fail / skip TC status sceenshot and video will be attached on the execution.

## How to include Jira Connector
### Maven 
Add the following dependency on your project pox.xml:
```xml
     <dependency>
         <groupId>org.anax.framework</groupId>
         <artifactId>anax-zapi</artifactId>
         <version>LATEST</version>
     </dependency>
```

### Gradle

Add the following dependency:

Then, add the dependency for Anax-Chrome to your project:
```gradle
compile 'org.anax.framework:anax-zapi:LATEST'
```

### Configure the following properties on your project application.properties file


Configure the zapi enabled variable, by default is enabled , so no need to include it except if you want to disable it:
```gradle
zapi.enabled = true
```

Configure the zapi rest api url:
```gradle
zapi.url = https://HOST_REQUIRED/rest/zapi/latest/
```

Configure the jira rest api url:
```gradle
jira.url = https://HOST_REQUIRED/rest/api/latest
```

Configure the jira username in order to authenticate the api connection:
```gradle
api.user = USER
```

Configure the jira password in order to authenticate the api connection:
```gradle
zapi.password = PWD
```

Configure the suite name that ANAX will execute, keep the same name as the cycle on Jira:
```gradle
anax.exec.suite = JIRA_CYCLE_NAME
```

Configure the jira project prefix:
```gradle
jira.project.prefix = PREFIX
```

Configure the jira project name:
```gradle
jira.project = JIRA_PROJECT
```
