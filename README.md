# Maven Content Check Plugin

[![TravisCI Status](https://travis-ci.org/pkozelka/contentcheck-maven-plugin.png?branch=master)](https://travis-ci.org/pkozelka/contentcheck-maven-plugin)

[![Codeship Status](https://codeship.com/projects/b3d55ae0-f6b0-0132-cdf6-4e1088c82680/status?branch=master)](https://codeship.com/projects/86152)


Gives you more control over your WAR file's contents:

* detects *arrival of new jars* into `WEB-INF/lib` - dependency changes have non-trivial *transitive impacts*
* detects *class conflicts* between contained jars - are often easy to avoid, but rarely detected - and can cause really ugly, mysterious troubles
* helps you identify *license issues*

For more info, see the [project website](http://code.kozelka.net/contentcheck-maven-plugin/index.html).

## Contributing

Any contribution is welcome. You can help by

* reporting [issues](https://github.com/pkozelka/contentcheck-maven-plugin/issues) of any kind
* proofreading the texts
* contributing patches, ideally by forking this repo and posting pull-request
