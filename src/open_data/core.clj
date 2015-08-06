(ns open-data.core
  (:gen-class)
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            monger.joda-time
            [org.httpkit.client :as http]
            [cheshire.core :refer :all]
            [clj-time.core :as t])
  (:import (org.bson.types ObjectId)))


(def conn (mg/connect))
(def db (mg/get-db conn "open-data"))

(defn prettyify-line
  "Temporary:
    Index each element in the line to better understand what data is relevant"
  [index line-array]
  (def counter (atom 0))
  (for [index (nth line-array index)]
    (do
      (println counter index)
      (swap! counter inc))))

(def categories '(:incident-id :incident-type :case-number :incident-date :suspect :arrested :address :victim :details :released-by :date-modified))

(defn turn-empty-string-to-nil
  [element]
  (if (clojure.string/blank? element)
    nil
    element))

(defn normalize
  "trims white space off of each end of a string unless it's nil and turns empty strings into nil"
  [element]
  (when (not (nil? element))
    (->> element
         clojure.string/trim
         turn-empty-string-to-nil)))

(defn extract-year-month-day
  "given a datetime of 2009-11-22T00:00:00.000 returns a list of (2009 11 22)"
  [date-time]
  (map #(int (Float/parseFloat %)) (clojure.string/split (subs date-time 0 10) #"-")))


(defn parse-address-in-document
  "turns the :incidend-date value of 2009-11-22T00:00:00.000, for example, into an actual datetime"
  [incident-map]
  (update-in incident-map [:incident-date] #(apply t/date-time (extract-year-month-day %))))

(defn create-incident-document
  "Creates a map with corresponding keys of `categories` defined above"
  [line]
  (do
    (->> line
         (#(nthnext % 8))
         (map normalize)
         (zipmap categories)
         (#(conj % {:_id (ObjectId.)}))
         parse-address-in-document
         (mc/insert db "incidents"))))

(defn store-data
  "stores each line into Mongo"
  [data]
  (for [line data]
    (create-incident-document line)))

(defn fetch-all-data
  "fetches data from city of Madison, parses it, massages it, and stores it"
  []
  (let [{:keys [status headers body error] :as resp} @(http/get "https://data.cityofmadison.com/api/views/d686-rvcw/rows.json?accessType=DOWNLOAD")]
    (if error
      (println "Failed, exception: " error)
      (->> body
           (#(parse-string % keyword))
           :data
           store-data))))