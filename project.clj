(defproject com.lemondronor/turboshrimp-xuggler "0.0.1-SNAPSHOT"
  :description (str "An AR.Drone video decoder for the turboshrimp library "
                    "that uses the xuggler H.264 decoder.")
  :url "https://github.com/wiseman/turboshrimp-xuggler"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :deploy-repositories [["releases" :clojars]]
  :repositories [["xuggle"
                  {:url "http://xuggle.googlecode.com/svn/trunk/repo/share/java/"
                   :checksum :ignore}]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [xuggle/xuggle-xuggler "5.2"]]
  :profiles {:test
             {:dependencies [[com.lemondronor/turboshrimp "0.3.1"]]
              :resource-paths ["test-resources"]}})
