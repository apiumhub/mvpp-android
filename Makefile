run-github-server:
	docker run --name github_dyson -d -v $(PWD)/app/src/test/resources/fakeserver:/api-fakes -p 8080:8080 apiumhub/dyson-quick-mockserver

start-github-server:
	docker start github_dyson

stop-github-server:
	docker stop github_dyson

remove-github-server:
	docker stop github_dyson && docker rm github_dyson