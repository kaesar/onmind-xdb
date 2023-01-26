# OnMind-XDB (Javalin)

You need to have installed **Java** (JDK or **GraalVM**), just check with `java --version`. To build (e.g. macOS) run:

```bash
./gradlew build
```

To launch it, you can run:

```bash
./gradlew run
```

> You can check the service in a browser with the address: `http://localhost:9990`

## Trying to make a binary

To make a binary with **GraalVM** (e.g. macOS) run:

```bash
sh make-native-mac.sh
```

> To do this you need to have installed **GraalVM** and **Native Image**, example: `gu install native-image`
