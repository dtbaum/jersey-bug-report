# jersey-bug-report
Jersey client seems to be not thread-safe:
When the first GET request is in progress, all parallel requests from other Jersey client instances fail with SSLHandshakeException: PKIX path building failed. 
Once the first GET request is completed, all subsequent requests work without error.

This project demonstrates an issue, as support for a pull request in https://github.com/eclipse-ee4j/jersey.
