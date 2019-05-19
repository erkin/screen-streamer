(ns screen-streamer.client
  "Streaming client"
  (:use [screen-streamer [const :only [tiles port max-packet-length]]])
  (:gen-class)
  (:import (java.awt.image BufferedImage)
           (java.net DatagramPacket DatagramSocket)))

(defonce snips (atom (vec (repeat tiles (byte-array 0)))))
(defonce client (atom nil))
(defonce running (atom false))
(defonce counter (atom 0))



(defn update-snips
  "Iterate through maps of new snips and update the vector of existing ones."
  [news olds]
  (let [img (first news)]
    (if img
      (recur (rest news) (assoc olds (:index img) (:image img)))
      (reset! snips olds))))

(defn dismantle-packet
  "Split the first two bytes of the `DatagramPacket` for frame
  counter and snip index respectively. Pass the `ByteArray` of the
  image to check for duplicates and update the counter."
  [packet]
  (let [data (.getData packet)
        frame (first data)]
    (when (>= frame @counter)
      (reset! counter frame)
      (update-snips {:index (int (second data)) :image data} snips))))



(defn create-client []
  (DatagramSocket. port))

(defn start-client []
  (when (not @running)
   (reset! running true)
   (reset! client (create-client))
   (while @running
     (let [packet (DatagramPacket. (byte-array max-packet-length)
                                   max-packet-length)]
       (.receive @client packet)
       (dismantle-packet packet)
       (print (map count snips))))))

(defn stop-client []
  (when @running
   (reset! running false)
   (.close @client)
   (reset! client nil)))
