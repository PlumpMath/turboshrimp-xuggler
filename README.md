# turboshrimp-xuggler

[![Build Status](https://travis-ci.org/wiseman/turboshrimp-xuggler.svg)](https://travis-ci.org/wiseman/turboshrimp-xuggler)

This is an AR.Drone video decoder for
[turboshrimp](https://github.com/wiseman/turboshrimp) that uses
[xuggler](http://www.xuggle.com/xuggler).

## Usage

```
(require '[com.lemondronor.turboshrimp.xuggler :as video])

(def decoder (video/decoder))
(decoder <byte array containing video frame>)
```

## License

Copyright © 2014, 2015 John Wiseman

Distributed under the MIT license.  See the accompanying LICENSE file.
