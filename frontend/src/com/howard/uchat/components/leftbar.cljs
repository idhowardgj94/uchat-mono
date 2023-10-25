(ns com.howard.uchat.components.leftbar
  (:require
   ["react-modal" :as Modal]
   ["react-avatar$default" :as Avatar]
   ["react-icons/ai" :refer [AiFillCaretDown AiFillCaretRight
                             AiOutlineCaretDown AiOutlinePlus]]
   ["react-icons/go" :refer [GoPlusCircle]]
   [com.howard.uchat.components.utilities :refer
    [popup]]
   [com.howard.uchat.components.basic :refer [form-group label input]]
   [com.howard.uchat.db :as db]
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]))

(defn multi-choose-list
  []
  [:div.flex.bg-yellow-300
   [:div "test"]])
(defn tree-menu
  "tree menu components,
  opts:
  open?
  title: string
  right-button-component: reagent fn
  data: vector of map {:avatar :title :href}
  TODO: row-component
  "
  [opts]
  (let [open-tree? (:open? opts)
        title (:title opts)
        {right :right-button-component} opts
        data (:data opts)]
    [:<>
     [:section.px-1.w-full.flex.mb-1
      [:button.flex-1.flex.items-center.text-gray-200
       {:on-click #(swap! open-tree? not)}
       (if (= true @open-tree?)
         [:> AiFillCaretDown]
         [:> AiFillCaretRight])
       [:p.px-1 title]]
      [:button.mr-2.ml-auto.text-gray-200
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
     [:> Modal {:isOpen true
                :style {:overlay {:backgroundColor "rgba(90,90,90, 0.3)"}
                        :content {:margin "0 auto"
                                  :width "70%"
                                  :max-height "fit-content"
                                  :display "block"}}
                :contentLabel "Create channel"}
      [:div
       [:div.text-2xl.font-bold.text-center "Create channel"]
       [form-group
        [label {:for "name"} "Channel Name"]
        [input {:name "name"
                :id "name"}]]
       [multi-choose-list]]]]))

(defn leftbar
  "navigation component"
  []
  (let [open-con (r/atom false)
        open-new (r/atom false)
        open-tree? (r/atom true)
        open-direct? (r/atom true)
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
                   :data (->> (:channel-subscriptions @subscriptions)
                              (map (fn [it]
                                     {:id (:other_user it)
                                      :avatar [:> Avatar {:name (:other_user it) :size 18 :className "rounded-full"}]
                                      :title (:other_user it)
                                      :href (rfe/href :routes/channels {:uuid "#"
                                                                        :channel-type :channel})})))}]
       [:div.my-2]
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
                                                                       :channel-type :direct}))}))))}]])))
