# Maven Content Check Plugin

[![TravisCI Status](https://travis-ci.org/pkozelka/contentcheck-maven-plugin.png?branch=master)](https://travis-ci.org/pkozelka/contentcheck-maven-plugin)


Gives you more control over your WAR file's contents:

* detects *arrival of new jars* into `WEB-INF/lib` - dependency changes have non-trivial *transitive impacts*; some commonly used libraries embed others which makes the real content of the class-path quite chaotic
* detects *class conflicts* between contained jars - are often easy to avoid, but rarely detected - and can cause really ugly, mysterious troubles
* helps you identify *license issues*

For more info, see the [project website](http://code.kozelka.net/contentcheck-maven-plugin/index.html) or [usage](src/site/markdown/usage.md).

## Contributing

Any contribution is welcome. You can help by

* reporting [issues](https://github.com/pkozelka/contentcheck-maven-plugin/issues) of any kind
* proofreading the texts
* contributing patches, ideally by forking this repo and posting pull-request
