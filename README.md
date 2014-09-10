# jscs plugin #

[jscs](https://github.com/jscs-dev/node-jscs) JSCS — JavaScript Code Style.
jscs is a code style checker, see more here [here](https://github.com/jscs-dev/node-jscs).<br/>
jscs plugin for WebStorm, PHPStorm and other Idea family IDE with Javascript plugin, provides integration with jscs and shows errors and warnings inside the editor.
* Support displaying jscs warnings as intellij inspections

## Getting started ##
### Prerequisites ###
If you do not have nodejs installed on your machine, download and install [NodeJS](http://nodejs.org/).<br/>

Install jscs npm package [jscs npm](https://www.npmjs.org/package/jscs)</a>:<br/>
```bash
$ cd <project path>
$ npm install jscs
```
Or, install jscs globally:<br/>
```bash
$ npm install -g jscs
```

### Settings ###
To get started, you need to set the jscs plugin settings:<br/>

* Go to preferences, jscs plugin page and check the Enable plugin.
* Set the path to the nodejs interpreter bin file.
* Select whether to let jscs search for ```.jscsrc``` file
* Set the path to the jscs bin file. should point to ```<project path>node_modules/jscs/bin/jscs.js``` if you installed locally or ```/usr/local/bin/jscs``` if you installed globally.
  * For Windows: install jscs globally and point to the jscs cmd file like, e.g. ```C:\Users\<username>\AppData\Roaming\npm\jscs.cmd```
* Set the ```.jscsrc``` file, or jscs will use the default settings.
* You can also set a path to a custom rules directory.
* By default, jscs plugin annotate the editor with warning or error based on the jscs configuration, you can check the 'Treat all jscs issues as warnings' checkbox to display all issues from jscs as warnings.

Configuration:<br/>
![jscs config](https://raw.githubusercontent.com/idok/jscs-plugin/master/doc/config.png)


Inspection:<br/>
![jscs inline](https://raw.githubusercontent.com/idok/jscs-plugin/master/doc/inspect-inline.png)


Analyze Code:<br/>
![jscs inline](https://raw.githubusercontent.com/idok/jscs-plugin/master/doc/inspect.png)

### A Note to contributors ###
jscs plugin uses the code from [here](https://github.com/idok/scss-lint-plugin/tree/master/intellij-common) as a module, to run the project you need to clone that project as well.
