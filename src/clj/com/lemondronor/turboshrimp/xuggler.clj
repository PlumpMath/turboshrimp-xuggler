(ns com.lemondronor.turboshrimp.xuggler
  "This is an AR.Drone video decoder that uses the xuggler library."
  (:import [java.io InputStream]
           [java.nio ByteBuffer]
           [com.xuggle.ferry IBuffer]
           [com.xuggle.xuggler ICodec$ID IPacket IPixelFormat IPixelFormat$Type
            IRational IStreamCoder IStreamCoder$Direction IStreamCoder$Flags
            IVideoPicture]
           [com.xuggle.xuggler.video ConverterFactory ConverterFactory$Type
            IConverter]))


;; (defn payload-input-stream [frame-queue]
;;   (let [payload (atom nil)
;;         offset (atom 0)]
;;     (proxy [InputStream] []
;;       (read []
;;         (let [p @payload]
;;           (when (and p (> @offset (count p)))
;;             (reset! offset 0)
;;             (reset! payload (:payload (pave/pull-frame frame-queue))))
;;           (if @p
;;             (let [b (aget @payload @offset)]
;;               (swap! offset inc)
;;               b)
;;             -1))))))


(defrecord Decoder [^IStreamCoder video-stream-coder])


(defn decode-frame [^IStreamCoder coder ^bytes frame-data]
  (let [^ByteBuffer bb (ByteBuffer/wrap frame-data)
        ^IBuffer buffer (IBuffer/make nil (.capacity bb))
        ^IPacket packet (IPacket/make buffer)]
    (.put (.getByteBuffer packet) (.array bb))
    (if (.isComplete packet)
      (if (= (.getStreamIndex packet) 0)
        (let [^IVideoPicture video-picture (IVideoPicture/make
                                            IPixelFormat$Type/YUV420P
                                            640
                                            360)]
          (.decodeVideo coder video-picture packet 0)
          (when (.isComplete video-picture)
            (let [^ConverterFactory$Type type
                  (ConverterFactory/findRegisteredConverter
                   ConverterFactory/XUGGLER_BGR_24)
                  ^IConverter converter
                  (ConverterFactory/createConverter
                   (.getDescriptor type)
                   video-picture)
                  image (.toImage converter video-picture)]
              image)))))))


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
                (.setAutomaticallyStampPacketsForStream true))]
    (when (< (.open coder nil nil) 0)
      (throw (ex-info "Error opening coder" {})))
    (fn [frame]
      (decode-frame coder (:payload frame)))))
