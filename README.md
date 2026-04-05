# About Placeholder API Paper
This is an unofficial port of Placeholder API to PaperMC, check out the original by Patbox [here](https://modrinth.com/mod/placeholder-api) and a NeoForge port [here](https://modrinth.com/project/placeholder-api-neoforge).

Placeholder API is a small, JIJ-able API that allows creation and parsing placeholders within strings and Minecraft Text Components.
Placeholders use simple format of `%modid:type%` or `%modid:type/data%`.
It also includes simple, general usage text format indented for simplifying user input in configs/chats/etc.

For information about usage (for developers and users) you can check official docs of the original Fabric version at https://placeholders.pb4.eu/!

## Developers
To depend on Placeholder API Paper, add the following to your buildscript:
```groovy
repositories {
    // ... other repositories
    maven("https://maven.offsetmonkey538.top/releases") { name = "OffsetMonkey538" }
}

dependencies {
    // ... other dependencies
    implementation "eu.pb4:placeholder-api-paper:[VERSION]"
}
```
This will allow you to use the library in your project, but requires users to manually download it.  
If you want to JIJ the library, use the [Shadow]() Gradle plugin, or see the Paper documentation for how to download it at runtime [here](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/#loaders).

Make sure to replace the `[VERSION]` with an actual valid version. For a list of available versions, check the maven [here](https://maven.offsetmonkey538.top/#/releases/eu/pb4/placeholder-api-paper).
