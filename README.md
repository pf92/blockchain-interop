# A Framework for Blockchain Interoperability and Runtime Selection
This repository provides the reference implementation of a geneneral-purpose framework for storing abritrary data on blockchains. The framework abstracts technical details and provides interfaces for writing data into and reading data from multiple blockchains. The framework monitors multiple blockchains, calculates their individual benefits and determines the most beneficial one based on user-defined requirements. Furthermore, the framework is able to react to various events such as a rapid decrease of a blockchain network's hash rate or a steadily increase of the cost of writing data into a blockchain. In case another blockchain is more appropriate, the framework enables to switch to that chain at runtime. During a switchover, a user-defined amount of data can be migrated. The reference implementation supports Bitcoin, Ethereum, Ethereum Classic, and Expanse.

Note: This is a proof-of-concept implementation. It has not been tested for production.

## Components
### Blockchain Manager
The central component of the framework is the Blockchain Manager. The Blockchain Manager has a list of all supported blockchains. Each blockchain must be registered at the Blockchain Manager in order to get recognized by the selection and switchover algorithm. Furthermore, the Blockchain Manager exposes methods enabling the user
* to specify settings that affect the weighted ranking system and the threshold validation mechanism,
* to register a blockchain,
* to preselect a blockchain that is considered as the most beneficial chain for the
startup phase,
* to write data to the current blockchain,
* to subscribe to switchover suggestions and
* to initiate a switchover to another blockchain

### The Blockchain Component
The *Blockchain component* represents a blockchain that has been registered at the Blockchain Manager and contains the following subcomponents:
* Metric Collector
* Data Access Service

A blockchain’s *Metric Collector* is responsible for downloading new blocks from the network, for calculating the values of the metrics and for exposing these values through Observables.

The second subcomponent of the Blockchain component is the *Data Access Service*. The Data Access Service hides internal blockchain-specific implementation details and exposes interface methods for read and write operations. The Blockchain Manager write data to or reads data from the blockchain through these interface methods. Thus, the Blockchain Manager is agnostic to blockchain-specific details.

### External Data Sources
External data sources are components that provide relevant information for the framework but they are not part of the framework itself. For the Blockchain Manager, it is not relevant where the data come from, i.e., it is irrelevant if data is requested from public services, from public nodes or from a network node that runs on a private infrastructure, since the Metric Collector translates the received data into a neutral format that can be processed by the Blockchain Manager. We tested the reference implementation running a private network node for each supported blockchain. We used the following nodes:
* Bitcoin: Bitcore 4.1.1
* Ethereum and Ethereum Classic: Parity 2.0.1
* Expanse: Gexp 1.7.2

# Demo
We provide demo code that shows how to use the framework. The code is located under src⁩/main⁩/java⁩/com/ieee19/bc/interop/pf⁩/demo/Demo.java.

# License
This reference implementation is released under the Apache License Version 2.0.
