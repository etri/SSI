![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/decentralized-identity.github.io/master/images/logo-small.png)

# Universal Resolver Driver: did:icon

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:stack** identifiers.

## Specifications

* [Decentralized Identifiers](https://w3c-ccg.github.io/did-spec/)
* Blockicon DID Method Specification (missing)

## Example DIDs

```
did:icon:02:1b7aef9ba939d5730e2c2a52fe35578fe8d7a67ac137c4c8
```

## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t universalresolver/driver-did-icon
docker run -p 8080:8080 universalresolver/driver-did-icon
curl -X GET http://localhost:8080/1.0/identifiers/did:icon:02:1b7aef9ba939d5730e2c2a52fe35578fe8d7a67ac137c4c8
```

## Build (native Java)

 1. First, build https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/java

Then run:

	mvn clean install

## Driver Environment Variables

The driver recognizes the following environment variables:

(none)

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

(none)
