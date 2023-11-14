(ns com.howard.uchat.components.leftbar
  (:require
   ["react" :refer [useEffect]]
   ["react-modal" :as Modal]
   ["react-avatar$default" :as Avatar]
   ["react-icons/ai" :refer [AiFillCaretDown AiFillCaretRight
                             AiOutlineCaretDown AiOutlinePlus]]
   ["react-icons/go" :refer [GoPlusCircle]]
   [com.howard.uchat.components.utilities :refer
    [popup]]
   [com.howard.uchat.components.basic :refer [form-group label input]]
   [com.howard.uchat.components.button :refer [button]]
   [com.howard.uchat.components.multi-choose-list :refer [multi-choose-list]]
   [com.howard.uchat.db :as db]
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]))

(def mocks  [{:id 1
              :text "idhowardgj94"
              :avatar [:> Avatar {:name "idhowardgj94" :size 30 :className "rounded-full"}]}
             {:id 2
              :text "eva"
              :avatar [:> Avatar {:name "eva" :size 30 :className "rounded-full"}]}
             {:id 3
              :text "ricky"
              :avatar [:> Avatar {:name "ricky" :size 30 :className "rounded-full"}]}
             {:id 4
              :text "louis"
              :avatar [:> Avatar {:name "louis" :size 30 :className "rounded-full"}]}
             {:id 5
              :text "lisa"
              :avatar [:> Avatar {:name "lisa" :size 30 :className "rounded-full"}]}])

