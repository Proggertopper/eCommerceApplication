# Kubernetes Local Deployment

This folder contains a simple Kubernetes setup for local practice and resume/demo purposes. It is intentionally plain YAML, without Helm, so the main Kubernetes objects are easy to read.

## What Is Included

- `namespace.yaml` - dedicated namespace for the project.
- `secrets.yaml` - demo credentials for local development.
- `configmap.yaml` - shared non-secret environment variables.
- `infra.yaml` - PostgreSQL, MySQL, MongoDB, RabbitMQ, Redis, Zookeeper, Kafka, Keycloak, Zipkin.
- `apps.yaml` - config server, Eureka, gateway, product, user, order, notification services.

## Kubernetes Concepts Used Here

- **Namespace**: separates this project from other workloads in the cluster.
- **Secret**: stores sensitive values such as database passwords. The values here are only demo values.
- **ConfigMap**: stores non-secret configuration.
- **Deployment**: tells Kubernetes how many replicas of a container should run and how to restart them.
- **Service**: gives a stable DNS name and virtual IP to pods. For example, the product service can connect to PostgreSQL through `postgres:5432`.
- **Readiness probe**: tells Kubernetes when a pod is ready to receive traffic.
- **Liveness probe**: tells Kubernetes when a pod should be restarted.

## Build Local Images

From the repository root:

```bash
mvn clean package
```

For Minikube on PowerShell, build images directly inside the Minikube Docker daemon:

```powershell
minikube docker-env | Invoke-Expression

docker build -t ecommerce-configserver:latest ./configserver
docker build -t ecommerce-eureka:latest ./eureka
docker build -t ecommerce-gateway:latest ./gateway
docker build -t ecommerce-product:latest ./product
docker build -t ecommerce-user:latest ./user
docker build -t ecommerce-order:latest ./order
docker build -t ecommerce-notification:latest ./notification
```

For Kind, build images normally and then load them:

```bash
docker build -t ecommerce-configserver:latest ./configserver
docker build -t ecommerce-eureka:latest ./eureka
docker build -t ecommerce-gateway:latest ./gateway
docker build -t ecommerce-product:latest ./product
docker build -t ecommerce-user:latest ./user
docker build -t ecommerce-order:latest ./order
docker build -t ecommerce-notification:latest ./notification

kind load docker-image ecommerce-configserver:latest
kind load docker-image ecommerce-eureka:latest
kind load docker-image ecommerce-gateway:latest
kind load docker-image ecommerce-product:latest
kind load docker-image ecommerce-user:latest
kind load docker-image ecommerce-order:latest
kind load docker-image ecommerce-notification:latest
```

## Apply Manifests

```bash
kubectl apply -f deploy/k8s/namespace.yaml
kubectl apply -f deploy/k8s/secrets.yaml
kubectl apply -f deploy/k8s/configmap.yaml
kubectl apply -f deploy/k8s/infra.yaml
kubectl apply -f deploy/k8s/apps.yaml
```

Check status:

```bash
kubectl get pods -n ecommerce
kubectl get svc -n ecommerce
```

Watch pods until they are running:

```bash
kubectl get pods -n ecommerce -w
```

## Open the Application

Forward the gateway:

```bash
kubectl port-forward -n ecommerce svc/gateway 8080:8080
```

Then open:

- Gateway: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

Forward other useful services:

```bash
kubectl port-forward -n ecommerce svc/eureka 8761:8761
kubectl port-forward -n ecommerce svc/keycloak 8181:8080
kubectl port-forward -n ecommerce svc/zipkin 9411:9411
```

## Troubleshooting

Show pods:

```bash
kubectl get pods -n ecommerce
```

Read logs:

```bash
kubectl logs -n ecommerce deploy/gateway
kubectl logs -n ecommerce deploy/product-service
```

Describe a pod when it is stuck:

```bash
kubectl describe pod -n ecommerce <pod-name>
```

Delete everything:

```bash
kubectl delete namespace ecommerce
```

## Notes

This setup is for local learning. It uses demo credentials, single replicas, and simple in-cluster infrastructure. A production setup would normally use managed databases, external secrets, persistent volumes, ingress/TLS, resource limits tuned per service, and proper Keycloak realm import.
