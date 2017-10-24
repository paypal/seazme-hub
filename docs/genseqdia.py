import urllib
import re

#TODO incremental rename
def getSequenceDiagram( text, outputFile, style = 'napkin' ):
    request = {}
    request["message"] = text
    request["style"] = style
    request["apiVersion"] = "1"

    url = urllib.urlencode(request)

    f = urllib.urlopen("http://www.websequencediagrams.com/", url)
    line = f.readline()
    f.close()

    expr = re.compile("(\?(img|pdf|png|svg)=[a-zA-Z0-9]+)")
    m = expr.search(line)

    if m == None:
        print "Invalid response from server."
        return False

    urllib.urlretrieve("http://www.websequencediagrams.com/" + m.group(0), outputFile )
    return True


arch = """
title Architecture

common-network/DataSource->hadoop-network-dmz/DataHub API: continuous documents and information import
hadoop-network-dmz/DataHub API->hadoop-network/HBASE: incremental save
common-network/DataSource->hadoop-network-dmz/DataHub API: continuous documents and information import
hadoop-network-dmz/DataHub API->hadoop-network/HBASE: incremental save

hadoop-network-dmz/SparkBatch->service-network/ElasticSearch: index updates
hadoop-network-dmz/SparkBatch<->hadoop-network/HBASE: analytics algorithms

common-network/User<->hadoop-network-dmz/SearchWebApp: interactive search session
hadoop-network-dmz/SearchWebApp<->service-network/ElasticSearch: actual search
common-network/User<->hadoop-network-dmz/DataHub API: analytics results lookup
"""


demo = """
title Authentication Sequence

Alice->Bob: Authentication Request
note right of Bob: Bob thinks about it
Bob->Alice: Authentication Response

note over A,B: text1
note left of A: text2
note right of A
    multiline
    text
end note

note over A,B: text1
note left of A: text2
note right of A
    multiline
    text
end note

loop text
    A->B: text
end
"""


createAPIKey = """
title SSO based/Create API key

WebBrowser->DataHub: GET /access/apikey
note right of WebBrowser: this is one time API key request
DataHub->SSO: SAML2.0 Auth Request/redirect
WebBrowser->SSO: credentials
SSO->WebBrowser: SAML2.0 encrypted token
DataHub->WebBrowser: "Authorization header"
"""
createApplication = """
title data intake source registration request

WebBrowser->DataHub: PUT /access/intake/applications
note right of WebBrowser: this is one time application provisioning
WebBrowser->DataHub: GET /access/applications/{id}
WebBrowser->DataHub: GET /access/applications/{id}
note right of WebBrowser: polling request status
WebBrowser->DataHub: GET /access/applications/{id}
"""

fullScan = """
title Full Confluence Scan and upload to DataHub
note right of Script: It is assumed that API key
note right of Script: has been provisioned and
note right of Script: used in below exchange
Script->DataHub: PUT /intake/sessions?incremental=false
DataHub->Script: trxid
Script<->Confluence: get next space/page
Script->DataHub: POST /intake/sessions/{id}/document
Confluence<->Script: get next space/page
Script->DataHub: POST /intake/sessions/{id}/document
Confluence<->Script: get next space/page
Script->DataHub: POST /intake/sessions/{id}/document
Confluence<->Script: get next space/page
Script->DataHub: POST /intake/sessions/{id}/document
note right of Script: ... it might take hours ...
Confluence<->Script: get next space/page
Script->DataHub: POST /intake/sessions/{id}/document
Script->DataHub: PUT /intake/sessions/{id}/submit
"""

incrementalScan = """
title Incremental Confluence Scan and upload to DataHub
note right of Script: It is assumed that API key
note right of Script: has been provisioned and
note right of Script: used in below exchange
Script->DataHub: PUT /intake/sessions?incremental=true
DataHub->Script: trxid, time segments
Script<->Confluence: get list of updated pages for given time segment
Confluence<->Script: get next page
Script->DataHub: POST /intake/sessions/{id}/document
Confluence<->Script: get next page
Script->DataHub: POST /intake/sessions/{id}/document
note right of Script: ... it usually takes 15 minutes to complete one update
Confluence<->Script: get next page
Script->DataHub: POST /intake/sessions/{id}/document
Script->DataHub: PUT /intake/sessions/{id}/submit
"""

#getSequenceDiagram(demo, "demo.png")
getSequenceDiagram(createAPIKey, "create-apikey.png")
getSequenceDiagram(createApplication, "create-application.png")
getSequenceDiagram(fullScan, "scan.png")
getSequenceDiagram(incrementalScan, "update.png")
getSequenceDiagram(arch, "arch.png")
