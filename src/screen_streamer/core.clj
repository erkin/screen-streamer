(ns screen-streamer.core
  "GUI portion of the program"
  (:use screen-streamer.const
        [screen-streamer.server :only [start-server stop-server]]
        [screen-streamer.client :only [start-client stop-client image]]
        [screen-streamer.screen :only [screen-size save-image]]
        [clojure.core.async :only [thread]]
        seesaw.core
        [seesaw.chooser :only [choose-file]])
  (:gen-class :main true))


(def l (label))

(def p (grid-panel :border "screen-streamer"
                   :columns 1
                   :items [l]))

(def f (frame :on-close :exit
              :content p))

(defn set-status [status]
  (config! p :border status))

(defn set-screen [image]
  (config! l :icon image))

(defn prepare-client-frame
  "Resize the screen and set a watcher on the image atom that blits the
  image on the screen with each update."
  []
  (set-screen [])
  (config! f :size [(first screen-size) :by (second screen-size)])
  (add-watch image :client-watcher
             (fn [key atom old-image new-image]
               (when-not (nil? new-image)
                 (set-screen (icon new-image))))))

(defn clear-client-frame []
  (set-screen [])
  (remove-watch image :client-watcher))

(defn save-screenshot
  "Ask the user for a path to save the screenshot."
  []
  (let [img @image]
    ;; Ignore event if image isn't ready.
    (when-not (nil? img)
      (choose-file
       f :type :save
       :success-fn (fn [fc path]
                     (save-image img path))))))



(def client-file-menu
  (menu
   :text "File"
   :items [(action
            :name "Start listening"
            :handler (fn [e]
                       (thread (start-client))
                       (prepare-client-frame)
                       (set-status "Listening."))
            :tip "Establish connection.")
           (action
            :name "Stop listening"
            :handler (fn [e]
                       (stop-client)
                       (clear-client-frame)
                       (set-status "Not listening."))
            :tip "Disestablish connection.")
           (action
            :name "Save screenshot"
            :handler (fn [e]
                       (save-screenshot)))
           (action
            :name "Exit"
            :handler (fn [e]
                       (stop-client)
                       (.dispose f))
            :tip "Exit program.")]))

(def server-file-menu
  (menu
   :text "File"
   :items [(action
            :name "Start broadcasting"
            :handler (fn [e]
                       (thread (start-server))
                       (set-status "Broadcasting."))
            :tip "List active connections.")
           (action
            :name "Stop broadcasting"
            :handler (fn [e]
                       (stop-server)
                       (set-status "Not broadcasting."))
            :tip "Close session to client.")
           (action
            :name "Exit"
            :handler (fn [e]
                       (stop-server)
                       (.dispose f))
            :tip "Exit program.")]))

(def help-menu
  (menu
   :text "Help"
   :items [(action
            :name "About"
            :handler (fn [e] (alert f about-message
                                   :title "About"
                                   :type :info))
            :tip "About this program.")]))



(defn launch-frame [f]
  (invoke-later (show! (pack! f))))

(defn launch-client []
  (config! f :menubar (menubar :items [client-file-menu help-menu]))
  (config! f :title (str program-name " client"))
  (config! f :size [640 :by 480])
  (set-status "Not listening.")
  (launch-frame f))

(defn launch-server []
  (config! f :menubar (menubar :items [server-file-menu help-menu]))
  (config! f :title (str program-name " server"))
  (config! f :size [320 :by 240])
  (set-status "Not broadcasting.")
  (launch-frame f))

(defn -main [& args]
  (native!)
  (case (input "Pick the mode of operation"
               :title program-name
               :choices ["client" "server"])
    "client" (launch-client)
    "server" (launch-server)
    nil      (System/exit 0)))
