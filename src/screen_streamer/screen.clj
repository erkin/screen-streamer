(ns screen-streamer.screen
  "Screen capture"
  (:use [screen-streamer.const tiles])
  (:require [clojure.tools.logging :as log])
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
  (let [width  (.getWidth image)
        height (.getHeight image)
        w (quot width  tiles)
        h (quot height tiles)
        grid (range 0 (Math/sqrt tiles))]
    (when (not= width  (* w tiles))
      (log/warn "Screen width doesn't evenly divide by tiles."
                "Cropping out" (- width (* w tiles)) "columns."))
    (when (not= height (* h tiles))
      (log/warn "Screen height doesn't evenly divide by tiles."
                "Cropping out" (- height (* h tiles)) "rows."))
    ;; Iterate over the grid
    (for [i grid
          j grid]
      (.getSubimage image (* i w) (* j h) w h))))

(defn assemble-image [images]
  (comment "stub"))

(defn image->bytes
  "Convert `BufferedImage` to `ByteArray`.
  `format` is a string that describes the image format,
  such as `png` or `jpg`."
  [image format]
  (let [baos (ByteArrayOutputStream.)]
    (ImageIO/write image format baos)
    (.toByteArray baos)))
