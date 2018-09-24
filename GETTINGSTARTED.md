
# Getting Started


## Make sure you have
At least [Java JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

[Clojure](https://clojure.org/guides/getting_started) 

[Lein](https://leiningen.org/)


## Download HBase
```
http://apache.claz.org/hbase/2.1.0/
https://hbase.apache.org/book.html#quickstart
```

## Install HBase
```
export JAVA_HOME=$(/usr/libexec/java_home)
tar -xzvf hbase-2.1.0-client-bin.tar.gz
cd hbase-2.1.0
bin/start-hbase.sh
test "telnet 127.0.0.1 2181"
```

## Setup namespaces for the Datahub!
```
./bin/hbase shell
```

### In Hbase Shell
```
create_namespace 'datahub'
create_namespace 'datahub'
create 'datahub:apps', 'self'
create 'datahub:intake-sessions', 'self', 'app'
create 'datahub:intake-data', 'self', 'app', 'session'
list 'datahub:.*'
scan 'datahub:apps', { 'LIMIT' => 5 }
```

## In a Separate Shell
```
git clone https://github.com/paypal/seazme-hub.git
cd seazme-hub/
cp config.edn-template config.edn
<your-favorite-editor> config.edn
```

### Use this simple configuration for your local system.

```
;; config.edn
;; All config is by convention
;; Different types of configuration reside on the same level
;; Not all combinations are valid
;; Not all fields are always required
{
 ;;-Service
 :hashed-passwords #{"7c79b93154ab138d53f8e4d9050ccc71bf3a3245"} ;;example username:"open" password:"seazme"" -> "open:seazme" -> "b3BlbjpzZWF6bWU=" -> "Basic b3BlbjpzZWF6bWU=" -> "e778a55c8abd2c23ac308e2d4f79081713d036dd"
 ;;-Hadoop
 :configuration
 {:hbase {
          "hbase.client.retries.number" "1"
          "hbase.client.scanner.timeout.period" "1800000"
          "zookeeper.session.timeout" "1000"
          "zookeeper.recovery.retry" "1"
          "hbase.zookeeper.property.clientPort" "2181"
          "hbase.zookeeper.quorum" "localhost"
          }
  :kerberos {
             :user "<username>@<host>.COM" :path "username.keytab"
             }
  }
 }
```


### Start the server
```
lein ring server-headless
```

You should see `started server on port 3000`. Navigate to http://localhost:3000 in your browser.
