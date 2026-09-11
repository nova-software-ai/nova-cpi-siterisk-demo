import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// GENERATED from SiteRisk_MappingSpec.xlsx by Nova. Do not hand-edit; regenerate from the spec.
// FieldMapping rows: 11   RiskRules rows: 7

def round1(v) { v == null ? null : (Math.round((v as Double) * 10.0) / 10.0) }
def n(v)      { v == null ? Double.NEGATIVE_INFINITY : (v as Double) }

def Message processData(Message message) {
    def src = new JsonSlurper().parseText(message.getBody(String) ?: '{}')
    def t = [:]

    t.siteId = (src?.get('location')?.get('zip') as String)?.trim()?.toUpperCase()
    t.siteName = src?.get('location')?.get('city')
    t.region = src?.get('location')?.get('stateAbbreviation')
    t.tempF = round1(src?.get('weather')?.get('temperature'))
    t.aqi = src?.get('airQuality')?.get('usAqi')
    t.pm25 = round1(src?.get('airQuality')?.get('pm2_5'))
    t.windMph = round1(src?.get('weather')?.get('windSpeed'))
    t.observedAt = src?.get('weather')?.get('observedAt')

    // integration-test overrides (properties set by the sender hook)
    ['tempF':'simTempF','aqi':'simAqi','windMph':'simWindMph'].each { k, prop ->
        def o = message.getProperty(prop)
        if (o != null) { t[k] = (o as String).isNumber() ? (o as Double) : t[k] }
    }

    // --- decision table from sheet 'RiskRules', evaluated by Priority ---
    if (n(t.aqi) >= 130) {        // R10 prio 1
        t.riskLevel = 'SEVERE'
        t.advisory  = 'Outdoor work suspended'
        t.dispatchFlag = true
        t.ruleApplied = 'R10'
    } else if (n(t.aqi) >= 100 && n(t.tempF) >= 90) {        // R20 prio 2
        t.riskLevel = 'HIGH'
        t.advisory  = 'Heat and air quality - rotate crews hourly'
        t.dispatchFlag = true
        t.ruleApplied = 'R20'
    } else if (n(t.aqi) >= 100) {        // R30 prio 3
        t.riskLevel = 'ELEVATED'
        t.advisory  = 'Sensitive groups limit exposure'
        t.dispatchFlag = false
        t.ruleApplied = 'R30'
    } else if (n(t.tempF) >= 95) {        // R40 prio 4
        t.riskLevel = 'HIGH'
        t.advisory  = 'Heat protocol in effect'
        t.dispatchFlag = true
        t.ruleApplied = 'R40'
    } else if (n(t.tempF) <= 32) {        // R50 prio 5
        t.riskLevel = 'ELEVATED'
        t.advisory  = 'Freeze protocol - inspect lines'
        t.dispatchFlag = false
        t.ruleApplied = 'R50'
    } else if (n(t.windMph) >= 30) {        // R60 prio 6
        t.riskLevel = 'ELEVATED'
        t.advisory  = 'Suspend lift operations'
        t.dispatchFlag = true
        t.ruleApplied = 'R60'
    } else if (true) {        // R70 prio 99
        t.riskLevel = 'NORMAL'
        t.advisory  = 'No restrictions'
        t.dispatchFlag = false
        t.ruleApplied = 'R70'
    }

    def missing = ['siteId','siteName','region','tempF','aqi','observedAt','riskLevel','advisory','dispatchFlag'].findAll { t[it] == null }
    if (missing) {
        message.setProperty('errorCode','MAPPING_INCOMPLETE')
        throw new RuntimeException('MAPPING_INCOMPLETE: ' + missing.join(','))
    }

    message.setBody(JsonOutput.toJson([site: t, sourcePayload: src]))
    message.setHeader('Content-Type','application/json')
    return message
}