(defn- default-create-channel-form
  []
  {:name {:error? false
          :value ""}
   :usernames #{}})

(defn- get-reagent-props
  "get reagent props from this.
  helper function when use reagent lifecycle hook."
  [this]
  (-> this .-props .-argv))

(defn create-channel-modal
  "opt:
   :open? - atom<bool>, open modal or not.
   :close - fn, close modal fn"
  []
  (let [form (r/atom (default-create-channel-form))]
    (r/create-class
     {:component-did-update (fn [this prev]
                              ;; props format will be a clojure vector like following structure.
                              ;; [fn {:foo "bar"}]
                              ;; where second map is the real props.
                              (let [prev-open? (-> prev
                                                   second
                                                   :open?)
                                    current-open? (-> (get-reagent-props this)
                                                      second
                                                      :open?)]
                                (when (and
                                       (not current-open?)
                                       (not= prev-open? current-open?))
                                  (reset! form (default-create-channel-form)))))
      :reagent-render
      (fn [opt]
        (let [{:keys [close]} opt]
          [:> Modal {:isOpen (-> opt :open?)
                     :onRequestClose (fn []
                                       (reset! form (default-create-channel-form))
                                       (close))
                     :style {:overlay {:backgroundColor "rgba(90,90,90, 0.3)"}
                             :content {:margin "0 auto"
                                       :width "70%"
                                       :height "fit-content"
                                       :max-height "80%"
                                       :display "block"}}
                     :contentLabel "Create channel"}
           [:div
            [:div.text-2xl.font-bold.text-center.border-b.border-solid.py-2.mb-2 "Create channel"]
            [form-group
             [label {:for "name"} "Channel Name"]
             [input {:name "name"
                     :error? (-> @form :name :error?)
                     :on-change (fn [e]
                                  (swap! form assoc-in [:name :value] (-> e .-target .-value)))
                     :id "name"}]]
            [multi-choose-list {:data mocks
                                :on-click (fn [e]
                                            (let [{:keys [value checked]} e]
                                              (js/console.log checked)
                                              (swap! form update :usernames (if checked conj disj) value)
                                              (js/console.log (clj->js e))))}]
            [:div.flex.items-center.my-4.justify-center
             [button {:text "create"}]
             [:div.mx-2]
             [button {:text "Cancel" :color "yellow"
                      :on-click (fn []
                                  (print @form)
                                  (close))}]]]]))})))

(defn tree-menu
  "tree menu components,
  opts:
  :open? - bool
  :title - string
  :right-button-component - reagent fn
  :data - vector of map {:avatar :title :href}
  :open-create-handler - fn 
  :create-modal - vector (hicup)
  TODO: row-component
  "
  [opts]
  (let [open-tree? (:open? opts)
        open-create-handler (:open-create-handler opts)
        title (:title opts)
        {right :right-button-component} opts
        create-modal (:create-modal opts)
        data (:data opts)]
    [:<>
     [:section.px-1.w-full.flex.mb-1
      [:button.flex-1.text-gray-200.flex.items-center
       {:on-click #(swap! open-tree? not)}
       (if (= true @open-tree?)
         [:> AiFillCaretDown]
         [:> AiFillCaretRight])
       [:p.px-1 title]]
      [:button.mr-2.ml-auto.text-gray-200
       {:on-click open-create-handler}
       right]]
     (when @open-tree?
       [:<>
        ;; TODO: href
        (for [{:keys [avatar title href id]} data]
          ^{:key id}
          [:section.px-1.w-full.flex.text-gray-200
           [:a.flex-1.flex.pl-4.flex.items-center.hover:bg-gray-400.p-1
            {:href href}
            avatar
            [:spac.ml-2  title]]])])
     create-modal]))

(defn leftbar
  "navigation component"
  []
  (let [open-con (r/atom false)
        open-new (r/atom false)
        open-tree? (r/atom true)
        open-direct? (r/atom true)
        open-create-channel? (r/atom false)
        subscriptions (re-frame/subscribe [::db/subscribe [:direct-subscriptions :channel-subscriptions]])]
    (fn []
      [:sidebar {:style {:color "rgb(63, 67, 80)"
                         :background-color "rgb(30, 50, 92)"
                         :flex-basis "239px"}}
       [:div.h-14.w-full.px-4.items-center.hover:text-black.py-2.block.flex
        [:section.relative
         [:button.p-1.hover:bg-gray-100.hover:text-black.m1-2.text-white
          {:on-click #(swap! open-con not)}
          [:span "Contributors"]
          [:> AiOutlineCaretDown {:className "inline m-1"}]]
         [popup {:style {:width "100%"}
                 :className "rounded"
                 :open open-con}
          [:div.hover:bg-gray-200.p-2
           [:button.text-sm.text-black.font-light "View members"]]]]
        [:section.inline.ml-auto.mr-0.relative
         [:button.mr-0.ml-auto.text-white.hover:text-black.hover:bg-gray-100.p-1
          {:on-click #(swap! open-new not)}
          [:> GoPlusCircle]]
         [popup {:style {:width "10rem"
                         :position "absolute"}
                 :className "mx-2"
                 :open open-new}
          [:div.flex.w-full
           [:button.flex-1.font-light.text-sm.hover:bg-gray-200.p-1 "Create new channel"]]]]]

       [tree-menu {:open? open-tree?
                   :title "Channels"
                   :right-button-component [:> AiOutlinePlus]
                   :open-create-handler (fn [] (reset! open-create-channel? true))
                   :create-modal [create-channel-modal {:open? @open-create-channel?
                                                        :close (fn []
                                                                 (swap! open-create-channel? not))}]
                   :data (->> (:channel-subscriptions @subscriptions)
                              (map (fn [it]
                                     {:id (:other_user it)
                                      :avatar [:> Avatar {:name  (:name it) :size 18 :className "rounded-full"}]
                                      :title (:name it)
                                      :href (rfe/href :routes/channels {:uuid "#"
                                                                        :channel-type :channel})})))}]
       [:div.my-2
        [tree-menu {:open? open-direct?
                    :title "Direct Messages"
                    :right-button-component [:> AiOutlinePlus]
                    :data  (->> (:direct-subscriptions @subscriptions)
                                (map (fn [it]
                                       (let [{:keys [other_name other_user channel_uuid]} it]
                                         {:id (or channel_uuid other_name)
                                          :avatar [:> Avatar {:name (:other_name it) :size 18 :className "rounded-full"}]
                                          :title other_name
                                          :href
                                          (if (nil? channel_uuid)
                                            (rfe/href :routes/create-direct {:other-username other_user})
                                            (rfe/href :routes/channels {:uuid channel_uuid
                                                                        :channel-type :direct}))}))))}]]])))
