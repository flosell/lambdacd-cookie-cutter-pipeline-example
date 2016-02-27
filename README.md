# cookie-cutter-pipeline-example

An example on how to use parameterized pipeline templates to generate several pipelines with the same structure. 
This can be useful if you have several projects that all need basically the same pipeline but have a few minor variations, 
e.g. different repositories and differently named build scripts. 

This example can also be your introduction on using functions to generate pipelines in general. 

**Note: Some features presented here require LambdaCD 0.7.0**

## Usage

* `lein run` will start your pipeline with a web-ui listening on port 8080

## Files

* `pipeline.clj` contains your pipeline-definition
* `steps.clj` contains your custom build-steps
