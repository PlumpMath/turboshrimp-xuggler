(ns com.lemondronor.turboshrimp.xuggler-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [com.lemondronor.turboshrimp.pave :as pave]
            [com.lemondronor.turboshrimp.xuggler :as xuggler]))

(deftest xuggler-test
  (testing "Whatever"
    (let [decoder (xuggler/decoder)
          frame (-> "1-frame.pave"
                    io/resource
                    io/input-stream
                    pave/read-frame)
          img (decoder frame)]
      (println img))))
