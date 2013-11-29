all:
	rsync -azi --delete --exclude Makefile --exclude .git ../contentcheck-maven-plugin/target/site/ ./