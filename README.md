Fix Virtual Host App
==========
[![Build Status](https://travis-ci.org/tmoreira2020/fix-virtual-host-app.svg?branch=master)](https://travis-ci.org/tmoreira2020/fix-virtual-host-app)
[![Coverage Status](https://coveralls.io/repos/tmoreira2020/fix-virtual-host-app/badge.svg?branch=master)](https://coveralls.io/r/tmoreira2020/fix-virtual-host-app?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/br.com.thiagomoreira.liferay.plugins.fix-virtual-host-app/fix-virtual-host-app/badge.svg)](https://maven-badges.herokuapp.com/maven-central/br.com.thiagomoreira.liferay.plugins.fix-virtual-host-app/fix-virtual-host-app)

This app acts in the startup time of portal and help to fix the virtual host of Liferay. Given an existent dump  that points to a domain, for instance www.myliferay.com this hook will "fix" it to point to a dev/uat/test environment for instance: dev.myliferay.com. It does this based on the value of environment variable called LIFERAY_VIRTUAL_HOST.

### License

Fix Virtual Host App is licensed under [Apache 2](http://www.apache.org/licenses/LICENSE-2.0) license.

### Maven/Gradle

Fix Virtual Host App is available on Maven central, the artifact is as follows:

Maven:

```xml
<dependency>
    <groupId>br.com.thiagomoreira.liferay.plugins.fix-virtual-host-app</groupId>
    <artifactId>fix-virtual-host-app</artifactId>
    <version>1.0.0</version>
    <type>lpkg</type>
</dependency>
```
Gradle:

```groovy
dependencies {
    compile(group: "br.com.thiagomoreira.liferay.plugins.fix-virtual-host-app", name: "fix-virtual-host-app", version: "1.0.0", type: "lpkg");
}
```
### Support
Fix Virtual Host App tracks [bugs and feature requests](https://github.com/tmoreira2020/fix-virtual-host-app/issues) with Github's issue system. Feel free to open your [new ticket](https://github.com/tmoreira2020/fix-virtual-host-app/issues/new)!

### Contributing

Fix Virtual Host App is a project based on Maven to improve it you just need to fork the repository, clone it and from the command line invoke

```shell
mvn package
```
After complete your work you can send a pull request to incorporate the modifications.

Enjoy!
