SHELL := /bin/bash
.PHONY: test install run help

JAVA_VERSION := "$(shell java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)"

help: ## Show this help message.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m\033[0m\n"} /^[$$()% a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

# test: ## running all test
# ifneq ($(JAVA_VERSION), "17")
# 	@echo "[ERROR] Your java version is '$(JAVA_VERSION)'. It should be greater than 17" && exit 1
# endif
# 	@./mvnw clean verify -P native

run: ## up the API and load data 	@[ "$$JAVA_VERSION" -lt "17" ] && echo "$JAVA_VERSION should be greater than 17" && exit 1 @if [ $(JAVA_VERSION) -lt "17e" ]; then
ifneq ($(JAVA_VERSION), "17")
	@$(error "[ERROR] Your java version is $(JAVA_VERSION). It should be greater than 17") && exit 1;
endif
	@./mvnw spring-boot:run -Dspring.profiles.active=dev

start: ## run project
	@API_PROFILE='dev' docker-compose down
	@API_PROFILE='dev' docker-compose up -d
	@docker-compose logs -f webhook

restart: ## restart project
	@API_PROFILE='dev' docker-compose stop webhook # used to rebuild API after modification
	@API_PROFILE='dev' docker-compose up -d
	@docker-compose logs -f webhook

kill: ## kill project
	@if [ -n "$(shell docker ps -a -q)" ]; then echo "Removing containers..." && docker rm -f $(shell docker ps -a -q); fi;
	@if [ -n "$(shell docker volume ls -q)" ]; then echo "Removing volumes..." && docker volume rm $(shell docker volume ls -q); fi;