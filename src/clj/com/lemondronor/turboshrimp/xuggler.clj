(ns com.lemondronor.turboshrimp.xuggler
  "This is an AR.Drone video decoder that uses the xuggler library to
  decode H.264."
  (:import [java.io InputStream]
           [java.nio ByteBuffer]
           [com.xuggle.ferry IBuffer]
           [com.xuggle.xuggler ICodec$ID IPacket IPixelFormat IPixelFormat$Type
            IRational IStreamCoder IStreamCoder$Direction IStreamCoder$Flags
            IVideoPicture]
           [com.xuggle.xuggler.video ConverterFactory ConverterFactory$Type
            IConverter]))


(defn decode-frame [^IStreamCoder coder ^IConverter converter
                    ^IVideoPicture video-picture frame]
  (let [^bytes frame-data (:payload frame)
        ^ByteBuffer bb (ByteBuffer/wrap frame-data)
        ^IBuffer buffer (IBuffer/make nil (.capacity bb))
        ^IPacket packet (IPacket/make buffer)]
    (.put (.getByteBuffer packet) (.array bb))
    (if (and (.isComplete packet)
             (= (.getStreamIndex packet) 0))
      (do
        (.decodeVideo coder video-picture packet 0)
        (if (.isComplete video-picture)
          (.toImage converter video-picture)
          nil))
      nil)))


(defn decoder []
  (let [coder (doto (IStreamCoder/make
                     IStreamCoder$Direction/DECODING
                     ICodec$ID/CODEC_ID_H264)
                (.setNumPicturesInGroupOfPictures 12)
                (.setBitRate 2999240)
                (.setBitRateTolerance 4000000)
                (.setPixelType IPixelFormat$Type/YUV420P)
                (.setHeight 360)
                (.setWidth 640)
                (.setFlag IStreamCoder$Flags/FLAG_QSCALE true)
                (.setGlobalQuality 0)
                (.setFrameRate (IRational/make 25 1))
                (.setTimeBase (IRational/make 1 25))
                (.setAutomaticallyStampPacketsForStream true))
        ^IVideoPicture video-picture (IVideoPicture/make
                                      IPixelFormat$Type/YUV420P
                                      640
                                      360)
        ^ConverterFactory$Type type (ConverterFactory/findRegisteredConverter
                                     ConverterFactory/XUGGLER_BGR_24)
        converter (ConverterFactory/createConverter
                   (.getDescriptor type)
                   video-picture)]
        (when (< (.open coder nil nil) 0)
      (throw (ex-info "Error opening coder" {})))
    (fn [frame]
      (decode-frame coder converter video-picture frame))))
