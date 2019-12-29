# StaticSite

Static site generator using a common sense DSL

## TODO

* ~~Create DSL parser that outputs HTML~~
* ~~Update personal site to use better CSS/JS (more linked, less inline)~~
* Create file watcher service that can watch directories and filter on file types
* Create web server that will serve up HTML and any required assets
* Wire it all up together
    * Any changes to DSL file generates new HTML file
    * Any changes to linked assets (CSS, JS) generates new compressed files
    * Localhost automatically refreshes page
