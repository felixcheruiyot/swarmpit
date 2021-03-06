(ns swarmpit.component.api-access
  (:require [material.components :as comp]
            [swarmpit.component.message :as message]
            [swarmpit.component.state :as state]
            [swarmpit.component.mixin :as mixin]
            [swarmpit.component.progress :as progress]
            [swarmpit.url :refer [dispatch!]]
            [swarmpit.ajax :as ajax]
            [swarmpit.routes :as routes]
            [sablono.core :refer-macros [html]]
            [rum.core :as rum]))

(enable-console-print!)

(defn- me-handler
  []
  (ajax/get
    (routes/path-for-backend :me)
    {:state      [:loading?]
     :on-success (fn [{:keys [response]}]
                   (state/update-value [:api-token] (:api-token response) state/form-value-cursor))}))

(defn- init-form-state
  []
  (state/set-value {:loading? true} state/form-state-cursor))

(def mixin-init-form
  (mixin/init-form
    (fn [_]
      (init-form-state)
      (me-handler))))

(defn- generate-handler
  []
  (ajax/post
    (routes/path-for-backend :api-token-generate)
    {:on-success (fn [{:keys [response]}]
                   (state/update-value [:token] response state/form-value-cursor))
     :on-error   (fn [_]
                   (message/error (str "Failed to generate API token.")))}))

(defn- remove-handler
  []
  (ajax/delete
    (routes/path-for-backend :api-token-remove)
    {:on-success (fn [_]
                   (state/update-value [:api-token] nil state/form-value-cursor)
                   (state/update-value [:token] nil state/form-value-cursor))
     :on-error   (fn [_]
                   (message/error (str "Failed to remove API token.")))}))

(defn- form-token [value]
  (comp/text-field
    {:label           "Authorization Token"
     :fullWidth       true
     :name            "token"
     :key             "token"
     :variant         "outlined"
     :margin          "normal"
     :multiline       true
     :value           value
     :InputProps      {:readOnly true
                       :style    {:fontFamily "monospace"}}
     :InputLabelProps {:shrink true}}))

(defn old-token-form [api-token]
  [(comp/typography {:key "info"}
                    ["Token for this user was already created. If you lost your token, please generate new one and "
                     "the former token will be revoked."])
   (form-token (str "Bearer ..." (:mask api-token)))])

(defn new-token-form [token]
  [(comp/typography {:key "notice"} "Copy your token and store it safely, value will be displayed only once.")
   (form-token (:token token))])

(defn no-token-form []
  [(comp/typography {:key "notoken"} "Your user doesn't have any API token.")
   (comp/typography {:key "info"} "New token doesn't expire, but it can be revoked or regenenerated.")])

(rum/defc form-api-token < rum/reactive []
  (let [{:keys [api-token token]} (state/react state/form-value-cursor)
        state (if (and api-token (not token))
                :old
                (if token :new :none))]
    (html
      [:div.Swarmpit-access-form
       (comp/grid
         {:container true
          :spacing   24}
         (comp/grid
           {:item true
            :xs   12}
           (case state
             :old (old-token-form api-token)
             :new (new-token-form token)
             :none (no-token-form)))
         (comp/grid
           {:item true
            :xs   12}
           (html
             [:div.Swarmpit-form-buttons
              (comp/button
                {:variant  "contained"
                 :key      "submit"
                 :disabled false
                 :onClick  generate-handler
                 :color    "primary"}
                (case state :none "Generate" "Regenerate"))
              (comp/button
                {:variant  "outlined"
                 :key      "remove"
                 :disabled (= :none state)
                 :onClick  remove-handler}
                "Remove")])))])))

(rum/defc form < rum/reactive
                 mixin-init-form []
  (let [state (state/react state/form-state-cursor)]
    (progress/form
      (:loading? state)
      (form-api-token))))

