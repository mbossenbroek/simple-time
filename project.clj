(defproject simple-time "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [joda-time "2.3"]]
  :plugins [[codox "0.6.6"]]
  :codox {:include [simple-time.core]
          :src-dir-uri "https://TODO"
          :src-linenum-anchor-prefix "L"})
