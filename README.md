# jersey-bug-report
Jersey client is not thread-safe and ignores SSL settings due to race condition commited in https://github.com/eclipse-ee4j/jersey/pull/5749.  
When the first GET request is in progress, all parallel requests from other Jersey client ignore configured SSL settings and switch to default - in this case the default cert isn't accepted:  requests fail with SSLHandshakeException: PKIX path building failed. 
