# cookie-cutter-pipeline-example

An example on how to use parameterized pipeline templates to generate several pipelines with the same structure. 
This can be useful if you have several projects that all need basically the same pipeline but have a few minor variations, 
e.g. different repositories and differently named build scripts. 

This example can also be your introduction on using functions to generate pipelines in general. 

**Note: This example relies on features currently in development.**
If you still want to try it out, you need to build LambdaCD from the current master. 
To install the latest code into your local maven repository: 

* `git clone git@github.com:flosell/lambdacd.git`
* `cd lambdacd`
* `./go release-local`

## Usage

* `lein run` will start your pipeline with a web-ui listening on port 8080

## Files

* `pipeline.clj` contains your pipeline-definition
* `steps.clj` contains your custom build-steps
