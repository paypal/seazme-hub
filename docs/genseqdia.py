import urllib
import re

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


createUser = """
title SSO based/Create API key

WebBrowser->datahubserv: PUT /access/user
note right of WebBrowser: this is one time provisioning
WebBrowser->SSO: credentials
SSO->WebBrowser: SAML2.0
datahubserv->WebBrowser: "API key, expiration"
"""

fullScan = """
title Full Confluence Scan and upload to datahubserv
note right of Script: It is assumed that API key
note right of Script: has been provisioned and
note right of Script: used in below exchange
Script->datahubserv: PUT /intake/begin, incremental==false
datahubserv->Script: trxid
Script<->Confluence: get next space/page
Script->datahubserv: intake/insert
Confluence<->Script: get next space/page
Script->datahubserv: intake/insert
Confluence<->Script: get next space/page
Script->datahubserv: intake/insert
Confluence<->Script: get next space/page
Script->datahubserv: intake/insert
note right of Script: ... it might take hours ...
Confluence<->Script: get next space/page
Script->datahubserv: intake/insert
Script->datahubserv: PUT /intake/commit
"""

incrementalScan = """
title Incremental Confluence Scan and upload to datahubserv
note right of Script: It is assumed that API key
note right of Script: has been provisioned and
note right of Script: used in below exchange
Script->datahubserv: PUT /intake/begin, incremental==true
datahubserv->Script: trxid, time segments
Script<->Confluence: get list of updated pages for given time segment
Confluence<->Script: get next page
Script->datahubserv: intake/insert
Confluence<->Script: get next page
Script->datahubserv: intake/insert
note right of Script: ... it usually takes 15 minutes to complete one update
Confluence<->Script: get next page
Script->datahubserv: intake/insert
Script->datahubserv: PUT /intake/commit
"""

#getSequenceDiagram(demo, "demo.png")
getSequenceDiagram(createUser, "create-user.png")
getSequenceDiagram(fullScan, "full-scan.png")
getSequenceDiagram(incrementalScan, "incremental-scan.png")
