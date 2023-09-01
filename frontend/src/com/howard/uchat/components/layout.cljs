(ns com.howard.uchat.components.layout
  (:require
   [com.howard.uchat.styles :as s]
   [reagent.core :as r]
   [com.howard.uchat.components.utilities :refer
    [popup >children verticle-line get-childern get-opts]]
   ["react-icons/go" :refer [GoPlusCircle]]
   ["react-icons/ai" :refer [AiOutlineCaretDown AiFillCaretRight AiOutlinePlus
                             AiOutlineGlobal AiFillCaretDown]]
   ["react-icons/si" :refer [SiGooglemessages  SiIconify]]
   ["react-icons/sl" :refer [SlLogout]]
   ["react-avatar$default" :as Avatar]))

(defn popup-status-bar
  []
  [:div
   [:button.hover:bg-gray-200.w-full.my-2
    [:div.px-4.w-full.flex.items-center
     [:> Avatar {:name "Howard" :className "rounded-full" :size 36}]
     [:div.p-3 "@lhchangq"]]]
   [verticle-line]
   [:button.hover:bg-gray-200.w-full.my-2
    [:div.px-4.w-full.flex.items-center
     [:> SiIconify {:size "14px" :color "gray"}]
     [:div.p-2.text-gray-400.text-sm "Set a Custom Status"]]]
   [verticle-line]
   [:button.hover:bg-gray-200.w-full.my-2
    [:div.px-4.w-full.flex.items-center
     [:> SlLogout {:size "14px"}]
     [:div.p-2 "Logout"]]]])

(defn header []
  (let [popup-open? (r/atom false)]
    [:header.basis-10.flex.items-center.px-3.shrink-0
     {:style {:background-color "#14213e"
              :color "rgba(255, 255, 255, 0.64)"}}
     [:> SiGooglemessages {:size 24}]
     [:h1.font-lg.ml-2 "Channel"]
     [:div.relative.mr-0.ml-auto
      [:button {:on-click #(swap! popup-open? not)}
       [:div.rounded-full.overflow-hidden
        [:> Avatar {:name "Howard" :size 30}]]
       (comment "status light")
       [:span.rounded-full.bg-yellow-200.z-50.absolute.inline-block
        {:style {:min-width 10
                 :min-height 10
                 :right "0px"
                 :bottom "4px"}}]]
      [popup {:style {:right "1px"}
              :open popup-open?}
       [popup-status-bar]]]]))

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
           [:button.flex-1.flex.pl-4.flex.items-center.hover:bg-gray-400.p-1
            avatar
            [:span.ml-2 title]]])])]))

(defn leftbar
  "navigation component"
  []
  (let [open-con (r/atom false)
        open-new (r/atom false)
        open-tree? (r/atom true)
        open-direct? (r/atom true)]
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
                   :data [{:id 1
                           :avatar [:> AiOutlineGlobal]
                           :title "Genernal"
                           :href "#"}]}]
       [:div.my-2]
       [tree-menu {:open? open-direct?
                   :title "Direct Messages"
                   :right-button-component [:> AiOutlinePlus]
                   :data [{:id 1
                           :avatar [:> Avatar {:name "Howard" :size 18 :className "rounded-full"}]
                           :title "Lhchangq"
                           :href "#"}
                          {:id 2
                           :avatar [:> Avatar {:name "Eva" :size 18 :className "rounded-full"}]
                           :title "Eva"
                           :href "#"}
                          {:id 3
                           :avatar [:> Avatar {:name "rtkao" :size 18 :className "rounded-full"}]
                           :title "rtkao"
                           :href "#"}
                          {:id 4
                           :avatar [:> Avatar {:name "pkyeh" :size 18 :className "rounded-full"}]
                           :title "pkyeh"
                           :href "#"}
                          ]}]
       ])))

(defn main-layout
  "this is main layout for uchat."
  [opts & children]
  (let [opt (get-opts opts)
        children' (get-childern opts children)]
    [:div.h-screen.flex.flex-col
     [header]
     [:div.flex.flex-1.overflow-auto opt
      [leftbar]
      [>children children']]]))

(defn guest-layout
  "guest layout is used when user is not login yet."
  [opts & children]
  (let [opt (get-opts opts)
        children' (get-childern opts children)]
    [:div.w-screen.h-screen.overflow-auto (assoc-in opt [:style :background-image] s/login-background)
     [:div.container.mx-auto.flex.max-w-6xl.overflow-auto.h-full.items-center
      [:section.flex-1
       [:h1.text-6xl {:style {:font-weight "bold"}} "Welcome to
                                      UChat workspace"]]
      [:section.flex-1.items-center.justify-center
       [>children children']]]]))
