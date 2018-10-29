[![Build Status](https://travis-ci.org/alanktwong/noobchain.svg?branch=master)](https://travis-ci.org/alanktwong/noobchain)
[![SonarCloud Status](https://sonarcloud.io/api/project_badges/measure?project=alanktwong_noobchain&metric=alert_status)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# noobchain
This application was generated using JHipster 4.6.2, you can find documentation and help at [https://jhipster.github.io/documentation-archive/v4.6.2](https://jhipster.github.io/documentation-archive/v4.6.2).

The purpose of this application is to expose in a JHipster monolith, a blockchain prototype

* [Creating Your First Blockchain with Java. Part 1](https://medium.com/programmers-blockchain/create-simple-blockchain-java-tutorial-from-scratch-6eeed3cb03fa) or [its github](https://github.com/CryptoKass/NoobChain-Tutorial-Part-1)
* [Creating Your First Blockchain with Java. Part 2 — Transactions](https://medium.com/programmers-blockchain/creating-your-first-blockchain-with-java-part-2-transactions-2cdac335e0ce) or [its github](https://github.com/CryptoKass/NoobChain-Tutorial-Part-1) or [its github](https://github.com/CryptoKass/NoobChain-Tutorial-Part-2)

## Domain model

* Block chain is a linked list with blocks pointing to their predecesors
    + Each block can receive many transactions
* Each transaction
    + has the public key of its sender
    + has the public key of its recipient
    + has many transaction inputs
        - Each transaction input has an unspent transaction output
    + has many transaction outputs
        - Each transaction output has the public key of its recipient
        - Each transaction output belongs to a transaction
* Each wallet can participate in a transaction
    + Has a private key
    + Has a public key
        - Owns unspent transaction outputs

## Development

Before you can build this project, you must install and configure the following dependencies on your machine:

1. [Node.js][]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.
2. [Yarn][]: We use Yarn to manage Node dependencies.
   Depending on your system, you can install Yarn either from source or as a pre-packaged bundle.

After installing Node, you should be able to run the following command to install development tools.
You will only need to run this command when dependencies change in [package.json](package.json).

    yarn install

We use yarn scripts and [Webpack][] as our build system.


Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

    ./gradlew
    yarn start

[Yarn][] is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in [package.json](package.json). You can also run `yarn update` and `yarn install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `yarn help update`.

The `yarn run` command will list all of the scripts available to run for this project.

### Service workers

Service workers are commented by default, to enable them please uncomment the following code.

* The service worker registering script in index.html
```
<script>
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker
        .register('./sw.js')
        .then(function() { console.log('Service Worker Registered'); });
    }
</script>
```
* The copy file option in webpack-common.js
```js
{ from: './src/main/webapp/sw.js', to: 'sw.js' },
```
Note: Add the respective scripts/assets in `sw.js` that is needed to be cached.

### Managing dependencies

For example, to add [Leaflet][] library as a runtime dependency of your application, you would run following command:

    yarn add --exact leaflet

To benefit from TypeScript type definitions from [DefinitelyTyped][] repository in development, you would run following command:

    yarn add --dev --exact @types/leaflet

Then you would import the JS and CSS files specified in library's installation instructions so that [Webpack][] knows about them:

Edit [src/main/webapp/app/vendor.ts](src/main/webapp/app/vendor.ts) file:
~~~
import 'leaflet/dist/leaflet.js';
~~~

Edit [src/main/webapp/content/css/vendor.css](src/main/webapp/content/css/vendor.css) file:
~~~
@import '~leaflet/dist/leaflet.css';
~~~

Note: there are still few other things remaining to do for Leaflet that we won't detail here.

For further instructions on how to develop with JHipster, have a look at [Using JHipster in development][].

### Using angular-cli

You can also use [Angular CLI][] to generate some custom client code.

For example, the following command:

    ng generate component my-component

will generate few files:

    create src/main/webapp/app/my-component/my-component.component.html
    create src/main/webapp/app/my-component/my-component.component.ts
    update src/main/webapp/app/app.module.ts

## Building for production

To optimize the noobchain application for production, run:

    ./gradlew -Pprod clean bootRepackage

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

    java -jar build/libs/*.war

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

Refer to [Using JHipster in production][] for more details.

## Testing

To launch your application's tests, run:

    ./gradlew test

### Client tests

Unit tests are run by [Karma][] and written with [Jasmine][]. They're located in [src/test/javascript/](src/test/javascript/) and can be run with:

    yarn test



For more information, refer to the [Running tests page][].

## Using Docker to simplify development (optional)

You can use Docker to improve your JHipster development experience. A number of docker-compose configuration are available in the [src/main/docker](src/main/docker) folder to launch required third party services.
For example, to start a mysql database in a docker container, run:

    docker-compose -f src/main/docker/mysql.yml up -d

To stop it and remove the container, run:

    docker-compose -f src/main/docker/mysql.yml down

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a docker image of your app by running:

    ./gradlew bootRepackage -Pprod buildDocker

Then run:

    docker-compose -f src/main/docker/app.yml up -d

For more information refer to [Using Docker and Docker-Compose][], this page also contains information on the docker-compose sub-generator (`jhipster docker-compose`), which is able to generate docker configurations for one or several JHipster applications.

## Continuous Integration (optional)

To configure CI for your project, run the ci-cd sub-generator (`jhipster ci-cd`), this will let you generate configuration files for a number of Continuous Integration systems. Consult the [Setting up Continuous Integration][] page for more information.

We're using the following CI tools, after following [this blog post](https://sivalabs.in/2018/01/ci-cd-springboot-applications-using-travis-ci/):

* [Travis CI](https://travis-ci.org/). The Travis gem works with Ruby 2.3.3
* [Sonar Cloud](https://sonarcloud.io). Also see [Sonar Qube Scanner for Gradle](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Gradle)


[JHipster Homepage and latest documentation]: https://jhipster.github.io
[JHipster 4.6.2 archive]: https://jhipster.github.io/documentation-archive/v4.6.2

[Using JHipster in development]: https://jhipster.github.io/documentation-archive/v4.6.2/development/
[Using Docker and Docker-Compose]: https://jhipster.github.io/documentation-archive/v4.6.2/docker-compose
[Using JHipster in production]: https://jhipster.github.io/documentation-archive/v4.6.2/production/
[Running tests page]: https://jhipster.github.io/documentation-archive/v4.6.2/running-tests/
[Setting up Continuous Integration]: https://jhipster.github.io/documentation-archive/v4.6.2/setting-up-ci/


[Node.js]: https://nodejs.org/
[Yarn]: https://yarnpkg.org/
[Webpack]: https://webpack.github.io/
[Angular CLI]: https://cli.angular.io/
[BrowserSync]: http://www.browsersync.io/
[Karma]: http://karma-runner.github.io/
[Jasmine]: http://jasmine.github.io/2.0/introduction.html
[Protractor]: https://angular.github.io/protractor/
[Leaflet]: http://leafletjs.com/
[DefinitelyTyped]: http://definitelytyped.org/
