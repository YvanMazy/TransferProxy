## 🌐 Overview

**TransferProxy** is a proxy for Minecraft **Java Edition**, harnessing the power of the new transfer packet feature
introduced in Minecraft **24w03a**
(release in 1.20.5). This feature allows server-to-server transfers, opening up a myriad of possibilities for server
networks.
Whether it's for large-scale server networks or small private servers, TransferProxy aims to provide a versatile and
efficient solution for Minecraft server management.

Here are the project features:

- ⚡ **Performance**: The project was designed to be lightweight and inexpensive in terms of resources. It is capable of
  supporting thousands of requests simultaneously.
- ⚙️ **Plugins**: The project works with plugins, which allows you to create your own redirection rules.
- 🍪 **Cookie**: The cookie system added in the same snapshot is also implemented.
- 🌎 **Community**: The project aims to be community and collaborative, suggestions are appreciated.
- 📚 **Wiki**: Documentation can be found [here](https://github.com/Darkkraft/TransferProxy/wiki), if any information is
  missing feel free to make an issue.

## 📥️ Installation and Setup

1. Download the latest version of TransferProxy by clicking [here](https://github.com/Darkkraft/TransferProxy/releases).
2. Drag and drop the jar file into a folder.
3. Start the server with ``java -jar TransferProxy-version.jar``
4. The configuration and plugins folder should be created.

#### ➡️ Now what's next?

When your server is correctly installed, you must install a plugin to define the redirection rules and customize the
motd if necessary. You have two options:

- Download an official plugin [here](https://github.com/Darkkraft/TransferProxy/wiki#official-plugins), if you want to
  do tests or if your project is small.
- Develop your own plugin, documentation is available [here](https://github.com/Darkkraft/TransferProxy/wiki/Plugins).

In case you want to develop your own plugin, a demo repository is
available [here](https://github.com/Darkkraft/TransferProxy-Demo-plugin). It uses Gradle.

#### 🆘 Troubleshooting

If you have any problems using TransferProxy, the first thing to do is to check that you haven't missed anything in
the [official wiki](https://github.com/Darkkraft/TransferProxy/wiki).
<br>If you can't find a solution, then you can create a GitHub issue. It should get an answer quickly.

## 📋 Requirements

- Java version: 17 or higher.
- Minecraft client version: 1.20.5 or higher, to support the transfer packet functionality.

## 🔌 Contribution

Contributions are highly encouraged! Whether it's through raising issues or submitting pull requests. Forks are even
recommended to best adapt TransferProxy to your server. The project was designed to make it easy to modify certain
parts.

⭐ If you're interested in the project, please leave a star to show your support. It's through contributions like these that the project is able to exist and thrive.

## 📄 License - MIT

TransferProxy is under the MIT License, offering the freedom to use, modify, and distribute the software. This open
license is ideal for collaborative development but does not provide any warranty or liability protection.

---

*Note: TransferProxy is constantly evolving. Features, documentation, and setup instructions are subject to change as
the project progresses.*
