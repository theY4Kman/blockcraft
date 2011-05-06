all: build jar

build: src/they4kman/blockcraft/*.java
	javac -cp ~/projects/blockcraft/bukkit-0.0.1-SNAPSHOT.jar:/usr/local/share/java/zmq.jar -d . src/they4kman/blockcraft/*.java

jar: they4kman/blockcraft/*.class plugin.yml
	jar -cf blockcraft.jar they4kman/ org/ plugin.yml
