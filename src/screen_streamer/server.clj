(ns screen-streamer.server
  "Streaming server"
  (:use screen-streamer.network
        [screen-streamer.const tiles])
  (:require [clojure.tools.logging :as log])
  (:gen-class)
  (:import (java.awt.image BufferedImage)
           (java.net DatagramPacket DatagramSocket)))

;; We'll use port 0 to let the system allocate a free port for us.
(def port 0)
(def max-packet-length 65536)
(def broadcast-address (get-broadcast-address))

(defonce snips (atom (vector (byte-array 0) 4)))
(defonce server (atom nil))
(defonce counter (atom 0))



(defn check-duplicates
  "Compare two lists of snips for duplicates, return uniques and their
  indices as pairs."
  [imgs dups]
  (for [i (range 0 tiles)
        :let [img (nth imgs i)
              dup (nth dups i)]
        :when (not= img dup)]
    [i img]))

(defn prepare-packets
  "Take pairs of snips and their indices from a certain frame, turn them
  into a single `ByteArray`."
  [imgs frame]
  (byte-array (map (fn [img] (byte-array [(byte frame) (byte (first img)) (second img)])) imgs)))

(defn send-packet
  "Send a packet to the `InetAddress` returned by `get-broadcast-address`
  at port `port`. `msg` must be a `ByteArray`."
  [msg]
  (let [len (.length msg)]
    (if (< len max-packet-length)
      (DatagramPacket. msg len broadcast-address port)
      (throw (ex-info "Data too big to put into a UDP packet!"
                      {:length len})))))

(defn burst-frame
  "Take snips as `ByteArray`s, compare them against previous ones, label
  and send the unique ones as `DatagramPacket`s."
  [imgs]
  (let [pairs (check-duplicates imgs @snips)]
    ;; Replace the previous snips with the new one.
    (swap! snips imgs)
    (run! send-packet (prepare-packets pairs @frame-counter))
    ;; Update the frame counter and keep it bytesized.
    (if (= @frame-counter 127)
      (reset! frame-counter 0)
      (swap! frame-counter inc))))



(defn create-server []
  (let [socket (DatagramSocket. port)]
    (.setBroadcast socket true)
    socket))

(defn start-server []
  (reset! server (create-server)))

(defn stop-server []
  (.close @server)
  (reset! server nil))
