ROOT_DIR := ../../../../
VERSION := $(shell cd $(ROOT_DIR); mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

TIMEZONE := "America/Sao_Paulo"
CONFFILE := "cointrader.conf"




#### -------- Maven WAR --------

package_jar:
	cd $(ROOT_DIR) && mvn -U clean compile assembly:single
	
	


#### -------- Local Image --------

# -- Docker image --

publish_local_image:
	cd $(ROOT_DIR) && docker build -t nucleodevel/cointrader:latest -t nucleodevel/cointrader:$(VERSION) .


# -- Container --

start_container_local_image:
	export CONFFILE=$(CONFFILE) && export TIMEZONE=$(TIMEZONE) && cd $(ROOT_DIR) && docker compose -f src/main/resources/docker/compose/container-local-image-docker-compose.yml up


# -- Stack --

log_stack_local_image:
	docker service logs cointrader_app --raw --tail 200 -f	

stop_stack_local_image:
	docker stack rm cointrader	
	
start_stack_local_image:
	export CONFFILE=$(CONFFILE) && export TIMEZONE=$(TIMEZONE) && cd $(ROOT_DIR) && docker stack deploy -c src/main/resources/docker/compose/stack-local-image-docker-compose.yml cointrader
	
update_stack_local_image:
	docker service update cointrader_app --force
