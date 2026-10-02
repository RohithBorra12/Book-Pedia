# Book Store — a basic microservices project for learning

A small, working project that touches every tool you'll be asked about in interviews:
**Java 8 → 21, Spring Boot 3, microservices, JWT + role-based security, PostgreSQL, MySQL, MongoDB,
Redis, Kafka, Resilience4j circuit breaker, Prometheus, Grafana, ELK (Kibana), Docker, Kubernetes,
Jenkins CI/CD and AWS.**

The business logic is deliberately simple (log in, see books, place an order) so you can focus on
*how each technology works and where it's used*.

---

## 1. Architecture

```
                        ┌──────────────────────────┐
   Browser ───────────► │  frontend (React + nginx) │  :3000
                        └────────────┬─────────────┘
              /api/auth, /api/users  │  /api/books        /api/orders
            ┌────────────────────────┼──────────────────────────┐
            ▼                        ▼                          ▼
   ┌────────────────┐      ┌──────────────────┐   REST + ┌──────────────────┐
   │  user-service  │      │   book-service   │◄─circuit─│  order-service   │
   │   :8081        │      │   :8082          │  breaker │   :8083          │
   │ JWT login,     │      │ catalog, Redis   │          │ orders           │
   │ roles          │      │ cache            │          │                  │
   └───────┬────────┘      └───┬─────────┬────┘          └───┬──────────┬───┘
           ▼                   ▼         ▼                   ▼          │
      PostgreSQL           MongoDB     Redis               MySQL        │ publishes
                               ▲                                        ▼
                               └──── consumes ◄──── Kafka topic "order-events"

   Every service ──► /actuator/prometheus ──► Prometheus ──► Grafana   (metrics)
   Every service ──► Logstash ──► Elasticsearch ──► Kibana               (logs)
```

**Flow when you place an order**

1. Frontend sends `POST /api/orders` with your JWT.
2. order-service checks the token (same secret as user-service, no extra call needed).
3. order-service calls book-service over REST to get price and stock — wrapped in a **circuit breaker**.
4. Order is saved in **MySQL**.
5. order-service publishes an `OrderEvent` to **Kafka**.
6. book-service consumes the event, reduces stock in **MongoDB**, and clears the **Redis** cache.

---

## 2. What you need installed

| Tool | Why | Check |
|---|---|---|
| Docker Desktop (give it 6–8 GB RAM) | Runs everything | `docker --version` |
| Java 21 + Maven 3.9 | Only if you run services outside Docker | `java -version`, `mvn -v` |
| Node 20 | Only if you run the frontend outside Docker | `node -v` |
| Minikube + kubectl | Kubernetes step | `minikube version` |
| AWS CLI + eksctl | AWS step | `aws --version` |

---

## 3. Run it (one command)

```bash
docker compose up --build
```

First build takes a few minutes (Maven downloads dependencies). Then open:

| What | URL | Login |
|---|---|---|
| App | http://localhost:3000 | `admin` / `admin123` (or register a new user) |
| Swagger — users | http://localhost:8081/swagger-ui.html | paste token in "Authorize" |
| Swagger — books | http://localhost:8082/swagger-ui.html | |
| Swagger — orders | http://localhost:8083/swagger-ui.html | |
| Prometheus | http://localhost:9090 | |
| Grafana | http://localhost:3001 | `admin` / `admin` → Dashboards → Bookstore → Bookstore Overview |

**With logging (Kibana):** needs about 3 GB more RAM.

```bash
docker compose --profile logging up --build
```

Kibana: http://localhost:5601 → Menu → Discover → "Create data view" → index pattern `bookstore-logs-*`
→ now search e.g. `service : "order-service"` or `message : *circuit*`.

**Stop everything:** `docker compose down` (add `-v` to also delete data).

### Run a service from your IDE instead (good for debugging)

```bash
docker compose up -d postgres mysql mongo redis kafka   # only the infrastructure
cd user-service && mvn spring-boot:run                    # repeat for the other services
cd frontend && npm install && npm run dev                 # http://localhost:5173
```

The `application.yml` files default to `localhost`, so nothing needs to change.

---

## 4. Try each concept yourself

**Role-based security**
- Log in as a normal user → no "Add a book" or "Delete" buttons, and `GET /api/users` returns **403**.
- Log in as admin → you can add/delete books and see everyone's orders.
- Call any API with no token → **401**.

**Redis cache**
- Call `GET /api/books` twice. book-service logs `Cache MISS - loading books from MongoDB` only the first time.
- Peek inside Redis: `docker compose exec redis redis-cli keys '*'`

**Kafka (event-driven)**
- Order a book. Watch the logs: order-service says `Published order ...`, book-service says `Received order event` and the stock drops.
- See the messages: `docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-events --from-beginning`

