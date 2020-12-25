## Overview

This is Project for developement of decentralized self-sovereign identity management technology using blockchain. This project is developed by ETRI, Coinplug, Iconloop, BD, POSA, KFTC, and Koscom, and is composed of three components listed below:

- SSI-app: SSI application to manage DIDs and VCs/VPs, Shopping mall application to buy/sell the luxury product by using DIDs and VCs/VPs, Stock report application to manage the stock report by using DIDs and VCs/VPs
- SSI-agent: DID resolver to support resolve DIDs of Metadium, ICON, and Indy, SSI agent to manage friend list in SSI app, vault to backup and recover keys, DIDs, and VCs.
- SSI-blockchain: Blockchain storage for DIDs. The blockchain storages are composed of Metadium, ICON, and indy-sdk (Indy-node blockchain can be acquired from the official repository of Indy). 
Please refer the directories in this repository if you want to see more details.

## How to run
Install as follow order: SSI-blockchain -> SSI-agent -> SSI-app. Refer the detail guide of installation included in each directiry.
