(ns app.worker)

(defonce timerID (atom nil))
(defonce interval (atom 100))

(js/self.addEventListener
 "message"
 (fn [e]
   (let [data (.-data e)]
     (cond
       (= data "start")
       (do
         (js/console.log "starting")
         (js/postMessage "tick")
         (reset! timerID (js/setInterval #(js/postMessage "tick") @interval)))
       (number? (.-interval data))
       (do
         (js/console.log "setting interval")
         (reset! interval (.-interval data))
         (js/console.log "interval =" @interval)
         (when-not (nil? @timerID)
           (js/clearInterval @timerID)
           (reset! timerID (js/setInterval #(js/postMessage "tick") @interval))))
       (= data "stop")
       (do
         (js/console.log "stopping")
         (js/clearInterval @timerID)
         (reset! timerID nil))))))