**Circuit breaker**
1. `docker compose stop book-service`
2. Place an order from Swagger or `api-requests.http` → it's saved as **PENDING** instead of failing.
3. Check http://localhost:8083/actuator/circuitbreakers → after a few failures the state becomes **OPEN**.
4. `docker compose start book-service` → after 10 seconds it goes HALF_OPEN, then CLOSED.

**Dead Letter Topic**
- Send bad JSON to Kafka: `docker compose exec kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic order-events` and type `hello`.
- book-service retries twice, then moves it to `order-events.DLT`.

**Monitoring**
- Click around the app, then open the Grafana dashboard: requests/sec, response time, errors, JVM memory, circuit breaker state.
- In Prometheus try queries like `http_server_requests_seconds_count`.

**Unit tests** (JUnit 5 + Mockito): `cd order-service && mvn test`

---

## 5. Where each concept lives (use this to revise)

### Java features

| Version | Feature | File |
|---|---|---|
| 8 | Lambdas, Streams, method references | `AuthService.findAll`, `BookService.titlesByAuthor`, `OrderService.summary` |
| 8 | Optional | `AuthService.login`, `BookService.getById` |
| 8 | CompletableFuture | `OrderEventPublisher.publish` |
| 8 | java.time | `Order.createdAt` |
| 9 | `List.of` | book-service `DataSeeder` |
| 10 | `var` | `OrderService.placeOrder`, `JwtAuthFilter` |
| 11 | `String.strip()` | `AuthService.register` |
| 14 | Switch expressions | `OrderStatus.description` |
| 15 | Text blocks | user-service `DataSeeder` |
| 16 | Records | all `dto` packages, `OrderEvent` |
| 16 | Pattern matching `instanceof` | `BookClient.fallback` |
| 16 | `Stream.toList()` | `AuthService`, `OrderService` |
| 17 | Sealed interfaces | `BookLookup` |
| 21 | Pattern matching for switch, record patterns, `when` guards | `OrderService.placeOrder` |
| 21 | Sequenced collections (`getFirst`) | `OrderService.summary` |
| 21 | Virtual threads | `spring.threads.virtual.enabled: true` in every `application.yml` |

### Spring and tools

| Concept | File(s) |
|---|---|
| REST controllers, validation (`@Valid`) | `*Controller.java`, `dto/*` |
| Global exception handling | `exception/GlobalExceptionHandler.java` |
| Spring Data JPA (PostgreSQL, MySQL) | `UserRepository`, `OrderRepository` |
| Spring Data MongoDB | `Book`, `BookRepository` |
| JWT creation and validation | `security/JwtUtil.java`, `security/JwtAuthFilter.java` |
| Stateless security, 401 vs 403 | `config/SecurityConfig.java` |
| Role-based access (`@PreAuthorize`) | `BookController`, `OrderController`, `UserController` |
| Password hashing (BCrypt) | user-service `SecurityConfig`, `AuthService` |
| Redis caching (`@Cacheable`, `@CacheEvict`) | `BookService` |
| Kafka producer | `OrderEventPublisher`, `KafkaTopicConfig` |
| Kafka consumer, retries, Dead Letter Topic | `OrderEventListener`, book-service `KafkaConfig` |
| Service-to-service REST (`RestClient`) | `BookClient` |
| Circuit breaker + fallback | `BookClient`, `resilience4j` section in order-service `application.yml` |
| Metrics (Actuator + Micrometer) | `management` section in `application.yml`, `monitoring/prometheus.yml` |
| Grafana dashboard as code | `monitoring/grafana/provisioning/` |
| Centralized JSON logging | `logback-spring.xml`, `monitoring/logstash/logstash.conf` |
| API docs (Swagger/OpenAPI) | `config/OpenApiConfig.java` |
| Unit tests (JUnit 5 + Mockito) | `src/test/...` in each service |
| Multi-stage Docker builds | each `Dockerfile` |
| Local environment | `docker-compose.yml` |
| K8s Deployment, Service, ConfigMap, Secret, probes, HPA | `k8s/apps.yaml`, `k8s/infra.yaml` |
| CI/CD | `Jenkinsfile` |
| AWS EKS and ECR | `aws/` |

---

## 6. How code goes from your laptop to production (company style)

```
feature/add-wishlist ──PR + code review──► develop ──────────────► main
        │                                     │                       │
   Jenkins: build + test               Jenkins: build, test,     Jenkins: build, test,
                                       push image, deploy DEV    push image, WAIT FOR
                                                                 APPROVAL, deploy PROD
```

Typical day:

