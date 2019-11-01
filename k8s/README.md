# Kubernetes setup

This setup uses [fabrikate](https://github.com/microsoft/fabrikate) project.

Local install:
 - Download the latest [relase of fabrikate](https://github.com/microsoft/fabrikate/releases) unzip and move the `/usr/local/bin` dir
 - Install helm with `brew install kubernetes-helm`
 - Install kubectl with `brew install kubectl`
 - Install minikube with `brew install minikube`
 - Start minikube with `minikube start`
 - Init the remote dependencies `fab install`
 - Generate k8s yamls `fab generate local`
 - Go inside the generated dir with `cd generated/local`
 - Apply the yamls with `kubectl apply --recursive -f .`
 
 
Interesting commands:
 - `kubectl cluster-info`
 - `minikube dashboard`
 - grafana
   - `kubectl get secret --namespace grafana grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo`
   - `export POD_NAME=$(kubectl get pods --namespace grafana -l "app=grafana,release=grafana" -o jsonpath="{.items[0].metadata.name}")`
   - `kubectl --namespace monitoring port-forward $POD_NAME 3000`
 - kibana
   - `export POD_NAME=$(kubectl get pods --namespace kibana -l "app=kibana,release=kibana" -o jsonpath="{.items[0].metadata.name}")`
   - `kubectl --namespace kibana port-forward $POD_NAME 5601:5601`
 - rabbitmq
   - `kubectl get secret --namespace default rabbitmq -o jsonpath="{.data.rabbitmq-password}" | base64 --decode ; echo`
   - `export POD_NAME=$(kubectl get pods --namespace default -l "app=rabbitmq,release=rabbitmq" -o jsonpath="{.items[0].metadata.name}")`
   - `kubectl --namespace default port-forward $POD_NAME 15672:15672`

Interesting urls:
 - `http://127.0.0.1:51536/api/v1/namespaces/prometheus/services/http:prometheus-server:80/proxy/targets`
 

Cleanup:
 - `minikube stop`
 - `minikube delete`
 
