# barcode-validator

The Barcode Validator is an open source project comissioned and developed in collaboration with the [Consortium for the Barcode of Life](http://www.barcodeoflife.org) (CBOL) at the Smithsonian Institution.

It provides tools to assess the quality of sequencing data for the purposes of validation before submission to GenBank as a barcode record.  

## Requirements
Java Development Kit 1.6+

## Installation
Make sure that you are in the *develop* branch. The build system included in master is not up to date with some dependency changes and will fail. This will be fixed when the next release is made and the updated build system is merged into master.

There are two build systems available at the moment.  Future builds of this project will exclusively use Gradle.  Apache Ant + Apache Ivy is still avaialble for those transitioning.

### Using Gradle
From the root folder run the following command:

    gradlew createPlugin

This will create the plugin in researchTool/build/distributions/

### Using Apache Ant + Apache Ivy
The default build process uses Apache Ivy.

    ant create-plugin

This will create the plugin in researchTool/build


## Development 
### Branches
The development of this project follows the [Gitflow branching strategy](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).  All development is done in the develop branch and merged to master when complete.  Thus master only contains released code.

When switching branches it is always a good idea to run a build with

    gradlew build

This will ensure any dependency changes for the new branch are applied and everything compiles.

### Modules
The project currently contains two modules:

* Validation - A pipeline that runs on sequencing data and generates a report listing statistics on the quality of the data.
* Research Tool - A Geneious plugin that provides a user interface for running the validation pipeline in batch for the purposes of identifying ideal thresholds for quality metrics.  This module depends on the Validation module.


## Releases
See https://github.com/Biomatters/barcode-validator/releases

## Contributing
Report bugs via [https://github.com/Biomatters/barcode-validator/issues](GitHub issues)
