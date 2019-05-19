(ns screen-streamer.const
  "Constant values"
  (:gen-class))

(def program-name "screen-streamer")

(def about-message
  (str program-name " v0.2" \newline
       \newline
       "Copyright (C) 2019 Erkin Batu Altunbaş" \newline
       \newline
       "Each file of this project's source code is subject" \newline
       "to the terms of the Mozilla Public Licence v2.0" \newline
       "If a copy of the MPL was not distributed with this" \newline
       "file, you can obtain one at https://mozilla.org/MPL/2.0"))

(def max-packet-length 65536)
(def port 5001)
;; Must be a square number.
(def tiles 4)
(def image-format "png")
