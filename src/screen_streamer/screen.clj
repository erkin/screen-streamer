(ns screen-streamer.screen
  "Screen capture"
  (:use screen-streamer.const)
  (:gen-class)
  (:import (java.io ByteArrayOutputStream)
           (java.awt AWTException Robot Rectangle Toolkit)
           (java.awt.image BufferedImage)
           (javax.imageio ImageIO)))



(defn screen-grab
  "Take a screenshot and return a `BufferedImage`."
  []
  (.createScreenCapture
   (Robot.)
   (Rectangle. (.getScreenSize (Toolkit/getDefaultToolkit)))))

(defn split-image
  "Split the `BufferedImage` into `tiles` number of tiles
  and return a list of `BufferedImage`s.
  Leftover columns/rows will be lost if image width/height
  cannot be evenly divided by the number of tiles."
  [image]
  ;; Dimensions of screen and tiles
  (let [w (quot (.getWidth  image) tiles)
        h (quot (.getHeight image) tiles)
        grid (range 0 (Math/sqrt tiles))]
    ;; Iterate over the grid
    (for [i grid
          j grid]
      (.getSubimage image (* i w) (* j h) w h))))

(defn assemble-image [images]
  (comment "stub"))

(defn image->bytes
  "Convert `BufferedImage` to `ByteArray`.
  `fmt` is a string that describes the image format,
  such as `png` or `jpg`."
  [image fmt]
  (let [baos (ByteArrayOutputStream.)]
    (ImageIO/write image fmt baos)
    (.toByteArray baos)))
