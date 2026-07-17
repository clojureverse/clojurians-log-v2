(ns clojurians-log.components.common
  (:require
   [clojurians-log.components.icons :as icons]
   [co.gaiwan.slack.markdown :as mformat]
   [lambdaisland.ornament :as o]))

(o/defstyled avatar-img :img
  :w-10 :h-10 :rounded :mr-3)

(o/defstyled username-span :span
  :font-bold)

(o/defstyled timestamp-span :span
  :text-xs
  {:color "#9ca3af"})

(o/defstyled message-body :p
  :m-0
  :leading-normal
  {:color "#000"})

(o/defstyled message-row :div
  :flex :items-start :mb-4 :text-sm
  [:&.highlight {:background-color "#fcf87a"}])

(o/defstyled message-content :div
  {:flex 1
   :overflow "hidden"})

(o/defstyled reactions-row :div
  :mt-2)

(o/defstyled reply-thread :div
  :ml-2 :pl-3 :border-l-2 :border-gray-100)

(o/defstyled channel-link :a
  :block :text-white :no-underline :py-1 :px-4
  [:&:hover :bg-indigo-500])

(o/defstyled search-input :input
  :appearance-none :rounded-lg :py-2
  {:border "1px solid #9ca3af"})

(o/defstyled prose-wrapper :div
  {:max-width "65ch"
   :color "#374151"
   :font-size "1.125rem"
   :line-height "1.75"}
  [:h2 :text-2xl :font-bold :mb-2]
  [:h4 {:font-weight "600"
        :margin-top "1.5rem"}]
  [:p {:margin-top "1.25rem"
       :margin-bottom "1.25rem"}]
  [:ul {:margin-top "1.25rem"
        :margin-bottom "1.25rem"
        :padding-left "1.625rem"
        :list-style-type "disc"}]
  [:li {:margin-top "0.5rem"
        :margin-bottom "0.5rem"}]
  [:a :text-blue-500 :underline])

(o/defstyled date-link :a
  :p-1
  {:color "#4338ca"})

(o/defstyled team-icon-wrapper :div
  :bg-white :flex :items-center :justify-center :rounded-lg :overflow-hidden
  {:height "3rem"
   :width "3rem"
   :color "#000"
   :font-size "1.5rem"
   :font-weight "600"
   :margin-bottom "0.25rem"}
  [:img {:width "100%" :height "100%" :object-fit "cover"}]
  [:svg {:fill "currentColor"
         :height "2.5rem"
         :width "2.5rem"
         :display "block"}]
  [:&.team-icon-indigo
   {:background-color "#a5b4fc"
    :opacity 0.25}]
  [:&.team-icon-dim
   {:background-color "#fff"
    :opacity 0.25}])

