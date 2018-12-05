(ns swarmpit.component.service.info.deployment
  (:require [material.components :as comp]
            [material.component.form :as form]
            [sablono.core :refer-macros [html]]
            [rum.core :as rum]))

(enable-console-print!)

(defn item [name value]
  (html
    [:div.Swarmoit-row-space
     [:span name]
     [:span value]]))

(rum/defc form < rum/static [deployment]
  (let [autoredeploy (:autoredeploy deployment)
        update-delay (get-in deployment [:update :delay])
        update-parallelism (get-in deployment [:update :parallelism])
        update-order (get-in deployment [:update :order])
        update-failure-action (get-in deployment [:update :failureAction])
        rollback-delay (get-in deployment [:rollback :delay])
        rollback-parallelism (get-in deployment [:rollback :parallelism])
        rollback-order (get-in deployment [:rollback :order])
        rollback-failure-action (get-in deployment [:rollback :failureAction])
        placement (:placement deployment)
        restart-policy-condition (get-in deployment [:restartPolicy :condition])
        restart-policy-delay (get-in deployment [:restartPolicy :delay])
        restart-policy-window (get-in deployment [:restartPolicy :window])
        restart-policy-attempts (get-in deployment [:restartPolicy :attempts])]
    (comp/card
      {:className "Swarmpit-form-card"}
      (comp/card-header
        {:className "Swarmpit-form-card-header"
         :title     "Deployment"})
      (comp/card-content
        {}
        (comp/grid
          {:container true
           :spacing   40}
          (comp/grid
            {:item true
             :xs   6}
            (item "Autoredeploy" (str autoredeploy)))
          (comp/grid
            {:item true
             :xs   12}
            (form/subsection "Placements")
            (map #(comp/chip {:label (:rule %)}) placement))
          (comp/grid
            {:item true
             :xs   12
             :sm   6}
            (form/subsection "Restart Policy")
            (comp/grid
              {:container true
               :direction "column"}
              (comp/grid
                {:item true}
                (item "Condition" restart-policy-condition))
              (comp/grid
                {:item true}
                (item "Delay" (str restart-policy-delay "s")))
              (comp/grid
                {:item true}
                (item "Window" (str restart-policy-window "s")))
              (comp/grid
                {:item true}
                (item "Max Attempts" restart-policy-attempts))))
          (comp/grid
            {:item true
             :xs   12
             :sm   6}
            (form/subsection "Update Config")
            (comp/grid
              {:container true
               :direction "column"}
              (comp/grid
                {:item true}
                (item "Parallelism" update-parallelism))
              (comp/grid
                {:item true}
                (item "Delay" (str update-delay "s")))
              (comp/grid
                {:item true}
                (item "Order" update-order))
              (comp/grid
                {:item true}
                (item "On Failure" update-failure-action))))
          (when (= "rollback" (:failureAction update))
            (comp/grid
              {:item true
               :xs   12
               :sm   6}
              (form/subsection "Rollback Config")
              (comp/grid
                {:container true
                 :direction "column"}
                (comp/grid
                  {:item true}
                  (item "Parallelism" rollback-parallelism))
                (comp/grid
                  {:item true}
                  (item "Delay" (str rollback-delay "s")))
                (comp/grid
                  {:item true}
                  (item "Order" rollback-order))
                (comp/grid
                  {:item true}
                  (item "On Failure" rollback-failure-action))))))))))

