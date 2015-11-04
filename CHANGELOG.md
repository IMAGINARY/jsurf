# Change log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased][unreleased]

- added long options --help and --version

## [0.1.4] - 2015-09-07
### Changed
- removed unintended executable privilege of some .jsurf example files

### Fixed
- fixed division of univariate polynomials by constants

### Added
- change log

## [0.1.3] - 2015-03-03
### Added
- unit tests via JUnit

### Changed
- ignore Eclipse build files via .gitignore

### Fixed
- avoid side effects of multivariate polynomial addition (which sometimes changed
  the internal representation of variables like X, Y and Z)

## [0.1.2] - 2014-07-03
### Changed
- switched to non-GPL licensed implementation of the javax.vecmath API
- switched gradle plugin 'maven' to 'maven-publish'
- updated to Gradle 1.12
- ignore Eclipse project files via .gitignore

## [0.1.1] - 2014-02-07
### Added
- .gitignore
- .jsurf examples files and demo for gradle 'run' task

### Fixed
- properly exit after saving PNG image

## [0.1.0] - 2013-06-13
### Added
- basic setup for Maven deployment to GitHub
- import code from internal code repository of IMAGINARY

### Changed
- improved scheduling of parallel rendering tasks

[unreleased]: https://github.com/IMAGINARY/jsurf/compare/v0.1.4...HEAD
[0.1.4]: https://github.com/IMAGINARY/jsurf/compare/v0.1.3...v0.1.4
[0.1.3]: https://github.com/IMAGINARY/jsurf/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/IMAGINARY/jsurf/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/IMAGINARY/jsurf/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/IMAGINARY/jsurf/compare/v0.0.0...v0.1.0
