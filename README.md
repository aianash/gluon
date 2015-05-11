# Gluon

Any new catalogue item or updates to catalogue items are injected
to the backend (internal) system using this service.

Gluon also performs various checks and transformation on catalogue items
before publising to kafka topic "catalogue-items"

Primary services that are listening to the kafka topic are [Asterix (Cassie)](http://bitbucket.org/shoplanedev/cassie)
and [Onyx](http://bitbucket.org/shoplanedev/creed). Asterix is responsible for injecting catalogue items to data storage.
And Onyx is responsible for adding the catalogue items to index.