```bash
git checkout develop && git pull
git checkout -b feature/add-wishlist
# ...write code + tests...
mvn test
git add . && git commit -m "Add wishlist endpoint"
git push -u origin feature/add-wishlist
# open a Pull Request into develop -> review -> merge -> Jenkins deploys to DEV automatically
# later: PR from develop into main -> merge -> Jenkins asks for approval -> deploys to PROD
```

The **same Docker image** (tagged with the Jenkins build number) is promoted from DEV to PROD.
Only the configuration differs: each environment is its own namespace (`bookstore-dev`, `bookstore-prod`)
with its own ConfigMap and Secret.

---

## 7. Kubernetes on your laptop (Minikube)

```bash
minikube start --cpus=4 --memory=8192
minikube addons enable metrics-server        # for the autoscaler
./k8s/build-local-images.sh                  # builds images inside Minikube
./k8s/deploy.sh dev local latest             # deploys to namespace bookstore-dev
kubectl get pods -n bookstore-dev -w         # wait until all are Running and READY 1/1
minikube service frontend -n bookstore-dev   # opens the app in your browser
```

Useful commands to practise:

```bash
kubectl logs deploy/order-service -n bookstore-dev -f
kubectl describe pod <pod-name> -n bookstore-dev
kubectl scale deploy/book-service --replicas=2 -n bookstore-dev
kubectl rollout restart deploy/book-service -n bookstore-dev
kubectl rollout undo deploy/book-service -n bookstore-dev     # rollback
kubectl delete namespace bookstore-dev                        # clean up
```

---

## 8. Jenkins

1. Run Jenkins locally: `docker run -d -p 8080:8080 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts`
   (for real Docker builds the agent also needs Docker, Maven, JDK 21 and kubectl installed).
2. Install the plugins: Pipeline, Git, Docker Pipeline, Credentials Binding.
3. Add a credential with ID `docker-registry` (Docker Hub username + access token).
4. Edit `REGISTRY` at the top of the `Jenkinsfile`.
5. Create a **Multibranch Pipeline** job pointing at your GitHub repo. Jenkins discovers the
   `feature/*`, `develop` and `main` branches automatically and runs the right stages for each.

---

## 9. Deploy to AWS (do this last — it costs money)

> EKS + 2 EC2 nodes + a load balancer cost roughly a few US dollars per day. Delete the cluster when you finish.

```bash
aws configure                                   # your access key, secret, region
./aws/setup-ecr.sh ap-south-1                   # creates ECR repos, prints your REGISTRY

# build and push images
REGISTRY=<printed registry>
for s in user-service book-service order-service frontend; do
  docker build -t $REGISTRY/bookstore-$s:v1 $s
  docker push   $REGISTRY/bookstore-$s:v1
done

eksctl create cluster -f aws/eksctl-cluster.yaml   # ~15-20 minutes, also configures kubectl
./k8s/deploy.sh prod $REGISTRY v1
kubectl get svc frontend -n bookstore-prod          # EXTERNAL-IP is your public URL

# CLEAN UP
eksctl delete cluster -f aws/eksctl-cluster.yaml
```

**Going further on AWS (what real companies do):** replace the in-cluster databases with
**RDS** (PostgreSQL, MySQL), **ElastiCache** (Redis), **MSK** (Kafka) and **DocumentDB** or MongoDB Atlas.
You only change the URLs in the ConfigMap in `k8s/apps.yaml`. Store passwords in
**AWS Secrets Manager**, view logs in **CloudWatch**, and create the whole setup with **Terraform**.

---

## 10. Interview questions this project prepares you for

- Why does each service have its own database? (database-per-service)
- How does book-service trust a token it didn't create? (shared signing key, stateless JWT)
- Difference between 401 and 403? Where is each produced here?
- Why use Kafka instead of calling book-service to reduce stock? (loose coupling, retries, async)
- What happens if Kafka is down? What is a Dead Letter Topic?
- Explain the circuit breaker states CLOSED → OPEN → HALF_OPEN.
- When does `@Cacheable` NOT work? (calling the method from inside the same class — no proxy)
- What are virtual threads and why do they help a blocking REST app?
- Sealed interface + pattern-matching switch: why no `default` needed?
- Liveness vs readiness probes?
- How does the same image go from DEV to PROD with different settings?
- Pull (Prometheus) vs push (Logstash) — metrics vs logs?

## 11. Ideas to extend later

Spring Cloud Gateway as a single entry point · Eureka/Config Server · distributed tracing with Zipkin ·
Flyway migrations · Testcontainers integration tests · Helm charts · Terraform for AWS ·
Saga pattern (cancel order if stock is gone) · Keycloak / OAuth2 instead of custom JWT.
