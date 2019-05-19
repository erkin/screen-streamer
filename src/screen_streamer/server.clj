(ns screen-streamer.server
  "Streaming server"
  (:use screen-streamer.network
        screen-streamer.const)
  (:gen-class)
  (:import (java.awt.image BufferedImage)
           (java.net DatagramPacket DatagramSocket)))

;; We'll use port 0 to let the system allocate a free port for us.
(def port 0)
(def max-packet-length 65536)

(defonce snips (atom (vec (repeat tiles (byte-array 0)))))
(defonce server (atom nil))
(defonce counter (atom 0))



(defn check-duplicates
  "Compare two lists of `ByteArray`s for duplicates, return uniques and
  their indices as maps."
  [imgs dups]
  (for [i (range tiles)
        :let [img (nth imgs i)
              dup (nth dups i)]
        :when (not= img dup)]
    {:index i :image img}))

(defn prepare-packets
  "Take snips and their indices from a certain frame, turn them into
  a single `ByteArray`."
  [imgs frame]
  (defn prepare-packet [img]
    (byte-array [(byte frame) (byte (img :index)) (img :image)]))
  (byte-array (map prepare-packet imgs)))

(defn send-packet
  "Send a packet to the `broadcast-address` at port `port`.
  `msg` must be a `ByteArray`."
  [msg]
  (let [len (.length msg)]
    (if (< len max-packet-length)
      (DatagramPacket. msg len broadcast-address port)
      (throw (ex-info "Data too big to put into a UDP packet!"
                      {:length len})))))

(defn burst-frame
  "Take snips as `ByteArray`s, compare them against previous ones, label
  and send the unique ones as `DatagramPacket`s."
  [new-snips]
  (let [imgs (check-duplicates new-snips @snips)]
    (when (not (empty? imgs))
      ;; Replace the previous snips with the new ones.
      (swap! snips new-snips)
      (run! send-packet (prepare-packets imgs @counter))
      ;; Update the frame counter and keep it bytesized.
      (if (= @counter 127)
        (reset! counter 0)
        (swap! counter inc)))))

;; (defn zap [address]
;;   (send-packet (bytes address)))



(defn create-server []
  (let [socket (DatagramSocket. port)]
    (.setBroadcast socket true)
    socket))

(defn start-server []
  (reset! server (create-server)))

(defn stop-server []
  (.close @server)
  (reset! server nil))
