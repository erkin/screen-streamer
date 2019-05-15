(ns screen-streamer.network
  "Networking functions"
  (:require [clojure.tools.logging :as log])
  (:gen-class)
  (:import (java.net InetAddress Inet4Address
                     InterfaceAddress NetworkInterface)))

(defn get-addresses
  "Iterate through `NetworkInterfaces` and return a list of their
  addresses. IPv4 only."
  []
  (->> (NetworkInterface/getNetworkInterfaces)
       enumeration-seq
       (map bean)
       (mapcat :interfaceAddresses)
       (map bean)
       (filter #(= (.getClass (:address %)) Inet4Address))))

(defn get-localhost-address []
  (InetAddress/getLocalHost))

(defn get-broadcast-address []
  (get (first (get-addresses)) :broadcast))
