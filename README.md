# turboshrimp-xuggler

[![Build Status](https://travis-ci.org/wiseman/turboshrimp-xuggler.svg)](https://travis-ci.org/wiseman/turboshrimp-xuggler)

This is an AR.Drone PaVE video decoder for
[turboshrimp](https://github.com/wiseman/turboshrimp) that uses
[xuggler](http://www.xuggle.com/xuggler).

## Usage

```
(require '[com.lemondronor.turboshrimp.xuggler :as video])

(def decoder (video/decoder))
(decoder &lt;<byte array containing video frame&gt;)

```

## Testing

```
lein test
```

A sample PaVE video file is supplied in
`test-resources/video-long.pave`. You can decode and display it with

```
cat test-resources/video-long.pave | lein run -m showvideo --fps 30 --file
```

You can see latency reduction in action with

```
cat test-resources/video-long.pave | lein run -m showvideo --fps 120 --file
```

You should see output like this:

```
06:34:36.905 [main] DEBUG com.lemondronor.turboshrimp.pave - Skipped 7 frames
06:34:37.049 [main] DEBUG com.lemondronor.turboshrimp.pave - Skipped 5 frames
06:34:37.191 [main] DEBUG com.lemondronor.turboshrimp.pave - Skipped 2 frames
[etc.]
```

## License

Copyright © 2014, 2015,2016 John Wiseman

Distributed under the MIT license.  See the accompanying LICENSE file.
