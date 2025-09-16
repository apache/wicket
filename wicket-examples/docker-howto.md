# How to update the Docker image used for the demo

## Build 

```
docker build --tag apache.jfrog.io/wicket-docker/wicket-examples:LATEST-10 .
```

## Push

1. Login

```
docker login apache.jfrog.io
```
Enter your ASF id and password when prompted.

2. Push

```
docker push apache.jfrog.io/wicket-docker/wicket-examples:LATEST-10
```

3. Optional: You could verify at https://apache.jfrog.io/ui/packages/docker:%2F%2Fwicket-examples/LATEST-10?projectKey=wicket