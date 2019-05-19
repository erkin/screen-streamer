(ns screen-streamer.network
  "Networking functions"
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

(def localhost-address
  (InetAddress/getLocalHost))

(def broadcast-address
  (get (first (get-addresses)) :broadcast))
