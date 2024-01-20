## 🌐 Overview

TransferProxy is a proxy for Minecraft, harnessing the power of the new transfer packet feature introduced in Minecraft 24w03a
(release in 1.20.5). This feature allows server-to-server transfers, opening up a myriad of possibilities for server networks.
Whether it's for large-scale server networks or small private servers, TransferProxy aims to provide a versatile and
efficient solution for Minecraft server management.

## 🚀 Features

- **Transfer packet**: Automatically transfers players upon connection using the new "Transfer packets".
- **High performance**: Built to be very light and fast, it aims to have minimal resource cost.
- **Flexibility**: Designed to support plugins for targeted server selection and custom redirection strategies.

## ⚙️ How to use it ?

The project is currently under development; but it is already possible to use it. To do this you will have to clone the
repository and modify the Main class
located [here](https://github.com/Darkkraft/TransferProxy/blob/master/core/src/main/java/be/darkkraft/transferproxy/main/Main.java#L38).
It's a little tedious but that will quickly change.

Here is an example of code that works:

```java
final TransferProxy proxy = new TransferProxyImpl(configuration);

proxy.getModuleManager().setLoginHandler(connection -> {
    if (connection.getName().contains("potato")) { // Automatically kick players with "potato" in their name
        connection.forceDisconnect();
        return;
    } else if (connection.getName().equals("Darkkraft")) { // Transfer the player to the development server if his name is "Darkkraft"
        connection.transfer("dev.my-domain.com", 25565);
        return;
    }
    connection.transfer("lobby.my-domain.com", 25565); // Transfers the player to the lobby server.
});

proxy.start();
```

## 📋 Requirements

- Java version: 17 or higher.
- Minecraft client version: 1.20.5 or higher, to support the transfer packet functionality.

## 📥 Installation and Setup

Setting up TransferProxy is straightforward:

1. Download and launch the TransferProxy JAR file.
2. A default configuration file will be created. (More information [coming soon](#)).
3. Then choose an existing plugin or develop your own plugin. This will allow you to configure the redirection rules.

## 🔌 Contribution

Contributions are highly encouraged! Whether it's through raising issues or submitting pull requests. Forks are even
recommended to best adapt TransferProxy to your server. The project was designed to make it easy to modify certain
parts.

## 📄 License - MIT

TransferProxy is under the MIT License, offering the freedom to use, modify, and distribute the software. This open
license is ideal for collaborative development but does not provide any warranty or liability protection.

---

*Note: TransferProxy is constantly evolving. Features, documentation, and setup instructions are subject to change as
the project progresses.*
