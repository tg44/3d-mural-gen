# 3d-mural-gen

This repository is build for various 3d-printing model generation automation.

At the moment it has 2 packages containing classes to help model writing and rendering, and has 2 package with actual "thing" generators.

This project is build with the scala language. 
If you are not familiar with the language, I'm sadly admit that the code-quality will not help you to understand things.

## Utility code

### OpenScad

This package is mostly openscad related code. 
It has an abstract scala layer to work with openScad native objects.
Basically this is a big code generator and language definition layer.

With these helpers you can generate the raw scad file, the stl, or even pop up a viewer as you like.

(For model generation, you will need to have the openscad command configured and available as a commandline program too.
For linux it is done if you installed it.
For mac and windows pls [read the docs](https://en.wikibooks.org/wiki/OpenSCAD_User_Manual/Using_OpenSCAD_in_a_command_line_environment#Windows_notes))

Most of the code in this package inspired by the [dzufferey/scadla](https://github.com/dzufferey/scadla) library.

### Pipeline

This package contains the machinery of parallel multi node execution.
It has a webserver which can queue up tasks, and it has consumers whoes can consume and solve them and upload the result (stl) to S3.

This could be seemed a little overkill, but when one model is ten minutes, and you want to render ~200 of them it can be really handy!

The pipeline is `Api->RabbitMQ->consumers->S3` where all the started applications are both have the api and the consumer role.


## Model code

### Cellpattern

Inspired by [this Conway's World](https://www.thingiverse.com/thing:2814299) project.

You need to add the initial cells, and the generation number, and this will generate an (in theory) printable tower.

### Mural

Inspired by [the 3DMNmural project](https://twitter.com/3dmakernoob/status/1155475519196016640) and [Cha Yong Rye](http://jongrye.com/sculptures/).

The main idea is to generate OpenLockable Hexa tiles based on a height-map.

The output is similar [to this](https://drive.google.com/open?id=1rk2NyYOc_m-v5FOpxUEixLS5qOZHiTYx).

(I started to build the pipeline when I realized that the GrandCanyon is about 4h of computation on 8 threads, and if I ever want this to grow as a service for others; 4h is a really long time for a single request.)

## Local testing, building, deploying

We have a docker-compose file which brings up a rabbitMq and a Minio instance locally. Both are configured for easy local developement. (RabbitMQ management UI on `localhost:15672` Minio UI on `localhost:9001`.)

We also have a Dockerfile which builds a ~minimal image after compiled the code. The created image has a build in openscad so it can work instantly after deployed and configured properly.

Which we don't have currently: config from env-vars. It's on my todo list implicitly. 

## Future

My todo list:
 1. I want to try this out on (aws) spot instances to test if it really scales well or not.
 1. I want to make a "user friendly" UI for the cellpattern generator and the mural generator too.
 1. I want to try out if the generated cell-pattern is actually printable or not :D (The mural is working!)
 1. I want to make the mural job to be parametrized with a cutting shape (so you can cut out countries, islands or continents).
 1. I want to implement other generators just for fun.
 1. If this can be a thing, I want to host a version, with autoscaling aws instances!

## Interested?

If you are interested in the project, or has ideas pls write an issue, if sb wants to understand the code, I can help there (or maybe document/clean it a bit more, start an official discord server for it etc.).

If you are coming with openscad + basic high-level programing background I think the cellpattern is a good starting point (not the Generator but the other classes). (If you are lost in the language use the "click into" or the `scala what is` googlesearch methods.)

Every idea is welcome!
