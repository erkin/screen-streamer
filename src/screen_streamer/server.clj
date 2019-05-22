(ns screen-streamer.server
  "Streaming server"
  (:use [screen-streamer.const :only [tiles port max-packet-length freq]]
        [screen-streamer.screen :only [grab-snips]]
        [screen-streamer.network :only [broadcast-address]])
  (:gen-class)
  (:import (java.awt.image BufferedImage)
           (java.net DatagramPacket DatagramSocket InetSocketAddress)))


(defonce snips (atom (vec (repeat tiles (byte-array 0)))))
(defonce server (atom nil))
(defonce running (atom false))
(defonce counter (atom 0))


;; Internal functions

(defn check-duplicates
  "Compare two vectors of `ByteArray`s for duplicates, return uniques and
  their indices as maps."
  [imgs dups]
  (for [i (range tiles)
        :let [img (nth imgs i)
              dup (nth dups i)]
        :when (not= (seq img) (seq dup))]
    {:index i :image img}))

(defn prepare-packets
  "Take snips and their indices from a certain frame, turn them into
  a single `ByteArray`."
  [imgs frame]
  (letfn [(prepare-packet [img]
            (byte-array (concat (list (byte frame) (byte (img :index)))
                                (seq (img :image)))))]
    (mapv prepare-packet imgs)))

(defn send-packet
  "Send a packet to the `broadcast-address`.
  `msg` must be a `ByteArray`."
  [msg]
  (let [len (alength msg)]
    ;; Silently discard tiles that wouldn't fit in a UDP packet.
    (when (< len max-packet-length)
      (.send @server (DatagramPacket. msg len broadcast-address port)))))

(defn burst-frame
  "Take snips as `ByteArray`s, compare them against previous ones, label
  and send the unique ones as `DatagramPacket`s."
  [new-snips]
  (let [imgs (check-duplicates new-snips @snips)]
    (when (seq imgs)
      ;; Replace the previous snips with the new ones.
      (reset! snips new-snips)
      (run! send-packet (prepare-packets imgs @counter))
      ;; Update the frame counter and keep it bytesized.
      (if (= @counter 127)
        (reset! counter 0)
        (swap! counter inc)))))



(defn create-server []
  (let [socket (DatagramSocket. nil)
        address (InetSocketAddress. broadcast-address port)]
    (.setBroadcast socket true)
    ;; For testing on the same computer.
    (.setReuseAddress socket true)
    (.bind socket address)
    socket))

(defn start-server []
  (when-not @running
    (reset! running true)
    (reset! server (create-server))
    (while @running
      (Thread/sleep freq)
      ;; Send the chopped up screenshot.
      (burst-frame (grab-snips)))))

(defn stop-server []
  (when @running
    (reset! running false)
    (.close @server)
    (reset! server nil)))
