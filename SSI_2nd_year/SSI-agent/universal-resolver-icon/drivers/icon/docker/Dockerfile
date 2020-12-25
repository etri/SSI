# Dockerfile for universalresolver/driver-did-icon

FROM universalresolver/base-alpine
MAINTAINER Woosik Lee <woosik@iconloop.com>

# build driver-did-stack

ADD . /opt/driver-did-icon
RUN cd /opt/driver-did-icon && mvn clean install package -N -DskipTests

# variables

ENV uniresolver_driver_did_icon_node_url=https://bicon.net.solidwallet.io/api/v3
ENV uniresolver_driver_did_icon_score_addr=cxf800a0c1c351bde3efd4af8c79fcb77b585e7de5
ENV uniresolver_driver_did_icon_network_id=3

# done

EXPOSE 8091

RUN chmod a+rx /opt/driver-did-icon/docker/run-driver-did-icon.sh
CMD "/opt/driver-did-icon/docker/run-driver-did-icon.sh"
