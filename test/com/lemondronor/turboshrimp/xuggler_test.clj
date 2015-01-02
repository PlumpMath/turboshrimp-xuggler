(ns com.lemondronor.turboshrimp.xuggler-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [com.lemondronor.turboshrimp.pave :as pave]
            [com.lemondronor.turboshrimp.xuggler :as xuggler])
  (:import [java.awt.image BufferedImage]))


(deftest xuggler-test
  (testing "Xuggler decoder"
    (let [decoder (xuggler/decoder)
          frame (-> "1-frame.pave"
                    io/resource
                    io/input-stream
                    pave/read-frame)
          image (decoder frame)]
      (is (instance? BufferedImage image)))))
