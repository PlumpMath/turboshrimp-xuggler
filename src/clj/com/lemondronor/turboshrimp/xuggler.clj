(ns com.lemondronor.turboshrimp.xuggler
  "This is an AR.Drone video decoder that uses the xuggler library."
  (:import [java.io InputStream]
           [java.nio ByteBuffer]
           [com.xuggle.ferry IBuffer]
           [com.xuggle.xuggler ICodec$ID IPacket IPixelFormat IPixelFormat$Type
            IRational IStreamCoder IStreamCoder$Direction IVideoPicture]))


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

(defn decode-frame [coder frame]
  (let [^bytes ba (:payload frame)
        ^ByteBuffer bb (ByteBuffer/wrap ba)
        ^IBuffer buffer (IBuffer/make nil (.capacity bb))
        ^IPacket packet (IPacket/make buffer)]
    (.put (.getByteBuffer packet) (.array bb))
    (if (.isComplete packet)
      (if (= (.getStreamIndex packet) 0)
        (let [^IVideoPicture video-picture (IVideoPicture/make
                                            IPixelFormat$Type/YUV420P
                                            640
                                            360)]
          (.decodeVideo coder video-picture packet 0))))))


(defn decoder []
  (let [coder (doto (IStreamCoder/make
                     IStreamCoder$Direction/DECODING
                     ICodec$ID/CODEC_ID_H264)
                (.setNumPicturesInGroupOfPictures 5)
                (.setBitRate 2999240)
                (.setBitRateTolerance 4000000)
                (.setPixelType IPixelFormat$Type/YUV420P)
                (.setHeight 360)
                (.setWidth 640)
                (.setGlobalQuality 0)
                (.setFrameRate (IRational/make 25 1)))]
    (println coder)
    (when (< (.open coder nil nil) 0)
      (throw (ex-info "Error opening coder" {})))))
