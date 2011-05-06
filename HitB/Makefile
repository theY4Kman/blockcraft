all: build jar

build: src/they4kman/hitb/*.java
	javac -cp ~/projects/blockcraft/bukkit-0.0.1-SNAPSHOT.jar:/usr/local/share/java/zmq.jar -d . src/they4kman/hitb/*.java

jar: they4kman/hitb/*.class plugin.yml
	jar -cf hitb.jar they4kman/ org/ plugin.yml
