(defproject open-data "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.novemberain/monger "3.0.0-rc2"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.18"]
                 [cheshire "5.5.0"]
                 [clj-time "0.10.0"]]
  :main ^:skip-aot open-data.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
