# Maven Content Check Plugin

[![TravisCI Status](https://travis-ci.org/pkozelka/contentcheck-maven-plugin.png?branch=master)](https://travis-ci.org/pkozelka/contentcheck-maven-plugin)

[![Codeship Status](https://codeship.com/projects/b3d55ae0-f6b0-0132-cdf6-4e1088c82680/status?branch=master)](https://codeship.com/projects/86152)


Protects your WAR against

* uncontrolled *arrival of new jars* into WEB-INF/lib - which happens easily in the Maven's world of *transitive dependencies*
* *class conflicts* between contained jars

For more info, see the [project website](http://code.kozelka.net/contentcheck-maven-plugin/index.html).

## Contributing

Any contribution is welcome. You can help by

* reporting [issues](https://github.com/pkozelka/contentcheck-maven-plugin/issues) of any kind
* proofreading the texts
* contributing patches, ideally by forking this repo and posting pull-request
