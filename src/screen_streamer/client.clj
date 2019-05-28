(ns screen-streamer.client
  "Streaming client"
  (:use [screen-streamer.const :only [tiles port max-packet-length]]
        [screen-streamer.network :only [broadcast-address]]
        [screen-streamer.screen :only [make-image]])
  (:gen-class)
  (:import (java.awt.image BufferedImage)
           (java.net DatagramPacket DatagramSocket
                     InetSocketAddress SocketException)))

(defonce snips (atom (vec (repeat tiles (byte-array 0)))))
(defonce image (atom nil))

(defonce client (atom nil))
(defonce running (atom false))
(defonce counter (atom 0))


;; Internal functions

(defn update-snips
  "Take a map containing a new snip and update the vector of old ones."
  [img]
  (reset! snips (assoc @snips (img :index) (img :image))))

(defn dismantle-packet
  "Split the first two bytes of the `DatagramPacket` for frame
  counter and snip index respectively."
  [packet]
  (let [data (.getData packet)
        frame (first data)
        index (second data)
        image (subvec (vec data) 2)]
    (when (>= frame @counter)
      (if (= @counter 127)
        (reset! counter 0)
        (reset! counter (int frame)))
      {:index (int index) :image (byte-array image)})))



(defn create-client []
  (let [socket (DatagramSocket. nil)
        address (InetSocketAddress. broadcast-address port)]
    ;; For testing multiple clients on the same computer.
    (.setReuseAddress socket true)
    (.bind socket address)
    socket))

(defn start-client []
  (when-not @running
    (reset! running true)
    (reset! client (create-client))
    (while @running
      (let [packet (DatagramPacket. (byte-array max-packet-length)
                                    max-packet-length)]
        ;; Ignore the exception thrown when the socket is closed from the
        ;; main thread. Temporary fix, I swear.
        (try (.receive ^DatagramSocket @client packet)
             (catch SocketException ex []))
        (let [data (dismantle-packet packet)]
          ;; Check if the frame is new.
          (when data
            (update-snips data)
            ;; Don't attempt to regenerate an image with empty snips.
            (when-not (some empty? @snips)
              ;; Reassemble the image with the new snips.
              (reset! image (make-image @snips)))))))))

(defn stop-client []
  (when @running
    (reset! running false)
    (.close @client)
    (reset! client nil)))
