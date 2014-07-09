install:
	mvn clean install

site:
	git fetch origin +gh-pages:gh-pages
	rm -rf target/site
	git clone . target/site -b gh-pages --single-branch
	mvn site
	cd target/site && git add -A && git commit -m 'updated site' && git push

site-deploy:
	git push origin gh-pages
