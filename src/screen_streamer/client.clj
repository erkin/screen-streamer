(ns screen-streamer.client
  "Streaming client"
  (:use screen-streamer.network)
  (:gen-class)
  (:import (java.net InetSocketAddress DatagramPacket DatagramSocket)))

;; We'll use port 0 to let the system allocate a free port for us.
(def port 0)
(def max-packet-length 65536)

(defonce client (atom nil))
(defonce counter (atom 0))


