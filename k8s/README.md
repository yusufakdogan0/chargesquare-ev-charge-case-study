# Kubernetes Minimal Deployment

This directory contains the minimum Kubernetes manifests required to run the `session-service`, `station-service`, and `postgres` database in a Kubernetes cluster, matching the docker-compose setup without relying on advanced Kubernetes features.

## Manifest Overview

- **`configmap.yaml`**: Contains the shared, non-sensitive environment variables such as the database name (`POSTGRES_DB`) and user (`POSTGRES_USER`). It avoids hardcoding configuration into each deployment individually where shared.
- **`postgres.yaml`**: Deploys the PostgreSQL 16 database using a `Deployment` and exposes it internally via a `ClusterIP` Service on port 5432.
- **`station-service.yaml`**: Deploys the `station-service` backend and exposes it internally via a `ClusterIP` Service on port 8081.
- **`session-service.yaml`**: Deploys the `session-service` backend and exposes it internally via a `ClusterIP` Service on port 8082.

## Important Notes on Security & Deployment

1. **Sensitive Variables (Action Required):** To adhere to the minimum constraints, Kubernetes `Secret` resources were not used. Instead, placeholders (`PLACEHOLDER_CHANGE_ME`) are used for `POSTGRES_PASSWORD` and `JWT_SECRET` in the Deployment manifests. **You must replace these placeholders with secure values before deploying.**
2. **Local Images:** The `station-service` and `session-service` manifests assume the Docker images `station-service:latest` and `session-service:latest` exist locally on the Kubernetes nodes. They use `imagePullPolicy: Never` to prevent Kubernetes from attempting to download them from a remote registry. Make sure to build the images (e.g. `docker build -t station-service:latest ./station-service`) or load them into your cluster environment before deploying.
3. **No External Exposure:** The services are exposed using `ClusterIP`, making them reachable only within the cluster (as per minimum exposure requirements). To test the APIs from your local machine, use `kubectl port-forward`:
   ```sh
   kubectl port-forward svc/session-service 8082:8082
   kubectl port-forward svc/station-service 8081:8081
   ```

## Validation

These manifests have been validated using `kubectl`'s client-side dry-run feature. They were **not** deployed to a running cluster, as permitted by the case study minimum constraints.

To validate them yourself without making cluster changes, run:
```sh
kubectl apply --dry-run=client -f k8s/
```
