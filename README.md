## Overview
_SSI Project_ is a research project funded by national government (Full name of the project is "Development of Distributed Self Sovereign Identity Management Technology using Blockchain). The SSI Project aims to develop a identity management system that enables people to manage their identity by themselves. This project is developed by ETRI, Coinplug, Iconloop, BD, POSA, KFTC, and Koscom, and is composed of three components listed below The project was started in April 2019 and finished in December 2020.

Each _SSI_1st_year_ and _SSI_2nd_year_ directory works with follow scenarios:
* **SSI_1st_year**
  * The owner requests the government ID with his DID, and the government issues the owner's government ID as a VC with the government’s signature. 
  * The owner applies for a job with the government ID VC. If the company verify the VC with the owners DID and the government’s signature, the company issues a job certificate VC to the owner.
  * The owner applies for a loan with the government ID VC and the job certificate VC. The bank allows the application if both VCs are valid.

* **SSI_2nd_year**
  * Luxury shopping mall scenario: The owner is able to buy both new and used luxury item from the luxury shopping mall. The owner is possible to get the certificates to prove the ownership of the new item that issued by the shopping mall server after buying a new item. The owner also gets the certificate to track the buying/selling history of the item that issued by the shopping mall server. The owner is also able to buy/sell the used item. In the case of trading the used item, the seller issues these certificates to the buyer directly. 
  * Applying for a job: The owner is able to get the transcript VC with his DIDs and VCs. After getting the transcript VC, the owner applies for a job with his transcript VC and other VCs. The transcript VC support range proof and selective disclosure, so that the owner does not need to fully disclose the attributes in the transcript certificate. After that the company verifies the VCs, the company issues a job certificate VC to the owner. The company is able to revoke the certificate if the owner quit the job. 
  * DID login: The owner is able to use his DID when he does sign-up a website. When the owner tries to login the website, he proves the ownership of his DID to the website via DID Auth. If the ownership of the DID is verified, the owner is able to login the website. 
  * Stock report: The owner is able to issue the VC to the stock company to prove that the owner receives and reads the stock report.


### Authors
Please refer [AUTHORS.md](AUTHORS.md).

### License
Please refer License.md in each directory.

### Acknowledgement
This work was supported by the Institute for Information & communications Technology Promotion(IITP) grant funded by the Korea government(MSIT) (Development of Decentralized Self Sovereign Identity Management Technology using Blockchain)
