(defproject com.lemondronor/turboshrimp-xuggler "0.0.5-SNAPSHOT"
  :description "An AR.Drone video decoder for the turboshrimp library that uses the xuggler H.264 decoder."
  :url "https://github.com/wiseman/turboshrimp-xuggler"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :deploy-repositories [["releases" :clojars]]
  :repositories [["xuggle"
                  {:url "http://xuggle.googlecode.com/svn/trunk/repo/share/java/"
                   :checksum :ignore}]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [xuggle/xuggle-xuggler "5.2"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :profiles {:dev
             {:dependencies [[com.lemondronor/turboshrimp "0.3.8"]
                             [com.lemonodor/gflags "0.7.3"]
                             [com.lemonodor/xio "0.2.2"]]
              :resource-paths ["test-resources"]
              :source-paths ["examples"]
              :plugins [[lein-cloverage "1.0.2"]]}})
