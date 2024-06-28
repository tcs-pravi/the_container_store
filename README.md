[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=alert_status&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=bugs&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=reliability_rating&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=vulnerabilities&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=security_rating&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=code_smells&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=sqale_rating&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=coverage&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=duplicated_lines_density&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=ncloc&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=the-container-store_preston-integrations-proposal-services&metric=sqale_index&token=98e28dd424396a635fc60e32c52b777f0e3a7126)](https://sonarcloud.io/summary/new_code?id=the-container-store_preston-integrations-proposal-services)

# Preston-Integrations-Proposal-Services

### Running the application
1. Run `./gradlew bootRun` to start the application

1.  The following endpoints come out of the box for free:
    - [About Endpoint](http://localhost:8080/about)
    - [Health Endpoint](http://localhost:8080/health)
    - [Metrics Endpoint](http://localhost:8080/actuator/metrics)
    - [Swagger Endpoint](http://localhost:8080/swagger-ui.html)

### Intellij setup
Ensure that the following plugins are installed
- SonarLint - *for running static code analysis locally*
- Save Actions - *use to apply code styles on save*

### TCS Libraries
The project uses TCS built-in libraries/gradle plugin
1. [Gradle Releaser Plugin](https://github.com/the-container-store/Gradle-Releaser) that provides among other things semantic versioning
that adheres to TCS standard ([Technology Decision](https://github.com/the-container-store/Technical-Architecture/blob/master/adr/0005-version-numbers.md))
1. [Spring Boot Starter](https://github.com/the-container-store/spring-boot-starters) plugin that brings in a
prescribed list of spring and common library dependency management. Project can just define the needed libraries.

## Mutation Testing

Test coverage can be evaluated with `pitest`, which will change application code
and run tests; insufficient coverage means tests still pass despite changed code
so more tests may be needed to cover for those possibilities.

The end result is better test coverage, hopefully for less bugs.

Run `pitest` mutation testing locally with

`./gradlew pitest`

See additional details [at this repo](/the-container-store/Iris-Services).

### How do I use pitest?

`pitest` works with your existing unit tests. Assuming your test suite already
runs green, the tool will automatically mutate your application code and run the
same tests. Test coverage is determined by how many "mutants" are "killed."

Test coverage is evaluated by line coverage %, mutation coverage %, and test
strength.

### Viewing reports

To view reports locally,

`open "$(ls -td build/reports/pitest/* | head -1)/index.html"`

### Excluding classes

To exclude controller tests or repository tests if applicable, add an entry like
this to build.gradle:

```
pitest {
    ...
    excludedTestClasses = [
                              'com.containerstore.iris.controller.*Test',
                              'com.containerstore.iris.repository.*Test'
                          ]
```

### Viewing test coverage report per pull request

A report is also generated from the GitHub Action that runs mutation testing per
pull request. To access this report,

1. Click Actions tab.
2. Click the test-coverage job.
3. Click Summary.
4. Scroll to the bottom to download the report artifact.

After downloading, extract and view the .html file.

### Do I need to run mutation testing locally every time?

Not necessarily. A GitHub Action will trigger for each pull request and run
mutation testing automatically. Then the only change needed would be to add or
update unit tests to ensure test coverage continues to meet thresholds.

### The tests I need to add are low-value and would only create toil for us

Once the GitHub Action is added, the workflow's PITEST_GOALS variable can be
tweaked to meet operational reality versus future quality requirements. The team
can determine an initial threshold and ratchet up over time as appropriate.

We hope by adding mutation testing early enough with sufficiently realistic
thresholds in this template project, we prevent the class of bugs that, but for
some minor logic to handle a NullPointerException or other benign return value,
caused a multitude of cumulative debugging and investigation sessions.

For legacy projects, we hope a gradual increase of the threshold over sprints
helps make "fearless refactoring" a possibility.

### How do we adjust mutation test coverage thresholds?

Modify the PITEST_GOALS comma-separated values in the file

    .github/workflows/test-coverage.yml

Be sure to use `x,y,z` format, without spaces!

The numbers represent line coverage %, mutation coverage %, and test strength %,
respectively.

### Run a mutation test manually

This can be done as well:

1. Click Actions tab.
2. Click the test-coverage job.
3. Click Run Workflow.
4. Select the branch.
5. Click Run Workflow.

Report results are available as a .zip file.

## Troubleshooting

### The /health endpoint fails with JDBC error

Verify you are logged in with `vault login -method=ldap username=$(whoaami)`

The token value is the value of the `id` key in the command `vault token lookup`

Make sure you have a VAULT_TOKEN defined with `export VAULT_TOKEN=token-id`
