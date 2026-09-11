# NovaSiteRisk - SAP Integration Suite artifact under source control

Site risk classification built on an inherited Cloud Integration flow.
Promoted by SAP Continuous Integration and Delivery, pipeline type `cpi`.

    spec/SiteRisk_MappingSpec.xlsx                 business mapping spec (11 fields, 7 conditional rules)
    spec/gen_mapping.py                            spec -> Groovy generator
    src/main/resources/script/siteRiskMap.groovy   GENERATED - regenerate, do not hand-edit
    src/.../integrationflow/NovaSiteRisk.iflw      BPMN, normalized so diffs are reviewable
    integrationTest/messageBody                    payload for the Integration Test stage

Regenerate: python3 spec/gen_mapping.py spec/SiteRisk_MappingSpec.xlsx src/main/resources/script/siteRiskMap.groovy
