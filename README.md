# Pattern Lab For Sling

![Pattern Lab For Sling](https://raw.githubusercontent.com/kciecierski/patternlab-for-sling/master/img/pattern-lab-for-sling.jpg)

## Introducing atomic design to Sling

### Atomic design

_Atomic design_ is a clear methodology for crafting design systems.

Atomic design gives us the ability to traverse from abstract to concrete.
Because of this, we can create systems that promote consistency and scalability while simultaneously showing things in their final context.
And by assembling rather than deconstructing, we’re crafting a system right out of the gate instead of cherry picking patterns after the fact.


Clients and team members are able to better appreciate the concept of design systems by actually seeing the steps laid out in front of them.
In a lot of ways, this is how we’ve been doing things all along, even if we haven’t been consciously thinking about it in this specific way.
[[Brad Frost]( http://bradfrost.com/blog/post/atomic-web-design/)]

### Patterns
A visual _design system_ is built out of the core components of typography, layout, shape or form, and colour. [[Laura Kalbag](https://24ways.org/2012/design-systems/)]

Atomic design distinguishes five distinct levels, each of them represented by different type of Patterns:

![Pattern Lab For Sling - patterns](https://raw.githubusercontent.com/kciecierski/patternlab-for-sling/master/img/atomic-web-design.gif)

UI Patterns can be included inside each other like Russian nesting dolls.
Because of that, applying atomic design to your application allows to maximise reusing of HTML markup, reduce technical debt avoiding code duplication and accelerate implementation of new features.

### Pattern Lab

__Pattern Lab__ tool allows to adapt atomic design in development practice by giving ability to preview and test the nested Patterns.
[The original implementation](http://patternlab.io/) is a static site generator (powered by either PHP or Node) that stitches together UI components.

__Pattern Lab For Sling__ main purpose is to support prototyping the site with following atomic design straight on your instance of Apache Sling or its implementation which the most known is [Adobe AEM](http://www.adobe.com/marketing-cloud/enterprise-content-management.html).
The benefit coming from prototyping straight in your web application is possibility to utilize your Patterns for final implementation by integrating them the proper data from backend.

The tool allows also to utilize great Pattern Lab features for prototyping and testing your Patterns with your Sling applications based on with HTL language.
As Sling based application, it also you ability to use global variables to access current scope data, like current page path with _${currentPage.path}_,
or to add already implemented logic in your application or include already implemented files and templates. Additionally, as HTL language allows to define unlimited number of templates in one HTML file,
Pattern Lab For Sling allows to present the same data set with all templates defined in one file.


## Features
### Preview Patterns with different CSS, JS and data sets

The main feature of Pattern Lab is ability to preview implemented HTML files and HTL templates with configured set of CSS, JS and content data.

Each of the pattern is rendered on separate page with configured CSS and JS files that which are embedded in displayed on Pattern Lab Page.

For HTL templates it is also possible to provide example set of data, without need to implement authoring part of your application, by defining proper JSON formatted files.
The file data needs to placed in the HTML file's folder keep proper naming convention to be utilized to generate proper Pattern.

As an example, let's consider _/atoms/buttons/button.html_ file containing definitions two HTML templates: _simplebutton_ and _textbutton_.

Adding data files below will cause generation of proper Patterns for each of the template:

* /atoms/buttons/button.js
* /atoms/buttons/button.1.js
* /atoms/buttons/button.homepage-example.js
* /atoms/buttons/button.in-footer.js

By adding template name to data file, it is possible to specify template specific content data. The files below causes generation of Patterns for _simplebutton_ template only:

* /atoms/buttons/button.simplebutton.js
* /atoms/buttons/button.simplebutton.1.js
* /atoms/buttons/button.simplebutton.homepage-example.js
* /atoms/buttons/button.simplebutton.in-footer.js

The content of data files is simple JSON with parameters that should be passed into template:

    {"btnClass" : "", "btnLink" : "#", "btnText" : "Button"}

The parameters can be also nested:

    {
        "link" : "#",
        "number" : "75%",
        "headline" : "Lorem ipsum dolor sit (37 characters)",
        "progress" : {
            "max" : "100",
            "progressValue" : "75",
            "label" : "75%"
        }
    }


Base on the file structure of Patterns in application, Pattern Lab For Sling is building proper hierarchical menu:

![Pattern Lab For Sling - structure](https://raw.githubusercontent.com/kciecierski/patternlab-for-sling/master/img/structure.jpg)

### Grouping and displaying subset of Patterns

Pattern Lab allows you to create library of your Patterns structured in the the most convenient way for your project,
by reflecting proper the structure of your application. Base on this structure each of the Pattern is associated with proper id.
Patterns can be filtered by using their ids or the proper id's prefix and __pattern__ Sling selector in url to Pattern Lab resource:

    http://<sling.host>:<sling.port>/etc/pattern-lab/pattern-lab-demo.pattern.atoms-buttons.html

### Displaying "raw" Pattern

This feature allows you to access Pattern HTML and display it in your browser directly, by using __raw__ Sling selector your URL:

    http://<sling.host>:<sling.port>/etc/pattern-lab/pattern-lab-demo.pattern.atoms-buttons-text-button.raw.html

The direct links to Patterns can be utilized by any visual testing applications to set up continuous integration workflow detecting regression issues or changes appearing during patterns development and application releases.

### Pattern Responsive design validation

This is the client side functionality coming from original Pattern Lab, allowing you to control width of the page or select one of the view port modes:
* _S_ - adapts Patterns viewport to random small width used on mobile devices
* _M_ - adapts Patterns viewport to random small width used mainly on tablet devices
* _L_ - adapts Patterns viewport to random small width used mainly by desktop browsers
* _FULL_ - adapts Patterns viewport to current browser width
* _RANDOM_ - adapts Patterns viewport to any random width
* _DISCO_ - presents Patterns continuously and randomly in different view ports

## Features in development
* previewing HTL templates and HTML as per original Pattern Lab
* searching patterns as per original Pattern Lab
* [AEM addon](https://github.com/kciecierski/patternlab-for-sling-aem-addon)

## Demo

Based on https://github.com/kciecierski/patternlab-for-sling-demo

Link to live demo page to be added.

## Installation

### Requirements

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Sling 8](http://sling.apache.org/downloads.cgi) or version with Sling Models >= 1.1.0 (should be compatible with AEM >=6.1)
* [Maven 3+](http://maven.apache.org/download.cgi)

### 1. Run Sling

    java -jar org.apache.sling.launchpad-8.jar

By default, it is running on port 8080, you can change it with -p parameter:

    java -jar org.apache.sling.launchpad-8.jar -p <port>

### 2. Check out and install Pattern Lab For Sling

    mvn clean install -PautoInstallBundle


Optionally, you can also override default connection parameters:

    mvn clean install -PautoInstallBundle -Dsling.host=<host> -Dsling.port=<port> -Dsling.user=<user>  -Dsling.password=<password>

### 3. Configure and install your Sling application with HTL templates and Pattern Lab resource

Example for resource file:

    /etc/pattern-lab/pattern-lab-demo.json:

and resource value:

    {
        "sling:resourceType" : "patternlab/components/page",
        "appsPath" : "/apps/patternlab-demo", - root path for HTL templates
        "headerCss" : ["/etc/designs/patternlab-demo/clientlib/css/style.css", ..], // array of links to CSS files that should be used in header of Pattern
        "headerJs" : ["", ..], // array of links to JS files that should be used in header of Pattern
        "footerCss" : ["", ..], // array of links to CSS files that should be used in footer of Pattern
        "footerJs" : ["", ..]  // array of links to JS files that should be used in footer of Pattern
    }

it can be accessed with link:

    http://<sling.host>:<sling.port>/etc/pattern-lab/pattern-lab-demo.html

You can find example of Sling application under in this repository: https://github.com/kciecierski/patternlab-for-sling-demo
