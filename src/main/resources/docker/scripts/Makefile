ROOT_DIR := ../../../../../
VERSAO := $(shell cd $(ROOT_DIR); mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

TIMEZONE := "America/Sao_Paulo"
CONFFILE := "cointrader.conf"




#### -------- Maven WAR --------

package_war:
	cd $(ROOT_DIR) && mvn -U clean compile assembly:single




#### -------- Local Image --------

# -- Docker image --

publish_local_image:
	cd $(ROOT_DIR) && docker build -t nucleodevel/cointrader:latest -t nucleodevel/cointrader:$(VERSAO) .


# -- Container --

start_container_local_image:
	export CONFFILE=$(CONFFILE) && export TIMEZONE=$(TIMEZONE) && cd $(ROOT_DIR) && docker-compose -f src/main/resources/docker/compose/container-local-image-docker-compose.yml up


# -- Service --

log_service_local_image:
	docker service logs cointrader_app --raw --tail 200 -f	

stop_service_local_image:
	docker stack rm cointrader	
	
start_service_local_image:
	export CONFFILE=$(CONFFILE) && export TIMEZONE=$(TIMEZONE) && cd $(ROOT_DIR) && docker stack deploy -c src/main/resources/docker/compose/service-local-image-docker-compose.yml cointrader
	
update_service_local_image:
	docker service update cointrader_app --force
