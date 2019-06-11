# Programming Lab for Sean Chen Piano

This is a page for programming adventures in music and other subjects!

This (unnecessary) SPA is written in [Clojurescript](https://github.com/clojure/clojurescript) with the [Shadow-cljs](http://shadow-cljs.org/) workflow and
- [Reagent](https://github.com/reagent-project/reagent) (React)
- [Re-frame](https://github.com/Day8/re-frame)
- [CLJS bach](https://github.com/ctford/cljs-bach)
- [stylefy](https://github.com/Jarzka/stylefy)
- among others.

## Installation

First make sure shadow-cljs is installed, preferrably globally, as these instructions assume the existence of the command `shadow-cljs` (I'm using yarn):
```
[yarn global add] shadow-cljs
```
While you're at it, also do
```
yarn [install]
```

First build `worker.cljs` for release, since that's not going to change often:
```
shadow-cljs release worker
```

### Development
It's encouraged to start a shadow-cljs if you're going to be running shadow-cljs commands alot, since then you won't need to start a new JVM each time:
```
shadow-cljs start
```
Then, watch the app, which will also make a dev server capable of hot reloading:
```
shadow-cljs watch app
```
Once it's done, go to `localhost:8080`.

### Release
Just run `shadow-cljs release app`!

# Sub-Apps
The subapps are as follows.

## Polyrhythms

This page is a polyrhythmic metronome. It allows you to generate accurate polyrhythms in various tempi. Uses the Web-Audio API and a webworker to schedule the ticks.
Visit [this blogpost](https://www.seanchenpiano.com/pianonotes/2019/06/07/polyrhythms-and-introducing-labs-seanchenpiano-com) to see details about what this is, why, and how.

### TODO
* Either put a message about landscape being ideal, or make a vertical version
* Volume/Off controls (individual?)
* Multiple tracks?
