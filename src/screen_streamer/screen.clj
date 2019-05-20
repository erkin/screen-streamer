(ns screen-streamer.screen
  "Screen capture"
  (:use [screen-streamer.const :only [tiles image-format]])
  (:gen-class)
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream)
           (java.awt Graphics2D Robot Rectangle Toolkit)
           (java.awt.image BufferedImage)
           (javax.imageio ImageIO)))


(def screen-size
  (let [size (.getScreenSize (Toolkit/getDefaultToolkit))]
    (list (.getWidth size) (.getHeight size))))


;;; Internal functions

(defn image->bytes
  "Convert `BufferedImage` to `ByteArray`.
  `fmt` is a string that describes the image format,
  such as `png` or `jpg`."
  [image fmt]
  (let [baos (ByteArrayOutputStream.)]
    (ImageIO/write image fmt baos)
    (.toByteArray baos)))

(defn bytes->image
  "Convert `ByteArray` to `BufferedImage`."
  [data]
  (let [bais (ByteArrayInputStream. data)]
    (ImageIO/read bais)))


;;; Server functions

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
  (let [row (Math/sqrt tiles)
        w (quot (.getWidth  image) row)
        h (quot (.getHeight image) row)
        grid (range row)]
    ;; Iterate over the grid
    (for [i (range row)
          j (range row)]
      (.getSubimage image (* j w) (* i h) w h))))

(defn grab-snips
  "Capture a screenshot and return a vector of `ByteArray`s containing
  its snips."
  []
  (mapv #(image->bytes % image-format) (split-image (screen-grab))))


;;; Client functions

(defn make-images [snips]
  (mapv bytes->image snips))

(defn stitch-snips
  "Assembles a vector of `BufferedImage`s together as tiles.
    Returns a new `BufferedImage`."
  [snips]
  (let [row (Math/sqrt tiles)
        w (.getWidth  (first snips))
        h (.getHeight (first snips))
        image (BufferedImage. (* w row) (* h row)
                              BufferedImage/TYPE_INT_ARGB)
        canvas (.createGraphics image)]
    (doseq [i (range row)
            j (range row)]
      (.drawImage canvas
                  (nth snips (+ j (* i row)))
                  nil (* j w) (* i h)))
    (.dispose canvas)
    image))

(defn make-image
  [snips]
  (stitch-snips (mapv bytes->image snips)))