#_
(o/defstyled team-sidebar :div
  :bg-indigo-900 :flex-none :p-4
  {:color "#d6bcfa"}
  [:.team-label
   :text-center :text-white :opacity-50 :text-sm]
  ([]
   [:<>
    [:div
     [team-icon-wrapper {}
      [:img {:alt "" :src "https://twitter.com/tailwindcss/profile_image"}]]
     [:div.team-label "⌘1"]]
    [:div
     [team-icon-wrapper {:class "team-icon-indigo"}
      "L"]
     [:div.team-label "⌘2"]]
    [:div
     [team-icon-wrapper {:class "team-icon-dim"}
      [:svg {:viewbox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
       [:path {:d "M16 10c0 .553-.048 1-.601 1H11v4.399c0 .552-.447.601-1 .601-.553 0-1-.049-1-.601V11H4.601C4.049 11 4 10.553 4 10c0-.553.049-1 .601-1H9V4.601C9 4.048 9.447 4 10 4c.553 0 1 .048 1 .601V9h4.399c.553 0 .601.447.601 1z"}]]]
     [:div.team-label "⌘3"]]]))

(o/defstyled channel-sidebar :div
  :bg-indigo-700 :flex-none :w-64 :pb-6 :overflow-x-hidden :overflow-y-scroll
  :absolute :inset-y-0 :left-0 :z-10
  {:color "#d6bcfa"
   :transform "translateX(-100%)"
   :transition "transform 200ms ease-in-out"}
  [:&.sidebar-visible
   {:transform "translateX(0)"}]
  [:at-media {:min-width "768px"}
   {:position "relative"
    :transform "translateX(0)"}]
  [:.sidebar-header
   :text-white :mb-2 :mt-3 :px-4 :flex :justify-between]
  [:.sidebar-header-left :flex-1]
  [:.sidebar-title
   :font-semibold :text-xl :leading-tight :mb-1]
  [:.sidebar-title [:a :text-white :no-underline]]
  [:.sidebar-status-row
   :flex :items-center :mb-6]
  [:.sidebar-status-icon
   {:height "0.5rem"
    :width "0.5rem"
    :fill "currentColor"
    :color "green"
    :margin-right "0.5rem"}]
  [:.sidebar-status-text
   :text-white :opacity-50 :text-sm]
  [:.sidebar-section
   :mb-8]
  [:.sidebar-section-label
   :px-4 :mb-2 :text-white :flex :justify-between :items-center]
  [:.sidebar-section-label-text
   :opacity-75]
  ([channels]
   [:<>
    {:id "sidebar"}
    [:div.sidebar-header
     [:div.sidebar-header-left
      [:h1.sidebar-title
       [:a {:href "/"} "Clojurians Log v2"]]
      [:div.sidebar-status-row
       [:svg.sidebar-status-icon {:viewbox "0 0 20 20"}
        [:circle {:cx "10" :cy "10" :r "10"}]]
       [:span.sidebar-status-text "Clojure programming"]]]]
    [:div.sidebar-section
     [:div.sidebar-section-label
      [:div.sidebar-section-label-text "Channels"]]
     (for [channel channels]
       [channel-link {:href (str "/" (:name channel))}
        (str "# " (:name channel))])]
    [:div.sidebar-section
     [:div.sidebar-section-label
      [:div.sidebar-section-label-text "Apps"]]]]))


(o/defstyled app-top-bar :div
  :border-b :flex :px-6 :py-2 :items-center :flex-none
  [:.title-row
   :flex :flex-row :justify-between :w-full :items-center]
  [:.title-area
   :overflow-hidden :w-full]
  [:.title-text
   {:color "#111827"
    :margin-bottom "0.25rem"}
   :font-extrabold]
  [:.subtitle-text
   {:color "#4b5563"
    :overflow "hidden"
    :text-overflow "ellipsis"
    :white-space "nowrap"}
   :text-sm]
  [:#mobile-menu-btn
   :md:hidden :border-none
   {:background "none"}
   :p-4]
  [:.menu-btn-icon
   {:width "1.5rem"
    :height "1.5rem"}]
  [:.search-area
   :ml-auto :hidden :md:block]
  [:.search-container
   :relative]
  [:.top-bar-search-input
   {:padding-left "2rem"
    :padding-right "1rem"}]
  [:.search-icon-wrapper
   :absolute :inset-y-0 :left-0 :flex :items-center :justify-center
   {:padding-left "0.75rem"}]
  [:.search-icon-svg
   {:fill "currentColor"
    :color "#9ca3af"
    :height "1rem"
    :width "1rem"}]
  ([title subtitle date]
   [:<>
    [:div.title-row
     [:div.title-area
      [:h3.title-text title " " date]
      #_(let [text (mformat/message->text subtitle {})]
          [:div.subtitle-text {:title text} text])]
     [:button#mobile-menu-btn
      [:div.menu-btn-icon icons/menu]]]
    [:div.search-area
     [:div.search-container
      [:form {:action "/search"}
       [search-input {:class "top-bar-search-input"
                      :placeholder "Search"
                      :name "q"
                      :type "search"}]]
      [:div.search-icon-wrapper
       [:svg.search-icon-svg {:viewbox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
        [:path {:d "M12.9 14.32a8 8 0 1 1 1.41-1.41l5.35 5.33-1.42 1.42-5.33-5.34zM8 14A6 6 0 1 0 8 2a6 6 0 0 0 0 12z"}]]]]]]))

(o/defstyled content-scroll :div
  :px-6 :py-4 :flex-1 :overflow-y-scroll)

(o/defstyled content-wrapper :div
  :flex-1 :flex :flex-col :bg-white :overflow-hidden)

(o/defstyled slack-layout :div
  {:font-family "'Inter', sans-serif"
   :-webkit-font-smoothing "antialiased"
   :-moz-osx-font-smoothing "grayscale"}
  :h-screen :flex
  ([{:keys [channels title subtitle date]
     :or {channels []
          title "Archives"
          subtitle "🦄 Try out the search feature -->"}} & body]
   [:<>
    [channel-sidebar channels]
    [content-wrapper
     [app-top-bar title subtitle date]
     (into [content-scroll] body)]]))

(o/defstyled welcome-title :h2
  :mb-4 :text-xl :font-bold)

(o/defstyled footer-text :p
  :text-sm)

(defn user-id-handler [slack-id->name]
  (fn [[_ user-id] _]
    [:span.username
     [:a #_{:href (str "https://someteam.slack.com/team/" user-id)}
      "@" (get slack-id->name user-id user-id)]]))

(defn emoji-handler [[_ code] _]
  [:span.emoji (get @mformat/standard-emoji-map code code)])

(o/defstyled message-head :div
  [:a :no-underline]
  ([display-name ts created-at channel-name date]
   [:div
    [username-span display-name]
    [:a {:href (str "/" channel-name "/" date "/" ts)}
     [timestamp-span (str " " created-at)]]]))

(defn message [{:member/keys [image-192 display-name]
                :message/keys [text created-at deleted-ts ts]
                :keys [reactions highlight]}
               {:keys [slack-id->name channel-name date]}]
  [message-row {:class (if highlight ["highlight"] [])}
   [avatar-img {:src (if deleted-ts "/assets/imgs/trash.png" image-192)}]
   [message-content
    (when-not deleted-ts
      [message-head display-name ts created-at channel-name date])
    [message-body
     (mformat/markdown->hiccup
      text
      {:handlers
       {:user-id (user-id-handler slack-id->name)
        :emoji emoji-handler}})]
    [reactions-row {:class "slack-message__reactions"}
     (for [reaction reactions]
       [:div.slack-message__reaction
        (mformat/text->emoji (:reaction/reaction reaction))
        " "
        (:count reaction)])]]])

(defn render-replies [replies msg-opts]
  [reply-thread
   (for [msg replies]
     [message msg msg-opts])])

(defn home-page [{:keys [channels]}]
  [slack-layout {:channels channels}
   [welcome-title "👋 Welcome Clojurians!"]
   [prose-wrapper
    [:p "This is a public archive of the " [:a {:href "https://clojurians.net/" "Clojurians Slack"}] " community."]
    [:p [:a {:href "https://arnebrasseur.net/"} "@plexus"] " handles hosting and system administration."]
    [:p "Despite a number of measures we've taken, we still occasionally get overwhelmed by aggressive scrapers,
         have a look at the " [:a {:href "/__kula"} "Server Monitoring"] " page to see the current situation."]
    [:p "Under " [:a {:href "/stats"} "/stats"] " you can see how much traffic gets through to the app."]
    [:p "This archive is served by a Clojure application backed by PostgreSQL. You can help make it better!
         Find the source code, create issues, or contribute at "
     [:a {:href "https://github.com/clojureverse/clojurians-log-v2"}
      "github.com/clojureverse/clojurians-log-v2"]]
    [:h4 "Searching the entire archive"]
    [:p "Use the top right box to search over ~2 million messages from the logs! The search queries supports some special syntax like: "]
    [:ul
     [:li "Search for `clojure` for a simple search"]
     [:li "Search for `clojure spaghetti` for messages containing both clojure and spaghetti (PS: you won't get back any results 😉)"]
     [:li "Search for `plant OR soil` for messages containing either plant or soil"]
     [:li "Search for `macro -magic` for finding a macro which isn't magical"]
     [:li "Search for `\"macro magic\"` for finding the most magical macros"]]
    [footer-text "Made with 💜 by "
     [:a {:href "https://miteshshah.com/"} "@oxalorg"]
     " & "
     [:a {:href "https://arnebrasseur.net/"} "@plexus"]]]])

(defn search-page [{:keys [query messages count]}]
  [slack-layout {:title (str "Search results for \"" query "\"")
                 :subtitle (str "in entire clojurians slack archive")}
   [:p "Found " count " results"]
   (for [msg messages]
     [message msg {}])])

(defn channel-page [{:keys [channels channel message-counts-by-date]}]
  [slack-layout
   {:channels channels :title (:name channel) :subtitle (:topic channel)}
   [:ul
    (for [{:keys [created-at count]} message-counts-by-date]
      [:li
       [date-link {:href (str "/" (:name channel) "/" created-at)}
        (str created-at "  --- (" count " messages)")]])]])

(defn channel-date-page [{:keys [channels channel messages replies date member-cache-id-name ts]}]
  (let [channel-name (:name channel)
        msg-opts {:slack-id->name member-cache-id-name
                  :channel-name channel-name
                  :date date}]
    [slack-layout
     {:channels channels :title channel-name :subtitle (:topic channel) :date date}
     (for [msg messages]
       [:div
        [message (cond-> msg
                   (= ts (:message/ts msg))
                   (assoc :highlight true))
         msg-opts]
        [render-replies
         (map #(cond-> %
                 (= ts (:message/ts %))
                 (assoc :highlight true))
              (get replies (:message/id msg)))
         msg-opts]])]))
