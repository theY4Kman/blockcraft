all: build jar

build: src/they4kman/BADASS/*.java src/org/javaworld/JarResources.java
	javac -cp ~/projects/blockcraft/bukkit-0.0.1-SNAPSHOT.jar:/usr/local/share/java/zmq.jar -d . src/they4kman/BADASS/*.java src/org/javaworld/JarResources.java

jar: build plugin.yml
	jar -cf BADASS.jar they4kman/ org/ plugin.yml cutouts/
