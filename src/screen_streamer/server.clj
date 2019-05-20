(ns screen-streamer.server
  "Streaming server"
  (:use [screen-streamer.const :only [tiles port max-packet-length]]
        [screen-streamer.screen :only [grab-snips]]
        [screen-streamer.network :only [broadcast-address]])
  (:gen-class)
  (:import (java.awt.image BufferedImage)
           (java.net DatagramPacket DatagramSocket InetSocketAddress)))


(defonce snips (atom (vec (repeat tiles (byte-array 0)))))
(defonce server (atom nil))
(defonce running (atom false))
(defonce counter (atom 0))



(defn check-duplicates
  "Compare two lists of `ByteArray`s for duplicates, return uniques and
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
    (if (< len max-packet-length)
      (.send @server (DatagramPacket. msg len broadcast-address port))
      (throw (ex-info "Data too big to put into a UDP packet!"
                      {:length len})))))

(defn burst-frame
  "Take snips as `ByteArray`s, compare them against previous ones, label
  and send the unique ones as `DatagramPacket`s."
  [new-snips]
  (let [imgs (check-duplicates new-snips @snips)]
    (when (not (empty? imgs))
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
    (.setReuseAddress socket true)
    (.bind socket address)
    socket))

(defn start-server []
  (when (not @running)
    (reset! running true)
    (reset! server (create-server))
    (while @running
      (Thread/sleep 70)
      ;; Send the chopped up screenshot.
      (burst-frame (grab-snips)))))

(defn stop-server []
  (when @running
    (reset! running false)
    (.close @server)
    (reset! server nil)))
