# Anax - A next-gen Software Quality Assurance Framework
Origin: ἄναξ (pronounced: /á.naks/ → /ˈa.naks/ → /ˈa.naks/) is a Greek word meaning 'the Leader' or 'the King'.  It is our intention to make Anax a leading software quality assurance framework.

## Status: Getting serious


We are using `Github Actions` for our builds. Current status:

- Develop [![Actions](https://github.com/anaxdojo/anax/actions/workflows/maven-build.yml/badge.svg?branch=develop)](https://github.com/anaxdojo/anax/actions/workflows/maven-build.yml)

We are currently experimenting with the major directions that Anax is going to take in the future. The 'develop' branch is considered _highly_ volatile, and the master branch is not stable either. However, we've already done *some* effort to decide on publishing and the first artefacts are now available in Bintray.

## How to work with our new release
### Maven 
Using the bintray repo is simple - create a new settings.xml file or add the "profiles" tag (usually in your MAVEN_HOME):
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'
          xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
    <profiles>
        <profile>
            <id>bintray</id>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-thanosa75-maven</id>
                    <name>bintray</name>
                    <url>https://dl.bintray.com/thanosa75/maven</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-thanosa75-maven</id>
                    <name>bintray-plugins</name>
                    <url>https://dl.bintray.com/thanosa75/maven</url>
                </pluginRepository>
            </pluginRepositories>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray</activeProfile>
    </activeProfiles>
</settings>
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
