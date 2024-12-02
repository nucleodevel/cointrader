ROOT_DIR := ../../../../
VERSION := $(shell cd $(ROOT_DIR); mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

TIMEZONE := "America/Sao_Paulo"



#### -------- Maven WAR --------

package_jar:
	cd $(ROOT_DIR) && mvn -U clean compile assembly:single
	
	


#### -------- Local Image --------

# -- Docker image --

publish_local_image:
	cd $(ROOT_DIR) && docker build -t nucleodevel/cointrader:latest -t nucleodevel/cointrader:$(VERSION) .


#### -------- Mercado Bitcoin --------

# -- Container --

start_container_mercadobitcoin_local_image:
	export HOSTNAME=mercadobitcoin && export CONFFILE=mercadobitcoin.conf && export TIMEZONE=$(TIMEZONE) && cd $(ROOT_DIR) && docker compose -f src/main/resources/docker/compose/container-local-image-docker-compose.yml up


# -- Stack --

log_stack_mercadobitcoin_local_image:
	docker service logs mercadobitcoin_app --raw --tail 200 -f	

stop_stack_mercadobitcoin_local_image:
	docker stack rm mercadobitcoin	
	
start_stack_mercadobitcoin_local_image:
	export HOSTNAME=mercadobitcoin && export CONFFILE=mercadobitcoin.conf && export TIMEZONE=$(TIMEZONE) && cd $(ROOT_DIR) && docker stack deploy -c src/main/resources/docker/compose/stack-local-image-docker-compose.yml mercadobitcoin
	
update_stack_mercadobitcoin_local_image:
	docker service update mercadobitcoin_app --force


#### -------- Foxbit --------

# -- Container --

start_container_foxbit_local_image:
	export HOSTNAME=foxbit && export CONFFILE=foxbit.conf && export TIMEZONE=$(TIMEZONE) && cd $(ROOT_DIR) && docker compose -f src/main/resources/docker/compose/container-local-image-docker-compose.yml up


# -- Stack --

log_stack_foxbit_local_image:
	docker service logs foxbit_app --raw --tail 200 -f	

stop_stack_foxbit_local_image:
	docker stack rm foxbit	
	
start_stack_foxbit_local_image:
	export HOSTNAME=foxbit && export CONFFILE=foxbit.conf && export TIMEZONE=$(TIMEZONE) && cd $(ROOT_DIR) && docker stack deploy -c src/main/resources/docker/compose/stack-local-image-docker-compose.yml foxbit
	
update_stack_foxbit_local_image:
	docker service update foxbit_app --force
