all: build jar

build: src/they4kman/hitb/*.java src/org/javaworld/JarResources.java
	javac -cp ~/projects/blockcraft/bukkit-0.0.1-SNAPSHOT.jar:/usr/local/share/java/zmq.jar -d . src/they4kman/hitb/*.java src/org/javaworld/JarResources.java

jar: build plugin.yml
	jar -cf hitb.jar they4kman/ org/ plugin.yml cutouts/
