# Anax - A next-gen Software Quality Assurance Framework
Origin: ἄναξ (pronounced: /á.naks/ → /ˈa.naks/ → /ˈa.naks/) is a Greek word meaning 'the Leader' or 'the King'.  It is our intention to make Anax a leading software quality assurance framework.

## Status: Getting serious


We are using `Github Actions` for our builds. Current status:

- Develop [![Actions](https://github.com/anaxdojo/anax/actions/workflows/maven-build.yml/badge.svg?branch=develop)](https://github.com/anaxdojo/anax/actions/workflows/maven-build.yml)

We are currently experimenting with the major directions that Anax is going to take in the future. The 'develop' branch is considered _highly_ volatile, and the master branch is not stable either. However, we've already done *some* effort to decide on publishing and the first artefacts are now available in Bintray.

## How to work with our new release
### Maven 

Complete instructions are in the page [Installing a Package.](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#installing-a-package)

Then, add the dependency for Chrome to your project:
```xml
<dependency> 
    <groupId>org.anax.framework</groupId> 
    <artifactId>anax-chrome</artifactId> 
    <version>LATEST</version>
</dependency>
```

### Gradle

For gradle [see instructions here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package).

Then, add the dependency for Anax-Chrome to your project:
```gradle
compile 'org.anax.framework:anax-chrome:LATEST'
```

## The 10m quick intro
For the impatient and eager, we have created a 10m quick guide. Read on [Setting up an Anax project (10m guide)](https://github.com/thanosa75/anax/wiki/Anax-Setup-in-10m) to find out. For a more detailed and feature full explanation of what Anax is, you need to see the [Anax explained](https://github.com/thanosa75/anax/wiki/Anax-Explained) page.
