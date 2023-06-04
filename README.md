<p align="center"><img src="https://github.com/ssoper/Zebec/raw/main/gh/zebec.png" alt="Zebec Logo"></p>

[![tests](https://github.com/ssoper/Zebec/actions/workflows/coverage.yml/badge.svg)](https://github.com/ssoper/Zebec/actions/workflows/coverage.yml)
[![codecov](https://codecov.io/gh/ssoper/Zebec/branch/master/graph/badge.svg?token=HU5CYI3X3X)](https://codecov.io/gh/ssoper/Zebec)
[![Download](https://img.shields.io/badge/download-v1.0.1-blue)](https://github.com/ssoper/Zebec/packages/108070)
[![License](https://img.shields.io/github/license/ssoper/Zebec)](https://github.com/ssoper/Zebec/blob/master/LICENSE)

# Zebec

Static site compiler. Written in Kotlin.

## Features

* HTML files compiled from Kotlin-based DSL (KTML) 🔧
* Includes a [minifier](https://yui.github.io/yuicompressor/) for CSS and JavaScript files 🗜
* Watches for changed files that are automatically compiled or minified 👁 
* Ships with a tiny web server to make local development a cinch 💅
* [Downloadable JAR](https://github.com/ssoper/Zebec/packages) that runs as a service 📦

## Roadmap

* ✅ Tests!
    * ✅ Github Actions
    * ✅ Surface test results and code coverage via badges
* Blogging
    * ✅ Markdown
    * ✅ Templating
    * ✅ List entries
    * ✅ [Unsplash](https://source.unsplash.com/)
    * ✅ Support [srcset](http://thenewcode.com/944/Responsive-Images-For-Retina-Using-srcset-and-the-x-Designator)
    * Social Media [tags](https://blog.hubspot.com/marketing/open-graph-tags-facebook-twitter-linkedin)
        * ✅ [LinkedIn](https://www.linkedin.com/help/linkedin/answer/46687/making-your-website-shareable-on-linkedin)
        * ✅ [Twitter](https://developer.twitter.com/en/docs/tweets/optimize-with-cards/guides/getting-started)
        * Add’l [types](https://ogp.me/#types) such as `article:tag`
        * Add support for [Twitter handle](https://developer.twitter.com/en/docs/tweets/optimize-with-cards/guides/getting-started)
    * Filter entries by category
    * Add ability to override published date in metadata
    * Generate feed + links in blog index
      * RSS 2
      * Atom
* Generate site files
    * [robots.txt](https://support.google.com/webmasters/answer/6062596?hl=en&ref_topic=6061961)
    * [sitemap](https://support.google.com/webmasters/answer/183668?hl=en&ref_topic=4581190)
* Generate shields.io badges (examples: w3c validation, link backs, etc.)
* Saved state of compiled files via local database (SQL file)

## Questions?

Try the [troubleshooting](troubleshooting.md) documentation first. You can also [create an issue](https://github.com/ssoper/Zebec/issues). 
